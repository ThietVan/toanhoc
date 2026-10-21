package com.example.toanhoc.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.R
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.domain.challenge.Challenge
import com.example.toanhoc.domain.challenge.ChallengeRules
import com.example.toanhoc.domain.lecture.speakable
import com.example.toanhoc.ui.lecture.rememberLectureVoice
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private object TT {
    val Bg = Color(0xFFF4F6FB)
    val Ink = Color(0xFF111827)
    val Muted = Color(0xFF6B7280)
    val Line = Color(0xFFE2E6EE)
    val Blue = Color(0xFF2F55D4)
    val BlueSoft = Color(0xFFEAF0FE)
    val Navy = Color(0xFF1B2440)
}

/**
 * Màn con làm "Thử thách vượt bài".
 * Không báo đúng/sai từng câu (kết quả chỉ bố mẹ xem). Mỗi câu phải chọn rồi bấm "Chọn đáp án này" –
 * tránh bấm nhầm; 0,6 giây đầu mỗi câu khóa bấm để con đọc đề.
 */
@Composable
fun ChallengeTestScreen(
    child: ChildProfile,
    challenge: Challenge,
    onAnswer: (unit: Int, question: Int, picked: String, ms: Long) -> Unit,
    onFinish: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var started by rememberSaveable(challenge.id) { mutableStateOf(false) }
    var askPause by remember { mutableStateOf(false) }
    val cursor = challenge.cursor()
    val voice = rememberLectureVoice()

    // Làm hết câu → báo xong (một lần)
    LaunchedEffect(cursor == null, challenge.finished) {
        if (cursor == null && !challenge.finished) onFinish()
    }
    BackHandler { if (cursor == null || !started) onExit() else askPause = true }

    Box(modifier.fillMaxSize().background(TT.Bg)) {
        when {
            cursor == null -> DoneView(child, onExit)
            !started -> IntroView(child, challenge, onStart = { started = true }, onExit = onExit)
            else -> {
                val (ui, qi) = cursor
                val unit = challenge.units[ui]
                val q = unit.questions[qi]
                val number = challenge.answeredCount + 1
                Column(Modifier.fillMaxSize()) {
                    // Thanh trên
                    Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button) { voice.stop(); askPause = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.Pause, contentDescription = "Tạm dừng", tint = TT.Ink) }
                        Column(Modifier.weight(1f)) {
                            FText("THỬ THÁCH VƯỢT BÀI", size = 11.sp, weight = FontWeight.ExtraBold, color = TT.Muted)
                            FText("Câu $number / ${challenge.totalQuestions}", size = 17.sp, weight = FontWeight.ExtraBold, color = TT.Ink)
                        }
                        Row(
                            Modifier.clip(CircleShape).background(Color.White).border(1.dp, TT.Line, CircleShape).padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Bolt, null, tint = RoadTone.Bolt, modifier = Modifier.size(16.dp))
                            FText("Phần ${ui + 1}/${challenge.units.size}", size = 12.sp, weight = FontWeight.ExtraBold, color = TT.Ink)
                        }
                    }
                    val frac by animateFloatAsState((number - 1f) / challenge.totalQuestions.coerceAtLeast(1), tween(400), label = "p")
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth().height(6.dp).clip(CircleShape).background(TT.Line)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(CircleShape).background(TT.Blue))
                    }

                    AnimatedContent(
                        targetState = Triple(ui, qi, q.statement),
                        transitionSpec = { (slideInHorizontally { it / 3 } + fadeIn(tween(250))) togetherWith (slideOutHorizontally { -it / 3 } + fadeOut(tween(150))) },
                        label = "q", modifier = Modifier.weight(1f)
                    ) { (u, i, _) ->
                        val qq = challenge.units[u].questions[i]
                        QuestionView(
                            unitTitle = challenge.units[u].title,
                            statement = qq.statement,
                            options = qq.options,
                            autoRead = child.grade <= 2,
                            speak = { text -> voice.speak(speakable(text)) },
                            stopVoice = { voice.stop() },
                            onConfirm = { picked, ms -> voice.stop(); onAnswer(u, i, picked, ms) }
                        )
                    }
                }
            }
        }

        if (askPause) PauseDialog(
            onResume = { askPause = false },
            onPause = { askPause = false; voice.stop(); onExit() }
        )
    }
}

