package com.example.toanhoc.ui.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.toanhoc.R
import kotlin.math.pow

/**
 * Bộ âm thanh của ứng dụng (tự tổng hợp, cùng "chất" marimba + chuông).
 *
 * Nguyên tắc:
 *  - Âm thanh BÁO TIN, không trách phạt: tiếng sai là tiếng "bụp" trầm, mềm.
 *  - Trả lời đúng liên tiếp → tiếng chuông CAO DẦN (mỗi câu lên 1 cung), sai thì về lại.
 *  - Khi giọng đọc bài giảng đang nói → hiệu ứng tự nhỏ lại (không đè lên lời giảng).
 *  - Bài kiểm tra chỉ có tiếng chạm (không báo đúng/sai từng câu).
 *  - Bố mẹ bật/tắt và chọn mức to nhỏ trong Phụ huynh › Cài đặt (lưu trên máy).
 */
enum class Sfx(val res: Int, val gain: Float) {
    TAP(R.raw.sfx_tap, 0.7f),
    TAB(R.raw.sfx_tab, 0.6f),
    APP_OPEN(R.raw.sfx_app_open, 0.6f),
    CORRECT(R.raw.sfx_correct, 0.85f),
    WRONG(R.raw.sfx_wrong, 0.8f),
    HINT(R.raw.sfx_hint, 0.75f),
    MISSION(R.raw.sfx_mission_start, 0.85f),
    FIXED(R.raw.sfx_twin_fixed, 1f),
    TICK(R.raw.sfx_score_tick, 0.6f),
    COMPLETE(R.raw.sfx_lesson_complete, 0.9f),
    SCENE(R.raw.sfx_scene, 0.6f),
    POP(R.raw.sfx_pop, 0.55f),
    DING(R.raw.sfx_ding, 0.85f),
    COIN(R.raw.sfx_coin, 0.8f),
    BADGE(R.raw.sfx_badge, 0.9f),
    COUNTDOWN(R.raw.sfx_countdown, 0.85f)
}

enum class SfxVolume(val label: String, val gain: Float) { LOW("Nhỏ", 0.45f), MID("Vừa", 0.75f), HIGH("To", 1f) }

object SoundEffectsManager {

    private var soundPool: SoundPool? = null
    private val ids = HashMap<Sfx, Int>()
    private var isInitialized = false

    /** Bật/tắt hiệu ứng (bố mẹ chỉnh). */
    var enabled by mutableStateOf(true)
        private set
    var volume by mutableStateOf(SfxVolume.MID)
        private set

    /** Giọng đọc đang nói → hạ hiệu ứng xuống còn 30%. */
    @Volatile var voiceActive: Boolean = false

    /** Số câu đúng liên tiếp hiện tại (để tiếng chuông cao dần). */
    var streak: Int = 0
        private set

    private const val PREFS = "sound_prefs"

    fun init(context: Context) {
        if (isInitialized) return
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            enabled = prefs.getBoolean("sfx_on", true)
            volume = runCatching { SfxVolume.valueOf(prefs.getString("sfx_volume", null) ?: "") }.getOrDefault(SfxVolume.MID)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val pool = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build()
            Sfx.entries.forEach { s -> ids[s] = pool.load(context.applicationContext, s.res, 1) }
            soundPool = pool
            isInitialized = true
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Failed to init SoundPool", e)
        }
    }

    fun setEnabled(context: Context, on: Boolean) {
        init(context); enabled = on
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean("sfx_on", on).apply()
    }

    fun setVolume(context: Context, v: SfxVolume) {
        init(context); volume = v
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString("sfx_volume", v.name).apply()
    }

    /** Phát một âm; [rate] 0.5–2.0 đổi cao độ (1 = gốc). */
    fun play(context: Context, sfx: Sfx, rate: Float = 1f) {
        try {
            init(context)
            if (!enabled) return
            val id = ids[sfx] ?: return
            if (id == 0) return
            val v = (sfx.gain * volume.gain * (if (voiceActive) 0.3f else 1f)).coerceIn(0f, 1f)
            soundPool?.play(id, v, v, 1, 0, rate.coerceIn(0.5f, 2f))
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error playing $sfx", e)
        }
    }

    /** Chạm / chọn. */
    fun playSelect(context: Context) = play(context, Sfx.TAP)

    /** Trả lời đúng: chuông cao dần theo chuỗi đúng liên tiếp (tối đa +1 quãng tám). */
    fun playSuccess(context: Context) {
        streak++
        val semis = ((streak - 1).coerceIn(0, 6) * 2).toDouble()
        play(context, Sfx.CORRECT, 2.0.pow(semis / 12.0).toFloat())
    }

    /** Trả lời sai: tiếng "bụp" mềm; chuỗi đúng về 0. */
    fun playFailure(context: Context) {
        streak = 0
        play(context, Sfx.WRONG)
    }

    /** Bắt đầu một phiên mới: chuỗi đúng tính lại từ đầu. */
    fun resetStreak() { streak = 0 }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            ids.clear()
            isInitialized = false
        } catch (e: Exception) {
            Log.e("SoundEffectsManager", "Error releasing SoundPool", e)
        }
    }
}
