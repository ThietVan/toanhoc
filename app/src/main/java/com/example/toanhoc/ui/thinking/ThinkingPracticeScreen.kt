package com.example.toanhoc.ui.thinking

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.lecture.Lecture
import com.example.toanhoc.domain.lecture.LectureScene
import com.example.toanhoc.domain.lecture.Lectures
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.theory.KidTheories
import com.example.toanhoc.domain.theory.KidTheory
import com.example.toanhoc.domain.theory.Tok
import com.example.toanhoc.domain.thinking.ThinkingEngine
import com.example.toanhoc.ui.lecture.LectureOverlay
import com.example.toanhoc.ui.session.BlindedPauseOverlay
import com.example.toanhoc.ui.session.SessionUiState
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.sound.SoundEffectsManager
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay

/* ------------------------------------------------------------------------------------------
 * TOÁN TƯ DUY – giao diện riêng ("chương trình đặc biệt"): tông xanh đêm, thẻ học viên, nhiệm vụ 12 câu.
 *
 * Luồng khi con trả lời sai (thay cho hiện đáp án ngay):
 *   Sai lần 1 → GỢI Ý (quy tắc của dạng, không lộ đáp án) → con thử lại.
 *   Sai lần 2 / bấm "Chưa biết làm" → PHÂN TÍCH: con chọn gì, sai ở đâu, đáp án đúng
 *        + thẻ VIDEO giải chính câu này (cách ở trường + cách nhanh)
 *        + CÂU SINH ĐÔI: cùng dạng, khác số – làm đúng thì được dấu "Đã sửa lỗi".
 * ---------------------------------------------------------------------------------------- */

internal object TD {
    val BgTop = Color(0xFF0D1430); val BgBottom = Color(0xFF17214A)
    val Card = Color(0xFF1C2754); val CardHi = Color(0xFF243266); val Line = Color(0xFF33427F)
    val Ink = Color(0xFFF3F6FF); val Muted = Color(0xFFA9B4DA); val Faint = Color(0xFF6E7BAA)
    val Amber = Color(0xFFFFC24B); val AmberInk = Color(0xFF3A2600)
    val Cyan = Color(0xFF4FD8E8); val Green = Color(0xFF34D399); val GreenDeep = Color(0xFF0E5A3F)
    val Coral = Color(0xFFFF7A7A); val CoralDeep = Color(0xFF5A1F2A); val Violet = Color(0xFF9B8CFF)
    val Panel = Color(0xFFF7F8FC); val PanelInk = Color(0xFF1B2440); val PanelMuted = Color(0xFF5B6476)
}

@Composable
internal fun TText(
    text: String, size: TextUnit, weight: FontWeight = FontWeight.Bold, color: Color = TD.Ink,
    modifier: Modifier = Modifier, align: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE, letterSpacing: TextUnit = TextUnit.Unspecified
) = Text(
    text, modifier, color = color, fontSize = size, fontWeight = weight, fontFamily = NunitoFontFamily,
    textAlign = align, lineHeight = lineHeight, maxLines = maxLines, overflow = TextOverflow.Ellipsis, letterSpacing = letterSpacing
)


/** Chữ thường, nhưng phân số (3/4) hiển thị tử trên – mẫu dưới. */
@Composable
internal fun MText(
    text: String, size: TextUnit, weight: FontWeight = FontWeight.Bold, color: Color = TD.Ink,
    modifier: Modifier = Modifier, align: TextAlign? = null, lineHeight: TextUnit = TextUnit.Unspecified, maxLines: Int = Int.MAX_VALUE
) {
    if (!com.example.toanhoc.ui.design.hasFraction(text)) { TText(text, size, weight, color, modifier, align, lineHeight, maxLines); return }
    if (text.length <= 14)
        Box(modifier, contentAlignment = if (align == TextAlign.Center) Alignment.Center else Alignment.CenterStart) {
            com.example.toanhoc.ui.design.InlineMath(text, size, color, weight)
        }
    else com.example.toanhoc.ui.design.MathParagraph(text, size, color, weight, align ?: TextAlign.Start, modifier)
}

