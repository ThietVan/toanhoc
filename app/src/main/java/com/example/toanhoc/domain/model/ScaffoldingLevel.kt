package com.example.toanhoc.domain.model

/**
 * Scaffolding levels for 15-minute deliberate practice session.
 */
enum class ScaffoldingLevel {
    WORKED_EXAMPLE,   // Fully worked example + comprehension step
    FADED_STEP,       // Intermediate reasoning step hidden
    INDEPENDENT,      // Pólya 4-step independent problem solving
    FLUENCY_DRILL     // Automaticity rapid response drill
}
