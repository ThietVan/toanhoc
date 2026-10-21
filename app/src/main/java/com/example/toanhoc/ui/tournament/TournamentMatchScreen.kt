package com.example.toanhoc.ui.tournament

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.League
import com.example.toanhoc.ui.canvas.DiagramView
import com.example.toanhoc.ui.canvas.diagramFor
import com.example.toanhoc.ui.design.FormattedMathStatement
import com.example.toanhoc.ui.family.AvatarView
import com.example.toanhoc.ui.family.CoinAmount
import com.example.toanhoc.ui.family.FButton
import com.example.toanhoc.ui.family.FText
import com.example.toanhoc.ui.family.FamilyTone

private val Purple = Color(0xFF7C3AED)
private val PurpleSoft = Color(0xFFEDE9FE)

private fun clock(ms: Long): String { val s = ms / 1000; return "%d:%02d".format(s / 60, s % 60) }

@Composable
fun TournamentMatchScreen(
    state: MatchState,
    canPlayAgain: Boolean,
    onAnswer: (String) -> Unit,
    onForfeit: () -> Unit,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmLeave by remember { mutableStateOf(false) }
    BackHandler { if (state.phase == MatchPhase.PLAYING) confirmLeave = true else if (state.phase == MatchPhase.RESULT) onExit() }

    val sfxContext = androidx.compose.ui.platform.LocalContext.current
    val allFound = state.found >= state.opponents.size && state.opponents.isNotEmpty()
    LaunchedEffect(state.phase, allFound) {
        if (state.phase == MatchPhase.MATCHING && allFound) com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, com.example.toanhoc.ui.sound.Sfx.COUNTDOWN)
        if (state.phase == MatchPhase.PLAYING) com.example.toanhoc.ui.sound.SoundEffectsManager.resetStreak()
        if (state.phase == MatchPhase.RESULT && !state.forfeited)
            com.example.toanhoc.ui.sound.SoundEffectsManager.play(sfxContext, if (state.place == 1) com.example.toanhoc.ui.sound.Sfx.BADGE else com.example.toanhoc.ui.sound.Sfx.COMPLETE)
    }
    LaunchedEffect(state.feedback) {
        val fb = state.feedback ?: return@LaunchedEffect
        if (fb.second) com.example.toanhoc.ui.sound.SoundEffectsManager.playSuccess(sfxContext) else com.example.toanhoc.ui.sound.SoundEffectsManager.playFailure(sfxContext)
    }
    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        when (state.phase) {
            MatchPhase.MATCHING -> Matching(state)
            MatchPhase.PLAYING -> Playing(state, onAnswer) { confirmLeave = true }
            MatchPhase.RESULT -> Result(state, canPlayAgain, onPlayAgain, onExit)
        }
    }

    if (confirmLeave) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { FText("Rời trận đấu?", size = 19.sp, weight = FontWeight.ExtraBold) },
            text = { FText("Nếu rời bây giờ, con sẽ xếp cuối và không nhận được điểm hay xu của trận này.", size = 15.sp) },
            confirmButton = { TextButton(onClick = { confirmLeave = false; onForfeit() }) { FText("Rời trận", weight = FontWeight.ExtraBold, color = FamilyTone.Red) } },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { FText("Thi tiếp", weight = FontWeight.ExtraBold, color = Purple) } }
        )
    }
}

/* ------------------------------- Tìm bạn cùng thi ------------------------------- */