/** Kết quả từng câu trong nhiệm vụ (để vẽ chuỗi 12 nút và báo cáo cuối). */
enum class Outcome { CORRECT, FIXED, MISSED }

/** Nhật kí lượt nhiệm vụ đang làm – giữ trong bộ nhớ để màn Báo cáo đọc lại. */
object ThinkingRunLog {
    var key: String = ""
        private set
    val outcomes = androidx.compose.runtime.mutableStateListOf<Outcome>()
    fun reset(k: String) { key = k; outcomes.clear() }
    val fixedCount get() = outcomes.count { it == Outcome.FIXED }
    val bestStreak: Int get() { var best = 0; var cur = 0; outcomes.forEach { if (it == Outcome.CORRECT) { cur++; best = maxOf(best, cur) } else cur = 0 }; return best }
}

private enum class Phase { ASK, HINT, REVEAL, TWIN, TWIN_OK, TWIN_BAD }

fun tierLabel(t: AcademicTier) = when (t) { AcademicTier.TRUNG_BINH -> "Cơ bản"; AcademicTier.GIOI -> "Nâng cao"; else -> "Thử thách" }
fun stageLabel(stage: String?) = when (stage) { "EASY" -> "Dạng 1"; "HARD" -> "Dạng 2"; "REVIEW" -> "Ôn tập"; else -> "Luyện tập" }

