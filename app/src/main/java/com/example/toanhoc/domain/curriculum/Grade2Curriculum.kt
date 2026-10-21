package com.example.toanhoc.domain.curriculum

/**
 * Toán 2 – Kết nối tri thức với cuộc sống: 14 chủ đề, 75 bài (tên bài theo mục lục sách).
 * Lời giảng, ví dụ, mẹo trong `teaching` do app tự soạn – không chép từ sách.
 *
 * Nhóm kiến thức của bộ sinh đề dùng cho Lớp 2 (kèm vài nhóm Lớp 1 cho phần ôn tập đầu năm):
 *   G2_01 thành phần phép tính, tia số · G2_02 cộng/trừ qua 10 · G2_03 cộng/trừ có nhớ trong 100
 *   G2_04 nhiều hơn/ít hơn · G2_05 nhân/chia 2, 5 · G2_06 số và phép tính trong 1000
 *   G2_07 hình học · G2_08 dm, m, km, kg, l · G2_09 giờ, tiền · G2_10 thống kê, khả năng
 */
object Grade2Curriculum {

    private const val NUM = "ARITHMETIC"
    private const val GEO = "GEOMETRY_MEASUREMENT"
    private const val DATA = "DATA_STATS_PROBABILITY"
    private const val ALG = "ALGEBRAIC_THINKING"

    private fun src(node: String, include: String? = null, exclude: String? = null, max: Int? = null, carry: Boolean? = null) =
        PracticeSource(node, QuestionFilter(include?.let(::Regex), exclude?.let(::Regex), max, carry))

    private const val G105 = "G1_05_PLACE_VALUE_100"
    private const val G106 = "G1_06_ADD_SUB_100_NO_CARRY"
    private const val G107 = "G1_07_WORD_PROBLEMS_G1"
    private const val G108 = "G1_08_MEASURE_LENGTH_CM"
    private const val G109 = "G1_09_TIME_CLOCK_CALENDAR"
    private const val G101 = "G1_01_SPATIAL_SHAPES"
    private const val G01 = "G2_01_TERMS_AND_NUMBER_LINE"
    private const val G02 = "G2_02_MAKE_10_ADD_SUB"
    private const val G03 = "G2_03_ADD_SUB_100_WITH_CARRY"
    private const val G04 = "G2_04_WORD_MORE_LESS"
    private const val G05 = "G2_05_MUL_DIV_2_5"
    private const val G06 = "G2_06_NUM_1000_ADD_SUB"
    private const val G07 = "G2_07_GEOMETRY_G2"
    private const val G08 = "G2_08_MEASURE_DM_M_KM_KG_L"
    private const val G09 = "G2_09_TIME_MONEY_G2"
    private const val G10 = "G2_10_STATS_PROBABILITY_G2"

    private fun b(n: Int, title: String, domain: String, vararg s: PracticeSource, teaching: LessonTeaching? = null, gap: Boolean = false) =
        TextbookLesson("L2_B$n", n, title, domain, s.toList(), teaching, gap)

    /* ------------------------ Nội dung dạy: Bài 7 (cộng qua 10) ------------------------ */

