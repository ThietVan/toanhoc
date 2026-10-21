package com.example.toanhoc.ui.lecture

import android.content.Context
import android.content.res.AssetManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/* ------------------------------------------------------------------------------------------
 * GIỌNG PIPER vi_VN-vais1000 chạy ngay trên máy (không cần mạng) qua thư viện sherpa-onnx.
 *
 *  - Mô hình + dữ liệu phát âm được Gradle tự tải khi build (xem app/build.gradle.kts) và đóng
 *    vào thư mục assets "piper/": model.onnx, tokens.txt, espeak-ng-data/.
 *  - Gọi sherpa-onnx bằng phản chiếu (reflection): app vẫn build và chạy bình thường nếu thiếu
 *    thư viện/mô hình – khi đó LectureVoice tự quay về giọng Google trên máy.
 *
 * Ghi nguồn: giọng Piper vi_VN-vais1000 (MIT), dữ liệu VAIS-1000 – Truong Do (CC BY 4.0);
 * sherpa-onnx (Apache 2.0); espeak-ng (GPL-3.0).
 * ---------------------------------------------------------------------------------------- */
internal object PiperTts {
    const val ID = "piper:vi_VN-vais1000"
    const val CREDIT = "Giọng chuẩn: Piper vi_VN-vais1000, dữ liệu VAIS-1000 (Truong Do, CC BY 4.0). " +
        "Bộ đọc: sherpa-onnx (Apache 2.0), espeak-ng (GPL-3.0)."
    private const val DIR = "piper"
    private const val TAG = "PiperTts"

    private val mutex = Mutex()
    @Volatile private var engine: Engine? = null
    @Volatile private var failed = false

    /** Một bộ đọc đã nạp mô hình. Không đọc song song – mỗi lần một câu. */
    class Engine internal constructor(
        private val tts: Any, private val generate: Method, private val samplesOf: Method, private val rateOf: Method
    ) {
        /** Đọc [text] thành mẫu âm thanh (float −1..1) + tần số lấy mẫu. [speed] > 1 là nhanh hơn. */
        fun synth(text: String, speed: Float): Pair<FloatArray, Int> {
            val audio = synchronized(this) { generate.invoke(tts, text, 0, speed) }
            return (samplesOf.invoke(audio) as FloatArray) to (rateOf.invoke(audio) as Int)
        }
    }

    /** true nếu bản cài có kèm mô hình Piper. */
    fun bundled(context: Context): Boolean =
        runCatching { context.assets.list(DIR)?.contains("model.onnx") == true }.getOrDefault(false)

    /** Nạp bộ đọc (lần đầu mất 1–2 giây, sau đó dùng lại). null nếu máy không chạy được. */
    suspend fun get(context: Context): Engine? {
        engine?.let { return it }
        if (failed) return null
        return mutex.withLock {
            engine ?: if (failed) null else withContext(Dispatchers.IO) {
                runCatching { load(context.applicationContext) }
                    .onFailure { Log.w(TAG, "Không nạp được giọng Piper, dùng giọng Google", it); failed = true }
                    .getOrNull()
            }.also { engine = it }
        }
    }

    private fun load(ctx: Context): Engine {
        require(bundled(ctx)) { "Bản cài không kèm mô hình Piper" }
        val dataDir = copyEspeakData(ctx)
        val pkg = "com.k2fsa.sherpa.onnx."
        val vits = make(pkg + "OfflineTtsVitsModelConfig")
        set(vits, "model", "$DIR/model.onnx")
        set(vits, "tokens", "$DIR/tokens.txt")
        set(vits, "dataDir", dataDir)
        val model = make(pkg + "OfflineTtsModelConfig")
        set(model, "vits", vits)
        set(model, "numThreads", Runtime.getRuntime().availableProcessors().coerceIn(1, 4))
        set(model, "debug", false)
        set(model, "provider", "cpu")
        val cfg = make(pkg + "OfflineTtsConfig")
        set(cfg, "model", model)
        val cls = Class.forName(pkg + "OfflineTts")
        val ctor = cls.constructors.first {
            it.parameterTypes.size == 2 && it.parameterTypes[0] == AssetManager::class.java
        }
        val tts = ctor.newInstance(ctx.assets, cfg)
        val gen = cls.methods.first {
            it.name == "generate" && it.parameterTypes.contentEquals(
                arrayOf(String::class.java, Int::class.javaPrimitiveType, Float::class.javaPrimitiveType)
            )
        }
        val audioCls = gen.returnType
        return Engine(tts, gen, audioCls.getMethod("getSamples"), audioCls.getMethod("getSampleRate"))
    }