@Composable
fun ThinkingPracticeScreen(
    uiState: SessionUiState,
    lessonTitle: String,
    childName: String,
    onSelectOption: (String) -> Unit,
    onPauseToggle: (Boolean) -> Unit,
    onSaveAndExit: () -> Unit,
    onRevealSolution: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onPauseToggle(true) }
    val context = LocalContext.current
    DisposableEffect(Unit) { SoundEffectsManager.init(context); onDispose { } }
    val runKey = "${uiState.lessonId}:${uiState.lessonStage}"
    var briefed by rememberSaveable(runKey) { mutableStateOf(uiState.totalAnsweredCount > 0) }
    LaunchedEffect(runKey) {
        if (uiState.totalAnsweredCount == 0 || ThinkingRunLog.key != runKey) ThinkingRunLog.reset(runKey)
        SoundEffectsManager.resetStreak()
    }

    val problem = uiState.currentProblem
    val step = problem?.steps?.getOrNull(uiState.currentStepIndex)
    val qKey = "${problem?.problemId}:${uiState.totalAnsweredCount}"

    var phase by remember(qKey) { mutableStateOf(Phase.ASK) }
    var picked by remember(qKey) { mutableStateOf<String?>(null) }
    var hadError by remember(qKey) { mutableStateOf(false) }
    var fixed by remember(qKey) { mutableStateOf(false) }
    var twin by remember(qKey) { mutableStateOf<AcademicProblem?>(null) }
    var twinPick by remember(qKey) { mutableStateOf<String?>(null) }
    var video by remember { mutableStateOf<Lecture?>(null) }
    var locked by remember(qKey) { mutableStateOf(true) }
    LaunchedEffect(qKey) { delay(600); locked = false }

    val theory: KidTheory? = remember(problem?.problemId) { problem?.let { runCatching { KidTheories.build(it) }.getOrNull() } }

    // Ghi kết quả từng câu đúng một lần (đúng ngay / tự sửa được / chưa được)
    var recorded by remember(qKey) { mutableStateOf(false) }
    fun record(o: Outcome) { if (!recorded) { recorded = true; ThinkingRunLog.outcomes += o } }

    // Phản ứng với lượt sai: lần 1 → gợi ý, lần 2 → phân tích
    var seenWrong by remember(qKey) { mutableStateOf(0) }
    LaunchedEffect(uiState.wrongCount, uiState.isCurrentStepError, qKey) {
        if (uiState.isCurrentStepError && uiState.wrongCount > seenWrong && phase != Phase.REVEAL) {
            seenWrong = uiState.wrongCount
            hadError = true
            picked = uiState.userInput
            SoundEffectsManager.playFailure(context)
            if (uiState.wrongCount == 1) phase = Phase.HINT
            else { onRevealSolution(); phase = Phase.REVEAL }
        }
    }
    // Đúng (ngay lần đầu, hoặc sau gợi ý = tự sửa lỗi)
    LaunchedEffect(uiState.feedbackCorrect, qKey) {
        if (uiState.feedbackCorrect) {
            SoundEffectsManager.playSuccess(context)
            if (hadError) fixed = true
            record(if (hadError) Outcome.FIXED else Outcome.CORRECT)
        }
    }

    // Bảng gợi ý / phân tích trượt lên → hai nốt marimba ấm
    LaunchedEffect(phase) {
        if (phase == Phase.HINT || phase == Phase.REVEAL) { delay(320); SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.HINT) }
    }
    val streak = ThinkingRunLog.outcomes.takeLastWhile { it == Outcome.CORRECT }.size

    Box(modifier.fillMaxSize().background(Brush.verticalGradient(listOf(TD.BgTop, TD.BgBottom)))) {
        Column(Modifier.fillMaxSize()) {
            // ---------- Thanh trên ----------
            Row(Modifier.fillMaxWidth().padding(start = 6.dp, end = 14.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(CircleShape).clickable(role = Role.Button) { onPauseToggle(true) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Pause, contentDescription = "Tạm dừng", tint = TD.Muted)
                }
                Column(Modifier.weight(1f)) {
                    TText("TOÁN TƯ DUY · ${stageLabel(uiState.lessonStage).uppercase()}", 11.sp, FontWeight.ExtraBold, TD.Cyan, letterSpacing = 1.2.sp)
                    TText(lessonTitle, 16.sp, FontWeight.ExtraBold, maxLines = 1)
                }
                if (streak >= 2) StreakChip(streak)
            }
            Spacer(Modifier.height(10.dp))
            ProgressChain(ThinkingRunLog.outcomes, current = uiState.totalAnsweredCount, Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(14.dp))

            // ---------- Câu hỏi ----------
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (problem != null && step != null) {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(TD.Card)
                            .border(1.dp, TD.Line, RoundedCornerShape(24.dp)).padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TText("CÂU ${(uiState.totalAnsweredCount + 1).coerceAtMost(12)}/12", 12.sp, FontWeight.Black, TD.Muted, letterSpacing = 1.sp)
                            Spacer(Modifier.weight(1f))
                            TierChip(problem.academicTier)
                        }
                        MText(problem.statementVi, 21.sp, FontWeight.ExtraBold, lineHeight = 30.sp)
                    }
                    val opts = step.options
                    val longest = opts.maxOfOrNull { it.length } ?: 0
                    val perRow = if (longest > 14) 1 else 2
                    val revealed = phase != Phase.ASK && phase != Phase.HINT
                    opts.chunked(perRow).forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEachIndexed { c, opt ->
                                val idx = r * perRow + c
                                val state = when {
                                    uiState.feedbackCorrect && opt == step.expectedAnswer -> OptState.RIGHT
                                    revealed && opt == step.expectedAnswer -> OptState.RIGHT
                                    opt == picked && hadError -> OptState.WRONG
                                    else -> OptState.IDLE
                                }
                                OptionTile(
                                    letter = "ABCD".getOrElse(idx) { ' ' }.toString(), text = opt, state = state,
                                    enabled = !locked && phase == Phase.ASK && !uiState.feedbackCorrect && uiState.currentHintLevel < 2,
                                    size = if (longest <= 6) 22.sp else if (longest <= 12) 18.sp else 16.sp,
                                    modifier = Modifier.weight(1f)
                                ) { SoundEffectsManager.playSelect(context); onSelectOption(opt) }
                            }
                            repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
                Spacer(Modifier.height(90.dp))
            }

            // ---------- Thanh dưới ----------
            Box(Modifier.fillMaxWidth().background(TD.BgBottom).padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding()) {
                when {
                    uiState.feedbackCorrect -> Row(Modifier.fillMaxWidth().heightIn(min = 52.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(TD.Green), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = TD.GreenDeep, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        TText(if (hadError) "Tự sửa được rồi!" else if (streak >= 2) "Chính xác! Chuỗi ${streak + 1} câu" else "Chính xác!", 19.sp, FontWeight.Black, TD.Green)
                    }
                    phase == Phase.ASK -> GhostButton("Chưa biết làm? Xem phân tích", Icons.Rounded.Psychology) {
                        hadError = true; picked = null; onRevealSolution(); phase = Phase.REVEAL
                    }
                    else -> Spacer(Modifier.height(52.dp))
                }
            }
        }

        // ---------- Bảng gợi ý / phân tích (trượt lên) ----------
        AnimatedVisibility(
            visible = phase != Phase.ASK && problem != null && step != null,
            enter = slideInVertically { it } + fadeIn(), exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            if (problem != null && step != null) {
                AnalysisSheet(
                    phase = phase, problem = problem, answer = step.expectedAnswer, picked = picked, theory = theory,
                    twin = twin, twinPick = twinPick,
                    onRetry = { phase = Phase.ASK },
                    onVideo = { video = runCatching { Lectures.forMistake(problem, picked) }.getOrNull() },
                    onStartTwin = {
                        val t = twin ?: runCatching { ThinkingEngine.twin(problem) }.getOrNull()
                        if (t == null) { record(Outcome.MISSED); onContinue() } else { twin = t; phase = Phase.TWIN }
                    },
                    onPickTwin = { opt ->
                        val t = twin ?: return@AnalysisSheet
                        twinPick = opt
                        if (opt == t.steps.first().expectedAnswer) { fixed = true; phase = Phase.TWIN_OK; SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.FIXED) }
                        else { phase = Phase.TWIN_BAD; SoundEffectsManager.playFailure(context) }
                    },
                    onTwinVideo = { twin?.let { t -> video = runCatching { Lectures.forMistake(t, twinPick) }.getOrNull() } },
                    onNext = { SoundEffectsManager.playSelect(context); record(if (fixed) Outcome.FIXED else Outcome.MISSED); onContinue() }
                )
            }
        }

        if (!briefed) {
            MissionBriefing(
                lessonTitle = lessonTitle, stage = stageLabel(uiState.lessonStage), childName = childName, grade = uiState.gradeLevel,
                onStart = { SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.MISSION); briefed = true },
                onExit = onSaveAndExit
            )
        }

        video?.let { lec ->
            LectureOverlay(lecture = lec, kicker = "VIDEO PHÂN TÍCH", title = "Giải chính câu con vừa làm", onClose = { video = null }, onDone = { video = null })
        }

        if (uiState.isPaused) {
            BlindedPauseOverlay(elapsedSeconds = uiState.elapsedSeconds, onResumeSession = { onPauseToggle(false) }, onSaveAndExit = onSaveAndExit)
        }
    }
}

