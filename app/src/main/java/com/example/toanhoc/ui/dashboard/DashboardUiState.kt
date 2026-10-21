package com.example.toanhoc.ui.dashboard

import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.MisconceptionLogEntity
import com.example.toanhoc.data.local.entity.StudentMasteryEntity

/**
 * UI State for Academic Dashboard Screen focused on Kid's Learning Experience.
 */
data class DashboardUiState(
    val selectedGrade: Int = 1,
    val isTodayCompleted: Boolean = false,
    val todayCompletedSeconds: Int = 0,
    val todayAccuracyPercentage: Int = 0,
    val todayAvgLatencyMs: Long = 0L,
    val phase2TargetNode: KnowledgeNodeEntity? = null,
    val targetLessonTitleVi: String = "",
    val targetLessonGoalVi: String = "",
    val reviewStatusMessageVi: String = "",
    val phase3ReviewNodesCount: Int = 0,
    val topicCompletionPercentage: Int = 0,
    val gradeNodes: List<KnowledgeNodeEntity> = emptyList(),
    val masteryMap: Map<String, StudentMasteryEntity> = emptyMap(),
    val misconceptionLogs: List<MisconceptionLogEntity> = emptyList()
)
