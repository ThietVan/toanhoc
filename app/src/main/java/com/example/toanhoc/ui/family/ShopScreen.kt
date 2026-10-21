package com.example.toanhoc.ui.family

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.AvatarShop
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.FamilyData
import com.example.toanhoc.data.family.Reward
import com.example.toanhoc.data.family.ShopItem
import com.example.toanhoc.data.family.ShopKind

/**
 * Tab "Cửa hàng": dùng xu để
 *  - Thay đồ cho nhân vật (tóc, trang phục, phông nền, thú cưng) – mặc thử trước khi mua;
 *  - Đổi quà thật bố mẹ đã cài (gửi yêu cầu, chờ bố mẹ trao).
 */
@Composable
fun ShopScreen(
    data: FamilyData,
    child: ChildProfile,
    onBuy: (ShopItem) -> Unit,
    onEquip: (ShopItem) -> Unit,
    onRequestReward: (Reward) -> Unit,
    onSetGender: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var section by rememberSaveable { mutableStateOf(0) }
    var tab by rememberSaveable { mutableStateOf(0) }
    var trying by remember { mutableStateOf<ShopItem?>(null) }
    var redeeming by remember { mutableStateOf<Reward?>(null) }
    val affordableRewards = data.rewardsFor(child.id).count { child.coins >= it.cost }

    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Đầu trang: tiêu đề + số xu
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    FText("Cửa hàng", size = 28.sp, weight = FontWeight.ExtraBold)
                    FText("Dùng xu học được để trang trí và đổi quà", size = 14.sp, color = FamilyTone.Muted)
                }
                CoinPill(child.coins, big = true)
            }

            // Hai mục
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(16.dp)).padding(4.dp)) {
                listOf("🎨  Trang trí", "🎁  Quà của bố mẹ").forEachIndexed { i, t ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(if (section == i) FamilyTone.Ink else Color.Transparent)
                            .clickable(role = Role.Tab) { section = i }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FText(t, size = 15.sp, weight = FontWeight.ExtraBold, color = if (section == i) Color.White else FamilyTone.Ink, maxLines = 1)
                            if (i == 1 && affordableRewards > 0) {
                                Box(Modifier.size(20.dp).clip(CircleShape).background(FamilyTone.Red), contentAlignment = Alignment.Center) {
                                    FText("$affordableRewards", size = 11.sp, weight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            if (section == 0) {
                // Xem trước: cảnh đầy đủ, mặc thử món đang chọn
                Box(Modifier.fillMaxWidth().aspectRatio(1.1f).clip(RoundedCornerShape(24.dp)).border(1.dp, FamilyTone.Line, RoundedCornerShape(24.dp))) {
                    CharacterScene(trying?.let { child.look.with(it) } ?: child.look, Modifier.fillMaxSize())
                    Box(Modifier.padding(12.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        FText(if (trying != null) "Đang mặc thử" else child.name, size = 13.sp, weight = FontWeight.ExtraBold)
                    }
                }
                trying?.let { item ->
                    val enough = child.coins >= item.price
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFFF4D6)).border(1.dp, Color(0xFFF5D07A), RoundedCornerShape(16.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            FText(item.name, size = 16.sp, weight = FontWeight.ExtraBold)
                            FText(
                                if (enough) "Con có ${child.coins} xu, đủ để mua" else "Cần thêm ${item.price - child.coins} xu nữa – học tiếp nhé!",
                                size = 12.sp, weight = FontWeight.Bold, color = if (enough) FamilyTone.Green else FamilyTone.Red
                            )
                        }
                        FButton("Bỏ", small = true, outlined = true, color = FamilyTone.Muted) { trying = null }
                        Row(
                            Modifier.heightIn(min = 40.dp).clip(RoundedCornerShape(10.dp)).background(if (enough) Color(0xFFE08A00) else FamilyTone.Line)
                                .clickable(enabled = enough, role = Role.Button) { onBuy(item); trying = null }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FText("Mua", size = 15.sp, weight = FontWeight.ExtraBold, color = Color.White)
                            CoinAmount(item.price, plus = false, size = 16.dp, onDark = true)
                        }
                    }
                }
                run {
                    FText("Con là", size = 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("trai" to "Bé trai", "gai" to "Bé gái").forEach { (g, label) ->
                            val sel = child.gender == g
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(Color.White)
                                    .border(2.dp, if (sel) FamilyTone.Primary else FamilyTone.Line, RoundedCornerShape(16.dp))
                                    .clickable(role = Role.RadioButton) { trying = null; onSetGender(g) }.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CharacterPortrait(child.look.copy(gender = g, hair = if (child.hair == AvatarShop.defaultHair(child.gender)) AvatarShop.defaultHair(g) else child.hair), 56.dp)
                                FText(label, size = 14.sp, weight = FontWeight.ExtraBold, color = if (sel) FamilyTone.Primary else FamilyTone.Ink)
                            }
                        }
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarShop.TABS.forEachIndexed { i, (name, _) -> Chip(name, i == tab) { tab = i; trying = null } }
                }
                AvatarShop.TABS[tab].second.forEach { kind ->
                    if (AvatarShop.TABS[tab].second.size > 1) FText(kind.label, size = 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Muted)
                    AvatarShop.of(kind).chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { item ->
                                ShopTile(child, item, trying == item, Modifier.weight(1f)) {
                                    if (AvatarShop.isOwned(child, item)) { trying = null; onEquip(item) } else trying = item
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            } else {
                RewardsSection(data, child) { redeeming = it }
            }
        }
    }

    redeeming?.let { r ->
        AlertDialog(
            onDismissRequest = { redeeming = null },
            title = { FText("Đổi ${r.title}?", size = 19.sp, weight = FontWeight.ExtraBold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(r.emoji, fontSize = 52.sp)
                    CoinAmount(r.cost, plus = false, size = 24.dp)
                    FText("Bố mẹ sẽ nhận được yêu cầu và trao quà cho con.", size = 15.sp, align = TextAlign.Center)
                }
            },
            confirmButton = { TextButton(onClick = { onRequestReward(r); redeeming = null }) { FText("Đổi quà", weight = FontWeight.ExtraBold, color = FamilyTone.Coin) } },
            dismissButton = { TextButton(onClick = { redeeming = null }) { FText("Để sau") } }
        )
    }
}

@Composable
private fun RewardsSection(data: FamilyData, child: ChildProfile, onRedeem: (Reward) -> Unit) {
    val rewards = data.rewardsFor(child.id).sortedBy { it.cost }
    val waiting = data.requests.filter { it.childId == child.id && !it.delivered }
    val received = data.requests.count { it.childId == child.id && it.delivered }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        waiting.forEach { r ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FamilyTone.GreenSoft).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(r.emoji, fontSize = 30.sp)
                Column(Modifier.weight(1f)) {
                    FText(r.title, size = 16.sp, weight = FontWeight.ExtraBold)
                    FText("Đang chờ bố mẹ trao quà", size = 13.sp, color = FamilyTone.Green, weight = FontWeight.Bold)
                }
            }
        }
        if (rewards.isEmpty()) {
            FCard {
                FText("Chưa có quà nào", size = 17.sp, weight = FontWeight.ExtraBold)
                FText("Nhờ bố mẹ vào khu phụ huynh để thêm quà. Con cứ tích xu trước nhé!", size = 14.sp, color = FamilyTone.Muted, lineHeight = 20.sp)
            }
        }
        rewards.forEach { r ->
            val enough = child.coins >= r.cost
            FCard(padding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(FamilyTone.CoinSoft), contentAlignment = Alignment.Center) {
                        Text(r.emoji, fontSize = 32.sp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FText(r.title, size = 16.sp, weight = FontWeight.ExtraBold)
                        CoinAmount(r.cost, plus = false, size = 18.dp)
                    }
                    if (enough) FButton("Đổi", small = true, color = FamilyTone.Coin) { onRedeem(r) }
                    else Icon(Icons.Rounded.Lock, contentDescription = "Chưa đủ xu", tint = FamilyTone.Line)
                }
                ProgressBar(child.coins.toFloat() / r.cost, if (enough) FamilyTone.Green else Color(0xFFFFC21A))
                FText(
                    if (enough) "Đủ xu rồi! Bấm Đổi để gửi cho bố mẹ." else "Còn ${r.cost - child.coins} xu nữa",
                    size = 12.sp, weight = FontWeight.Bold, color = if (enough) FamilyTone.Green else FamilyTone.Muted
                )
            }
        }
        if (received > 0) FText("Con đã nhận $received món quà 🎉", size = 13.sp, color = FamilyTone.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ShopTile(child: ChildProfile, item: ShopItem, isTrying: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val owned = AvatarShop.isOwned(child, item)
    val equipped = AvatarShop.isEquipped(child, item)
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(Color.White)
            .border(2.dp, when { isTrying -> Color(0xFFE08A00); equipped -> FamilyTone.Primary; else -> FamilyTone.Line }, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ItemPreview(child, item)
        FText(item.name, size = 13.sp, weight = FontWeight.Bold, maxLines = 1, align = TextAlign.Center)
        when {
            equipped -> FText("Đang dùng", size = 12.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
            owned -> FText("Dùng", size = 12.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Ink)
            else -> CoinAmount(item.price, plus = false, size = 15.dp, muted = child.coins < item.price)
        }
    }
}

/** Hình xem nhanh của món đồ, vẽ trên chính nhân vật của con. */
@Composable
private fun ItemPreview(child: ChildProfile, item: ShopItem) {
    val box = Modifier.size(60.dp)
    when (item.kind) {
        ShopKind.SKIN, ShopKind.HAIR_COLOR -> Box(box.padding(8.dp).clip(CircleShape).background(Color(item.color)).border(1.dp, FamilyTone.Line, CircleShape))
        ShopKind.HAIR -> CharacterPortrait(child.look.copy(hair = item.id), 60.dp)
        ShopKind.OUTFIT -> CharacterFigure(child.look.copy(outfit = item.id), box)
        ShopKind.SCENE -> CharacterScene(child.look.copy(scene = item.id, pet = "none"), box.clip(RoundedCornerShape(12.dp)), animate = false)
        ShopKind.ACCESSORY -> if (item.id == "huychuong") CharacterFigure(child.look.copy(accessory = item.id), box)
            else CharacterPortrait(child.look.copy(accessory = item.id), 60.dp)
        ShopKind.PET -> if (item.id == "none") Box(box.padding(10.dp).clip(CircleShape).background(FamilyTone.Paper).border(1.dp, FamilyTone.Line, CircleShape))
            else PetIcon(item.id, box)
    }
}
