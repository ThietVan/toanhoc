package com.example.toanhoc.ui.exam

import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ExamMode

/**
 * State representation for Assessment Exam Screen.
 */
data class ExamUiState(
    val examMode: ExamMode = ExamMode.LEVEL_UP_EXAM,
    val targetTier: AcademicTier = AcademicTier.GIOI,
    val grade: Int = 1,
    val problems: List<AcademicProblem> = emptyList(),
    val currentProblemIndex: Int = 0,
    val studentAnswersMap: Map<Int, String> = emptyMap(),
    val totalSecondsRemaining: Int = 900, // 15 minutes
    val isSubmitted: Boolean = false,
    val scoreOutOf10: Int = 0,
    val isPassed: Boolean = false
)
