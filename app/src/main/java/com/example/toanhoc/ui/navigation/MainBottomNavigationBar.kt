package com.example.toanhoc.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.NunitoFontFamily

/** [icon] khi đang chọn (đặc), [iconOutline] khi không chọn (viền). */
data class NavItem(val label: String, val icon: ImageVector, val iconOutline: ImageVector)

private val Ink = Color(0xFF111827)
private val Muted = Color(0xFF6B7280)
private val Line = Color(0xFFE4E7EC)
private val Pill = Color(0xFFEEF2FC)

/**
 * Thanh điều hướng dưới – cùng ngôn ngữ thiết kế với app (nền trắng, viền mảnh, chữ Nunito, xanh #2F55D4):
 *  - 4 ô chia đều bề ngang, vùng chạm cao 64dp (không còn hiệu ứng gợn tròn bị cắt méo);
 *  - mục đang chọn: biểu tượng đặc trong viên thuốc xanh nhạt, chữ đậm xanh, nảy nhẹ khi chọn;
 *  - mục khác: biểu tượng viền xám;
 *  - nền trắng kéo xuống dưới thanh cử chỉ của hệ thống (không còn dải xám);
 *  - chấm đỏ báo việc mới (quà đổi được, huy hiệu chờ nhận).
 */
@Composable
fun MainBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dots: Set<Int> = emptySet(),
    /** Tên tab thứ hai ("Cửa hàng" khi có hồ sơ con; mặc định "Thành tích"). */
    secondTab: String? = null
) {
    val items = listOf(
        NavItem("Bài học", Icons.Rounded.Explore, Icons.Outlined.Explore),
        if (secondTab != null) NavItem(secondTab, Icons.Rounded.Storefront, Icons.Outlined.Storefront)
        else NavItem("Thành tích", Icons.Rounded.EmojiEvents, Icons.Outlined.EmojiEvents),
        NavItem("Giải đấu", Icons.Rounded.SportsEsports, Icons.Outlined.SportsEsports),
        NavItem("Cá nhân", Icons.Rounded.Person, Icons.Outlined.Person)
    )
    val context = LocalContext.current

    Column(
        modifier
            .fillMaxWidth()
            .background(Color.White)
            .drawBehind { drawLine(Line, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                NavTab(
                    item = item,
                    selected = selectedTab == index,
                    hasDot = index in dots,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (selectedTab != index) {
                            com.example.toanhoc.ui.sound.SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.TAB)
                            onTabSelected(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavTab(item: NavItem, selected: Boolean, hasDot: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val pillColor by animateColorAsState(if (selected) Pill else Color.Transparent, tween(180), label = "pill")
    val contentColor by animateColorAsState(if (selected) GivenDataBlue else Muted, tween(180), label = "content")
    val pillWidth by animateDpAsState(if (selected) 60.dp else 40.dp, spring(dampingRatio = 0.7f, stiffness = 500f), label = "pillW")
    val iconScale by animateFloatAsState(if (selected) 1f else 0.94f, spring(dampingRatio = 0.5f, stiffness = 600f), label = "scale")

    Column(
        modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // không dùng gợn sóng: phản hồi bằng viên thuốc + nảy nhẹ
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .width(pillWidth)
                .height(32.dp)
                .clip(CircleShape)
                .background(pillColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.icon else item.iconOutline,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp).scale(iconScale)
            )
            if (hasDot) {
                Box(
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 12.dp, y = 3.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE5484D))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
        Text(
            text = item.label,
            modifier = Modifier.padding(top = 4.dp),
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontFamily = NunitoFontFamily,
            color = contentColor,
            maxLines = 1
        )
    }
}
