package com.example.toanhoc.ui.design

/* ------------------------------------------------------------------------------------------
 * Bộ phân tích biểu thức toán (thuần Kotlin, không phụ thuộc Compose, dễ viết unit test).
 *
 * Nhận diện:
 *   3/4                  phân số số nguyên
 *   1,5/2   x/2   □/4    số thập phân, chữ cái, ô trống làm tử/mẫu
 *   (8 + 3)/12           tử hoặc mẫu là một biểu thức trong ngoặc (ngoặc được bỏ khi hiển thị)
 *   (1/2 + 1/3)/4        phân số lồng nhau
 *   2 1/3                hỗn số (số nguyên là chữ thường, phân số đứng ngay sau)
 *   3/4 + 1/4 = 1        nhiều phân số trong một biểu thức, mỗi phân số tách riêng
 *
 * KHÔNG biến thành phân số (để giữ nguyên là chữ):
 *   km/h, m/s, và/hoặc   hai bên đều là chữ, hoặc là từ nhiều chữ cái
 *   12/10/2024           chuỗi nhiều dấu gạch chéo (ngày tháng)
 *   ngày 5/10, lớp 3/2   đứng sau các từ "ngày", "tháng", "lớp", "phòng"
 * ---------------------------------------------------------------------------------------- */

internal sealed interface MathNode {
    data class Plain(val text: String) : MathNode
    data class Fraction(
        val numerator: List<MathNode>,
        val denominator: List<MathNode>
    ) : MathNode
}

internal object MathParser {

    private val UNKNOWN_SYMBOLS = setOf('□', '▢', '☐', '?')
    private val NON_FRACTION_PRECEDERS = setOf("ngày", "tháng", "lớp", "phòng")
    private val TWO_OR_MORE_LETTERS = Regex("""\p{L}{2,}""")
    private val X_BETWEEN_DIGITS = Regex("""(?<=\d) x (?=\d)""")

    private class Operand(
        val content: String,
        val isGroup: Boolean,
        val end: Int,
        /** true nếu là số, ô trống hoặc nhóm trong ngoặc; false nếu chỉ là một chữ cái đơn. */
        val mathy: Boolean
    )

    fun parse(input: String): List<MathNode> {
        val nodes = ArrayList<MathNode>()
        val buf = StringBuilder()

        fun flush() {
            if (buf.isNotEmpty()) {
                nodes += MathNode.Plain(normalize(buf.toString()))
                buf.setLength(0)
            }
        }

        var i = 0
        while (i < input.length) {
            val atBoundary = i == 0 || !input[i - 1].isLetterOrDigit()
            val left = if (atBoundary) readOperand(input, i) else null

            if (left != null && left.end < input.length && input[left.end] == '/') {
                val right = readOperand(input, left.end + 1)
                val blockedByContext = lastWord(buf) in NON_FRACTION_PRECEDERS
                if (right != null && (left.mathy || right.mathy)) {
                    if (blockedByContext || isChained(input, right.end)) {
                        // "ngày 5/10", hoặc a/b/c (ví dụ ngày tháng): giữ nguyên cả chuỗi là chữ
                        var e = right.end
                        while (isChained(input, e)) e = wordEnd(input, e + 1)
                        buf.append(input, i, e)
                        i = e
                        continue
                    }
                    flush()
                    nodes += MathNode.Fraction(toNodes(left), toNodes(right))
                    i = right.end
                    continue
                }
            }

            if (input[i].isLetterOrDigit()) {
                val e = wordEnd(input, i)
                buf.append(input, i, e)
                i = e
            } else {
                buf.append(input[i])
                i++
            }
        }
        flush()
        return nodes
    }

    private fun toNodes(op: Operand): List<MathNode> =
        if (op.isGroup) parse(op.content.trim()) else listOf(MathNode.Plain(op.content))

    private fun readOperand(s: String, start: Int): Operand? {
        if (start >= s.length) return null
        val c = s[start]
        return when {
            c == '(' -> {
                // "f(x)/2": ngoặc đứng sát sau chữ/số là lời gọi hàm, không phải tử số
                val close = matchingParen(s, start)
                if (close < 0) return null
                val inner = s.substring(start + 1, close)
                if (inner.isBlank() || TWO_OR_MORE_LETTERS.containsMatchIn(inner)) return null
                Operand(inner, isGroup = true, end = close + 1, mathy = true)
            }
            c in UNKNOWN_SYMBOLS -> Operand(c.toString(), false, start + 1, true)
            c.isLetterOrDigit() -> {
                val end = wordEnd(s, start)
                val word = s.substring(start, end)
                when {
                    isNumber(word) -> Operand(word, false, end, true)
                    word.length == 1 -> Operand(word, false, end, false)
                    else -> null // từ nhiều chữ như "km", "4kg"
                }
            }
            else -> null
        }
    }

    /** Hết một "từ": chữ + số liên tiếp, cho phép dấu . hoặc , nằm giữa hai chữ số (số thập phân). */
    private fun wordEnd(s: String, start: Int): Int {
        var i = start
        while (i < s.length) {
            val ch = s[i]
            val isDecimalMark = (ch == '.' || ch == ',') &&
                i > start && s[i - 1].isDigit() &&
                i + 1 < s.length && s[i + 1].isDigit()
            if (ch.isLetterOrDigit() || isDecimalMark) i++ else break
        }
        return i
    }

    private fun isNumber(word: String): Boolean =
        word.isNotEmpty() && word[0].isDigit() && word.all { it.isDigit() || it == '.' || it == ',' }

    private fun isChained(s: String, end: Int): Boolean =
        end + 1 < s.length && s[end] == '/' && s[end + 1].isLetterOrDigit()

    private fun matchingParen(s: String, open: Int): Int {
        var depth = 0
        for (i in open until s.length) {
            when (s[i]) {
                '(' -> depth++
                ')' -> {
                    depth--
                    if (depth == 0) return i
                }
            }
        }
        return -1
    }

    private fun lastWord(buf: CharSequence): String {
        var end = buf.length
        while (end > 0 && buf[end - 1].isWhitespace()) end--
        var start = end
        while (start > 0 && buf[start - 1].isLetter()) start--
        return buf.subSequence(start, end).toString().lowercase()
    }

    /** Chuẩn hóa ký hiệu: dấu trừ, dấu nhân đúng chuẩn toán học. */
    private fun normalize(text: String): String =
        text
            .replace(" - ", " − ")
            .replace('*', '×')
            .replace(X_BETWEEN_DIGITS, " × ")
}

/* ----------------------------- Đọc to cho TalkBack ----------------------------- */

internal fun List<MathNode>.toSpoken(): String = joinToString("") { it.toSpoken() }

internal fun MathNode.toSpoken(): String = when (this) {
    is MathNode.Plain -> text
        .replace("+", " cộng ")
        .replace("−", " trừ ")
        .replace("×", " nhân ")
        .replace("÷", " chia ")
        .replace("=", " bằng ")
        .replace(Regex("\\s+"), " ")
    is MathNode.Fraction -> {
        val n = numerator.toSpoken().trim()
        val d = denominator.toSpoken().trim()
        if (n.contains(' ') || d.contains(' ')) " phân số có tử số $n, mẫu số $d "
        else " $n phần $d "
    }
}
