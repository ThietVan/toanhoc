package com.example.toanhoc.domain.curriculum

/**
 * Toán 3 – Kết nối tri thức với cuộc sống: 16 chủ đề, 81 bài (tên bài theo mục lục sách).
 * Lời giảng trong `teaching` do app tự soạn. `practiceGap = true`: bộ sinh đề chưa có dạng đề riêng.
 */
object Grade3Curriculum {

    private const val NUM = "ARITHMETIC"
    private const val GEO = "GEOMETRY_MEASUREMENT"
    private const val DATA = "DATA_STATS_PROBABILITY"
    private const val ALG = "ALGEBRAIC_THINKING"

    private fun src(node: String, include: String? = null, exclude: String? = null, max: Int? = null, carry: Boolean? = null) =
        PracticeSource(node, QuestionFilter(include?.let(::Regex), exclude?.let(::Regex), max, carry))

    private fun table(n: Int) = src(G01, """Tính nhẩm: ($n × \d+|\d+ × $n|\d+ : $n) = """)

    private const val G101 = "G1_01_SPATIAL_SHAPES"
    private const val G109 = "G1_09_TIME_CLOCK_CALENDAR"
    private const val G205 = "G2_05_MUL_DIV_2_5"
    private const val G206 = "G2_06_NUM_1000_ADD_SUB"
    private const val G207 = "G2_07_GEOMETRY_G2"
    private const val G208 = "G2_08_MEASURE_DM_M_KM_KG_L"
    private const val G209 = "G2_09_TIME_MONEY_G2"
    private const val G210 = "G2_10_STATS_PROBABILITY_G2"
    private const val G403 = "G4_03_MUL_DIV_2_DIGITS"
    private const val G01 = "G3_01_MUL_DIV_TABLES_3_9"
    private const val G02 = "G3_02_FIND_UNKNOWN_COMPONENT"
    private const val G03 = "G3_03_TIMES_MORE_LESS_FRAC"
    private const val G04 = "G3_04_DIVISION_WITH_REMAINDER"
    private const val G05 = "G3_05_ORDER_OF_OPERATIONS"
    private const val G06 = "G3_06_NUM_100K_ROUNDING_ROMAN"
    private const val G07 = "G3_07_TWO_STEP_WORD_PROBLEMS"
    private const val G08 = "G3_08_GEOMETRY_MIDPOINT_CIRCLE"
    private const val G09 = "G3_09_PERIMETER_AREA_RECT_SQUARE"
    private const val G10 = "G3_10_MEASURE_STATS_G3"

    private fun b(n: Int, title: String, domain: String, vararg s: PracticeSource, teaching: LessonTeaching? = null, gap: Boolean = false) =
        TextbookLesson("L3_B$n", n, title, domain, s.toList(), teaching, gap)

    /* --------------------- Nội dung dạy: Bài 25 (chia hết, chia có dư) --------------------- */