/* ======================================== Thành phần ======================================== */

private enum class OptState { IDLE, WRONG, RIGHT }

@Composable
private fun OptionTile(letter: String, text: String, state: OptState, enabled: Boolean, size: TextUnit, modifier: Modifier, onClick: () -> Unit) {
    val bg by animateColorAsState(when (state) { OptState.RIGHT -> TD.Green; OptState.WRONG -> TD.CoralDeep; else -> TD.Card }, tween(220), label = "opt")
    val line = when (state) { OptState.RIGHT -> TD.Green; OptState.WRONG -> TD.Coral; else -> TD.Line }
    val fg = when (state) { OptState.RIGHT -> TD.GreenDeep; OptState.WRONG -> TD.Coral; else -> TD.Ink }
    Row(
        modifier.heightIn(min = 68.dp).clip(RoundedCornerShape(18.dp)).background(bg).border(1.5.dp, line, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(if (state == OptState.IDLE) TD.CardHi else Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                OptState.RIGHT -> Icon(Icons.Rounded.Check, contentDescription = "Đáp án đúng", tint = TD.GreenDeep, modifier = Modifier.size(18.dp))
                OptState.WRONG -> Icon(Icons.Rounded.Close, contentDescription = "Con đã chọn – chưa đúng", tint = TD.Coral, modifier = Modifier.size(18.dp))
                else -> TText(letter, 13.sp, FontWeight.Black, TD.Muted)
            }
        }
        Spacer(Modifier.width(10.dp))
        MText(text, size, FontWeight.Black, fg, Modifier.weight(1f), align = TextAlign.Center, maxLines = 2)
        Spacer(Modifier.width(28.dp))
    }
}

