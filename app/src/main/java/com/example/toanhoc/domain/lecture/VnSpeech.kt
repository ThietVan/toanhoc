package com.example.toanhoc.domain.lecture

/* ------------------------------------------------------------------------------------------
 * LỜI ĐỌC tách khỏi CHỮ HIỂN THỊ.
 *
 * Màn hình hiển thị đúng ký hiệu toán (2 < 3, 3/4, 25%, 3,5 km), còn giọng đọc nhận một câu
 * đã đổi hết sang lời như giáo viên đọc: "hai bé hơn ba", "ba phần tư", "hai mươi lăm phần trăm",
 * "ba phẩy năm ki-lô-mét". Mọi chỗ trong app phát giọng đều đi qua đây, nên:
 *   - giọng nào cũng đọc giống nhau (giọng Google trên máy, Piper, giọng AI trên mạng);
 *   - không còn kiểu "số hai dấu nhỏ hơn số ba".
 * Thuật ngữ theo sách Kết nối tri thức: "bé hơn", "lớn hơn", "nghìn", "linh".
 * ---------------------------------------------------------------------------------------- */
object VnSpeech {

    private val DIGIT = arrayOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")

    /** Đọc một số tự nhiên bằng chữ: 21 → "hai mươi mốt", 105 → "một trăm linh năm", 2014 → "hai nghìn không trăm mười bốn". */
    fun number(n: Long): String {
        if (n == 0L) return "không"
        if (n < 0) return "âm " + number(-n)
        val units = listOf("", "nghìn", "triệu", "tỉ")
        val groups = mutableListOf<Int>()
        var x = n
        while (x > 0) { groups += (x % 1000).toInt(); x /= 1000 }
        val parts = mutableListOf<String>()
        for (i in groups.indices.reversed()) {
            val g = groups[i]
            if (g == 0) continue
            val hasHigher = i < groups.lastIndex
            parts += (three(g, full = hasHigher) + " " + units.getOrElse(i) { "" }).trim()
        }
        return parts.joinToString(" ")
    }

    /** Ba chữ số; [full] = có nhóm lớn hơn đứng trước nên phải đọc đủ "không trăm", "linh". */
    private fun three(g: Int, full: Boolean): String {
        val h = g / 100; val t = (g / 10) % 10; val u = g % 10
        val out = mutableListOf<String>()
        if (h > 0 || full) out += DIGIT[h] + " trăm"
        when {
            t == 0 && u == 0 -> {}
            t == 0 -> { if (h > 0 || full) out += "linh"; out += DIGIT[u] }
            t == 1 -> { out += "mười"; if (u == 5) out += "lăm" else if (u > 0) out += DIGIT[u] }
            else -> {
                out += DIGIT[t] + " mươi"
                when (u) { 0 -> {}; 1 -> out += "mốt"; 4 -> out += "tư"; 5 -> out += "lăm"; else -> out += DIGIT[u] }
            }
        }
        return out.joinToString(" ")
    }

    /** "3,5" → "ba phẩy năm"; "0,05" → "không phẩy không năm"; "12" → "mười hai". */
    fun decimal(s: String): String {
        val clean = s.replace(" ", "").replace(".", "")
        val ip = clean.substringBefore(','); val fp = clean.substringAfter(',', "")
        val head = number(ip.toLongOrNull() ?: return s)
        if (fp.isEmpty()) return head
        val zeros = fp.takeWhile { it == '0' }
        val rest = fp.drop(zeros.length)
        val tail = (zeros.map { "không" } + (if (rest.isNotEmpty()) listOf(number(rest.toLong())) else emptyList())).joinToString(" ")
        return "$head phẩy $tail"
    }

    private val ORDINAL = mapOf(1 to "nhất", 2 to "hai", 3 to "ba", 4 to "tư")

    private val UNITS = listOf(
        "km/giờ" to "ki-lô-mét một giờ", "km/h" to "ki-lô-mét một giờ", "m/giây" to "mét một giây", "m/s" to "mét một giây",
        "km²" to "ki-lô-mét vuông", "dm²" to "đề-xi-mét vuông", "cm²" to "xăng-ti-mét vuông", "mm²" to "mi-li-mét vuông", "m²" to "mét vuông",
        "dm³" to "đề-xi-mét khối", "cm³" to "xăng-ti-mét khối", "m³" to "mét khối",
        "°C" to "độ C", "km" to "ki-lô-mét", "dm" to "đề-xi-mét", "cm" to "xăng-ti-mét", "mm" to "mi-li-mét",
        "kg" to "ki-lô-gam", "ml" to "mi-li-lít", "ha" to "héc-ta", "m" to "mét", "g" to "gam", "l" to "lít"
    )

