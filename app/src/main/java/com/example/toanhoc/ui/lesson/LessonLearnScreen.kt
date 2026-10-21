package com.example.toanhoc.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.curriculum.TeachCard
import com.example.toanhoc.domain.curriculum.TextbookLesson
import com.example.toanhoc.domain.curriculum.TryQuestion
import com.example.toanhoc.ui.canvas.BlueprintInterpreter
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.sound.SoundEffectsManager
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber

private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Line = Color(0xFFE4E7EC)
private val Paper = Color(0xFFF7F8FA)

private sealed interface LearnPage {
    data class Teach(val card: TeachCard) : LearnPage
    data class Tips(val tips: List<String>) : LearnPage
    data class Try(val q: TryQuestion, val number: Int, val of: Int) : LearnPage
}

/**
 * "Học trước, làm sau": từng thẻ ngắn – cách làm ở trường (kèm sơ đồ) → mẹo → thử ngay vài câu.
 * Xong thì bước Học bài được đánh dấu hoàn thành và mở Dạng 1.
 */
@Composable
fun LessonLearnScreen(
    lesson: TextbookLesson,
    onClose: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val teaching = lesson.teaching ?: return
    val context = LocalContext.current
    val accent = domainColor(lesson.domain)
    val pages = remember(lesson.id) {
        buildList<LearnPage> {
            teaching.cards.forEach { add(LearnPage.Teach(it)) }
            if (teaching.tips.isNotEmpty()) add(LearnPage.Tips(teaching.tips))
            teaching.tryIt.forEachIndexed { i, q -> add(LearnPage.Try(q, i + 1, teaching.tryIt.size)) }
        }
    }
    var index by rememberSaveable(lesson.id) { mutableStateOf(0) }
    var picked by rememberSaveable(lesson.id, index) { mutableStateOf<String?>(null) }
    val page = pages[index]
    val isLast = index == pages.lastIndex
    val canGoOn = page !is LearnPage.Try || picked != null

    BackHandler { if (index > 0) index-- else onClose() }

    Column(modifier.fillMaxSize().background(Paper)) {
        // Thanh trên: đóng + tiến độ từng thẻ
        Row(
            Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onClose),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = Ink) }
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pages.indices.forEach { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (i <= index) accent else Line)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = index,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f),
            label = "learnPage"
        ) { i ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Bài ${lesson.number} · Học trước",
                    fontFamily = NunitoFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = accent
                )
                when (val p = pages[i]) {
                    is LearnPage.Teach -> TeachPage(p.card)
                    is LearnPage.Tips -> TipsPage(p.tips)
                    is LearnPage.Try -> TryPage(p, picked) { opt ->
                        if (picked == null) {
                            picked = opt
                            if (opt == p.q.answer) SoundEffectsManager.playSuccess(context) else SoundEffectsManager.playFailure(context)
                        }
                    }
                }
            }
        }

        // Nút tiếp
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .drawBehind { drawLine(Line, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1.dp.toPx()) }
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canGoOn) accent else Line)
                    .clickable(enabled = canGoOn, role = Role.Button) {
                        SoundEffectsManager.playSelect(context)
                        if (isLast) onFinish() else index++
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    when {
                        isLast -> "Xong, vào làm Dạng 1"
                        page is LearnPage.Try && picked == null -> "Chọn một đáp án"
                        else -> "Tiếp"
                    },
                    fontFamily = NunitoFontFamily, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (canGoOn) Color.White else Muted
                )
                if (canGoOn) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TeachPage(card: TeachCard) {
    Text(card.title, fontFamily = NunitoFontFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Ink, lineHeight = 30.sp)
    val diagram = remember(card) {
        card.diagramType?.let {
            BlueprintInterpreter.interpret(it, card.diagramExpr.ifEmpty { card.diagramStatement }, "", card.diagramStatement, emptyList())
        }
    }
    if (diagram != null) {
        DiagramView(
            diagram,
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 6.dp)
        )
    }
    Text(card.body, fontFamily = NunitoFontFamily, fontSize = 18.sp, color = Ink, lineHeight = 28.sp)
}

@Composable
private fun TipsPage(tips: List<String>) {
    Text("Mẹo nhỏ", fontFamily = NunitoFontFamily, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
    tips.forEach { tip ->
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SoftAmberBg)
                .drawBehind { drawRect(TargetUnknownAmber, size = Size(3.dp.toPx(), size.height)) }
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = TargetUnknownAmber, modifier = Modifier.padding(top = 2.dp).size(20.dp))
            Text(tip, fontFamily = NunitoFontFamily, fontSize = 16.sp, color = Ink, lineHeight = 24.sp)
        }
    }
}

@Composable
private fun TryPage(p: LearnPage.Try, picked: String?, onPick: (String) -> Unit) {
    Text("Thử ngay · Câu ${p.number}/${p.of}", fontFamily = NunitoFontFamily, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Muted)
    Text(p.q.statement, fontFamily = NunitoFontFamily, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ink, lineHeight = 30.sp)
    p.q.options.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { opt ->
                val isAnswer = opt == p.q.answer
                val (bg, border, fg) = when {
                    picked == null -> Triple(Color.White, Line, Ink)
                    isAnswer -> Triple(SoftGreenBg, MasteryVerifiedGreen, MasteryVerifiedGreen)
                    opt == picked -> Triple(SoftRedBg, ErrorDiagnosticRed, ErrorDiagnosticRed)
                    else -> Triple(Color.White, Line, Muted)
                }
                Box(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(if (picked != null && (isAnswer || opt == picked)) 2.dp else 1.dp, border, RoundedCornerShape(14.dp))
                        .clickable(enabled = picked == null, role = Role.Button) { onPick(opt) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(opt, fontFamily = NunitoFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = fg)
                }
            }
        }
    }
    if (picked != null) {
        val right = picked == p.q.answer
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (right) SoftGreenBg else SoftRedBg)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                if (right) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null,
                tint = if (right) MasteryVerifiedGreen else ErrorDiagnosticRed, modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (right) "Đúng rồi!" else "Chưa đúng, đáp án là ${p.q.answer}",
                    fontFamily = NunitoFontFamily, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold,
                    color = if (right) MasteryVerifiedGreen else ErrorDiagnosticRed
                )
                Text(p.q.explain, fontFamily = NunitoFontFamily, fontSize = 15.sp, color = Ink, lineHeight = 22.sp)
            }
        }
    }
}
