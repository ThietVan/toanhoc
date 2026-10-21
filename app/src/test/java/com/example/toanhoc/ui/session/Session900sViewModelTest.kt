package com.example.toanhoc.ui.session

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
class Session900sViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: Session900sViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = Session900sViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession initializes count-up stopwatch timer state correctly`() {
        viewModel.startSession(grade = 1)
        val state = viewModel.uiState.value

        assertEquals(1, state.gradeLevel)
        assertEquals(0, state.elapsedSeconds)
        assertFalse(state.isPaused)
        assertFalse(state.isSessionFinished)
        assertFalse(state.isRoundPassed)
    }

    @Test
    fun `pause and resume session updates state flow correctly`() {
        viewModel.startSession(grade = 1)
        viewModel.onPauseToggle(true)
        assertTrue("Session should be marked as paused", viewModel.uiState.value.isPaused)

        viewModel.onPauseToggle(false)
        assertFalse("Session should be resumed", viewModel.uiState.value.isPaused)
    }

    @Test
    fun `session is NOT marked passed if fewer than 12 questions completed`() {
        viewModel.startSession(grade = 1)
        val problem = viewModel.uiState.value.currentProblem
        val step = problem?.steps?.getOrNull(0)

        if (step != null) {
            viewModel.submitOptionChoice(step.expectedAnswer)
        }

        val state = viewModel.uiState.value
        assertEquals(1, state.totalAnsweredCount)
        assertFalse("Round must NOT be passed when only 1 question answered", state.isRoundPassed)
    }

    @Test
    fun `stepBackOrUndo resets current step input if at first step`() {
        viewModel.startSession(grade = 1)
        viewModel.onKeyPress("5")
        assertEquals("5", viewModel.uiState.value.userInput)

        viewModel.onStepBackOrUndo()
        assertEquals("", viewModel.uiState.value.userInput)
    }
}