@Composable
private fun TierChip(t: AcademicTier) {
    val (fg, bg) = when (t) {
        AcademicTier.TRUNG_BINH -> TD.Cyan to TD.Cyan.copy(alpha = 0.14f)
        AcademicTier.GIOI -> TD.Violet to TD.Violet.copy(alpha = 0.16f)
        else -> TD.Amber to TD.Amber.copy(alpha = 0.16f)
    }
    Row(Modifier.clip(CircleShape).background(bg).padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(when (t) { AcademicTier.TRUNG_BINH -> 1; AcademicTier.GIOI -> 2; else -> 3 }) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = fg, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(4.dp))
        TText(tierLabel(t), 12.sp, FontWeight.ExtraBold, fg)
    }
}

@Composable
private fun StreakChip(n: Int) {
    val pulse by rememberInfiniteTransition(label = "streak").animateFloat(1f, 1.08f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "s")
    Row(
        Modifier.scale(pulse).clip(CircleShape).background(TD.Amber.copy(alpha = 0.16f)).border(1.dp, TD.Amber.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = TD.Amber, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        TText("$n câu liền", 13.sp, FontWeight.Black, TD.Amber)
    }
}

/** Chuỗi 12 nút: xanh = đúng ngay, vàng = tự sửa được, đỏ = chưa được; nút hiện tại sáng nhấp nháy. */
@Composable
internal fun ProgressChain(outcomes: List<Outcome>, current: Int, modifier: Modifier = Modifier, total: Int = 12) {
    val glow by rememberInfiniteTransition(label = "chain").animateFloat(0.45f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "g")
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val o = outcomes.getOrNull(i)
            val color = when {
                o == Outcome.CORRECT -> TD.Green
                o == Outcome.FIXED -> TD.Amber
                o == Outcome.MISSED -> TD.Coral
                i == current -> TD.Cyan
                else -> TD.Line
            }
            Box(
                Modifier.weight(1f).height(if (i == current && o == null) 10.dp else 8.dp).clip(CircleShape)
                    .background(color).alpha(if (i == current && o == null) glow else 1f)
            )
        }
    }
}

@Composable
private fun GhostButton(text: String, icon: ImageVector, onClick: () -> Unit) = Row(
    Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp)).border(1.dp, TD.Line, RoundedCornerShape(16.dp))
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
) {
    Icon(icon, contentDescription = null, tint = TD.Muted, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(8.dp))
    TText(text, 15.sp, FontWeight.ExtraBold, TD.Muted)
}

@Composable
internal fun PrimaryButton(text: String, modifier: Modifier = Modifier, color: Color = TD.Amber, ink: Color = TD.AmberInk, icon: ImageVector? = Icons.AutoMirrored.Rounded.ArrowForward, onClick: () -> Unit) = Row(
    modifier.fillMaxWidth().heightIn(min = 54.dp).clip(RoundedCornerShape(16.dp)).background(color)
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp),
    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
) {
    TText(text, 17.sp, FontWeight.Black, ink)
    if (icon != null) { Spacer(Modifier.width(8.dp)); Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(20.dp)) }
}

