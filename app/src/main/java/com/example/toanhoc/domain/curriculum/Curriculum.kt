package com.example.toanhoc.domain.curriculum

import com.example.toanhoc.domain.generator.ParametricProblemEngine
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ScaffoldingLevel
import kotlin.random.Random

/* ==========================================================================================
 * CHƯƠNG TRÌNH THEO SÁCH GIÁO KHOA (Kết nối tri thức với cuộc sống)
 *
 * Cấu trúc: Lớp → Chủ đề → Bài. Mỗi Bài có 4 bước:
 *   LEARN  – Bài giảng: hình động + giọng đọc, có câu đố cuối; bắt buộc xem trước Dạng 1 (mọi bài)
 *   EASY   – Dạng 1: đề dễ
 *   HARD   – Dạng 2: đề khó
 *   REVIEW – Ôn tập: trộn đề của bài này với các bài trước trong cùng chủ đề
 *
 * Câu hỏi lấy từ ParametricProblemEngine (theo nhóm kiến thức `nodeId`), rồi LỌC theo nội dung
 * từng bài (vd. Bài 1 chỉ dùng số 0–5, Bài 10 chỉ có phép cộng).
 * Thêm bài mới = thêm một dòng TextbookLesson vào file dữ liệu của lớp.
 * ======================================================================================== */

enum class LessonStage(val label: String, val shortLabel: String) {
    LEARN("Bài giảng", "Bài giảng"),
    EASY("Dạng 1 · Dễ", "Dạng 1"),
    HARD("Dạng 2 · Khó", "Dạng 2"),
    REVIEW("Ôn tập", "Ôn tập")
}

/** Bộ lọc câu hỏi theo nội dung bài. Mọi điều kiện đều phải thỏa. */
data class QuestionFilter(
    val include: Regex? = null,
    val exclude: Regex? = null,
    val maxNumber: Int? = null,
    /** true = chỉ phép cộng/trừ CÓ nhớ; false = chỉ KHÔNG nhớ; null = không xét. */
    val carry: Boolean? = null
) {
    fun accepts(statement: String): Boolean {
        if (include != null && !include.containsMatchIn(statement)) return false
        if (exclude != null && exclude.containsMatchIn(statement)) return false
        if (carry != null) {
            val m = OP.find(statement) ?: return false
            val a = m.groupValues[1].replace(" ", "").toLong()
            val b = m.groupValues[3].replace(" ", "").toLong()
            if (hasCarry(a, b, m.groupValues[2] == "+") != carry) return false
        }
        if (maxNumber != null) {
            val nums = Regex("""\d+""").findAll(statement).map { it.value.toInt() }
            if (nums.any { it > maxNumber }) return false
        }
        return true
    }

    companion object {
        private val OP = Regex("""(\d+(?: \d{3})*)\s*([+-])\s*(\d+(?: \d{3})*)""")

        /** Cộng: có cột nào tổng ≥ 10. Trừ: có cột nào chữ số bị trừ nhỏ hơn chữ số trừ. */
        fun hasCarry(a: Long, b: Long, add: Boolean): Boolean {
            var x = a; var y = b
            while (x > 0 || y > 0) {
                val dx = x % 10; val dy = y % 10
                if (add && dx + dy >= 10) return true
                if (!add && dx < dy) return true
                x /= 10; y /= 10
            }
            return false
        }
    }
}

/** Một nguồn câu hỏi: nhóm kiến thức của bộ sinh đề + bộ lọc. */
data class PracticeSource(val nodeId: String, val filter: QuestionFilter = QuestionFilter())

/** Một thẻ dạy: tiêu đề, lời giảng, sơ đồ minh họa (tùy chọn – dựng bằng bộ vẽ sơ đồ của app). */
data class TeachCard(
    val title: String,
    val body: String,
    val diagramType: String? = null,     // "NUMBER_LINE", "SINGAPORE_BAR_MODEL", ...
    val diagramStatement: String = "",
    val diagramExpr: String = ""
)

