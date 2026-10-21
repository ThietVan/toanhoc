package com.example.toanhoc.data.seed

import com.example.toanhoc.data.local.entity.KnowledgeNodeEntity
import com.example.toanhoc.data.local.entity.PrerequisiteEdgeEntity

/**
 * Full Curriculum Seed Data for Primary Math Curriculum Vietnam (CT GDPT 2018 - Grades 1 to 5).
 *
 * Cấu trúc chuẩn hóa:
 *  - 50 Core Knowledge Nodes (10 nodes/lớp) bao phủ đủ 3 mạch kiến thức:
 *    (1) Số và Phép tính, (2) Hình học và Đo lường, (3) Một số yếu tố Thống kê và Xác suất,
 *    kết hợp mạch Giải toán có lời văn & Tư duy toán học.
 *  - Hệ số IRT (intrinsicDifficulty b in [-2.8, 3.2], discrimination a in [1.0, 1.55])
 *    được cân chỉnh theo độ phức tạp nhận thức thực tế của từng chủ đề.
 *  - Đồ thị tiên quyết (seedEdges) là DAG đa chiều: kết nối cả tiến trình nội cấp (Intra-grade)
 *    lẫn tiến trình dọc xuyên lớp theo đúng chuyên đề (Cross-grade Vertical Domain Links).
 */
object CurriculumSeedData {

