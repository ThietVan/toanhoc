package com.example.toanhoc.data.repository

import com.example.toanhoc.domain.model.KnowledgeComponent

interface MathRepository {
    suspend fun getKnowledgeComponentsForGrade(grade: Int): List<KnowledgeComponent>
    suspend fun getKcMasteryMap(grade: Int): Map<String, Double>
    suspend fun saveKcMastery(kcId: String, grade: Int, mastery: Double)
    suspend fun logAttempt(grade: Int, kcId: String, isCorrect: Boolean, responseLatencyMs: Long)
}