data class TryQuestion(
    val statement: String,
    val options: List<String>,
    val answer: String,
    val explain: String
)

data class LessonTeaching(
    val cards: List<TeachCard>,
    val tips: List<String>,
    val tryIt: List<TryQuestion>
)

data class TextbookLesson(
    val id: String,
    val number: Int,
    val title: String,
    val domain: String,                  // khóa màu/hình minh họa: ARITHMETIC, GEOMETRY_MEASUREMENT, ...
    val sources: List<PracticeSource>,
    val teaching: LessonTeaching? = null,
    /** true = bộ sinh đề chưa có dạng đề riêng cho bài này, đang mượn dạng gần nhất (cần bổ sung). */
    val practiceGap: Boolean = false
)

data class TextbookTopic(val number: Int, val title: String, val lessons: List<TextbookLesson>)

data class GradeCurriculum(
    val grade: Int,
    /** Số thứ tự chủ đề đầu tiên của Học kì 2 (theo sách: Tập 2). */
    val semester2FromTopic: Int,
    val topics: List<TextbookTopic>
) {
    val lessons: List<TextbookLesson> get() = topics.flatMap { it.lessons }

    fun semesterOf(topic: TextbookTopic): Int = if (topic.number >= semester2FromTopic) 2 else 1
    fun semesterOf(lesson: TextbookLesson): Int = semesterOf(topicOf(lesson))
    fun lessonsOf(semester: Int): List<TextbookLesson> =
        topics.filter { semesterOf(it) == semester }.flatMap { it.lessons }
    fun lesson(id: String): TextbookLesson? = lessons.firstOrNull { it.id == id }
    fun topicOf(lesson: TextbookLesson): TextbookTopic = topics.first { lesson in it.lessons }

    /** Nguồn cho bước Ôn tập: bài này + các bài trước nó trong cùng chủ đề. */
    fun reviewSources(lesson: TextbookLesson): List<PracticeSource> {
        val topic = topicOf(lesson)
        val upTo = topic.lessons.takeWhile { it.id != lesson.id } + lesson
        return upTo.flatMap { it.sources }.distinct()
    }
}

object Curricula {
    fun forGrade(grade: Int): GradeCurriculum? = when (grade) {
        1 -> Grade1Curriculum.data
        2 -> Grade2Curriculum.data
        3 -> Grade3Curriculum.data
        4 -> Grade4Curriculum.data
        5 -> Grade5Curriculum.data
        else -> null
    }

    fun findLesson(lessonId: String): Pair<GradeCurriculum, TextbookLesson>? =
        ((1..5).mapNotNull { forGrade(it) } + ThinkingCurricula.all)
            .firstNotNullOfOrNull { c -> c.lesson(lessonId)?.let { c to it } }
}

/**
 * TOÁN TƯ DUY – lộ trình riêng, KHÔNG khóa theo tiến độ sách: mở được bất cứ lúc nào, học theo thứ tự nào cũng được.
 * Trong một chuyên đề vẫn giữ trình tự sư phạm Bài giảng → Dạng 1 → Dạng 2 → Ôn tập.
 */
object ThinkingCurricula {
    private fun td(g: Int, n: Int, title: String, node: String, domain: String = "HEURISTICS_COMBINATORICS") =
        TextbookLesson("TD${g}_B$n", n, title, domain, listOf(PracticeSource(node)))

    private fun grade(g: Int, base: List<Pair<String, String>>, topics: List<Pair<String, String>>): GradeCurriculum {
        var n = 0
        return GradeCurriculum(
            grade = g, semester2FromTopic = 99,
            topics = listOf(
                TextbookTopic(1, "Nền tảng tư duy", base.map { (t, node) -> td(g, ++n, t, node) }),
                TextbookTopic(2, "Chuyên đề", topics.map { (t, node) -> td(g, ++n, t, node) })
            )
        )
    }

    val grade1 = grade(1,
        listOf("Tìm số còn thiếu – đi ngược" to "T1_03"),
        listOf("Tính nhanh – ghép số tròn chục" to "T1_01", "Dãy số theo quy luật" to "T1_02", "Bài toán nhiều hơn – ít hơn" to "T1_04"))