private fun ruleText(t: KidTheory?): String? =
    t?.quickRule ?: t?.rule?.firstOrNull()?.joinToString("") { when (it) { is Tok.T -> it.s; is Tok.P -> it.s } }?.trim()

/* -------------------- Bảng gợi ý / phân tích -------------------- */

@Composable
private fun AnalysisSheet(
    phase: Phase, problem: AcademicProblem, answer: String, picked: String?, theory: KidTheory?,
    twin: AcademicProblem?, twinPick: String?,
    onRetry: () -> Unit, onVideo: () -> Unit, onStartTwin: () -> Unit, onPickTwin: (String) -> Unit, onTwinVideo: () -> Unit, onNext: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().heightIn(max = 620.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(TD.Panel)
            .verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 18.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(5.dp).clip(CircleShape).background(Color(0xFFD3D8E6)))
        when (phase) {
            Phase.HINT -> {
                SheetHeader(Icons.Rounded.Lightbulb, Color(0xFFE8952E), "Chưa đúng – thử lại một lần nhé", "Con được thử lại. Đọc kĩ đề và nhớ quy tắc này:")
                ruleText(theory)?.let { Callout(it, Color(0xFFFBF3EA), Color(0xFFF3DDC2), Color(0xFFB45F06)) }
                PrimaryButton("Thử lại", icon = Icons.Rounded.Replay, onClick = onRetry)
            }
            Phase.REVEAL -> {
                SheetHeader(Icons.Rounded.Psychology, Color(0xFF5B3FD6), "Phân tích lỗi", "Cùng xem chuyện gì đã xảy ra.")
                VerdictRow(picked, answer)
                val note = picked?.let { theory?.errorNotes?.get(it) }
                if (note != null) Callout(note, Color(0xFFFFF1F1), Color(0xFFF6C9C9), Color(0xFF9B2C2C))
                else ruleText(theory)?.let { Callout(it, Color(0xFFFBF3EA), Color(0xFFF3DDC2), Color(0xFFB45F06)) }
                VideoCard(theory, onVideo)
                PrimaryButton("Làm câu sinh đôi để sửa lỗi", onClick = onStartTwin)
                SkipLink("Bỏ qua, sang câu tiếp", onNext)
            }
            Phase.TWIN, Phase.TWIN_OK, Phase.TWIN_BAD -> {
                val t = twin
                if (t != null) {
                    val tAns = t.steps.first().expectedAnswer
                    SheetHeader(Icons.Rounded.Replay, Color(0xFF0B7A6F), "Câu sinh đôi", "Cùng dạng, khác số. Làm đúng là con đã sửa được lỗi!")
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, Color(0xFFDCE3EF), RoundedCornerShape(18.dp)).padding(14.dp)
                    ) { MText(t.statementVi, 18.sp, FontWeight.ExtraBold, TD.PanelInk, lineHeight = 25.sp) }
                    t.steps.first().options.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { opt ->
                                val st = when {
                                    phase != Phase.TWIN && opt == tAns -> OptState.RIGHT
                                    phase == Phase.TWIN_BAD && opt == twinPick -> OptState.WRONG
                                    else -> OptState.IDLE
                                }
                                LightOption(opt, st, enabled = phase == Phase.TWIN, modifier = Modifier.weight(1f)) { onPickTwin(opt) }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                    if (phase == Phase.TWIN_OK) {
                        FixedStamp()
                        PrimaryButton("Câu tiếp theo", onClick = onNext)
                    }
                    if (phase == Phase.TWIN_BAD) {
                        val tNote = runCatching { KidTheories.build(t)?.errorNotes?.get(twinPick) }.getOrNull()
                        Callout(tNote ?: "Đáp án đúng là $tAns.", Color(0xFFFFF1F1), Color(0xFFF6C9C9), Color(0xFF9B2C2C))
                        VideoCard(null, onTwinVideo, label = "Xem video giải câu sinh đôi")
                        PrimaryButton("Câu tiếp theo", onClick = onNext)
                    }
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun SheetHeader(icon: ImageVector, tint: Color, title: String, sub: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.width(12.dp))
    Column {
        TText(title, 19.sp, FontWeight.Black, TD.PanelInk)
        TText(sub, 14.sp, FontWeight.SemiBold, TD.PanelMuted, lineHeight = 19.sp)
    }
}

@Composable
private fun VerdictRow(picked: String?, answer: String) = Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    if (picked != null) Verdict("Con chọn", picked, Color(0xFFFDE8E8), Color(0xFFD64545), Icons.Rounded.Close, Modifier.weight(1f))
    Verdict("Đáp án đúng", answer, Color(0xFFDDF5E7), Color(0xFF1F9D55), Icons.Rounded.Check, Modifier.weight(1f))
}

@Composable
private fun Verdict(label: String, value: String, bg: Color, fg: Color, icon: ImageVector, modifier: Modifier) = Column(
    modifier.clip(RoundedCornerShape(16.dp)).background(bg).padding(horizontal = 14.dp, vertical = 10.dp)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        TText(label, 12.sp, FontWeight.ExtraBold, fg)
    }
    MText(value, 22.sp, FontWeight.Black, fg, maxLines = 1)
}

