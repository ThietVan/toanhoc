package com.example.toanhoc.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toanhoc.domain.generator.FixedTheoryRegistry
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.ui.design.Fib
import com.example.toanhoc.ui.design.FormattedMathStatement
import com.example.toanhoc.ui.design.PaperColors
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import androidx.compose.ui.draw.drawBehind
import com.example.toanhoc.ui.theme.TargetUnknownAmber

/**
 * Topic-specific Fixed Theory Lesson Content data.
 */
data class FixedTheoryLesson(
    val titleVi: String,
    val ruleRuleVi: String,
    val exampleStatementVi: String,
    val stepsVi: List<Pair<String, String>>
)

/**
 * Modal Bottom Sheet presenting DYNAMICALLY MATCHED Parallel Pedagogical Lessons & Worked Examples
 * matching the EXACT mathematical operator (+, -, ×, :) without spoiling the student's current practice question.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheoryBottomSheet(
    problem: AcademicProblem?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    /** Bài cùng dạng, khác số – nếu dựng được lý thuyết cho trẻ thì dùng giao diện mới. */
    example: AcademicProblem? = null,
    gradeLevel: Int = 0
) {
    val kid = androidx.compose.runtime.remember(example?.problemId, example?.statementVi) {
        example?.let { com.example.toanhoc.domain.theory.KidTheories.build(it) }
    }
    val nodeId = problem?.nodeId ?: "G1_01"
    val statement = problem?.statementVi ?: ""
    val lesson = FixedTheoryRegistry.getLessonForNode(nodeId, statement)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = Fib.S3, bottom = Fib.S2)
                    .size(width = Fib.S5, height = 4.dp)
                    .clip(CircleShape)
                    .background(PaperColors.Hairline)
            )
        },
        shape = RoundedCornerShape(topStart = Fib.S5, topEnd = Fib.S5),
        modifier = modifier
    ) {
        if (kid != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Fib.S4)
                    .padding(bottom = Fib.S4)
                    .verticalScroll(rememberScrollState())
            ) {
                com.example.toanhoc.ui.theory.KidTheoryContent(
                    theory = kid,
                    gradeLabel = if (gradeLevel > 0) "LỚP $gradeLevel · LÝ THUYẾT" else "LÝ THUYẾT",
                    childQuestion = problem?.statementVi,
                    onTry = onDismiss
                )
            }
            return@ModalBottomSheet
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Fib.S4, vertical = Fib.S3)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Fib.S4)
        ) {
            // 1. Tiêu đề
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoStories,
                        contentDescription = null,
                        tint = GivenDataBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = lesson.titleVi,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NunitoFontFamily,
                        color = PaperColors.Ink
                    )
                    Text(
                        text = "Đọc lý thuyết rồi quay lại làm bài nhé",
                        fontSize = 13.sp,
                        fontFamily = NunitoFontFamily,
                        color = PaperColors.InkSoft
                    )
                }
            }

            // 2. Ghi nhớ – cùng kiểu với thẻ giải thích khi làm sai
            if (lesson.ruleRuleVi.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftAmberBg)
                        .drawBehind {
                            drawRect(
                                color = TargetUnknownAmber,
                                size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height)
                            )
                        }
                        .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = TargetUnknownAmber,
                        modifier = Modifier
                            .padding(top = 1.dp)
                            .size(18.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Ghi nhớ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NunitoFontFamily,
                            color = TargetUnknownAmber
                        )
                        Text(
                            text = lesson.ruleRuleVi,
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            fontFamily = NunitoFontFamily,
                            color = PaperColors.Ink
                        )
                    }
                }
            }

            // 3. Ví dụ + các bước giải mẫu, chung một thẻ
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, PaperColors.Hairline, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ví dụ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = NunitoFontFamily,
                    color = PaperColors.InkSoft
                )
                FormattedMathStatement(
                    statement = lesson.exampleStatementVi,
                    fontSize = 18.sp,
                    color = PaperColors.Ink
                )

                if (lesson.stepsVi.isNotEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(PaperColors.Hairline)
                    )
                    Text(
                        text = "Cách giải",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = NunitoFontFamily,
                        color = PaperColors.InkSoft
                    )
                    lesson.stepsVi.forEachIndexed { index, stepPair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SoftBlueBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = NunitoFontFamily,
                                    color = GivenDataBlue
                                )
                            }
                            Column(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stepPair.first,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = NunitoFontFamily,
                                    color = PaperColors.Ink
                                )
                                if (stepPair.second.isNotEmpty()) {
                                    FormattedMathStatement(
                                        statement = stepPair.second,
                                        fontSize = 16.sp,
                                        color = GivenDataBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            val context = androidx.compose.ui.platform.LocalContext.current

            // 5. Action Button ("Đã hiểu, tiếp tục làm bài")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Fib.S6)
                    .clip(RoundedCornerShape(Fib.S3))
                    .background(GivenDataBlue)
                    .clickable {
                        com.example.toanhoc.ui.sound.SoundEffectsManager.playSelect(context)
                        onDismiss()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã hiểu, làm bài tiếp",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = NunitoFontFamily,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(Fib.S2))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
