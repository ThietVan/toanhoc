package com.example.toanhoc.domain.challenge

import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.LessonProblemGenerator
import com.example.toanhoc.domain.curriculum.LessonStage
import com.example.toanhoc.domain.curriculum.TextbookLesson
import com.example.toanhoc.domain.model.AcademicProblem
import kotlinx.serialization.Serializable
import kotlin.random.Random

/* ------------------------------------------------------------------------------------------
 * LỘ TRÌNH DO BỐ MẸ CHỌN + THỬ THÁCH VƯỢT BÀI
 *
 * 1. Bố mẹ bỏ chọn những bài cho rằng con đã biết.
 * 2. Các bài bị bỏ được gom theo MẢNG KIẾN THỨC (nhóm đề của bộ sinh đề, ~9–17 mảng/lớp).
 * 3. Mỗi mảng 3 câu:  ① Cơ bản (mức Dạng 1)  ② Mức Dạng 2 + DỮ KIỆN THỪA + đáp án bẫy  ③ Nâng cao (mức khó nhất kiểm tra học kì).
 * 4. Đúng cả 3 (và không trả lời quá nhanh) → mảng được DUYỆT: các bài bị bỏ trong mảng xem như thành thạo, không cộng xu.
 *    Chưa đạt → bài bị GIỮ LẠI trong lộ trình; kiểm tra lại sau 3 ngày hoặc khi con học xong Dạng 1 của một bài trong mảng.
 * Câu ① sai → dừng mảng đó luôn (không bắt con làm tiếp 2 câu khó hơn).
 * ---------------------------------------------------------------------------------------- */

@Serializable
data class ChallengeQuestion(
    /** 0 = cơ bản, 1 = có dữ kiện thừa, 2 = nâng cao. */
    val role: Int,
    val lessonId: String,
    val statement: String,
    val options: List<String>,
    val answer: String,
    /** Câu dữ kiện thừa đã chèn vào đề (để tô màu trong báo cáo). */
    val noise: String? = null,
    /** Đáp án bẫy = kết quả khi con lỡ dùng số thừa. */
    val trap: String? = null,
    val picked: String? = null,
    /** Thời gian con làm câu này (ms). */
    val ms: Long = 0
) {
    val answered get() = picked != null
    val correct get() = picked != null && picked == answer
    val tooFast get() = picked != null && ms < ChallengeRules.minMs(statement)
}

@Serializable
data class ChallengeUnit(
    val key: String,
    val title: String,
    /** Các bài bị bỏ qua thuộc mảng này (được duyệt → đánh dấu thành thạo). */
    val lessonIds: List<String>,
    val questions: List<ChallengeQuestion>
) {
    /** Câu 1 sai → mảng dừng sớm. */
    val stoppedEarly get() = questions.firstOrNull()?.let { it.answered && !it.correct } == true
    val done get() = stoppedEarly || questions.all { it.answered }
    val passed get() = done && questions.all { it.correct && !it.tooFast }

    /** Câu kế tiếp con phải làm trong mảng (null = mảng xong). */
    fun nextIndex(): Int? = if (done) null else questions.indexOfFirst { !it.answered }.takeIf { it >= 0 }

    /** Lý do chưa duyệt, viết cho phụ huynh. */
    fun reasons(): List<String> {
        if (!done || passed) return emptyList()
        val out = mutableListOf<String>()
        val (q1, q2, q3) = Triple(questions.getOrNull(0), questions.getOrNull(1), questions.getOrNull(2))
        if (q1 != null && q1.answered && !q1.correct) out += "Câu 1 (cơ bản) chưa đúng – con chưa nắm chắc cách làm nên dừng mảng này sớm."
        if (q2 != null && q2.answered && !q2.correct) out +=
            if (q2.trap != null && q2.picked == q2.trap) "Câu 2: con bị dữ kiện thừa đánh lừa (đã dùng cả số không liên quan)."
            else "Câu 2 (mức Dạng 2, có dữ kiện thừa) chưa đúng."
        if (q3 != null && q3.answered && !q3.correct) out += "Câu 3 (nâng cao, nhiều bước) chưa đúng."
        questions.forEachIndexed { i, q -> if (q.correct && q.tooFast) out += "Câu ${i + 1} con trả lời quá nhanh (${q.ms / 1000} giây) – có thể đã đoán, nên chưa tính." }
        return out
    }
}

