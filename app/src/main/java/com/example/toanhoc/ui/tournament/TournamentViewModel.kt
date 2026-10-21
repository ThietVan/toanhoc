package com.example.toanhoc.ui.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.data.family.BotRun
import com.example.toanhoc.data.family.ChildProfile
import com.example.toanhoc.data.family.League
import com.example.toanhoc.data.family.LeagueBot
import com.example.toanhoc.data.family.LeagueState
import com.example.toanhoc.data.family.MatchResult
import com.example.toanhoc.domain.curriculum.ExamQuestion
import com.example.toanhoc.domain.curriculum.TournamentQuestionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

enum class MatchPhase { MATCHING, PLAYING, RESULT }

data class MatchState(
    val matchId: Long = 0,
    val phase: MatchPhase = MatchPhase.MATCHING,
    val grade: Int = 1,
    val me: ChildProfile? = null,
    val opponents: List<LeagueBot> = emptyList(),
    /** Số bạn đã "vào phòng" trong màn tìm bạn. */
    val found: Int = 0,
    val questions: List<ExamQuestion> = emptyList(),
    val index: Int = 0,
    val answers: Map<Int, String> = emptyMap(),
    /** Đáp án vừa chọn + đúng/sai, hiện ~0,6 giây rồi sang câu sau. */
    val feedback: Pair<String, Boolean>? = null,
    val elapsedMs: Long = 0,
    val runs: List<BotRun> = emptyList(),
    val results: List<MatchResult> = emptyList(),
    val myCorrect: Int = 0,
    val myTimeMs: Long = 0,
    val forfeited: Boolean = false,
    val reported: Boolean = false
) {
    val place: Int get() = results.indexOfFirst { it.isMe } + 1
    val points: Int get() = if (forfeited) 0 else League.matchPoints(myCorrect, place, myTimeMs, grade)
    val coins: Int get() = if (forfeited) 0 else League.PAYOUT[(place - 1).coerceIn(0, 4)]
    /** Số câu bạn thi đã làm xong tại thời điểm hiện tại. */
    fun botDone(run: BotRun): Int = if (phase == MatchPhase.RESULT) run.correct.size else run.doneAtMs.count { it <= elapsedMs }
}

/** Một trận giải đấu 10 câu với 4 bạn cùng bảng. */
class TournamentViewModel(
    private val builder: TournamentQuestionBuilder = TournamentQuestionBuilder()
) : ViewModel() {

    private val _state = MutableStateFlow(MatchState())
    val state: StateFlow<MatchState> = _state.asStateFlow()
    private var ticker: Job? = null
    private var job: Job? = null

    fun start(child: ChildProfile, league: LeagueState, progress: Map<String, Int>) {
        ticker?.cancel(); job?.cancel()
        val rng = Random(System.nanoTime())
        val opponents = league.bots.shuffled(rng)
        _state.value = MatchState(matchId = System.currentTimeMillis(), grade = child.grade, me = child, opponents = opponents)
        job = viewModelScope.launch {
            val built = launch {
                val qs = withContext(Dispatchers.Default) { builder.build(child.grade, { progress[it] ?: 0 }, League.QUESTIONS) }
                val hard = qs.indices.map { it % 2 == 1 }
                val runs = opponents.map { League.simulate(it, league.rating, hard, child.grade, rng) }
                _state.update { it.copy(questions = qs, runs = runs) }
            }
            // Màn tìm bạn: từng bạn lần lượt vào phòng
            for (i in 1..opponents.size) {
                delay(450L + rng.nextLong(500))
                _state.update { it.copy(found = i) }
            }
            built.join()
            delay(700)
            _state.update { it.copy(phase = MatchPhase.PLAYING) }
            ticker = launch {
                while (_state.value.phase == MatchPhase.PLAYING) {
                    delay(100)
                    _state.update { it.copy(elapsedMs = it.elapsedMs + 100) }
                }
            }
        }
    }

    fun answer(option: String) {
        val s = _state.value
        if (s.phase != MatchPhase.PLAYING || s.feedback != null) return
        val q = s.questions.getOrNull(s.index) ?: return
        val ok = option == q.problem.steps.first().expectedAnswer
        _state.update { it.copy(answers = it.answers + (it.index to option), myCorrect = it.myCorrect + if (ok) 1 else 0, feedback = option to ok) }
        viewModelScope.launch {
            delay(650)
            val cur = _state.value
            if (cur.index >= cur.questions.lastIndex) finish()
            else _state.update { it.copy(index = it.index + 1, feedback = null) }
        }
    }

    private fun finish() {
        ticker?.cancel()
        val s = _state.value
        val me = MatchResult(s.me?.name ?: "Con", true, s.myCorrect, s.elapsedMs, null)
        val results = League.rank(s.runs.map { MatchResult(it.bot.name, false, it.totalCorrect, it.totalMs, it.bot) } + me)
        _state.update { it.copy(phase = MatchPhase.RESULT, results = results, myTimeMs = s.elapsedMs, feedback = null) }
    }

    /** Bỏ cuộc giữa trận: xếp cuối, không có điểm và xu. */
    fun forfeit() {
        ticker?.cancel(); job?.cancel()
        val s = _state.value
        val others = League.rank(s.runs.map { MatchResult(it.bot.name, false, it.totalCorrect, it.totalMs, it.bot) })
        _state.update {
            it.copy(
                phase = MatchPhase.RESULT, forfeited = true, myTimeMs = s.elapsedMs, feedback = null,
                results = others + MatchResult(s.me?.name ?: "Con", true, s.myCorrect, s.elapsedMs, null)
            )
        }
    }

    fun markReported() = _state.update { it.copy(reported = true) }

    override fun onCleared() {
        ticker?.cancel(); job?.cancel()
        super.onCleared()
    }
}
