package com.example.toanhoc.ui.lecture

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/** Một giọng tiếng Việt có thể chọn. */
data class VoiceOption(val name: String, val label: String, val highQuality: Boolean, val needsNetwork: Boolean)

/** Các mức tốc độ cho bố mẹ chọn (1.0 = tốc độ gốc của bộ đọc). */
enum class VoiceSpeed(val label: String, val rate: Float) {
    SLOW("Chậm rãi", 0.82f), NORMAL("Vừa", 0.95f), NATURAL("Tự nhiên", 1.05f), FAST("Nhanh", 1.15f)
}

/**
 * Giọng đọc tiếng Việt có thể "chờ đọc xong" – để trình phát bài giảng chuyển hình đúng lúc lời đọc kết thúc.
 * [speak] trả về true khi đọc xong, false khi bị dừng/lỗi (vd. con bấm tạm dừng).
 *
 *  - Mặc định dùng GIỌNG CHUẨN Piper vi_VN-vais1000 chạy ngay trên máy (không cần mạng).
 *    Câu sau được tạo trong lúc câu trước đang đọc, nên không có khoảng chờ.
 *  - Nếu máy không chạy được Piper, tự quay về giọng Google có trên máy.
 *  - NGẮT NHỊP: tách câu thành từng cụm theo dấu câu, chèn khoảng lặng ngắn giữa các cụm
 *    (dấu phẩy ~0,2 s, hết câu ~0,4 s); cụm có phép tính đọc chậm hơn một chút.
 */
class LectureVoice(context: Context) {
    /** null = đang khởi động; true = có giọng tiếng Việt; false = máy không có (chạy bằng phụ đề). */
    var available by mutableStateOf<Boolean?>(null)
        private set
    var voices by mutableStateOf<List<VoiceOption>>(emptyList())
        private set
    var voiceName by mutableStateOf<String?>(null)
        private set
    var speed by mutableStateOf(VoiceSpeed.NORMAL)
        private set
    private var pitch by mutableFloatStateOf(1.05f)

    private val app = context.applicationContext
    private val prefs = app.getSharedPreferences("lecture_voice", Context.MODE_PRIVATE)
    private val pending = ConcurrentHashMap<String, CancellableContinuation<Boolean>>()
    private val batchLast = ConcurrentHashMap<String, String>()
    private var tts: TextToSpeech? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val piperBundled = PiperTts.bundled(app)
    private val piperLoad: Deferred<PiperTts.Engine?> = scope.async { if (piperBundled) PiperTts.get(app) else null }
    @Volatile private var piperJob: Job? = null
    private var piperOk: Boolean? = if (piperBundled) null else false
    private var googleOk: Boolean? = null
    private var googleVoices: List<Voice> = emptyList()

