package com.example.toanhoc.domain.curriculum

/**
 * Toán 4 – Kết nối tri thức với cuộc sống: 13 chủ đề, 73 bài (tên bài theo mục lục sách).
 * Lời giảng trong `teaching` do app tự soạn. `practiceGap = true`: bộ sinh đề chưa có dạng đề riêng.
 */
object Grade4Curriculum {

    private const val NUM = "ARITHMETIC"
    private const val GEO = "GEOMETRY_MEASUREMENT"
    private const val DATA = "DATA_STATS_PROBABILITY"
    private const val ALG = "ALGEBRAIC_THINKING"
    private const val PAT = "HEURISTICS_COMBINATORICS"

    private fun src(node: String, include: String? = null, exclude: String? = null, max: Int? = null, carry: Boolean? = null) =
        PracticeSource(node, QuestionFilter(include?.let(::Regex), exclude?.let(::Regex), max, carry))

    private const val G206 = "G2_06_NUM_1000_ADD_SUB"
    private const val G301 = "G3_01_MUL_DIV_TABLES_3_9"
    private const val G303 = "G3_03_TIMES_MORE_LESS_FRAC"
    private const val G304 = "G3_04_DIVISION_WITH_REMAINDER"
    private const val G306 = "G3_06_NUM_100K_ROUNDING_ROMAN"
    private const val G307 = "G3_07_TWO_STEP_WORD_PROBLEMS"
    private const val G310 = "G3_10_MEASURE_STATS_G3"
    private const val G01 = "G4_01_MILLIONS_CLASS_ROUNDING"
    private const val G02 = "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS"
    private const val G03 = "G4_03_MUL_DIV_2_DIGITS"
    private const val G04 = "G4_04_AVERAGE_AND_UNIT_METHOD"
    private const val G05 = "G4_05_SUM_DIFFERENCE_MODEL"
    private const val G06 = "G4_06_FRACTIONS_CONCEPT_COMPARE"
    private const val G07 = "G4_07_FRACTIONS_FOUR_OPS"
    private const val G08 = "G4_08_ANGLES_PARALLEL_SHAPES"
    private const val G09 = "G4_09_MEASURE_TON_M2_CENTURY"
    private const val G10 = "G4_10_BAR_CHART_PROBABILITY"

    private fun b(n: Int, title: String, domain: String, vararg s: PracticeSource, teaching: LessonTeaching? = null, gap: Boolean = false) =
        TextbookLesson("L4_B$n", n, title, domain, s.toList(), teaching, gap)

    /* ------------------- Nội dung dạy: Bài 25 (tổng và hiệu) ------------------- */

    private val teachSumDiff = LessonTeaching(
        cards = listOf(
            TeachCard(
                title = "Bài toán tổng – hiệu",
                body = "Đề cho biết TỔNG của hai số và HIỆU của chúng (số lớn hơn số bé bao nhiêu).\n" +
                    "Ví dụ: Hai số có tổng là 80, hiệu là 10. Tìm hai số.\nBước đầu tiên luôn là vẽ sơ đồ đoạn thẳng.",
                diagramType = "SINGAPORE_BAR_MODEL",
                diagramStatement = "Hai số có tổng bằng 80 và hiệu bằng 10. Số bé là:"
            ),
            TeachCard(
                title = "Cách 1: tìm số bé trước",
                body = "Bớt phần hơn của số lớn đi, hai đoạn sẽ bằng nhau và bằng số bé.\n" +
                    "Số bé = (Tổng − Hiệu) : 2\n= (80 − 10) : 2 = 35\nSố lớn = 35 + 10 = 45"
            ),
            TeachCard(
                title = "Cách 2: tìm số lớn trước",
                body = "Thêm vào số bé đúng bằng phần hơn, hai đoạn bằng nhau và bằng số lớn.\n" +
                    "Số lớn = (Tổng + Hiệu) : 2\n= (80 + 10) : 2 = 45\nSố bé = 45 − 10 = 35"
            )
        ),
        tips = listOf(
            "Thử lại cả hai điều kiện: 35 + 45 = 80 (đúng tổng) và 45 − 35 = 10 (đúng hiệu).",
            "Câu chữ hay gặp: \"hơn … là\", \"kém … là\", \"nhiều hơn\" chính là HIỆU; \"cả hai\", \"tất cả\" là TỔNG.",
            "Tìm được một số rồi thì lấy tổng trừ đi số đó để ra số còn lại – nhanh hơn tính lại từ đầu."
        ),
        tryIt = listOf(
            TryQuestion("Hai số có tổng bằng 50 và hiệu bằng 10. Số lớn là:", listOf("30", "20", "25", "40"), "30", "Số lớn = (50 + 10) : 2 = 30."),
            TryQuestion("Hai số có tổng bằng 36 và hiệu bằng 6. Số bé là:", listOf("21", "15", "18", "12"), "15", "Số bé = (36 − 6) : 2 = 15."),
            TryQuestion("Tuổi bố và tuổi con cộng lại là 48. Bố hơn con 30 tuổi. Con bao nhiêu tuổi?", listOf("18", "9", "39", "24"), "9", "Tuổi con là số bé: (48 − 30) : 2 = 9.")
        )
    )