    private val teachRemainder = LessonTeaching(
        cards = listOf(
            TeachCard(
                title = "Phép chia hết",
                body = "Có 12 cái kẹo chia đều cho 3 bạn. Mỗi bạn được 4 cái, không thừa cái nào.\n" +
                    "Ta viết: 12 : 3 = 4.\nKhông thừa gì nên gọi là phép chia hết."
            ),
            TeachCard(
                title = "Phép chia có dư",
                body = "Có 13 cái kẹo chia đều cho 3 bạn. Mỗi bạn được 4 cái, còn thừa 1 cái.\n" +
                    "Ta viết: 13 : 3 = 4 (dư 1).\nSố thừa ra gọi là số dư.",
                diagramType = "VERTICAL_ARITHMETIC",
                diagramStatement = "Đặt tính rồi tính: 13 : 3 = ?",
                diagramExpr = "13 : 3"
            ),
            TeachCard(
                title = "Cách làm ở lớp",
                body = "Tính 17 : 5\n• Nhẩm bảng nhân 5: 5 × 3 = 15 (gần 17 nhất mà không vượt quá).\n" +
                    "• Thương là 3.\n• 17 − 15 = 2, số dư là 2.\nVậy 17 : 5 = 3 (dư 2)."
            ),
            TeachCard(
                title = "Số dư luôn bé hơn số chia",
                body = "Chia cho 5 thì số dư chỉ có thể là 1, 2, 3 hoặc 4.\n" +
                    "Nếu số dư bằng hoặc lớn hơn số chia, nghĩa là còn chia tiếp được – thương đang bị thiếu."
            )
        ),
        tips = listOf(
            "Thử lại: thương × số chia + số dư = số bị chia. Ví dụ 3 × 5 + 2 = 17 là đúng.",
            "Thuộc bảng nhân thì tìm thương rất nhanh: tìm số trong bảng gần nhất mà không vượt quá số bị chia.",
            "Bài toán \"mỗi xe chở được … hỏi cần ít nhất mấy xe\": nếu còn dư thì phải thêm 1 xe nữa."
        ),
        tryIt = listOf(
            TryQuestion("Tính: 17 : 5 = ?", listOf("3 (dư 2)", "3 (dư 1)", "2 (dư 7)", "4 (dư 3)"), "3 (dư 2)", "5 × 3 = 15, 17 − 15 = 2."),
            TryQuestion("Trong phép chia cho 4, số dư lớn nhất có thể là bao nhiêu?", listOf("4", "3", "1", "5"), "3", "Số dư luôn bé hơn số chia, nên lớn nhất là 3."),
            TryQuestion("Tính: 25 : 4 = ?", listOf("6 (dư 1)", "5 (dư 5)", "6 (dư 2)", "7 (dư 1)"), "6 (dư 1)", "4 × 6 = 24, 25 − 24 = 1.")
        )
    )