    /** Đổi chữ hiển thị sang lời đọc. [numbersAsWords] = đọc số bằng chữ (cần cho giọng Piper; giọng Google cũng đọc tốt). */
    fun toSpeech(display: String, numbersAsWords: Boolean = true): String {
        var s = " " + display + " "
        // gộp nhóm nghìn "66 000" → "66000"
        s = s.replace(Regex("""(?<=\d) (?=\d{3}(?!\d))"""), "")
        s = s.replace("✓", "").replace("…", ", ").replace("...", ", ").replace("→", ", ")
        // ngày tháng trước khi xử lý phân số: "ngày 5/10" → "ngày 5 tháng 10"
        s = s.replace(Regex("""(ngày\s+)(\d{1,2})/(\d{1,2})(?!\d)""")) { "${it.groupValues[1]}${it.groupValues[2]} tháng ${it.groupValues[3]}" }
        // đơn vị đi sau số
        for ((u, w) in UNITS) s = s.replace(Regex("""(\d)\s?${Regex.escape(u)}(?![\p{L}\d])""")) { "${it.groupValues[1]} $w" }
        // hỗn số "2 1/3" → "2 và 1 phần 3"; phân số "3/4" → "3 phần 4"
        s = s.replace(Regex("""(\d+)\s+(\d+)/(\d+)""")) { "${it.groupValues[1]} và ${it.groupValues[2]} phần ${it.groupValues[3]}" }
        // mẫu số 4 đọc là "tư" (ba phần tư) như giáo viên vẫn đọc
        s = s.replace(Regex("""(\d+(?:,\d+)?)\s*/\s*(\d+(?:,\d+)?)""")) { "${it.groupValues[1]} phần ${if (it.groupValues[2] == "4") "tư" else it.groupValues[2]}" }
        s = s.replace(Regex("""□\s*/\s*(\d+)""")) { "ô trống phần ${it.groupValues[1]}" }
        // so sánh, phép tính
        s = s.replace("≤", " bé hơn hoặc bằng ").replace("≥", " lớn hơn hoặc bằng ").replace("≠", " khác ")
            .replace("<", " bé hơn ").replace(">", " lớn hơn ")
        s = s.replace(Regex("""=\s*\?"""), " bằng bao nhiêu").replace("%", " phần trăm")
        s = s.replace("+", " cộng ").replace("×", " nhân ").replace(Regex("""(?<=\d)\s+[xX]\s+(?=\d)"""), " nhân ")
        s = s.replace(Regex("""\s:\s"""), " chia ").replace("−", " trừ ").replace(Regex("""(?<=\d)\s*-\s*(?=\d)"""), " trừ ")
        s = s.replace("=", " bằng ")
        s = s.replace("(", " mở ngoặc ").replace(")", " đóng ngoặc ")
        s = s.replace("□", " ô trống ").replace(Regex("""(?<=[\s,])\?(?=\s|,)"""), " số cần tìm ")
        // tên điểm, đoạn thẳng, hình (quãng đường AB, tam giác ABC) đọc từng chữ
        s = s.replace(Regex("""((?:đường|đoạn|cạnh|giác|nhật|vuông|thang|hành|thoi|điểm|từ|đến)\s+)([A-Z]{2,4})(?![\p{L}])""")) {
            it.groupValues[1] + it.groupValues[2].toCharArray().joinToString(" ") }
        // chữ in hoa để nhấn mạnh (NHIỀU, HAI…) → chữ thường, để giọng không đánh vần
        s = s.replace(Regex("""(?<![\p{L}])\p{Lu}{2,}(?![\p{L}])""")) { it.value.lowercase() }
        // thứ tự: "thứ 4" → "thứ tư", "số thứ 15" → "thứ mười lăm"
        s = s.replace(Regex("""thứ\s+(\d+)""")) { m -> val k = m.groupValues[1].toInt(); "thứ " + (ORDINAL[k] ?: if (numbersAsWords) number(k.toLong()) else "$k") }
        if (numbersAsWords) {
            s = s.replace(Regex("""\d+(?:,\d+)?""")) { decimal(it.value) }
        }
        return s.replace(Regex("""\s+([,.!?])"""), "$1").replace(Regex("""\s+"""), " ").trim()
    }
}
