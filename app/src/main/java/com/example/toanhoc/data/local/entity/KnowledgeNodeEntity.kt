package com.example.toanhoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_nodes")
data class KnowledgeNodeEntity(
    @PrimaryKey val nodeId: String,
    val grade: Int,
    val domainCategory: String,
    val titleVi: String,
    val axiomSummaryVi: String,
    val intrinsicDifficulty: Float,
    val discrimination: Float,
    val generatorType: String
)
