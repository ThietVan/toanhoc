package com.example.toanhoc.domain.lecture

import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.LessonProblemGenerator
import com.example.toanhoc.domain.curriculum.LessonStage
import com.example.toanhoc.domain.curriculum.TextbookLesson
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.theory.KidTheories
import com.example.toanhoc.domain.theory.KidTheory
import com.example.toanhoc.domain.theory.Tok

/* ------------------------------------------------------------------------------------------
 * BÀI GIẢNG – bước bắt buộc trước Dạng 1 của mỗi bài.
 * Một bài giảng = chuỗi CẢNH; mỗi cảnh có nhiều NHỊP. Mỗi nhịp = 1 câu đọc to + hình đổi theo.
 * Trình phát đọc xong nhịp nào mới sang nhịp sau → hình và lời luôn khớp (giống video).
 *
 * Nội dung:
 *  - Bài có kịch bản riêng (SCRIPTS) → dùng kịch bản đó (vd. Số chẵn, số lẻ).
 *  - Bài khác → tự dựng từ phần dạy của bài (nếu có) + lý thuyết trẻ em (KidTheories) của một câu mẫu,
 *    cuối cùng là một câu đố con phải trả lời đúng.
 * ---------------------------------------------------------------------------------------- */

sealed interface LectureScene {
    val name: String
    val beats: List<String>

    data class Intro(val lessonNumber: Int, val lessonTitle: String) : LectureScene {
        override val name get() = "Mở đầu"
        override val beats get() = listOf(
            "Xin chào! Hôm nay mình cùng học bài: $lessonTitle.",
            "Con xem thật kĩ nhé, cuối bài có một câu đố nhỏ đấy!"
        )
    }

    /** Xếp kẹo thành từng đôi: vừa khít → số chẵn, thừa 1 → số lẻ. */
    data class Pairs(val count: Int, val owner: String, val girl: Boolean) : LectureScene {
        val even get() = count % 2 == 0
        override val name get() = if (even) "Số chẵn" else "Số lẻ"
        override val beats get() = listOf(
            "Bạn $owner có $count viên kẹo. " + COUNT_WORDS.take(count).joinToString(", ") + ".",
            if (even) "$owner xếp kẹo thành từng đôi, mỗi đôi 2 viên." else "$owner cũng xếp kẹo thành từng đôi.",
            if (even) "Vừa khít! Không thừa viên nào." else "Ồ! Còn thừa ra 1 viên, không có bạn để ghép đôi.",
            if (even) "Xếp đôi vừa khít thì là số chẵn. Vậy $count là số chẵn."
            else "Xếp đôi mà thừa 1 thì là số lẻ. Vậy $count là số lẻ."
        )
    }

    /** Dãy số 0..[to]: tô số chẵn, số lẻ, rồi nhấn mạnh xen kẽ. */
    data class EvenOddRow(val to: Int = 13) : LectureScene {
        override val name get() = "Dãy số"
        override val beats get() = listOf(
            "Cùng nhìn dãy số từ 0 đến $to nhé.",
            "Các số chẵn là: " + (0..to step 2).joinToString(", ") + ".",
            "Các số lẻ là: " + (1..to step 2).joinToString(", ") + ".",
            "Chẵn, lẻ, chẵn, lẻ. Hai loại số luôn đứng xen kẽ nhau."
        )
    }

    /** Mẹo nhìn chữ số cuối. */
    data class LastDigit(val evenNumber: Int, val oddNumber: Int) : LectureScene {
        override val name get() = "Mẹo nhanh"
        override val beats get() = listOf(
            "Số lớn thì sao? Không cần xếp kẹo đâu. Chỉ cần nhìn chữ số cuối cùng!",
            "Số $evenNumber có chữ số cuối là ${evenNumber % 10}. ${evenNumber % 10} là số chẵn, nên $evenNumber là số chẵn.",
            "Số $oddNumber có chữ số cuối là ${oddNumber % 10}. ${oddNumber % 10} là số lẻ, nên $oddNumber là số lẻ.",
            "Ghi nhớ nhé! Chữ số cuối là 0, 2, 4, 6, 8 thì là số chẵn. Là 1, 3, 5, 7, 9 thì là số lẻ."
        )
    }

    /** Khung GHI NHỚ của lý thuyết: mỗi dòng quy tắc là một nhịp, thẻ mẫu là nhịp cuối. */
    data class Rule(val theory: KidTheory) : LectureScene {
        override val name get() = "Ghi nhớ"
        override val beats get() = listOf("Cùng ghi nhớ cách làm nhé.") +
            theory.rule.map { line -> line.joinToString("") { tokText(it) }.trim() } +
            (if (theory.tiles.isNotEmpty()) listOf("Con xem các ví dụ nhỏ này.") else emptyList())
    }