    /** Tạo lớp cấu hình Kotlin với giá trị mặc định (hàm dựng không tham số, hoặc hàm dựng "mặc định" của Kotlin). */
    private fun make(name: String): Any {
        val cls = Class.forName(name)
        runCatching { return cls.getDeclaredConstructor().newInstance() }
        val c: Constructor<*> = cls.declaredConstructors.first {
            it.parameterTypes.lastOrNull()?.name == "kotlin.jvm.internal.DefaultConstructorMarker"
        }
        c.isAccessible = true
        val types = c.parameterTypes
        // [tham số…, mặt nạ int (1 hoặc nhiều), DefaultConstructorMarker]: bật mọi bit = dùng mọi giá trị mặc định
        val nMasks = types.reversed().drop(1).takeWhile { it == Int::class.javaPrimitiveType }.size
        val nParams = types.size - 1 - nMasks
        val args = Array<Any?>(types.size) { i ->
            when {
                i >= nParams && i < types.size - 1 -> -1
                i == types.size - 1 -> null
                else -> zero(types[i])
            }
        }
        return c.newInstance(*args)
    }

    private fun zero(t: Class<*>): Any? = when (t) {
        Int::class.javaPrimitiveType -> 0; Long::class.javaPrimitiveType -> 0L; Float::class.javaPrimitiveType -> 0f
        Double::class.javaPrimitiveType -> 0.0; Boolean::class.javaPrimitiveType -> false
        Short::class.javaPrimitiveType -> 0.toShort(); Byte::class.javaPrimitiveType -> 0.toByte(); Char::class.javaPrimitiveType -> ' '
        else -> null
    }

    private fun set(o: Any, prop: String, v: Any) {
        val setter = "set" + prop.replaceFirstChar { it.uppercase() }
        val m = o.javaClass.methods.firstOrNull { it.name == setter && it.parameterTypes.size == 1 }
        if (m != null) { m.invoke(o, v); return }
        val f = o.javaClass.getDeclaredField(prop)
        f.isAccessible = true
        f.set(o, v)
    }

    /** espeak-ng cần thư mục thật trên máy: chép từ assets sang bộ nhớ app (một lần cho mỗi bản cài). */
    private fun copyEspeakData(ctx: Context): String {
        val root = File(ctx.filesDir, DIR)
        val dest = File(root, "espeak-ng-data")
        val stamp = File(root, ".version")
        val ver = runCatching {
            @Suppress("DEPRECATION")
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).lastUpdateTime.toString()
        }.getOrDefault("1")
        if (dest.isDirectory && stamp.isFile && stamp.readText() == ver) return dest.absolutePath
        dest.deleteRecursively()
        copyAsset(ctx.assets, "$DIR/espeak-ng-data", dest)
        root.mkdirs()
        stamp.writeText(ver)
        return dest.absolutePath
    }

    private fun copyAsset(am: AssetManager, path: String, dest: File) {
        val children = am.list(path).orEmpty()
        if (children.isEmpty()) {
            dest.parentFile?.mkdirs()
            am.open(path).use { input -> dest.outputStream().use { input.copyTo(it) } }
        } else {
            dest.mkdirs()
            children.forEach { copyAsset(am, "$path/$it", File(dest, it)) }
        }
    }
}

/** Phát lần lượt các đoạn âm thanh (float, mono) qua một AudioTrack; dừng ngay khi coroutine bị huỷ. */
internal object PcmPlayer {
    suspend fun playAll(chunks: ReceiveChannel<Pair<FloatArray, Int>>): Boolean = withContext(Dispatchers.IO) {
        var track: AudioTrack? = null
        var rate = 22050
        var written = 0L
        try {
            for ((pcm, sr) in chunks) {
                val t = track ?: newTrack(sr).also { track = it; rate = sr; it.play() }
                var off = 0
                while (off < pcm.size) {
                    currentCoroutineContext().ensureActive()
                    val n = t.write(pcm, off, minOf(4096, pcm.size - off), AudioTrack.WRITE_BLOCKING)
                    if (n <= 0) return@withContext false
                    off += n
                }
                written += pcm.size
            }
            val t = track ?: return@withContext true
            // Chờ loa phát hết phần đã ghi (có giới hạn thời gian để không treo).
            val deadline = System.currentTimeMillis() + written * 1000 / rate + 1500
            while ((t.playbackHeadPosition.toLong() and 0xFFFFFFFFL) < written && System.currentTimeMillis() < deadline) delay(25)
            true
        } finally {
            track?.let { t -> runCatching { t.pause(); t.flush(); t.stop() }; t.release() }
        }
    }

    private fun newTrack(sampleRate: Int): AudioTrack {
        val min = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        return AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
            )
            .setAudioFormat(
                AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(min, sampleRate * 4 / 2))
            .build()
    }
}