    init {
        speed = runCatching { VoiceSpeed.valueOf(prefs.getString("speed", null) ?: "") }.getOrDefault(VoiceSpeed.NORMAL)
        if (piperBundled) available = true
        scope.launch {
            val ok = piperLoad.await() != null
            launchMain { piperOk = ok; refreshVoices() }
        }
        var engine: TextToSpeech? = null
        val created = TextToSpeech(app) { status ->
            val e = engine
            if (status == TextToSpeech.SUCCESS && e != null) {
                val r = e.setLanguage(Locale("vi", "VN"))
                val ok = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
                if (ok) googleVoices = runCatching { e.voices?.toList() }.getOrNull().orEmpty()
                    .filter { it.locale.language == "vi" && !it.features.orEmpty().contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
                    // Giọng chất lượng cao trước, giọng chạy trên máy (không cần mạng) trước
                    .sortedWith(compareByDescending<Voice> { it.quality }.thenBy { it.isNetworkConnectionRequired }.thenBy { it.name })
                e.setPitch(pitch)
                googleOk = ok
            } else {
                googleOk = false
            }
            refreshVoices()
        }
        engine = created
        created.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) = finish(utteranceId, true)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = finish(utteranceId, false)
            override fun onError(utteranceId: String?, errorCode: Int) = finish(utteranceId, false)
            override fun onStop(utteranceId: String?, interrupted: Boolean) = finish(utteranceId, false)
        })
        tts = created
    }

    private fun launchMain(block: () -> Unit) { scope.launch(Dispatchers.Main) { block() } }

    /** Danh sách giọng = giọng chuẩn Piper (nếu chạy được) + các giọng Google; chọn lại giọng đã lưu. */
    private fun refreshVoices() {
        val list = mutableListOf<VoiceOption>()
        if (piperOk == true) list += VoiceOption(PiperTts.ID, "Giọng chuẩn", highQuality = true, needsNetwork = false)
        googleVoices.forEachIndexed { i, v ->
            list += VoiceOption(v.name, "Giọng Google ${i + 1}", v.quality >= Voice.QUALITY_HIGH, v.isNetworkConnectionRequired)
        }
        voices = list
        val saved = prefs.getString(KEY_VOICE, null)
        val pick = when {
            saved == PiperTts.ID && piperOk != false -> PiperTts.ID
            saved != null && googleVoices.any { it.name == saved } -> saved
            piperOk == true || (piperOk == null && saved == null) -> PiperTts.ID
            else -> (googleVoices.firstOrNull { !it.isNetworkConnectionRequired } ?: googleVoices.firstOrNull())?.name
        }
        voiceName = pick
        if (pick != null && pick != PiperTts.ID) {
            val e = tts
            googleVoices.firstOrNull { it.name == pick }?.let { v -> runCatching { e?.voice = v } }
        }
        available = when {
            piperOk != false && piperBundled -> true
            googleOk == true -> true
            googleOk == false -> false
            else -> available
        }
    }

    fun selectVoice(name: String) {
        if (name == PiperTts.ID) {
            voiceName = name
        } else {
            val e = tts ?: return
            val v = googleVoices.firstOrNull { it.name == name } ?: return
            runCatching { e.voice = v }
            voiceName = name
        }
        prefs.edit().putString(KEY_VOICE, name).apply()
    }

    fun selectSpeed(s: VoiceSpeed) {
        speed = s
        prefs.edit().putString("speed", s.name).apply()
    }

    private fun finish(id: String?, ok: Boolean) {
        if (id == null) return
        val batch = id.substringBefore('#')
        val c = pending[batch] ?: return
        if (!ok || batchLast[batch] == id) {
            pending.remove(batch); batchLast.remove(batch)
            if (c.isActive) c.resume(ok)
        }
    }

    suspend fun speak(text: String): Boolean {
        val parts = phrases(text)
        if (parts.isEmpty()) return true
        val wantPiper = voiceName == PiperTts.ID || (voiceName == null && piperBundled)
        if (wantPiper) {
            val engine = piperLoad.await()
            if (engine != null) return speakPiper(engine, text, parts)
        }
        return speakGoogle(text, parts)
    }

    /** Giọng Piper: tạo âm thanh từng cụm (cụm sau tạo trong lúc cụm trước đang phát). */
    private suspend fun speakPiper(engine: PiperTts.Engine, text: String, parts: List<Phrase>): Boolean {
        val budget = (text.length * 200L / speed.rate).toLong() + parts.size * 600L + 15000L
        val job = scope.async {
            coroutineScope {
                val ch = Channel<Pair<FloatArray, Int>>(capacity = 2)
                launch {
                    try {
                        parts.forEachIndexed { i, ph ->
                            val (pcm, sr) = engine.synth(ph.text, speed.rate * (if (ph.mathy) 0.93f else 1f))
                            ch.send(pcm to sr)
                            if (i < parts.lastIndex && ph.pauseMs > 0) ch.send(FloatArray((sr * ph.pauseMs / 1000).toInt()) to sr)
                        }
                    } finally { ch.close() }
                }
                PcmPlayer.playAll(ch)
            }
        }
        piperJob = job
        com.example.toanhoc.ui.sound.SoundEffectsManager.voiceActive = true
        return try {
            withTimeoutOrNull(budget) { job.await() } ?: run { job.cancel(); true }
        } catch (e: CancellationException) {
            job.cancel()
            currentCoroutineContext().ensureActive()
            false
        } catch (e: Throwable) {
            false
        } finally {
            if (piperJob === job) piperJob = null
            com.example.toanhoc.ui.sound.SoundEffectsManager.voiceActive = false
        }
    }

    private suspend fun speakGoogle(text: String, parts: List<Phrase>): Boolean {
        val engine = tts ?: return false
        if (googleOk != true) return false
        val budget = (text.length * 160L / speed.rate).toLong() + parts.size * 450L + 5000L
        com.example.toanhoc.ui.sound.SoundEffectsManager.voiceActive = true
        val result = try { withTimeoutOrNull(budget) {
            suspendCancellableCoroutine { cont ->
                val batch = UUID.randomUUID().toString()
                pending[batch] = cont
                cont.invokeOnCancellation { pending.remove(batch); batchLast.remove(batch); engine.stop() }
                var failed = false
                parts.forEachIndexed { i, ph ->
                    if (failed) return@forEachIndexed
                    val id = "$batch#$i"
                    if (i == parts.lastIndex) batchLast[batch] = id
                    engine.setSpeechRate(speed.rate * (if (ph.mathy) 0.93f else 1f))
                    engine.setPitch(if (ph.question) pitch + 0.04f else pitch)
                    val mode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                    if (engine.speak(ph.text, mode, null, id) != TextToSpeech.SUCCESS) failed = true
                    else if (i < parts.lastIndex && ph.pauseMs > 0) engine.playSilentUtterance(ph.pauseMs, TextToSpeech.QUEUE_ADD, "$batch#s$i")
                }
                engine.setPitch(pitch)
                if (failed) {
                    pending.remove(batch); batchLast.remove(batch)
                    if (cont.isActive) cont.resume(false)
                }
            }
        } } finally { com.example.toanhoc.ui.sound.SoundEffectsManager.voiceActive = false }
        return result ?: true
    }

    fun stop() {
        piperJob?.cancel()
        tts?.stop()
        pending.keys.toList().forEach { b -> pending.remove(b)?.let { if (it.isActive) it.resume(false) }; batchLast.remove(b) }
    }

    fun shutdown() {
        piperJob?.cancel()
        pending.values.forEach { if (it.isActive) it.resume(false) }
        pending.clear(); batchLast.clear()
        tts?.stop(); tts?.shutdown(); tts = null
        scope.cancel()
    }

    companion object {
        const val SAMPLE = "Năm nay mẹ 27 tuổi, con 5 tuổi. Hỏi 8 năm nữa, mẹ hơn con bao nhiêu tuổi?"
        /** Khoá mới: mọi người dùng chuyển sang giọng chuẩn Piper một lần, sau đó giữ lựa chọn của bố mẹ. */
        private const val KEY_VOICE = "voice_v2"

        internal data class Phrase(val text: String, val pauseMs: Long, val mathy: Boolean, val question: Boolean)

        /** Tách lời đọc thành cụm theo dấu câu, kèm khoảng lặng sau mỗi cụm. */
        internal fun phrases(text: String): List<Phrase> {
            val out = mutableListOf<Phrase>()
            val sentences = text.trim().split(Regex("""(?<=[.!?])\s+"""))
            for (s in sentences) {
                val chunks = s.split(Regex("""(?<=[,;])\s+|(?<=[^\d]:)\s+""")).map { it.trim() }.filter { it.isNotEmpty() }
                chunks.forEachIndexed { i, c ->
                    val endSentence = i == chunks.lastIndex
                    val mathy = Regex("""(?<!\p{L})(cộng|trừ|nhân|chia|bằng|phần)(?!\p{L})""").findAll(c).count() >= 2
                    out += Phrase(c, if (endSentence) 380L else 200L, mathy, c.endsWith("?"))
                }
            }
            return out
        }
    }
}

@Composable
fun rememberLectureVoice(): LectureVoice {
    val context = LocalContext.current
    val voice = remember { LectureVoice(context) }
    DisposableEffect(voice) { onDispose { voice.shutdown() } }
    return voice
}