    val data = GradeCurriculum(
        grade = 3,
        semester2FromTopic = 8,
        topics = listOf(
            TextbookTopic(1, "Ôn tập và bổ sung", listOf(
                b(1, "Ôn tập các số đến 1000", NUM, src(G206, exclude = "Tính")),
                b(2, "Ôn tập phép cộng, phép trừ trong phạm vi 1000", NUM, src(G206, "Tính")),
                b(3, "Tìm thành phần trong phép cộng, phép trừ", ALG, src(G02, "số hạng|số bị trừ|số trừ")),
                b(4, "Ôn tập bảng nhân 2; 5, bảng chia 2; 5", ALG, src(G205, "^Tính")),
                b(5, "Bảng nhân 3, bảng chia 3", ALG, table(3)),
                b(6, "Bảng nhân 4, bảng chia 4", ALG, table(4)),
                b(7, "Ôn tập hình học và đo lường", GEO, src(G207), src(G208)),
                b(8, "Luyện tập chung", NUM, src(G02), src(G01), src(G206))
            )),
            TextbookTopic(2, "Bảng nhân, bảng chia", listOf(
                b(9, "Bảng nhân 6, bảng chia 6", ALG, table(6)),
                b(10, "Bảng nhân 7, bảng chia 7", ALG, table(7)),
                b(11, "Bảng nhân 8, bảng chia 8", ALG, table(8)),
                b(12, "Bảng nhân 9, bảng chia 9", ALG, table(9)),
                b(13, "Tìm thành phần trong phép nhân, phép chia", ALG, src(G02, "thừa số|số bị chia|số chia")),
                b(14, "Một phần mấy", NUM, src(G03, "tô màu")),
                b(15, "Luyện tập chung", ALG, src(G01), src(G02), src(G03, "tô màu"))
            )),
            TextbookTopic(3, "Làm quen với hình phẳng, hình khối", listOf(
                b(16, "Điểm ở giữa, trung điểm của đoạn thẳng", GEO, src(G08, "trung điểm")),
                b(17, "Hình tròn. Tâm, bán kính, đường kính của hình tròn", GEO, src(G08, "Hình tròn")),
                b(18, "Góc, góc vuông, góc không vuông", GEO, src(G08, "góc vuông"), gap = true),
                b(19, "Hình tam giác, hình tứ giác. Hình chữ nhật, hình vuông", GEO, src(G207, "tam giác|tứ giác"), src(G08, "góc vuông")),
                b(20, "Thực hành vẽ góc vuông, vẽ đường tròn, hình vuông, hình chữ nhật và vẽ trang trí", GEO, src(G08), gap = true),
                b(21, "Khối lập phương, khối hộp chữ nhật", GEO, src(G101, "khối hình học|Đồ vật"), src(G207, "hình khối")),
                b(22, "Luyện tập chung", GEO, src(G08), src(G207))
            )),
            TextbookTopic(4, "Phép nhân, phép chia trong phạm vi 100", listOf(
                b(23, "Nhân số có hai chữ số với số có một chữ số", ALG, src(G01, """(?<!\d)\d{2} × \d(?!\d)""")),
                b(24, "Gấp một số lên một số lần", ALG, src(G03, "Gấp")),
                b(25, "Phép chia hết, phép chia có dư", ALG, src(G04), teaching = teachRemainder),
                b(26, "Chia số có hai chữ số cho số có một chữ số", ALG, src(G04, """(?<!\d)\d{2} : \d(?!\d)"""), src(G01, """(?<!\d)\d{2} : \d(?!\d)""")),
                b(27, "Giảm một số đi một số lần", ALG, src(G03, "Giảm")),
                b(28, "Bài toán giải bằng hai bước tính", NUM, src(G07)),
                b(29, "Luyện tập chung", ALG, src(G03), src(G04), src(G07))
            )),
            TextbookTopic(5, "Một số đơn vị đo độ dài, khối lượng, dung tích, nhiệt độ", listOf(
                b(30, "Mi-li-mét", GEO, src(G10, "mm")),
                b(31, "Gam", GEO, src(G10, """\d g\b|\.\.\. g""")),
                b(32, "Mi-li-lít", GEO, src(G10, "ml")),
                b(33, "Nhiệt độ. Đơn vị đo nhiệt độ", GEO, src(G10, "°C")),
                b(34, "Thực hành và trải nghiệm với các đơn vị mi-li-mét, gam, mi-li-lít, độ C", GEO, src(G10, exclude = "Bảng thống kê")),
                b(35, "Luyện tập chung", GEO, src(G10))
            )),
            TextbookTopic(6, "Phép nhân, phép chia trong phạm vi 1000", listOf(
                b(36, "Nhân số có ba chữ số với số có một chữ số", ALG, src(G01, "Đặt tính"), gap = true),
                b(37, "Chia số có ba chữ số cho số có một chữ số", ALG, src(G04, "phép chia có dư"), gap = true),
                b(38, "Biểu thức số. Tính giá trị của biểu thức số", ALG, src(G05)),
                b(39, "So sánh số lớn gấp mấy lần số bé", ALG, src(G03, "gấp mấy")),
                b(40, "Luyện tập chung", ALG, src(G05), src(G03), src(G04))
            )),
            TextbookTopic(7, "Ôn tập học kì 1", listOf(
                b(41, "Ôn tập phép nhân, phép chia trong phạm vi 100, 1000", ALG, src(G01), src(G04)),
                b(42, "Ôn tập biểu thức số", ALG, src(G05)),
                b(43, "Ôn tập hình học và đo lường", GEO, src(G08), src(G10)),
                b(44, "Ôn tập chung", NUM, src(G01), src(G02), src(G05), src(G07))
            )),
            TextbookTopic(8, "Các số đến 10 000", listOf(
                b(45, "Các số có bốn chữ số. Số 10 000", NUM, src(G06, "Làm tròn"), gap = true),
                b(46, "So sánh các số trong phạm vi 10 000", NUM, src(G06, "Làm tròn"), gap = true),
                b(47, "Làm quen với chữ số La Mã", NUM, src(G06, "La Mã")),
                b(48, "Làm tròn số đến hàng chục, hàng trăm", NUM, src(G06, "đến hàng (chục|trăm) ta")),
                b(49, "Luyện tập chung", NUM, src(G06, exclude = "Tính"))
            )),
            TextbookTopic(9, "Chu vi, diện tích một số hình phẳng", listOf(
                b(50, "Chu vi hình tam giác, hình tứ giác, hình chữ nhật, hình vuông", GEO, src(G207, "chu vi"), src(G09, "Tính chu vi")),
                b(51, "Diện tích của một hình. Xăng-ti-mét vuông", GEO, src(G09, "[Dd]iện tích")),
                b(52, "Diện tích hình chữ nhật, diện tích hình vuông", GEO, src(G09, "[Dd]iện tích")),
                b(53, "Luyện tập chung", GEO, src(G09))
            )),
            TextbookTopic(10, "Cộng, trừ, nhân, chia trong phạm vi 10 000", listOf(
                b(54, "Phép cộng trong phạm vi 10 000", NUM, src(G06, """Tính: .*\+"""), gap = true),
                b(55, "Phép trừ trong phạm vi 10 000", NUM, src(G206, "Tính hiệu"), gap = true),
                b(56, "Nhân số có bốn chữ số với số có một chữ số", ALG, src(G01, "Đặt tính"), gap = true),
                b(57, "Chia số có bốn chữ số cho số có một chữ số", ALG, src(G04, "phép chia có dư"), src(G403, " : "), gap = true),
                b(58, "Luyện tập chung", NUM, src(G06, "Tính"), src(G01), src(G04))
            )),
            TextbookTopic(11, "Các số đến 100 000", listOf(
                b(59, "Các số có năm chữ số. Số 100 000", NUM, src(G06, """Làm tròn số \d+ \d{3} """)),
                b(60, "So sánh các số trong phạm vi 100 000", NUM, src(G06, "Làm tròn"), gap = true),
                b(61, "Làm tròn số đến hàng nghìn, hàng chục nghìn", NUM, src(G06, "đến hàng (nghìn|chục nghìn)")),
                b(62, "Luyện tập chung", NUM, src(G06, exclude = "Tính"))
            )),
            TextbookTopic(12, "Cộng, trừ trong phạm vi 100 000", listOf(
                b(63, "Phép cộng trong phạm vi 100 000", NUM, src(G06, """\+""")),
                b(64, "Phép trừ trong phạm vi 100 000", NUM, src(G206, "Tính hiệu"), gap = true),
                b(65, "Luyện tập chung", NUM, src(G06, """\+"""), src(G206, "Tính hiệu"))
            )),
            TextbookTopic(13, "Xem đồng hồ. Tháng – năm. Tiền Việt Nam", listOf(
                b(66, "Xem đồng hồ. Tháng – năm", DATA, src(G209, "giờ|phút"), src(G109, "lịch")),
                b(67, "Thực hành xem đồng hồ, xem lịch", DATA, src(G209, "giờ"), src(G109)),
                b(68, "Tiền Việt Nam", DATA, src(G209, "đồng"), src(G07, "đồng")),
                b(69, "Luyện tập chung", DATA, src(G209), src(G07, "đồng"))
            )),
            TextbookTopic(14, "Nhân, chia trong phạm vi 100 000", listOf(
                b(70, "Nhân số có năm chữ số với số có một chữ số", ALG, src(G01, "Đặt tính"), gap = true),
                b(71, "Chia số có năm chữ số cho số có một chữ số", ALG, src(G04, "phép chia có dư"), gap = true),
                b(72, "Luyện tập chung", ALG, src(G01), src(G04))
            )),
            TextbookTopic(15, "Làm quen với yếu tố thống kê, xác suất", listOf(
                b(73, "Thu thập, phân loại, ghi chép số liệu. Bảng số liệu", DATA, src(G10, "Bảng thống kê")),
                b(74, "Khả năng xảy ra của một sự kiện", DATA, src(G210, "hộp")),
                b(75, "Thực hành và trải nghiệm thu thập, phân loại, ghi chép số liệu, đọc bảng số liệu", DATA, src(G10, "Bảng thống kê"), src(G210))
            )),
            TextbookTopic(16, "Ôn tập cuối năm", listOf(
                b(76, "Ôn tập các số trong phạm vi 10 000, 100 000", NUM, src(G06, exclude = "Tính")),
                b(77, "Ôn tập phép cộng, phép trừ trong phạm vi 100 000", NUM, src(G06, "Tính"), src(G206, "Tính")),
                b(78, "Ôn tập phép nhân, phép chia trong phạm vi 100 000", ALG, src(G01), src(G04)),
                b(79, "Ôn tập hình học và đo lường", GEO, src(G08), src(G09), src(G10)),
                b(80, "Ôn tập bảng số liệu, khả năng xảy ra của một sự kiện", DATA, src(G10, "Bảng"), src(G210, "hộp")),
                b(81, "Ôn tập chung", NUM, src(G02), src(G05), src(G07), src(G09))
            ))
        )
    )
}
