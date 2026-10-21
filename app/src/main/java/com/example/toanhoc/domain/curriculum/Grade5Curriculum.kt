package com.example.toanhoc.domain.curriculum

/**
 * Toán 5 – Kết nối tri thức với cuộc sống: 12 chủ đề, 75 bài (tên bài theo mục lục sách).
 * Lời giảng trong `teaching` do app tự soạn. `practiceGap = true`: bộ sinh đề chưa có dạng đề riêng.
 */
object Grade5Curriculum {

    private const val NUM = "ARITHMETIC"
    private const val GEO = "GEOMETRY_MEASUREMENT"
    private const val DATA = "DATA_STATS_PROBABILITY"
    private const val ALG = "ALGEBRAIC_THINKING"
    private const val PAT = "HEURISTICS_COMBINATORICS"

    private fun src(node: String, include: String? = null, exclude: String? = null, max: Int? = null, carry: Boolean? = null) =
        PracticeSource(node, QuestionFilter(include?.let(::Regex), exclude?.let(::Regex), max, carry))

    private const val G209 = "G2_09_TIME_MONEY_G2"
    private const val G308 = "G3_08_GEOMETRY_MIDPOINT_CIRCLE"
    private const val G309 = "G3_09_PERIMETER_AREA_RECT_SQUARE"
    private const val G401 = "G4_01_MILLIONS_CLASS_ROUNDING"
    private const val G402 = "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS"
    private const val G403 = "G4_03_MUL_DIV_2_DIGITS"
    private const val G406 = "G4_06_FRACTIONS_CONCEPT_COMPARE"
    private const val G407 = "G4_07_FRACTIONS_FOUR_OPS"
    private const val G408 = "G4_08_ANGLES_PARALLEL_SHAPES"
    private const val G409 = "G4_09_MEASURE_TON_M2_CENTURY"
    private const val G410 = "G4_10_BAR_CHART_PROBABILITY"
    private const val G01 = "G5_01_DECIMAL_FRACTIONS_MIXED"
    private const val G02 = "G5_02_DECIMALS_PLACE_ROUNDING"
    private const val G03 = "G5_03_DECIMALS_FOUR_OPS"
    private const val G04 = "G5_04_SUM_DIFF_RATIO_PROBLEMS"
    private const val G05 = "G5_05_PERCENTAGE_MAP_SCALE"
    private const val G06 = "G5_06_TRIANGLE_TRAPEZOID_CIRCLE"
    private const val G07 = "G5_07_SURFACE_AREA_VOLUME_3D"
    private const val G08 = "G5_08_TIME_OPS_AND_MOTION"
    private const val G10 = "G5_10_COMPREHENSIVE_HEURISTICS"

    private fun b(n: Int, title: String, domain: String, vararg s: PracticeSource, teaching: LessonTeaching? = null, gap: Boolean = false) =
        TextbookLesson("L5_B$n", n, title, domain, s.toList(), teaching, gap)

    /* ------------------- Nội dung dạy: Bài 19 (cộng số thập phân) ------------------- */

    private val teachDecimalAdd = LessonTeaching(
        cards = listOf(
            TeachCard(
                title = "Bước 1: đặt thẳng dấu phẩy",
                body = "Viết số hạng này dưới số hạng kia sao cho các DẤU PHẨY thẳng cột với nhau.\n" +
                    "Khi đó hàng phần mười thẳng hàng phần mười, hàng đơn vị thẳng hàng đơn vị.",
                diagramType = "VERTICAL_ARITHMETIC",
                diagramStatement = "Đặt tính rồi tính: 12,5 + 3,75 = ?",
                diagramExpr = "12,5 + 3,75"
            ),
            TeachCard(
                title = "Bước 2: cộng như số tự nhiên",
                body = "Cộng từ phải sang trái như cộng các số tự nhiên, nhớ sang hàng bên trái nếu tổng một cột từ 10 trở lên.\n" +
                    "12,50 + 3,75: 0 + 5 = 5; 5 + 7 = 12 viết 2 nhớ 1; 2 + 3 + 1 = 6; 1 + 0 = 1."
            ),
            TeachCard(
                title = "Bước 3: đặt dấu phẩy ở tổng",
                body = "Dấu phẩy của tổng đặt thẳng cột với các dấu phẩy ở trên.\nVậy 12,5 + 3,75 = 16,25."
            ),
            TeachCard(
                title = "Số tự nhiên cộng số thập phân",
                body = "Coi số tự nhiên như có \",0\" ở sau: 7 = 7,0.\nVí dụ: 7 + 0,5 = 7,0 + 0,5 = 7,5."
            )
        ),
        tips = listOf(
            "Ước lượng trước khi tính: 12,5 + 3,75 khoảng 12 + 4 = 16. Kết quả 16,25 là hợp lý; ra 1,625 hay 162,5 là sai chỗ dấu phẩy.",
            "Phần thập phân ngắn hơn thì thêm chữ số 0 vào cuối cho đủ (12,5 = 12,50) – giá trị không đổi mà cột thẳng hơn.",
            "Đổi chỗ các số hạng, tổng không đổi: chọn cách cộng cho tròn trước, vd. 2,7 + 4,5 + 0,3 = (2,7 + 0,3) + 4,5 = 7,5."
        ),
        tryIt = listOf(
            TryQuestion("Tính: 2,5 + 1,3 = ?", listOf("3,8", "3,7", "38", "4,8"), "3,8", "Thẳng dấu phẩy: 5 + 3 = 8 ở hàng phần mười, 2 + 1 = 3 ở hàng đơn vị."),
            TryQuestion("Tính: 4,75 + 1,2 = ?", listOf("5,95", "4,87", "5,77", "6,95"), "5,95", "Viết 1,2 thành 1,20 rồi cộng: 4,75 + 1,20 = 5,95."),
            TryQuestion("Tính: 7 + 0,5 = ?", listOf("0,57", "7,5", "1,2", "75"), "7,5", "7 = 7,0. Vậy 7,0 + 0,5 = 7,5.")
        )
    )