@Serializable
data class Challenge(
    val id: String,
    val childId: String,
    val grade: Int,
    /** Các bài bố mẹ giữ lại để con học. */
    val keptLessonIds: List<String>,
    val units: List<ChallengeUnit>,
    val createdAt: Long,
    val finishedAt: Long? = null,
    val seenByParent: Boolean = false
) {
    val finished get() = finishedAt != null
    val totalQuestions get() = units.sumOf { it.questions.size }
    val answeredCount get() = units.sumOf { u -> if (u.stoppedEarly) u.questions.size else u.questions.count { it.answered } }
    val passedUnits get() = units.filter { it.passed }
    val failedUnits get() = units.filter { it.done && !it.passed }
    val masteredLessonIds get() = passedUnits.flatMap { it.lessonIds }

    /** Vị trí câu kế tiếp: (mảng, câu) hoặc null nếu đã làm hết. */
    fun cursor(): Pair<Int, Int>? {
        units.forEachIndexed { ui, u -> u.nextIndex()?.let { return ui to it } }
        return null
    }

    fun answer(unit: Int, question: Int, picked: String, ms: Long): Challenge = copy(
        units = units.mapIndexed { ui, u ->
            if (ui != unit) u else u.copy(questions = u.questions.mapIndexed { qi, q -> if (qi == question) q.copy(picked = picked, ms = ms) else q })
        }
    )
}

object ChallengeRules {
    const val RETEST_DAYS = 3L
    const val SECONDS_PER_QUESTION = 45
    val ROLE_NAMES = listOf("Cơ bản", "Có dữ kiện thừa", "Nâng cao")
    val ROLE_HINTS = listOf("mức Dạng 1", "mức Dạng 2 · có số không liên quan", "mức khó nhất bài kiểm tra học kì")

    /** Trả lời nhanh hơn mức này coi như đoán (đề có lời văn cần thời gian đọc). */
    fun minMs(statement: String): Long = if (statement.length > 40) 3000 else 1500

    fun retestAt(failedAt: Long): Long = failedAt + RETEST_DAYS * 24 * 3600 * 1000

    /** Mảng kiến thức của một bài = nhóm đề chính của bài. */
    fun unitKey(lesson: TextbookLesson): String = lesson.sources.first().nodeId

    fun unitTitle(key: String, grade: Int): String {
        val name = KnowledgeUnits.name(key)
        val g = key.getOrNull(1)?.digitToIntOrNull()
        return if (g != null && g < grade) "$name (ôn lớp $g)" else name
    }
}

