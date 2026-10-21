package com.example.toanhoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_mastery")
data class StudentMasteryEntity(
    @PrimaryKey val nodeId: String,
    val pKnow: Float = 0.15f,
    val pTransit: Float = 0.20f,
    val pSlip: Float = 0.08f,
    val pGuess: Float = 0.05f,
    val memoryStabilityDays: Float = 1.0f,
    val lastPracticedEpochMs: Long = 0L,
    val nextReviewEpochMs: Long = 0L,
    val avgLatencyMs: Long = 0L,
    val isMastered: Boolean = false
)
