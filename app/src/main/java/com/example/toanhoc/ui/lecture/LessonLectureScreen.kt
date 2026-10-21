package com.example.toanhoc.ui.lecture

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.R
import com.example.toanhoc.domain.curriculum.GradeCurriculum
import com.example.toanhoc.domain.curriculum.TextbookLesson
import com.example.toanhoc.domain.lecture.Lecture
import com.example.toanhoc.domain.lecture.LectureScene
import com.example.toanhoc.domain.lecture.Lectures
import com.example.toanhoc.domain.lecture.speakable
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theory.RichLine
import com.example.toanhoc.ui.theory.StepRow
import com.example.toanhoc.ui.theory.TileGrid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private object L {
    val Ink = Color(0xFF1D2433); val Muted = Color(0xFF5B6476); val Line = Color(0xFFDCE3EF); val Paper = Color(0xFFEEF3FB)
    val Blue = Color(0xFF2F6BEA); val BlueSoft = Color(0xFFE3ECFF); val BlueLine = Color(0xFFBFD2FF)
    val Green = Color(0xFF1F9D55); val GreenSoft = Color(0xFFDDF5E7); val GreenLine = Color(0xFFB6E6CB)
    val Orange = Color(0xFFEF7F12); val OrangeSoft = Color(0xFFFFEBD6); val OrangeInk = Color(0xFFB45A00)
    val Red = Color(0xFFD64545); val RedSoft = Color(0xFFFDE8E8)
    val StageTop = Color(0xFFFFF7EA); val StageBottom = Color(0xFFFFEFD9); val Tray = Color(0xFFC9BFAE)
    val Amber = Color(0xFFB45F06); val AmberBg = Color(0xFFFBF3EA); val AmberLine = Color(0xFFF3DDC2); val AmberDot = Color(0xFFE8952E)
    val Candy = listOf(Color(0xFFFF6B8B), Color(0xFF4D8DFF), Color(0xFFFFC23D), Color(0xFF3CC48A), Color(0xFFB07CFF), Color(0xFFFF9F45), Color(0xFF29C5D6))
}

private const val BOY_RATIO = 400f / 958f
private const val GIRL_RATIO = 446f / 882f

@Composable
private fun T(
    text: String, size: TextUnit, weight: FontWeight = FontWeight.Bold, color: Color = L.Ink,
    modifier: Modifier = Modifier, align: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified, maxLines: Int = Int.MAX_VALUE
) = Text(
    text, modifier, color = color, fontSize = size, fontWeight = weight, fontFamily = NunitoFontFamily,
    textAlign = align, lineHeight = lineHeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis
)


/** Như T(), nhưng phân số (3/4) hiển thị tử trên – mẫu dưới. */
@Composable
private fun MT(
    text: String, size: TextUnit, weight: FontWeight = FontWeight.Bold, color: Color = L.Ink,
    modifier: Modifier = Modifier, align: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified
) {
    if (!com.example.toanhoc.ui.design.hasFraction(text)) { T(text, size, weight, color, modifier, align, lineHeight); return }
    if (text.length <= 12) Box(modifier, contentAlignment = if (align == TextAlign.Center) Alignment.Center else Alignment.CenterStart) {
        com.example.toanhoc.ui.design.InlineMath(text, size, color, weight)
    } else com.example.toanhoc.ui.design.MathParagraph(text, size, color, weight, align ?: TextAlign.Start, modifier)
}

/**
 * Bước "Bài giảng" của một bài: trình phát cảnh có hình động + giọng đọc, tự chuyển như video.
 * Lần đầu: phải xem lần lượt (không tua tới cảnh chưa xem) và trả lời đúng câu đố mới vào được Dạng 1.
 * [watchedBefore] = đã từng xem xong → được tua tự do và có nút "Vào làm bài".
 */
@Composable
fun LessonLectureScreen(
    curriculum: GradeCurriculum,
    lesson: TextbookLesson,
    watchedBefore: Boolean,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)
    val lecture by produceState<Lecture?>(null, lesson.id) {
        value = withContext(Dispatchers.Default) { Lectures.build(curriculum, lesson) }
    }
    Column(modifier.fillMaxSize().background(L.Paper)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Quay lại", tint = L.Ink) }
            Column(Modifier.weight(1f)) {
                T("BÀI GIẢNG · BÀI ${lesson.number}", 12.sp, FontWeight.ExtraBold, L.Muted)
                T(lesson.title, 18.sp, FontWeight.Black, maxLines = 1)
            }
            if (watchedBefore) {
                Row(
                    Modifier.heightIn(min = 40.dp).clip(CircleShape).background(Color.White).border(1.dp, L.Line, CircleShape)
                        .clickable(role = Role.Button, onClick = onFinish).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) { T("Vào làm bài", 13.sp, FontWeight.ExtraBold, L.Blue) }
            }
        }
        val lec = lecture
        if (lec == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = L.Blue) }
        } else {
            LecturePlayer(lec, freeSeek = watchedBefore, onFinish = onFinish)
        }
    }
}

