package com.example.toanhoc.ui.theory

import android.speech.tts.TextToSpeech
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.theory.Hue
import com.example.toanhoc.domain.theory.KidTheory
import com.example.toanhoc.domain.theory.Tile
import com.example.toanhoc.domain.theory.TheoryStep
import com.example.toanhoc.domain.theory.Tok
import com.example.toanhoc.domain.theory.Visual
import com.example.toanhoc.ui.theme.NunitoFontFamily
import java.util.Locale

/* Bảng màu thống nhất với bản demo đã duyệt */
private object K {
    val Ink = Color(0xFF111827); val Muted = Color(0xFF6B7280); val Line = Color(0xFFE4E7EC); val Paper = Color(0xFFF7F8FA)
    val Blue = Color(0xFF2F55D4); val BlueSoft = Color(0xFFE3EEFF)
    val Green = Color(0xFF1E7F4F); val GreenSoft = Color(0xFFDCF5E7); val GreenBox = Color(0xFFEAF4EE)
    val Purple = Color(0xFF6D28D9); val PurpleSoft = Color(0xFFEDE9FE)
    val Orange = Color(0xFFC2620A); val OrangeSoft = Color(0xFFFFE8CC)
    val Amber = Color(0xFFB45F06); val AmberBg = Color(0xFFFBF3EA); val AmberLine = Color(0xFFF3DDC2); val AmberDot = Color(0xFFE8952E)
}

private fun fg(h: Hue) = when (h) { Hue.BLUE -> K.Blue; Hue.GREEN -> K.Green; Hue.PURPLE -> K.Purple; Hue.ORANGE -> K.Orange; Hue.MUTED -> K.Muted; Hue.INK -> K.Ink }
private fun bg(h: Hue) = when (h) { Hue.BLUE -> K.BlueSoft; Hue.GREEN -> K.GreenSoft; Hue.PURPLE -> K.PurpleSoft; Hue.ORANGE -> K.OrangeSoft; Hue.MUTED, Hue.INK -> Color(0xFFF1F3F6) }

@Composable
private fun KText(text: String, size: TextUnit, weight: FontWeight = FontWeight.Normal, color: Color = K.Ink, modifier: Modifier = Modifier, lineHeight: TextUnit = TextUnit.Unspecified, align: TextAlign? = null) =
    Text(text, modifier = modifier, fontFamily = NunitoFontFamily, fontSize = size, fontWeight = weight, color = color, lineHeight = lineHeight, textAlign = align)

/* ------------------------------------------ Đọc to ------------------------------------------ */

/** Đọc to bằng giọng bài giảng (giọng chuẩn Piper trên máy; giọng Google nếu máy không chạy được). */
class Speaker internal constructor(
    private val voice: com.example.toanhoc.ui.lecture.LectureVoice,
    private val scope: kotlinx.coroutines.CoroutineScope,
    private val onState: (Boolean) -> Unit
) {
    private var job: kotlinx.coroutines.Job? = null
    fun speak(text: String) {
        job?.cancel(); voice.stop(); onState(true)
        job = scope.launch { voice.speak(text); onState(false) }
    }
    fun stop() { job?.cancel(); voice.stop(); onState(false) }
}

@Composable
fun rememberSpeaker(): Pair<Speaker, Boolean> {
    var speaking by remember { mutableStateOf(false) }
    val voice = com.example.toanhoc.ui.lecture.rememberLectureVoice()
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val speaker = remember(voice) { Speaker(voice, scope) { speaking = it } }
    return speaker to speaking
}

/* ---------------------------------------- Màn lý thuyết ---------------------------------------- */

/**
 * Nội dung lý thuyết đầy đủ: đầu trang (chủ đề + Đọc to) · GHI NHỚ · VÍ DỤ (các bước) · câu của con + nút thử.
 */
