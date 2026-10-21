package com.example.toanhoc.domain.model

/**
 * 3 Academic Tiers for primary math mastery.
 */
enum class AcademicTier(val titleVi: String, val descriptionVi: String) {
    TRUNG_BINH(
        titleVi = "TRUNG BÌNH",
        descriptionVi = "Cấp độ Chuẩn nền tảng: Bài toán 1-2 bước tính thuận chiều, dữ kiện trực tiếp."
    ),
    GIOI(
        titleVi = "GIỎI",
        descriptionVi = "Cấp độ Biến đổi cấu trúc: Bài toán ngược, 2-3 bước suy luận, dữ kiện ẩn/đổi đơn vị."
    ),
    XUAT_SAC(
        titleVi = "XUẤT SẮC",
        descriptionVi = "Cấp độ Tư duy Hệ thống: Toán liên môn, Olympiad Tiểu học, chiến lược nâng cao."
    );

    companion object {
        fun fromString(tierStr: String): AcademicTier {
            return entries.firstOrNull { it.name.equals(tierStr, ignoreCase = true) } ?: TRUNG_BINH
        }
    }
}
