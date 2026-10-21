package com.example.toanhoc.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SwitchAccount
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.BadgeDef
import com.example.toanhoc.data.family.BadgeTier
import com.example.toanhoc.data.family.Badges
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.FamilyData

/**
 * Tab "Cá nhân" của con = phòng trưng bày thành tích:
 * avatar + xu + chuỗi ngày, huy hiệu sẵn sàng nhận (bấm Nhận lấy xu),
 * bộ sưu tập 8 huy hiệu × 3 cấp Đồng – Bạc – Vàng, xu gần đây.
 */
@Composable
fun ChildProfileScreen(
    data: FamilyData,
    child: ChildProfile,
    onClaimBadge: (BadgeDef, Int) -> Unit,
    onSwitchProfile: () -> Unit,
    onOpenShop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var detail by remember { mutableStateOf<BadgeDef?>(null) }
    val claimable = Badges.claimable(child, data)
    val earned = Badges.earnedCount(child, data)

    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Phòng của bé: nhân vật đứng trong phông, thú cưng bên cạnh
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White)
                    .border(1.dp, FamilyTone.Line, RoundedCornerShape(24.dp)),
            ) {
                Box(Modifier.fillMaxWidth().aspectRatio(1.15f)) {
                    CharacterScene(child.look, Modifier.fillMaxSize())
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CoinPill(child.coins)
                        Spacer(Modifier.weight(1f))
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.92f))
                                .clickable(role = Role.Button, onClick = onSwitchProfile),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.SwitchAccount, contentDescription = "Đổi người học", tint = FamilyTone.Ink) }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        FText(child.name, size = 24.sp, weight = FontWeight.ExtraBold, maxLines = 1)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FText("Lớp ${child.grade}", size = 14.sp, weight = FontWeight.SemiBold, color = FamilyTone.Muted)
                            Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFFB020), modifier = Modifier.size(16.dp))
                            FText("${data.streak(child.id)} ngày liên tiếp", size = 14.sp, weight = FontWeight.SemiBold, color = FamilyTone.Muted)
                        }
                    }
                    FButton("Thay đồ", small = true, color = FamilyTone.Coin, onClick = onOpenShop)
                }
                Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("🏅", "$earned/${Badges.totalTiers}", "huy hiệu", Modifier.weight(1f))
                    MiniStat("📘", "${child.counter(Badges.Keys.LESSONS)}", "bài học trọn", Modifier.weight(1f))
                    MiniStat("⏱️", "${(data.studySeconds[child.id]?.values?.sum() ?: 0) / 60}", "phút đã học", Modifier.weight(1f))
                }
            }

            // 2. Sẵn sàng nhận
            if (claimable.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionTitle("Con có quà chờ nhận!", trailing = "${claimable.size}")
                    claimable.forEach { (b, t) ->
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Brush.horizontalGradient(listOf(Color(0xFFFFF4D6), Color.White)))
                                .border(1.5.dp, Color(0xFFF5D07A), RoundedCornerShape(18.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Medal(b.emoji, BadgeTier.entries[t], 56.dp, locked = false)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                FText("${b.name} · ${BadgeTier.entries[t].label}", size = 16.sp, weight = FontWeight.ExtraBold)
                                FText(b.goal(b.targets[t]), size = 13.sp, color = FamilyTone.Muted, maxLines = 2)
                            }
                            ClaimButton(b.coins[t]) { onClaimBadge(b, t) }
                        }
                    }
                }
            }

            // 3. Bộ sưu tập
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Tủ huy hiệu", trailing = "Chạm để xem")
                Badges.all.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        row.forEach { b -> BadgeCard(b, child, data, Modifier.weight(1f)) { detail = b } }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // 4. Xu gần đây
            val log = data.coinLog.filter { it.childId == child.id }.takeLast(6).reversed()
            if (log.isNotEmpty()) {
                FCard {
                    SectionTitle("Xu gần đây")
                    log.forEach { e ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FText(e.reason, size = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                            if (e.amount > 0) CoinAmount(e.amount, size = 16.dp)
                            else FText("${e.amount}", size = 14.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted)
                        }
                    }
                }
            }
        }
    }

    detail?.let { b ->
        val v = b.metric(child, data)
        AlertDialog(
            onDismissRequest = { detail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val r = b.reached(v)
                    Medal(b.emoji, BadgeTier.entries[(r - 1).coerceAtLeast(0)], 52.dp, locked = r == 0)
                    FText(b.name, size = 20.sp, weight = FontWeight.ExtraBold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    b.targets.forEachIndexed { t, target ->
                        val tier = BadgeTier.entries[t]
                        val reached = v >= target
                        val claimed = b.key(t) in child.claimed
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (reached) Color(tier.fill) else FamilyTone.Paper).padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Medal(b.emoji, tier, 36.dp, locked = !reached)
                            Column(Modifier.weight(1f)) {
                                FText(tier.label, size = 14.sp, weight = FontWeight.ExtraBold)
                                FText(b.goal(target), size = 12.sp, color = FamilyTone.Muted)
                                if (!reached) FText("${v.coerceAtMost(target)}/$target ${b.unit}", size = 12.sp, weight = FontWeight.Bold, color = FamilyTone.Primary)
                            }
                            when {
                                claimed -> Icon(Icons.Rounded.Check, contentDescription = "Đã nhận", tint = FamilyTone.Green)
                                reached -> ClaimButton(b.coins[t]) { onClaimBadge(b, t) }
                                else -> CoinAmount(b.coins[t], size = 16.dp, muted = true)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { detail = null }) { FText("Đóng", weight = FontWeight.ExtraBold, color = FamilyTone.Primary) } }
        )
    }
}

