package com.example.toanhoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "misconception_logs")
data class MisconceptionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nodeId: String,
    val bugTag: String,
    val occurrenceCount: Int = 1,
    val lastDetectedEpochMs: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
