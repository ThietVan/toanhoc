package com.example.toanhoc.ui.exam

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.canvas.diagramFor
import com.example.toanhoc.ui.design.FormattedMathStatement
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber

private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Line = Color(0xFFE4E7EC)
private val Paper = Color(0xFFF7F8FA)
private val Accent = Color(0xFF5B3FD6)

@Composable
fun SemesterExamScreen(
    state: SemesterExamState,
    onAnswer: (String) -> Unit,
    onGoTo: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onExit)
    Box(modifier.fillMaxSize().background(Paper)) {
        when {
            state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = Accent)
            state.submitted -> ResultView(state, onGoTo, onExit)
            else -> QuestionView(state, onAnswer, onGoTo, onSubmit, onExit)
        }
    }
}

/* ------------------------------------ Làm bài ------------------------------------ */

@Composable
private fun QuestionView(
    state: SemesterExamState,
    onAnswer: (String) -> Unit,
    onGoTo: (Int) -> Unit,
    onSubmit: () -> Unit,
    onExit: () -> Unit
) {
    val q = state.questions.getOrNull(state.index) ?: return
    val step = q.problem.steps.first()
    val picked = state.answers[state.index]
    val isLast = state.index == state.questions.lastIndex
    val unanswered = state.questions.size - state.answers.size

    Column(Modifier.fillMaxSize()) {
        // Thanh trên
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(start = 8.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onExit), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, contentDescription = "Thoát", tint = Ink)
            }
            Column(Modifier.weight(1f)) {
                Text("Kiểm tra học kì ${state.semester}", fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Accent)
                Text("Câu ${state.index + 1}/${state.questions.size}", fontFamily = NunitoFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = Muted, modifier = Modifier.size(18.dp))
                Text("%02d:%02d".format(state.elapsedSeconds / 60, state.elapsedSeconds % 60), fontFamily = NunitoFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
        // Dải số câu: chạm để nhảy câu; đã trả lời = tô màu
        Row(
            Modifier.fillMaxWidth().background(Color.White).horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.questions.indices.forEach { i ->
                val answered = state.answers.containsKey(i)
                val current = i == state.index
                Box(
                    Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (answered) Accent else Color.White)
                        .border(if (current) 2.dp else 1.dp, if (current) Ink else Line, CircleShape)
                        .clickable(role = Role.Button) { onGoTo(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("${i + 1}", fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (answered) Color.White else Muted)
                }
            }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(20.dp)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FormattedMathStatement(statement = q.problem.statementVi, fontSize = 20.sp, color = Ink)
                val diagram = remember(q.problem.problemId, q.problem.statementVi) { diagramFor(q.problem) }
                if (diagram != null) {
                    DiagramView(diagram, Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Paper).padding(vertical = 12.dp, horizontal = 4.dp))
                }
            }
            val longest = step.options.maxOfOrNull { it.length } ?: 0
            val fs = when { longest <= 4 -> 20.sp; longest <= 10 -> 17.sp; longest <= 18 -> 15.sp; else -> 14.sp }
            step.options.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { opt ->
                        val sel = opt == picked
                        Box(
                            Modifier
                                .weight(1f)
                                .heightIn(min = 64.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (sel) SoftBlueBg else Color.White)
                                .border(if (sel) 2.dp else 1.dp, if (sel) GivenDataBlue else Line, RoundedCornerShape(14.dp))
                                .clickable(role = Role.Button) { onAnswer(opt) }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(opt, fontFamily = NunitoFontFamily, fontSize = fs, fontWeight = FontWeight.Bold, color = if (sel) GivenDataBlue else Ink, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
            Text(
                "Không có gợi ý trong bài kiểm tra. Có thể chọn lại đáp án trước khi nộp.",
                fontFamily = NunitoFontFamily, fontSize = 12.sp, color = Muted
            )
        }

        // Thanh dưới
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BottomButton("Câu trước", enabled = state.index > 0, primary = false, modifier = Modifier.weight(1f)) { onGoTo(state.index - 1) }
            if (isLast) {
                BottomButton(
                    if (unanswered > 0) "Nộp bài (còn $unanswered câu trống)" else "Nộp bài",
                    enabled = true, primary = true, modifier = Modifier.weight(1.6f), onClick = onSubmit
                )
            } else {
                BottomButton("Câu sau", enabled = true, primary = true, modifier = Modifier.weight(1.6f)) { onGoTo(state.index + 1) }
            }
        }
    }
}

@Composable
private fun BottomButton(text: String, enabled: Boolean, primary: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (primary) Accent else Color.White)
            .border(1.dp, if (primary) Accent else Line, RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text, fontFamily = NunitoFontFamily, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold,
            color = when { primary -> Color.White; enabled -> Ink; else -> Line }, textAlign = TextAlign.Center
        )
    }
}