/** Tên các mảng kiến thức (theo nhóm đề của bộ sinh đề), viết cho phụ huynh đọc. */
object KnowledgeUnits {
    private val NAMES = mapOf(
        "G1_01" to "Vị trí, hình phẳng và hình khối",
        "G1_02" to "Các số đến 10, so sánh số",
        "G1_03" to "Tách – gộp số trong phạm vi 10",
        "G1_04" to "Cộng, trừ trong phạm vi 10",
        "G1_05" to "Các số đến 100, chục và đơn vị",
        "G1_06" to "Cộng, trừ không nhớ trong phạm vi 100",
        "G1_07" to "Bài toán có lời văn lớp 1",
        "G1_08" to "Đo độ dài (cm)",
        "G1_09" to "Xem giờ, xem lịch",
        "G1_10" to "Quy luật, đếm hình",
        "G2_01" to "Số hạng – tổng, số bị trừ – hiệu, tia số",
        "G2_02" to "Cộng, trừ qua 10",
        "G2_03" to "Cộng, trừ có nhớ trong phạm vi 100",
        "G2_04" to "Bài toán nhiều hơn – ít hơn",
        "G2_05" to "Phép nhân, phép chia (bảng 2, bảng 5)",
        "G2_06" to "Các số đến 1000, cộng trừ trong phạm vi 1000",
        "G2_07" to "Hình học lớp 2",
        "G2_08" to "Đơn vị đo dm, m, km, kg, lít",
        "G2_09" to "Thời gian và tiền Việt Nam",
        "G2_10" to "Thống kê và khả năng xảy ra",
        "G3_01" to "Bảng nhân, bảng chia 3 – 9",
        "G3_02" to "Tìm thành phần chưa biết",
        "G3_03" to "Gấp, giảm một số lần; một phần mấy",
        "G3_04" to "Phép chia có dư",
        "G3_05" to "Biểu thức và thứ tự thực hiện",
        "G3_06" to "Các số đến 100 000, làm tròn, số La Mã",
        "G3_07" to "Bài toán giải bằng hai bước tính",
        "G3_08" to "Trung điểm, hình tròn",
        "G3_09" to "Chu vi, diện tích hình chữ nhật, hình vuông",
        "G3_10" to "Đo lường và thống kê lớp 3",
        "G4_01" to "Số đến lớp triệu, làm tròn số",
        "G4_02" to "Biểu thức chứa chữ, tính chất phép tính",
        "G4_03" to "Nhân, chia với số có hai chữ số",
        "G4_04" to "Trung bình cộng, rút về đơn vị",
        "G4_05" to "Tìm hai số khi biết tổng và hiệu",
        "G4_06" to "Phân số: khái niệm, so sánh",
        "G4_07" to "Bốn phép tính với phân số",
        "G4_08" to "Góc, đường thẳng song song, vuông góc",
        "G4_09" to "Đơn vị đo: tấn, m², thế kỉ",
        "G4_10" to "Biểu đồ cột, khả năng xảy ra",
        "G5_01" to "Phân số thập phân, hỗn số",
        "G5_02" to "Số thập phân: hàng, làm tròn",
        "G5_03" to "Bốn phép tính với số thập phân",
        "G5_04" to "Bài toán tổng – hiệu – tỉ",
        "G5_05" to "Tỉ số phần trăm, tỉ lệ bản đồ",
        "G5_06" to "Tam giác, hình thang, hình tròn",
        "G5_07" to "Diện tích xung quanh, thể tích",
        "G5_08" to "Số đo thời gian, chuyển động đều",
        "G5_10" to "Toán tổng hợp, suy luận"
    )

    fun name(nodeId: String): String = NAMES[nodeId.take(5)] ?: nodeId
}

/** Một mảng cần kiểm tra trước khi tạo câu hỏi (để bố mẹ xem trước). */
data class UnitPlan(val key: String, val title: String, val lessons: List<TextbookLesson>)

object ChallengeBuilder {

    /** Gom các bài bị bỏ qua theo mảng, giữ thứ tự sách. */
    fun plan(curriculum: GradeCurriculum, skipped: Collection<String>): List<UnitPlan> =
        curriculum.lessons.filter { it.id in skipped }
            .groupBy { ChallengeRules.unitKey(it) }
            .map { (key, ls) -> UnitPlan(key, ChallengeRules.unitTitle(key, curriculum.grade), ls) }

    fun build(
        id: String, childId: String, curriculum: GradeCurriculum, kept: List<String>, plans: List<UnitPlan>,
        now: Long, gen: LessonProblemGenerator = LessonProblemGenerator(), rng: Random = Random.Default
    ): Challenge = Challenge(
        id = id, childId = childId, grade = curriculum.grade, keptLessonIds = kept,
        units = plans.map { unitOf(curriculum, it, gen, rng) }, createdAt = now
    )