    /** VÍ DỤ: đề → từng bước → kết luận. */
    data class Example(val theory: KidTheory, val label: String? = null, val intro: String? = null) : LectureScene {
        override val name get() = label ?: "Ví dụ"
        override val beats get() = listOf(intro ?: "Cùng làm một ví dụ. ${theory.example}") +
            theory.steps.mapIndexed { i, st ->
                buildString {
                    append("Bước ${i + 1}. ").append(st.title).append(". ")
                    if (st.line.isNotEmpty()) append(st.line.joinToString(" ") { tokText(it).trim() }).append(". ")
                    st.note?.let { append(it) }
                }.trim()
            } + theory.conclusion
    }

    /** CÁCH NHANH (toán tư duy): mẹo rút gọn cho chính bài vừa giải theo cách ở trường. */
    data class Quick(val theory: KidTheory) : LectureScene {
        override val name get() = "Cách nhanh"
        override val beats get() = listOf("Bây giờ là cách nhanh hơn." + (theory.quickRule?.let { " $it" } ?: "")) +
            theory.quick.map { st ->
                buildString {
                    append(st.title).append(". ")
                    if (st.line.isNotEmpty()) append(st.line.joinToString(" ") { tokText(it).trim() }).append(". ")
                    st.note?.let { append(it) }
                }.trim()
            } + "Hai cách đều ra cùng một đáp số."
    }

    /** Đáp án con đã chọn sai + lỗi sai tương ứng. */
    data class Mistake(val statement: String, val picked: String, val note: String) : LectureScene {
        override val name get() = "Con chọn $picked"
        override val beats get() = listOf("Con đã chọn $picked. Chưa đúng rồi, không sao cả!", note)
    }

    /** Dự phòng khi chưa dựng được lý thuyết: đề + các bước của bộ sinh đề + đáp án. */
    data class Steps(val statement: String, val lines: List<String>, val answer: String) : LectureScene {
        override val name get() = "Ví dụ"
        override val beats get() = listOf("Cùng làm một ví dụ. $statement") + lines + "Vậy đáp án là $answer."
    }

    /** Thẻ dạy có sẵn trong chương trình: tiêu đề + các dòng giảng. */
    data class Card(val title: String, val lines: List<String>) : LectureScene {
        override val name get() = title
        override val beats get() = lines.mapIndexed { i, l -> if (i == 0) "$title. $l" else l }.ifEmpty { listOf(title) }
    }

    data class Tips(val tips: List<String>) : LectureScene {
        override val name get() = "Mẹo"
        override val beats get() = tips.mapIndexed { i, t -> "Mẹo ${i + 1}. $t" }
    }

    /** Câu đố: con phải chọn đúng mới đi tiếp. */
    data class Quiz(val question: String, val options: List<String>, val answer: String, val explain: String) : LectureScene {
        override val name get() = "Đố vui"
        override val beats get() = listOf("$question Con bấm vào câu trả lời nhé.")
    }

    data class Outro(
        val lessonTitle: String,
        val cta: String = "Bắt đầu làm Dạng 1",
        val say: String = "Giỏi lắm! Con đã xem xong bài giảng. Giờ mình cùng làm bài tập Dạng 1 nhé.",
        val headline: String = "Con hiểu bài rồi!"
    ) : LectureScene {
        override val name get() = "Hoàn thành"
        override val beats get() = listOf(say)
    }

    companion object {
        val COUNT_WORDS = listOf("Một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười")
        fun tokText(t: Tok) = when (t) { is Tok.T -> t.s; is Tok.P -> t.s }
    }
}

data class Lecture(val lessonId: String, val title: String, val scenes: List<LectureScene>)

object Lectures {

    /** Kịch bản viết tay theo mã bài. Thêm bài mới = thêm một dòng. */
    private val SCRIPTS: Map<String, () -> List<LectureScene>> = mapOf(
        "L4_B3" to ::evenOdd
    )

    private fun evenOdd(): List<LectureScene> = listOf(
        LectureScene.Pairs(6, "Nam", girl = false),
        LectureScene.Pairs(7, "Lan", girl = true),
        LectureScene.EvenOddRow(13),
        LectureScene.LastDigit(124, 357),
        LectureScene.Quiz("58 là số chẵn hay số lẻ?", listOf("Số chẵn", "Số lẻ"), "Số chẵn", "Chữ số cuối là 8, nên 58 là số chẵn.")
    )

    fun hasScript(lessonId: String) = lessonId in SCRIPTS

