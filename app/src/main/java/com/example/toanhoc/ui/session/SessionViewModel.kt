package com.example.toanhoc.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.domain.model.SessionPhase
import com.example.toanhoc.domain.model.SessionState
import com.example.toanhoc.domain.usecase.GetSessionProgressUseCase
import com.example.toanhoc.domain.usecase.SessionProgressSummary
import com.example.toanhoc.domain.usecase.StartSessionUseCase
import com.example.toanhoc.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SessionViewModel(
    private val startSessionUseCase: StartSessionUseCase = StartSessionUseCase(),
    private val submitAnswerUseCase: SubmitAnswerUseCase = SubmitAnswerUseCase(),
    private val getSessionProgressUseCase: GetSessionProgressUseCase = GetSessionProgressUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionState())
    val uiState: StateFlow<SessionState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var problemStartTimeMs: Long = System.currentTimeMillis()

    fun startSession(grade: Int) {
        val initialState = startSessionUseCase(grade)
        _uiState.value = initialState
        problemStartTimeMs = System.currentTimeMillis()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.totalSecondsRemaining > 0) {
                delay(1000L)
                _uiState.update { state ->
                    val newTotal = state.totalSecondsRemaining - 1
                    val newPhaseSec = (state.phaseSecondsRemaining - 1).coerceAtLeast(0)

                    var newPhase = state.currentPhase
                    var updatedPhaseSec = newPhaseSec

                    // Check phase transitions
                    if (newTotal == 720 && state.currentPhase == SessionPhase.AUTOMATICITY) {
                        newPhase = SessionPhase.WORKED_EXAMPLE
                        updatedPhaseSec = 480
                    } else if (newTotal == 240 && state.currentPhase == SessionPhase.WORKED_EXAMPLE) {
                        newPhase = SessionPhase.FIRE_RETRIEVAL
                        updatedPhaseSec = 240
                    } else if (newTotal <= 0) {
                        newPhase = SessionPhase.SUMMARY
                        updatedPhaseSec = 0
                    }

                    state.copy(
                        totalSecondsRemaining = newTotal,
                        phaseSecondsRemaining = updatedPhaseSec,
                        currentPhase = newPhase
                    )
                }
            }
        }
    }

    fun onKeyPress(key: String) {
        _uiState.update { it.copy(userInput = it.userInput + key) }
    }

    fun onBackspace() {
        _uiState.update {
            if (it.userInput.isNotEmpty()) {
                it.copy(userInput = it.userInput.dropLast(1))
            } else it
        }
    }

    fun submitAnswer() {
        val latencyMs = System.currentTimeMillis() - problemStartTimeMs
        _uiState.update { state ->
            submitAnswerUseCase(state, latencyMs)
        }
        problemStartTimeMs = System.currentTimeMillis()
    }

    fun getSummary(): SessionProgressSummary {
        return getSessionProgressUseCase(_uiState.value)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
