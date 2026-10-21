package com.example.toanhoc.domain.engine

import com.example.toanhoc.data.seed.CurriculumSeedData
import com.example.toanhoc.domain.generator.ParametricProblemEngine
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.ScaffoldingLevel

/**
 * Wrapper delegating to ParametricProblemEngine for generating AcademicProblem instances.
 */
class ParametricProblemGenerator(
    private val engine: ParametricProblemEngine = ParametricProblemEngine()
) {
    fun generateProblem(grade: Int, level: ScaffoldingLevel = ScaffoldingLevel.INDEPENDENT): AcademicProblem {
        val gradeNodes = CurriculumSeedData.seedNodes.filter { it.grade == grade }
        val nodeId = gradeNodes.randomOrNull()?.nodeId ?: "G${grade}_01"
        return engine.generateProblemForNode(nodeId = nodeId, grade = grade, scaffoldingLevel = level)
    }
}