@Composable
fun KidTheoryContent(theory: KidTheory, gradeLabel: String?, childQuestion: String?, onTry: () -> Unit, modifier: Modifier = Modifier) {
    val (speaker, speaking) = rememberSpeaker()
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (gradeLabel != null) KText(gradeLabel, 12.sp, FontWeight.ExtraBold, K.Muted)
                KText(theory.topic, 19.sp, FontWeight.Black)
            }
            Row(
                Modifier.heightIn(min = 40.dp).clip(CircleShape).background(Color.White).border(1.dp, K.Line, CircleShape)
                    .clickable(role = Role.Button) { if (speaking) speaker.stop() else speaker.speak(theory.speechText()) }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(if (speaking) Icons.Rounded.StopCircle else Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, tint = K.Blue, modifier = Modifier.size(18.dp))
                KText(if (speaking) "Dừng" else "Đọc to", 13.sp, FontWeight.ExtraBold, K.Blue)
            }
        }
        RuleCard(theory)
        ExampleCard(theory, label = "Ví dụ")
        if (childQuestion != null) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White)
                        .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(14.dp)).padding(12.dp)
                ) {
                    Text(
                        buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF374151), fontWeight = FontWeight.Bold)) { append("Câu của con: ") }
                            withStyle(SpanStyle(color = K.Ink, fontWeight = FontWeight.ExtraBold)) { append(childQuestion) }
                        },
                        fontFamily = NunitoFontFamily, fontSize = 15.sp, lineHeight = 21.sp
                    )
                }
                Row(
                    Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(16.dp)).background(K.Blue)
                        .clickable(role = Role.Button) { speaker.stop(); onTry() },
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                ) {
                    KText("Con thử làm câu của mình", 17.sp, FontWeight.Black, Color.White)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/** Khung GHI NHỚ: quy tắc ngắn + thẻ mẫu. */
@Composable
fun RuleCard(theory: KidTheory) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(K.AmberBg).border(1.dp, K.AmberLine, RoundedCornerShape(20.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(K.AmberDot), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            KText("Ghi nhớ", 19.sp, FontWeight.Black, K.Amber)
        }
        theory.rule.forEach { line -> RichLine(line, 16.sp) }
        if (theory.tiles.isNotEmpty()) TileGrid(theory.tiles)
    }
}