    val grade2 = grade(2,
        listOf("Tìm số còn thiếu – đi ngược" to "T2_03"),
        listOf("Tính nhanh" to "T2_01", "Dãy số theo quy luật" to "T2_02", "Hơn – kém và bài toán tuổi" to "T2_04"))

    val grade3 = grade(3,
        listOf("Tìm số còn thiếu – đi ngược" to "T3_04"),
        listOf("Tính nhanh" to "T3_01", "Dãy số theo quy luật" to "T3_02", "Bài toán tính tuổi" to "T3_03"))

    val grade4 = grade(4,
        listOf("Tính nhanh" to "T4_01", "Dãy số và đếm chữ số" to "T4_05"),
        listOf("Tìm hai số khi biết tổng và hiệu" to "T4_02", "Bài toán gấp lần (tổng – hiệu và số lần)" to "T4_03", "Trung bình cộng" to "T4_04",
            "Phân số: so sánh và tính nhanh" to "T4_06", "Phân số của một số" to "T4_07"))

    val grade5 = grade(5,
        listOf("Tính nhanh với số thập phân" to "T5_01"),
        listOf("Tỉ số phần trăm" to "T5_02", "Tìm hai số khi biết tổng (hiệu) và tỉ số" to "T5_03", "Chuyển động đều" to "T5_04"))

    val all: List<GradeCurriculum> get() = listOf(grade1, grade2, grade3, grade4, grade5)

    fun forGrade(grade: Int): GradeCurriculum? = all.firstOrNull { it.grade == grade }

    fun isThinking(lessonId: String) = lessonId.startsWith("TD")
}

/**
 * Sinh câu hỏi cho một bước của bài: chọn nguồn ngẫu nhiên, chọn độ khó theo bước, lọc theo nội dung bài.
 * Không tìm được câu đúng bộ lọc sau nhiều lần → dùng câu gần nhất của nguồn đó (không bao giờ kẹt).
 */
class LessonProblemGenerator(
    private val engine: ParametricProblemEngine = ParametricProblemEngine(),
    private val rng: Random = Random.Default
) {
    var lastFilterMiss = false
        private set

    fun next(curriculum: GradeCurriculum, lesson: TextbookLesson, stage: LessonStage, index: Int): AcademicProblem {
        val sources = if (stage == LessonStage.REVIEW) curriculum.reviewSources(lesson) else lesson.sources
        val tier = when (stage) {
            LessonStage.HARD -> AcademicTier.GIOI
            LessonStage.REVIEW -> if (index % 3 == 2) AcademicTier.GIOI else AcademicTier.TRUNG_BINH
            else -> AcademicTier.TRUNG_BINH
        }
        val scaffolding = when {
            stage == LessonStage.EASY && index < 3 -> ScaffoldingLevel.FADED_STEP
            else -> ScaffoldingLevel.INDEPENDENT
        }
        return generate(curriculum.grade, sources, tier, scaffolding)
    }

    /** Câu cho bài kiểm tra học kì: độ khó cao, không gợi ý. */
    fun exam(curriculum: GradeCurriculum, lesson: TextbookLesson, hardest: Boolean): AcademicProblem =
        generate(
            curriculum.grade, lesson.sources,
            if (hardest) AcademicTier.XUAT_SAC else AcademicTier.GIOI,
            ScaffoldingLevel.INDEPENDENT
        )

    private fun generate(
        grade: Int,
        sources: List<PracticeSource>,
        tier: AcademicTier,
        scaffolding: ScaffoldingLevel
    ): AcademicProblem {
        val source = sources[rng.nextInt(sources.size)]
        var last: AcademicProblem? = null
        repeat(MAX_TRIES) {
            val p = engine.generateProblemForNode(source.nodeId, grade, scaffolding, tier)
            if (source.filter.accepts(p.statementVi)) { lastFilterMiss = false; return p }
            last = p
        }
        // Độ khó cao có thể không có câu khớp (vd. bài chỉ dùng số 0–5): thử lại ở mức dễ
        if (tier != AcademicTier.TRUNG_BINH) repeat(MAX_TRIES) {
            val p = engine.generateProblemForNode(source.nodeId, grade, scaffolding, AcademicTier.TRUNG_BINH)
            if (source.filter.accepts(p.statementVi)) { lastFilterMiss = false; return p }
        }
        lastFilterMiss = true
        return last!!
    }

    companion object { const val MAX_TRIES = 120 }
}