    private val teachAddOver10 = LessonTeaching(
        cards = listOf(
            TeachCard(
                title = "Cộng qua 10 là gì?",
                body = "Khi cộng hai số mà kết quả lớn hơn 10, ta gọi là cộng qua 10.\n" +
                    "Ví dụ: 9 + 5, 8 + 6, 7 + 7.\nBí quyết: gom cho đủ 10 trước, rồi cộng phần còn lại.",
                diagramType = "SINGAPORE_BAR_MODEL",
                diagramStatement = "Gộp 9 và 5 được số mấy?"
            ),
            TeachCard(
                title = "Cách làm ở lớp: tách để đủ 10",
                body = "9 + 5 = ?\n• 9 cần thêm 1 nữa thì thành 10.\n• Tách 5 thành 1 và 4.\n" +
                    "• 9 + 1 = 10, rồi 10 + 4 = 14.\nVậy 9 + 5 = 14.",
                diagramType = "NUMBER_LINE",
                diagramStatement = "Tính: 9 + 1 + 4 = ?",
                diagramExpr = "9 + 1 + 4"
            ),
            TeachCard(
                title = "Làm lại với 8 + 6",
                body = "• 8 cần thêm 2 nữa thì thành 10.\n• Tách 6 thành 2 và 4.\n" +
                    "• 8 + 2 = 10, rồi 10 + 4 = 14.\nVậy 8 + 6 = 14.",
                diagramType = "NUMBER_LINE",
                diagramStatement = "Tính: 8 + 2 + 4 = ?",
                diagramExpr = "8 + 2 + 4"
            ),
            TeachCard(
                title = "10 cộng mấy thì rất dễ",
                body = "Các số từ 11 đến 19 đều là 10 cộng thêm một số:\n" +
                    "10 + 3 = 13\n10 + 7 = 17\nNên chỉ cần gom được 10 là tính ra ngay."
            )
        ),
        tips = listOf(
            "Thuộc các cặp số cộng lại bằng 10: 1 và 9, 2 và 8, 3 và 7, 4 và 6, 5 và 5 – tách số sẽ rất nhanh.",
            "Giữ số lớn, tách số bé để bù cho số lớn tròn 10: 9 + 5 thì tách 5, không tách 9.",
            "Đổi chỗ hai số, kết quả không đổi: 5 + 9 = 9 + 5. Gặp 5 + 9 cứ tính 9 + 5 cho dễ."
        ),
        tryIt = listOf(
            TryQuestion("Tính: 9 + 4 = ?", listOf("12", "13", "14", "5"), "13", "9 thêm 1 thành 10, còn lại 3: 10 + 3 = 13."),
            TryQuestion("Tính: 8 + 5 = ?", listOf("3", "12", "13", "14"), "13", "8 thêm 2 thành 10, còn lại 3: 10 + 3 = 13."),
            TryQuestion("Tính: 7 + 7 = ?", listOf("13", "14", "15", "12"), "14", "7 thêm 3 thành 10, còn lại 4: 10 + 4 = 14.")
        )
    )

    /* ------------------------------------ Mục lục ------------------------------------ */