/** Khung VÍ DỤ: đề + các bước + kết luận. Dùng chung cho nút Lý thuyết và Gợi ý. */
@Composable
fun ExampleCard(theory: KidTheory, label: String) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, K.Line, RoundedCornerShape(20.dp)).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KText(label, 14.sp, FontWeight.ExtraBold, K.Muted, Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).background(Color(0xFFEEF2FC)).padding(horizontal = 10.dp, vertical = 3.dp)) {
                KText("Cùng dạng · khác số", 12.sp, FontWeight.ExtraBold, K.Blue)
            }
        }
        KText(theory.example, 19.sp, FontWeight.ExtraBold, lineHeight = 26.sp)
        Box(Modifier.fillMaxWidth().height(1.dp).background(K.Line))
        KText("Cách giải", 14.sp, FontWeight.ExtraBold, K.Muted)
        theory.steps.forEachIndexed { i, st -> StepRow(i + 1, st) }
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(K.GreenBox).padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = K.Green, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            KText(theory.conclusion, 17.sp, FontWeight.Black, Color(0xFF17663F), lineHeight = 23.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StepRow(n: Int, st: TheoryStep) {
    Row {
        Box(Modifier.size(30.dp).clip(CircleShape).background(K.Blue), contentAlignment = Alignment.Center) {
            KText("$n", 15.sp, FontWeight.Black, Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            KText(st.title, 17.sp, FontWeight.Black, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
            if (st.line.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    st.line.forEach { t -> BigTok(t) }
                }
            }
            st.visual?.let { VisualView(it) }
            st.note?.let { if (com.example.toanhoc.ui.design.hasFraction(it)) com.example.toanhoc.ui.design.MathParagraph(it, 15.sp, Color(0xFF4B5563), FontWeight.Normal) else KText(it, 15.sp, color = Color(0xFF4B5563), lineHeight = 21.sp) }
        }
    }
}

@Composable
private fun BigTok(t: Tok) {
    val longLine = (t as? Tok.T)?.s?.length ?: 0
    val size = if (longLine > 22) 18.sp else 24.sp
    when (t) {
        is Tok.T -> if (com.example.toanhoc.ui.design.hasFraction(t.s)) com.example.toanhoc.ui.design.InlineMath(t.s.trim(), size, if (t.hue == Hue.INK) K.Ink else fg(t.hue), FontWeight.Black, Modifier.padding(vertical = 2.dp))
            else KText(t.s.trim(), size, FontWeight.Black, if (t.hue == Hue.INK) K.Ink else fg(t.hue), Modifier.padding(vertical = 2.dp))
        is Tok.P -> Box(Modifier.clip(RoundedCornerShape(12.dp)).background(bg(t.hue)).padding(horizontal = 10.dp, vertical = 2.dp)) {
            if (com.example.toanhoc.ui.design.hasFraction(t.s)) com.example.toanhoc.ui.design.InlineMath(t.s, if (t.s.length > 16) 16.sp else 22.sp, fg(t.hue), FontWeight.Black)
            else KText(t.s, if (t.s.length > 16) 16.sp else 22.sp, FontWeight.Black, fg(t.hue))
        }
    }
}

/** Câu thường có từ khoá tô màu (tự xuống dòng tự nhiên). */
@Composable
internal fun RichLine(line: List<Tok>, size: TextUnit) {
    val plain = line.joinToString("") { when (it) { is Tok.T -> it.s; is Tok.P -> it.s } }
    if (com.example.toanhoc.ui.design.hasFraction(plain)) { com.example.toanhoc.ui.design.MathParagraph(plain, size, K.Ink, FontWeight.SemiBold); return }
    Text(
        buildAnnotatedString {
            line.forEach { t ->
                when (t) {
                    is Tok.T -> withStyle(SpanStyle(color = if (t.hue == Hue.INK) K.Ink else fg(t.hue), fontWeight = FontWeight.SemiBold)) { append(t.s) }
                    is Tok.P -> withStyle(SpanStyle(color = fg(t.hue), background = bg(t.hue), fontWeight = FontWeight.Black)) { append(" ${t.s} ") }
                }
            }
        },
        fontFamily = NunitoFontFamily, fontSize = size, lineHeight = (size.value * 1.5f).sp, color = K.Ink
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TileGrid(tiles: List<Tile>) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp), maxItemsInEachRow = if (tiles.size == 4) 4 else 3) {
        tiles.forEach { t ->
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (t.hue == Hue.INK) Color.White else bg(t.hue)).padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (com.example.toanhoc.ui.design.hasFraction(t.top)) com.example.toanhoc.ui.design.InlineMath(t.top, if (t.top.length > 8) 15.sp else 20.sp, fg(t.hue), FontWeight.Black)
                else KText(t.top, if (t.top.length > 8) 15.sp else 20.sp, FontWeight.Black, fg(t.hue), align = TextAlign.Center)
                KText(t.bottom, 12.sp, FontWeight.ExtraBold, fg(t.hue).copy(alpha = 0.85f), align = TextAlign.Center, lineHeight = 16.sp)
            }
        }
    }
}

/* ------------------------------------------ Hình minh hoạ ------------------------------------------ */

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VisualView(v: Visual) {
    when (v) {
        is Visual.Column -> ColumnView(v)
        is Visual.Places -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            v.cells.forEach { c ->
                Column(
                    Modifier.widthIn(min = 52.dp).clip(RoundedCornerShape(12.dp)).background(bg(c.hue)).padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    KText(c.top, 24.sp, FontWeight.Black, fg(c.hue))
                    KText(c.bottom, 10.sp, FontWeight.ExtraBold, fg(c.hue))
                }
            }
        }
        is Visual.Dots -> FlowRow(Modifier.clip(RoundedCornerShape(12.dp)).background(K.Paper).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            v.groups.forEach { g ->
                repeat(g.count) {
                    Box(Modifier.size(22.dp).clip(CircleShape).background(if (g.crossed) bg(g.hue) else fg(g.hue)), contentAlignment = Alignment.Center) {
                        if (g.crossed) KText("✕", 13.sp, FontWeight.Black, fg(g.hue))
                    }
                }
            }
        }
        is Visual.Bars -> BarsView(v)
    }
}