@Composable
private fun QuestionView(
    unitTitle: String,
    statement: String,
    options: List<String>,
    autoRead: Boolean,
    speak: suspend (String) -> Boolean,
    stopVoice: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var picked by remember { mutableStateOf<String?>(null) }
    var unlocked by remember { mutableStateOf(false) }
    val shownAt = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        shownAt.longValue = System.currentTimeMillis()
        delay(600); unlocked = true
        if (autoRead) speak(statement)
    }
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
                .align(Alignment.CenterHorizontally).widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Đề bài
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).border(1.dp, TT.Line, RoundedCornerShape(22.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FText(unitTitle, size = 12.sp, weight = FontWeight.ExtraBold, color = TT.Blue, modifier = Modifier.weight(1f), maxLines = 1)
                    Row(
                        Modifier.clip(CircleShape).background(TT.BlueSoft).clickable(role = Role.Button) { scope.launch { stopVoice(); speak(statement) } }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, null, tint = TT.Blue, modifier = Modifier.size(16.dp))
                        FText("Đọc đề", size = 12.sp, weight = FontWeight.ExtraBold, color = TT.Blue)
                    }
                }
                FText(statement, size = if (statement.length > 120) 19.sp else 22.sp, weight = FontWeight.ExtraBold, color = TT.Ink, lineHeight = if (statement.length > 120) 27.sp else 31.sp)
            }
            FText("Chọn một đáp án", size = 13.sp, weight = FontWeight.Bold, color = TT.Muted)
            options.forEachIndexed { i, opt ->
                OptionRow(('A' + i).toString(), opt, picked == opt, enabled = unlocked) { picked = opt }
            }
            Spacer(Modifier.height(4.dp))
        }
        // Nút xác nhận
        Box(Modifier.fillMaxWidth().background(TT.Bg).navigationBarsPadding().padding(16.dp), contentAlignment = Alignment.Center) {
            val ready = picked != null && unlocked
            val bg by animateColorAsState(if (ready) TT.Blue else TT.Line, tween(200), label = "b")
            Row(
                Modifier.widthIn(max = 640.dp).fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp)).background(bg)
                    .clickable(enabled = ready, role = Role.Button) {
                        val p = picked ?: return@clickable
                        onConfirm(p, System.currentTimeMillis() - shownAt.longValue)
                    },
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                FText(if (picked == null) "Chọn một đáp án ở trên" else "Chọn đáp án này", size = 17.sp, weight = FontWeight.ExtraBold, color = if (ready) Color.White else TT.Muted)
                if (ready) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun OptionRow(letter: String, text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val border by animateColorAsState(if (selected) TT.Blue else TT.Line, tween(150), label = "ob")
    val bg by animateColorAsState(if (selected) TT.BlueSoft else Color.White, tween(150), label = "obg")
    Row(
        Modifier.fillMaxWidth().heightIn(min = 60.dp).clip(RoundedCornerShape(16.dp)).background(bg)
            .border(if (selected) 2.dp else 1.dp, border, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(32.dp).clip(CircleShape).background(if (selected) TT.Blue else TT.Bg),
            contentAlignment = Alignment.Center
        ) { FText(letter, size = 15.sp, weight = FontWeight.ExtraBold, color = if (selected) Color.White else TT.Muted) }
        FText(text, size = 19.sp, weight = FontWeight.ExtraBold, color = TT.Ink, lineHeight = 25.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun IntroView(child: ChildProfile, ch: Challenge, onStart: () -> Unit, onExit: () -> Unit) {
    val resumed = ch.answeredCount > 0
    val minutes = (((ch.totalQuestions - ch.answeredCount) * ChallengeRules.SECONDS_PER_QUESTION) + 59) / 60
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 6.dp)) {
            Box(Modifier.size(48.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onExit), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = TT.Ink)
            }
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
                .align(Alignment.CenterHorizontally).widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
        ) {
            KidPair(170)
            Row(
                Modifier.clip(CircleShape).background(RoadTone.BoltSoft).padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.Bolt, null, tint = RoadTone.Bolt, modifier = Modifier.size(18.dp))
                FText("Thử thách từ bố mẹ", size = 13.sp, weight = FontWeight.ExtraBold, color = RoadTone.Bolt)
            }
            FText(if (resumed) "Làm tiếp thử thách nhé, ${child.name}!" else "${child.name} ơi, cùng vượt thử thách nào!", size = 25.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, lineHeight = 32.sp)
            FText(
                "Làm tốt thì con được bỏ qua những bài đã biết để học bài mới.",
                size = 15.sp, color = TT.Muted, align = TextAlign.Center, lineHeight = 22.sp
            )
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, TT.Line, RoundedCornerShape(18.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoLine("${ch.units.size} phần", "${ch.totalQuestions} câu · khoảng $minutes phút")
                InfoLine("Không tính giờ", "Đọc thật kĩ đề rồi mới chọn nhé")
                InfoLine("Có thể dừng", "Câu đã làm được lưu, lần sau làm tiếp")
            }
        }
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), contentAlignment = Alignment.Center) {
            Row(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp)).background(TT.Blue)
                    .clickable(role = Role.Button, onClick = onStart),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                FText(if (resumed) "Làm tiếp (câu ${ch.answeredCount + 1})" else "Bắt đầu", size = 18.sp, weight = FontWeight.ExtraBold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = Color.White)
            }
        }
    }
}