/**
 * Lớp phủ toàn màn hình phát một bài giảng có sẵn (vd. "video giải câu này" khi con làm sai).
 * Được tua tự do; [onDone] khi bấm nút cuối, [onClose] khi bấm X hoặc nút quay lại.
 */
@Composable
fun LectureOverlay(lecture: Lecture, kicker: String, title: String, onClose: () -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onClose)
    Column(modifier.fillMaxSize().background(L.Paper)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = L.Ink) }
            Column(Modifier.weight(1f)) {
                T(kicker, 12.sp, FontWeight.ExtraBold, L.Muted)
                T(title, 18.sp, FontWeight.Black, maxLines = 1)
            }
        }
        LecturePlayer(lecture, freeSeek = true, onFinish = onDone)
    }
}

private data class QuizUi(val solved: Boolean = false, val wrongs: Set<String> = emptySet(), val shakeId: Int = 0, val shakeOpt: String? = null)
private data class QuizEvent(val id: Int, val correct: Boolean)

@Composable
private fun ColumnScope.LecturePlayer(lecture: Lecture, freeSeek: Boolean, onFinish: () -> Unit) {
    val voice = rememberLectureVoice()
    val scenes = lecture.scenes
    val last = scenes.lastIndex
    var scene by remember { mutableIntStateOf(0) }
    var beat by remember { mutableIntStateOf(0) }
    var reached by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var muted by remember { mutableStateOf(false) }
    var caption by remember { mutableStateOf("") }
    var run by remember { mutableIntStateOf(0) }
    var quizUi by remember { mutableStateOf(QuizUi()) }
    var quizEvent by remember { mutableStateOf<QuizEvent?>(null) }
    var voiceSettings by remember { mutableStateOf(false) }

    suspend fun hold(ms: Long) {
        var t = 0L
        while (t < ms) { delay(50); if (!paused) t += 50 }
    }

    suspend fun narrate(text: String) {
        caption = text
        val spoken = speakable(text)
        val est = (spoken.length * 65L).coerceAtLeast(1800L)
        while (true) {
            snapshotFlow { paused }.first { !it }
            if (muted || voice.available != true) { hold(est); return }
            if (voice.speak(spoken)) return
            if (!paused) { hold(est); return }   // bị ngắt không phải do tạm dừng (vd. tắt tiếng)
        }
    }

    fun goTo(i: Int) {
        voice.stop()
        paused = false
        quizEvent = null
        quizUi = QuizUi()
        beat = 0
        scene = i.coerceIn(0, last)
        run++
    }

    LaunchedEffect(scene) { if (scene > reached) reached = scene }
    val sfxContext = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(scene, run) {
        when {
            scenes[scene] is LectureScene.Outro -> com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.COMPLETE)
            scene > 0 -> com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.SCENE)
        }
    }

    LaunchedEffect(scene, run) {
        withTimeoutOrNull(2500) { snapshotFlow { voice.available }.first { it != null } }
        val sc = scenes[scene]
        for (b in sc.beats.indices) {
            beat = b
            if (b > 0 && (sc is LectureScene.Example || sc is LectureScene.Quick || sc is LectureScene.Rule || sc is LectureScene.Steps || sc is LectureScene.Mistake))
                com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.POP)
            narrate(sc.beats[b])
            hold(if (b == sc.beats.lastIndex) 600 else 380)
        }
        if (sc !is LectureScene.Quiz && sc !is LectureScene.Outro && scene < last) {
            beat = 0
            scene += 1
        }
    }

    LaunchedEffect(quizEvent) {
        val e = quizEvent ?: return@LaunchedEffect
        val q = scenes[scene] as? LectureScene.Quiz ?: return@LaunchedEffect
        if (e.correct) {
            com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.DING)
            narrate("Đúng rồi! ${q.explain}")
            hold(500)
            if (scene < last) { beat = 0; quizUi = QuizUi(); scene += 1 }
            quizEvent = null
        } else {
            com.example.toanhoc.ui.sound.SoundEffectsManager.playFailure(sfxContext)
            narrate("Chưa đúng rồi. Con đọc lại đề và thử lại nhé.")
        }
    }

    Column(
        Modifier.weight(1f).fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sân khấu
        Box(
            Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(L.StageTop, L.StageBottom)))
        ) {
            AnimatedContent(
                targetState = scene,
                transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) },
                label = "scene"
            ) { idx ->
                val sc = scenes[idx]
                val b = if (idx == scene) beat else sc.beats.lastIndex
                key(run) {
                    SceneView(
                        sc, b, quizUi,
                        onPick = { opt ->
                            val q = sc as? LectureScene.Quiz
                            if (q != null && !quizUi.solved) {
                                val ok = opt == q.answer
                                quizUi = if (ok) quizUi.copy(solved = true)
                                else quizUi.copy(wrongs = quizUi.wrongs + opt, shakeId = quizUi.shakeId + 1, shakeOpt = opt)
                                quizEvent = QuizEvent((quizEvent?.id ?: 0) + 1, ok)
                            }
                        },
                        onFinish = { voice.stop(); onFinish() },
                        onRestart = { goTo(0) }
                    )
                }
            }
        }

        // Tiến độ theo cảnh
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            scenes.forEachIndexed { i, s ->
                val frac = when {
                    i < scene -> 1f
                    i > scene -> 0f
                    else -> (beat + 1f) / s.beats.size.coerceAtLeast(1)
                }
                val anim by animateFloatAsState(frac, tween(400), label = "bar")
                val canJump = freeSeek || i <= reached
                Box(
                    Modifier.weight(1f).height(8.dp).clip(CircleShape).background(L.Line)
                        .clickable(enabled = canJump, role = Role.Button) { goTo(i) }
                ) { Box(Modifier.fillMaxHeight().fillMaxWidth(anim).background(L.Blue)) }
            }
        }
        Row(Modifier.fillMaxWidth()) {
            T(scenes[scene].name, 13.sp, FontWeight.ExtraBold, L.Muted, Modifier.weight(1f), maxLines = 1)
            T("${scene + 1}/${scenes.size}", 13.sp, FontWeight.ExtraBold, L.Muted)
        }

        // Phụ đề
        Row(
            Modifier.fillMaxWidth().heightIn(min = 64.dp).clip(RoundedCornerShape(18.dp)).background(Color.White)
                .border(1.dp, L.Line, RoundedCornerShape(18.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                if (muted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = null, tint = L.Blue, modifier = Modifier.padding(top = 2.dp).size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            T(caption, 16.sp, FontWeight.Bold, lineHeight = 22.sp, maxLines = 4, modifier = Modifier.weight(1f))
            if (voice.available == true) {
                Box(
                    Modifier.size(36.dp).clip(CircleShape).clickable(role = Role.Button) {
                        paused = true; voice.stop(); voiceSettings = true
                    },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Tune, contentDescription = "Chỉnh giọng đọc", tint = L.Muted, modifier = Modifier.size(20.dp)) }
            }
        }
        if (voiceSettings) VoiceSettingsDialog(voice) { voiceSettings = false; paused = false }
        if (voice.available == false) {
            T(
                "Máy chưa có giọng đọc tiếng Việt nên bài giảng chạy bằng chữ. Bố mẹ có thể cài giọng Việt trong Cài đặt › Chuyển văn bản thành giọng nói.",
                12.sp, FontWeight.SemiBold, L.Muted, lineHeight = 16.sp
            )
        }

        // Điều khiển
        val sc = scenes[scene]
        val canNext = scene < last && (freeSeek || scene < reached)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            CtlButton(Icons.Rounded.SkipPrevious, "Cảnh trước", enabled = scene > 0) { goTo(scene - 1) }
            CtlButton(Icons.Rounded.Replay, "Nghe lại cảnh này") { goTo(scene) }
            CtlButton(
                if (paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                if (paused) "Phát tiếp" else "Tạm dừng", main = true,
                enabled = sc !is LectureScene.Outro
            ) { paused = !paused; if (paused) voice.stop() }
            CtlButton(Icons.Rounded.SkipNext, "Cảnh sau", enabled = canNext) { goTo(scene + 1) }
            CtlButton(
                if (muted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                if (muted) "Bật giọng đọc" else "Tắt giọng đọc", toggled = muted
            ) { muted = !muted; if (muted) voice.stop() }
        }
    }
}

/** Bố mẹ chọn tốc độ và giọng đọc; có nút nghe thử. Lựa chọn được lưu cho mọi bài giảng. */
@Composable
private fun VoiceSettingsDialog(voice: LectureVoice, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    fun preview() { voice.stop(); scope.launch { voice.speak(speakable(LectureVoice.SAMPLE)) } }
    AlertDialog(
        onDismissRequest = { voice.stop(); onDismiss() },
        confirmButton = { TextButton(onClick = { voice.stop(); onDismiss() }) { T("Xong", 16.sp, FontWeight.ExtraBold, L.Blue) } },
        dismissButton = { TextButton(onClick = { preview() }) { T("Nghe thử", 16.sp, FontWeight.ExtraBold, L.Blue) } },
        title = { T("Giọng đọc bài giảng", 20.sp, FontWeight.Black) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                T("Tốc độ", 14.sp, FontWeight.ExtraBold, L.Muted)
                VoiceSpeed.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { sp ->
                            val on = voice.speed == sp
                            Box(
                                Modifier.weight(1f).heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(if (on) L.Blue else Color.White)
                                    .border(1.dp, if (on) L.Blue else L.Line, RoundedCornerShape(12.dp))
                                    .clickable(role = Role.RadioButton) { voice.selectSpeed(sp); preview() },
                                contentAlignment = Alignment.Center
                            ) { T(sp.label, 15.sp, FontWeight.ExtraBold, if (on) Color.White else L.Ink) }
                        }
                    }
                }
                if (voice.voices.size > 1) {
                    Spacer(Modifier.height(4.dp))
                    T("Giọng", 14.sp, FontWeight.ExtraBold, L.Muted)
                    voice.voices.forEach { v ->
                        val on = voice.voiceName == v.name
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 44.dp).clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.RadioButton) { voice.selectVoice(v.name); preview() }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (on) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = null, tint = if (on) L.Blue else L.Muted, modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            T(v.label, 16.sp, FontWeight.Bold, modifier = Modifier.weight(1f))
                            val tag = listOfNotNull(if (v.highQuality) "chất lượng cao" else null, if (v.needsNetwork) "cần mạng" else null).joinToString(" · ")
                            if (tag.isNotEmpty()) T(tag, 12.sp, FontWeight.SemiBold, L.Muted)
                        }
                    }
                }
                if (voice.voiceName == PiperTts.ID) T(PiperTts.CREDIT, 12.sp, FontWeight.SemiBold, L.Muted, lineHeight = 16.sp)
                else T(
                    "Mẹo: cài bản mới nhất của \"Dịch vụ lời nói của Google\" và tải giọng Tiếng Việt chất lượng cao trong Cài đặt › Chuyển văn bản thành giọng nói để giọng tự nhiên hơn.",
                    12.sp, FontWeight.SemiBold, L.Muted, lineHeight = 16.sp
                )
            }
        },
        containerColor = Color.White
    )
}