    val seedNodes = listOf(
        // =========================================================================
        // GRADE 1 (10 CORE NODES)
        // =========================================================================
        KnowledgeNodeEntity(
            nodeId = "G1_01_SPATIAL_SHAPES",
            grade = 1,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Vị trí không gian & Hình phẳng, khối hình",
            axiomSummaryVi = "Nhận biết vị trí định hướng (trên - dưới, trái - phải, trước - sau, ở giữa); nhận dạng hình vuông, hình tròn, hình tam giác, hình chữ nhật và khối lập phương, khối hộp chữ nhật qua đồ vật thực tế.",
            intrinsicDifficulty = -2.8f,
            discrimination = 1.0f,
            generatorType = "G1_01_SPATIAL_SHAPES"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_02_NUM_0_10_COMPARE",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Đếm, so sánh & Thứ tự các số trong phạm vi 10",
            axiomSummaryVi = "Đếm, đọc, viết các số từ 0 đến 10; nhận biết số liền trước, số liền sau; so sánh lớn hơn, bé hơn, bằng nhau và sắp xếp dãy số theo thứ tự từ bé đến lớn hoặc ngược lại.",
            intrinsicDifficulty = -2.6f,
            discrimination = 1.05f,
            generatorType = "G1_02_NUM_0_10_COMPARE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_03_SPLIT_COMBINE_10",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Sơ đồ Tách - Gộp số trong phạm vi 10",
            axiomSummaryVi = "Hiểu cấu tạo số trong phạm vi 10 thông qua thao tác tách một số thành hai phần và gộp hai phần thành một số nguyên vẹn (nền tảng của phép cộng và phép trừ).",
            intrinsicDifficulty = -2.35f,
            discrimination = 1.15f,
            generatorType = "G1_03_SPLIT_COMBINE_10"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_04_ADD_SUB_10_MEANING",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Phép cộng, Phép trừ trong phạm vi 10",
            axiomSummaryVi = "Hiểu ý nghĩa phép cộng là thêm vào/gộp lại, phép trừ là bớt đi/tách ra; tính nhẩm cộng, trừ trong phạm vi 10 (kể cả phép tính với số 0) và tính giá trị dãy tính 2 bước từ trái sang phải.",
            intrinsicDifficulty = -2.1f,
            discrimination = 1.15f,
            generatorType = "G1_04_ADD_SUB_10_MEANING"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_05_PLACE_VALUE_100",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Các số đến 100: Cấu tạo Chục và Đơn vị",
            axiomSummaryVi = "Đọc, viết, so sánh các số đến 100; nhận biết số tròn chục, số có hai chữ số gồm hàng chục và hàng đơn vị (ab = a chục + b đơn vị) và thứ tự số trên tia số.",
            intrinsicDifficulty = -1.9f,
            discrimination = 1.15f,
            generatorType = "G1_05_PLACE_VALUE_100"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_06_ADD_SUB_100_NO_CARRY",
            grade = 1,
            domainCategory = "ARITHMETIC",
            titleVi = "Cộng, trừ không nhớ trong phạm vi 100",
            axiomSummaryVi = "Tính nhẩm cộng, trừ các số tròn chục; đặt tính rồi tính cộng, trừ không nhớ giữa số có hai chữ số với số có một hoặc hai chữ số theo cột dọc.",
            intrinsicDifficulty = -1.65f,
            discrimination = 1.2f,
            generatorType = "G1_06_ADD_SUB_100_NO_CARRY"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_07_WORD_PROBLEMS_G1",
            grade = 1,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Bài toán có lời văn 1 bước tính (Không nhớ)",
            axiomSummaryVi = "Nhận diện từ khóa thực tế (thêm, cho thêm, có tất cả, bớt đi, cho đi, còn lại) để viết phép tính cộng hoặc trừ không nhớ trong phạm vi 100.",
            intrinsicDifficulty = -1.45f,
            discrimination = 1.25f,
            generatorType = "G1_07_WORD_PROBLEMS_G1"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_08_MEASURE_LENGTH_CM",
            grade = 1,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Đo độ dài bằng xăng-ti-mét (cm)",
            axiomSummaryVi = "Nhận biết đơn vị đo độ dài xăng-ti-mét (cm); đọc đúng số đo độ dài băng giấy, bút chì trên thước thẳng có vạch chia cm và thực hiện phép cộng, trừ số đo độ dài.",
            intrinsicDifficulty = -1.55f,
            discrimination = 1.1f,
            generatorType = "G1_08_MEASURE_LENGTH_CM"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_09_TIME_CLOCK_CALENDAR",
            grade = 1,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Xem đồng hồ giờ đúng & Lịch ngày trong tuần",
            axiomSummaryVi = "Đọc giờ đúng trên mặt đồng hồ khi kim dài chỉ số 12; nhận biết một tuần lễ có 7 ngày, đọc thứ và ngày trên tờ lịch hằng ngày (hôm qua, hôm nay, ngày mai).",
            intrinsicDifficulty = -1.5f,
            discrimination = 1.1f,
            generatorType = "G1_09_TIME_CLOCK_CALENDAR"
        ),
        KnowledgeNodeEntity(
            nodeId = "G1_10_PATTERNS_COUNTING",
            grade = 1,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Quy luật dãy số, dãy hình & Đếm hình",
            axiomSummaryVi = "Phát hiện quy luật đếm thêm hoặc đếm bớt (1, 2, 5, 10), tìm hình còn thiếu trong dãy hình lặp lại theo chu kì và đếm số hình phẳng đơn giản.",
            intrinsicDifficulty = -1.25f,
            discrimination = 1.3f,
            generatorType = "G1_10_PATTERNS_COUNTING"
        ),

        // =========================================================================
        // GRADE 2 (10 CORE NODES)
        // =========================================================================
        KnowledgeNodeEntity(
            nodeId = "G2_01_TERMS_AND_NUMBER_LINE",
            grade = 2,
            domainCategory = "ARITHMETIC",
            titleVi = "Thành phần phép cộng, phép trừ & Tia số",
            axiomSummaryVi = "Gọi đúng tên thành phần và kết quả phép tính: Số hạng - Tổng, Số bị trừ - Số trừ - Hiệu; xác định số liền trước, số liền sau và biểu diễn số trên tia số.",
            intrinsicDifficulty = -1.1f,
            discrimination = 1.1f,
            generatorType = "G2_01_TERMS_AND_NUMBER_LINE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_02_MAKE_10_ADD_SUB",
            grade = 2,
            domainCategory = "ARITHMETIC",
            titleVi = "Cộng, trừ qua 10 trong phạm vi 20",
            axiomSummaryVi = "Vận dụng phương pháp tách - gộp số cho đủ chục (gộp cho đủ 10 rồi cộng với số còn lại; trừ để được 10 rồi trừ tiếp số còn lại) trong phạm vi 20.",
            intrinsicDifficulty = -0.85f,
            discrimination = 1.25f,
            generatorType = "G2_02_MAKE_10_ADD_SUB"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_03_ADD_SUB_100_WITH_CARRY",
            grade = 2,
            domainCategory = "ARITHMETIC",
            titleVi = "Cộng, trừ có nhớ trong phạm vi 100",
            axiomSummaryVi = "Đặt tính dọc và thực hiện phép cộng có nhớ 1 chục sang hàng chục, phép trừ có mượn 1 chục ở hàng chục trong phạm vi 100.",
            intrinsicDifficulty = -0.6f,
            discrimination = 1.3f,
            generatorType = "G2_03_ADD_SUB_100_WITH_CARRY"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_04_WORD_MORE_LESS",
            grade = 2,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Bài toán về Nhiều hơn, Ít hơn một số đơn vị",
            axiomSummaryVi = "Phân tích quan hệ hơn kém giữa hai đại lượng trên sơ đồ đoạn thẳng để tìm số lớn, số bé hoặc tìm phần chênh lệch (nhiều hơn/ít hơn bao nhiêu).",
            intrinsicDifficulty = -0.35f,
            discrimination = 1.35f,
            generatorType = "G2_04_WORD_MORE_LESS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_05_MUL_DIV_2_5",
            grade = 2,
            domainCategory = "ARITHMETIC",
            titleVi = "Bảng nhân 2, 5 & Bảng chia 2, 5",
            axiomSummaryVi = "Hiểu phép nhân là tổng các số hạng bằng nhau, phép chia là phép tính ngược của phép nhân; nắm vững tên gọi Thừa số - Tích, Số bị chia - Số chia - Thương và bảng nhân, chia 2, 5.",
            intrinsicDifficulty = -0.25f,
            discrimination = 1.25f,
            generatorType = "G2_05_MUL_DIV_2_5"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_06_NUM_1000_ADD_SUB",
            grade = 2,
            domainCategory = "ARITHMETIC",
            titleVi = "Các số trong phạm vi 1000 & Cộng, trừ có nhớ",
            axiomSummaryVi = "Cấu tạo thập phân của số có ba chữ số (Trăm - Chục - Đơn vị), so sánh các số đến 1000; thực hiện phép cộng, trừ không nhớ và có nhớ trong phạm vi 1000.",
            intrinsicDifficulty = -0.1f,
            discrimination = 1.2f,
            generatorType = "G2_06_NUM_1000_ADD_SUB"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_07_GEOMETRY_G2",
            grade = 2,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Đường gấp khúc, Chu vi & Khối trụ, khối cầu",
            axiomSummaryVi = "Tính độ dài đường gấp khúc, tính chu vi hình tam giác, hình tứ giác; nhận biết khối trụ và khối cầu qua các vật thể thực tế.",
            intrinsicDifficulty = -0.3f,
            discrimination = 1.15f,
            generatorType = "G2_07_GEOMETRY_G2"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_08_MEASURE_DM_M_KM_KG_L",
            grade = 2,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Đơn vị đo độ dài (dm, m, km), Khối lượng (kg) & Dung tích (l)",
            axiomSummaryVi = "Nắm vững bảng chuyển đổi đơn vị (1 m = 10 dm = 100 cm, 1 km = 1000 m); thực hiện phép tính với số đo khối lượng (kg) và dung tích (lít).",
            intrinsicDifficulty = -0.4f,
            discrimination = 1.1f,
            generatorType = "G2_08_MEASURE_DM_M_KM_KG_L"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_09_TIME_MONEY_G2",
            grade = 2,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Thời gian (Giờ - Phút, Ngày - Tháng) & Tiền Việt Nam",
            axiomSummaryVi = "Xem giờ rưỡi (15, 30 phút), nhận biết 1 giờ = 60 phút, 1 ngày = 24 giờ; nhận biết các tờ tiền Việt Nam trong phạm vi 1000 đồng.",
            intrinsicDifficulty = -0.2f,
            discrimination = 1.2f,
            generatorType = "G2_09_TIME_MONEY_G2"
        ),
        KnowledgeNodeEntity(
            nodeId = "G2_10_STATS_PROBABILITY_G2",
            grade = 2,
            domainCategory = "DATA_PROBABILITY",
            titleVi = "Thu thập số liệu, Biểu đồ tranh & Khả năng xảy ra",
            axiomSummaryVi = "Thu thập, kiểm đếm và đọc số liệu trên biểu đồ tranh; nhận biết các khả năng Chắc chắn, Có thể, Không thể xảy ra của một sự kiện đơn giản.",
            intrinsicDifficulty = -0.15f,
            discrimination = 1.25f,
            generatorType = "G2_10_STATS_PROBABILITY_G2"
        ),

        // =========================================================================
        // GRADE 3 (10 CORE NODES)
        // =========================================================================
        KnowledgeNodeEntity(
            nodeId = "G3_01_MUL_DIV_TABLES_3_9",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Bảng nhân, bảng chia từ 3 đến 9 & Nhân/chia số có 2, 3 chữ số",
            axiomSummaryVi = "Thuộc lòng bảng nhân 3 đến 9 và bảng chia tương ứng; nhân/chia số có hai, ba chữ số với số có một chữ số.",
            intrinsicDifficulty = 0.1f,
            discrimination = 1.3f,
            generatorType = "G3_01_MUL_DIV_TABLES_3_9"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_02_FIND_UNKNOWN_COMPONENT",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Tìm thành phần chưa biết trong 4 phép tính",
            axiomSummaryVi = "Vận dụng quy tắc tìm số hạng, số bị trừ, số trừ, thừa số, số bị chia và số chia chưa biết trong biểu thức.",
            intrinsicDifficulty = 0.25f,
            discrimination = 1.3f,
            generatorType = "G3_02_FIND_UNKNOWN_COMPONENT"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_03_TIMES_MORE_LESS_FRAC",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Gấp/giảm một số lần & Tìm một trong các phần bằng nhau",
            axiomSummaryVi = "Giải bài toán gấp một số lên nhiều lần (nhân) hoặc giảm đi nhiều lần (chia); tìm một phần mấy của một số.",
            intrinsicDifficulty = 0.35f,
            discrimination = 1.35f,
            generatorType = "G3_03_TIMES_MORE_LESS_FRAC"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_04_DIVISION_WITH_REMAINDER",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Phép chia có dư & Bài toán vận dụng thực tế",
            axiomSummaryVi = "Thực hiện phép chia có dư với quy tắc số dư luôn bé hơn số chia; vận dụng giải bài toán xếp hàng, chuyến thuyền.",
            intrinsicDifficulty = 0.45f,
            discrimination = 1.35f,
            generatorType = "G3_04_DIVISION_WITH_REMAINDER"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_05_ORDER_OF_OPERATIONS",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Thứ tự thực hiện phép tính & Dấu ngoặc ( )",
            axiomSummaryVi = "Tính giá trị biểu thức theo quy tắc: nhân/chia trước, cộng/trừ sau; ưu tiên thực hiện phép tính trong ngoặc ( ) trước.",
            intrinsicDifficulty = 0.5f,
            discrimination = 1.4f,
            generatorType = "G3_05_ORDER_OF_OPERATIONS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_06_NUM_100K_ROUNDING_ROMAN",
            grade = 3,
            domainCategory = "ARITHMETIC",
            titleVi = "Các số đến 100 000, Làm tròn số & Chữ số La Mã",
            axiomSummaryVi = "Đọc, viết các số trong phạm vi 100 000; làm tròn số đến hàng chục, hàng trăm, hàng nghìn; đọc và viết chữ số La Mã từ I đến XX.",
            intrinsicDifficulty = 0.3f,
            discrimination = 1.2f,
            generatorType = "G3_06_NUM_100K_ROUNDING_ROMAN"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_07_TWO_STEP_WORD_PROBLEMS",
            grade = 3,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Bài toán giải bằng 2 bước tính",
            axiomSummaryVi = "Phân tích bài toán hai bước tính hợp lý (kết hợp các phép cộng, trừ, nhân, chia) và viết câu lời văn, phép tính minh họa.",
            intrinsicDifficulty = 0.65f,
            discrimination = 1.45f,
            generatorType = "G3_07_TWO_STEP_WORD_PROBLEMS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_08_GEOMETRY_MIDPOINT_CIRCLE",
            grade = 3,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Điểm ở giữa, Trung điểm & Hình tròn (Tâm, Bán kính, Đường kính)",
            axiomSummaryVi = "Xác định trung điểm của đoạn thẳng; nhận biết các yếu tố tâm O, bán kính r, đường kính d = 2r của hình tròn và góc vuông bằng ê-ke.",
            intrinsicDifficulty = 0.4f,
            discrimination = 1.25f,
            generatorType = "G3_08_GEOMETRY_MIDPOINT_CIRCLE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_09_PERIMETER_AREA_RECT_SQUARE",
            grade = 3,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Chu vi & Diện tích Hình chữ nhật, Hình vuông",
            axiomSummaryVi = "Nắm vững công thức chu vi P = (a+b)x2, P = ax4 và diện tích S = axb, S = axa của hình chữ nhật và hình vuông (đơn vị cm², dm², m²).",
            intrinsicDifficulty = 0.6f,
            discrimination = 1.4f,
            generatorType = "G3_09_PERIMETER_AREA_RECT_SQUARE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G3_10_MEASURE_STATS_G3",
            grade = 3,
            domainCategory = "DATA_PROBABILITY",
            titleVi = "Đơn vị mm, g, ml, Nhiệt độ (°C) & Bảng thống kê số liệu",
            axiomSummaryVi = "Đổi đơn vị mm, g, ml; xem nhiệt kế đo nhiệt độ (°C); đọc và phân tích số liệu trên bảng thống kê 2 chiều đơn giản.",
            intrinsicDifficulty = 0.35f,
            discrimination = 1.2f,
            generatorType = "G3_10_MEASURE_STATS_G3"
        ),

        // =========================================================================
        // GRADE 4 (10 CORE NODES)
        // =========================================================================
        KnowledgeNodeEntity(
            nodeId = "G4_01_MILLIONS_CLASS_ROUNDING",
            grade = 4,
            domainCategory = "ARITHMETIC",
            titleVi = "Số có nhiều chữ số: Lớp triệu & Làm tròn số",
            axiomSummaryVi = "Đọc, viết số đến lớp triệu; nhận biết giá trị theo hàng và lớp; làm tròn số đến hàng trăm nghìn.",
            intrinsicDifficulty = 0.7f,
            discrimination = 1.25f,
            generatorType = "G4_01_MILLIONS_CLASS_ROUNDING"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS",
            grade = 4,
            domainCategory = "ARITHMETIC",
            titleVi = "Biểu thức chứa chữ & Tính chất giao hoán, kết hợp, phân phối",
            axiomSummaryVi = "Tính giá trị biểu thức chứa 1, 2, 3 chữ; áp dụng tính chất a+b=b+a, (a+b)+c=a+(b+c), a*(b+c)=a*b+a*c để tính nhanh.",
            intrinsicDifficulty = 0.85f,
            discrimination = 1.35f,
            generatorType = "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_03_MUL_DIV_2_DIGITS",
            grade = 4,
            domainCategory = "ARITHMETIC",
            titleVi = "Nhân, chia với số có 2, 3 chữ số & Dấu hiệu chia hết",
            axiomSummaryVi = "Đặt tính nhân, chia cho số có hai chữ số; nắm vững dấu hiệu chia hết cho 2, 5, 3, 9 và tính chất chia một tổng cho một số.",
            intrinsicDifficulty = 1.0f,
            discrimination = 1.4f,
            generatorType = "G4_03_MUL_DIV_2_DIGITS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_04_AVERAGE_AND_UNIT_METHOD",
            grade = 4,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Tìm số trung bình cộng & Bài toán rút về đơn vị",
            axiomSummaryVi = "Tìm số trung bình cộng của nhiều số (Tổng : Số các số hạng); giải bài toán rút về đơn vị bằng phép chia và phép nhân.",
            intrinsicDifficulty = 1.1f,
            discrimination = 1.45f,
            generatorType = "G4_04_AVERAGE_AND_UNIT_METHOD"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_05_SUM_DIFFERENCE_MODEL",
            grade = 4,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Bài toán Tìm hai số khi biết Tổng và Hiệu",
            axiomSummaryVi = "Vẽ sơ đồ đoạn thẳng và áp dụng công thức: Số lớn = (Tổng + Hiệu) : 2; Số bé = (Tổng - Hiệu) : 2.",
            intrinsicDifficulty = 1.2f,
            discrimination = 1.5f,
            generatorType = "G4_05_SUM_DIFFERENCE_MODEL"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_06_FRACTIONS_CONCEPT_COMPARE",
            grade = 4,
            domainCategory = "ARITHMETIC",
            titleVi = "Phân số: Rút gọn, Quy đồng & So sánh phân số",
            axiomSummaryVi = "Hiểu tử số và mẫu số; rút gọn phân số về tối giản; quy đồng mẫu số và so sánh hai phân số cùng mẫu hoặc khác mẫu.",
            intrinsicDifficulty = 0.9f,
            discrimination = 1.35f,
            generatorType = "G4_06_FRACTIONS_CONCEPT_COMPARE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_07_FRACTIONS_FOUR_OPS",
            grade = 4,
            domainCategory = "ARITHMETIC",
            titleVi = "4 phép tính với Phân số & Tìm phân số của một số",
            axiomSummaryVi = "Cộng, trừ, nhân, chia phân số (quy đồng mẫu số khi cộng trừ, đảo ngược phân số khi chia); tìm giá trị phân số của một số.",
            intrinsicDifficulty = 1.3f,
            discrimination = 1.5f,
            generatorType = "G4_07_FRACTIONS_FOUR_OPS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_08_ANGLES_PARALLEL_SHAPES",
            grade = 4,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Góc (Nhọn, Tù, Bẹt), Hai đường thẳng song song/vuông góc & Hình bình hành, Hình thoi",
            axiomSummaryVi = "Nhận biết các loại góc; góc vuông, hai đường thẳng song song, vuông góc; tính diện tích hình bình hành (S=a*h) và hình thoi (S=m*n/2).",
            intrinsicDifficulty = 1.05f,
            discrimination = 1.3f,
            generatorType = "G4_08_ANGLES_PARALLEL_SHAPES"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_09_MEASURE_TON_M2_CENTURY",
            grade = 4,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Đơn vị đo Khối lượng (Yến, Tạ, Tấn), Diện tích (dm², m², mm²) & Thế kỷ",
            axiomSummaryVi = "Chuyển đổi đơn vị đo khối lượng yến, tạ, tấn; đơn vị diện tích dm², m², mm² và xác định thế kỷ tương ứng với năm cho trước.",
            intrinsicDifficulty = 0.95f,
            discrimination = 1.25f,
            generatorType = "G4_09_MEASURE_TON_M2_CENTURY"
        ),
        KnowledgeNodeEntity(
            nodeId = "G4_10_BAR_CHART_PROBABILITY",
            grade = 4,
            domainCategory = "DATA_PROBABILITY",
            titleVi = "Biểu đồ cột & Khả năng xảy ra của sự kiện",
            axiomSummaryVi = "Đọc, phân tích dữ liệu trên biểu đồ cột đơn; tính dãy số liệu thống kê và đánh giá khả năng xảy ra của sự kiện.",
            intrinsicDifficulty = 0.8f,
            discrimination = 1.25f,
            generatorType = "G4_10_BAR_CHART_PROBABILITY"
        ),

        // =========================================================================
        // GRADE 5 (10 CORE NODES)
        // =========================================================================
        KnowledgeNodeEntity(
            nodeId = "G5_01_DECIMAL_FRACTIONS_MIXED",
            grade = 5,
            domainCategory = "ARITHMETIC",
            titleVi = "Phân số thập phân & Hỗn số",
            axiomSummaryVi = "Chuyển đổi phân số có mẫu là 10, 100, 1000 về phân số thập phân; viết và đọc hỗn số gồm phần nguyên và phần phân số.",
            intrinsicDifficulty = 1.4f,
            discrimination = 1.35f,
            generatorType = "G5_01_DECIMAL_FRACTIONS_MIXED"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_02_DECIMALS_PLACE_ROUNDING",
            grade = 5,
            domainCategory = "ARITHMETIC",
            titleVi = "Số thập phân: Cấu tạo, So sánh & Làm tròn số",
            axiomSummaryVi = "Nhận biết phần nguyên và phần thập phân (mười, trăm, nghìn); so sánh hai số thập phân và làm tròn số thập phân đến hàng đơn vị, hàng phần mười.",
            intrinsicDifficulty = 1.5f,
            discrimination = 1.4f,
            generatorType = "G5_02_DECIMALS_PLACE_ROUNDING"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_03_DECIMALS_FOUR_OPS",
            grade = 5,
            domainCategory = "ARITHMETIC",
            titleVi = "4 phép tính với Số thập phân",
            axiomSummaryVi = "Đặt tính rồi tính cộng, trừ, nhân, chia số thập phân; tính nhẩm nhân/chia với 10, 100, 1000 và 0,1, 0,01, 0,001.",
            intrinsicDifficulty = 1.7f,
            discrimination = 1.5f,
            generatorType = "G5_03_DECIMALS_FOUR_OPS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_04_SUM_DIFF_RATIO_PROBLEMS",
            grade = 5,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Bài toán Tỉ số: Tìm hai số khi biết Tổng - Tỉ hoặc Hiệu - Tỉ",
            axiomSummaryVi = "Vẽ sơ đồ tỉ lệ phần bằng nhau và giải bài toán Tìm hai số khi biết Tổng và Tỉ số hoặc Hiệu và Tỉ số.",
            intrinsicDifficulty = 1.85f,
            discrimination = 1.55f,
            generatorType = "G5_04_SUM_DIFF_RATIO_PROBLEMS"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_05_PERCENTAGE_MAP_SCALE",
            grade = 5,
            domainCategory = "ARITHMETIC",
            titleVi = "Tỉ số phần trăm (%) & Tỉ lệ bản đồ",
            axiomSummaryVi = "Tính tỉ số phần trăm của hai số; tìm giá trị phần trăm của một số; tính độ dài thực tế dựa trên tỉ lệ bản đồ 1:N.",
            intrinsicDifficulty = 2.0f,
            discrimination = 1.5f,
            generatorType = "G5_05_PERCENTAGE_MAP_SCALE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_06_TRIANGLE_TRAPEZOID_CIRCLE",
            grade = 5,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Diện tích Hình tam giác, Hình thang & Hình tròn",
            axiomSummaryVi = "Tính diện tích hình tam giác S = (a*h)/2, diện tích hình thang S = (a+b)*h/2, chu vi C = d*3,14 và diện tích S = r*r*3,14 của hình tròn.",
            intrinsicDifficulty = 1.9f,
            discrimination = 1.45f,
            generatorType = "G5_06_TRIANGLE_TRAPEZOID_CIRCLE"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_07_SURFACE_AREA_VOLUME_3D",
            grade = 5,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Diện tích xung quanh, Toàn phần & Thể tích Hình hộp chữ nhật, Hình lập phương",
            axiomSummaryVi = "Tính diện tích xung quanh, diện tích toàn phần và thể tích V = a*b*c, V = a*a*a của hình hộp chữ nhật và hình lập phương (đơn vị cm³, dm³, m³).",
            intrinsicDifficulty = 2.2f,
            discrimination = 1.55f,
            generatorType = "G5_07_SURFACE_AREA_VOLUME_3D"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_08_TIME_OPS_AND_MOTION",
            grade = 5,
            domainCategory = "GEOMETRY_MEASUREMENT",
            titleVi = "Phép tính thời gian & Bài toán Chuyển động đều (s = v * t)",
            axiomSummaryVi = "Cộng, trừ, nhân, chia số đo thời gian; áp dụng công thức chuyển động s = v*t, v = s/t, t = s/v và toán hai xe đi ngược chiều/cùng chiều.",
            intrinsicDifficulty = 2.4f,
            discrimination = 1.55f,
            generatorType = "G5_08_TIME_OPS_AND_MOTION"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_09_PIE_CHART_PROBABILITY_RATIO",
            grade = 5,
            domainCategory = "DATA_PROBABILITY",
            titleVi = "Biểu đồ hình quạt tròn & Tỉ lệ xác suất thực nghiệm",
            axiomSummaryVi = "Đọc, phân tích tỉ lệ phần trăm trên biểu đồ hình quạt tròn và tính xác suất thực nghiệm trong các trò chơi đơn giản.",
            intrinsicDifficulty = 2.1f,
            discrimination = 1.4f,
            generatorType = "G5_09_PIE_CHART_PROBABILITY_RATIO"
        ),
        KnowledgeNodeEntity(
            nodeId = "G5_10_COMPREHENSIVE_HEURISTICS",
            grade = 5,
            domainCategory = "HEURISTICS_COMBINATORICS",
            titleVi = "Giải toán tổng hợp & Chiến lược tư duy toán học",
            axiomSummaryVi = "Vận dụng tổng hợp các phương pháp tư duy toán học (giả thiết tạm, thử chọn, suy luận ngược, cấu tạo số) để giải bài toán tiểu học nâng cao.",
            intrinsicDifficulty = 2.6f,
            discrimination = 1.55f,
            generatorType = "G5_10_COMPREHENSIVE_HEURISTICS"
        )
    )

