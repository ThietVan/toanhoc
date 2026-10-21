package com.example.toanhoc.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toanhoc.data.local.entity.StudentProfileEntity
import com.example.toanhoc.data.repository.KnowledgeRepository
import com.example.toanhoc.data.seed.CurriculumSeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ViewModel powering the Academic Dashboard Screen focused on Kid's Learning Experience.
 */
class DashboardViewModel(
    private val repository: KnowledgeRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _profileState = MutableStateFlow(StudentProfileEntity())
    val profileState: StateFlow<StudentProfileEntity> = _profileState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun selectGrade(grade: Int) {
        val updatedProfile = _profileState.value.copy(selectedGrade = grade)
        _profileState.value = updatedProfile
        _uiState.update { it.copy(selectedGrade = grade) }
        viewModelScope.launch {
            repository?.saveStudentProfile(updatedProfile)
        }
        loadDashboardData(grade)
    }

    fun updateProfile(profile: StudentProfileEntity) {
        _profileState.value = profile
        viewModelScope.launch {
            repository?.saveStudentProfile(profile)
        }
    }

    fun getKidFriendlyLessonName(nodeId: String, defaultTitle: String): String {
        return when (nodeId) {
            "G1_01_SPATIAL_SHAPES" -> "Bài 1: Vị trí không gian & Các khối hình"
            "G1_02_NUM_0_10_COMPARE" -> "Bài 2: Đếm và so sánh số từ 0 đến 10"
            "G1_03_SPLIT_COMBINE_10" -> "Bài 3: Tách và gộp các số phạm vi 10"
            "G1_04_ADD_SUB_10_MEANING" -> "Bài 4: Ý nghĩa phép cộng và phép trừ"
            "G1_05_PLACE_VALUE_100" -> "Bài 5: Số Chục và Số Đơn vị đến 100"
            "G1_06_ADD_SUB_100_NO_CARRY" -> "Bài 6: Cộng trừ không nhớ phạm vi 100"
            "G1_07_WORD_PROBLEMS_G1" -> "Bài 7: Giải toán có lời văn Lớp 1"
            "G1_08_MEASURE_LENGTH_CM" -> "Bài 8: Đo độ dài bằng xăng-ti-mét"
            "G1_09_TIME_CLOCK_CALENDAR" -> "Bài 9: Xem đồng hồ và đọc lịch ngày"
            "G1_10_PATTERNS_COUNTING" -> "Bài 10: Tìm quy luật dãy số cách đều"

            "G2_01_TERMS_AND_NUMBER_LINE" -> "Bài 1: Tên gọi thành phần phép tính & Tia số"
            "G2_02_MAKE_10_ADD_SUB" -> "Bài 2: Cộng trừ qua 10 phạm vi 20"
            "G2_03_ADD_SUB_100_WITH_CARRY" -> "Bài 3: Cộng trừ có nhớ phạm vi 100"
            "G2_04_WORD_MORE_LESS" -> "Bài 4: Bài toán Nhiều hơn và Ít hơn"
            "G2_05_MUL_DIV_2_5" -> "Bài 5: Bảng nhân 2, 5 và Bảng chia 2, 5"
            "G2_06_NUM_1000_ADD_SUB" -> "Bài 6: Phép tính trong phạm vi 1000"
            "G2_07_GEOMETRY_G2" -> "Bài 7: Đường gấp khúc và Hình tứ giác"
            "G2_08_MEASURE_DM_M_KM_KG_L" -> "Bài 8: Đơn vị đo dm, m, km, kg, lít"
            "G2_09_TIME_MONEY_G2" -> "Bài 9: Xem đồng hồ 15-30 phút & Tiền Việt Nam"
            "G2_10_STATS_PROBABILITY_G2" -> "Bài 10: Biểu đồ tranh & Khả năng xảy ra"

            "G3_01_MUL_DIV_TABLES_3_9" -> "Bài 1: Bảng nhân, chia từ 3 đến 9"
            "G3_02_FIND_UNKNOWN_COMPONENT" -> "Bài 2: Tìm thành phần chưa biết"
            "G3_03_TIMES_MORE_LESS_FRAC" -> "Bài 3: Gấp, giảm số lần & Phân số 1/n"
            "G3_04_DIVISION_WITH_REMAINDER" -> "Bài 4: Phép chia hết và Phép chia có dư"
            "G3_05_ORDER_OF_OPERATIONS" -> "Bài 5: Thứ tự thực hiện phép tính"
            "G3_06_NUM_100K_ROUNDING_ROMAN" -> "Bài 6: Số phạm vi 100.000 & Chữ số La Mã"
            "G3_07_TWO_STEP_WORD_PROBLEMS" -> "Bài 7: Giải bài toán bằng 2 bước tính"
            "G3_08_GEOMETRY_MIDPOINT_CIRCLE" -> "Bài 8: Trung điểm & Hình tròn (d = 2r)"
            "G3_09_PERIMETER_AREA_RECT_SQUARE" -> "Bài 9: Chu vi và Diện tích hình chữ nhật"
            "G3_10_MEASURE_STATS_G3" -> "Bài 10: Đơn vị mm, g, ml, °C & Bảng số liệu"

            "G4_01_MILLIONS_CLASS_ROUNDING" -> "Bài 1: Lớp triệu & Làm tròn số lớn"
            "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS" -> "Bài 2: Biểu thức chữ & Tính chất phép tính"
            "G4_03_MUL_DIV_2_DIGITS" -> "Bài 3: Nhân chia 2 chữ số & Dấu hiệu chia hết"
            "G4_04_AVERAGE_AND_UNIT_METHOD" -> "Bài 4: Trung bình cộng & Rút về đơn vị"
            "G4_05_SUM_DIFFERENCE_MODEL" -> "Bài 5: Tìm hai số biết Tổng và Hiệu"
            "G4_06_FRACTIONS_CONCEPT_COMPARE" -> "Bài 6: Rút gọn, Quy đồng & So sánh Phân số"
            "G4_07_FRACTIONS_FOUR_OPS" -> "Bài 7: Cộng và Trừ hai phân số khác mẫu"
            "G4_08_ANGLES_PARALLEL_SHAPES" -> "Bài 8: Góc nhọn, tù, bẹt & Hình bình hành, thoi"
            "G4_09_MEASURE_TON_M2_CENTURY" -> "Bài 9: Đơn vị Tấn, m², dm², mm² & Thế kỉ"
            "G4_10_BAR_CHART_PROBABILITY" -> "Bài 10: Biểu đồ cột (Bar Chart) & Xác suất"

            "G5_01_DECIMAL_FRACTIONS_MIXED" -> "Bài 1: Phân số thập phân và Hỗn số"
            "G5_02_DECIMALS_PLACE_ROUNDING" -> "Bài 2: Số thập phân & Đơn vị km², ha"
            "G5_03_DECIMALS_FOUR_OPS" -> "Bài 3: 4 phép tính Số thập phân"
            "G5_04_SUM_DIFF_RATIO_PROBLEMS" -> "Bài 4: Bài toán Tổng - Tỉ & Hiệu - Tỉ"
            "G5_05_PERCENTAGE_MAP_SCALE" -> "Bài 5: Tỉ số phần trăm (%) & Tỉ lệ bản đồ"
            "G5_06_TRIANGLE_TRAPEZOID_CIRCLE" -> "Bài 6: Diện tích Hình tam giác, Hình thang, Hình tròn"
            "G5_07_SURFACE_AREA_VOLUME_3D" -> "Bài 7: Diện tích toàn phần & Thể tích 3D"
            "G5_08_TIME_OPS_AND_MOTION" -> "Bài 8: Bài toán chuyển động ngược chiều"
            "G5_09_PIE_CHART_PROBABILITY_RATIO" -> "Bài 9: Biểu đồ hình quạt tròn (Pie Chart)"
            "G5_10_COMPREHENSIVE_HEURISTICS" -> "Bài 10: Toán tư duy nâng cao & Tổng hợp"
            else -> defaultTitle
        }
    }

    fun getKidFriendlyLessonGoal(nodeId: String, defaultSummary: String): String {
        return when (nodeId) {
            "G1_01_SPATIAL_SHAPES" -> "Bé sẽ nhận biết vị trí không gian và các khối hình phẳng!"
            "G1_02_NUM_0_10_COMPARE" -> "Bé sẽ thuộc thứ tự và so sánh thành thạo các số từ 0 đến 10!"
            "G1_03_SPLIT_COMBINE_10" -> "Bé sẽ thuộc các cặp số cộng lại bằng 10 để nhẩm siêu tốc!"
            "G1_04_ADD_SUB_10_MEANING" -> "Bé sẽ hiểu rõ khi nào cần cộng thêm và khi nào cần bớt đi!"
            "G1_05_PLACE_VALUE_100" -> "Bé sẽ phân tích đúng các hàng Chục và Đơn vị của số có 2 chữ số!"
            "G1_06_ADD_SUB_100_NO_CARRY" -> "Bé sẽ tự tin đặt tính dọc cộng trừ hai chữ số không nhớ!"
            "G1_07_WORD_PROBLEMS_G1" -> "Bé sẽ làm quen giải bài toán thực tế thêm bớt!"
            "G1_08_MEASURE_LENGTH_CM" -> "Bé sẽ biết dùng thước kẻ đo độ dài chính xác bằng cm!"

            "G2_03_ADD_SUB_100_WITH_CARRY" -> "Bé sẽ làm chủ phép cộng trừ có nhớ 1 chục phạm vi 100!"
            "G3_05_ORDER_OF_OPERATIONS" -> "Bé sẽ nhớ quy tắc nhân chia trước, cộng trừ sau!"
            "G4_01_MILLIONS_CLASS_ROUNDING" -> "Bé sẽ đọc viết thành thạo các số lớp triệu!"
            "G4_05_SUM_DIFFERENCE_MODEL" -> "Bé sẽ vẽ sơ đồ đoạn thẳng tìm hai số khi biết Tổng và Hiệu!"
            "G4_07_FRACTIONS_FOUR_OPS" -> "Bé sẽ quy đồng mẫu số và tính thành thạo cộng trừ phân số!"
            "G5_08_TIME_OPS_AND_MOTION" -> "Bé sẽ hiểu công thức tính quãng đường, vận tốc và thời gian!"
            else -> defaultSummary
        }
    }

    fun loadDashboardData(targetGrade: Int? = null) {
        viewModelScope.launch {
            try {
                if (repository != null) {
                    val savedProfile = repository.getStudentProfile()
                    val grade = targetGrade ?: savedProfile.selectedGrade

                    // Update profile state with current selectedGrade
                    val updatedProfile = savedProfile.copy(selectedGrade = grade)
                    _profileState.value = updatedProfile

                    // Save profile to DB if grade changed
                    if (savedProfile.selectedGrade != grade) {
                        repository.saveStudentProfile(updatedProfile)
                    }

                    // 1. Check if DB has all expected seed nodes for this grade
                    val expectedCountForGrade = CurriculumSeedData.seedNodes.count { it.grade == grade }
                    val existingNodes = repository.getNodesForGrade(grade)
                    if (existingNodes.size < expectedCountForGrade) {
                        repository.seedKnowledgeNodes(
                            CurriculumSeedData.seedNodes,
                            CurriculumSeedData.seedEdges
                        )
                    }

                    // 2. Load Grade Nodes and Masteries
                    val nodes = repository.getNodesForGrade(grade)
                    val reviewNodes = repository.getNodesDueForReviewOrWithMisconceptions()
                    val misconceptions = repository.getMisconceptionLogs()

                    val masteries = nodes.mapNotNull { node ->
                        repository.getStudentMastery(node.nodeId)
                    }.associateBy { it.nodeId }

                    // 3. Target Lesson & Goal: Strictly pick the FIRST unmastered node in sequential curriculum order
                    val targetNode = nodes.firstOrNull { node ->
                        masteries[node.nodeId]?.isMastered != true
                    } ?: nodes.firstOrNull()
                    val targetTitle = if (targetNode != null) getKidFriendlyLessonName(targetNode.nodeId, targetNode.titleVi) else ""
                    val targetGoal = if (targetNode != null) getKidFriendlyLessonGoal(targetNode.nodeId, targetNode.axiomSummaryVi) else ""

                    // 4. Review Status Message
                    val reviewMsg = if (reviewNodes.isNotEmpty()) {
                        val topReview = reviewNodes.first()
                        val topTitle = getKidFriendlyLessonName(topReview.nodeId, topReview.titleVi)
                        if (reviewNodes.size == 1) {
                            "Cần củng cố: $topTitle"
                        } else {
                            "Cần củng cố: $topTitle (và ${reviewNodes.size - 1} bài khác)"
                        }
                    } else {
                        "🌟 Tuyệt vời! Bé đã nhớ rất tốt các bài cũ, không có bài nào cần củng cố."
                    }

                    // 5. Topic Completion Percentage Formula: (Count of nodes with lastPracticedEpochMs > 0) / Total Nodes * 100%
                    val totalNodesCount = nodes.size.coerceAtLeast(1)
                    val practicedNodesCount = nodes.count { node ->
                        val m = masteries[node.nodeId]
                        m != null && m.lastPracticedEpochMs > 0L
                    }
                    val topicCompletionPct = (practicedNodesCount * 100) / totalNodesCount

                    // 6. Load Daily Session Log
                    val todayLog = repository.getDailySession(LocalDate.now().toString())

                    _uiState.update {
                        it.copy(
                            selectedGrade = grade,
                            gradeNodes = nodes,
                            phase2TargetNode = targetNode,
                            targetLessonTitleVi = targetTitle,
                            targetLessonGoalVi = targetGoal,
                            reviewStatusMessageVi = reviewMsg,
                            phase3ReviewNodesCount = reviewNodes.size,
                            topicCompletionPercentage = topicCompletionPct,
                            masteryMap = masteries,
                            misconceptionLogs = misconceptions,
                            isTodayCompleted = todayLog?.isCompleted == true,
                            todayCompletedSeconds = todayLog?.completedSeconds ?: 0,
                            todayAccuracyPercentage = ((todayLog?.phase1Accuracy ?: 0f) * 100).toInt(),
                            todayAvgLatencyMs = todayLog?.phase1AvgLatencyMs ?: 0L
                        )
                    }
                } else {
                    val grade = targetGrade ?: _uiState.value.selectedGrade
                    val nodes = CurriculumSeedData.seedNodes.filter { it.grade == grade }
                    val targetNode = nodes.firstOrNull()
                    _uiState.update {
                        it.copy(
                            selectedGrade = grade,
                            gradeNodes = nodes,
                            phase2TargetNode = targetNode,
                            targetLessonTitleVi = if (targetNode != null) getKidFriendlyLessonName(targetNode.nodeId, targetNode.titleVi) else "",
                            targetLessonGoalVi = if (targetNode != null) getKidFriendlyLessonGoal(targetNode.nodeId, targetNode.axiomSummaryVi) else "",
                            reviewStatusMessageVi = "🌟 Tuyệt vời! Bé đã nhớ rất tốt các bài cũ, không có bài nào cần củng cố.",
                            phase3ReviewNodesCount = 0,
                            topicCompletionPercentage = 0
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("DashboardViewModel", "Error loading dashboard data", e)
            }
        }
    }
}
