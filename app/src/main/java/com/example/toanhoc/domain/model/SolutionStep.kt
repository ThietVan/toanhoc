package com.example.toanhoc.domain.model

import com.example.toanhoc.domain.engine.CognitiveBugTag

/**
 * Single step of solution reasoning with bug trap rules and AnswerMode support.
 */
data class SolutionStep(
    val stepIndex: Int,
    val promptVi: String,
    val mathExpression: String,
    val expectedAnswer: String,
    val isHiddenForStudent: Boolean = false,
    val bugRules: Map<String, CognitiveBugTag> = emptyMap(),
    val answerMode: AnswerMode = AnswerMode.DIRECT_KEYPAD_INPUT,
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0
)
