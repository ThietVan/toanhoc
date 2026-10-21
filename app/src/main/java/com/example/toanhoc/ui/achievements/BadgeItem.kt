package com.example.toanhoc.ui.achievements

/**
 * Achievement Badge item with real unlock condition logic.
 */
data class BadgeItem(
    val id: String,
    val iconEmoji: String,
    val titleVi: String,
    val descriptionVi: String,
    val gradeLabelVi: String,
    val isEarned: Boolean
)