@Composable
private fun CtlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, main: Boolean = false,
    enabled: Boolean = true, toggled: Boolean = false, onClick: () -> Unit
) {
    val size = if (main) 62.dp else 46.dp
    val bg = when { main -> L.Blue; toggled -> L.OrangeSoft; else -> Color.White }
    val fg = when { main -> Color.White; toggled -> L.OrangeInk; else -> L.Ink }
    Box(
        Modifier.size(size).alpha(if (enabled) 1f else 0.35f).clip(CircleShape).background(bg)
            .border(1.dp, if (main) L.Blue else L.Line, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = desc, tint = fg, modifier = Modifier.size(if (main) 30.dp else 22.dp)) }
}

/* ======================================== Các cảnh ======================================== */

@Composable
private fun SceneView(
    sc: LectureScene, beat: Int, quizUi: QuizUi,
    onPick: (String) -> Unit, onFinish: () -> Unit, onRestart: () -> Unit
) {
    when (sc) {
        is LectureScene.Intro -> IntroView(sc)
        is LectureScene.Pairs -> Stage43 { PairsView(sc, beat) }
        is LectureScene.EvenOddRow -> Stage43 { EvenOddRowView(sc, beat) }
        is LectureScene.LastDigit -> Stage43 { LastDigitView(sc, beat) }
        is LectureScene.Rule -> ScrollScene(beat) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(L.AmberBg).border(1.dp, L.AmberLine, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(L.AmberDot), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    T("Ghi nhớ", 20.sp, FontWeight.Black, L.Amber)
                }
                sc.theory.rule.forEachIndexed { i, line -> Reveal(beat >= i + 1) { RichLine(line, 18.sp) } }
                if (sc.theory.tiles.isNotEmpty()) Reveal(beat >= sc.theory.rule.size + 1) { TileGrid(sc.theory.tiles) }
            }
        }
        is LectureScene.Example -> ScrollScene(beat) {
            WhiteCard {
                if (sc.label != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(L.BlueSoft), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = L.Blue, modifier = Modifier.size(17.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        T(sc.label, 15.sp, FontWeight.Black, L.Blue)
                    }
                } else T("Ví dụ", 14.sp, FontWeight.ExtraBold, L.Muted)
                MT(sc.theory.example, 20.sp, FontWeight.ExtraBold, lineHeight = 27.sp)
                sc.theory.steps.forEachIndexed { i, st -> Reveal(beat >= i + 1) { StepRow(i + 1, st) } }
                Reveal(beat >= sc.theory.steps.size + 1) { Conclusion(sc.theory.conclusion) }
            }
        }
        is LectureScene.Steps -> ScrollScene(beat) {
            WhiteCard {
                T("Ví dụ", 14.sp, FontWeight.ExtraBold, L.Muted)
                MT(sc.statement, 20.sp, FontWeight.ExtraBold, lineHeight = 27.sp)
                sc.lines.forEachIndexed { i, l -> Reveal(beat >= i + 1) { NumberedLine(i + 1, l) } }
                Reveal(beat >= sc.lines.size + 1) { Conclusion("Vậy đáp án là ${sc.answer}") }
            }
        }
        is LectureScene.Card -> ScrollScene(beat) {
            WhiteCard {
                T(sc.title, 22.sp, FontWeight.Black, L.Blue, lineHeight = 28.sp)
                sc.lines.forEachIndexed { i, l -> Reveal(beat >= i) { T(l, 18.sp, FontWeight.Bold, lineHeight = 26.sp) } }
            }
        }
        is LectureScene.Tips -> ScrollScene(beat) {
            T("Mẹo nhỏ", 22.sp, FontWeight.Black, L.Amber)
            sc.tips.forEachIndexed { i, tip ->
                Reveal(beat >= i) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(L.AmberBg).border(1.dp, L.AmberLine, RoundedCornerShape(16.dp)).padding(14.dp)
                    ) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(L.AmberDot), contentAlignment = Alignment.Center) {
                            T("${i + 1}", 14.sp, FontWeight.Black, Color.White)
                        }
                        Spacer(Modifier.width(10.dp))
                        T(tip, 16.sp, FontWeight.Bold, lineHeight = 23.sp)
                    }
                }
            }
        }
        is LectureScene.Quick -> ScrollScene(beat) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(L.AmberBg).border(1.dp, L.AmberLine, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(L.AmberDot), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    T("Cách nhanh", 20.sp, FontWeight.Black, L.Amber)
                }
                sc.theory.quickRule?.let { T(it, 16.sp, FontWeight.Bold, L.Ink, lineHeight = 22.sp) }
                sc.theory.quick.forEachIndexed { i, st -> Reveal(beat >= i + 1) { StepRow(i + 1, st) } }
                Reveal(beat >= sc.theory.quick.size + 1) { Conclusion("Hai cách đều ra cùng một đáp số") }
            }
        }
        is LectureScene.Mistake -> ScrollScene(beat) {
            WhiteCard {
                T("Đề bài", 14.sp, FontWeight.ExtraBold, L.Muted)
                MT(sc.statement, 19.sp, FontWeight.ExtraBold, lineHeight = 26.sp)
                Row(
                    Modifier.clip(RoundedCornerShape(14.dp)).background(L.RedSoft).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = null, tint = L.Red, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    T("Con chọn: ${sc.picked}", 17.sp, FontWeight.Black, L.Red)
                }
                Reveal(beat >= 1) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(L.AmberBg).border(1.dp, L.AmberLine, RoundedCornerShape(16.dp)).padding(14.dp)
                    ) {
                        Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = L.AmberDot, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(10.dp))
                        T(sc.note, 17.sp, FontWeight.Bold, lineHeight = 24.sp)
                    }
                }
            }
        }
        is LectureScene.Quiz -> QuizView(sc, quizUi, onPick)
        is LectureScene.Outro -> OutroView(sc, onFinish, onRestart)
    }
}

