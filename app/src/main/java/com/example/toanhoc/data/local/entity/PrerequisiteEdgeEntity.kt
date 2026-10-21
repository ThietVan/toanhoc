package com.example.toanhoc.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "prerequisite_edges",
    primaryKeys = ["parentNodeId", "childNodeId"]
)
data class PrerequisiteEdgeEntity(
    val parentNodeId: String,
    val childNodeId: String,
    val implicitCreditWeight: Float
)
