package com.example.toanhoc.ui.tournament

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.EntryKind
import com.example.toanhoc.data.family.League
import com.example.toanhoc.data.family.LeagueState
import com.example.toanhoc.data.family.Standing
import com.example.toanhoc.ui.family.AvatarView
import com.example.toanhoc.ui.family.CoinAmount
import com.example.toanhoc.ui.family.CoinPill
import com.example.toanhoc.ui.family.FButton
import com.example.toanhoc.ui.family.FCard
import com.example.toanhoc.ui.family.FText
import com.example.toanhoc.ui.family.FamilyTone
import com.example.toanhoc.ui.family.SectionTitle
import com.example.toanhoc.ui.theme.NunitoFontFamily
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Tab "Giải đấu": hạng giải, bảng xếp hạng tuần (con + 4 bạn cùng bảng), nút vào trận.
 * [league] đã được "roll" sang tuần/ngày hiện tại.
 */
@Composable
fun TournamentScreen(
    child: ChildProfile,
    league: LeagueState,
    onPlay: () -> Unit,
    onAckWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    val now = LocalDateTime.now()
    val tier = League.TIERS[league.tier]
    val standings = League.standings(league, child.name, now)
    val myRank = standings.indexOfFirst { it.isMe } + 1
    val daysLeft = (DayOfWeek.SUNDAY.value - LocalDate.now().dayOfWeek.value) + 1
    val entry = League.entry(league, child.coins)

    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    FText("Giải đấu", size = 28.sp, weight = FontWeight.ExtraBold)
                    FText("Thi 10 câu khó với các bạn cùng bảng", size = 14.sp, color = FamilyTone.Muted)
                }
                CoinPill(child.coins)
            }

            // Kết quả tuần trước
            league.lastWeek?.takeIf { !it.seen }?.let { r ->
                val up = r.toTier > r.fromTier
                val down = r.toTier < r.fromTier
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(if (up) Color(0xFFEAF4EE) else if (down) Color(0xFFFBF3EA) else Color.White)
                        .border(1.dp, FamilyTone.Line, RoundedCornerShape(20.dp)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(if (up) "🎉" else if (down) "💪" else "⭐", fontSize = 34.sp)
                    FText(
                        when {
                            up -> "Lên hạng ${League.TIERS[r.toTier].name}!"
                            down -> "Tuần này mình lấy lại hạng nhé!"
                            else -> "Giữ vững hạng ${League.TIERS[r.toTier].name}"
                        }, size = 20.sp, weight = FontWeight.ExtraBold
                    )
                    FText("Tuần trước con về hạng ${r.rank} trong bảng.", size = 14.sp, color = FamilyTone.Muted)
                    FButton("Tuyệt!", small = true, onClick = onAckWeek)
                }
            }

            // Thang hạng
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                    .background(Brush.verticalGradient(listOf(Color(tier.soft), Color.White)))
                    .border(1.dp, FamilyTone.Line, RoundedCornerShape(22.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(tier.emoji, fontSize = 40.sp)
                    Column(Modifier.weight(1f)) {
                        FText("Hạng ${tier.name}", size = 22.sp, weight = FontWeight.ExtraBold, color = Color(tier.color))
                        FText("Tuần này · còn $daysLeft ngày", size = 13.sp, color = FamilyTone.Muted)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    League.TIERS.forEachIndexed { i, t ->
                        val reached = i <= league.tier
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(if (i == league.tier) 40.dp else 32.dp).clip(CircleShape)
                                    .background(if (reached) Color(t.soft) else Color(0xFFEEF0F3))
                                    .border(2.dp, if (i == league.tier) Color(t.color) else Color.Transparent, CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text(t.emoji, fontSize = if (i == league.tier) 20.sp else 15.sp, color = if (reached) Color.Unspecified else Color.Black.copy(alpha = 0.25f)) }
                            FText(t.name, size = 10.sp, weight = FontWeight.Bold, color = if (reached) FamilyTone.Ink else FamilyTone.Muted, maxLines = 1)
                        }
                    }
                }
            }

            // Bảng xếp hạng tuần
            FCard(padding = 12.dp) {
                SectionTitle("Bảng xếp hạng tuần", trailing = "Top 2 lên hạng")
                standings.forEachIndexed { i, s ->
                    if (i == 2) PromotionLine()
                    StandingRow(i + 1, s, child)
                }
                if (league.tier > 0 && league.matchesThisWeek < 2) {
                    FText("Thi ít nhất 2 trận trong tuần để chắc chắn giữ hạng ${tier.name}.", size = 12.sp, color = FamilyTone.Coin, weight = FontWeight.Bold)
                }
            }

            // Vào trận
            FCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFEDE9FE)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(30.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        FText("Trận đấu 10 câu", size = 17.sp, weight = FontWeight.ExtraBold)
                        FText(if (child.grade == 1) "Kiến thức lớp 1" else "Kiến thức lớp 1 – ${child.grade}", size = 13.sp, color = FamilyTone.Muted)
                    }
                    FText("${(League.MAX_PER_DAY - league.matchesToday).coerceAtLeast(0)}/${League.MAX_PER_DAY} lượt", size = 12.sp, weight = FontWeight.Bold, color = FamilyTone.Muted)
                }
                // Thưởng theo hạng
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    League.PAYOUT.forEachIndexed { i, c ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            FText(listOf("🥇", "🥈", "🥉", "4", "5")[i], size = if (i < 3) 18.sp else 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted)
                            CoinAmount(c, size = 15.dp)
                        }
                    }
                }
                when (entry) {
                    EntryKind.FREE -> PlayButton("Thi đấu · vé miễn phí hôm nay", null, true, onPlay)
                    EntryKind.PAID -> PlayButton("Thi đấu", League.FEE, true, onPlay)
                    EntryKind.NO_COINS -> {
                        PlayButton("Cần ${League.FEE} xu để thi", League.FEE, false) {}
                        FText("Học bài để nhận thêm xu, hoặc mai dùng vé miễn phí nhé!", size = 12.sp, color = FamilyTone.Muted)
                    }
                    EntryKind.DAY_LIMIT -> FText("Hôm nay con đã thi đủ ${League.MAX_PER_DAY} trận. Mai thi tiếp nhé! 🌙", size = 14.sp, weight = FontWeight.Bold, color = FamilyTone.Muted)
                }
            }

            if (league.played > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat("${league.played}", "trận đã thi", Modifier.weight(1f))
                    Stat("${league.wins}", "lần về nhất", Modifier.weight(1f))
                    Stat("${if (league.answered > 0) league.correct * 100 / league.answered else 0}%", "câu đúng", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PromotionLine() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFF86C9A4)))
        FText("▲ vùng lên hạng", size = 11.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Green)
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFF86C9A4)))
    }
}

