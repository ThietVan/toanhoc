package com.example.toanhoc.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.ui.theme.NunitoFontFamily

/* Bộ xem trước sơ đồ trong Android Studio (không ảnh hưởng ứng dụng). Mỗi mục là một đề thật từ bộ sinh đề. */

private data class Sample(val type: String, val statement: String, val expr: String = "", val options: List<String> = emptyList())

@Composable
private fun Gallery(samples: List<Sample>) {
    Column(
        Modifier.background(Color.White).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        samples.forEach { s ->
            Text(s.statement, fontFamily = NunitoFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            val d = BlueprintInterpreter.interpret(s.type, s.expr.ifEmpty { s.statement }, "", s.statement, s.options)
            if (d == null) Text("— không có sơ đồ —", color = Color.Red)
            else DiagramView(
                d,
                Modifier.fillMaxWidth().clip(RoundedCornerShape(13.dp)).background(Color(0xFFF7F8FA)).padding(vertical = 13.dp, horizontal = 5.dp)
            )
        }
    }
}

@Preview(name = "1 · Tia số", widthDp = 360, heightDp = 1500, showBackground = true)
@Composable
private fun PreviewNumberLine() = Gallery(
    listOf(
        Sample("NUMBER_LINE", "Trên tia số, số liền sau của 81 là số nào?"),
        Sample("NUMBER_LINE", "Tính: 7 + 3 = ?", "7 + 3"),
        Sample("NUMBER_LINE", "Tìm số thích hợp thay cho dấu ?: 9 - ? = 4", "9 - ? = 4"),
        Sample("NUMBER_LINE", "Tính: 8 - 3 + 4 = ?", "8 - 3 + 4"),
        Sample("NUMBER_LINE", "Tìm số còn thiếu trong dãy số: 40; 50; ...; 70"),
        Sample("NUMBER_LINE", "Rô-bốt đo một băng giấy dọc theo thước kẻ: một đầu ở vạch số 2, đầu kia ở vạch số 9 (đơn vị cm). Băng giấy dài bao nhiêu xăng-ti-mét?", "9 cm - 2 cm"),
        Sample("NUMBER_LINE", "Số nào lớn nhất trong các số sau: 6, 9, 8, 4?"),
    )
)

@Preview(name = "2 · Sơ đồ đoạn thẳng", widthDp = 360, heightDp = 1500, showBackground = true)
@Composable
private fun PreviewBars() = Gallery(
    listOf(
        Sample("SINGAPORE_BAR_MODEL", "Tách 8 gồm 3 và số mấy?", "8 - 3"),
        Sample("SINGAPORE_BAR_MODEL", "An có 32 bông hoa, An cho Nam 21 bông hoa. Hỏi An còn lại bao nhiêu bông hoa?", "32 - 21"),
        Sample("SINGAPORE_BAR_MODEL", "Mai có 33 bông hoa, Bình có 15 bông hoa. Hỏi cả hai bạn có tất cả bao nhiêu bông hoa?", "33 + 15"),
        Sample("SINGAPORE_BAR_MODEL", "Lớp 2A trồng được 37 cây hoa. Lớp 2B trồng được nhiều hơn lớp 2A là 18 cây hoa. Hỏi lớp 2B trồng được bao nhiêu cây hoa?", "37 + 18"),
        Sample("SINGAPORE_BAR_MODEL", "Mai có 40 nhãn vở và nhiều hơn Mi 13 nhãn vở. Hỏi Mi có bao nhiêu nhãn vở?", "40 - 13"),
        Sample("SINGAPORE_BAR_MODEL", "Ngăn trên có 6 quyển sách. Số sách ở ngăn dưới gấp 3 lần số sách ở ngăn trên. Hỏi cả hai ngăn có tất cả bao nhiêu quyển sách?"),
        Sample("SINGAPORE_BAR_MODEL", "Hai số có tổng bằng 80 và hiệu bằng 10. Số bé là:"),
        Sample("SINGAPORE_BAR_MODEL", "Hai số có hiệu bằng 15 và tỉ số là 2/5. Số lớn là:"),
    )
)

@Preview(name = "3 · Hình học", widthDp = 360, heightDp = 2200, showBackground = true)
@Composable
private fun PreviewGeometry() = Gallery(
    listOf(
        Sample("GEOMETRY_GRID", "Tính chu vi hình tam giác có độ dài các cạnh là 5 cm, 9 cm và 6 cm:"),
        Sample("GEOMETRY_GRID", "Tính diện tích hình chữ nhật có chiều dài 9 cm và chiều rộng 5 cm:"),
        Sample("GEOMETRY_GRID", "Một hình vuông có chu vi là 56 cm. Diện tích của hình vuông đó là:"),
        Sample("GEOMETRY_GRID", "Hình tròn tâm O có bán kính 14 cm. Đường kính của hình tròn đó dài bao nhiêu xăng-ti-mét?"),
        Sample("GEOMETRY_GRID", "M là trung điểm của đoạn thẳng AB dài 8 cm. Độ dài đoạn thẳng AM bằng bao nhiêu xăng-ti-mét?"),
        Sample("GEOMETRY_GRID", "Một đường gấp khúc gồm 4 đoạn thẳng có độ dài lần lượt là 6 cm, 4 cm, 9 cm, 5 cm. Độ dài đường gấp khúc đó là:"),
        Sample("GEOMETRY_GRID", "Tính diện tích hình thang có độ dài hai đáy lần lượt là 6 cm và 11 cm, chiều cao 4 cm:"),
        Sample("GEOMETRY_GRID", "Tính diện tích hình thoi có độ dài hai đường chéo lần lượt là 11 cm và 6 cm:"),
        Sample("GEOMETRY_GRID", "Tính diện tích hình tam giác có độ dài đáy 15 cm và chiều cao 10 cm:"),
    )
)

@Preview(name = "4 · Đặt tính & dãy hình", widthDp = 360, heightDp = 1100, showBackground = true)
@Composable
private fun PreviewOthers() = Gallery(
    listOf(
        Sample("VERTICAL_ARITHMETIC", "Đặt tính rồi tính: 458 + 274 = ?", "458 + 274"),
        Sample("VERTICAL_ARITHMETIC", "Đặt tính rồi tính: 12,5 + 3,75 = ?", "12,5 + 3,75"),
        Sample("VERTICAL_ARITHMETIC", "Đặt tính rồi tính: 864 : 4 = ?", "864 : 4"),
        Sample("SPATIAL_SHAPES", "Rô-bốt xếp 3 hình theo thứ tự từ trái sang phải: Hình tam giác, Khối lập phương, Hình tròn. Hình nào ở vị trí ở giữa?"),
        Sample("SPATIAL_SHAPES", "Trong các hình dưới đây, hình nào là khối hình học (không phải hình phẳng)?",
            options = listOf("Hình vuông", "Khối hộp chữ nhật", "Hình tròn", "Hình tam giác")),
    )
)