@Composable
private fun Matching(state: MatchState) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        val all = state.found >= state.opponents.size
        FText(if (all) "Đủ người rồi! Chuẩn bị…" else "Đang tìm bạn cùng thi…", size = 22.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center)
        FText("10 câu khó · ai đúng nhiều và nhanh nhất sẽ thắng", size = 14.sp, color = FamilyTone.Muted, align = TextAlign.Center)
        Column(Modifier.widthIn(max = 420.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.me?.let { me ->
                PlayerSlot(true, { AvatarView(me, 44.dp) }, me.name, "Lớp ${me.grade}", true)
            }
            state.opponents.forEachIndexed { i, b ->
                val joined = i < state.found
                PlayerSlot(joined, { if (joined) AvatarView(b.look, 44.dp) else Box(Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFEEF0F3))) },
                    if (joined) b.name else "Đang tìm…", if (joined) "Lớp ${state.grade}" else "", false)
            }
        }
        if (!all) CircularProgressIndicator(color = Purple, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun PlayerSlot(joined: Boolean, avatar: @Composable () -> Unit, name: String, sub: String, me: Boolean) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (me) PurpleSoft else Color.White)
            .border(1.dp, if (joined) FamilyTone.Line else Color(0xFFEEF0F3), RoundedCornerShape(16.dp)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        avatar()
        Column(Modifier.weight(1f)) {
            FText(name, size = 16.sp, weight = FontWeight.ExtraBold, color = if (joined) FamilyTone.Ink else FamilyTone.Muted)
            if (sub.isNotEmpty()) FText(sub, size = 12.sp, color = FamilyTone.Muted)
        }
        if (joined) FText(if (me) "Con" else "Sẵn sàng", size = 12.sp, weight = FontWeight.ExtraBold, color = if (me) Purple else FamilyTone.Green)
    }
}

/* ------------------------------------ Làm bài ------------------------------------ */