@Composable
private fun Reveal(visible: Boolean, content: @Composable () -> Unit) =
    AnimatedVisibility(visible, enter = fadeIn(tween(350)) + expandVertically(tween(350))) { content() }

@Composable
private fun WhiteCard(content: @Composable ColumnScope.() -> Unit) = Column(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, L.Line, RoundedCornerShape(20.dp)).padding(18.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp), content = content
)

@Composable
private fun Conclusion(text: String) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(L.GreenSoft).padding(horizontal = 14.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(Icons.Rounded.Check, contentDescription = null, tint = L.Green, modifier = Modifier.size(22.dp))
    Spacer(Modifier.width(10.dp))
    MT(text, 18.sp, FontWeight.Black, Color(0xFF17663F), lineHeight = 24.sp)
}

@Composable
private fun NumberedLine(n: Int, text: String) = Row {
    Box(Modifier.size(30.dp).clip(CircleShape).background(L.Blue), contentAlignment = Alignment.Center) { T("$n", 15.sp, FontWeight.Black, Color.White) }
    Spacer(Modifier.width(12.dp))
    T(text, 18.sp, FontWeight.Bold, lineHeight = 25.sp, modifier = Modifier.padding(top = 3.dp))
}

/** Cảnh nhiều chữ: cuộn được, tự cuộn xuống phần vừa hiện. */
@Composable
private fun ScrollScene(beat: Int, content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    LaunchedEffect(beat) { delay(380); scroll.animateScrollTo(scroll.maxValue) }
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = content
    )
}

