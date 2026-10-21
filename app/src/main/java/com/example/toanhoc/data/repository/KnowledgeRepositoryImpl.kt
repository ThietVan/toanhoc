package com.example.toanhoc.data.repository

import com.example.toanhoc.data.local.dao.MathAcademicDao
import com.example.toanhoc.data.local.dao.StudentProfileDao
import com.example.toanhoc.data.local.entity.DailySessionLogEntity
import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity
import com.example.toanhoc.data.local.entity.StudentProfileEntity

class KnowledgeRepositoryImpl(
    private val dao: MathAcademicDao,
    private val profileDao: StudentProfileDao? = null
) : KnowledgeRepository {

    override suspend fun getNodesForGrade(grade: Int): List<KnowledgeNodeEntity> {
        return dao.getNodesForGrade(grade)
    }

    override suspend fun getNodeById(nodeId: String): KnowledgeNodeEntity? {
        return dao.getNodeById(nodeId)
    }

    override suspend fun seedKnowledgeNodes(
        nodes: List<KnowledgeNodeEntity>,
        edges: List<PrerequisiteEdgeEntity>
    ) {
        dao.insertNodes(nodes)
        dao.insertEdges(edges)
    }

    override suspend fun getReadyToLearnNodesForGrade(grade: Int): List<KnowledgeNodeEntity> {
        return dao.getReadyToLearnNodesForGrade(grade)
    }

    override suspend fun getPrerequisiteParentNodes(childNodeId: String): List<KnowledgeNodeEntity> {
        return dao.getPrerequisiteParentNodes(childNodeId)
    }

    override suspend fun getNodesDueForReviewOrWithMisconceptions(currentTimeMs: Long): List<KnowledgeNodeEntity> {
        return dao.getNodesDueForReviewOrWithMisconceptions(currentTimeMs)
    }

    override suspend fun getStudentMastery(nodeId: String): StudentMasteryEntity? {
        return dao.getStudentMastery(nodeId)
    }

    override suspend fun recordAttemptResult(
        nodeId: String,
        isCorrect: Boolean,
        responseLatencyMs: Long,
        bugTag: String?
    ) {
        dao.recordAttemptResult(nodeId, isCorrect, responseLatencyMs, bugTag)
    }

    override suspend fun resetNodeMasteryForRelearning(nodeId: String) {
        dao.resetNodeMasteryForRelearning(nodeId)
    }

    override suspend fun saveDailySession(sessionLog: DailySessionLogEntity) {
        dao.upsertDailySessionLog(sessionLog)
    }

    override suspend fun getDailySession(dateIso: String): DailySessionLogEntity? {
        return dao.getDailySessionLog(dateIso)
    }

    override suspend fun getStudentProfile(): StudentProfileEntity {
        return profileDao?.getProfile() ?: StudentProfileEntity()
    }

    override suspend fun saveStudentProfile(profile: StudentProfileEntity) {
        profileDao?.saveProfile(profile)
    }

    override suspend fun getMisconceptionLogs(): List<MisconceptionLogEntity> {
        return dao.getAllMisconceptionLogs()
    }
}
