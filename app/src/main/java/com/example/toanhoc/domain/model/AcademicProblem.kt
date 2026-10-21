package com.example.toanhoc.domain.model

/**
 * Academic Problem representation containing mathematical statement, solution steps, canvas blueprint, and tier.
 */
data class AcademicProblem(
    val problemId: String,
    val nodeId: String,
    val scaffoldingLevel: ScaffoldingLevel,
    val statementVi: String,
    val blueprintSpec: VisualBlueprintSpec? = null,
    val steps: List<SolutionStep>,
    val targetLatencyMs: Long = 2500L,
    val academicTier: AcademicTier = AcademicTier.TRUNG_BINH
)