    val data = GradeCurriculum(
        grade = 5,
        semester2FromTopic = 7,
        topics = listOf(
            TextbookTopic(1, "Ôn tập và bổ sung", listOf(
                b(1, "Ôn tập số tự nhiên", NUM, src(G401)),
                b(2, "Ôn tập các phép tính với số tự nhiên", NUM, src(G403), src(G402)),
                b(3, "Ôn tập phân số", NUM, src(G406)),
                b(4, "Phân số thập phân", NUM, src(G406), src(G02), gap = true),
                b(5, "Ôn tập các phép tính với phân số", NUM, src(G407)),
                b(6, "Cộng, trừ hai phân số khác mẫu số", NUM, src(G407, """\+""")),
                b(7, "Hỗn số", NUM, src(G01)),
                b(8, "Ôn tập hình học và đo lường", GEO, src(G408), src(G409)),
                b(9, "Luyện tập chung", NUM, src(G01), src(G407))
            )),
            TextbookTopic(2, "Số thập phân", listOf(
                b(10, "Khái niệm số thập phân", NUM, src(G02, "thuộc hàng")),
                b(11, "So sánh các số thập phân", NUM, src(G02), gap = true),
                b(12, "Viết số đo đại lượng dưới dạng số thập phân", NUM, src(G02), src(G409), gap = true),
                b(13, "Làm tròn số thập phân", NUM, src(G02, "Làm tròn")),
                b(14, "Luyện tập chung", NUM, src(G02))
            )),
            TextbookTopic(3, "Một số đơn vị đo diện tích", listOf(
                b(15, "Ki-lô-mét vuông. Héc-ta", GEO, src(G409, "m²"), gap = true),
                b(16, "Các đơn vị đo diện tích", GEO, src(G409, "m²")),
                b(17, "Thực hành và trải nghiệm với một số đơn vị đo đại lượng", GEO, src(G409)),
                b(18, "Luyện tập chung", GEO, src(G409))
            )),
            TextbookTopic(4, "Các phép tính với số thập phân", listOf(
                b(19, "Phép cộng số thập phân", NUM, src(G03, """\+"""), teaching = teachDecimalAdd),
                b(20, "Phép trừ số thập phân", NUM, src(G03, " - ")),
                b(21, "Phép nhân số thập phân", NUM, src(G03, "×")),
                b(22, "Phép chia số thập phân", NUM, src(G03, " : ")),
                b(23, "Nhân, chia số thập phân với 10; 100; 1000;... hoặc với 0,1; 0,01; 0,001;...", NUM, src(G03, "Tính nhẩm")),
                b(24, "Luyện tập chung", NUM, src(G03))
            )),
            TextbookTopic(5, "Một số hình phẳng. Chu vi và diện tích", listOf(
                b(25, "Hình tam giác. Diện tích hình tam giác", GEO, src(G06, "tam giác")),
                b(26, "Hình thang. Diện tích hình thang", GEO, src(G06, "thang")),
                b(27, "Đường tròn. Chu vi và diện tích hình tròn", GEO, src(G308, "Hình tròn"), gap = true),
                b(28, "Thực hành và trải nghiệm đo, vẽ, lắp ghép, tạo hình", GEO, src(G06), src(G408, "bình hành|thoi")),
                b(29, "Luyện tập chung", GEO, src(G06), src(G308, "Hình tròn"))
            )),
            TextbookTopic(6, "Ôn tập học kì 1", listOf(
                b(30, "Ôn tập số thập phân", NUM, src(G02)),
                b(31, "Ôn tập các phép tính với số thập phân", NUM, src(G03)),
                b(32, "Ôn tập một số hình phẳng", GEO, src(G06), src(G408, "bình hành|thoi")),
                b(33, "Ôn tập diện tích, chu vi một số hình phẳng", GEO, src(G06), src(G309)),
                b(34, "Ôn tập đo lường", GEO, src(G409)),
                b(35, "Ôn tập chung", NUM, src(G02), src(G03), src(G06))
            )),
            TextbookTopic(7, "Tỉ số và các bài toán liên quan", listOf(
                b(36, "Tỉ số. Tỉ số phần trăm", ALG, src(G05, "%")),
                b(37, "Tỉ lệ bản đồ và ứng dụng", ALG, src(G05, "bản đồ")),
                b(38, "Tìm hai số khi biết tổng và tỉ số của hai số đó", ALG, src(G04, "tổng")),
                b(39, "Tìm hai số khi biết hiệu và tỉ số của hai số đó", ALG, src(G04, "hiệu")),
                b(40, "Tìm tỉ số phần trăm của hai số", ALG, src(G05, "%"), gap = true),
                b(41, "Tìm giá trị phần trăm của một số", ALG, src(G05, """Tìm \d+%""")),
                b(42, "Máy tính cầm tay", NUM, src(G03), gap = true),
                b(43, "Thực hành và trải nghiệm sử dụng máy tính cầm tay", NUM, src(G03), gap = true),
                b(44, "Luyện tập chung", ALG, src(G04), src(G05))
            )),
            TextbookTopic(8, "Thể tích. Đơn vị đo thể tích", listOf(
                b(45, "Thể tích của một hình", GEO, src(G07)),
                b(46, "Xăng-ti-mét khối. Đề-xi-mét khối", GEO, src(G07), gap = true),
                b(47, "Mét khối", GEO, src(G07), gap = true),
                b(48, "Luyện tập chung", GEO, src(G07))
            )),
            TextbookTopic(9, "Diện tích và thể tích của một số hình khối", listOf(
                b(49, "Hình khai triển của hình lập phương, hình hộp chữ nhật và hình trụ", GEO, src(G07), gap = true),
                b(50, "Diện tích xung quanh và diện tích toàn phần của hình hộp chữ nhật", GEO, src(G07, "hộp chữ nhật"), gap = true),
                b(51, "Diện tích xung quanh và diện tích toàn phần của hình lập phương", GEO, src(G07, "lập phương"), gap = true),
                b(52, "Thể tích của hình hộp chữ nhật", GEO, src(G07, "hộp chữ nhật")),
                b(53, "Thể tích của hình lập phương", GEO, src(G07, "lập phương")),
                b(54, "Thực hành tính toán và ước lượng thể tích một số hình khối", GEO, src(G07)),
                b(55, "Luyện tập chung", GEO, src(G07))
            )),
            TextbookTopic(10, "Số đo thời gian, vận tốc. Các bài toán liên quan đến chuyển động đều", listOf(
                b(56, "Các đơn vị đo thời gian", GEO, src(G409, "thế kỷ"), src(G209, "giờ|phút"), gap = true),
                b(57, "Cộng, trừ số đo thời gian", GEO, src(G209, "giờ|phút"), gap = true),
                b(58, "Nhân, chia số đo thời gian với một số", GEO, src(G209, "giờ|phút"), gap = true),
                b(59, "Vận tốc của một chuyển động đều", ALG, src(G08, "Vận tốc")),
                b(60, "Quãng đường, thời gian của một chuyển động đều", ALG, src(G08, "Quãng đường")),
                b(61, "Thực hành tính toán và ước lượng về vận tốc, quãng đường, thời gian trong chuyển động đều", ALG, src(G08)),
                b(62, "Luyện tập chung", ALG, src(G08))
            )),
            TextbookTopic(11, "Một số yếu tố thống kê và xác suất", listOf(
                b(63, "Thu thập, phân loại, sắp xếp các số liệu", DATA, src(G410, "biểu đồ"), gap = true),
                b(64, "Biểu đồ hình quạt tròn", DATA, src(G410, "biểu đồ"), gap = true),
                b(65, "Tỉ số của số lần lặp lại một sự kiện so với tổng số lần thực hiện", DATA, src(G410, "túi"), gap = true),
                b(66, "Thực hành và trải nghiệm thu thập, phân tích, biểu diễn các số liệu thống kê", DATA, src(G410)),
                b(67, "Luyện tập chung", DATA, src(G410))
            )),
            TextbookTopic(12, "Ôn tập cuối năm", listOf(
                b(68, "Ôn tập số tự nhiên, phân số, số thập phân", NUM, src(G401), src(G406), src(G02)),
                b(69, "Ôn tập các phép tính với số tự nhiên, phân số, số thập phân", NUM, src(G403), src(G407), src(G03)),
                b(70, "Ôn tập tỉ số, tỉ số phần trăm", ALG, src(G04), src(G05)),
                b(71, "Ôn tập hình học", GEO, src(G06), src(G07)),
                b(72, "Ôn tập đo lường", GEO, src(G409)),
                b(73, "Ôn tập toán chuyển động đều", ALG, src(G08)),
                b(74, "Ôn tập một số yếu tố thống kê và xác suất", DATA, src(G410)),
                b(75, "Ôn tập chung", PAT, src(G03), src(G04), src(G06), src(G08), src(G10))
            ))
        )
    )
}
