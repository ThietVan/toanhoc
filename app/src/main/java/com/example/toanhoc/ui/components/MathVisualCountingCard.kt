package com.example.toanhoc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.style.TextDecoration
import com.example.toanhoc.ui.theme.NunitoFontFamily
import com.example.toanhoc.ui.theme.SoftGreenBg
import com.example.toanhoc.ui.theme.SubtleBorder
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.ui.design.FormattedMathStatement
import com.example.toanhoc.ui.theme.ErrorDiagnosticRed
import com.example.toanhoc.ui.theme.GivenDataBlue
import com.example.toanhoc.ui.theme.MasteryVerifiedGreen
import com.example.toanhoc.ui.theme.SoftAmberBg
import com.example.toanhoc.ui.theme.SoftBlueBg
import com.example.toanhoc.ui.theme.SoftRedBg
import com.example.toanhoc.ui.theme.TargetUnknownAmber
import com.example.toanhoc.ui.theme.TextDark
import com.example.toanhoc.ui.theme.TextMuted

private val ARITHMETIC_EXPR_REGEX = Regex("""(\d+)\s*([+\-])\s*(\d+)""")

/**
 * Grade-Specific Pedagogical Explanation Card Router.
 * Dispatches:
 * - Grade 1 Basic Arithmetic (e.g. 5 + 3, 8 - 2): Visual Counting Blocks Card (Singapore CPA Model)
 * - All other domains (Geometry, Spatial, Word Problems, Grades 2-5): Standard Mathematical Step Breakdown
 */
@Composable
fun PedagogicalExplanationCard(
    gradeLevel: Int,
    problem: AcademicProblem?,
    modifier: Modifier = Modifier,
    socraticExplanation: String? = null,
    bugTitle: String? = null,
    userAnswer: String? = null
) {
    val statement = problem?.statementVi ?: ""
    val primaryLabel = problem?.blueprintSpec?.primaryLabel ?: ""
    val stepExpr = problem?.steps?.firstOrNull()?.mathExpression ?: ""

    val exprMatch = ARITHMETIC_EXPR_REGEX.find(primaryLabel)
        ?: ARITHMETIC_EXPR_REGEX.find(stepExpr)
        ?: ARITHMETIC_EXPR_REGEX.find(statement)

    val isGrade1Arithmetic = gradeLevel == 1 && exprMatch != null

    if (isGrade1Arithmetic) {
        MathVisualCountingCard(
            mathExpression = exprMatch.value,
            socraticExplanation = socraticExplanation,
            bugTitle = bugTitle,
            modifier = modifier
        )
    } else {
        StandardStepMathExplanationCard(
            problem = problem,
            socraticExplanation = socraticExplanation,
            bugTitle = bugTitle,
            userAnswer = userAnswer,
            modifier = modifier
        )
    }
}

/**
 * Thẻ giải thích khi bé chọn sai (Hình học, vị trí, toán lời văn, lớp 2–5).
 * Bố cục tối giản, đọc từ trên xuống:
 *  1. Tiêu đề ngắn "Cùng xem lại nhé"
 *  2. So sánh: bé chọn ✗  →  đáp án đúng ✓
 *  3. Các bước giải (danh sách đánh số, không lặp lại đề bài)
 *  4. Ghi nhớ: mẹo/nhận xét, tông ấm – không dùng màu đỏ cảnh báo
 */
