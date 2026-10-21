package com.example.toanhoc

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import com.example.toanhoc.data.LessonProgressStore
import com.example.toanhoc.data.local.database.AppDatabase
import com.example.toanhoc.domain.curriculum.Curricula
import com.example.toanhoc.domain.curriculum.LessonStage
import com.example.toanhoc.ui.lecture.LessonLectureScreen
import com.example.toanhoc.ui.exam.SemesterExamScreen
import com.example.toanhoc.ui.exam.SemesterExamViewModel
import com.example.toanhoc.domain.curriculum.stages
import com.example.toanhoc.ui.lesson.LessonPathScreen
import com.example.toanhoc.data.repository.KnowledgeRepositoryImpl
import com.example.toanhoc.domain.model.AcademicTier
import com.example.toanhoc.domain.model.ExamMode
import com.example.toanhoc.domain.model.SessionPhase
import com.example.toanhoc.domain.usecase.SessionProgressSummary
import com.example.toanhoc.ui.dashboard.AcademicDashboardScreen
import com.example.toanhoc.ui.dashboard.DashboardViewModel
import com.example.toanhoc.ui.exam.AssessmentExamScreen
import com.example.toanhoc.ui.exam.AssessmentExamViewModel
import com.example.toanhoc.ui.session.DeliberatePracticeScreen
import com.example.toanhoc.ui.session.Session900sViewModel
import com.example.toanhoc.ui.session.SessionSummaryScreen
import com.example.toanhoc.ui.theme.AcademicTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import com.example.toanhoc.data.family.Badges
import com.example.toanhoc.data.family.CoinRules
import com.example.toanhoc.ui.family.CoinToastHost
import com.example.toanhoc.ui.tournament.TournamentViewModel
import com.example.toanhoc.ui.tournament.TournamentMatchScreen
import com.example.toanhoc.ui.tournament.MatchPhase
import com.example.toanhoc.data.family.League
import com.example.toanhoc.data.family.EntryKind
import androidx.compose.foundation.layout.Box
import com.example.toanhoc.data.family.FamilyStore
import com.example.toanhoc.data.family.MistakeEntry
import com.example.toanhoc.ui.dashboard.ChildHub
import com.example.toanhoc.ui.family.ChildEditorScreen
import com.example.toanhoc.ui.family.GateMode
import com.example.toanhoc.ui.family.ParentActions
import com.example.toanhoc.ui.family.ParentGateScreen
import com.example.toanhoc.ui.family.ParentHomeScreen
import com.example.toanhoc.ui.family.ProfilePickerScreen
import com.example.toanhoc.ui.family.RoadmapScreen
import com.example.toanhoc.ui.family.ChallengeTestScreen
import com.example.toanhoc.ui.family.ChallengeReportScreen
import com.example.toanhoc.domain.challenge.ChallengeBuilder
import kotlinx.coroutines.delay

enum class AppScreen {
    PROFILE_PICKER,
    PARENT_GATE,
    PARENT_HOME,
    CHILD_EDITOR,
    DASHBOARD,
    SESSION_PRACTICE,
    SESSION_SUMMARY,
    EXAM,
    LESSON_PATH,
    LESSON_LEARN,
    SEMESTER_EXAM,
    TOURNAMENT_MATCH,
    /** Bố mẹ chọn lộ trình học. */
    ROADMAP,
    /** Con làm thử thách vượt bài. */
    CHALLENGE,
    /** Bố mẹ xem kết quả thử thách. */
    CHALLENGE_REPORT
}

/** Sau khi nhập đúng mã phụ huynh thì đi đâu. */
enum class GateTarget { PARENT_HOME, ADD_CHILD, MORE_TIME, FIRST_SETUP }

