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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.FamilyData

/**
 * Màn mở app: "Ai đang học hôm nay?" – chạm avatar để vào học,
 * bên dưới là lối vào khu phụ huynh (có mã PIN).
 * Lần đầu (chưa có hồ sơ): màn chào mừng mời bố mẹ thiết lập.
 */
@Composable
fun ProfilePickerScreen(
    data: FamilyData,
    onPickChild: (String) -> Unit,
    onAddChild: () -> Unit,
    onParent: () -> Unit,
    onStartSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize().background(FamilyTone.Paper)) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 520.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (data.children.isEmpty()) Welcome(onStartSetup)
            else {
                Gap(12.dp)
                FText("Ai đang học hôm nay?", size = 28.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                FText("Chạm vào ảnh của con để bắt đầu", size = 15.sp, color = FamilyTone.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Gap(4.dp)
                val tiles: List<ChildProfile?> = data.children + listOf(null)
                tiles.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        row.forEach { child ->
                            if (child != null) ChildTile(child, Modifier.weight(1f)) { onPickChild(child.id) }
                            else AddTile(Modifier.weight(1f), onAddChild)
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                Gap(8.dp)
                val pending = data.requests.count { !it.delivered }
                ParentEntry(pending, onParent)
            }
        }
    }
}

@Composable
private fun ChildTile(child: ChildProfile, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(22.dp)).background(Color.White).border(1.dp, FamilyTone.Line, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(vertical = 20.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AvatarView(child, 96.dp)
        FText(child.name, size = 19.sp, weight = FontWeight.ExtraBold, maxLines = 1, align = TextAlign.Center)
        FText("Lớp ${child.grade}", size = 14.sp, weight = FontWeight.SemiBold, color = FamilyTone.Muted)
        CoinPill(child.coins)
    }
}

@Composable
private fun AddTile(modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.heightIn(min = 220.dp).clip(RoundedCornerShape(22.dp)).border(2.dp, FamilyTone.Line, RoundedCornerShape(22.dp))
            .clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
    ) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(FamilyTone.PrimarySoft), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = FamilyTone.Primary, modifier = Modifier.size(32.dp))
        }
        FText("Thêm con", size = 16.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
        FText("Cần mã phụ huynh", size = 12.sp, color = FamilyTone.Muted)
    }
}

@Composable
private fun ParentEntry(pendingRequests: Int, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(FamilyTone.Parent)
            .clickable(role = Role.Button, onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color.White)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            FText("Dành cho phụ huynh", size = 17.sp, weight = FontWeight.ExtraBold, color = Color.White)
            FText(
                if (pendingRequests > 0) "$pendingRequests yêu cầu đổi quà đang chờ" else "Báo cáo · Phần thưởng · Cài đặt",
                size = 13.sp, color = if (pendingRequests > 0) Color(0xFFFFD08A) else Color.White.copy(alpha = 0.7f), weight = FontWeight.SemiBold
            )
        }
        Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun Welcome(onStartSetup: () -> Unit) {
    Gap(40.dp)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        listOf(
            com.example.toanhoc.data.family.Look("trai", "s1", "ngan", "den", "dongphuc", "congvien"),
            com.example.toanhoc.data.family.Look("gai", "s2", "duoingua", "nau", "vay", "phong"),
            com.example.toanhoc.data.family.Look("trai", "s3", "dung", "den", "sieunhan", "bien"),
            com.example.toanhoc.data.family.Look("gai", "s4", "haibui", "den", "aothun", "lop")
        ).forEach { l -> CharacterPortrait(l, 68.dp, Modifier.padding(horizontal = 4.dp)) }
    }
    FText("Chào mừng đến với\nToán Thông Thái", size = 28.sp, weight = FontWeight.ExtraBold, align = TextAlign.Center, modifier = Modifier.fillMaxWidth(), lineHeight = 36.sp)
    FText(
        "Bố mẹ thiết lập một lần, khoảng 1 phút:",
        size = 16.sp, color = FamilyTone.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth()
    )
    FCard {
        listOf(
            "1" to "Tạo mã PIN 4 số cho khu phụ huynh",
            "2" to "Nhập tên, năm sinh và lớp của con",
            "3" to "Con chọn bé trai/bé gái và màu da cho nhân vật"
        ).forEach { (n, t) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(32.dp).clip(CircleShape).background(FamilyTone.PrimarySoft), contentAlignment = Alignment.Center) {
                    FText(n, size = 15.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
                }
                FText(t, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            }
        }
    }
    FButton("Bắt đầu thiết lập", modifier = Modifier.fillMaxWidth(), onClick = onStartSetup)
    FText(
        "Dữ liệu chỉ lưu trên máy này. Ứng dụng chỉ cần tên và năm sinh của con.",
        size = 12.sp, color = FamilyTone.Muted, align = TextAlign.Center, modifier = Modifier.fillMaxWidth()
    )
}