    val data = GradeCurriculum(
        grade = 4,
        semester2FromTopic = 8,
        topics = listOf(
            TextbookTopic(1, "Ôn tập và bổ sung", listOf(
                b(1, "Ôn tập các số đến 100 000", NUM, src(G306, exclude = "Tính")),
                b(2, "Ôn tập các phép tính trong phạm vi 100 000", NUM, src(G306, "Tính"), src(G301), src(G304)),
                b(3, "Số chẵn, số lẻ", NUM, src(G03, "Nhận xét")),
                b(4, "Biểu thức chứa chữ", ALG, src(G02, "với a =")),
                b(5, "Giải bài toán có ba bước tính", NUM, src(G307), src(G04, "Mua"), gap = true),
                b(6, "Luyện tập chung", NUM, src(G02), src(G03), src(G307))
            )),
            TextbookTopic(2, "Góc và đơn vị đo góc", listOf(
                b(7, "Đo góc, đơn vị đo góc", GEO, src(G08, "Góc"), gap = true),
                b(8, "Góc nhọn, góc tù, góc bẹt", GEO, src(G08, "Góc")),
                b(9, "Luyện tập chung", GEO, src(G08, "Góc"))
            )),
            TextbookTopic(3, "Số có nhiều chữ số", listOf(
                b(10, "Số có sáu chữ số. Số 1 000 000", NUM, src(G01, "chữ số|viết là")),
                b(11, "Hàng và lớp", NUM, src(G01, "hàng|Lớp")),
                b(12, "Các số trong phạm vi lớp triệu", NUM, src(G01, "triệu")),
                b(13, "Làm tròn số đến hàng trăm nghìn", NUM, src(G01, "Làm tròn")),
                b(14, "So sánh các số có nhiều chữ số", NUM, src(G01), gap = true),
                b(15, "Làm quen với dãy số tự nhiên", PAT, src(G01), gap = true),
                b(16, "Luyện tập chung", NUM, src(G01))
            )),
            TextbookTopic(4, "Một số đơn vị đo đại lượng", listOf(
                b(17, "Yến, tạ, tấn", GEO, src(G09, "tấn")),
                b(18, "Đề-xi-mét vuông, mét vuông, mi-li-mét vuông", GEO, src(G09, "m²")),
                b(19, "Giây, thế kỉ", GEO, src(G09, "thế kỷ")),
                b(20, "Thực hành và trải nghiệm sử dụng một số đơn vị đo đại lượng", GEO, src(G09)),
                b(21, "Luyện tập chung", GEO, src(G09))
            )),
            TextbookTopic(5, "Phép cộng và phép trừ", listOf(
                b(22, "Phép cộng các số có nhiều chữ số", NUM, src(G306, """\+""")),
                b(23, "Phép trừ các số có nhiều chữ số", NUM, src(G206, "Tính hiệu"), gap = true),
                b(24, "Tính chất giao hoán và kết hợp của phép cộng", ALG, src(G02, "giao hoán")),
                b(25, "Tìm hai số biết tổng và hiệu của hai số đó", ALG, src(G05), teaching = teachSumDiff),
                b(26, "Luyện tập chung", ALG, src(G05), src(G02), src(G306, """\+"""))
            )),
            TextbookTopic(6, "Đường thẳng vuông góc. Đường thẳng song song", listOf(
                b(27, "Hai đường thẳng vuông góc", GEO, src(G08, "Góc"), gap = true),
                b(28, "Thực hành và trải nghiệm vẽ hai đường thẳng vuông góc", GEO, src(G08, "Góc"), gap = true),
                b(29, "Hai đường thẳng song song", GEO, src(G08, "bình hành"), gap = true),
                b(30, "Thực hành và trải nghiệm vẽ hai đường thẳng song song", GEO, src(G08, "bình hành"), gap = true),
                b(31, "Hình bình hành, hình thoi", GEO, src(G08, "bình hành|thoi")),
                b(32, "Luyện tập chung", GEO, src(G08))
            )),
            TextbookTopic(7, "Ôn tập học kì 1", listOf(
                b(33, "Ôn tập các số đến lớp triệu", NUM, src(G01)),
                b(34, "Ôn tập phép cộng, phép trừ", NUM, src(G306, """\+"""), src(G05)),
                b(35, "Ôn tập hình học", GEO, src(G08)),
                b(36, "Ôn tập đo lường", GEO, src(G09)),
                b(37, "Ôn tập chung", NUM, src(G01), src(G02), src(G05), src(G08))
            )),
            TextbookTopic(8, "Phép nhân và phép chia", listOf(
                b(38, "Nhân với số có một chữ số", ALG, src(G301, "Đặt tính"), src(G03, "×"), gap = true),
                b(39, "Chia cho số có một chữ số", ALG, src(G304, "phép chia có dư"), src(G03, " : "), gap = true),
                b(40, "Tính chất giao hoán và kết hợp của phép nhân", ALG, src(G02), gap = true),
                b(41, "Nhân, chia với 10, 100, 1 000, ...", ALG, src(G03), gap = true),
                b(42, "Tính chất phân phối của phép nhân đối với phép cộng", ALG, src(G02, """a × \(b \+ c\)""")),
                b(43, "Nhân với số có hai chữ số", ALG, src(G03, """× \d{2} = """)),
                b(44, "Chia cho số có hai chữ số", ALG, src(G03, """: \d{2} = """)),
                b(45, "Thực hành và trải nghiệm ước lượng trong tính toán", ALG, src(G03, "Đặt tính")),
                b(46, "Tìm số trung bình cộng", ALG, src(G04, "trung bình")),
                b(47, "Bài toán liên quan đến rút về đơn vị", ALG, src(G04, "Mua")),
                b(48, "Luyện tập chung", ALG, src(G03), src(G04))
            )),
            TextbookTopic(9, "Làm quen với yếu tố thống kê, xác suất", listOf(
                b(49, "Dãy số liệu thống kê", DATA, src(G310, "Bảng thống kê"), src(G10, "biểu đồ")),
                b(50, "Biểu đồ cột", DATA, src(G10, "biểu đồ cột")),
                b(51, "Số lần xuất hiện của một sự kiện", DATA, src(G10, "túi")),
                b(52, "Luyện tập chung", DATA, src(G10))
            )),
            TextbookTopic(10, "Phân số", listOf(
                b(53, "Khái niệm phân số", NUM, src(G303, "tô màu"), src(G06, "Tìm")),
                b(54, "Phân số và phép chia số tự nhiên", NUM, src(G06), gap = true),
                b(55, "Tính chất cơ bản của phân số", NUM, src(G06, "Rút gọn")),
                b(56, "Rút gọn phân số", NUM, src(G06, "Rút gọn")),
                b(57, "Quy đồng mẫu số các phân số", NUM, src(G06, "Rút gọn"), gap = true),
                b(58, "So sánh phân số", NUM, src(G06), gap = true),
                b(59, "Luyện tập chung", NUM, src(G06))
            )),
            TextbookTopic(11, "Phép cộng, phép trừ phân số", listOf(
                b(60, "Phép cộng phân số", NUM, src(G07, """\+""")),
                b(61, "Phép trừ phân số", NUM, src(G07, """\+"""), gap = true),
                b(62, "Luyện tập chung", NUM, src(G07, """\+"""))
            )),
            TextbookTopic(12, "Phép nhân, phép chia phân số", listOf(
                b(63, "Phép nhân phân số", NUM, src(G07, "×")),
                b(64, "Phép chia phân số", NUM, src(G07, " : ")),
                b(65, "Tìm phân số của một số", NUM, src(G06, "Tìm")),
                b(66, "Luyện tập chung", NUM, src(G07), src(G06, "Tìm"))
            )),
            TextbookTopic(13, "Ôn tập cuối năm", listOf(
                b(67, "Ôn tập số tự nhiên", NUM, src(G01)),
                b(68, "Ôn tập phép tính với số tự nhiên", NUM, src(G03), src(G02)),
                b(69, "Ôn tập phân số", NUM, src(G06)),
                b(70, "Ôn tập phép tính với phân số", NUM, src(G07)),
                b(71, "Ôn tập hình học và đo lường", GEO, src(G08), src(G09)),
                b(72, "Ôn tập một số yếu tố thống kê và xác suất", DATA, src(G10)),
                b(73, "Ôn tập chung", NUM, src(G02), src(G04), src(G05), src(G07))
            ))
        )
    )
}