/* ------------------------------ Tiến độ & mở khóa ------------------------------ */

enum class StageState { DONE, CURRENT, LOCKED }

/** Các bước của bài: mọi bài đều có Bài giảng trước Dạng 1. */
fun TextbookLesson.stages(): List<LessonStage> = LessonStage.entries

/**
 * [completed] = số bước đã xong theo thứ tự (LEARN=1, EASY=2, HARD=3, REVIEW=4) – lưu theo ordinal + 1.
 * (Trước đây bài không có nội dung dạy được bỏ qua bước đầu; nay mọi bài đều phải xem Bài giảng.)
 */
@Suppress("UnusedReceiverParameter")
fun TextbookLesson.effectiveCompleted(completed: Int): Int = completed

fun TextbookLesson.stageState(stage: LessonStage, completed: Int): StageState {
    val c = effectiveCompleted(completed)
    return when {
        stage.ordinal < c -> StageState.DONE
        stage.ordinal == c -> StageState.CURRENT
        else -> StageState.LOCKED
    }
}

/** Số bước đã xong / tổng số bước hiển thị của bài. */
fun TextbookLesson.doneCount(completed: Int): Int = stages().count { stageState(it, completed) == StageState.DONE }

/** Bài mở khi là bài đầu tiên, hoặc bài liền trước đã qua Dạng 1. */
fun GradeCurriculum.isUnlocked(lesson: TextbookLesson, progress: (String) -> Int): Boolean {
    val idx = lessons.indexOf(lesson)
    if (idx <= 0) return true
    val prev = lessons[idx - 1]
    // Dùng mức cao nhất từng đạt: bài trước bị "học lại" sau bài kiểm tra thì bài sau vẫn mở
    val best = maxOf(progress(prev.id), progress(LessonKeys.ever(prev.id)))
    return prev.effectiveCompleted(best) > LessonStage.EASY.ordinal
}

/** Khóa phụ trong bảng tiến độ (cùng một Map<String, Int>). */
object LessonKeys {
    fun ever(lessonId: String) = "ever:$lessonId"
    fun relearn(lessonId: String) = "relearn:$lessonId"
    fun exam(grade: Int, semester: Int) = "exam:$grade:$semester"
    /** 1 = bài được duyệt "thành thạo" qua thử thách vượt bài (không học trong app, không có xu). */
    fun mastered(lessonId: String) = "mastered:$lessonId"
    /** Số lượt Dạng 2 đúng trọn 12/12 câu ngay lần đầu (mỗi lượt là một phiên riêng). */
    fun perfect(lessonId: String) = "perfect:$lessonId"
}

/**
 * XẾP LOẠI một bài đã học xong cả 4 bước (thay cho "4/4"):
 *  - Hiểu bài   : qua đủ các bước (mỗi bước ≥ 80% đúng lần đầu).
 *  - Thành thạo : thêm 2 lượt Dạng 2 đúng trọn 12/12 (hai phiên khác nhau) – hoặc được duyệt qua thử thách.
 *  (Học giỏi / Xuất sắc: cần bộ câu nâng cao – bổ sung sau.)
 */
enum class LessonRating(val label: String) {
    HIEU_BAI("Hiểu bài"), THANH_THAO("Thành thạo");

    companion object {
        const val PERFECT_RUNS = 2
        fun of(lesson: TextbookLesson, progress: (String) -> Int): LessonRating? {
            if (progress(LessonKeys.mastered(lesson.id)) == 1) return THANH_THAO
            if (lesson.doneCount(progress(lesson.id)) < lesson.stages().size) return null
            return if (progress(LessonKeys.perfect(lesson.id)) >= PERFECT_RUNS) THANH_THAO else HIEU_BAI
        }
    }
}