/** Cảnh hình động: khung 4:3 ở giữa sân khấu. */
@Composable
private fun Stage43(content: @Composable BoxWithConstraintsScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BoxWithConstraints(Modifier.aspectRatio(4f / 3f), content = content)
    }
}

@Composable
private fun Pill(text: String, fg: Color, bg: Color, line: Color, size: TextUnit = 18.sp, modifier: Modifier = Modifier) = Box(
    modifier.clip(CircleShape).background(bg).border(2.dp, line, CircleShape).padding(horizontal = 14.dp, vertical = 5.dp)
) { T(text, size, FontWeight.Black, fg, maxLines = 1) }

@Composable
private fun PopIn(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    AnimatedVisibility(visible, modifier, enter = scaleIn(spring(0.5f, Spring.StiffnessMediumLow)) + fadeIn()) { content() }

@Composable
private fun Kid(girl: Boolean, height: Dp, modifier: Modifier = Modifier) = Image(
    painterResource(if (girl) R.drawable.be_gai else R.drawable.be_trai), contentDescription = null,
    modifier = modifier.height(height).aspectRatio(if (girl) GIRL_RATIO else BOY_RATIO), contentScale = ContentScale.Fit
)

@Composable
private fun IntroView(sc: LectureScene.Intro) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
        val kidH = minOf(maxHeight * 0.55f, maxWidth * 0.95f)
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            PopIn(shown) { Pill("Bài ${sc.lessonNumber}", L.Blue, L.BlueSoft, L.BlueLine, 15.sp) }
            T(sc.lessonTitle, 26.sp, FontWeight.Black, align = TextAlign.Center, lineHeight = 32.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                PopIn(shown) { Kid(false, kidH) }
                PopIn(shown) { Kid(true, kidH * 0.96f) }
            }
        }
    }
}