    private fun unitOf(c: GradeCurriculum, plan: UnitPlan, gen: LessonProblemGenerator, rng: Random): ChallengeUnit {
        val ls = plan.lessons
        val early = ls.take((ls.size + 1) / 2)
        val late = ls.drop(ls.size / 2).ifEmpty { ls }
        val used = mutableSetOf<String>()

        fun pick(make: () -> Pair<TextbookLesson, AcademicProblem>, wantWords: Boolean = false): Pair<TextbookLesson, Quiz>? {
            var fallback: Pair<TextbookLesson, Quiz>? = null
            repeat(24) {
                val (l, p) = make()
                val q = quizOf(p) ?: return@repeat
                if (q.statement in used) return@repeat
                if (!wantWords || Noise.isWordProblem(q.statement)) { used += q.statement; return l to q }
                if (fallback == null) fallback = l to q
            }
            return fallback?.also { used += it.second.statement }
        }

        val q1 = pick({ early.random(rng).let { it to gen.next(c, it, LessonStage.EASY, 5) } })
        val q2 = pick({ late.random(rng).let { it to gen.next(c, it, LessonStage.HARD, 5) } }, wantWords = true)
        val q3 = pick({ ls.last().let { it to gen.exam(c, it, hardest = true) } })
            ?: pick({ late.random(rng).let { it to gen.next(c, it, LessonStage.HARD, 7) } })

        val questions = listOfNotNull(
            q1?.let { (l, q) -> ChallengeQuestion(0, l.id, q.statement, q.options.shuffled(rng), q.answer) },
            q2?.let { (l, q) ->
                val n = Noise.inject(q, c.grade, rng)
                ChallengeQuestion(1, l.id, n.statement, n.options.shuffled(rng), q.answer, n.sentence, n.trap)
            },
            q3?.let { (l, q) -> ChallengeQuestion(2, l.id, q.statement, q.options.shuffled(rng), q.answer) }
        )
        return ChallengeUnit(plan.key, plan.title, ls.map { it.id }, questions)
    }

    data class Quiz(val statement: String, val options: List<String>, val answer: String)

    /** Câu trắc nghiệm từ một câu sinh ra: dùng lựa chọn sẵn có, hoặc tự tạo nhiễu cho đáp án là số. */
    fun quizOf(p: AcademicProblem): Quiz? {
        val step = p.steps.firstOrNull() ?: return null
        val answer = step.expectedAnswer.trim()
        if (answer.isEmpty()) return null
        val opts = step.options.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val options = when {
            opts.size >= 3 && answer in opts -> opts.take(4).let { if (answer in it) it else it.dropLast(1) + answer }
            else -> NumberText.distractors(answer)?.let { it + answer } ?: return null
        }
        return Quiz(p.statementVi.trim(), options, answer)
    }
}

/** Đọc/ghi số kiểu Việt Nam: "66 000", "28 quả". */
internal object NumberText {
    private val LEAD = Regex("""^(\d{1,3}(?: \d{3})+|\d+)(.*)$""")

    /** (giá trị, phần chữ phía sau) – chỉ số nguyên. */
    fun parse(s: String): Pair<Long, String>? {
        val m = LEAD.find(s.trim()) ?: return null
        val suffix = m.groupValues[2]
        // Chỉ nhận "28", "28 quả", "66 000 đồng" – không nhận "3C", "2,5", "3/4"
        if (suffix.isNotEmpty() && !suffix.startsWith(" ")) return null
        return m.groupValues[1].replace(" ", "").toLongOrNull()?.let { it to suffix }
    }

    fun format(n: Long, like: String, suffix: String): String {
        val grouped = like.trim().matches(Regex("""^\d{1,3}(?: \d{3})+.*"""))
        val digits = if (grouped || n >= 10_000) n.toString().reversed().chunked(3).joinToString(" ").reversed() else n.toString()
        return digits + suffix
    }

