package com.example.toanhoc.domain.curriculum

/**
 * Toán 1 – Kết nối tri thức với cuộc sống: 10 chủ đề, 41 bài (tên bài theo mục lục sách).
 * Lời giảng, ví dụ, mẹo trong `teaching` do app tự soạn – không chép từ sách.
 *
 * Nhóm kiến thức của bộ sinh đề (nodeId) dùng cho Lớp 1:
 *   G1_01 hình phẳng/khối · G1_02 số 0–10, so sánh · G1_03 tách–gộp · G1_04 cộng/trừ trong 10
 *   G1_05 số đến 100 · G1_06 cộng/trừ không nhớ trong 100 · G1_07 toán có lời văn
 *   G1_08 đo độ dài · G1_09 giờ, lịch · G1_10 quy luật, dãy số
 */
object Grade1Curriculum {

    private const val NUM = "ARITHMETIC"
    private const val GEO = "GEOMETRY_MEASUREMENT"
    private const val PAT = "HEURISTICS_COMBINATORICS"
    private const val TIME = "DATA_STATS_PROBABILITY"

    private fun src(node: String, include: String? = null, exclude: String? = null, max: Int? = null, carry: Boolean? = null) =
        PracticeSource(
            nodeId = node,
            filter = QuestionFilter(include?.let(::Regex), exclude?.let(::Regex), max, carry)
        )

    private const val G01 = "G1_01_SPATIAL_SHAPES"
    private const val G02 = "G1_02_NUM_0_10_COMPARE"
    private const val G03 = "G1_03_SPLIT_COMBINE_10"
    private const val G04 = "G1_04_ADD_SUB_10_MEANING"
    private const val G05 = "G1_05_PLACE_VALUE_100"
    private const val G06 = "G1_06_ADD_SUB_100_NO_CARRY"
    private const val G07 = "G1_07_WORD_PROBLEMS_G1"
    private const val G08 = "G1_08_MEASURE_LENGTH_CM"
    private const val G09 = "G1_09_TIME_CLOCK_CALENDAR"
    private const val G10 = "G1_10_PATTERNS_COUNTING"

    private fun b(n: Int, title: String, domain: String, vararg s: PracticeSource, teaching: LessonTeaching? = null) =
        TextbookLesson("L1_B$n", n, title, domain, s.toList(), teaching)

    /* ----------------------------- Nội dung dạy: Bài 10 ----------------------------- */

    private val teachAddition10 = LessonTeaching(
        cards = listOf(
            TeachCard(
                title = "Gộp lại thì làm phép cộng",
                body = "Có 3 quả bóng đỏ và 2 quả bóng xanh. Gộp tất cả lại, ta có 5 quả bóng.\n" +
                    "Ta viết: 3 + 2 = 5\nĐọc là: ba cộng hai bằng năm.",
                diagramType = "SINGAPORE_BAR_MODEL",
                diagramStatement = "Gộp 3 và 2 được số mấy?"
            ),
            TeachCard(
                title = "Thêm vào cũng là phép cộng",
                body = "Trên cành có 4 con chim. Bay đến thêm 3 con nữa.\n" +
                    "Bây giờ có tất cả: 4 + 3 = 7 con chim.",
                diagramType = "NUMBER_LINE",
                diagramStatement = "Tính: 4 + 3 = ?",
                diagramExpr = "4 + 3"
            ),
            TeachCard(
                title = "Cách tính ở lớp: đếm thêm",
                body = "Nhớ số thứ nhất trong đầu, rồi đếm tiếp thêm số thứ hai.\n" +
                    "Ví dụ 6 + 2: nhớ 6, đếm tiếp 7, 8.\nVậy 6 + 2 = 8.",
                diagramType = "NUMBER_LINE",
                diagramStatement = "Tính: 6 + 2 = ?",
                diagramExpr = "6 + 2"
            ),
            TeachCard(
                title = "Cộng với số 0",
                body = "Số nào cộng với 0 cũng bằng chính số đó.\n5 + 0 = 5\n0 + 7 = 7"
            )
        ),
        tips = listOf(
            "Đổi chỗ hai số, kết quả không đổi: 2 + 7 = 7 + 2 = 9. Hãy bắt đầu đếm từ số lớn hơn cho nhanh.",
            "Thuộc các cặp số cộng lại bằng 10: 1 và 9, 2 và 8, 3 và 7, 4 và 6, 5 và 5.",
            "Chưa nhẩm được thì dùng ngón tay: giơ ngón tay cho số thứ nhất, giơ thêm cho số thứ hai, rồi đếm tất cả."
        ),
        tryIt = listOf(
            TryQuestion("Tính: 5 + 3 = ?", listOf("7", "8", "9", "2"), "8", "Nhớ 5, đếm tiếp 3 số: 6, 7, 8."),
            TryQuestion(
                "Lan có 2 quả cam, mẹ cho Lan thêm 4 quả. Lan có tất cả mấy quả cam?",
                listOf("5", "6", "7", "2"), "6",
                "Đổi chỗ thành 4 + 2 cho dễ: nhớ 4, đếm tiếp 5, 6."
            ),
            TryQuestion("Tính: 0 + 9 = ?", listOf("0", "8", "9", "10"), "9", "Cộng với 0 thì giữ nguyên số đó.")
        )
    )

    /* ------------------------------------ Mục lục ------------------------------------ */

    private const val ADD_ONLY = """^Tính: \d+ \+ \d+ = \?$|: \d+ \+ \? = \d+$"""
    private const val SUB_ONLY = """^Tính: \d+ - \d+ = \?$|: \d+ - \? = \d+$"""

