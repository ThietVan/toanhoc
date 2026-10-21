package com.example.toanhoc.ui.exam

import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ExamMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AssessmentExamViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AssessmentExamViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AssessmentExamViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startExam generates 10 exam problems`() {
        viewModel.startExam(mode = ExamMode.LEVEL_UP_EXAM, grade = 1, targetTier = AcademicTier.GIOI)
        val state = viewModel.uiState.value

        assertEquals(10, state.problems.size)
        assertEquals(ExamMode.LEVEL_UP_EXAM, state.examMode)
        assertEquals(AcademicTier.GIOI, state.targetTier)
        assertFalse(state.isSubmitted)
    }

    @Test
    fun `submitting all correct answers passes level up exam`() {
        viewModel.startExam(mode = ExamMode.LEVEL_UP_EXAM, grade = 1, targetTier = AcademicTier.GIOI)

        // Fill all 10 answers correctly
        viewModel.uiState.value.problems.forEach { problem ->
            val expected = problem.steps.lastOrNull()?.expectedAnswer ?: ""
            viewModel.onAnswerInput(expected)
            viewModel.onNextProblem()
        }

        viewModel.submitExam()
        val result = viewModel.uiState.value

        assertTrue(result.isSubmitted)
        assertEquals(10, result.scoreOutOf10)
        assertTrue(result.isPassed)
    }
}