    fun distractors(answer: String): List<String>? {
        val (a, suf) = parse(answer) ?: return null
        val wrong = listOf(a + 1, a - 1, a + 10, a - 10, a + 2, a * 2).filter { it >= 0 && it != a }.distinct().take(3)
        if (wrong.size < 3) return null
        return wrong.map { format(it, answer, suf) }
    }
}

/** Chèn một câu dữ kiện thừa vào đề + tạo đáp án bẫy tương ứng. */
object Noise {
    data class Result(val statement: String, val sentence: String, val options: List<String>, val trap: String?)

    /** Câu nhiễu trung tính + những từ mà nếu đề có thì câu nhiễu có thể gây hiểu nhầm (tránh dùng). */
    private class Tpl(val text: String, val range: IntRange, val avoid: List<String>)

    private val TEMPLATES = listOf(
        Tpl("Hôm đó là thứ %d.", 2..6, listOf("thứ", "ngày", "lịch", "tuần")),
        Tpl("Bạn Minh năm nay %d tuổi.", 6..10, listOf("tuổi", "minh")),
        Tpl("Ngoài sân có %d cây phượng.", 3..9, listOf("cây", "sân")),
        Tpl("Trong vườn có %d chú chim sẻ.", 3..9, listOf("chim", "vườn")),
        Tpl("Cửa hàng mở cửa lúc %d giờ sáng.", 6..9, listOf("giờ", "phút", "đồng hồ", "cửa hàng")),
        Tpl("Trên giá còn %d quyển truyện tranh.", 4..9, listOf("quyển", "truyện", "sách", "giá"))
    )

    fun isWordProblem(s: String): Boolean =
        s.contains("Hỏi") || s.split(Regex("""\s+""")).count { w -> w.any { it.isLetter() } } >= 14

    /** Ranh giới câu: dấu chấm/chấm than (không phải "...") rồi chữ in hoa. */
    private val SENTENCE_END = Regex("""(?<!\.)[.!](?!\.)\s+(?=\p{Lu})""")

    fun inject(q: ChallengeBuilder.Quiz, grade: Int, rng: Random): Result {
        val existing = Regex("""\d+""").findAll(q.statement).map { it.value.toLong() }.toSet()
        val lower = q.statement.lowercase()
        val tpl = TEMPLATES.filter { t -> t.avoid.none { lower.contains(it) } }.ifEmpty { TEMPLATES.takeLast(1) }.random(rng)
        val k = tpl.range.filter { it.toLong() !in existing }.ifEmpty { tpl.range.toList() }.random(rng)
        val sentence = tpl.text.format(k)
        val words = isWordProblem(q.statement)

        val statement = SENTENCE_END.find(q.statement)?.takeIf { words }?.let { m ->
            q.statement.substring(0, m.range.first + 1) + " " + sentence + " " + q.statement.substring(m.range.last + 1)
        } ?: "$sentence ${q.statement}"

        // Đáp án bẫy (chỉ với bài toán có lời, đáp án là số nhỏ): kết quả nếu con lỡ cộng/trừ thêm số thừa
        var trap: String? = null
        var options = q.options
        val parsed = NumberText.parse(q.answer)
        if (words && parsed != null && parsed.first in 1..300 && !q.answer.trim().matches(Regex("""^\d{1,3} \d{3}.*"""))) {
            val (a, suf) = parsed
            val cands = listOf(a + k, a - k).filter { it > 0 && it != a }.map { NumberText.format(it, q.answer, suf) }
            val t = cands.firstOrNull { it in options } ?: cands.firstOrNull()
            if (t != null) {
                trap = t
                if (t !in options) {
                    val wrong = options.filter { it != q.answer }
                    options = if (wrong.isEmpty()) options + t else options.map { if (it == wrong.last()) t else it }
                }
            }
        }
        return Result(statement, sentence, options, trap)
    }
}
