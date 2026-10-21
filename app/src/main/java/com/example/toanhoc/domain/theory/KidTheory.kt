package com.example.toanhoc.domain.theory

/* ------------------------------------------------------------------------------------------
 * Lý thuyết cho trẻ – dữ liệu có cấu trúc (không phải đoạn văn dài):
 *   GHI NHỚ: 1–3 câu ngắn có từ khoá tô màu + thẻ mẫu trực quan
 *   VÍ DỤ:   đề cùng dạng, khác số; các bước = tên ngắn + dòng minh hoạ chữ to + 1 câu giải thích
 *   KẾT LUẬN: "Vậy …"
 * Màu thống nhất: XANH = số/phép tính chính · CAM = chỗ dễ quên (nhớ, mượn, phần hơn)
 *                 TÍM = trừ/bớt · XANH LÁ = cộng/kết quả.
 * ---------------------------------------------------------------------------------------- */

enum class Hue { INK, BLUE, GREEN, PURPLE, ORANGE, MUTED }

/** Một mẩu trong câu/dòng minh hoạ: chữ thường hoặc "viên thuốc" tô màu. */
sealed interface Tok {
    data class T(val s: String, val hue: Hue = Hue.INK) : Tok
    data class P(val s: String, val hue: Hue) : Tok
}

/** Thẻ mẫu trong khung Ghi nhớ: dòng trên to, dòng dưới nhỏ. */
data class Tile(val top: String, val bottom: String, val hue: Hue = Hue.INK)

sealed interface Visual {
    /**
     * Đặt tính dọc. [rows] các số (không dấu cách); [op] "+", "-", "×"; [result] kết quả (null = chưa điền).
     * [carries]: số nhớ/mượn theo từng cột tính từ phải (chỉ số 0 = cột đơn vị), "" = không có.
     * [partials]: tích riêng (nhân với số có 2 chữ số), viết lùi dần.
     */
    data class Column(
        val rows: List<String>, val op: String, val result: String?,
        val carries: List<String> = emptyList(), val partials: List<String> = emptyList()
    ) : Visual

    /** Sơ đồ đoạn thẳng: mỗi hàng nhiều đoạn (độ dài tương đối), [bracket] = nhãn ngoặc tổng bên phải. */
    data class Bars(val rows: List<BarRow>, val bracket: String? = null) : Visual

    /** Chấm tròn cho lớp 1: các nhóm (số chấm, màu, gạch bỏ?). */
    data class Dots(val groups: List<DotGroup>) : Visual

    /** Các ô theo hàng (trăm/chục/đơn vị…): mỗi ô = (chữ số, tên hàng, màu). */
    data class Places(val cells: List<Tile>) : Visual
}

data class BarRow(val label: String, val segments: List<Seg>)
data class Seg(val units: Float, val hue: Hue, val label: String? = null)
data class DotGroup(val count: Int, val hue: Hue, val crossed: Boolean = false)

data class TheoryStep(
    val title: String,
    val line: List<Tok> = emptyList(),
    val note: String? = null,
    val visual: Visual? = null
)

data class KidTheory(
    val topic: String,
    val rule: List<List<Tok>>,
    val tiles: List<Tile> = emptyList(),
    val example: String,
    val steps: List<TheoryStep>,
    val conclusion: String,
    /** CÁCH NHANH (chỉ có khi dạng đó có mẹo thật); [steps] là CÁCH Ở TRƯỜNG. */
    val quick: List<TheoryStep> = emptyList(),
    val quickRule: String? = null,
    /** Đáp án sai → vì sao sai (dùng cho video giải chính câu con làm sai). */
    val errorNotes: Map<String, String> = emptyMap()
) {
    /** Văn bản thuần để đọc to (TTS). */
    fun speechText(): String = buildString {
        append(topic).append(". Ghi nhớ. ")
        rule.forEach { line -> append(line.joinToString("") { tokText(it) }).append(" ") }
        append("Ví dụ. ").append(example).append(" ")
        steps.forEachIndexed { i, st ->
            append("Bước ${i + 1}. ").append(st.title).append(". ")
            if (st.line.isNotEmpty()) append(st.line.joinToString(" ") { tokText(it) }).append(". ")
            st.note?.let { append(it).append(" ") }
        }
        append(conclusion)
    }.let { com.example.toanhoc.domain.lecture.VnSpeech.toSpeech(it) }

    private fun tokText(t: Tok) = when (t) { is Tok.T -> t.s; is Tok.P -> t.s }
}
