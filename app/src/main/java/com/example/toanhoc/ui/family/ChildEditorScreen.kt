package com.example.toanhoc.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.data.family.AvatarShop
import com.example.toanhoc.data.family.Look
import com.example.toanhoc.data.family.ShopKind
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.domain.curriculum.Curricula
import androidx.compose.foundation.layout.height
import java.time.LocalDate

/**
 * Tạo / sửa hồ sơ con: tên, năm sinh, lớp đang học (mặc định lớp 1), bé trai/bé gái và màu da.
 * Gợi ý lớp theo tuổi nhưng để bố mẹ tự chọn.
 */
@Composable
fun ChildEditorScreen(
    initial: ChildProfile?,
    title: String,
    saveText: String,
    onSave: (name: String, birthYear: Int, grade: Int, gender: String, skin: String) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onCancel)
    val year = LocalDate.now().year
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var birthYear by remember { mutableIntStateOf(initial?.birthYear ?: (year - 7)) }
    var grade by remember { mutableIntStateOf(initial?.grade ?: 1) }
    var gender by remember { mutableStateOf(initial?.gender ?: "trai") }
    var skin by remember { mutableStateOf(initial?.skin ?: "s1") }
    var confirmDelete by remember { mutableStateOf(false) }
    val age = year - birthYear
    val gradeByAge = (age - 5).coerceIn(1, 5)

    Column(modifier.fillMaxSize().background(FamilyTone.Paper).imePadding()) {
        BackRow(title, onCancel)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).align(Alignment.CenterHorizontally).widthIn(max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (initial == null) {
                FCard {
                    FText("Nhân vật của con", size = 16.sp, weight = FontWeight.ExtraBold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("trai" to "Bé trai", "gai" to "Bé gái").forEach { (g, label) ->
                            val sel = g == gender
                            Column(
                                Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                                    .border(2.dp, if (sel) FamilyTone.Primary else FamilyTone.Line, RoundedCornerShape(18.dp))
                                    .clickable(role = Role.RadioButton) { gender = g }.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CharacterFigure(Look(gender = g, skin = skin, hair = AvatarShop.defaultHair(g)), Modifier.size(width = 100.dp, height = 170.dp))
                                FText(label, size = 15.sp, weight = FontWeight.ExtraBold, color = if (sel) FamilyTone.Primary else FamilyTone.Ink)
                            }
                        }
                    }
                    FText("Con dùng xu thưởng để mua phụ kiện, phông nền và thú cưng.", size = 12.sp, color = FamilyTone.Muted)
                }
            }

            FCard {
                FText("Tên con", size = 16.sp, weight = FontWeight.ExtraBold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 24) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { FText("Ví dụ: Bin, Na, Minh Anh", color = FamilyTone.Muted) },
                    textStyle = TextStyle(fontFamily = NunitoFontFamily, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FamilyTone.Ink),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FamilyTone.Primary, unfocusedBorderColor = FamilyTone.Line)
                )
                FText("Tên gọi ở nhà là đủ – ứng dụng dùng để chào con.", size = 12.sp, color = FamilyTone.Muted)
            }

            FCard {
                FText("Năm sinh", size = 16.sp, weight = FontWeight.ExtraBold)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StepButton(Icons.Rounded.Remove, "Giảm năm", enabled = birthYear > year - 13) { birthYear-- }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        FText("$birthYear", size = 28.sp, weight = FontWeight.ExtraBold)
                        FText("Năm nay con $age tuổi", size = 13.sp, color = FamilyTone.Muted)
                    }
                    StepButton(Icons.Rounded.Add, "Tăng năm", enabled = birthYear < year - 4) { birthYear++ }
                }
            }

            FCard {
                FText("Lớp con cần học", size = 16.sp, weight = FontWeight.ExtraBold)
                GradeSelector(selected = grade, byAge = gradeByAge, onSelect = { grade = it })
                val lessonCount = Curricula.forGrade(grade)?.lessons?.size
                FText(
                    "Toán lớp $grade · Kết nối tri thức" + (lessonCount?.let { " · $it bài" } ?: ""),
                    size = 14.sp, weight = FontWeight.Bold, color = FamilyTone.Primary
                )
                if (gradeByAge != grade) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(FamilyTone.PrimarySoft)
                            .clickable(role = Role.Button) { grade = gradeByAge }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FText("Theo tuổi (chấm xanh), con thường học lớp $gradeByAge.", size = 13.sp, modifier = Modifier.weight(1f))
                        FText("Chọn lớp $gradeByAge", size = 13.sp, weight = FontWeight.ExtraBold, color = FamilyTone.Primary)
                    }
                }
                FText(
                    "Có thể chọn lớp thấp hơn để con củng cố nền tảng. Bài học mở dần theo thứ tự trong sách.",
                    size = 12.sp, color = FamilyTone.Muted, lineHeight = 17.sp
                )
            }

            if (onDelete != null) {
                FButton("Xóa hồ sơ của con", modifier = Modifier.fillMaxWidth(), outlined = true, color = FamilyTone.Red) { confirmDelete = true }
            }
            Gap(8.dp)
        }
        Box(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 12.dp)) {
            FButton(saveText, modifier = Modifier.fillMaxWidth(), enabled = name.isNotBlank()) {
                onSave(name.trim(), birthYear, grade, gender, skin)
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { FText("Xóa hồ sơ của ${initial?.name}?", size = 18.sp, weight = FontWeight.ExtraBold) },
            text = { FText("Xu, đồ trang trí, nhật ký học và yêu cầu đổi quà của con sẽ bị xóa khỏi máy này. Không thể hoàn tác.", size = 14.sp) },
            confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { FText("Xóa", weight = FontWeight.ExtraBold, color = FamilyTone.Red) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { FText("Giữ lại", weight = FontWeight.Bold) } }
        )
    }
}

@Composable
private fun StepButton(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp).clip(CircleShape).background(if (enabled) FamilyTone.PrimarySoft else FamilyTone.Paper)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, contentDescription = desc, tint = if (enabled) FamilyTone.Primary else FamilyTone.Line) }
}

/** Thanh chọn lớp dạng phân đoạn: chữ "LỚP" nhỏ + số lớn; chấm xanh dưới lớp đúng độ tuổi. */
@Composable
private fun GradeSelector(selected: Int, byAge: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FamilyTone.Paper)
            .border(1.dp, FamilyTone.Line, RoundedCornerShape(16.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        (1..5).forEach { g ->
            val sel = g == selected
            Column(
                Modifier.weight(1f).height(72.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (sel) FamilyTone.Primary else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(g) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FText("LỚP", size = 10.sp, weight = FontWeight.ExtraBold, color = if (sel) Color.White.copy(alpha = 0.75f) else FamilyTone.Muted, maxLines = 1)
                FText("$g", size = 26.sp, weight = FontWeight.ExtraBold, color = if (sel) Color.White else FamilyTone.Ink, maxLines = 1)
                Box(
                    Modifier.size(5.dp).clip(CircleShape)
                        .background(if (g == byAge) (if (sel) Color.White else FamilyTone.Green) else Color.Transparent)
                )
            }
        }
    }
}