    fun build(curriculum: GradeCurriculum, lesson: TextbookLesson, gen: LessonProblemGenerator = LessonProblemGenerator()): Lecture {
        val scenes = mutableListOf<LectureScene>(LectureScene.Intro(lesson.number, lesson.title))
        val script = SCRIPTS[lesson.id]
        if (script != null) {
            scenes += script()
        } else {
            val samples = (0 until 8).map { gen.next(curriculum, lesson, LessonStage.EASY, 3 + it) }
            val withTheory = samples.firstNotNullOfOrNull { p -> runCatching { KidTheories.build(p) }.getOrNull()?.let { p to it } }
            lesson.teaching?.cards?.forEach { c -> scenes += LectureScene.Card(c.title, c.body.lines().map { it.trim() }.filter { it.isNotEmpty() }) }
            if (withTheory != null) {
                val theory = withTheory.second
                if (theory.rule.isNotEmpty()) scenes += LectureScene.Rule(theory)
                if (theory.quick.isNotEmpty()) {
                    scenes += LectureScene.Example(theory, label = "Cách ở trường")
                    scenes += LectureScene.Quick(theory)
                } else scenes += LectureScene.Example(theory)
            } else if (lesson.teaching == null) {
                scenes += stepsOf(samples.first())
            }
            lesson.teaching?.tips?.takeIf { it.isNotEmpty() }?.let { scenes += LectureScene.Tips(it.take(3)) }

            val used = withTheory?.first?.statementVi
            val quiz = lesson.teaching?.tryIt?.firstOrNull()?.let { LectureScene.Quiz(it.statement, it.options, it.answer, it.explain) }
                ?: samples.filter { it.statementVi != used }.firstNotNullOfOrNull { quizOf(it) }
                ?: samples.firstNotNullOfOrNull { quizOf(it) }
            quiz?.let { scenes += it }
        }
        scenes += LectureScene.Outro(lesson.title)
        return Lecture(lesson.id, lesson.title, scenes)
    }

    /**
     * "Video giải câu này": dựng riêng cho câu con vừa làm sai – nói lỗi sai của đáp án con chọn,
     * giải lại CHÍNH bài đó theo cách ở trường, rồi cách nhanh (nếu có). null nếu chưa dựng được.
     */
    fun forMistake(problem: AcademicProblem, picked: String?): Lecture? {
        val theory = runCatching { KidTheories.build(problem) }.getOrNull() ?: return null
        val scenes = mutableListOf<LectureScene>()
        val note = picked?.let { theory.errorNotes[it] }
        if (picked != null && note != null) scenes += LectureScene.Mistake(problem.statementVi, picked, note)
        if (theory.rule.isNotEmpty()) scenes += LectureScene.Rule(theory)
        scenes += LectureScene.Example(
            theory, label = if (theory.quick.isNotEmpty()) "Cách ở trường" else "Lời giải",
            intro = "Cùng giải lại chính bài này. ${theory.example}"
        )
        if (theory.quick.isNotEmpty()) scenes += LectureScene.Quick(theory)
        scenes += LectureScene.Outro(
            "Giải câu này", cta = "Làm tiếp", headline = "Con hiểu rồi!",
            say = "Con đã hiểu cách giải rồi. Mình làm tiếp câu sau nhé!"
        )
        return Lecture(problem.problemId, "Giải câu này", scenes)
    }

    private fun stepsOf(p: AcademicProblem): LectureScene.Steps {
        val visible = p.steps.filter { !it.isHiddenForStudent }.ifEmpty { p.steps }
        val lines = visible.mapNotNull { s ->
            listOf(s.promptVi, s.mathExpression).filter { it.isNotBlank() && it != p.statementVi }.joinToString(": ").ifBlank { null }
        }
        return LectureScene.Steps(p.statementVi, lines.take(4), p.steps.last().expectedAnswer)
    }

    /** Câu đố từ một câu sinh ra: dùng 4 lựa chọn sẵn có, hoặc tự tạo nhiễu cho đáp án là số. */
    fun quizOf(p: AcademicProblem): LectureScene.Quiz? {
        val step = p.steps.firstOrNull() ?: return null
        val answer = step.expectedAnswer.trim()
        if (answer.isEmpty()) return null
        val options = step.options.map { it.trim() }.distinct().takeIf { it.size >= 2 && answer in it }
            ?: numericOptions(answer) ?: return null
        val explain = runCatching { KidTheories.build(p)?.conclusion }.getOrNull() ?: "Đáp án đúng là $answer."
        return LectureScene.Quiz(p.statementVi, options, answer, explain)
    }

    private fun numericOptions(answer: String): List<String>? {
        val a = answer.replace(" ", "").toLongOrNull() ?: return null
        val wrong = listOf(a + 1, a - 1, a + 10, a - 10, a + 2, a * 10).filter { it >= 0 && it != a }.distinct().take(3)
        if (wrong.size < 3) return null
        return (wrong + a).shuffled().map { it.toString() }
    }
}

/** Đổi chữ hiển thị sang lời đọc (xem [VnSpeech]): "2 < 3" → "hai bé hơn ba", "3/4" → "ba phần tư". */
fun speakable(s: String): String = VnSpeech.toSpeech(s)