@Composable
private fun StandingRow(rank: Int, s: Standing, child: ChildProfile) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (s.isMe) Color(0xFFEEF2FC) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FText(
            "$rank", size = 16.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center,
            color = if (rank <= 2) FamilyTone.Green else FamilyTone.Muted, modifier = Modifier.width(22.dp)
        )
        val b = s.bot
        if (b != null) AvatarView(b.look, 40.dp) else AvatarView(child, 40.dp)
        Column(Modifier.weight(1f)) {
            FText(s.name, size = 15.sp, weight = if (s.isMe) FontWeight.ExtraBold else FontWeight.Bold, maxLines = 1)
            FText("Lớp ${child.grade}", size = 11.sp, color = FamilyTone.Muted)
        }
        FText("${s.points}", size = 16.sp, weight = FontWeight.ExtraBold, color = if (s.isMe) FamilyTone.Primary else FamilyTone.Ink)
        FText("điểm", size = 11.sp, color = FamilyTone.Muted)
    }
}

@Composable
private fun PlayButton(text: String, fee: Int?, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Color(0xFF7C3AED) else FamilyTone.Line)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        FText(text, size = 17.sp, weight = FontWeight.ExtraBold, color = if (enabled) Color.White else FamilyTone.Muted)
        if (fee != null) {
            Box(Modifier.width(12.dp))
            Row(
                Modifier.clip(CircleShape).background(Color.White.copy(alpha = if (enabled) 0.18f else 0.6f)).padding(horizontal = 8.dp, vertical = 3.dp)
            ) { CoinAmount(fee, plus = false, size = 16.dp, onDark = enabled) }
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(14.dp)).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FText(value, size = 18.sp, weight = FontWeight.ExtraBold)
        FText(label, size = 12.sp, color = FamilyTone.Muted)
    }
}

internal fun Modifier.clickableRole(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick))