/* -------- Xếp kẹo thành đôi -------- */

@Composable
private fun BoxWithConstraintsScope.PairsView(s: LectureScene.Pairs, beat: Int) {
    val w = maxWidth; val h = maxHeight
    val n = s.count; val pairs = n / 2
    val scatter = listOf(0.42f to 0.30f, 0.61f to 0.22f, 0.82f to 0.32f, 0.47f to 0.58f, 0.68f to 0.54f, 0.88f to 0.64f, 0.58f to 0.80f, 0.76f to 0.80f)
    val trayX = if (s.even) listOf(0.42f, 0.63f, 0.84f) else listOf(0.38f, 0.57f, 0.76f)
    val c = w * 0.085f
    val trayW = w * 0.19f; val trayH = c * 1.55f
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { delay(500); repeat(n) { shown = it + 1; delay(520) } }
    val inf = rememberInfiniteTransition(label = "wob")
    val wob by inf.animateFloat(-12f, 12f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "wob")

    Kid(s.girl, minOf(h * 0.82f, w * 0.6f), Modifier.align(Alignment.BottomStart).padding(start = 4.dp).offset(y = h * 0.02f))
    PopIn(true, Modifier.align(BiasAlignment(0.26f, -0.85f))) { Pill("Kẹo của ${s.owner}", L.Muted, Color.White, L.Line, 14.sp) }

    // Khay đôi
    val trayAlpha by animateFloatAsState(if (beat >= 1) 1f else 0f, tween(400), label = "tray")
    trayX.take(pairs).forEach { x ->
        val ok = beat >= 2
        val stroke by animateColorAsState(if (ok) L.Green else L.Tray, tween(400), label = "ts")
        val fill by animateColorAsState(if (ok) L.GreenSoft else Color.White.copy(alpha = 0.5f), tween(400), label = "tf")
        Box(
            Modifier.offset(x = w * x - trayW / 2, y = h * 0.5f - trayH / 2).size(trayW, trayH).alpha(trayAlpha)
                .clip(RoundedCornerShape(14.dp)).background(fill)
                .drawBehind {
                    drawRoundRect(
                        stroke, cornerRadius = CornerRadius(14.dp.toPx()),
                        style = Stroke(2.5.dp.toPx(), pathEffect = if (ok) null else PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
                    )
                }
        )
    }

    // Kẹo
    for (i in 0 until n) {
        val target = if (beat >= 1) {
            val k = i / 2
            if (k < pairs) (trayX[k] + if (i % 2 == 0) -0.05f else 0.05f) to 0.5f else 0.92f to 0.5f
        } else scatter[i]
        val spec = tween<Float>(700, delayMillis = if (beat >= 1) 200 + i * 200 else 0, easing = FastOutSlowInEasing)
        val x by animateFloatAsState(target.first, spec, label = "cx")
        val y by animateFloatAsState(target.second, spec, label = "cy")
        val scale by animateFloatAsState(if (i < shown) 1f else 0f, spring(0.45f, Spring.StiffnessMediumLow), label = "cs")
        val alone = !s.even && i == n - 1 && beat >= 2
        val color = L.Candy[i % L.Candy.size]
        Box(
            Modifier.offset(x = w * x - c / 2, y = h * y - c / 2).size(c)
                .graphicsLayer { scaleX = scale; scaleY = scale; rotationZ = if (alone) wob else 0f }
                .then(if (alone) Modifier.border(3.dp, L.Orange, CircleShape) else Modifier)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(color.copy(alpha = 0.75f), color)))
        ) {
            Box(Modifier.offset(x = c * 0.22f, y = c * 0.18f).size(c * 0.24f).clip(CircleShape).background(Color.White.copy(alpha = 0.7f)))
        }
    }

    if (!s.even) PopIn(beat >= 2, Modifier.align(BiasAlignment(0.82f, 0.42f))) {
        Pill("thừa 1", L.OrangeInk, L.OrangeSoft, Color(0xFFFFD2A6), 15.sp)
    }
    PopIn(beat >= 3, Modifier.align(BiasAlignment(0.3f, 0.82f))) {
        if (s.even) Pill("${s.count} là số chẵn", L.Blue, L.BlueSoft, L.BlueLine)
        else Pill("${s.count} là số lẻ", L.Green, L.GreenSoft, L.GreenLine)
    }
}