@Composable
private fun Playing(state: MatchState, onAnswer: (String) -> Unit, onLeave: () -> Unit) {
    val q = state.questions.getOrNull(state.index) ?: return
    val step = q.problem.steps.first()
    // Câu mới hiện ra → khoá đáp án 0,5 giây để cú bấm vội không rơi vào câu mới
    var unlocked by remember(state.index) { mutableStateOf(false) }
    LaunchedEffect(state.index) { delay(500); unlocked = true }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().background(Color.White).padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onLeave), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, contentDescription = "Rời trận", tint = FamilyTone.Ink)
            }
            FText("Câu ${state.index + 1}/${state.questions.size}", size = 17.sp, weight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
            Icon(Icons.Rounded.Timer, contentDescription = null, tint = FamilyTone.Muted, modifier = Modifier.size(18.dp))
            FText(clock(state.elapsedMs), size = 16.sp, weight = FontWeight.ExtraBold)
        }
        // Đường đua: mỗi người một làn, tiến theo số câu đã làm
        Column(
            Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            state.me?.let { Lane(true, { AvatarView(it, 26.dp) }, it.name, state.answers.size, state.questions.size) }
            state.runs.forEach { r ->
                Lane(false, { AvatarView(r.bot.look, 26.dp) }, r.bot.name, state.botDone(r), state.questions.size)
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(20.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormattedMathStatement(statement = q.problem.statementVi, fontSize = 19.sp, color = FamilyTone.Ink)
                val diagram = remember(q.problem.problemId, q.problem.statementVi) { diagramFor(q.problem) }
                if (diagram != null) DiagramView(diagram, Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(FamilyTone.Paper).padding(vertical = 10.dp, horizontal = 4.dp))
            }
            val longest = step.options.maxOfOrNull { it.length } ?: 0
            val fs = when { longest <= 4 -> 20.sp; longest <= 10 -> 17.sp; longest <= 18 -> 15.sp; else -> 14.sp }
            step.options.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { opt ->
                        val fb = state.feedback
                        val isPicked = fb?.first == opt
                        val isRight = fb != null && opt == step.expectedAnswer
                        val bg = when { isPicked && fb!!.second -> Color(0xFFEAF4EE); isPicked -> Color(0xFFFBEDED); isRight -> Color(0xFFEAF4EE); else -> Color.White }
                        val bd = when { isPicked && fb!!.second -> FamilyTone.Green; isPicked -> FamilyTone.Red; isRight -> FamilyTone.Green; else -> FamilyTone.Line }
                        Box(
                            Modifier.weight(1f).heightIn(min = 64.dp).clip(RoundedCornerShape(14.dp)).background(bg)
                                .border(if (fb != null && (isPicked || isRight)) 2.dp else 1.dp, bd, RoundedCornerShape(14.dp))
                                .clickable(enabled = fb == null && unlocked, role = Role.Button) { onAnswer(opt) }.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) { FText(opt, size = fs, weight = FontWeight.Bold, align = TextAlign.Center) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Lane(me: Boolean, avatar: @Composable () -> Unit, name: String, done: Int, total: Int) {
    val frac by animateFloatAsState(if (total == 0) 0f else done.toFloat() / total, label = "lane")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        avatar()
        FText(name, size = 12.sp, weight = if (me) FontWeight.ExtraBold else FontWeight.SemiBold, color = if (me) Purple else FamilyTone.Ink, maxLines = 1, modifier = Modifier.width(76.dp))
        Box(Modifier.weight(1f).height(10.dp).clip(CircleShape).background(Color(0xFFEEF0F3))) {
            Box(Modifier.fillMaxWidth(frac).height(10.dp).clip(CircleShape).background(if (me) Purple else Color(0xFFB9B2D8)))
        }
        FText("$done/$total", size = 11.sp, weight = FontWeight.Bold, color = FamilyTone.Muted, modifier = Modifier.width(34.dp), align = TextAlign.End)
    }
}

/* ------------------------------------ Kết quả ------------------------------------ */

@Composable
private fun Result(state: MatchState, canPlayAgain: Boolean, onPlayAgain: () -> Unit, onExit: () -> Unit) {
    val place = state.place
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(if (state.forfeited) "🏳️" else listOf("🥇", "🥈", "🥉", "🎖️", "💪")[(place - 1).coerceIn(0, 4)], fontSize = 64.sp)
            FText(
                when {
                    state.forfeited -> "Con đã rời trận"
                    place == 1 -> "Vô địch trận này!"
                    place <= 3 -> "Về hạng $place – giỏi lắm!"
                    else -> "Về hạng $place – trận sau cố lên!"
                }, size = 24.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center
            )
            if (!state.forfeited) {
                FText("Đúng ${state.myCorrect}/${state.questions.size} câu · ${clock(state.myTimeMs)}", size = 15.sp, color = FamilyTone.Muted)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.clip(CircleShape).background(Color(0xFFFFF4D6)).padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) { CoinAmount(state.coins, size = 22.dp) }
                    Box(Modifier.clip(CircleShape).background(PurpleSoft).padding(horizontal = 14.dp, vertical = 8.dp)) {
                        FText("+${state.points} điểm giải", size = 17.sp, weight = FontWeight.ExtraBold, color = Purple)
                    }
                }
            }
            Column(
                Modifier.widthIn(max = 480.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(20.dp)).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                state.results.forEachIndexed { i, r ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (r.isMe) PurpleSoft else Color.Transparent).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FText("${i + 1}", size = 16.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted, modifier = Modifier.width(20.dp))
                        val b = r.bot
                        if (b != null) AvatarView(b.look, 36.dp) else state.me?.let { AvatarView(it, 36.dp) }
                        FText(r.name, size = 15.sp, weight = if (r.isMe) FontWeight.ExtraBold else FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                        FText(if (r.isMe && state.forfeited) "rời trận" else "${r.correct}/10", size = 14.sp, weight = FontWeight.ExtraBold)
                        FText(clock(r.timeMs), size = 13.sp, color = FamilyTone.Muted, modifier = Modifier.width(44.dp), align = TextAlign.End)
                    }
                }
            }
            FText("Xếp hạng: đúng nhiều hơn đứng trên; bằng nhau thì ai nhanh hơn.", size = 12.sp, color = FamilyTone.Muted, align = TextAlign.Center)
        }
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FButton("Bảng xếp hạng", modifier = Modifier.weight(1f), outlined = true, color = Purple, onClick = onExit)
            if (canPlayAgain) FButton("Thi tiếp", modifier = Modifier.weight(1f), color = Purple, onClick = onPlayAgain)
        }
    }
}