@Composable
private fun ColumnView(v: Visual.Column) {
    val all = v.rows + listOfNotNull(v.result) + v.partials
    val width = all.maxOf { it.length }
    val cell = 26.dp
    @Composable fun DigitRow(text: String, color: Color, op: String = "", shift: Int = 0, small: Boolean = false) {
        Row {
            Box(Modifier.width(cell), contentAlignment = Alignment.Center) { if (op.isNotEmpty()) KText(if (op == "-") "−" else op, 22.sp, FontWeight.ExtraBold, K.Muted) }
            val padded = " ".repeat((width - text.length - shift).coerceAtLeast(0)) + text + " ".repeat(shift)
            padded.forEach { ch ->
                Box(Modifier.width(cell), contentAlignment = Alignment.Center) {
                    if (ch != ' ') KText(ch.toString(), if (small) 13.sp else 26.sp, FontWeight.Black, color)
                }
            }
        }
    }
    Column(Modifier.clip(RoundedCornerShape(14.dp)).background(K.Paper).padding(horizontal = 12.dp, vertical = 8.dp)) {
        if (v.carries.any { it.isNotEmpty() }) {
            Row {
                Spacer(Modifier.width(cell))
                (width - 1 downTo 0).forEach { i ->
                    Box(Modifier.width(cell), contentAlignment = Alignment.Center) {
                        val c = v.carries.getOrNull(i).orEmpty()
                        if (c.isNotEmpty()) KText(c, 13.sp, FontWeight.Black, K.Orange)
                    }
                }
            }
        }
        v.rows.forEachIndexed { i, r -> DigitRow(r, if (i == 0) K.Blue else K.Green, if (i == v.rows.lastIndex) v.op else "") }
        Box(Modifier.padding(start = cell).width(cell * width).height(3.dp).clip(RoundedCornerShape(2.dp)).background(K.Ink))
        if (v.partials.isNotEmpty()) {
            v.partials.forEachIndexed { i, p -> DigitRow(p, K.Purple, if (i == v.partials.lastIndex) "+" else "", shift = i) }
            Box(Modifier.padding(start = cell).width(cell * width).height(2.dp).background(K.Ink))
        }
        if (v.result != null) DigitRow(v.result, K.Green)
        else DigitRow("?".repeat(maxOf(v.rows.maxOf { it.length }, 1)), Color(0xFFD1D5DB))
    }
}

@Composable
private fun BarsView(v: Visual.Bars) {
    val maxUnits = v.rows.maxOf { r -> r.segments.sumOf { it.units.toDouble() } }.toFloat().coerceAtLeast(1f)
    val full = 200.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            v.rows.forEach { r ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (r.label.isNotEmpty()) KText(r.label, 13.sp, FontWeight.Black, modifier = Modifier.width(52.dp))
                    r.segments.forEachIndexed { i, sg ->
                        Box(
                            Modifier.width(full * (sg.units / maxUnits)).height(26.dp)
                                .clip(RoundedCornerShape(topStart = if (i == 0) 6.dp else 0.dp, bottomStart = if (i == 0) 6.dp else 0.dp, topEnd = if (i == r.segments.lastIndex) 6.dp else 0.dp, bottomEnd = if (i == r.segments.lastIndex) 6.dp else 0.dp))
                                .background(when (sg.hue) { Hue.ORANGE -> Color(0xFFF6B26B); Hue.PURPLE -> Color(0xFFC4B5FD); Hue.GREEN -> Color(0xFF8ED1AE); else -> Color(0xFF9DB6F5) })
                                .border(1.dp, Color.White),
                            contentAlignment = Alignment.Center
                        ) { sg.label?.let { KText(it, 12.sp, FontWeight.Black, Color(0xFF1F2937)) } }
                    }
                }
            }
        }
        if (v.bracket != null) {
            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(10.dp).height((v.rows.size * 36 - 10).dp).drawBehind {
                val w = 2.5.dp.toPx()
                drawLine(K.Blue, Offset(0f, w / 2), Offset(size.width, w / 2), w)
                drawLine(K.Blue, Offset(size.width - w / 2, 0f), Offset(size.width - w / 2, size.height), w)
                drawLine(K.Blue, Offset(0f, size.height - w / 2), Offset(size.width, size.height - w / 2), w)
            })
            Spacer(Modifier.width(6.dp))
            KText(v.bracket, 15.sp, FontWeight.Black, K.Blue)
        }
    }
}
