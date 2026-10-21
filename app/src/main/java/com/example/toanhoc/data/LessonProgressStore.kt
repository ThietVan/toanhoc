package com.example.toanhoc.data

import android.content.Context
import com.example.toanhoc.domain.curriculum.LessonKeys
import com.example.toanhoc.domain.curriculum.LessonStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tiến độ bài trong sách, lưu trong SharedPreferences (không đổi cơ sở dữ liệu):
 *   "<lessonId>"          → số bước đã xong hiện tại
 *   "ever:<lessonId>"     → số bước cao nhất từng đạt (để bài sau không bị khóa lại)
 *   "relearn:<lessonId>"  → 1 nếu bài phải học lại sau bài kiểm tra học kì
 *   "exam:<lớp>:<kì>"     → điểm cao nhất bài kiểm tra học kì (%)
 */
class LessonProgressStore(context: Context, childId: String? = null) {
    // Hồ sơ con đầu tiên (c1) dùng lại tệp cũ để giữ tiến độ đã học trước khi có nhiều hồ sơ
    private val prefs = context.getSharedPreferences(
        if (childId == null || childId == "c1") "lesson_progress" else "lesson_progress_$childId",
        Context.MODE_PRIVATE
    )
    private val _snapshot = MutableStateFlow(readAll())
    val snapshot: StateFlow<Map<String, Int>> = _snapshot.asStateFlow()

    fun completed(lessonId: String): Int = prefs.getInt(lessonId, 0)

    /** [totalStages]: số bước của bài – học lại xong bước cuối thì bỏ cờ "cần học lại". */
    fun markDone(lessonId: String, stage: LessonStage, totalStages: Int = LessonStage.entries.size): MarkResult {
        val n = stage.ordinal + 1
        val firstTime = n > prefs.getInt(LessonKeys.ever(lessonId), 0)
        val wasRelearn = prefs.getInt(LessonKeys.relearn(lessonId), 0) == 1
        val finishing = stage == LessonStage.entries.last() || n >= totalStages
        val e = prefs.edit()
        if (n > completed(lessonId)) e.putInt(lessonId, n)
        if (firstTime) e.putInt(LessonKeys.ever(lessonId), n)
        if (finishing) e.remove(LessonKeys.relearn(lessonId))
        e.apply()
        _snapshot.value = readAll()
        return MarkResult(firstTime = firstTime, relearnFinished = wasRelearn && finishing)
    }

    fun isMastered(lessonId: String): Boolean = prefs.getInt(LessonKeys.mastered(lessonId), 0) == 1

    /**
     * Bài được duyệt qua thử thách vượt bài: coi như xong cả 4 bước, nhưng KHÔNG ghi "ever"
     * (không tính huy hiệu bài đã học, không mở thưởng xu cho các bước đó).
     */
    fun markMastered(lessonIds: Collection<String>) {
        if (lessonIds.isEmpty()) return
        val e = prefs.edit()
        lessonIds.forEach { id ->
            if (completed(id) < LessonStage.entries.size) {
                e.putInt(id, LessonStage.entries.size)
                e.putInt(LessonKeys.mastered(id), 1)
            }
            e.remove(LessonKeys.relearn(id))
        }
        e.apply()
        _snapshot.value = readAll()
    }

    /** Một lượt Dạng 2 đúng trọn 12/12 → +1 (dùng cho xếp loại "Thành thạo"). Trả về số lượt sau khi cộng. */
    fun recordPerfectHard(lessonId: String): Int {
        val n = prefs.getInt(LessonKeys.perfect(lessonId), 0) + 1
        prefs.edit().putInt(LessonKeys.perfect(lessonId), n).apply()
        _snapshot.value = readAll()
        return n
    }

    fun needsRelearn(lessonId: String): Boolean = prefs.getInt(LessonKeys.relearn(lessonId), 0) == 1

    /** Bài kiểm tra chưa đạt: các bài có câu sai phải học lại từ đầu. */
    fun resetForRelearn(lessonIds: Collection<String>) {
        val e = prefs.edit()
        lessonIds.forEach { id ->
            val cur = completed(id)
            val mastered = isMastered(id)
            // Bài "thành thạo qua thử thách" bị trả về: con học thật từ đầu (có xu như bình thường)
            if (!mastered && cur > prefs.getInt(LessonKeys.ever(id), 0)) e.putInt(LessonKeys.ever(id), cur)
            e.remove(LessonKeys.mastered(id))
            e.putInt(id, 0)
            e.putInt(LessonKeys.relearn(id), 1)
        }
        e.apply()
        _snapshot.value = readAll()
    }

    /** Lưu điểm cao nhất; trả về điểm cao nhất trước đó (-1 nếu chưa làm). */
    fun recordExam(grade: Int, semester: Int, percent: Int): Int {
        val k = LessonKeys.exam(grade, semester)
        val before = prefs.getInt(k, -1)
        if (percent > before) {
            prefs.edit().putInt(k, percent).apply()
            _snapshot.value = readAll()
        }
        return before
    }

    fun snapshotNow(): Map<String, Int> = readAll()

    /** Xóa toàn bộ tiến độ (khi phụ huynh xóa hồ sơ con). */
    fun clearAll() {
        prefs.edit().clear().apply()
        _snapshot.value = emptyMap()
    }

    private fun readAll(): Map<String, Int> =
        prefs.all.mapNotNull { (k, v) -> (v as? Int)?.let { k to it } }.toMap()
}

/** [firstTime]: lần đầu qua bước này (có xu); [relearnFinished]: vừa học lại xong bài bị trả về. */
data class MarkResult(val firstTime: Boolean, val relearnFinished: Boolean)