/* ------------------------------------ Kết quả ------------------------------------ */

@Composable
private fun ResultView(state: SemesterExamState, onGoTo: (Int) -> Unit, onExit: () -> Unit) {
    val passed = state.passed
    val wrong = state.wrongLessonIds.mapNotNull { Curricula.findLesson(it)?.second }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(84.dp).clip(CircleShape).background(if (passed) SoftGreenBg else SoftAmberBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (passed) Icons.Rounded.EmojiEvents else Icons.Rounded.Replay, contentDescription = null,
                        tint = if (passed) MasteryVerifiedGreen else TargetUnknownAmber, modifier = Modifier.size(42.dp)
                    )
                }
                Text("${state.percent}%", fontFamily = NunitoFontFamily, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                Text(
                    "Đúng ${state.correctCount}/${state.questions.size} câu · ${state.elapsedSeconds / 60} phút",
                    fontFamily = NunitoFontFamily, fontSize = 15.sp, color = Muted
                )
                Text(
                    if (passed) "Đạt bài kiểm tra học kì ${state.semester}!" else "Chưa đạt – cần từ ${SemesterExamState.PASS_PERCENT}% trở lên",
                    fontFamily = NunitoFontFamily, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (passed) MasteryVerifiedGreen else TargetUnknownAmber, textAlign = TextAlign.Center
                )
            }

            if (!passed && wrong.isNotEmpty()) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SoftAmberBg).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Các bài cần học lại từ đầu", fontFamily = NunitoFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                    Text(
                        "Những bài dưới đây có câu làm sai. Con học lại Học bài, Dạng 1, Dạng 2 và Ôn tập của từng bài rồi kiểm tra lại nhé.",
                        fontFamily = NunitoFontFamily, fontSize = 14.sp, color = Ink, lineHeight = 21.sp
                    )
                    wrong.forEach { l ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(TargetUnknownAmber))
                            Text("Bài ${l.number}: ${l.title}", fontFamily = NunitoFontFamily, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                        }
                    }
                }
            }

            Text("Xem lại từng câu", fontFamily = NunitoFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            state.questions.forEachIndexed { i, q ->
                val step = q.problem.steps.first()
                val mine = state.answers[i]
                val right = mine == step.expectedAnswer
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, Line, RoundedCornerShape(14.dp)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(if (right) MasteryVerifiedGreen else ErrorDiagnosticRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(if (right) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = if (right) "Đúng" else "Sai", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Câu ${i + 1}. ${q.problem.statementVi}", fontFamily = NunitoFontFamily, fontSize = 14.sp, color = Ink, lineHeight = 20.sp)
                        if (!right) {
                            Text(
                                "Con chọn: ${mine ?: "(bỏ trống)"} · Đáp án: ${step.expectedAnswer}",
                                fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                color = ErrorDiagnosticRed, modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(SoftRedBg).padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(1.dp))
        }
        Box(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp)) {
            BottomButton("Về trang chủ", enabled = true, primary = true, modifier = Modifier.fillMaxWidth(), onClick = onExit)
        }
    }
}
