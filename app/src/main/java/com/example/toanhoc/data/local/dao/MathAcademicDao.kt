package com.example.toanhoc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity

@Dao
abstract class MathAcademicDao {

    // --- Basic Node CRUD ---
    @Query("SELECT * FROM knowledge_nodes WHERE grade = :grade ORDER BY intrinsicDifficulty ASC")
    abstract suspend fun getNodesForGrade(grade: Int): List<KnowledgeNodeEntity>

    @Query("SELECT * FROM knowledge_nodes WHERE nodeId = :nodeId LIMIT 1")
    abstract suspend fun getNodeById(nodeId: String): KnowledgeNodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertNodes(nodes: List<KnowledgeNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertEdges(edges: List<PrerequisiteEdgeEntity>)

    // --- 1. Ready to Learn Nodes ---
    @Query("""
        SELECT kn.* FROM knowledge_nodes AS kn
        LEFT JOIN student_mastery AS sm ON kn.nodeId = sm.nodeId
        WHERE (sm.isMastered IS NULL OR sm.isMastered = 0)
          AND kn.grade = :grade
          AND NOT EXISTS (
              SELECT 1 FROM prerequisite_edges AS pe
              LEFT JOIN student_mastery AS psm ON pe.parentNodeId = psm.nodeId
              WHERE pe.childNodeId = kn.nodeId
                AND (psm.isMastered IS NULL OR psm.isMastered = 0)
          )
        ORDER BY kn.intrinsicDifficulty ASC
    """)
    abstract suspend fun getReadyToLearnNodesForGrade(grade: Int): List<KnowledgeNodeEntity>

    // --- 2. Diagnostic Backtracking ---
    @Query("""
        SELECT kn.* FROM knowledge_nodes AS kn
        INNER JOIN prerequisite_edges AS pe ON kn.nodeId = pe.parentNodeId
        WHERE pe.childNodeId = :childNodeId
    """)
    abstract suspend fun getPrerequisiteParentNodes(childNodeId: String): List<KnowledgeNodeEntity>

    // --- 3. Due for Review & Misconception Review Nodes ---
    @Query("""
        SELECT DISTINCT kn.* FROM knowledge_nodes AS kn
        LEFT JOIN student_mastery AS sm ON kn.nodeId = sm.nodeId
        LEFT JOIN misconception_logs AS ml ON kn.nodeId = ml.nodeId
        WHERE (sm.nextReviewEpochMs > 0 AND sm.nextReviewEpochMs <= :currentTimeMs)
           OR (ml.isResolved IS NOT NULL AND ml.isResolved = 0)
    """)
    abstract suspend fun getNodesDueForReviewOrWithMisconceptions(currentTimeMs: Long): List<KnowledgeNodeEntity>

    // --- Mastery & Logs Operations ---
    @Query("SELECT * FROM student_mastery WHERE nodeId = :nodeId LIMIT 1")
    abstract suspend fun getStudentMastery(nodeId: String): StudentMasteryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertStudentMastery(mastery: StudentMasteryEntity)

    @Query("SELECT * FROM misconception_logs WHERE nodeId = :nodeId AND bugTag = :bugTag LIMIT 1")
    abstract suspend fun getMisconceptionLog(nodeId: String, bugTag: String): MisconceptionLogEntity?

    @Query("SELECT * FROM misconception_logs WHERE isResolved = 0 ORDER BY occurrenceCount DESC LIMIT 20")
    abstract suspend fun getAllMisconceptionLogs(): List<MisconceptionLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertMisconceptionLog(log: MisconceptionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertDailySessionLog(sessionLog: DailySessionLogEntity)

    @Query("SELECT * FROM daily_session_logs WHERE dateIso = :dateIso LIMIT 1")
    abstract suspend fun getDailySessionLog(dateIso: String): DailySessionLogEntity?

    // --- 4. Transaction Step Result Update ---
    @Transaction
    open suspend fun recordAttemptResult(
        nodeId: String,
        isCorrect: Boolean,
        responseLatencyMs: Long,
        bugTag: String? = null,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        val currentMastery = getStudentMastery(nodeId) ?: StudentMasteryEntity(nodeId = nodeId)

        // BKT Update calculation with bounds coercion
        val pK = currentMastery.pKnow.coerceIn(0.01f, 0.99f)
        val pS = currentMastery.pSlip.coerceIn(0.01f, 0.50f)
        val pG = currentMastery.pGuess.coerceIn(0.01f, 0.50f)
        val pT = currentMastery.pTransit.coerceIn(0.01f, 0.50f)

        val posteriorObs = if (isCorrect) {
            val pObsCorrect = (pK * (1.0f - pS)) + ((1.0f - pK) * pG)
            (pK * (1.0f - pS)) / pObsCorrect
        } else {
            val pObsWrong = (pK * pS) + ((1.0f - pK) * (1.0f - pG))
            (pK * pS) / pObsWrong
        }

        val updatedPKnow = (posteriorObs + ((1.0f - posteriorObs) * pT)).coerceIn(0.0f, 1.0f)
        val isMastered = currentMastery.isMastered || (isCorrect && updatedPKnow >= 0.70f) || (updatedPKnow >= 0.85f)

        // Spaced Repetition Memory Stability
        val newStabilityDays = if (isCorrect) {
            currentMastery.memoryStabilityDays * 2.0f
        } else {
            (currentMastery.memoryStabilityDays * 0.5f).coerceAtLeast(1.0f)
        }

        val nextReviewEpoch = currentTimeMs + (newStabilityDays * 86_400_000L).toLong()

        upsertStudentMastery(
            currentMastery.copy(
                pKnow = updatedPKnow,
                memoryStabilityDays = newStabilityDays,
                lastPracticedEpochMs = currentTimeMs,
                nextReviewEpochMs = nextReviewEpoch,
                avgLatencyMs = if (currentMastery.avgLatencyMs == 0L) responseLatencyMs else (currentMastery.avgLatencyMs + responseLatencyMs) / 2,
                isMastered = isMastered
            )
        )

        // Record Misconception if applicable
        if (!isCorrect && !bugTag.isNullOrBlank()) {
            val existingLog = getMisconceptionLog(nodeId, bugTag)
            if (existingLog != null) {
                upsertMisconceptionLog(
                    existingLog.copy(
                        occurrenceCount = existingLog.occurrenceCount + 1,
                        lastDetectedEpochMs = currentTimeMs,
                        isResolved = false
                    )
                )
            } else {
                upsertMisconceptionLog(
                    MisconceptionLogEntity(
                        nodeId = nodeId,
                        bugTag = bugTag,
                        occurrenceCount = 1,
                        lastDetectedEpochMs = currentTimeMs,
                        isResolved = false
                    )
                )
            }
        }
    }

    @Transaction
    open suspend fun resetNodeMasteryForRelearning(
        nodeId: String,
        currentTimeMs: Long = System.currentTimeMillis()
    ) {
        val currentMastery = getStudentMastery(nodeId) ?: StudentMasteryEntity(nodeId = nodeId)
        upsertStudentMastery(
            currentMastery.copy(
                pKnow = 0.30f,
                isMastered = false,
                lastPracticedEpochMs = currentTimeMs
            )
        )
    }
}