@Composable
fun StandardStepMathExplanationCard(
    problem: AcademicProblem?,
    modifier: Modifier = Modifier,
    socraticExplanation: String? = null,
    bugTitle: String? = null,
    userAnswer: String? = null
) {
    val statement = problem?.statementVi?.trim().orEmpty()
    val correctAnswer = problem?.steps?.firstOrNull()?.expectedAnswer.orEmpty()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(1.dp, SubtleBorder, RoundedCornerShape(20.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Tiêu đề
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
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
                    text = bugTitle ?: "Cùng xem lại nhé",
                    fontFamily = NunitoFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "Sai một chút cũng không sao đâu",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }
        }

        // 2. So sánh đáp án
        if (correctAnswer.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!userAnswer.isNullOrBlank() && userAnswer != correctAnswer) {
                    AnswerCompareRow(
                        label = "Bé chọn",
                        value = userAnswer,
                        correct = false
                    )
                }
                AnswerCompareRow(
                    label = "Đáp án đúng",
                    value = correctAnswer,
                    correct = true
                )
            }
        }

        // 3. Các bước giải
        // Bỏ bước chỉ có lời nhắc chung ("Chọn phương án đúng…") mà không có phép tính:
        // phần Ghi nhớ bên dưới đã nói ý đó, giữ thẻ gọn để vừa một màn hình.
        val steps = problem?.steps.orEmpty().filter { step ->
            val expr = step.mathExpression.trim()
            val hasExpr = expr.isNotEmpty() && expr != statement
            hasExpr || !step.promptVi.startsWith("Chọn phương án đúng")
        }
        if (steps.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SubtleBorder)
            )
            Text(
                text = "Cách giải",
                fontFamily = NunitoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.3.sp
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                steps.forEach { step ->
                    val promptText = if (step.promptVi.startsWith("Chọn phương án đúng")) {
                        "Đọc kỹ đề bài và so sánh từng đáp án với yêu cầu"
                    } else {
                        step.promptVi
                    }
                    // Không lặp lại nguyên văn đề bài trong phần giải
                    val expr = step.mathExpression.trim()
                    val showExpr = expr.isNotEmpty() && expr != statement

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
                                text = "${step.stepIndex}",
                                fontFamily = NunitoFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GivenDataBlue
                            )
                        }
                        Column(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = promptText,
                                fontFamily = NunitoFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextDark
                            )
                            if (showExpr) {
                                FormattedMathStatement(
                                    statement = expr,
                                    fontSize = 16.sp,
                                    color = GivenDataBlue
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Ghi nhớ
        val tip = socraticExplanation?.takeIf { it.isNotBlank() }
            ?: "Đọc kỹ đề bài, rồi so sánh từng đáp án với điều đề bài hỏi nhé."
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
                .padding(start = 16.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Ghi nhớ",
                    fontFamily = NunitoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetUnknownAmber
                )
                Text(
                    text = tip,
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = TextDark
                )
            }
        }
    }
}

/** Một dòng so sánh đáp án: biểu tượng ✓/✗ + nhãn + giá trị. */
@Composable
private fun AnswerCompareRow(label: String, value: String, correct: Boolean) {
    val tint = if (correct) MasteryVerifiedGreen else ErrorDiagnosticRed
    val bg = if (correct) SoftGreenBg else SoftRedBg
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(tint),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (correct) Icons.Rounded.Check else Icons.Rounded.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = label,
            fontFamily = NunitoFontFamily,
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.width(84.dp)
        )
        Text(
            text = value,
            fontFamily = NunitoFontFamily,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            textDecoration = if (correct) null else TextDecoration.LineThrough
        )
    }
}

/**
 * Visual Counting Blocks Explanation Card for Grade 1 Basic Arithmetic Only.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MathVisualCountingCard(
    mathExpression: String,
    modifier: Modifier = Modifier,
    socraticExplanation: String? = null,
    bugTitle: String? = null
) {
    val match = ARITHMETIC_EXPR_REGEX.find(mathExpression)
    val numA = match?.groupValues?.getOrNull(1)?.toIntOrNull()
    val op = match?.groupValues?.getOrNull(2) ?: "+"
    val numB = match?.groupValues?.getOrNull(3)?.toIntOrNull()

    // Safety guard: Never render fake default numbers like 7 + 2 = 9
    if (numA == null || numB == null) {
        return
    }

    val isSubtraction = op == "-"
    val result = if (isSubtraction) (numA - numB).coerceAtLeast(0) else (numA + numB)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SoftBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = GivenDataBlue, modifier = Modifier.size(18.dp))
                }

                Column {
                    Text(
                        text = bugTitle ?: "Cùng xem lại nhé",
                        fontFamily = NunitoFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GivenDataBlue
                    )
                    Text(
                        text = "Cùng đếm khối ô vuông với Mèo Thông Thái nhé!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SoftBlueBg)
                    .padding(16.dp)
            ) {
                if (!isSubtraction) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                for (i in 1..numA) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(GivenDataBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$i",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "+",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TargetUnknownAmber,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                for (i in 1..numB) {
                                    val countNum = numA + i
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(TargetUnknownAmber),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$countNum",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        val countStepsText = (1..numB).joinToString("... ") { "${numA + it}" }
                        Text(
                            text = socraticExplanation ?: "Ban đầu có $numA ô màu xanh. Bé đếm tiếp $numB ô màu cam nhé: $countStepsText!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            for (i in 1..result) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GivenDataBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$i",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            repeat(numB) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SoftRedBg)
                                        .border(1.dp, ErrorDiagnosticRed, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = ErrorDiagnosticRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = socraticExplanation ?: "Có $numA ô vuông, bớt đi $numB ô bị gạch. Còn lại $result ô nguyên vẹn!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(SoftAmberBg)
                    .border(1.dp, TargetUnknownAmber.copy(alpha = 0.3f), RoundedCornerShape(50))
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Đáp án đúng:  $numA $op $numB = $result bé nhé!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TargetUnknownAmber
                )
            }
        }
    }
}
