package com.example.toanhoc.domain.model

/**
 * 3-Level Progression within a Mastery-Based Stage Round.
 */
enum class RoundLevel(val titleVi: String, val targetQuestionCount: Int) {
    LEVEL_1_FOUNDATION("Cấp 1: Làm quen", 3),
    LEVEL_2_APPLICATION("Cấp 2: Luyện tập", 5),
    LEVEL_3_MASTERY("Cấp 3: Thành thạo", 5)
}
