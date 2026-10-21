package com.example.toanhoc.data.repository

import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import com.example.toanhoc.data.local.entity.StudentProfileEntity

interface KnowledgeRepository {
    suspend fun getNodesForGrade(grade: Int): List<KnowledgeNodeEntity>
    suspend fun getNodeById(nodeId: String): KnowledgeNodeEntity?
    suspend fun seedKnowledgeNodes(nodes: List<KnowledgeNodeEntity>, edges: List<PrerequisiteEdgeEntity>)
    suspend fun getReadyToLearnNodesForGrade(grade: Int): List<KnowledgeNodeEntity>
    suspend fun getPrerequisiteParentNodes(childNodeId: String): List<KnowledgeNodeEntity>
    suspend fun getNodesDueForReviewOrWithMisconceptions(currentTimeMs: Long = System.currentTimeMillis()): List<KnowledgeNodeEntity>
    suspend fun getStudentMastery(nodeId: String): StudentMasteryEntity?
    suspend fun recordAttemptResult(nodeId: String, isCorrect: Boolean, responseLatencyMs: Long, bugTag: String? = null)
    suspend fun resetNodeMasteryForRelearning(nodeId: String)
    suspend fun saveDailySession(sessionLog: DailySessionLogEntity)
    suspend fun getDailySession(dateIso: String): DailySessionLogEntity?
    suspend fun getStudentProfile(): StudentProfileEntity
    suspend fun saveStudentProfile(profile: StudentProfileEntity)
    suspend fun getMisconceptionLogs(): List<MisconceptionLogEntity>
}
