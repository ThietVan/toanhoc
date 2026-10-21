package com.example.toanhoc.domain.model

/**
 * Atomic Knowledge Component (KC) representing a discrete node in the Primary Math Knowledge Graph (Grades 1-5).
 */
data class KnowledgeComponent(
    val id: String,
    val grade: Int,
    val category: String,
    val name: String,
    val description: String,
    val initialMastery: Double = 0.25
)
