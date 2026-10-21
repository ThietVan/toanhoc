package com.example.toanhoc.ui.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.toanhoc.domain.model.AcademicProblem
import com.example.toanhoc.domain.model.VisualBlueprintSpec
import com.example.toanhoc.domain.model.VisualBlueprintType

/**
 * Dựng mô hình sơ đồ cho một bài toán. Trả về null khi bài không cần / không nhận ra dạng sơ đồ
 * → màn hình không hiển thị khung sơ đồ.
 */
fun diagramFor(spec: VisualBlueprintSpec?, statement: String, options: List<String>): Diagram? {
    if (spec == null || spec.type == VisualBlueprintType.NONE) return null
    return BlueprintInterpreter.interpret(spec.type.name, spec.primaryLabel, spec.secondaryLabel, statement, options)
}

fun diagramFor(problem: AcademicProblem?): Diagram? =
    problem?.let { diagramFor(it.blueprintSpec, it.statementVi, it.steps.firstOrNull()?.options.orEmpty()) }

@Composable
fun rememberDiagram(problem: AcademicProblem?): Diagram? = remember(problem?.problemId, problem?.statementVi) { diagramFor(problem) }

/** Vẽ một sơ đồ đã dựng. */
@Composable
fun DiagramView(diagram: Diagram, modifier: Modifier = Modifier) {
    when (diagram) {
        is NumberLineDiagram -> NumberLineView(diagram, modifier)
        is BarDiagram -> BarModelView(diagram, modifier)
        is VerticalDiagram -> VerticalArithmeticView(diagram, modifier)
        is GeometryDiagram -> GeometryView(diagram, modifier)
        is ShapesDiagram -> ShapesRowView(diagram, modifier)
    }
}

/**
 * Tương thích với chỗ gọi cũ. Sơ đồ KHÔNG còn hiển thị câu trả lời của học sinh hay đáp án.
 */
@Composable
fun MathematicalBlueprint(
    spec: VisualBlueprintSpec,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") studentInput: String = "",
    statement: String = "",
    options: List<String> = emptyList()
) {
    val d = remember(spec, statement, options) { diagramFor(spec, statement, options) } ?: return
    DiagramView(d, modifier)
}