@Composable
private fun InfoLine(title: String, desc: String) = Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    Box(Modifier.size(8.dp).clip(CircleShape).background(TT.Blue))
    Column {
        FText(title, size = 15.sp, weight = FontWeight.ExtraBold, color = TT.Ink)
        FText(desc, size = 13.sp, color = TT.Muted)
    }
}

@Composable
private fun DoneView(child: ChildProfile, onExit: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
                .align(Alignment.CenterHorizontally).widthIn(max = 520.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)
        ) {
            Box(Modifier.size(72.dp).clip(CircleShape).background(RoadTone.BoltSoft), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = RoadTone.Bolt, modifier = Modifier.size(40.dp))
            }
            FText("Con đã hoàn thành thử thách!", size = 26.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, lineHeight = 33.sp)
            FText(
                "Giỏi lắm, ${child.name}! Bố mẹ sẽ xem kết quả và chọn bài tiếp theo cho con.",
                size = 15.sp, color = TT.Muted, align = TextAlign.Center, lineHeight = 22.sp
            )
            KidPair(190)
        }
        Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.widthIn(max = 520.dp).fillMaxWidth().height(58.dp).clip(RoundedCornerShape(18.dp)).background(TT.Blue)
                    .clickable(role = Role.Button, onClick = onExit),
                contentAlignment = Alignment.Center
            ) { FText("Về trang học", size = 18.sp, weight = FontWeight.ExtraBold, color = Color.White) }
        }
    }
}

@Composable
private fun KidPair(height: Int) = Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
    Image(painterResource(R.drawable.be_trai), null, Modifier.height(height.dp).aspectRatio(400f / 958f), contentScale = ContentScale.Fit)
    Image(painterResource(R.drawable.be_gai), null, Modifier.height((height * 0.96f).dp).aspectRatio(446f / 882f), contentScale = ContentScale.Fit)
}

@Composable
private fun PauseDialog(onResume: () -> Unit, onPause: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(TT.Navy.copy(alpha = 0.55f)).clickable(role = Role.Button, onClick = onResume),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(24.dp).widthIn(max = 420.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                .clickable(enabled = false) {}.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FText("Tạm dừng thử thách?", size = 21.sp, weight = FontWeight.ExtraBold)
            FText("Các câu con đã làm được lưu lại. Lần sau con làm tiếp từ câu đang dở nhé.", size = 15.sp, color = TT.Muted, lineHeight = 22.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).border(1.dp, TT.Line, RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClick = onPause),
                    contentAlignment = Alignment.Center
                ) { FText("Tạm dừng", size = 16.sp, weight = FontWeight.ExtraBold, color = TT.Ink) }
                Box(
                    Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp)).background(TT.Blue)
                        .clickable(role = Role.Button, onClick = onResume),
                    contentAlignment = Alignment.Center
                ) { FText("Làm tiếp", size = 16.sp, weight = FontWeight.ExtraBold, color = Color.White) }
            }
        }
    }
}

/** Thẻ trên trang học của con khi bố mẹ đã giao thử thách. */
@Composable
fun ChallengeBanner(challenge: Challenge, onStart: () -> Unit, modifier: Modifier = Modifier) {
    val left = challenge.totalQuestions - challenge.answeredCount
    val minutes = ((left * ChallengeRules.SECONDS_PER_QUESTION) + 59) / 60
    Row(
        modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(TT.Navy).clickable(role = Role.Button, onClick = onStart).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF6C453)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Bolt, null, tint = TT.Navy, modifier = Modifier.size(30.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            FText("Thử thách từ bố mẹ", size = 17.sp, weight = FontWeight.ExtraBold, color = Color.White)
            FText(
                if (challenge.answeredCount > 0) "Còn $left câu · khoảng $minutes phút" else "${challenge.totalQuestions} câu · khoảng $minutes phút",
                size = 13.sp, color = Color.White.copy(alpha = 0.75f)
            )
        }
        Box(Modifier.clip(CircleShape).background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp)) {
            FText(if (challenge.answeredCount > 0) "Làm tiếp" else "Bắt đầu", size = 14.sp, weight = FontWeight.ExtraBold, color = TT.Navy)
        }
    }
}