/* -------- Dãy số chẵn lẻ xen kẽ -------- */

@Composable
private fun BoxWithConstraintsScope.EvenOddRowView(s: LectureScene.EvenOddRow, beat: Int) {
    val tile = minOf(maxWidth / 8.4f, maxHeight / 4.4f)
    val nums = (0..s.to).toList()
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(tile * 0.35f, Alignment.CenterVertically)) {
        T("Chẵn, lẻ xen kẽ", (tile.value * 0.55f).sp, FontWeight.Black)
        nums.chunked(7).forEach { rowNums ->
            Row(horizontalArrangement = Arrangement.spacedBy(tile * 0.18f)) {
                rowNums.forEach { v -> NumTile(v, tile, beat, shown) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PopIn(beat >= 1) { Pill("số chẵn", L.Blue, L.BlueSoft, L.BlueLine, 14.sp) }
            PopIn(beat >= 2) { Pill("số lẻ", L.Green, L.GreenSoft, L.GreenLine, 14.sp) }
        }
    }
}

@Composable
private fun NumTile(v: Int, size: Dp, beat: Int, shown: Boolean) {
    val even = v % 2 == 0
    val want = if (even) beat >= 1 else beat >= 2
    var lit by remember { mutableStateOf(false) }
    LaunchedEffect(want) { if (want) { delay(300L + (v / 2) * 560L); lit = true } else lit = false }
    val jump = remember { Animatable(0f) }
    val wave = beat >= 3
    LaunchedEffect(wave) {
        if (wave) { delay(v * 170L); jump.animateTo(-1f, tween(200)); jump.animateTo(0f, tween(260)) }
    }
    val appear by animateFloatAsState(if (shown) 1f else 0f, tween(300, delayMillis = 200 + v * 60), label = "ap")
    val bg by animateColorAsState(if (!lit) Color.White else if (even) L.BlueSoft else L.GreenSoft, tween(400), label = "bg")
    val fg by animateColorAsState(if (!lit) L.Muted else if (even) L.Blue else L.Green, tween(400), label = "fg")
    val line by animateColorAsState(if (!lit) L.Line else if (even) L.BlueLine else L.GreenLine, tween(400), label = "ln")
    Box(
        Modifier.size(size).graphicsLayer { scaleX = appear; scaleY = appear; translationY = jump.value * size.toPx() * 0.3f }
            .clip(RoundedCornerShape(size * 0.24f)).background(bg).border(2.dp, line, RoundedCornerShape(size * 0.24f)),
        contentAlignment = Alignment.Center
    ) { T("$v", (size.value * 0.46f).sp, FontWeight.Black, fg) }
}

/* -------- Mẹo chữ số cuối -------- */

@Composable
private fun BoxWithConstraintsScope.LastDigitView(s: LectureScene.LastDigit, beat: Int) {
    val big = (maxWidth.value * 0.2f).sp
    AnimatedContent(beat >= 3, transitionSpec = { fadeIn(tween(350)) togetherWith fadeOut(tween(200)) }, label = "rules") { rules ->
        if (!rules) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
                Pill("Mẹo nhanh", L.Muted, Color.White, L.Line, 14.sp)
                val number = if (beat >= 2) s.oddNumber else s.evenNumber
                AnimatedContent(number, transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.8f)) togetherWith fadeOut(tween(200)) }, label = "num") { num ->
                    val isEven = num % 2 == 0
                    val hl = (isEven && beat >= 1) || (!isEven && beat >= 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        val digits = num.toString()
                        digits.forEachIndexed { i, ch ->
                            val lastDigit = i == digits.lastIndex && hl
                            val bg by animateColorAsState(if (!lastDigit) Color.Transparent else if (isEven) L.BlueSoft else L.GreenSoft, tween(500, 300), label = "d")
                            val fg by animateColorAsState(if (!lastDigit) L.Ink else if (isEven) L.Blue else L.Green, tween(500, 300), label = "df")
                            Box(Modifier.clip(RoundedCornerShape(16.dp)).background(bg).padding(horizontal = 8.dp)) {
                                T("$ch", big, FontWeight.Black, fg, lineHeight = big)
                            }
                        }
                    }
                }
                val hlNow = if (beat >= 2) 2 else if (beat >= 1) 1 else 0
                AnimatedContent(hlNow, label = "verdict") { k ->
                    when (k) {
                        1 -> Pill("${s.evenNumber % 10} chẵn → ${s.evenNumber} là số chẵn", L.Blue, L.BlueSoft, L.BlueLine, 16.sp)
                        2 -> Pill("${s.oddNumber % 10} lẻ → ${s.oddNumber} là số lẻ", L.Green, L.GreenSoft, L.GreenLine, 16.sp)
                        else -> Spacer(Modifier.height(34.dp))
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
                T("Ghi nhớ nhé!", 24.sp, FontWeight.Black)
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, L.Line, RoundedCornerShape(18.dp)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DigitRule("Chẵn", listOf(0, 2, 4, 6, 8), L.Blue, L.BlueSoft)
                    DigitRule("Lẻ", listOf(1, 3, 5, 7, 9), L.Green, L.GreenSoft)
                }
                T("Nhìn chữ số cuối cùng của số", 15.sp, FontWeight.Bold, L.Muted, align = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DigitRule(label: String, digits: List<Int>, fg: Color, bg: Color) = Row(
    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
) {
    T(label, 17.sp, FontWeight.Black, fg, Modifier.width(56.dp))
    digits.forEach { d ->
        Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(bg), contentAlignment = Alignment.Center) {
            T("$d", 17.sp, FontWeight.Black, fg)
        }
    }
}

/* -------- Câu đố -------- */

@Composable
private fun QuizView(q: LectureScene.Quiz, ui: QuizUi, onPick: (String) -> Unit) {
    val scroll = rememberScrollState()
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Pill("Đố con", L.Muted, Color.White, L.Line, 14.sp)
        MT(q.question, if (q.question.length > 60) 20.sp else 25.sp, FontWeight.Black, align = TextAlign.Center, lineHeight = if (q.question.length > 60) 27.sp else 32.sp)
        val short = q.options.all { it.length <= 12 }
        if (short) {
            q.options.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { opt -> AnswerButton(opt, q, ui, onPick, Modifier.weight(1f)) }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        } else {
            q.options.forEach { opt -> AnswerButton(opt, q, ui, onPick, Modifier.fillMaxWidth()) }
        }
        AnimatedVisibility(ui.solved, enter = scaleIn(spring(0.5f)) + fadeIn()) {
            Pill("Đúng rồi!", L.Green, L.GreenSoft, L.GreenLine)
        }
    }
}