@Composable
private fun Callout(text: String, bg: Color, line: Color, fg: Color) = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).border(1.dp, line, RoundedCornerShape(16.dp)).padding(14.dp)
) {
    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(10.dp))
    MText(text, 16.sp, FontWeight.Bold, TD.PanelInk, lineHeight = 23.sp)
}

/** Thẻ video lớn: ô xem trước tối với nút phát + tên các chương. */
@Composable
private fun VideoCard(theory: KidTheory?, onClick: () -> Unit, label: String = "Video giải chính câu này") {
    val hasQuick = theory?.quick?.isNotEmpty() == true
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.horizontalGradient(listOf(Color(0xFF1C2754), Color(0xFF33287A))))
            .clickable(role = Role.Button, onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(54.dp).clip(CircleShape).background(TD.Amber), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = TD.AmberInk, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            TText(label, 16.sp, FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Chapter("Cách ở trường")
                if (hasQuick) Chapter("⚡ Cách nhanh")
            }
        }
    }
}

@Composable
private fun Chapter(t: String) = Box(Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
    TText(t, 11.sp, FontWeight.ExtraBold, TD.Ink)
}

@Composable
private fun LightOption(text: String, st: OptState, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg = when (st) { OptState.RIGHT -> Color(0xFFDDF5E7); OptState.WRONG -> Color(0xFFFDE8E8); else -> Color.White }
    val line = when (st) { OptState.RIGHT -> Color(0xFF1F9D55); OptState.WRONG -> Color(0xFFD64545); else -> Color(0xFFDCE3EF) }
    Box(
        modifier.heightIn(min = 56.dp).clip(RoundedCornerShape(14.dp)).background(bg).border(1.5.dp, line, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick).padding(8.dp),
        contentAlignment = Alignment.Center
    ) { MText(text, 18.sp, FontWeight.Black, TD.PanelInk, Modifier.fillMaxWidth(), align = TextAlign.Center, maxLines = 2) }
}

@Composable
private fun FixedStamp() = Row(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFDDF5E7)).border(1.5.dp, Color(0xFF1F9D55), RoundedCornerShape(16.dp)).padding(14.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Icon(Icons.Rounded.Verified, contentDescription = null, tint = Color(0xFF1F9D55), modifier = Modifier.size(30.dp))
    Spacer(Modifier.width(10.dp))
    Column {
        TText("Đã sửa lỗi!", 19.sp, FontWeight.Black, Color(0xFF17663F))
        TText("Con làm đúng câu sinh đôi – lỗi này đã được ghi vào sổ là \"đã sửa\".", 14.sp, FontWeight.SemiBold, Color(0xFF17663F), lineHeight = 19.sp)
    }
}

