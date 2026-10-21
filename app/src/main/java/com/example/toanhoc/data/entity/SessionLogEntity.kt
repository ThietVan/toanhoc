package com.example.toanhoc.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_log")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val grade: Int,
    val kcId: String,
    val isCorrect: Boolean,
    val responseLatencyMs: Long
)