@Composable
private fun AnswerButton(opt: String, q: LectureScene.Quiz, ui: QuizUi, onPick: (String) -> Unit, modifier: Modifier) {
    val right = ui.solved && opt == q.answer
    val wrong = opt in ui.wrongs
    val shake = remember { Animatable(0f) }
    LaunchedEffect(ui.shakeId) {
        if (ui.shakeId > 0 && ui.shakeOpt == opt) {
            repeat(2) { shake.animateTo(-1f, tween(60)); shake.animateTo(1f, tween(60)) }
            shake.animateTo(0f, tween(60))
        }
    }
    val bg = when { right -> L.GreenSoft; wrong -> L.RedSoft; else -> Color.White }
    val line = when { right -> L.Green; wrong -> Color(0xFFF2B8B8); else -> L.Line }
    val fg = when { right -> L.Green; wrong -> L.Red; else -> L.Ink }
    Box(
        modifier.heightIn(min = 60.dp).graphicsLayer { translationX = shake.value * 10.dp.toPx() }
            .clip(RoundedCornerShape(18.dp)).background(bg).border(2.dp, line, RoundedCornerShape(18.dp))
            .clickable(enabled = !ui.solved && !wrong, role = Role.Button) { onPick(opt) }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) { MT(opt, 20.sp, FontWeight.Black, fg, align = TextAlign.Center, lineHeight = 26.sp) }
}

/* -------- Kết thúc -------- */

@Composable
private fun OutroView(sc: LectureScene.Outro, onFinish: () -> Unit, onRestart: () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().padding(18.dp)) {
        val kidH = minOf(maxHeight * 0.42f, maxWidth * 0.8f)
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Kid(false, kidH); Kid(true, kidH * 0.96f)
            }
            T(sc.headline, 26.sp, FontWeight.Black, align = TextAlign.Center)
            Row(
                Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)).background(L.Blue)
                    .clickable(role = Role.Button, onClick = onFinish),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                T(sc.cta, 18.sp, FontWeight.Black, Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
            }
            Box(Modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onRestart).padding(horizontal = 14.dp, vertical = 8.dp)) {
                T(if (sc.cta == "Bắt đầu làm Dạng 1") "Xem lại bài giảng" else "Xem lại", 15.sp, FontWeight.ExtraBold, L.Blue)
            }
        }
    }
}