@Composable
private fun ColumnScope.SkipLink(text: String, onClick: () -> Unit) = Box(
    Modifier.align(Alignment.CenterHorizontally).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp, vertical = 8.dp)
) { TText(text, 14.sp, FontWeight.ExtraBold, TD.PanelMuted) }

/* -------------------- Màn "Nhiệm vụ" trước khi bắt đầu -------------------- */

@Composable
private fun MissionBriefing(lessonTitle: String, stage: String, childName: String, grade: Int, onStart: () -> Unit, onExit: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(TD.BgTop, Color(0xFF1B2660))))) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onExit), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, contentDescription = "Thoát", tint = TD.Muted)
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.clip(CircleShape).background(TD.Cyan.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    TText("CHƯƠNG TRÌNH THỬ NGHIỆM", 11.sp, FontWeight.Black, TD.Cyan, letterSpacing = 1.sp)
                }
            }
            StudentCard(childName, grade)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TText("NHIỆM VỤ · $stage", 12.sp, FontWeight.Black, TD.Amber, letterSpacing = 1.4.sp)
                TText(lessonTitle, 28.sp, FontWeight.Black, lineHeight = 34.sp)
                TText("12 câu · cần đúng ngay lần đầu từ 10 câu để hoàn thành", 15.sp, FontWeight.SemiBold, TD.Muted)
            }
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(TD.Card).border(1.dp, TD.Line, RoundedCornerShape(20.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RuleLine(Icons.Rounded.Lightbulb, TD.Amber, "Sai lần 1", "Con nhận gợi ý và được thử lại.")
                RuleLine(Icons.Rounded.Psychology, TD.Violet, "Sai lần 2", "Phân tích lỗi + video giải chính câu đó.")
                RuleLine(Icons.Rounded.Replay, TD.Green, "Câu sinh đôi", "Làm đúng câu cùng dạng để được dấu \"Đã sửa lỗi\".")
                RuleLine(Icons.Rounded.LocalFireDepartment, Color(0xFFFF9F45), "Chuỗi đúng", "Đúng liên tiếp để giữ ngọn lửa.")
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton("Bắt đầu nhiệm vụ", onClick = onStart)
        }
    }
}

@Composable
private fun RuleLine(icon: ImageVector, tint: Color, title: String, body: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(tint.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
    Spacer(Modifier.width(12.dp))
    Column {
        TText(title, 15.sp, FontWeight.Black)
        TText(body, 14.sp, FontWeight.SemiBold, TD.Muted, lineHeight = 19.sp)
    }
}

/** Thẻ học viên "Lớp Tư duy" – mã học viên cố định theo tên con. */
@Composable
internal fun StudentCard(childName: String, grade: Int) {
    val code = remember(childName) { "TD$grade-" + (kotlin.math.abs(childName.hashCode()) % 9000 + 1000) }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF2B3A86), Color(0xFF5B3FD6), Color(0xFF7A4FE0))))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Psychology, contentDescription = null, tint = TD.Amber, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(6.dp))
                TText("THẺ HỌC VIÊN · LỚP TƯ DUY", 12.sp, FontWeight.Black, Color.White.copy(alpha = 0.85f), letterSpacing = 1.2.sp)
            }
            TText(childName.ifBlank { "Học viên" }, 26.sp, FontWeight.Black, Color.White, maxLines = 1)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                CardField("Mã học viên", code)
                CardField("Khối", "Lớp $grade")
                CardField("Khóa", "Thử nghiệm 01")
            }
        }
    }
}

@Composable
private fun CardField(label: String, value: String) = Column {
    TText(label.uppercase(), 10.sp, FontWeight.ExtraBold, Color.White.copy(alpha = 0.65f), letterSpacing = 0.8.sp)
    TText(value, 15.sp, FontWeight.Black, Color.White)
}
