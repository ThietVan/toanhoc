package com.example.toanhoc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_knowledge")
data class UserKnowledgeEntity(
    @PrimaryKey val kcId: String,
    val grade: Int,
    val masteryProbability: Double,
    val lastUpdatedTimestamp: Long
)