/** Màn đang học (tính thời gian học trong ngày). */
private val LEARNING_SCREENS = setOf(AppScreen.SESSION_PRACTICE, AppScreen.LESSON_LEARN, AppScreen.SEMESTER_EXAM, AppScreen.EXAM, AppScreen.LESSON_PATH, AppScreen.TOURNAMENT_MATCH, AppScreen.CHALLENGE)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Không phủ lớp xám hệ thống lên thanh điều hướng của app
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Disable OS soft input keyboard completely per AGENTS.md requirement
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // Initialize Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = KnowledgeRepositoryImpl(
            dao = database.mathAcademicDao(),
            profileDao = database.studentProfileDao()
        )

        val dashboardViewModel = DashboardViewModel(repository = repository)
        val sessionViewModel = Session900sViewModel(repository = repository)
        val examViewModel = AssessmentExamViewModel(repository = repository)
        val semesterExamViewModel = SemesterExamViewModel()
        val family = FamilyStore(applicationContext)
        val tournamentViewModel = TournamentViewModel()

        // Câu sai lần đầu trong lúc luyện → nhật ký "dạng hay sai" của con đang học
        sessionViewModel.onFirstWrongAnswer = { lessonId, nodeId, statement, picked, expected ->
            family.activeChildId.value?.let { cid ->
                family.recordMistake(MistakeEntry(cid, System.currentTimeMillis(), lessonId, nodeId, statement, picked, expected))
            }
        }

        setContent {
            AcademicTheme {
                val dashboardUiState by dashboardViewModel.uiState.collectAsState()
                val studentProfile by dashboardViewModel.profileState.collectAsState()
                val sessionUiState by sessionViewModel.uiState.collectAsState()
                val examUiState by examViewModel.uiState.collectAsState()

                var currentScreen by remember { mutableStateOf(AppScreen.PROFILE_PICKER) }
                var dashboardTab by remember { mutableStateOf(0) }
                val context = LocalContext.current
                // "Logo âm thanh" khi mở ứng dụng (một lần mỗi lần mở)
                LaunchedEffect(Unit) {
                    com.example.toanhoc.ui.sound.SoundEffectsManager.init(context)
                    delay(700)
                    com.example.toanhoc.ui.sound.SoundEffectsManager.play(context, com.example.toanhoc.ui.sound.Sfx.APP_OPEN)
                }
                val familyData by family.data.collectAsState()
                val activeChildId by family.activeChildId.collectAsState()
                val activeChild = familyData.child(activeChildId)
                // Tiến độ bài theo từng con
                val lessonProgress = remember(activeChildId) { LessonProgressStore(context, activeChildId) }
                val progressMap by lessonProgress.snapshot.collectAsState()

                // Cổng phụ huynh & màn sửa hồ sơ
                var gateMode by remember { mutableStateOf(GateMode.VERIFY) }
                var gateTarget by remember { mutableStateOf(GateTarget.PARENT_HOME) }
                var gateBack by remember { mutableStateOf(AppScreen.PROFILE_PICKER) }
                var editorChildId by remember { mutableStateOf<String?>(null) }
                var editorBack by remember { mutableStateOf(AppScreen.PARENT_HOME) }
                var firstSetup by remember { mutableStateOf(false) }

                fun openGate(target: GateTarget, back: AppScreen, mode: GateMode = if (family.hasPin()) GateMode.VERIFY else GateMode.CREATE) {
                    gateTarget = target; gateBack = back; gateMode = mode
                    currentScreen = AppScreen.PARENT_GATE
                }

                fun selectChild(id: String) {
                    val child = family.data.value.child(id) ?: return
                    family.setActive(id)
                    // Bù huy hiệu cho tiến độ đã có từ trước
                    val snap = LessonProgressStore(context, id).snapshotNow()
                    val ever = snap.filterKeys { it.startsWith("ever:") }.values
                    family.ensureAtLeast(id, Badges.Keys.LESSONS, ever.count { it >= 4 })
                    family.ensureAtLeast(id, Badges.Keys.HARD, ever.count { it >= 3 })
                    family.ensureAtLeast(id, Badges.Keys.EXAM, snap.filterKeys { it.startsWith("exam:") }.values.count { it >= 80 })
                    dashboardViewModel.updateProfile(studentProfile.copy(fullName = child.name, birthYear = child.birthYear, selectedGrade = child.grade))
                    dashboardViewModel.selectGrade(child.grade)
                    dashboardTab = 0
                    currentScreen = AppScreen.DASHBOARD
                }

                /** Vào một trận giải đấu (vé miễn phí hoặc trả phí). */
                fun startLeagueMatch() {
                    val cid = activeChildId ?: return
                    if (family.startMatch(cid) == null) return
                    val c = family.data.value.child(cid) ?: return
                    tournamentViewModel.start(c, c.league, lessonProgress.snapshotNow())
                    currentScreen = AppScreen.TOURNAMENT_MATCH
                }

                // Lộ trình & thử thách vượt bài
                var roadmapChildId by remember { mutableStateOf<String?>(null) }
                var challengeId by remember { mutableStateOf<String?>(null) }
                var reportChallengeId by remember { mutableStateOf<String?>(null) }
                var reportBack by remember { mutableStateOf(AppScreen.PARENT_HOME) }

                /** Qua một bước của bài: lưu tiến độ, lần đầu qua thì cộng xu. */
                fun completeStage(lessonId: String, stage: LessonStage) {
                    // Bài đã "thành thạo qua thử thách": con luyện thêm không cộng xu, không tính huy hiệu
                    if (lessonProgress.isMastered(lessonId)) return
                    val res = lessonProgress.markDone(lessonId, stage)
                    val cid = activeChildId ?: return


                    val number = Curricula.findLesson(lessonId)?.second?.number
                    if (res.firstTime) {
                        val (coins, name) = when (stage) {
                            LessonStage.LEARN -> CoinRules.LEARN to "Bài giảng"
                            LessonStage.EASY -> CoinRules.EASY to "Dạng 1"
                            LessonStage.HARD -> CoinRules.HARD to "Dạng 2"
                            LessonStage.REVIEW -> CoinRules.REVIEW to "Ôn tập"
                        }
                        family.award(cid, coins, "$name · Bài ${number ?: ""}".trim())
                    }
                    if (res.relearnFinished) {
                        family.award(cid, CoinRules.RELEARN_DONE, "Học lại xong Bài ${number ?: ""}".trim())
                        family.bump(cid, Badges.Keys.RELEARN)
                    }
                    if (res.firstTime && stage == LessonStage.HARD) family.bump(cid, Badges.Keys.HARD)
                    if (res.firstTime && stage == LessonStage.REVIEW) family.bump(cid, Badges.Keys.LESSONS)
                }

                // Đếm thời gian học khi con đang ở màn học (chỉ khi ứng dụng đang mở trên màn hình)
                LaunchedEffect(currentScreen, activeChildId) {
                    val cid = activeChildId
                    if (cid != null && currentScreen in LEARNING_SCREENS) {
                        while (true) {
                            delay(15_000)
                            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) family.addStudySeconds(cid, 15)
                        }
                    }
                }
                val semesterExamState by semesterExamViewModel.state.collectAsState()
                val matchState by tournamentViewModel.state.collectAsState()
                // Bài trong sách đang mở (màn hình lộ trình / học trước / làm bài)
                var openLessonId by remember { mutableStateOf<String?>(null) }

                /** Thoát khỏi phiên làm bài: về lộ trình bài nếu đang học theo sách, không thì về trang chủ. */
                fun leaveSession() {
                    dashboardViewModel.loadDashboardData()
                    currentScreen = if (sessionUiState.lessonId != null && openLessonId != null) AppScreen.LESSON_PATH else AppScreen.DASHBOARD
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                  Box(Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        AppScreen.PROFILE_PICKER -> {
                            ProfilePickerScreen(
                                data = familyData,
                                onPickChild = { selectChild(it) },
                                onAddChild = { openGate(GateTarget.ADD_CHILD, AppScreen.PROFILE_PICKER) },
                                onParent = { openGate(GateTarget.PARENT_HOME, AppScreen.PROFILE_PICKER) },
                                onStartSetup = { openGate(GateTarget.FIRST_SETUP, AppScreen.PROFILE_PICKER) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.PARENT_GATE -> {
                            ParentGateScreen(
                                store = family,
                                mode = gateMode,
                                purpose = when (gateTarget) {
                                    GateTarget.ADD_CHILD -> "Nhập mã PIN để thêm hồ sơ con"
                                    GateTarget.MORE_TIME -> "Nhập mã PIN để cho con học thêm 15 phút"
                                    else -> "Nhập mã PIN để xem báo cáo, phần thưởng và cài đặt"
                                },
                                onSuccess = {
                                    when {
                                        gateMode == GateMode.CHANGE -> currentScreen = AppScreen.PARENT_HOME
                                        gateTarget == GateTarget.MORE_TIME -> {
                                            activeChildId?.let { family.grantBonusMinutes(it, 15) }
                                            currentScreen = AppScreen.DASHBOARD
                                        }
                                        gateTarget == GateTarget.ADD_CHILD || gateTarget == GateTarget.FIRST_SETUP || familyData.children.isEmpty() -> {
                                            firstSetup = familyData.children.isEmpty()
                                            editorChildId = null
                                            editorBack = if (firstSetup) AppScreen.PROFILE_PICKER else gateBack
                                            currentScreen = AppScreen.CHILD_EDITOR
                                        }
                                        else -> currentScreen = AppScreen.PARENT_HOME
                                    }
                                },
                                onCancel = { currentScreen = gateBack },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.CHILD_EDITOR -> {
                            val editing = familyData.child(editorChildId)
                            ChildEditorScreen(
                                initial = editing,
                                title = when {
                                    editing != null -> "Sửa hồ sơ của ${editing.name}"
                                    firstSetup -> "Thông tin của con"
                                    else -> "Thêm hồ sơ con"
                                },
                                saveText = if (editing != null) "Lưu thay đổi" else "Tạo hồ sơ",
                                onSave = { name, birthYear, grade, gender, skin ->
                                    if (editing == null) {
                                        family.addChild(name, birthYear, grade, gender, skin)
                                        // Thiết lập lần đầu xong → sang khu phụ huynh (báo cáo, phần thưởng)
                                        currentScreen = if (firstSetup) AppScreen.PARENT_HOME else editorBack
                                        firstSetup = false
                                    } else {
                                        family.updateChild(editing.copy(name = name, birthYear = birthYear, grade = grade))
                                        if (editing.id == activeChildId && editing.grade != grade) dashboardViewModel.selectGrade(grade)
                                        currentScreen = editorBack
                                    }
                                },
                                onCancel = { currentScreen = editorBack },
                                onDelete = editing?.let { c ->
                                    {
                                        if (c.id == activeChildId) lessonProgress.clearAll() else LessonProgressStore(context, c.id).clearAll()
                                        family.deleteChild(c.id)
                                        currentScreen = AppScreen.PARENT_HOME
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.PARENT_HOME -> {
                            ParentHomeScreen(
                                data = familyData,
                                actions = ParentActions(
                                    progressOf = { id -> if (id == activeChildId) lessonProgress.snapshotNow() else LessonProgressStore(context, id).snapshotNow() },
                                    assignRelearn = { cid, lid ->
                                        if (cid == activeChildId) lessonProgress.resetForRelearn(listOf(lid))
                                        else LessonProgressStore(context, cid).resetForRelearn(listOf(lid))
                                    },
                                    editChild = { id -> editorChildId = id; editorBack = AppScreen.PARENT_HOME; currentScreen = AppScreen.CHILD_EDITOR },
                                    addChild = { editorChildId = null; editorBack = AppScreen.PARENT_HOME; currentScreen = AppScreen.CHILD_EDITOR },
                                    changePin = { openGate(GateTarget.PARENT_HOME, AppScreen.PARENT_HOME, GateMode.CHANGE) },
                                    setLimit = { id, m -> family.setDailyLimit(id, m) },
                                    addReward = { t, e, c, cid -> family.addReward(t, e, c, cid) },
                                    deleteReward = { family.deleteReward(it) },
                                    deliver = { family.markDelivered(it) },
                                    refund = { family.cancelRequest(it) },
                                    exit = { currentScreen = AppScreen.PROFILE_PICKER },
                                    openRoadmap = { id -> roadmapChildId = id; currentScreen = AppScreen.ROADMAP },
                                    openChallengeReport = { id -> reportChallengeId = id; reportBack = AppScreen.PARENT_HOME; currentScreen = AppScreen.CHALLENGE_REPORT },
                                    startChallengeNow = { id ->
                                        familyData.pendingChallenge(id)?.let { ch ->
                                            selectChild(id); challengeId = ch.id; currentScreen = AppScreen.CHALLENGE
                                        }
                                    }
                                ),
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.DASHBOARD -> {
                            val child = activeChild
                            if (child == null) {
                                LaunchedEffect(Unit) { currentScreen = AppScreen.PROFILE_PICKER }
                                return@Scaffold
                            }
                            BackHandler { currentScreen = AppScreen.PROFILE_PICKER }
                            AcademicDashboardScreen(
                                uiState = dashboardUiState,
                                studentProfile = studentProfile,
                                onSelectGrade = { selectedGrade ->
                                    dashboardViewModel.selectGrade(selectedGrade)
                                },
                                onStartSession = { targetNodeId ->
                                    sessionViewModel.startSession(dashboardUiState.selectedGrade, targetNodeId)
                                    currentScreen = AppScreen.SESSION_PRACTICE
                                },
                                onStartLevelUpExam = {
                                    examViewModel.startExam(
                                        mode = ExamMode.LEVEL_UP_EXAM,
                                        grade = dashboardUiState.selectedGrade,
                                        targetTier = AcademicTier.GIOI
                                    )
                                    currentScreen = AppScreen.EXAM
                                },
                                onStartParentExam = {
                                    examViewModel.startExam(
                                        mode = ExamMode.PARENT_VERIFICATION_EXAM,
                                        grade = dashboardUiState.selectedGrade,
                                        targetTier = AcademicTier.fromString(studentProfile.currentAcademicTier)
                                    )
                                    currentScreen = AppScreen.EXAM
                                },
                                onUpdateProfile = { updatedProfile ->
                                    dashboardViewModel.updateProfile(updatedProfile)
                                },
                                // chỉ chừa phần trên: thanh menu tự kéo nền trắng xuống dưới thanh cử chỉ của hệ thống
                                modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                                lessonProgress = progressMap,
                                onOpenLesson = { id ->
                                    openLessonId = id
                                    currentScreen = AppScreen.LESSON_PATH
                                },
                                tab = dashboardTab,
                                onTabChange = { dashboardTab = it },
                                onOpenExam = { semester ->
                                    semesterExamViewModel.start(dashboardUiState.selectedGrade, semester)
                                    currentScreen = AppScreen.SEMESTER_EXAM
                                },
                                hub = ChildHub(
                                    data = familyData,
                                    child = child,
                                    minutesLeft = family.minutesLeftToday(child, familyData),
                                    onBuy = { family.buy(child.id, it) },
                                    onEquip = { family.equip(child.id, it) },
                                    onRequestReward = { family.requestReward(child.id, it) },
                                    onClaimBadge = { b, t -> family.claimBadge(child.id, b, t) },
                                    onOpenLeague = { family.league(child.id) },
                                    onSetGender = { g -> family.setGender(child.id, g) },
                                    onPlayLeague = { startLeagueMatch() },
                                    onAckLeagueWeek = { family.ackLeagueWeek(child.id) },
                                    onSwitchProfile = { currentScreen = AppScreen.PROFILE_PICKER },
                                    onAskMoreTime = { openGate(GateTarget.MORE_TIME, AppScreen.DASHBOARD) },
                                    challenge = familyData.pendingChallenge(child.id),
                                    onStartChallenge = {
                                        familyData.pendingChallenge(child.id)?.let { challengeId = it.id; currentScreen = AppScreen.CHALLENGE }
                                    }
                                )
                            )
                        }

                        AppScreen.ROADMAP -> {
                            val rc = familyData.child(roadmapChildId)
                            if (rc == null) {
                                LaunchedEffect(Unit) { currentScreen = AppScreen.PARENT_HOME }
                                return@Scaffold
                            }
                            val rcProgress = remember(rc.id, familyData.challenges, progressMap) {
                                if (rc.id == activeChildId) lessonProgress.snapshotNow() else LessonProgressStore(context, rc.id).snapshotNow()
                            }
                            RoadmapScreen(
                                child = rc,
                                progress = rcProgress,
                                history = familyData.challengesOf(rc.id),
                                pending = familyData.pendingChallenge(rc.id),
                                onCreate = { kept, plans, startNow ->
                                    val curriculum = Curricula.forGrade(rc.grade)
                                    if (curriculum != null && plans.isNotEmpty()) {
                                        val id = family.newChallengeId()
                                        val ch = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                            ChallengeBuilder.build(id, rc.id, curriculum, kept, plans, System.currentTimeMillis())
                                        }
                                        family.saveChallenge(ch)
                                        if (startNow) { selectChild(rc.id); challengeId = ch.id; currentScreen = AppScreen.CHALLENGE }
                                    }
                                },
                                onStartPending = {
                                    familyData.pendingChallenge(rc.id)?.let { ch -> selectChild(rc.id); challengeId = ch.id; currentScreen = AppScreen.CHALLENGE }
                                },
                                onCancelPending = { familyData.pendingChallenge(rc.id)?.let { family.cancelChallenge(it.id) } },
                                onOpenReport = { id -> reportChallengeId = id; reportBack = AppScreen.ROADMAP; currentScreen = AppScreen.CHALLENGE_REPORT },
                                onBack = { currentScreen = AppScreen.PARENT_HOME },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.CHALLENGE -> {
                            val ch = familyData.challenges.firstOrNull { it.id == challengeId }
                            val c = familyData.child(ch?.childId)
                            if (ch == null || c == null) {
                                LaunchedEffect(Unit) { currentScreen = AppScreen.DASHBOARD }
                                return@Scaffold
                            }
                            ChallengeTestScreen(
                                child = c,
                                challenge = ch,
                                onAnswer = { u, q, picked, ms -> family.answerChallenge(ch.id, u, q, picked, ms) },
                                onFinish = {
                                    // Duyệt: bài trong mảng đạt → thành thạo (không xu, không huy hiệu)
                                    val latest = family.data.value.challenges.firstOrNull { it.id == ch.id }
                                    if (latest != null && !latest.finished) {
                                        family.finishChallenge(latest.id)
                                        val store = if (latest.childId == activeChildId) lessonProgress else LessonProgressStore(context, latest.childId)
                                        store.markMastered(latest.masteredLessonIds)
                                    }
                                },
                                onExit = { dashboardViewModel.loadDashboardData(); dashboardTab = 0; currentScreen = AppScreen.DASHBOARD },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.CHALLENGE_REPORT -> {
                            val ch = familyData.challenges.firstOrNull { it.id == reportChallengeId }
                            val c = familyData.child(ch?.childId)
                            if (ch == null || c == null) {
                                LaunchedEffect(Unit) { currentScreen = AppScreen.PARENT_HOME }
                                return@Scaffold
                            }
                            LaunchedEffect(ch.id) { if (!ch.seenByParent) family.markChallengeSeen(ch.id) }
                            ChallengeReportScreen(
                                child = c,
                                challenge = ch,
                                onBack = { currentScreen = reportBack },
                                onEditRoadmap = { roadmapChildId = c.id; currentScreen = AppScreen.ROADMAP },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.TOURNAMENT_MATCH -> {
                            val cid = activeChildId
                            // Hết trận: cộng điểm giải, xu theo hạng; câu sai đưa vào báo cáo phụ huynh
                            LaunchedEffect(matchState.matchId, matchState.phase) {
                                val st = matchState
                                if (cid != null && st.phase == MatchPhase.RESULT && !st.reported) {
                                    family.finishMatch(cid, st.place, st.myCorrect, st.answers.size, st.points, st.forfeited)
                                    val now = System.currentTimeMillis()
                                    st.answers.forEach { (i, picked) ->
                                        val q = st.questions.getOrNull(i) ?: return@forEach
                                        val exp = q.problem.steps.first().expectedAnswer
                                        if (picked != exp) family.recordMistake(MistakeEntry(cid, now, q.lessonId, q.problem.nodeId, q.problem.statementVi, picked, exp, "league"))
                                    }
                                    tournamentViewModel.markReported()
                                }
                            }
                            val child = familyData.child(cid)
                            val canAgain = child != null && League.entry(League.roll(child.league, child.id, java.time.LocalDateTime.now()), child.coins).let { it == EntryKind.FREE || it == EntryKind.PAID }
                            TournamentMatchScreen(
                                state = matchState,
                                canPlayAgain = canAgain,
                                onAnswer = tournamentViewModel::answer,
                                onForfeit = tournamentViewModel::forfeit,
                                onPlayAgain = { startLeagueMatch() },
                                onExit = { dashboardTab = 2; currentScreen = AppScreen.DASHBOARD },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.SEMESTER_EXAM -> {
                            // Nộp bài: lưu điểm; chưa đạt 80% → các bài có câu sai phải học lại từ đầu
                            LaunchedEffect(semesterExamState.submitted) {
                                if (semesterExamState.submitted && semesterExamState.questions.isNotEmpty()) {
                                    val st = semesterExamState
                                    val before = lessonProgress.recordExam(st.grade, st.semester, st.percent)
                                    if (!st.passed) {
                                        lessonProgress.resetForRelearn(st.wrongLessonIds)
                                    }
                                    activeChildId?.let { cid ->
                                        if (st.passed && before < 80) {
                                            family.award(cid, CoinRules.EXAM_PASS, "Đạt kiểm tra học kì ${st.semester}")
                                            family.bump(cid, Badges.Keys.EXAM)
                                        }
                                        val now = System.currentTimeMillis()
                                        st.questions.forEachIndexed { i, q ->
                                            val step = q.problem.steps.first()
                                            val mine = st.answers[i]
                                            if (mine != step.expectedAnswer) {
                                                family.recordMistake(MistakeEntry(cid, now, q.lessonId, q.problem.nodeId, q.problem.statementVi, mine ?: "(bỏ trống)", step.expectedAnswer, "exam"))
                                            }
                                        }
                                    }
                                }
                            }
                            SemesterExamScreen(
                                state = semesterExamState,
                                onAnswer = semesterExamViewModel::answer,
                                onGoTo = semesterExamViewModel::goTo,
                                onSubmit = semesterExamViewModel::submit,
                                onExit = { currentScreen = AppScreen.DASHBOARD },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.LESSON_PATH -> {
                            val found = openLessonId?.let { Curricula.findLesson(it) }
                            if (found == null) {
                                currentScreen = AppScreen.DASHBOARD
                            } else {
                                val (curriculum, lesson) = found
                                LessonPathScreen(
                                    curriculum = curriculum,
                                    lesson = lesson,
                                    completed = progressMap[lesson.id] ?: 0,
                                    coinsFor = { stage ->
                                        val ever = progressMap[com.example.toanhoc.domain.curriculum.LessonKeys.ever(lesson.id)] ?: 0
                                        if (ever > stage.ordinal) null else when (stage) {
                                            LessonStage.LEARN -> CoinRules.LEARN
                                            LessonStage.EASY -> CoinRules.EASY
                                            LessonStage.HARD -> CoinRules.HARD
                                            LessonStage.REVIEW -> CoinRules.REVIEW
                                        }
                                    },
                                    onBack = { currentScreen = AppScreen.DASHBOARD },
                                    onStartStage = { stage ->
                                        if (stage == LessonStage.LEARN) {
                                            currentScreen = AppScreen.LESSON_LEARN
                                        } else {
                                            sessionViewModel.startLessonSession(lesson.id, stage)
                                            currentScreen = AppScreen.SESSION_PRACTICE
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppScreen.LESSON_LEARN -> {
                            val found = openLessonId?.let { Curricula.findLesson(it) }
                            if (found == null) {
                                currentScreen = AppScreen.DASHBOARD
                            } else {
                                val (lectureCurriculum, lesson) = found
                                LessonLectureScreen(
                                    curriculum = lectureCurriculum,
                                    lesson = lesson,
                                    watchedBefore = (progressMap[com.example.toanhoc.domain.curriculum.LessonKeys.ever(lesson.id)] ?: 0) >= 1,
                                    onClose = { currentScreen = AppScreen.LESSON_PATH },
                                    onFinish = {
                                        completeStage(lesson.id, LessonStage.LEARN)
                                        sessionViewModel.startLessonSession(lesson.id, LessonStage.EASY)
                                        currentScreen = AppScreen.SESSION_PRACTICE
                                    },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppScreen.SESSION_PRACTICE -> {
                            if (sessionUiState.currentPhase == SessionPhase.SUMMARY || sessionUiState.isSessionFinished) {
                                currentScreen = AppScreen.SESSION_SUMMARY
                            } else if (sessionUiState.lessonId?.let { com.example.toanhoc.domain.curriculum.ThinkingCurricula.isThinking(it) } == true) {
                                val thinkingLesson = sessionUiState.lessonId?.let { Curricula.findLesson(it) }?.second
                                com.example.toanhoc.ui.thinking.ThinkingPracticeScreen(
                                    uiState = sessionUiState,
                                    lessonTitle = thinkingLesson?.title ?: "Toán tư duy",
                                    childName = activeChild?.name ?: "",
                                    onSelectOption = sessionViewModel::submitOptionChoice,
                                    onPauseToggle = sessionViewModel::onPauseToggle,
                                    onSaveAndExit = { sessionViewModel.onSaveAndExitSession { leaveSession() } },
                                    onRevealSolution = sessionViewModel::revealSolution,
                                    onContinue = sessionViewModel::continueAfterReveal,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            } else {
                                DeliberatePracticeScreen(
                                    uiState = sessionUiState,
                                    onSelectOption = sessionViewModel::submitOptionChoice,
                                    onPauseToggle = sessionViewModel::onPauseToggle,
                                    onSaveAndExit = {
                                        sessionViewModel.onSaveAndExitSession { leaveSession() }
                                    },
                                    onOpenTheory = { sessionViewModel.onToggleBottomSheet(true) },
                                    onCloseTheory = { sessionViewModel.onToggleBottomSheet(false) },
                                    onStepBack = sessionViewModel::onStepBackOrUndo,
                                    onDontKnowOrSkip = sessionViewModel::onDontKnowOrSkipClicked,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        AppScreen.EXAM -> {
                            AssessmentExamScreen(
                                uiState = examUiState,
                                onAnswerInput = examViewModel::onAnswerInput,
                                onPreviousProblem = examViewModel::onPreviousProblem,
                                onNextProblem = examViewModel::onNextProblem,
                                onSubmitExam = examViewModel::submitExam,
                                onExitExam = {
                                    dashboardViewModel.loadDashboardData()
                                    currentScreen = AppScreen.DASHBOARD
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppScreen.SESSION_SUMMARY -> {
                            BackHandler { leaveSession() }

                            // Qua bước của bài trong sách (≥ 80% đúng lần đầu) → lưu tiến độ, mở bước sau
                            LaunchedEffect(sessionUiState.lessonId, sessionUiState.lessonStage, sessionUiState.isRoundPassed) {
                                val id = sessionUiState.lessonId
                                val stage = sessionUiState.lessonStage?.let { runCatching { LessonStage.valueOf(it) }.getOrNull() }
                                if (id != null && stage != null && sessionUiState.isRoundPassed) {
                                    completeStage(id, stage)
                                }
                                // Đúng hết 12/12 câu ngay lần đầu → tính cho huy hiệu "Thần xạ thủ"
                                val total = sessionUiState.totalAnsweredCount
                                if (total >= 12 && sessionUiState.correctAnsweredCount >= total) {
                                    activeChildId?.let { family.bump(it, Badges.Keys.PERFECT) }
                                    // Dạng 2 đúng trọn → tính cho xếp loại "Thành thạo"
                                    if (id != null && stage == LessonStage.HARD) lessonProgress.recordPerfectHard(id)
                                }
                            }

                            val total = sessionUiState.totalAnsweredCount.coerceAtLeast(1)
                            val summary = SessionProgressSummary(
                                accuracyPercentage = (sessionUiState.correctAnsweredCount * 100) / total,
                                averageLatencyMs = sessionUiState.averageLatencyMs,
                                isAutomaticityAchieved = sessionUiState.averageLatencyMs in 1..2500L && ((sessionUiState.correctAnsweredCount * 100) / total) >= 80,
                                totalAnswered = sessionUiState.totalAnsweredCount,
                                correctCount = sessionUiState.correctAnsweredCount,
                                averageMastery = 0.85
                            )

                            val thinkingId = sessionUiState.lessonId?.takeIf { com.example.toanhoc.domain.curriculum.ThinkingCurricula.isThinking(it) }
                            if (thinkingId != null) {
                                com.example.toanhoc.ui.thinking.ThinkingReportScreen(
                                    lessonTitle = Curricula.findLesson(thinkingId)?.second?.title ?: "Toán tư duy",
                                    lessonKey = "$thinkingId:${sessionUiState.lessonStage}",
                                    stage = sessionUiState.lessonStage,
                                    childName = activeChild?.name ?: "",
                                    grade = sessionUiState.gradeLevel,
                                    correct = sessionUiState.correctAnsweredCount,
                                    total = sessionUiState.totalAnsweredCount,
                                    averageLatencyMs = sessionUiState.averageLatencyMs,
                                    passed = sessionUiState.isRoundPassed,
                                    perfectRuns = progressMap[com.example.toanhoc.domain.curriculum.LessonKeys.perfect(thinkingId)] ?: 0,
                                    onDone = { leaveSession() },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            } else SessionSummaryScreen(
                                summary = summary,
                                onRestartSession = { leaveSession() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                    // Thông báo "+10 xu" trượt xuống khi con được thưởng
                    CoinToastHost(family.awards, activeChildId, Modifier.padding(innerPadding))
                  }
                }
            }
        }
    }
}