    val data = GradeCurriculum(
        grade = 2,
        semester2FromTopic = 8,
        topics = listOf(
            TextbookTopic(1, "Ôn tập và bổ sung", listOf(
                b(1, "Ôn tập các số đến 100", NUM, src(G105)),
                b(2, "Tia số. Số liền trước, số liền sau", NUM, src(G01, "liền")),
                b(3, "Các thành phần của phép cộng, phép trừ", NUM, src(G01, "được gọi là|Biết")),
                b(4, "Hơn, kém nhau bao nhiêu", NUM, src(G04, "nặng hơn")),
                b(5, "Ôn tập phép cộng, phép trừ (không nhớ) trong phạm vi 100", NUM, src(G106)),
                b(6, "Luyện tập chung", NUM, src(G01), src(G106), src(G105))
            )),
            TextbookTopic(2, "Phép cộng, phép trừ trong phạm vi 20", listOf(
                b(7, "Phép cộng (qua 10) trong phạm vi 20", NUM, src(G02, """\+"""), teaching = teachAddOver10),
                b(8, "Bảng cộng (qua 10)", NUM, src(G02, """Tính nhẩm.*\+""")),
                b(9, "Bài toán về thêm, bớt một số đơn vị", NUM, src(G107, "thêm|còn lại"), src(G02, "bông hoa"), gap = true),
                b(10, "Luyện tập chung", NUM, src(G02), src(G107)),
                b(11, "Phép trừ (qua 10) trong phạm vi 20", NUM, src(G02, " - ")),
                b(12, "Bảng trừ (qua 10)", NUM, src(G02, "Tính nhẩm.* - ")),
                b(13, "Bài toán về nhiều hơn, ít hơn một số đơn vị", NUM, src(G04, "nhiều hơn|ít hơn")),
                b(14, "Luyện tập chung", NUM, src(G02), src(G04))
            )),
            TextbookTopic(3, "Làm quen với khối lượng, dung tích", listOf(
                b(15, "Ki-lô-gam", GEO, src(G08, "kg")),
                b(16, "Lít", GEO, src(G08, """\d l """)),
                b(17, "Thực hành và trải nghiệm với các đơn vị ki-lô-gam, lít", GEO, src(G08, """kg|\d l """)),
                b(18, "Luyện tập chung", GEO, src(G08, """kg|\d l """), src(G04, "kg"))
            )),
            TextbookTopic(4, "Phép cộng, phép trừ (có nhớ) trong phạm vi 100", listOf(
                b(19, "Phép cộng (có nhớ) số có hai chữ số với số có một chữ số", NUM,
                    // Bộ sinh đề chưa có dạng "2 chữ số + 1 chữ số có nhớ" → tạm dùng cộng qua 10 + cộng có nhớ
                    src(G02, """\+"""), src(G03, """\+""", carry = true), gap = true),
                b(20, "Phép cộng (có nhớ) số có hai chữ số với số có hai chữ số", NUM,
                    src(G03, """(?<!\d)\d{2} \+ \d{2}(?!\d)""", carry = true)),
                b(21, "Luyện tập chung", NUM, src(G03, """\+""", carry = true)),
                b(22, "Phép trừ (có nhớ) số có hai chữ số cho số có một chữ số", NUM,
                    // Bộ sinh đề chưa có dạng "2 chữ số − 1 chữ số có nhớ" → tạm dùng trừ qua 10 + trừ có nhớ
                    src(G02, " - "), src(G03, " - ", carry = true), gap = true),
                b(23, "Phép trừ (có nhớ) số có hai chữ số cho số có hai chữ số", NUM,
                    src(G03, """(?<!\d)\d{2} - \d{2}(?!\d)""", carry = true)),
                b(24, "Luyện tập chung", NUM, src(G03), src(G04))
            )),
            TextbookTopic(5, "Làm quen với hình phẳng", listOf(
                b(25, "Điểm, đoạn thẳng, đường thẳng, đường cong, ba điểm thẳng hàng", GEO, src(G07, "gấp khúc"), src(G101, "Hình phẳng"), gap = true),
                b(26, "Đường gấp khúc. Hình tứ giác", GEO, src(G07, "gấp khúc|tứ giác")),
                b(27, "Thực hành gấp, cắt, ghép, xếp hình. Vẽ đoạn thẳng", GEO, src(G07, "gấp khúc"), src(G101, "Rô-bốt xếp|Hình phẳng")),
                b(28, "Luyện tập chung", GEO, src(G07, "gấp khúc|tứ giác|tam giác"))
            )),
            TextbookTopic(6, "Ngày – giờ, giờ – phút, ngày – tháng", listOf(
                b(29, "Ngày – giờ, giờ – phút", DATA, src(G09, "giờ|phút")),
                b(30, "Ngày – tháng", DATA, src(G109, "Tờ lịch|Thứ|tuần")),
                b(31, "Thực hành và trải nghiệm xem đồng hồ, xem lịch", DATA, src(G09, "giờ"), src(G109, "lịch")),
                b(32, "Luyện tập chung", DATA, src(G09, "giờ|phút"), src(G109))
            )),
            TextbookTopic(7, "Ôn tập học kì 1", listOf(
                b(33, "Ôn tập phép cộng, phép trừ trong phạm vi 20, 100", NUM, src(G02), src(G03)),
                b(34, "Ôn tập hình phẳng", GEO, src(G07, "gấp khúc|tứ giác|tam giác")),
                b(35, "Ôn tập đo lường", GEO, src(G08), src(G09, "giờ")),
                b(36, "Ôn tập chung", NUM, src(G02), src(G03), src(G04), src(G07))
            )),
            TextbookTopic(8, "Phép nhân, phép chia", listOf(
                b(37, "Phép nhân", ALG, src(G05, "Mỗi bàn| × ")),
                b(38, "Thừa số, tích", ALG, src(G05, "phép nhân")),
                b(39, "Bảng nhân 2", ALG, src(G05, """^Tính: (2 × \d+|\d+ × 2) = \?$""")),
                b(40, "Bảng nhân 5", ALG, src(G05, """^Tính: (5 × \d+|\d+ × 5) = \?$""")),
                b(41, "Phép chia", ALG, src(G05, "chia đều| : ")),
                b(42, "Số bị chia, số chia, thương", ALG, src(G05, "phép chia")),
                b(43, "Bảng chia 2", ALG, src(G05, """^Tính: \d+ : 2 = \?$""")),
                b(44, "Bảng chia 5", ALG, src(G05, """^Tính: \d+ : 5 = \?$""")),
                b(45, "Luyện tập chung", ALG, src(G05))
            )),
            TextbookTopic(9, "Làm quen với hình khối", listOf(
                b(46, "Khối trụ, khối cầu", GEO, src(G07, "hình khối")),
                b(47, "Luyện tập chung", GEO, src(G07, "hình khối"), src(G101, "khối hình học|Đồ vật"))
            )),
            TextbookTopic(10, "Các số trong phạm vi 1000", listOf(
                b(48, "Đơn vị, chục, trăm, nghìn", NUM, src(G06, "trăm")),
                b(49, "Các số tròn trăm, tròn chục", NUM, src(G06, "trăm|liền")),
                b(50, "So sánh các số tròn trăm, tròn chục", NUM, src(G06, "lớn nhất|bé nhất")),
                b(51, "Số có ba chữ số", NUM, src(G06, "trăm|liền")),
                b(52, "Viết số thành tổng các trăm, chục, đơn vị", NUM, src(G06, "trăm")),
                b(53, "So sánh các số có ba chữ số", NUM, src(G06, "lớn nhất|bé nhất")),
                b(54, "Luyện tập chung", NUM, src(G06, exclude = "Tính"))
            )),
            TextbookTopic(11, "Độ dài và đơn vị đo độ dài. Tiền Việt Nam", listOf(
                b(55, "Đề-xi-mét. Mét. Ki-lô-mét", GEO, src(G08, """dm|km|\d m """)),
                b(56, "Giới thiệu tiền Việt Nam", GEO, src(G09, "đồng")),
                b(57, "Thực hành và trải nghiệm đo độ dài", GEO, src(G08, """dm|km|\d m """), src(G108)),
                b(58, "Luyện tập chung", GEO, src(G08, """dm|km|\d m """), src(G09, "đồng"))
            )),
            TextbookTopic(12, "Phép cộng, phép trừ trong phạm vi 1000", listOf(
                b(59, "Phép cộng (không nhớ) trong phạm vi 1000", NUM, src(G06, "Tính tổng", carry = false)),
                b(60, "Phép cộng (có nhớ) trong phạm vi 1000", NUM, src(G06, "Tính tổng", carry = true)),
                b(61, "Phép trừ (không nhớ) trong phạm vi 1000", NUM, src(G06, "Tính hiệu", carry = false)),
                b(62, "Phép trừ (có nhớ) trong phạm vi 1000", NUM, src(G06, "Tính hiệu", carry = true)),
                b(63, "Luyện tập chung", NUM, src(G06, "Tính"))
            )),
            TextbookTopic(13, "Làm quen với yếu tố thống kê, xác suất", listOf(
                b(64, "Thu thập, phân loại, kiểm đếm số liệu", DATA, src(G10, "kiểm đếm")),
                b(65, "Biểu đồ tranh", DATA, src(G10, "kiểm đếm"), gap = true),
                b(66, "Chắc chắn, có thể, không thể", DATA, src(G10, "hộp")),
                b(67, "Thực hành và trải nghiệm thu thập, phân loại, kiểm đếm số liệu", DATA, src(G10))
            )),
            TextbookTopic(14, "Ôn tập cuối năm", listOf(
                b(68, "Ôn tập các số trong phạm vi 1000", NUM, src(G06, exclude = "Tính")),
                b(69, "Ôn tập phép cộng, phép trừ trong phạm vi 100", NUM, src(G03), src(G02)),
                b(70, "Ôn tập phép cộng, phép trừ trong phạm vi 1000", NUM, src(G06, "Tính")),
                b(71, "Ôn tập phép nhân, phép chia", ALG, src(G05)),
                b(72, "Ôn tập hình học", GEO, src(G07)),
                b(73, "Ôn tập đo lường", GEO, src(G08), src(G09)),
                b(74, "Ôn tập kiểm đếm số liệu và lựa chọn khả năng", DATA, src(G10)),
                b(75, "Ôn tập chung", NUM, src(G03), src(G05), src(G06), src(G07))
            ))
        )
    )
}
