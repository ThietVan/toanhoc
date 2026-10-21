package com.example.toanhoc.domain.model

enum class VisualBlueprintType {
    NONE,
    NUMBER_LINE,
    SINGAPORE_BAR_MODEL,
    VERTICAL_ARITHMETIC,
    GEOMETRY_GRID,
    SPATIAL_SHAPES
}

/**
 * Visual Blueprint specification passed to Compose Canvas components for rendering math diagrams.
 */
data class VisualBlueprintSpec(
    val type: VisualBlueprintType,
    val primaryLabel: String = "",
    val secondaryLabel: String = "",
    val paramValue1: Double = 0.0,
    val paramValue2: Double = 0.0,
    val paramValue3: Double = 0.0,
    val extraData: Map<String, String> = emptyMap()
)