/** Học kì đã sẵn sàng kiểm tra: mọi bài trong kì đã qua Dạng 1. */
fun GradeCurriculum.examReady(semester: Int, progress: (String) -> Int): Boolean =
    lessonsOf(semester).all { l ->
        val best = maxOf(progress(l.id), progress(LessonKeys.ever(l.id)))
        l.effectiveCompleted(best) > LessonStage.EASY.ordinal
    }

/* ------------------------------ Kiểm tra học kì ------------------------------ */

data class ExamQuestion(val lessonId: String, val problem: AcademicProblem)

/**
 * Đề kiểm tra học kì: [count] câu, rải đều các bài trong kì (ưu tiên bài luyện tập/ôn tập có nhiều dạng),
 * 1/3 số câu ở mức khó nhất. Mỗi câu ghi lại thuộc bài nào để biết bài nào phải học lại.
 */
class SemesterExamBuilder(
    private val generator: LessonProblemGenerator = LessonProblemGenerator(),
    private val rng: Random = Random.Default
) {
    fun build(curriculum: GradeCurriculum, semester: Int, count: Int = 20): List<ExamQuestion> {
        val lessons = curriculum.lessonsOf(semester)
        if (lessons.isEmpty()) return emptyList()
        // Chọn bài: đi vòng qua danh sách đã xáo, đủ số câu thì dừng → mỗi bài tối đa ~1–2 câu
        val order = lessons.shuffled(rng)
        val picked = (0 until count).map { order[it % order.size] }
        val seen = mutableSetOf<String>()
        return picked.mapIndexed { i, l ->
            var q = generator.exam(curriculum, l, hardest = i % 3 == 2)
            // tránh trùng y hệt đề
            repeat(5) { if (!seen.add(q.statementVi)) q = generator.exam(curriculum, l, hardest = i % 3 == 2) }
            ExamQuestion(l.id, q)
        }.sortedBy { q -> lessons.indexOfFirst { it.id == q.lessonId } }
    }
}

/* ------------------------------ Giải đấu ------------------------------ */

/**
 * Đề giải đấu: [count] câu khó gồm kiến thức lớp đang học (~60%) và các lớp dưới (~40%).
 * Lớp 1 thì toàn bộ là lớp 1. Phần lớp đang học ưu tiên các bài con đã mở (≥ 3 bài),
 * để đề khó nhưng công bằng. Câu lẻ (2, 4, …) ở mức khó nhất.
 */
class TournamentQuestionBuilder(
    private val generator: LessonProblemGenerator = LessonProblemGenerator(),
    private val rng: Random = Random.Default
) {
    fun build(grade: Int, progress: (String) -> Int, count: Int = 10): List<ExamQuestion> {
        val current = Curricula.forGrade(grade) ?: return emptyList()
        val opened = current.lessons.filter { current.isUnlocked(it, progress) }
        val currentPool = (if (opened.size >= 3) opened else current.lessons).map { current to it }
        val lowerPool = (1 until grade).mapNotNull { Curricula.forGrade(it) }.flatMap { c -> c.lessons.map { c to it } }
        val nCurrent = if (lowerPool.isEmpty()) count else Math.round(count * 0.6f)
        val picks = currentPool.shuffled(rng).let { p -> List(nCurrent) { p[it % p.size] } } +
            if (lowerPool.isEmpty()) emptyList() else lowerPool.shuffled(rng).take(count - nCurrent)
        val seen = mutableSetOf<String>()
        return picks.shuffled(rng).mapIndexed { i, (c, l) ->
            val hardest = i % 2 == 1
            var q = generator.exam(c, l, hardest)
            repeat(5) { if (!seen.add(q.statementVi)) q = generator.exam(c, l, hardest) }
            ExamQuestion(l.id, q)
        }
    }
}
