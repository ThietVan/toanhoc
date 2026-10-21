package com.example.toanhoc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_session_logs")
data class DailySessionLogEntity(
    @PrimaryKey val dateIso: String,
    val completedSeconds: Int = 0,
    val phase1Accuracy: Float = 0.0f,
    val phase1AvgLatencyMs: Long = 0L,
    val phase2TargetNodeId: String = "",
    val phase2MasteryDelta: Float = 0.0f,
    val phase3ReviewedNodesCount: Int = 0,
    val isCompleted: Boolean = false
)