@Composable
private fun MiniStat(emoji: String, value: String, label: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.85f)).border(1.dp, FamilyTone.Line, RoundedCornerShape(14.dp)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        FText(value, size = 17.sp, weight = FontWeight.ExtraBold, maxLines = 1)
        FText(label, size = 11.sp, color = FamilyTone.Muted, maxLines = 1)
    }
}

/** Nút "Nhận" kèm đồng xu và số xu thưởng. */
@Composable
private fun ClaimButton(coins: Int, onClick: () -> Unit) {
    Row(
        Modifier.clip(CircleShape).background(Color(0xFFE08A00)).clickable(role = Role.Button, onClick = onClick)
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CoinIcon(24.dp)
        Column {
            FText("Nhận", size = 11.sp, weight = FontWeight.Bold, color = Color.White.copy(alpha = 0.9f))
            FText("+$coins", size = 15.sp, weight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

/** Huy chương tròn: viền theo cấp (Đồng/Bạc/Vàng), khóa = xám mờ. */
@Composable
internal fun Medal(emoji: String, tier: BadgeTier, size: Dp, locked: Boolean) {
    Box(
        Modifier.size(size).clip(CircleShape)
            .background(if (locked) Color(0xFFEEF0F3) else Color(tier.fill))
            .border(size * 0.07f, if (locked) Color(0xFFD5D9E0) else Color(tier.ring), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.45f).sp, modifier = Modifier.alpha(if (locked) 0.3f else 1f))
        if (locked) Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(size * 0.3f).align(Alignment.BottomEnd))
    }
}

@Composable
private fun BadgeCard(b: BadgeDef, child: ChildProfile, data: FamilyData, modifier: Modifier, onClick: () -> Unit) {
    val v = b.metric(child, data)
    val reached = b.reached(v)
    val next = b.targets.getOrNull(reached)
    val tier = BadgeTier.entries[(reached - 1).coerceAtLeast(0)]
    Column(
        modifier.clip(RoundedCornerShape(18.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Medal(b.emoji, tier, 64.dp, locked = reached == 0)
        FText(b.name, size = 15.sp, weight = FontWeight.ExtraBold, maxLines = 1, align = TextAlign.Center)
        // 3 chấm cấp
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            BadgeTier.entries.forEachIndexed { i, t ->
                Box(Modifier.size(width = 18.dp, height = 6.dp).clip(CircleShape).background(if (i < reached) Color(t.ring) else Color(0xFFE5E7EB)))
            }
        }
        if (next != null) {
            ProgressBar(v.toFloat() / next, Color(0xFFFFC21A))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FText("${v.coerceAtMost(next)}/$next ${b.unit}", size = 12.sp, weight = FontWeight.Bold, color = FamilyTone.Muted, modifier = Modifier.weight(1f), maxLines = 1)
                CoinAmount(b.coins[reached], size = 14.dp)
            }
        } else {
            Spacer(Modifier.height(2.dp))
            FText("Đã đạt cấp Vàng!", size = 12.sp, weight = FontWeight.ExtraBold, color = Color(BadgeTier.GOLD.ring))
            Spacer(Modifier.width(1.dp))
        }
    }
}

/** Thẻ nhắc nghỉ khi con đã học đủ thời gian bố mẹ đặt trong ngày. */
@Composable
fun RestTimeScreen(child: ChildProfile, limitMinutes: Int, onAskParent: () -> Unit, onSwitch: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(FamilyTone.Paper).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 420.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            AvatarView(child, 110.dp)
            Text("🌙", fontSize = 40.sp)
            FText("Hôm nay con học đủ rồi!", size = 26.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center)
            FText(
                "Con đã học $limitMinutes phút theo giờ bố mẹ đặt. Nghỉ ngơi, vận động một chút rồi mai mình học tiếp nhé.",
                size = 16.sp, color = FamilyTone.Muted, align = TextAlign.Center, lineHeight = 23.sp
            )
            Gap(4.dp)
            FButton("Bố mẹ cho học thêm 15 phút", modifier = Modifier.fillMaxWidth(), outlined = true, onClick = onAskParent)
            FButton("Đổi người học", modifier = Modifier.fillMaxWidth(), outlined = true, color = FamilyTone.Muted, onClick = onSwitch)
        }
    }
}