    val data = GradeCurriculum(
        grade = 1,
        semester2FromTopic = 6,
        topics = listOf(
            TextbookTopic(1, "Các số từ 0 đến 10", listOf(
                b(1, "Các số 0, 1, 2, 3, 4, 5", NUM, src(G02, "liền (sau|trước)|lớn nhất|bé nhất", max = 5)),
                b(2, "Các số 6, 7, 8, 9, 10", NUM, src(G02, "liền (sau|trước)|lớn nhất|bé nhất")),
                b(3, "Nhiều hơn, ít hơn, bằng nhau", NUM, src(G02, "so sánh")),
                b(4, "So sánh số", NUM, src(G02, "so sánh|lớn nhất|bé nhất|Sắp xếp")),
                b(5, "Mấy và mấy", NUM, src(G03)),
                b(6, "Luyện tập chung", NUM, src(G02), src(G03))
            )),
            TextbookTopic(2, "Làm quen với một số hình phẳng", listOf(
                b(7, "Hình vuông, hình tròn, hình tam giác, hình chữ nhật", GEO, src(G01, "Hình phẳng|Trong thực tế")),
                b(8, "Thực hành lắp ghép, xếp hình", GEO, src(G01, "Rô-bốt xếp"), src(G10, "quy luật")),
                b(9, "Luyện tập chung", GEO, src(G01, "Hình phẳng|Trong thực tế|Rô-bốt xếp"), src(G10, "quy luật"))
            )),
            TextbookTopic(3, "Phép cộng, phép trừ trong phạm vi 10", listOf(
                b(10, "Phép cộng trong phạm vi 10", NUM, src(G04, ADD_ONLY), src(G03, "Gộp"), teaching = teachAddition10),
                b(11, "Phép trừ trong phạm vi 10", NUM, src(G04, SUB_ONLY), src(G03, "Tách|chia vào")),
                b(12, "Bảng cộng, bảng trừ trong phạm vi 10", NUM, src(G04, """^Tính: \d+ [+-] \d+ = \?$""")),
                b(13, "Luyện tập chung", NUM, src(G04), src(G03))
            )),
            TextbookTopic(4, "Làm quen với một số hình khối", listOf(
                b(14, "Khối lập phương, khối hộp chữ nhật", GEO, src(G01, "khối hình học|Đồ vật")),
                b(15, "Vị trí, định hướng trong không gian", GEO, src(G01, "Rô-bốt xếp")),
                b(16, "Luyện tập chung", GEO, src(G01))
            )),
            TextbookTopic(5, "Ôn tập học kì 1", listOf(
                b(17, "Ôn tập các số trong phạm vi 10", NUM, src(G02), src(G03)),
                b(18, "Ôn tập phép cộng, phép trừ trong phạm vi 10", NUM, src(G04)),
                b(19, "Ôn tập hình học", GEO, src(G01), src(G10, "quy luật")),
                b(20, "Ôn tập chung", NUM, src(G02), src(G04), src(G01))
            )),
            TextbookTopic(6, "Các số đến 100", listOf(
                b(21, "Số có hai chữ số", NUM, src(G05, "chục|được viết là")),
                b(22, "So sánh số có hai chữ số", NUM, src(G05, "lớn nhất")),
                b(23, "Bảng các số từ 1 đến 100", PAT, src(G10, "dãy số"), src(G05, "chục")),
                b(24, "Luyện tập chung", NUM, src(G05), src(G10, "dãy số"))
            )),
            TextbookTopic(7, "Độ dài và đo độ dài", listOf(
                b(25, "Dài hơn, ngắn hơn", GEO, src(G08, "vạch|dài")),
                b(26, "Đơn vị đo độ dài", GEO, src(G08, "cm")),
                b(27, "Thực hành ước lượng và đo độ dài", GEO, src(G08, "vạch")),
                b(28, "Luyện tập chung", GEO, src(G08))
            )),
            TextbookTopic(8, "Phép cộng, phép trừ (không nhớ) trong phạm vi 100", listOf(
                b(29, "Phép cộng số có hai chữ số với số có một chữ số", NUM, src(G06, """(?<!\d)\d{2} \+ \d(?!\d)""", carry = false)),
                b(30, "Phép cộng số có hai chữ số với số có hai chữ số", NUM, src(G06, """(?<!\d)\d{2} \+ \d{2}(?!\d)""", carry = false)),
                b(31, "Phép trừ số có hai chữ số cho số có một chữ số", NUM, src(G06, """(?<!\d)\d{2} - \d(?!\d)""", carry = false)),
                b(32, "Phép trừ số có hai chữ số cho số có hai chữ số", NUM, src(G06, """(?<!\d)\d{2} - \d{2}(?!\d)""", carry = false)),
                b(33, "Luyện tập chung", NUM, src(G06), src(G07))
            )),
            TextbookTopic(9, "Thời gian. Giờ và lịch", listOf(
                b(34, "Xem giờ đúng trên đồng hồ", TIME, src(G09, "giờ|kim")),
                b(35, "Ngày trong tuần", TIME, src(G09, "Thứ|tuần")),
                b(36, "Thực hành xem lịch và giờ", TIME, src(G09)),
                b(37, "Luyện tập chung", TIME, src(G09))
            )),
            TextbookTopic(10, "Ôn tập cuối năm", listOf(
                b(38, "Ôn tập các số và phép tính trong phạm vi 10", NUM, src(G02), src(G03), src(G04)),
                b(39, "Ôn tập các số và phép tính trong phạm vi 100", NUM, src(G05), src(G06), src(G07)),
                b(40, "Ôn tập hình học và đo lường", GEO, src(G01), src(G08)),
                b(41, "Ôn tập chung", NUM, src(G04), src(G06), src(G07), src(G09))
            ))
        )
    )
}
