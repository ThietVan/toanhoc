package com.example.toanhoc.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.model.VisualBlueprintType
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.canvas.rememberDiagram
import com.example.toanhoc.ui.components.PedagogicalExplanationCard
import com.example.toanhoc.ui.design.FormattedMathStatement
import com.example.toanhoc.ui.design.StackedFractionView
import com.example.toanhoc.ui.sound.SoundEffectsManager
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.ui.semantics.Role
import com.example.toanhoc.domain.lecture.Lecture
import com.example.toanhoc.domain.lecture.Lectures
import com.example.toanhoc.ui.lecture.LectureOverlay
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber

private const val PHI = 1.618f

private val Ink = Color(0xFF111827)
private val InkSoft = Color(0xFF4B5563)
private val Paper = Color(0xFFF7F8FA)
private val Hairline = Color(0xFFE4E7EC)

private val S1 = 5.dp
private val S2 = 8.dp
private val S3 = 13.dp
private val S4 = 21.dp
private val S5 = 34.dp
private val S6 = 55.dp

@Composable
fun DeliberatePracticeScreen(
    uiState: SessionUiState,
    onSelectOption: (String) -> Unit,
    onPauseToggle: (Boolean) -> Unit,
    onSaveAndExit: () -> Unit,
    onOpenTheory: () -> Unit,
    onCloseTheory: () -> Unit,
    onStepBack: () -> Unit,
    onDontKnowOrSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onPauseToggle(true)
    }

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    DisposableEffect(Unit) {
        SoundEffectsManager.init(context)
        SoundEffectsManager.resetStreak()
        onDispose { }
    }

    LaunchedEffect(uiState.isCurrentStepError) {
        if (uiState.isCurrentStepError) {
            SoundEffectsManager.playFailure(context)
        }
    }

    LaunchedEffect(uiState.totalAnsweredCount) {
        if (uiState.totalAnsweredCount > 0 && !uiState.isCurrentStepError) {
            SoundEffectsManager.playSuccess(context)
        }
    }

    val problem = uiState.currentProblem
    val steps = problem?.steps
    val currentStep = steps?.getOrNull(uiState.currentStepIndex)
    val optionsList = currentStep?.options ?: emptyList()

    // Chống bấm vội: câu/bước mới hiện ra → khoá đáp án 0,6 giây; vừa chọn sai → khoá 0,8 giây
    val questionKey = "${problem?.problemId}:${uiState.currentStepIndex}:${uiState.totalAnsweredCount}"
    var unlocked by remember(questionKey) { mutableStateOf(false) }
    LaunchedEffect(questionKey) { delay(600); unlocked = true }
    LaunchedEffect(uiState.isCurrentStepError, uiState.userInput) {
        if (uiState.isCurrentStepError) { unlocked = false; delay(800); unlocked = true }
    }
    val canAnswer = unlocked && !uiState.feedbackCorrect && uiState.currentHintLevel < 2

    // "Video giải câu này": giải lại CHÍNH bài con vừa sai (cách ở trường + cách nhanh nếu có)
    var video by remember(problem?.problemId) { mutableStateOf<Lecture?>(null) }

    val totalProblems = 12
    val currentNumber = (uiState.totalAnsweredCount + 1).coerceIn(1, totalProblems)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
        ) {
            // 1. Practice Top Bar with Count-Up Elapsed Timer
            PracticeTopBar(
                currentNumber = currentNumber,
                totalProblems = totalProblems,
                elapsedSeconds = uiState.elapsedSeconds,
                onClose = onSaveAndExit
            )

            // 2. Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = S4, vertical = S3),
                verticalArrangement = Arrangement.spacedBy(S4)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(S4))
                        .background(Color.White)
                        .border(1.dp, Hairline, RoundedCornerShape(S4))
                        .padding(S4),
                    verticalArrangement = Arrangement.spacedBy(S3)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(S2)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(S5)
                                    .clip(RoundedCornerShape(S3))
                                    .background(SoftBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = null,
                                    tint = GivenDataBlue,
                                    modifier = Modifier.size(S4)
                                )
                            }
                            Column {
                                Text(
                                    text = "Quan sát và suy nghĩ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NunitoFontFamily,
                                    color = GivenDataBlue
                                )
                                if (steps != null && steps.size > 1) {
                                    Text(
                                        text = "Bước ${uiState.currentStepIndex + 1} trên ${steps.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = NunitoFontFamily,
                                        color = InkSoft
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(S3))
                                .border(1.dp, Hairline, RoundedCornerShape(S3))
                                .clickable {
                                    SoundEffectsManager.playSelect(context)
                                    onOpenTheory()
                                }
                                .padding(horizontal = S3, vertical = S2),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(S1)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoStories,
                                contentDescription = null,
                                tint = InkSoft,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Lý thuyết",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily,
                                color = InkSoft
                            )
                        }
                    }

                    FormattedMathStatement(
                        statement = problem?.statementVi ?: "",
                        fontSize = 22.sp,
                        color = Ink
                    )

                    val diagram = rememberDiagram(problem)
                    if (diagram != null) {
                        DiagramView(
                            diagram = diagram,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(S3))
                                .background(Paper)
                                .padding(vertical = S3, horizontal = S1)
                        )
                    }
                }

                if (currentStep != null) {
                    val promptSubtitle = when {
                        currentStep.promptVi.isNotEmpty() && !currentStep.promptVi.startsWith("Chọn phương án đúng") -> currentStep.promptVi
                        optionsList.any { opt -> opt.contains("Hình") || opt.contains("Khối") || opt.contains("đoạn") } -> "Bé hãy suy nghĩ và chọn hình thích hợp nhé"
                        optionsList.all { opt -> opt.trim().all { ch -> ch.isDigit() || ch == ',' || ch == '.' || ch == '-' || ch == '/' || ch.isWhitespace() } } -> "Bé hãy tính toán và chọn số thích hợp nhé"
                        else -> "Bé hãy suy nghĩ và chọn đáp án đúng nhất nhé"
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(S3)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Chọn đáp án đúng",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NunitoFontFamily,
                                color = Ink
                            )
                            Text(
                                text = promptSubtitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = NunitoFontFamily,
                                color = InkSoft
                            )
                        }

                        val itemsPerRow = if (optionsList.size > 4) 4 else 2
                        // Một cỡ chữ chung cho cả 4 ô, theo đáp án dài nhất
                        val longest = optionsList.maxOfOrNull { it.length } ?: 0
                        val optionFont = when {
                            longest <= 4 -> 20.sp
                            longest <= 8 -> 17.sp
                            longest <= 14 -> 15.sp
                            else -> 14.sp
                        }
                        optionsList.chunked(itemsPerRow).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(S3)
                            ) {
                                rowItems.forEach { optionText ->
                                    val isSelected = uiState.userInput == optionText
                                    val isWrong = isSelected && uiState.isCurrentStepError
                                    // Sai lần 1: chưa lộ đáp án (con tự nghĩ lại / xem ví dụ); từ lần 2 mới tô đáp án đúng
                                    val isRevealedCorrect = ((uiState.isCurrentStepError && uiState.wrongCount >= 2) || uiState.feedbackCorrect || uiState.currentHintLevel >= 2) &&
                                        optionText.trim() == currentStep.expectedAnswer.trim()
                                    AnswerTile(
                                        text = optionText,
                                        selected = isSelected,
                                        wrong = isWrong,
                                        correct = isRevealedCorrect,
                                        textSize = optionFont,
                                        enabled = canAnswer,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            onSelectOption(optionText)
                                        }
                                    )
                                }
                                repeat(itemsPerRow - rowItems.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Gợi ý: ví dụ tương tự đã giải sẵn
                val example = uiState.exampleProblem
                if (uiState.currentHintLevel == 1 && example != null) {
                    ExampleHintCard(example)
                }
                // Sai lần đầu: chỉ nhắc nhẹ, chưa cho xem đáp án
                if (uiState.isCurrentStepError && uiState.wrongCount == 1 && uiState.currentHintLevel < 2) {
                    TryAgainNudge(canUseExample = uiState.currentHintLevel == 0 && uiState.examplesUsed < SessionUiState.MAX_EXAMPLES)
                }
                if ((uiState.isCurrentStepError && uiState.wrongCount >= 2) || uiState.currentHintLevel >= 2 ||
                    (uiState.currentHintLevel == 1 && example == null)) {
                    PedagogicalExplanationCard(
                        gradeLevel = uiState.gradeLevel,
                        problem = problem,
                        socraticExplanation = uiState.socraticExplanationVi,
                        bugTitle = uiState.detectedBugTag?.titleVi,
                        userAnswer = if (uiState.isCurrentStepError) uiState.userInput else null
                    )
                    if (problem != null) {
                        val picked = if (uiState.isCurrentStepError) uiState.userInput.takeIf { it.isNotBlank() } else null
                        val lecture = remember(problem.problemId, picked) { runCatching { Lectures.forMistake(problem, picked) }.getOrNull() }
                        if (lecture != null) {
                            Row(
                                Modifier.fillMaxWidth().heightIn(min = 52.dp).clip(RoundedCornerShape(16.dp)).background(SoftBlueBg)
                                    .border(1.dp, GivenDataBlue.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .clickable(role = Role.Button) { SoundEffectsManager.playSelect(context); video = lecture }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.PlayCircle, contentDescription = null, tint = GivenDataBlue, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(S2))
                                Column(Modifier.weight(1f)) {
                                    Text("Xem video giải câu này", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = GivenDataBlue)
                                    val hasQuick = lecture.scenes.any { it is com.example.toanhoc.domain.lecture.LectureScene.Quick }
                                    Text(
                                        if (hasQuick) "Có giọng đọc · cách ở trường và cách nhanh" else "Có giọng đọc · giải từng bước",
                                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = NunitoFontFamily, color = Color(0xFF5B6476)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Thanh dưới: một nút đổi theo tình huống (bỏ "Câu trước" vì luyện tập không quay lại câu cũ)
            //  - vừa đúng: thanh xanh "Chính xác!" (tự sang câu sau)
            //  - chưa trả lời / sai: "Gợi ý" → "Xem lời giải"
            //  - đã xem lời giải: "Tiếp tục"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (uiState.feedbackCorrect) SoftGreenBg else Color.White)
                    .drawBehind {
                        drawLine(color = Hairline, start = Offset(0f, 0f), end = Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                    }
                    .padding(horizontal = S4, vertical = S3)
                    .heightIn(min = S6),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.feedbackCorrect -> Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(28.dp).clip(CircleShape).background(MasteryVerifiedGreen), contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(S2))
                        Text("Chính xác!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = MasteryVerifiedGreen)
                    }
                    uiState.currentHintLevel >= 2 -> BottomAction("Tiếp tục", primary = true) {
                        SoundEffectsManager.playSelect(context)
                        onDontKnowOrSkip()
                    }
                    else -> BottomAction(
                        when {
                            uiState.currentHintLevel == 1 -> "Xem lời giải"
                            uiState.examplesUsed >= SessionUiState.MAX_EXAMPLES -> "Xem lời giải"
                            else -> "Gợi ý: xem ví dụ tương tự (còn ${SessionUiState.MAX_EXAMPLES - uiState.examplesUsed})"
                        },
                        primary = false
                    ) {
                        SoundEffectsManager.playSelect(context)
                        onDontKnowOrSkip()
                    }
                }
            }
        }

        if (uiState.isPaused) {
            BlindedPauseOverlay(
                elapsedSeconds = uiState.elapsedSeconds,
                onResumeSession = { onPauseToggle(false) },
                onSaveAndExit = onSaveAndExit
            )
        }

        video?.let { lec ->
            LectureOverlay(
                lecture = lec, kicker = "VIDEO GIẢI BÀI", title = "Giải chính câu con vừa làm",
                onClose = { video = null }, onDone = { video = null }
            )
        }

        if (uiState.isBottomSheetOpen) {
            TheoryBottomSheet(
                problem = problem,
                onDismiss = onCloseTheory,
                example = uiState.theoryExample,
                gradeLevel = uiState.gradeLevel
            )
        }
    }
}

@Composable
private fun PracticeTopBar(
    currentNumber: Int,
    totalProblems: Int,
    elapsedSeconds: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val min = elapsedSeconds / 60
    val sec = elapsedSeconds % 60
    val timeText = "%02d:%02d".format(min, sec)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = S4, vertical = S3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(S3)
    ) {
        Box(
            modifier = Modifier
                .size(S5)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Hairline, CircleShape)
                .clickable {
                    SoundEffectsManager.playSelect(context)
                    onClose()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Lưu và thoát",
                tint = InkSoft,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(S1)
        ) {
            Text(
                text = "Câu $currentNumber trên $totalProblems",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = Ink
            )
            LinearProgressIndicator(
                progress = { currentNumber.toFloat() / totalProblems },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(S2)
                    .clip(CircleShape),
                color = GivenDataBlue,
                trackColor = Hairline
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(S3))
                .background(Color.White)
                .border(1.dp, Hairline, RoundedCornerShape(S3))
                .padding(horizontal = S3, vertical = S2),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(S1)
        ) {
            Icon(
                imageVector = Icons.Rounded.Timer,
                contentDescription = null,
                tint = InkSoft,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = timeText,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = NunitoFontFamily,
                color = Ink
            )
        }
    }
}

@Composable
private fun AnswerTile(
    text: String,
    selected: Boolean,
    wrong: Boolean,
    modifier: Modifier = Modifier,
    correct: Boolean = false,
    textSize: androidx.compose.ui.unit.TextUnit = 17.sp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    // Tối giản: chỉ đổi viền + nền rất nhạt, không tô đặc
    val background = when {
        wrong -> SoftRedBg
        correct -> SoftGreenBg
        selected -> SoftBlueBg
        else -> Color.White
    }
    val borderColor = when {
        wrong -> ErrorDiagnosticRed
        correct -> MasteryVerifiedGreen
        selected -> GivenDataBlue
        else -> Hairline
    }
    val textColor = when {
        wrong -> ErrorDiagnosticRed
        correct -> MasteryVerifiedGreen
        selected -> GivenDataBlue
        else -> Ink
    }
    val emphasized = selected || wrong || correct

    val parts = text.split("/")
    val isFraction = parts.size == 2 && parts[0].trim().all { it.isDigit() || it == '-' } && parts[1].trim().all { it.isDigit() }

    val dynamicFontSize = textSize

    Box(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clip(shape)
            .background(background)
            .border(if (emphasized) 2.dp else 1.dp, borderColor, shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isFraction) {
            StackedFractionView(
                numerator = parts[0],
                denominator = parts[1],
                fontSize = 16.sp,
                color = textColor
            )
        } else {
            Text(
                text = text,
                fontSize = dynamicFontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = NunitoFontFamily,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = (dynamicFontSize.value * 1.3f).sp
            )
        }
        if (wrong || correct) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(borderColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (correct) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = if (correct) "Đáp án đúng" else "Đáp án sai",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomAction(text: String, primary: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(S3)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(S6)
            .clip(shape)
            .background(if (primary) GivenDataBlue else Color.White)
            .border(1.dp, if (primary) GivenDataBlue else Hairline, shape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!primary) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = TargetUnknownAmber, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(S2))
        }
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = if (primary) Color.White else Ink)
        if (primary) {
            Spacer(Modifier.width(S2))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

/** Gợi ý: một bài cùng dạng (khác số) đã giải sẵn từng bước – con áp dụng cách làm vào câu của mình. */
@Composable
private fun ExampleHintCard(example: com.example.toanhoc.domain.model.AcademicProblem) {
    // Có lý thuyết cho trẻ của dạng này → dùng cùng giao diện với nút Lý thuyết
    val kid = remember(example.problemId, example.statementVi) { com.example.toanhoc.domain.theory.KidTheories.build(example) }
    if (kid != null) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            com.example.toanhoc.ui.theory.ExampleCard(kid, label = "Ví dụ tương tự")
            Text("Giờ con làm câu của mình theo cách tương tự nhé! Làm đúng vẫn được tính là đúng.",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = NunitoFontFamily, color = TargetUnknownAmber, lineHeight = 19.sp)
        }
        return
    }
    val statement = example.statementVi.trim()
    val answer = example.steps.firstOrNull()?.expectedAnswer.orEmpty()
    val steps = example.steps.filter { st ->
        val expr = st.mathExpression.trim()
        (expr.isNotEmpty() && expr != statement) || !st.promptVi.startsWith("Chọn phương án đúng")
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFF5D07A), RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = TargetUnknownAmber, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ví dụ tương tự", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = Ink)
        }
        Text(statement, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, fontFamily = NunitoFontFamily, color = Ink, lineHeight = 23.sp)
        val diagram = remember(example.problemId, example.statementVi) { com.example.toanhoc.ui.canvas.diagramFor(example) }
        if (diagram != null) {
            com.example.toanhoc.ui.canvas.DiagramView(
                diagram,
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(vertical = 10.dp, horizontal = 4.dp)
            )
        }
        steps.forEachIndexed { i, st ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(22.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFF5D07A), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("${i + 1}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = TargetUnknownAmber) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(st.promptVi, fontSize = 14.sp, fontFamily = NunitoFontFamily, color = Ink, lineHeight = 20.sp)
                    val expr = st.mathExpression.trim()
                    if (expr.isNotEmpty() && expr != statement) {
                        Text(expr, fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = NunitoFontFamily, color = GivenDataBlue)
                    }
                }
            }
        }
        if (answer.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Đáp số của ví dụ: ", fontSize = 14.sp, fontFamily = NunitoFontFamily, color = InkSoft)
                Text(answer, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, fontFamily = NunitoFontFamily, color = MasteryVerifiedGreen)
            }
        }
        Text(
            "Giờ con làm câu của mình theo cách tương tự nhé! Làm đúng vẫn được tính là đúng.",
            fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = NunitoFontFamily, color = TargetUnknownAmber, lineHeight = 19.sp
        )
    }
}

/** Sai lần đầu: nhắc nhẹ, chưa lộ đáp án. */
@Composable
private fun TryAgainNudge(canUseExample: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SoftRedBg)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Close, contentDescription = null, tint = ErrorDiagnosticRed, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            if (canUseExample) "Chưa đúng rồi. Con đọc lại đề và thử lại nhé – cần thì bấm Gợi ý để xem ví dụ tương tự."
            else "Chưa đúng rồi. Con đọc lại đề thật kỹ và thử lại nhé.",
            fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = NunitoFontFamily, color = Ink, lineHeight = 20.sp
        )
    }
}
