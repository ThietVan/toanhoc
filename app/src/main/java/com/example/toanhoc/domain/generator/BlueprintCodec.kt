package com.example.toanhoc.domain.generator

/**
 * Mã hóa DỮ LIỆU SƠ ĐỒ (chỉ chứa dữ kiện đề cho, KHÔNG chứa đáp án) vào một chuỗi "khóa=giá trị;khóa=giá trị".
 * Ẩn số được ghi là "?". Giá trị không được chứa ';' hoặc '='.
 * Engine (domain) ghi, MathematicalBlueprint (UI) đọc.
 */
object BlueprintCodec {
    fun encode(vararg pairs: Pair<String, Any?>): String =
        pairs.filter { it.second != null }.joinToString(";") { "${it.first}=${it.second}" }

    fun decode(raw: String): Map<String, String> {
        if (!raw.contains("=")) return emptyMap()
        return raw.split(";").mapNotNull {
            val i = it.indexOf('=')
            if (i <= 0) null else it.substring(0, i).trim() to it.substring(i + 1).trim()
        }.toMap()
    }
}