    val seedEdges = listOf(
        // Intra-Grade 1
        PrerequisiteEdgeEntity("G1_02_NUM_0_10_COMPARE", "G1_03_SPLIT_COMBINE_10", 1.0f),
        PrerequisiteEdgeEntity("G1_03_SPLIT_COMBINE_10", "G1_04_ADD_SUB_10_MEANING", 1.0f),
        PrerequisiteEdgeEntity("G1_04_ADD_SUB_10_MEANING", "G1_05_PLACE_VALUE_100", 0.9f),
        PrerequisiteEdgeEntity("G1_05_PLACE_VALUE_100", "G1_06_ADD_SUB_100_NO_CARRY", 1.0f),
        PrerequisiteEdgeEntity("G1_06_ADD_SUB_100_NO_CARRY", "G1_07_WORD_PROBLEMS_G1", 1.0f),

        // Grade 1 -> Grade 2 Cross-Grade
        PrerequisiteEdgeEntity("G1_06_ADD_SUB_100_NO_CARRY", "G2_01_TERMS_AND_NUMBER_LINE", 1.0f),
        PrerequisiteEdgeEntity("G1_04_ADD_SUB_10_MEANING", "G2_02_MAKE_10_ADD_SUB", 1.0f),
        PrerequisiteEdgeEntity("G2_02_MAKE_10_ADD_SUB", "G2_03_ADD_SUB_100_WITH_CARRY", 1.0f),
        PrerequisiteEdgeEntity("G2_03_ADD_SUB_100_WITH_CARRY", "G2_04_WORD_MORE_LESS", 1.0f),
        PrerequisiteEdgeEntity("G2_03_ADD_SUB_100_WITH_CARRY", "G2_05_MUL_DIV_2_5", 0.9f),

        // Grade 2 -> Grade 3
        PrerequisiteEdgeEntity("G2_05_MUL_DIV_2_5", "G3_01_MUL_DIV_TABLES_3_9", 1.0f),
        PrerequisiteEdgeEntity("G3_01_MUL_DIV_TABLES_3_9", "G3_02_FIND_UNKNOWN_COMPONENT", 1.0f),
        PrerequisiteEdgeEntity("G3_01_MUL_DIV_TABLES_3_9", "G3_03_TIMES_MORE_LESS_FRAC", 1.0f),
        PrerequisiteEdgeEntity("G3_01_MUL_DIV_TABLES_3_9", "G3_04_DIVISION_WITH_REMAINDER", 1.0f),
        PrerequisiteEdgeEntity("G3_01_MUL_DIV_TABLES_3_9", "G3_05_ORDER_OF_OPERATIONS", 1.0f),

        // Grade 3 -> Grade 4
        PrerequisiteEdgeEntity("G3_05_ORDER_OF_OPERATIONS", "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS", 1.0f),
        PrerequisiteEdgeEntity("G3_01_MUL_DIV_TABLES_3_9", "G4_03_MUL_DIV_2_DIGITS", 1.0f),
        PrerequisiteEdgeEntity("G3_07_TWO_STEP_WORD_PROBLEMS", "G4_04_AVERAGE_AND_UNIT_METHOD", 1.0f),
        PrerequisiteEdgeEntity("G3_07_TWO_STEP_WORD_PROBLEMS", "G4_05_SUM_DIFFERENCE_MODEL", 1.0f),
        PrerequisiteEdgeEntity("G3_03_TIMES_MORE_LESS_FRAC", "G4_06_FRACTIONS_CONCEPT_COMPARE", 1.0f),
        PrerequisiteEdgeEntity("G4_06_FRACTIONS_CONCEPT_COMPARE", "G4_07_FRACTIONS_FOUR_OPS", 1.0f),

        // Grade 4 -> Grade 5
        PrerequisiteEdgeEntity("G4_06_FRACTIONS_CONCEPT_COMPARE", "G5_01_DECIMAL_FRACTIONS_MIXED", 1.0f),
        PrerequisiteEdgeEntity("G5_01_DECIMAL_FRACTIONS_MIXED", "G5_02_DECIMALS_PLACE_ROUNDING", 1.0f),
        PrerequisiteEdgeEntity("G5_02_DECIMALS_PLACE_ROUNDING", "G5_03_DECIMALS_FOUR_OPS", 1.0f),
        PrerequisiteEdgeEntity("G4_05_SUM_DIFFERENCE_MODEL", "G5_04_SUM_DIFF_RATIO_PROBLEMS", 1.0f),
        PrerequisiteEdgeEntity("G4_07_FRACTIONS_FOUR_OPS", "G5_05_PERCENTAGE_MAP_SCALE", 1.0f),
        PrerequisiteEdgeEntity("G3_09_PERIMETER_AREA_RECT_SQUARE", "G5_06_TRIANGLE_TRAPEZOID_CIRCLE", 1.0f),
        PrerequisiteEdgeEntity("G5_06_TRIANGLE_TRAPEZOID_CIRCLE", "G5_07_SURFACE_AREA_VOLUME_3D", 1.0f),
        PrerequisiteEdgeEntity("G5_03_DECIMALS_FOUR_OPS", "G5_08_TIME_OPS_AND_MOTION", 1.0f),
        PrerequisiteEdgeEntity("G5_05_PERCENTAGE_MAP_SCALE", "G5_09_PIE_CHART_PROBABILITY_RATIO", 1.0f),
        PrerequisiteEdgeEntity("G5_08_TIME_OPS_AND_MOTION", "G5_10_COMPREHENSIVE_HEURISTICS", 1.0f)
    )
}
