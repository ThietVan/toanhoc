package com.example.toanhoc.domain.generator

import com.example.toanhoc.ui.session.FixedTheoryLesson

/**
 * Registry lý thuyết & bài mẫu thông minh cho đủ 50 node (Toán tiểu học lớp 1-5, CT GDPT 2018).
 *
 * Nguyên tắc thiết kế:
 *  - Đồng bộ 100% với tất cả các nhánh sinh đề (variants) trong ParametricProblemEngine.
 *  - Phân tích ngữ cảnh đề bài (statement) bằng các từ khóa đặc trưng để trả về đúng bài học,
 *    quy tắc và ví dụ mẫu khớp trực tiếp với câu hỏi học sinh đang làm.
 *  - Không dùng contains("x") hay contains(":") đơn lẻ tránh bắt nhầm từ tiếng Việt.
 *  - Hỗ trợ đầy đủ LEGACY_ALIASES đồng bộ với Engine.
 */
object FixedTheoryRegistry {

    private val LEGACY_ALIASES = mapOf(
        "G1_NUM_01" to "G1_03_SPLIT_COMBINE_10",
        "G1_ADD_01" to "G1_04_ADD_SUB_10_MEANING",
        "G1_FENCE_01" to "G1_10_PATTERNS_COUNTING",
        "G2_ADD_01" to "G2_03_ADD_SUB_100_WITH_CARRY",
        "G2_BAR_01" to "G2_04_WORD_MORE_LESS",
        "G3_OPS_01" to "G3_05_ORDER_OF_OPERATIONS",
        "G4_FRAC_01" to "G4_07_FRACTIONS_FOUR_OPS",
        "G5_SPEED_01" to "G5_08_TIME_OPS_AND_MOTION"
    )

    private fun lesson(
        title: String,
        rule: String,
        example: String,
        vararg steps: Pair<String, String>
    ) = FixedTheoryLesson(
        titleVi = title,
        ruleRuleVi = rule,
        exampleStatementVi = example,
        stepsVi = steps.toList()
    )

    private fun has(s: String, vararg keys: String): Boolean =
        keys.any { s.contains(it, ignoreCase = true) }

    /** Lấy phần biểu thức toán học sau dấu ':' trong các câu bắt đầu bằng "Tính" hoặc "Đặt tính". */
    private fun exprPart(s: String): String {
        val i = s.indexOf(':')
        val trimmed = s.trimStart()
        return if ((trimmed.startsWith("Tính") || trimmed.startsWith("Đặt tính")) && i >= 0) {
            s.substring(i + 1)
        } else {
            s
        }
    }

    private val FRACTION_OP = Regex("""\d+/\d+\s*([+×:-])\s*\d+/\d+""")

    fun getLessonForNode(nodeId: String, statement: String = ""): FixedTheoryLesson {
        val targetNodeId = LEGACY_ALIASES[nodeId] ?: nodeId
        return when (targetNodeId) {

            // =====================================================================
            // LỚP 1 (10 NODES - ĐA NHÁNH THEO TỪNG DẠNG BÀI)
            // =====================================================================
            "G1_01_SPATIAL_SHAPES" -> when {
                has(statement, "vị trí", "bên trái", "ở giữa", "bên phải") -> lesson(
                    "Vị trí không gian: Trái - Giữa - Phải",
                    "Quan sát dãy hình theo chiều từ trái sang phải: hình đầu tiên ở bên trái, hình thứ hai ở giữa, hình cuối cùng ở bên phải.",
                    "Cho 3 hình xếp từ trái sang phải: Hình vuông, Khối lập phương, Hình tròn. Hình nào ở giữa?",
                    "Xác định hai hình ở hai đầu" to "Bên trái là Hình vuông, bên phải là Hình tròn.",
                    "Tìm hình ở giữa" to "Hình nằm giữa hai hình trên là Khối lập phương."
                )
                has(statement, "cạnh", "đỉnh", "góc vuông", "không có góc") -> lesson(
                    "Đặc điểm các hình phẳng cơ bản",
                    "Hình tam giác có 3 cạnh và 3 đỉnh. Hình vuông có 4 cạnh bằng nhau. Hình chữ nhật có 2 cạnh dài bằng nhau và 2 cạnh ngắn bằng nhau. Hình tròn có đường bao cong tròn, không có cạnh thẳng.",
                    "Hình nào có 3 cạnh và 3 đỉnh?",
                    "Đếm số cạnh và đỉnh" to "Hình có 3 đoạn thẳng nối với nhau tạo thành 3 đỉnh nhọn.",
                    "Kết luận" to "Đó là Hình tam giác."
                )
                has(statement, "Đồ vật", "thực tế", "xúc xắc", "hộp sữa", "đồng hồ", "lá cờ") -> lesson(
                    "Nhận dạng hình phẳng và khối hình trong thực tế",
                    "Viên xúc xắc, khối rubik có dạng Khối lập phương (6 mặt đều là hình vuông). Hộp sữa, viên gạch có dạng Khối hộp chữ nhật. Mặt đồng hồ tròn có dạng Hình tròn. Lá cờ có dạng Hình chữ nhật.",
                    "Đồ vật 'hộp sữa giấy' có dạng hình gì?",
                    "Quan sát các mặt của hộp sữa" to "Hộp sữa là khối nổi trong không gian, có các mặt là hình chữ nhật.",
                    "Kết luận" to "Hộp sữa giấy có dạng Khối hộp chữ nhật."
                )
                else -> lesson(
                    "Phân biệt Hình phẳng và Khối hình học",
                    "Hình vuông, hình tròn, hình tam giác, hình chữ nhật là các hình phẳng. Khối lập phương và khối hộp chữ nhật là các khối hình học.",
                    "Trong các hình: Hình vuông, Hình tròn, Khối lập phương, Hình tam giác; hình nào là khối?",
                    "Loại trừ các hình phẳng" to "Hình vuông, Hình tròn, Hình tam giác là hình phẳng.",
                    "Chọn khối hình" to "Hình là khối trong không gian là Khối lập phương."
                )
            }

            "G1_02_NUM_0_10_COMPARE" -> when {
                has(statement, "liền sau", "liền trước") -> lesson(
                    "Số liền trước và Số liền sau",
                    "Trên tia số: Số liền sau của một số thì lớn hơn số đó 1 đơn vị (cộng thêm 1). Số liền trước của một số thì bé hơn số đó 1 đơn vị (bớt đi 1).",
                    "Tìm số liền trước và số liền sau của số 6.",
                    "Tìm số liền trước" to "Đếm lùi 1 bước trước số 6 là số 5 (6 - 1 = 5).",
                    "Tìm số liền sau" to "Đếm thêm 1 bước sau số 6 là số 7 (6 + 1 = 7)."
                )
                has(statement, "Sắp xếp", "thứ tự") -> lesson(
                    "Sắp xếp dãy số trong phạm vi 10",
                    "Từ bé đến lớn: viết số bé nhất trước rồi tăng dần đến số lớn nhất. Từ lớn đến bé: viết số lớn nhất trước rồi giảm dần đến số bé nhất.",
                    "Sắp xếp các số 7, 2, 9, 4 theo thứ tự từ bé đến lớn.",
                    "Tìm số bé nhất và lớn nhất" to "Trong 4 số, bé nhất là 2, tiếp theo là 4, rồi đến 7, lớn nhất là 9.",
                    "Viết dãy số" to "Thứ tự từ bé đến lớn là: 2; 4; 7; 9."
                )
                has(statement, "So sánh hai số", "bé hơn") && !has(statement, "bé nhất") -> lesson(
                    "So sánh hai số trong phạm vi 10",
                    "Trên tia số từ 0 đến 10, số nào đứng bên trái thì bé hơn, số nào đứng bên phải thì lớn hơn. Hai số giống nhau thì bằng nhau.",
                    "So sánh hai số 8 và 5.",
                    "Xem vị trí trên tia số" to "Số 8 đứng sau (bên phải) số 5 trên tia số.",
                    "Kết luận" to "Vậy 8 lớn hơn 5 (hay 5 bé hơn 8)."
                )
                else -> lesson(
                    "Tìm số lớn nhất, số bé nhất trong phạm vi 10",
                    "Trong các số từ 0 đến 10: số 0 là số bé nhất, số 10 là số lớn nhất. Số đứng càng xa số 0 về bên phải thì càng lớn.",
                    "Số nào lớn nhất trong các số: 3, 8, 5, 0?",
                    "Sắp xếp trên tia số" to "Các số theo thứ tự từ bé đến lớn là: 0, 3, 5, 8.",
                    "Chọn kết quả" to "Số lớn nhất là 8 (và số bé nhất là 0)."
                )
            }

            "G1_03_SPLIT_COMBINE_10" -> when {
                has(statement, "Gộp") -> lesson(
                    "Gộp hai số trong phạm vi 10",
                    "Gộp hai nhóm đồ vật hay gộp hai số lại với nhau thì ta thực hiện phép tính cộng.",
                    "Gộp 4 và 5 được số mấy?",
                    "Xác định hai phần cần gộp" to "Phần thứ nhất là 4, phần thứ hai là 5.",
                    "Thực hiện gộp" to "4 + 5 = 9. Vậy gộp 4 và 5 được 9."
                )
                has(statement, "gồm mấy và mấy") -> lesson(
                    "Cấu tạo số trong phạm vi 10",
                    "Một số có thể tách thành hai phần sao cho khi gộp hai phần đó lại ta được đúng số ban đầu.",
                    "Số 7 gồm mấy và mấy?",
                    "Thử gộp các cặp số" to "Ta thấy 3 + 4 = 7.",
                    "Kết luận" to "Vậy 7 gồm 3 và 4 (hoặc 4 và 3)."
                )
                else -> lesson(
                    "Tách số trong phạm vi 10",
                    "Khi tách một số thành hai phần: muốn tìm phần còn lại, ta lấy số ban đầu bớt đi (trừ đi) phần đã biết.",
                    "Tách 8 gồm 5 và số mấy?",
                    "Xác định số cần tách" to "Tổng là 8, một phần đã biết là 5.",
                    "Tìm phần còn lại" to "Lấy 8 bớt 5 còn 3 (8 - 5 = 3). Vậy 8 gồm 5 và 3."
                )
            }

            "G1_04_ADD_SUB_10_MEANING" -> {
                val e = exprPart(statement)
                when {
                    has(statement, "dấu ?") -> lesson(
                        "Tìm số còn thiếu trong phép cộng, phép trừ",
                        "Trong phép cộng a + ? = c: ta lấy tổng c bớt đi a. Trong phép trừ a - ? = c: ta lấy a bớt đi c (dựa vào sơ đồ tách - gộp số).",
                        "Tìm số thích hợp thay cho dấu ?: 3 + ? = 8",
                        "Dựa vào tách - gộp số" to "8 gồm 3 và mấy?",
                        "Tính kết quả" to "8 - 3 = 5. Vậy số cần điền vào dấu ? là 5 (kiểm tra: 3 + 5 = 8)."
                    )
                    Regex("""\b0\b""").containsMatchIn(e) || Regex("""(\d+)\s*-\s*\1""").containsMatchIn(e) -> lesson(
                        "Phép cộng và phép trừ với số 0",
                        "Bất kì số nào cộng với 0 cũng bằng chính số đó. Bất kì số nào trừ đi 0 cũng bằng chính số đó. Hai số giống nhau trừ đi nhau thì bằng 0.",
                        "Tính: 6 + 0 = ?; 6 - 0 = ? và 6 - 6 = ?",
                        "Cộng và trừ với 0" to "6 + 0 = 6 và 6 - 0 = 6 (giữ nguyên không đổi).",
                        "Hai số bằng nhau trừ nhau" to "Có 6 bớt đi hết 6 thì còn 0: 6 - 6 = 0."
                    )
                    e.count { it == '+' || it == '-' } >= 2 -> lesson(
                        "Dãy tính cộng, trừ có 2 bước tính",
                        "Khi biểu thức có hai phép tính cộng, trừ liên tiếp, ta thực hiện lần lượt từ trái sang phải: lấy kết quả phép tính đầu cộng (hoặc trừ) với số thứ ba.",
                        "Tính: 4 + 3 - 2 = ?",
                        "Bước 1 (bên trái trước)" to "4 + 3 = 7.",
                        "Bước 2 (tính tiếp với số cuối)" to "7 - 2 = 5. Kết quả là 5."
                    )
                    e.contains("-") -> lesson(
                        "Ý nghĩa phép trừ trong phạm vi 10",
                        "Phép trừ dùng khi bớt đi hoặc tách ra. Có thể đếm lùi trên tia số hoặc dựa vào phép tách số.",
                        "Tính: 9 - 4 = ?",
                        "Tách số hoặc đếm lùi" to "9 gồm 4 và 5 (hoặc từ 9 đếm lùi 4 bước: 8, 7, 6, 5).",
                        "Kết quả" to "9 - 4 = 5."
                    )
                    else -> lesson(
                        "Ý nghĩa phép cộng trong phạm vi 10",
                        "Phép cộng dùng khi thêm vào hoặc gộp lại. Có thể đếm thêm trên tia số hoặc dựa vào phép gộp số.",
                        "Tính: 5 + 3 = ?",
                        "Gộp số hoặc đếm thêm" to "Gộp 5 và 3 (hoặc từ 5 đếm thêm 3 bước: 6, 7, 8).",
                        "Kết quả" to "5 + 3 = 8."
                    )
                }
            }

            "G1_05_PLACE_VALUE_100" -> when {
                has(statement, "được viết là", "đọc") -> lesson(
                    "Đọc và viết các số có hai chữ số",
                    "Viết chữ số hàng chục ở bên trái, chữ số hàng đơn vị ở bên phải. Lưu ý quy tắc đọc: 'mốt' (1 đơn vị khi hàng chục từ 2 trở lên), 'tư' (4 đơn vị), 'lăm' (5 đơn vị).",
                    "Số 'bốn mươi lăm' được viết là số nào?",
                    "Xác định hàng chục và đơn vị" to "'Bốn mươi' là 4 chục, 'lăm' là 5 đơn vị.",
                    "Ghép chữ số" to "Viết 4 trước, 5 sau ta được số 45."
                )
                has(statement, "+ ...") -> lesson(
                    "Phân tích số thành tổng của số chục và đơn vị",
                    "Mỗi số có hai chữ số đều viết được thành tổng của số tròn chục và số đơn vị: ab = a0 + b.",
                    "Điền số thích hợp: 68 = 60 + ...",
                    "Tách chục và đơn vị" to "68 gồm 6 chục (60) và 8 đơn vị.",
                    "Điền số còn thiếu" to "68 = 60 + 8. Số cần điền là 8."
                )
                has(statement, "lớn nhất", "bé nhất") -> lesson(
                    "So sánh các số có hai chữ số",
                    "So sánh chữ số hàng chục trước: số nào có hàng chục lớn hơn thì lớn hơn. Nếu hàng chục bằng nhau thì số nào có hàng đơn vị lớn hơn sẽ lớn hơn.",
                    "Trong các số 37, 73, 33, 77; số nào lớn nhất?",
                    "So sánh hàng chục" to "Hai số 73 và 77 có hàng chục là 7 (lớn hơn hàng chục 3).",
                    "So sánh tiếp hàng đơn vị" to "Giữa 73 và 77: 7 đơn vị lớn hơn 3 đơn vị nên 77 là số lớn nhất."
                )
                else -> lesson(
                    "Cấu tạo Chục và Đơn vị của các số đến 100",
                    "Trong số có hai chữ số: chữ số đứng trước (bên trái) chỉ số chục, chữ số đứng sau (bên phải) chỉ số đơn vị. 10 đơn vị = 1 chục; 10 chục = 100.",
                    "Số 45 gồm mấy chục và mấy đơn vị?",
                    "Xác định hàng chục" to "Chữ số bên trái là 4, tức là 4 chục.",
                    "Xác định hàng đơn vị" to "Chữ số bên phải là 5, tức là 5 đơn vị. Vậy 45 gồm 4 chục và 5 đơn vị."
                )
            }

            "G1_06_ADD_SUB_100_NO_CARRY" -> when {
                has(statement, "Tính nhẩm") -> lesson(
                    "Cộng, trừ nhẩm các số tròn chục",
                    "Khi cộng hoặc trừ các số tròn chục, ta chỉ cần cộng hoặc trừ chữ số hàng chục với nhau rồi viết thêm một chữ số 0 vào bên phải kết quả.",
                    "Tính nhẩm: 30 + 40 = ? và 80 - 50 = ?",
                    "Phép cộng tròn chục" to "3 chục + 4 chục = 7 chục. Vậy 30 + 40 = 70.",
                    "Phép trừ tròn chục" to "8 chục - 5 chục = 3 chục. Vậy 80 - 50 = 30."
                )
                exprPart(statement).contains("-") -> lesson(
                    "Đặt tính rồi tính trừ (không nhớ) phạm vi 100",
                    "Đặt tính thẳng cột: hàng đơn vị thẳng hàng đơn vị, hàng chục thẳng hàng chục. Trừ từ phải sang trái: trừ hàng đơn vị trước, trừ hàng chục sau.",
                    "Đặt tính rồi tính: 58 - 23 = ?",
                    "Trừ hàng đơn vị" to "8 - 3 = 5, viết 5 thẳng cột đơn vị.",
                    "Trừ hàng chục" to "5 - 2 = 3, viết 3 thẳng cột chục. Kết quả là 35."
                )
                else -> lesson(
                    "Đặt tính rồi tính cộng (không nhớ) phạm vi 100",
                    "Đặt tính thẳng cột: hàng đơn vị thẳng hàng đơn vị, hàng chục thẳng hàng chục (nếu cộng với số có 1 chữ số thì viết thẳng cột đơn vị). Cộng từ phải sang trái.",
                    "Đặt tính rồi tính: 32 + 15 = ?",
                    "Cộng hàng đơn vị" to "2 + 5 = 7, viết 7 thẳng cột đơn vị.",
                    "Cộng hàng chục" to "3 + 1 = 4, viết 4 thẳng cột chục. Kết quả là 47."
                )
            }

            "G1_07_WORD_PROBLEMS_G1" -> if (has(statement, "cho", "bớt", "còn lại", "màu xanh")) {
                lesson(
                    "Bài toán có lời văn về bớt đi / tìm phần còn lại",
                    "Khi đề bài có các từ 'đã cho đi', 'bớt đi', 'bay đi', 'hỏi còn lại bao nhiêu' hoặc biết tổng và một phần, ta thực hiện phép tính trừ (-).",
                    "Lan có 26 quả cam, Lan biếu bà 12 quả cam. Hỏi Lan còn lại bao nhiêu quả cam?",
                    "Phân tích đề bài" to "Có 26 quả, biếu đi 12 quả nên số cam giảm đi (làm phép trừ).",
                    "Tính và đáp số" to "26 - 12 = 14 (quả cam). Đáp số: 14 quả cam."
                )
            } else {
                lesson(
                    "Bài toán có lời văn về thêm vào / gộp lại",
                    "Khi đề bài có các từ 'cho thêm', 'mua thêm', 'cả hai bạn có tất cả', ta thực hiện phép tính cộng (+).",
                    "An có 12 viên bi, Bình cho An thêm 5 viên bi. Hỏi An có tất cả bao nhiêu viên bi?",
                    "Phân tích đề bài" to "Được cho thêm nên số viên bi tăng lên (làm phép cộng).",
                    "Tính và đáp số" to "12 + 5 = 17 (viên bi). Đáp số: 17 viên bi."
                )
            }

            "G1_08_MEASURE_LENGTH_CM" -> when {
                has(statement, "thước kẻ", "vạch số") -> lesson(
                    "Cách đo độ dài đồ vật bằng thước kẻ (cm)",
                    "Nếu một đầu vật đặt đúng vạch số 0 thì đầu kia chỉ số mấy, vật dài bấy nhiêu cm. Nếu đặt lệch từ vạch số a đến vạch số b thì độ dài = b - a (cm).",
                    "Một băng giấy đặt dọc theo thước từ vạch số 2 đến vạch số 9. Băng giấy dài mấy cm?",
                    "Xác định hai đầu băng giấy" to "Đầu thứ nhất ở vạch 2, đầu thứ hai ở vạch 9.",
                    "Tính độ dài" to "Lấy 9 - 2 = 7 (cm). Vậy băng giấy dài 7 cm."
                )
                has(statement, "cắt đi", "-") -> lesson(
                    "Phép trừ số đo độ dài (cm)",
                    "Khi trừ các số đo cùng đơn vị xăng-ti-mét (cm), ta lấy các số trừ cho nhau rồi viết đơn vị cm vào sau kết quả.",
                    "Sợi dây dài 18 cm, cắt đi 6 cm. Hỏi sợi dây còn lại dài bao nhiêu cm?",
                    "Thực hiện phép trừ số" to "18 - 6 = 12.",
                    "Ghi đơn vị đo" to "Sợi dây còn lại dài 12 cm."
                )
                else -> lesson(
                    "Phép cộng số đo độ dài (cm)",
                    "Khi cộng các số đo cùng đơn vị xăng-ti-mét (cm), ta cộng các số với nhau rồi viết đơn vị cm vào sau kết quả.",
                    "Tính: 8 cm + 5 cm = ? cm",
                    "Thực hiện phép cộng số" to "8 + 5 = 13.",
                    "Ghi đơn vị đo" to "Kết quả là 13 cm."
                )
            }

            "G1_09_TIME_CLOCK_CALENDAR" -> when {
                has(statement, "tuần lễ") -> lesson(
                    "Số ngày trong một tuần lễ",
                    "Một tuần lễ có 7 ngày, lần lượt là: Thứ Hai, Thứ Ba, Thứ Tư, Thứ Năm, Thứ Sáu, Thứ Bảy và Chủ nhật.",
                    "Một tuần lễ có mấy ngày đi học và mấy ngày nghỉ cuối tuần?",
                    "Đếm tổng số ngày" to "Từ Thứ Hai đến Chủ nhật có tất cả 7 ngày.",
                    "Ghi nhớ" to "1 tuần lễ = 7 ngày."
                )
                has(statement, "Tờ lịch", "ngày mấy") -> lesson(
                    "Đọc tờ lịch hằng ngày",
                    "Hôm nay là ngày n thì ngày mai là ngày n + 1 (tăng thêm 1), còn hôm qua là ngày n - 1 (lùi lại 1).",
                    "Tờ lịch hôm nay ghi Thứ Năm, ngày 15. Hỏi ngày mai là ngày mấy, hôm qua là ngày mấy?",
                    "Tìm ngày mai" to "15 + 1 = 16 (Ngày 16).",
                    "Tìm hôm qua" to "15 - 1 = 14 (Ngày 14)."
                )
                has(statement, "Hôm nay", "ngày mai", "hôm qua", "thứ mấy") -> lesson(
                    "Thứ tự các thứ trong tuần",
                    "Thứ tự 7 ngày: Thứ Hai → Thứ Ba → Thứ Tư → Thứ Năm → Thứ Sáu → Thứ Bảy → Chủ nhật. Hôm qua là ngày đứng liền trước, ngày mai là ngày đứng liền sau.",
                    "Hôm nay là Thứ Tư. Hỏi hôm qua là thứ mấy và ngày mai là thứ mấy?",
                    "Tìm hôm qua (liền trước)" to "Trước Thứ Tư là Thứ Ba.",
                    "Tìm ngày mai (liền sau)" to "Sau Thứ Tư là Thứ Năm."
                )
                else -> lesson(
                    "Xem đồng hồ giờ đúng",
                    "Trên mặt đồng hồ: kim ngắn chỉ giờ, kim dài chỉ phút. Khi kim dài chỉ đúng số 12 thì kim ngắn chỉ vào số mấy, đồng hồ chỉ bấy nhiêu giờ.",
                    "Đồng hồ có kim ngắn chỉ số 8, kim dài chỉ số 12. Đồng hồ chỉ mấy giờ?",
                    "Quan sát kim dài" to "Kim dài chỉ đúng số 12 (giờ đúng).",
                    "Quan sát kim ngắn" to "Kim ngắn chỉ số 8. Vậy đồng hồ chỉ 8 giờ."
                )
            }

            "G1_10_PATTERNS_COUNTING" -> if (has(statement, "dãy hình", "Hình tiếp theo")) {
                lesson(
                    "Tìm quy luật của dãy hình lặp lại",
                    "Quan sát các hình đầu tiên để tìm ra nhóm hình được lặp đi lặp lại (ví dụ: A - B - A - B hoặc A - A - B - A - A - B).",
                    "Tìm hình tiếp theo: Hình vuông, Hình tròn, Hình vuông, Hình tròn, Hình vuông, ...",
                    "Tìm cụm lặp lại" to "Cụm 2 hình (Hình vuông - Hình tròn) được lặp đi lặp lại.",
                    "Xác định hình tiếp theo" to "Sau Hình vuông phải đến Hình tròn."
                )
            } else {
                lesson(
                    "Tìm quy luật dãy số đếm thêm / đếm bớt",
                    "Lấy hai số đứng liền nhau trừ đi nhau để biết dãy số đang đếm thêm (tăng dần) hay đếm bớt (giảm dần) bao nhiêu đơn vị (1, 2, 5 hoặc 10).",
                    "Tìm số tiếp theo trong dãy số: 5; 10; 15; ...",
                    "Tìm quy luật" to "10 - 5 = 5; 15 - 10 = 5. Đây là dãy đếm thêm 5.",
                    "Tính số tiếp theo" to "15 + 5 = 20. Số cần tìm là 20."
                )
            }

            // =====================================================================
            // LỚP 2 (10 NODES - ĐA NHÁNH)
            // =====================================================================
            "G2_01_TERMS_AND_NUMBER_LINE" -> when {
                has(statement, "liền trước", "liền sau") -> lesson(
                    "Số liền trước, số liền sau trên tia số",
                    "Muốn tìm số liền trước của một số, ta lấy số đó trừ đi 1. Muốn tìm số liền sau, ta lấy số đó cộng thêm 1.",
                    "Tìm số liền trước của 60.",
                    "Lấy số đã cho bớt 1" to "60 - 1 = 59.",
                    "Kết luận" to "Số liền trước của 60 là 59."
                )
                statement.contains("-") -> lesson(
                    "Tên gọi thành phần trong phép trừ",
                    "Trong phép trừ a - b = c: số đứng trước dấu trừ (a) là Số bị trừ, số đứng sau dấu trừ (b) là Số trừ, kết quả (c) gọi là Hiệu.",
                    "Trong phép tính 58 - 23 = 35, từng số được gọi là gì?",
                    "Xác định từng vị trí" to "58 đứng trước dấu trừ; 23 đứng sau dấu trừ; 35 là kết quả.",
                    "Gọi tên" to "58 là Số bị trừ, 23 là Số trừ, 35 là Hiệu."
                )
                else -> lesson(
                    "Tên gọi thành phần trong phép cộng",
                    "Trong phép cộng a + b = c: các số đem cộng (a và b) đều gọi là Số hạng, kết quả (c) gọi là Tổng.",
                    "Trong phép tính 35 + 24 = 59, số 35 và số 59 gọi là gì?",
                    "Nhận biết phép cộng" to "Hai số cộng với nhau cho ra kết quả.",
                    "Gọi tên" to "35 (và 24) là Số hạng; 59 là Tổng."
                )
            }

            "G2_02_MAKE_10_ADD_SUB" -> if (exprPart(statement).contains("-")) {
                lesson(
                    "Trừ qua 10 trong phạm vi 20 (Trừ để được 10 rồi trừ số còn lại)",
                    "Muốn trừ một số qua 10 (như 14 - 6): ta tách số trừ để trừ cho số bị trừ về tròn 10 trước, sau đó lấy 10 trừ đi phần còn lại.",
                    "Tính nhẩm qua 10: 15 - 7 = ?",
                    "Tách số trừ để trừ về 10" to "15 có 5 đơn vị nên tách 7 = 5 + 2. Lấy 15 - 5 = 10.",
                    "Trừ tiếp phần còn lại" to "10 - 2 = 8. Vậy 15 - 7 = 8."
                )
            } else {
                lesson(
                    "Cộng qua 10 trong phạm vi 20 (Gộp cho đủ 10 rồi cộng với số còn lại)",
                    "Muốn cộng hai số qua 10 (như 8 + 5): ta tách số thứ hai để gộp với số thứ nhất cho đủ 10 trước, rồi lấy 10 cộng với số còn lại.",
                    "Tính nhẩm qua 10: 9 + 6 = ?",
                    "Tách số để gộp đủ 10" to "9 cần thêm 1 để đủ 10 nên tách 6 = 1 + 5. Lấy 9 + 1 = 10.",
                    "Cộng phần còn lại" to "10 + 5 = 15. Vậy 9 + 6 = 15."
                )
            }

            "G2_03_ADD_SUB_100_WITH_CARRY" -> if (has(statement, "hiệu") || exprPart(statement).contains("-")) {
                lesson(
                    "Phép trừ có nhớ trong phạm vi 100",
                    "Đặt tính thẳng cột. Nếu chữ số hàng đơn vị không đủ trừ, ta mượn 1 chục (10 đơn vị) để trừ, rồi nhớ 1 vào chữ số hàng chục của số trừ (hoặc bớt 1 chục ở số bị trừ).",
                    "Đặt tính rồi tính hiệu: 52 - 27 = ?",
                    "Trừ hàng đơn vị" to "2 không trừ được 7, lấy 12 - 7 = 5, viết 5 nhớ 1.",
                    "Trừ hàng chục" to "2 thêm 1 bằng 3; 5 - 3 = 2, viết 2. Kết quả là 25."
                )
            } else {
                lesson(
                    "Phép cộng có nhớ trong phạm vi 100",
                    "Đặt tính thẳng cột. Cộng hàng đơn vị trước: nếu tổng hàng đơn vị từ 10 trở lên thì viết chữ số đơn vị và nhớ 1 chục sang hàng chục.",
                    "Đặt tính rồi tính tổng: 38 + 27 = ?",
                    "Cộng hàng đơn vị" to "8 + 7 = 15, viết 5 nhớ 1.",
                    "Cộng hàng chục" to "3 + 2 = 5, thêm 1 (nhớ) bằng 6, viết 6. Kết quả là 65."
                )
            }

            "G2_04_WORD_MORE_LESS" -> lesson(
                "Bài toán về Nhiều hơn, Ít hơn một số đơn vị",
                "Đọc kĩ xem đại lượng cần tìm là số lớn hay số bé: Muốn tìm số lớn (nhiều hơn) ta làm phép cộng. Muốn tìm số bé (ít hơn) ta làm phép trừ. Chú ý: Nếu A nhiều hơn B thì nghĩa là B ít hơn A.",
                "Lớp 2B có 35 học sinh, nhiều hơn lớp 2A 5 học sinh. Hỏi lớp 2A có bao nhiêu học sinh?",
                "Xác định lớp nào ít hơn" to "Lớp 2B nhiều hơn 2A nghĩa là lớp 2A ít hơn lớp 2B 5 học sinh.",
                "Thực hiện phép tính" to "35 - 5 = 30 (học sinh). Đáp số: 30 học sinh."
            )

            "G2_05_MUL_DIV_2_5" -> when {
                has(statement, "gọi là gì") -> lesson(
                    "Tên gọi thành phần của Phép nhân và Phép chia",
                    "Trong phép nhân a × b = c: a và b là các Thừa số, c là Tích. Trong phép chia a : b = c: a là Số bị chia, b là Số chia, c là Thương.",
                    "Trong phép nhân 2 × 8 = 16, số 16 được gọi là gì?",
                    "Nhận biết vị trí" to "16 là kết quả của phép nhân 2 × 8.",
                    "Kết luận" to "Số 16 được gọi là Tích (còn 2 và 8 là các Thừa số)."
                )
                has(statement, ":") -> lesson(
                    "Bảng chia 2 và Bảng chia 5",
                    "Phép chia là phép tính ngược của phép nhân. Muốn tính a : 2 (hoặc a : 5), ta nhẩm xem 2 (hoặc 5) nhân với mấy thì bằng a.",
                    "Tính: 35 : 5 = ?",
                    "Nhẩm bảng nhân 5" to "Ta có 5 × 7 = 35.",
                    "Suy ra thương" to "Vậy 35 : 5 = 7."
                )
                else -> lesson(
                    "Bảng nhân 2 và Bảng nhân 5",
                    "2 × k nghĩa là 2 được lấy k lần. Bảng nhân 2 có kết quả đếm thêm 2 (2, 4, 6, ..., 20). Bảng nhân 5 có kết quả đếm thêm 5 (5, 10, 15, ..., 50).",
                    "Mỗi bàn có 2 học sinh. Hỏi 7 bàn như vậy có bao nhiêu học sinh?",
                    "Viết phép nhân" to "2 học sinh được lấy 7 lần: 2 × 7.",
                    "Tính kết quả" to "2 × 7 = 14 (học sinh)."
                )
            }

            "G2_06_NUM_1000_ADD_SUB" -> when {
                has(statement, "liền sau", "liền trước", "bé nhất", "lớn nhất") -> lesson(
                    "So sánh và thứ tự các số trong phạm vi 1000",
                    "So sánh các số có 3 chữ số: so sánh hàng trăm trước, rồi đến hàng chục, cuối cùng là hàng đơn vị. Số liền trước kém 1 đơn vị, số liền sau hơn 1 đơn vị.",
                    "Tìm số bé nhất trong các số: 452, 389, 425, 501.",
                    "So sánh chữ số hàng trăm" to "Các chữ số hàng trăm là 4, 3, 4, 5. Chữ số 3 bé nhất.",
                    "Kết luận" to "Số bé nhất là 389."
                )
                else -> lesson(
                    "Cộng, trừ các số có ba chữ số trong phạm vi 1000",
                    "Đặt tính thẳng cột (đơn vị thẳng đơn vị, chục thẳng chục, trăm thẳng trăm). Tính lần lượt từ phải sang trái, có nhớ (không quá 1 lần) sang hàng tiếp theo.",
                    "Đặt tính rồi tính: 345 + 238 = ? và 654 - 231 = ?",
                    "Phép cộng 345 + 238" to "Đơn vị: 5 + 8 = 13 viết 3 nhớ 1. Chục: 4 + 3 + 1 = 8. Trăm: 3 + 2 = 5. Kết quả: 583.",
                    "Phép trừ 654 - 231" to "Đơn vị: 4 - 1 = 3. Chục: 5 - 3 = 2. Trăm: 6 - 2 = 4. Kết quả: 423."
                )
            }

            "G2_07_GEOMETRY_G2" -> if (has(statement, "gấp khúc")) {
                lesson(
                    "Tính độ dài đường gấp khúc",
                    "Muốn tính độ dài đường gấp khúc, ta tính tổng độ dài tất cả các đoạn thẳng tạo thành đường gấp khúc đó (cùng đơn vị đo).",
                    "Một đường gấp khúc gồm 3 đoạn thẳng có độ dài lần lượt 4 cm, 5 cm, 6 cm. Tính độ dài đường gấp khúc.",
                    "Cộng các đoạn thẳng" to "4 + 5 + 6 = 15 (cm).",
                    "Đáp số" to "15 cm."
                )
            } else {
                lesson(
                    "Chu vi hình tam giác, chu vi hình tứ giác",
                    "Chu vi của hình tam giác (hoặc hình tứ giác) bằng tổng độ dài 3 cạnh của hình tam giác (hoặc 4 cạnh của hình tứ giác) đó.",
                    "Tính chu vi hình tứ giác có các cạnh: 3 cm, 4 cm, 5 cm, 6 cm.",
                    "Cộng độ dài 4 cạnh" to "3 + 4 + 5 + 6 = 18 (cm).",
                    "Đáp số" to "Chu vi hình tứ giác là 18 cm."
                )
            }

            "G2_08_MEASURE_DM_M_KM_KG_L" -> if (has(statement, "Đổi")) {
                lesson(
                    "Quy đổi đơn vị đo độ dài: km, m, dm, cm",
                    "Ghi nhớ: 1 m = 10 dm; 1 dm = 10 cm; 1 m = 100 cm; 1 km = 1000 m. Đổi từ đơn vị lớn sang đơn vị bé thì nhân thêm 10, 100, 1000.",
                    "Đổi: 5 m = ... dm và 50 dm = ... m",
                    "Đổi 5 m ra dm" to "Vì 1 m = 10 dm nên 5 m = 50 dm.",
                    "Đổi 50 dm ra m" to "Vì 10 dm = 1 m nên 50 dm = 5 m."
                )
            } else {
                lesson(
                    "Bài toán với đơn vị khối lượng (kg) và dung tích (lít)",
                    "Khi giải toán có đơn vị ki-lô-gam (kg) hoặc lít (l), ta thực hiện phép tính với các số rồi ghi đơn vị kg hoặc l vào kết quả.",
                    "Can thứ nhất đựng 15 l nước, can thứ hai đựng 12 l nước. Cả hai can đựng bao nhiêu lít nước?",
                    "Thực hiện phép cộng" to "15 + 12 = 27 (l).",
                    "Đáp số" to "27 l nước."
                )
            }

            "G2_09_TIME_MONEY_G2" -> if (has(statement, "đồng", "tiền", "tờ")) {
                lesson(
                    "Tiền Việt Nam và tính tiền mua hàng",
                    "Tổng số tiền = cộng mệnh giá các tờ tiền. Tiền trả lại khi mua hàng = Số tiền đưa cho người bán trừ đi Giá tiền món hàng.",
                    "Nam mua bút giá 4000 đồng, đưa tờ 10 000 đồng. Hỏi cô bán hàng trả lại bao nhiêu tiền?",
                    "Lấy tiền đưa trừ giá mua" to "10 000 - 4000 = 6000 (đồng).",
                    "Đáp số" to "6000 đồng."
                )
            } else {
                lesson(
                    "Xem đồng hồ đến 15 phút, 30 phút",
                    "Mỗi khoảng giữa hai số trên đồng hồ là 5 phút: kim dài chỉ số 3 là 15 phút, chỉ số 6 là 30 phút (hay còn gọi là 'giờ rưỡi'), chỉ số 12 là giờ đúng.",
                    "Đồng hồ chỉ 4 giờ 30 phút. Khi đó kim dài chỉ vào số mấy và còn gọi là mấy giờ?",
                    "Vị trí kim dài" to "30 phút ứng với kim dài chỉ đúng số 6.",
                    "Cách gọi khác" to "4 giờ 30 phút còn gọi là 4 giờ rưỡi."
                )
            }

            "G2_10_STATS_PROBABILITY_G2" -> if (has(statement, "Bảng kiểm đếm", "nhiều nhất", "ít nhất")) {
                lesson(
                    "Đọc và so sánh số liệu trên Bảng kiểm đếm",
                    "Đọc số lượng gắn với từng loại trên bảng, sau đó so sánh các số để tìm ra loại có số lượng lớn nhất (nhiều nhất) hoặc bé nhất (ít nhất).",
                    "Bảng kiểm đếm: Táo: 8 quả, Cam: 12 quả, Chuối: 5 quả, Xoài: 10 quả. Loại nào nhiều nhất?",
                    "So sánh các số liệu" to "Trong các số 8, 12, 5, 10 thì 12 là lớn nhất.",
                    "Đọc tên loại quả" to "Số 12 ứng với quả Cam. Vậy Cam nhiều nhất."
                )
            } else {
                lesson(
                    "Khả năng xảy ra của sự kiện: Chắc chắn, Có thể, Không thể",
                    "- 'Chắc chắn': luôn luôn xảy ra (hộp chỉ toàn bóng đỏ thì lấy 1 quả chắc chắn là đỏ).\n- 'Không thể': không bao giờ xảy ra (hộp không có bóng xanh thì không thể lấy được bóng xanh).\n- 'Có thể': có lúc xảy ra, có lúc không (hộp có cả bóng đỏ và bóng xanh).",
                    "Trong hộp có 4 bóng đỏ và 3 bóng xanh. Lấy ngẫu nhiên 1 quả, khả năng lấy được bóng xanh là gì?",
                    "Phân tích các màu trong hộp" to "Trong hộp có cả bóng đỏ lẫn bóng xanh.",
                    "Kết luận" to "Ta có thể lấy trúng bóng xanh mà cũng có thể lấy trúng bóng đỏ, nên khả năng là 'Có thể'."
                )
            }

            // =====================================================================
            // LỚP 3 (10 NODES - ĐA NHÁNH)
            // =====================================================================
            "G3_01_MUL_DIV_TABLES_3_9" -> lesson(
                "Bảng nhân, bảng chia từ 3 đến 9",
                "Phép nhân và phép chia là hai phép tính ngược nhau: nếu a × b = c thì c : a = b và c : b = a. Số nào nhân với 1 cũng bằng chính nó; số nào nhân với 0 cũng bằng 0.",
                "Tính: 7 × 8 = ? và 56 : 7 = ?",
                "Nhẩm bảng nhân 7" to "7 × 8 = 56.",
                "Suy ra phép chia tương ứng" to "Vì 7 × 8 = 56 nên 56 : 7 = 8 (và 56 : 8 = 7)."
            )

            "G3_02_FIND_UNKNOWN_COMPONENT" -> when {
                has(statement, "- x =") -> lesson(
                    "Tìm số trừ chưa biết (a - x = b)",
                    "Quy tắc: Muốn tìm SỐ TRỪ chưa biết, ta lấy SỐ BỊ TRỪ trừ đi HIỆU (x = a - b). Tuyệt đối không lấy số bị trừ cộng với hiệu!",
                    "Tìm x, biết: 450 - x = 120",
                    "Xác định thành phần" to "450 là số bị trừ, x là số trừ, 120 là hiệu.",
                    "Áp dụng quy tắc" to "x = 450 - 120 = 330 (Thử lại: 450 - 330 = 120)."
                )
                has(statement, "x -") -> lesson(
                    "Tìm số bị trừ chưa biết (x - a = b)",
                    "Quy tắc: Muốn tìm SỐ BỊ TRỪ chưa biết, ta lấy HIỆU cộng với SỐ TRỪ (x = b + a).",
                    "Tìm x, biết: x - 135 = 240",
                    "Xác định thành phần" to "x là số bị trừ, 135 là số trừ, 240 là hiệu.",
                    "Áp dụng quy tắc" to "x = 240 + 135 = 375 (Thử lại: 375 - 135 = 240)."
                )
                has(statement, "x +") || has(statement, "+ x") -> lesson(
                    "Tìm số hạng chưa biết (x + a = b)",
                    "Quy tắc: Muốn tìm SỐ HẠNG chưa biết, ta lấy TỔNG trừ đi SỐ HẠNG đã biết (x = b - a).",
                    "Tìm x, biết: x + 215 = 580",
                    "Xác định thành phần" to "x là số hạng chưa biết, 215 là số hạng đã biết, 580 là tổng.",
                    "Áp dụng quy tắc" to "x = 580 - 215 = 365."
                )
                has(statement, "x :") -> lesson(
                    "Tìm số bị chia chưa biết (x : b = c)",
                    "Quy tắc: Muốn tìm SỐ BỊ CHIA chưa biết, ta lấy THƯƠNG nhân với SỐ CHIA (x = c × b).",
                    "Tìm x, biết: x : 6 = 15",
                    "Xác định thành phần" to "x là số bị chia, 6 là số chia, 15 là thương.",
                    "Áp dụng quy tắc" to "x = 15 × 6 = 90 (Thử lại: 90 : 6 = 15)."
                )
                else -> lesson(
                    "Tìm thừa số chưa biết (x × b = c)",
                    "Quy tắc: Muốn tìm THỪA SỐ chưa biết, ta lấy TÍCH chia cho THỪA SỐ đã biết (x = c : b).",
                    "Tìm x, biết: x × 6 = 42",
                    "Xác định thành phần" to "x là thừa số chưa biết, 6 là thừa số đã biết, 42 là tích.",
                    "Áp dụng quy tắc" to "x = 42 : 6 = 7."
                )
            }

            "G3_03_TIMES_MORE_LESS_FRAC" -> when {
                has(statement, "1/") -> lesson(
                    "Tìm một phần mấy (1/k) của một số",
                    "Muốn tìm một phần mấy (1/k) của một số, ta lấy số đó chia cho k.",
                    "Tìm 1/4 của 28.",
                    "Xác định phép tính" to "Tìm 1/4 của 28 tức là chia 28 thành 4 phần bằng nhau.",
                    "Tính kết quả" to "28 : 4 = 7. Vậy 1/4 của 28 là 7."
                )
                has(statement, "gấp số bé mấy lần") -> lesson(
                    "So sánh số lớn gấp mấy lần số bé",
                    "Muốn biết số lớn gấp mấy lần số bé, ta lấy số lớn chia cho số bé (không phải lấy số lớn trừ số bé).",
                    "Số bé là 6, số lớn là 42. Hỏi số lớn gấp mấy lần số bé?",
                    "Lấy số lớn chia số bé" to "42 : 6 = 7 (lần).",
                    "Kết luận" to "Số lớn gấp 7 lần số bé."
                )
                has(statement, "Giảm") -> lesson(
                    "Giảm một số đi một số lần",
                    "Muốn giảm một số đi k lần, ta lấy số đó chia cho k (phân biệt với 'bớt đi k đơn vị' là trừ đi k).",
                    "Giảm số 36 đi 4 lần ta được số nào?",
                    "Lấy số đã cho chia cho số lần" to "36 : 4 = 9.",
                    "Kết luận" to "Giảm 36 đi 4 lần được 9."
                )
                else -> lesson(
                    "Gấp một số lên một số lần",
                    "Muốn gấp một số lên k lần, ta lấy số đó nhân với k (phân biệt với 'thêm k đơn vị' là cộng thêm k).",
                    "Gấp số 8 lên 5 lần ta được số nào?",
                    "Lấy số đã cho nhân với số lần" to "8 × 5 = 40.",
                    "Kết luận" to "Gấp 8 lên 5 lần được 40."
                )
            }

            "G3_04_DIVISION_WITH_REMAINDER" -> if (has(statement, "Số dư lớn nhất")) {
                lesson(
                    "Số dư lớn nhất trong phép chia có dư",
                    "Trong mọi phép chia có dư, số dư luôn phải bé hơn số chia (Số dư < Số chia). Vì vậy: Số dư lớn nhất có thể luôn bằng (Số chia - 1).",
                    "Trong phép chia có số chia là 7, số dư lớn nhất có thể là bao nhiêu?",
                    "Áp dụng tính chất số dư" to "Các số dư có thể là: 1, 2, 3, 4, 5, 6 (bé hơn 7).",
                    "Chọn số lớn nhất" to "Số dư lớn nhất là 7 - 1 = 6."
                )
            } else {
                lesson(
                    "Thực hiện phép chia có dư",
                    "Ta tìm thương lớn nhất sao cho (Số chia × Thương) không vượt quá Số bị chia. Khi đó: Số dư = Số bị chia - (Số chia × Thương). Kiểm tra: Số dư phải bé hơn Số chia.",
                    "Thực hiện phép chia: 29 : 4 = ? (dư ?)",
                    "Ước lượng thương" to "4 × 7 = 28 (gần 29 nhất và không vượt quá 29), nên thương là 7.",
                    "Tính số dư" to "29 - 28 = 1 (1 bé hơn 4). Vậy 29 : 4 = 7 (dư 1)."
                )
            }

            "G3_05_ORDER_OF_OPERATIONS" -> if (statement.contains("(")) {
                lesson(
                    "Tính giá trị biểu thức có dấu ngoặc đơn ()",
                    "Khi biểu thức có dấu ngoặc đơn (), ta bắt buộc phải thực hiện phép tính ở TRONG NGOẶC TRƯỚC, ngoài ngoặc sau.",
                    "Tính giá trị biểu thức: (15 + 5) × 4 = ?",
                    "Bước 1 (Tính trong ngoặc trước)" to "15 + 5 = 20.",
                    "Bước 2 (Tính ngoài ngoặc)" to "20 × 4 = 80. Kết quả là 80."
                )
            } else {
                lesson(
                    "Thứ tự thực hiện phép tính không có ngoặc",
                    "Trong biểu thức có cả cộng, trừ, nhân, chia mà không có ngoặc: ta thực hiện NHÂN, CHIA TRƯỚC; CỘNG, TRỪ SAU.",
                    "Tính giá trị biểu thức: 50 - 6 × 4 = ?",
                    "Bước 1 (Nhân trước)" to "6 × 4 = 24.",
                    "Bước 2 (Trừ sau)" to "50 - 24 = 26. Kết quả là 26."
                )
            }

            "G3_06_NUM_100K_ROUNDING_ROMAN" -> when {
                has(statement, "La Mã") -> lesson(
                    "Đọc và viết Chữ số La Mã (I đến XX)",
                    "Kí hiệu gốc: I = 1, V = 5, X = 10. Quy tắc: Chữ số nhỏ viết bên phải chữ số lớn thì CỘNG THÊM (VI = 6, XI = 11); chữ số I viết ngay bên trái V hoặc X thì TRỪ ĐI 1 (IV = 4, IX = 9).",
                    "Chữ số La Mã XIX biểu diễn số tự nhiên nào?",
                    "Tách theo cụm" to "XIX gồm X (10) và IX (10 - 1 = 9).",
                    "Cộng giá trị" to "10 + 9 = 19. Vậy XIX là số 19."
                )
                has(statement, "liền sau", "liền trước") -> lesson(
                    "Số liền trước, số liền sau trong phạm vi 100 000",
                    "Số liền sau của số n là n + 1. Số liền trước của số n là n - 1.",
                    "Số liền sau của 45 999 là số nào?",
                    "Cộng thêm 1 đơn vị" to "45 999 + 1 = 46 000.",
                    "Kết luận" to "Số liền sau của 45 999 là 46 000."
                )
                else -> lesson(
                    "Quy tắc làm tròn số tự nhiên",
                    "Muốn làm tròn số đến hàng nào, ta nhìn vào chữ số ở hàng NGAY BÊN PHẢI nó:\n- Nếu chữ số đó là 0, 1, 2, 3, 4 thì giữ nguyên chữ số hàng làm tròn (làm tròn xuống).\n- Nếu chữ số đó là 5, 6, 7, 8, 9 thì cộng thêm 1 vào chữ số hàng làm tròn (làm tròn lên).\nSau đó thay tất cả các chữ số bên phải bằng chữ số 0.",
                    "Làm tròn số 34 620 đến hàng nghìn.",
                    "Xác định chữ số hàng nghìn và hàng trăm" to "Chữ số hàng nghìn là 4; chữ số ngay bên phải (hàng trăm) là 6.",
                    "Làm tròn lên" to "Vì 6 từ 5 trở lên nên tăng 4 thành 5, các chữ số sau đổi thành 0: được 35 000."
                )
            }

            "G3_07_TWO_STEP_WORD_PROBLEMS" -> when {
                has(statement, "gấp") -> lesson(
                    "Bài toán hai bước tính: Tìm giá trị phần thứ hai rồi tính tổng",
                    "Bước 1: Tìm số lượng của bạn thứ hai (gấp lên k lần thì lấy số của bạn thứ nhất nhân với k).\nBước 2: Tìm tổng số lượng của cả hai bạn (lấy số của bạn thứ nhất cộng với số vừa tìm được ở Bước 1).",
                    "Mai có 8 quả cam. Số cam của Lan gấp 3 lần số cam của Mai. Hỏi cả hai bạn có bao nhiêu quả cam?",
                    "Bước 1: Tìm số cam của Lan" to "8 × 3 = 24 (quả cam).",
                    "Bước 2: Tìm tổng số cam của hai bạn" to "8 + 24 = 32 (quả cam). Đáp số: 32 quả cam."
                )
                has(statement, "trả lại", "đồng") -> lesson(
                    "Bài toán mua hàng và tính tiền thừa (2 bước)",
                    "Bước 1: Tính tổng số tiền mua hàng = Giá 1 món × Số lượng món.\nBước 2: Tính số tiền người bán trả lại = Số tiền đưa - Tổng số tiền mua hàng.",
                    "Mẹ mua 4 quyển vở, mỗi quyển giá 3000 đồng. Mẹ đưa tờ 20 000 đồng. Hỏi cô bán hàng trả lại bao nhiêu tiền?",
                    "Bước 1: Tính tiền mua 4 quyển vở" to "3000 × 4 = 12 000 (đồng).",
                    "Bước 2: Tính tiền trả lại" to "20 000 - 12 000 = 8000 (đồng). Đáp số: 8000 đồng."
                )
                else -> lesson(
                    "Bài toán giải bằng hai bước tính",
                    "Bước 1: Tính tổng số quả ban đầu (lấy số quả mỗi thùng nhân với số thùng).\nBước 2: Tính số quả còn lại sau khi bán (lấy tổng ban đầu trừ đi số quả đã bán).",
                    "Có 5 thùng cam, mỗi thùng 8 quả. Cửa hàng đã bán đi 12 quả. Hỏi còn lại bao nhiêu quả cam?",
                    "Bước 1: Tìm tổng số cam ban đầu" to "8 × 5 = 40 (quả).",
                    "Bước 2: Tìm số cam còn lại" to "40 - 12 = 28 (quả). Đáp số: 28 quả."
                )
            }

            "G3_08_GEOMETRY_MIDPOINT_CIRCLE" -> if (has(statement, "trung điểm")) {
                lesson(
                    "Trung điểm của đoạn thẳng",
                    "Trung điểm M của đoạn thẳng AB là điểm nằm giữa A, B và chia đoạn thẳng AB thành hai phần bằng nhau: AM = MB = AB : 2.",
                    "M là trung điểm của đoạn thẳng AB dài 14 cm. Tính độ dài đoạn thẳng AM.",
                    "Áp dụng tính chất trung điểm" to "AM bằng một nửa độ dài AB.",
                    "Tính kết quả" to "14 : 2 = 7 (cm). Vậy AM = 7 cm."
                )
            } else {
                lesson(
                    "Hình tròn: Tâm, Bán kính (r) và Đường kính (d)",
                    "Trong một hình tròn: Độ dài đường kính (d) luôn gấp 2 lần độ dài bán kính (r).\n- Tìm đường kính: d = r × 2.\n- Tìm bán kính: r = d : 2.",
                    "Hình tròn tâm O có bán kính r = 6 cm. Hỏi đường kính d dài bao nhiêu cm?",
                    "Áp dụng công thức" to "d = r × 2.",
                    "Tính kết quả" to "6 × 2 = 12 (cm). Đường kính dài 12 cm."
                )
            }

            "G3_09_PERIMETER_AREA_RECT_SQUARE" -> when {
                has(statement, "chu vi hình vuông") -> lesson(
                    "Tính chu vi hình vuông",
                    "Muốn tính chu vi hình vuông, ta lấy độ dài một cạnh nhân với 4: P = a × 4 (đơn vị độ dài: cm, dm, m).",
                    "Tính chu vi hình vuông có cạnh dài 7 cm.",
                    "Áp dụng công thức" to "P = cạnh × 4.",
                    "Tính kết quả" to "7 × 4 = 28 (cm). Chu vi là 28 cm."
                )
                has(statement, "diện tích hình vuông") -> lesson(
                    "Tính diện tích hình vuông",
                    "Muốn tính diện tích hình vuông, ta lấy độ dài một cạnh nhân với chính nó: S = a × a (đơn vị diện tích: cm²).",
                    "Tính diện tích hình vuông có cạnh dài 7 cm.",
                    "Áp dụng công thức" to "S = cạnh × cạnh (không nhầm với nhân 4).",
                    "Tính kết quả" to "7 × 7 = 49 (cm²). Diện tích là 49 cm²."
                )
                has(statement, "chu vi hình chữ nhật") -> lesson(
                    "Tính chu vi hình chữ nhật",
                    "Muốn tính chu vi hình chữ nhật, ta lấy chiều dài cộng với chiều rộng (cùng đơn vị đo) rồi nhân với 2: P = (a + b) × 2.",
                    "Tính chu vi hình chữ nhật có chiều dài 8 cm và chiều rộng 5 cm.",
                    "Bước 1: Cộng chiều dài và chiều rộng" to "8 + 5 = 13 (cm).",
                    "Bước 2: Nhân tổng với 2" to "13 × 2 = 26 (cm). Chu vi là 26 cm."
                )
                else -> lesson(
                    "Tính diện tích hình chữ nhật",
                    "Muốn tính diện tích hình chữ nhật, ta lấy chiều dài nhân với chiều rộng (cùng đơn vị đo): S = a × b (đơn vị diện tích: cm²).",
                    "Tính diện tích hình chữ nhật có chiều dài 8 cm và chiều rộng 5 cm.",
                    "Áp dụng công thức" to "S = chiều dài × chiều rộng.",
                    "Tính kết quả" to "8 × 5 = 40 (cm²). Diện tích là 40 cm²."
                )
            }

            "G3_10_MEASURE_STATS_G3" -> when {
                has(statement, "Lớp nào", "học sinh giỏi") -> lesson(
                    "Đọc và phân tích Bảng số liệu thống kê",
                    "Đọc số liệu gắn với từng lớp trong bảng, sau đó so sánh các số để tìm lớp có số lượng lớn nhất (nhiều nhất) hoặc bé nhất (ít nhất).",
                    "Số học sinh giỏi: 3A: 24 học sinh, 3B: 29 học sinh, 3C: 25 học sinh, 3D: 21 học sinh. Lớp nào có nhiều học sinh giỏi nhất?",
                    "So sánh 4 số liệu" to "Trong các số 24, 29, 25, 21 thì số lớn nhất là 29.",
                    "Xác định tên lớp" to "Số 29 là của lớp 3B. Vậy lớp 3B có nhiều học sinh giỏi nhất."
                )
                has(statement, "Đổi") -> lesson(
                    "Quy đổi đơn vị Khối lượng (kg, g) và Dung tích (l, ml)",
                    "Ghi nhớ: 1 kg = 1000 g; 1 l = 1000 ml. Khi đổi từ kg sang g (hoặc từ l sang ml), ta lấy số đó nhân với 1000.",
                    "Đổi đơn vị: 4 kg = ... g và 6 l = ... ml",
                    "Đổi kg sang g" to "4 × 1000 = 4000 g.",
                    "Đổi l sang ml" to "6 × 1000 = 6000 ml."
                )
                else -> lesson(
                    "Tính toán với đơn vị gam (g) và mi-li-lít (ml)",
                    "Khi cộng hoặc trừ các khối lượng cùng đơn vị gam (g), ta thực hiện cộng hoặc trừ các số rồi viết đơn vị g vào sau kết quả.",
                    "Túi thứ nhất nặng 250 g, túi thứ hai nặng 300 g. Cả hai túi nặng bao nhiêu gam?",
                    "Cộng khối lượng hai túi" to "250 + 300 = 550.",
                    "Ghi đơn vị" to "Đáp số: 550 g."
                )
            }

            // =====================================================================
            // LỚP 4 (10 NODES - ĐA NHÁNH)
            // =====================================================================
            "G4_01_MILLIONS_CLASS_ROUNDING" -> when {
                has(statement, "Làm tròn") -> lesson(
                    "Làm tròn các số tự nhiên lớn (đến lớp triệu)",
                    "Xác định chữ số ở hàng cần làm tròn, sau đó nhìn chữ số đứng NGAY BÊN PHẢI nó: nếu từ 5 trở lên thì cộng 1 vào hàng làm tròn, nếu bé hơn 5 thì giữ nguyên; cuối cùng đổi tất cả chữ số bên phải thành 0.",
                    "Làm tròn số 15 482 900 đến hàng trăm nghìn.",
                    "Xét chữ số bên phải hàng trăm nghìn" to "Chữ số hàng trăm nghìn là 4; chữ số ngay bên phải (hàng chục nghìn) là 8 (lớn hơn 5).",
                    "Làm tròn lên" to "Tăng 4 thành 5 và thay các chữ số bên phải bằng 0: được 15 500 000."
                )
                has(statement, "viết là") -> lesson(
                    "Viết số tự nhiên từ cấu tạo Hàng và Lớp",
                    "Viết lần lượt từ lớp triệu đến lớp nghìn rồi lớp đơn vị (mỗi lớp phải có đủ 3 chữ số; hàng nào không có giá trị thì viết chữ số 0).",
                    "Số gồm 5 triệu, 3 trăm nghìn và 7 nghìn viết là gì?",
                    "Xác định từng hàng từ cao xuống thấp" to "Hàng triệu: 5; trăm nghìn: 3; chục nghìn: 0; nghìn: 7; trăm: 0; chục: 0; đơn vị: 0.",
                    "Viết số tách theo lớp" to "5 307 000."
                )
                else -> lesson(
                    "Cấu tạo Hàng và Lớp của số tự nhiên lớn",
                    "Tính từ phải sang trái, cứ 3 hàng hợp thành 1 lớp:\n- Lớp đơn vị: hàng đơn vị, hàng chục, hàng trăm.\n- Lớp nghìn: hàng nghìn, hàng chục nghìn, hàng trăm nghìn.\n- Lớp triệu: hàng triệu, hàng chục triệu, hàng trăm triệu.",
                    "Trong số 24 681 357, lớp triệu là bao nhiêu và chữ số hàng trăm nghìn là chữ số nào?",
                    "Tách theo từng lớp 3 chữ số" to "24 | 681 | 357.",
                    "Đọc giá trị" to "Số gồm 24 triệu; chữ số hàng trăm nghìn là chữ số 6."
                )
            }

            "G4_02_ALGEBRAIC_EXPRESSIONS_LAWS" -> if (has(statement, "Tính nhanh")) {
                lesson(
                    "Tính bằng cách thuận tiện nhất (Phân phối & Kết hợp)",
                    "- Nhân một số với một tổng: a × b + a × c = a × (b + c).\n- Tính chất giao hoán và kết hợp của phép cộng: nhóm hai số có tổng tròn chục, tròn trăm lại với nhau: (a + c) + b.",
                    "Tính nhanh: 36 × 28 + 36 × 72 = ?",
                    "Đặt thừa số chung 36 ra ngoài" to "36 × 28 + 36 × 72 = 36 × (28 + 72).",
                    "Tính tổng tròn trăm rồi nhân" to "36 × 100 = 3600."
                )
            } else {
                lesson(
                    "Tính giá trị của biểu thức chứa chữ",
                    "Muốn tính giá trị của biểu thức chứa chữ (như a, b, x), ta thay giá trị số của chữ đó vào biểu thức rồi thực hiện phép tính theo đúng thứ tự (trong ngoặc trước; nhân chia trước, cộng trừ sau).",
                    "Tính giá trị của biểu thức 4 × x + 15 với x = 6.",
                    "Thay x = 6 vào biểu thức" to "Ta có phép tính: 4 × 6 + 15.",
                    "Thực hiện nhân trước, cộng sau" to "24 + 15 = 39. Giá trị biểu thức là 39."
                )
            }

            "G4_03_MUL_DIV_2_DIGITS" -> if (has(statement, "chia hết")) {
                lesson(
                    "Dấu hiệu chia hết cho 2, 5, 3 và 9",
                    "- Chia hết cho 2: chữ số tận cùng là 0, 2, 4, 6, 8.\n- Chia hết cho 5: chữ số tận cùng là 0 hoặc 5.\n- Vừa chia hết cho 2 vừa chia hết cho 5: chữ số tận cùng là 0.\n- Chia hết cho 9 (hoặc 3): TỔNG các chữ số của nó chia hết cho 9 (hoặc 3).",
                    "Trong các số 245, 360, 419; số nào chia hết cho cả 2, 5 và 9?",
                    "Xét chữ số tận cùng" to "360 có tận cùng là 0 nên chia hết cho cả 2 và 5.",
                    "Xét tổng các chữ số" to "3 + 6 + 0 = 9 (chia hết cho 9). Vậy 360 chia hết cho cả 2, 5 và 9."
                )
            } else {
                lesson(
                    "Nhân, chia với số có hai chữ số",
                    "- Nhân với số có 2 chữ số: tích riêng thứ hai phải viết LỆCH SANG TRÁI MỘT CỘT so với tích riêng thứ nhất.\n- Chia cho số có 2 chữ số: chia từ trái sang phải, ở mỗi lượt chia thì số dư luôn phải bé hơn số chia.",
                    "Tính: 24 × 13 = ? và 312 : 24 = ?",
                    "Nhân 24 × 13" to "Tích riêng 1: 24 × 3 = 72. Tích riêng 2: 24 × 1 chục = 240. Cộng lại: 72 + 240 = 312.",
                    "Chia 312 : 24" to "31 : 24 được 1 (dư 7), hạ 2 được 72; 72 : 24 được 3. Kết quả là 13."
                )
            }

            "G4_04_AVERAGE_AND_UNIT_METHOD" -> when {
                has(statement, "Tìm số thứ ba") -> lesson(
                    "Tìm số hạng chưa biết khi biết Số trung bình cộng",
                    "Bước 1: Tìm tổng của các số = Trung bình cộng × Số các số hạng.\nBước 2: Tìm số còn thiếu = Tổng vừa tìm được trừ đi các số hạng đã biết.",
                    "Trung bình cộng của ba số là 40. Hai trong ba số đó là 35 và 42. Tìm số thứ ba.",
                    "Bước 1: Tìm tổng của ba số" to "40 × 3 = 120.",
                    "Bước 2: Tìm số thứ ba" to "120 - (35 + 42) = 120 - 77 = 43."
                )
                has(statement, "trung bình cộng") -> lesson(
                    "Quy tắc tìm Số trung bình cộng",
                    "Muốn tìm số trung bình cộng của nhiều số, ta tính TỔNG của các số đó, rồi lấy tổng đó CHIA cho số các số hạng.",
                    "Tìm số trung bình cộng của ba số: 24, 30 và 36.",
                    "Bước 1: Tính tổng các số" to "24 + 30 + 36 = 90.",
                    "Bước 2: Chia cho số các số hạng (3 số)" to "90 : 3 = 30. Trung bình cộng là 30."
                )
                else -> lesson(
                    "Bài toán liên quan đến Rút về đơn vị",
                    "Bước 1 (Rút về đơn vị): Tìm giá trị của 1 đơn vị bằng phép tính CHIA.\nBước 2: Tìm giá trị của nhiều đơn vị bằng phép tính NHÂN.",
                    "Mua 4 quyển vở hết 24 000 đồng. Hỏi mua 7 quyển vở như thế hết bao nhiêu tiền?",
                    "Bước 1: Giá tiền 1 quyển vở" to "24 000 : 4 = 6000 (đồng).",
                    "Bước 2: Giá tiền 7 quyển vở" to "6000 × 7 = 42 000 (đồng). Đáp số: 42 000 đồng."
                )
            }

            "G4_05_SUM_DIFFERENCE_MODEL" -> lesson(
                "Tìm hai số khi biết Tổng và Hiệu của hai số đó",
                "Hai công thức cốt lõi cần nhớ:\n- Số lớn = (Tổng + Hiệu) : 2\n- Số bé = (Tổng - Hiệu) : 2\n(Đối tượng nào 'nhiều hơn' thì đóng vai trò là Số lớn).",
                "Tổng hai số là 60, hiệu hai số là 14. Tìm số lớn và số bé.",
                "Tìm số lớn" to "(60 + 14) : 2 = 74 : 2 = 37.",
                "Tìm số bé" to "(60 - 14) : 2 = 46 : 2 = 23 (Kiểm tra: 37 + 23 = 60 và 37 - 23 = 14)."
            )

            "G4_06_FRACTIONS_CONCEPT_COMPARE" -> when {
                has(statement, "Rút gọn") -> lesson(
                    "Rút gọn phân số về phân số tối giản",
                    "Chia cả tử số và mẫu số cho cùng một số tự nhiên lớn hơn 1 (ưu tiên chia cho ước chung lớn nhất) cho đến khi không cùng chia hết cho số nào lớn hơn 1 được nữa.",
                    "Rút gọn phân số 12/18.",
                    "Tìm số chia chung lớn nhất" to "12 và 18 cùng chia hết cho 6.",
                    "Chia cả tử và mẫu cho 6" to "12/18 = (12 : 6)/(18 : 6) = 2/3."
                )
                has(statement, "bằng phân số") -> lesson(
                    "Tính chất cơ bản của phân số (Phân số bằng nhau)",
                    "Nếu nhân (hoặc chia) cả tử số và mẫu số của một phân số với cùng một số tự nhiên khác 0 thì ta được một phân số mới bằng phân số đã cho. (Lưu ý: Cộng thêm cùng một số vào tử và mẫu KHÔNG tạo ra phân số bằng nhau).",
                    "Tìm phân số bằng phân số 2/3.",
                    "Nhân cả tử số và mẫu số với 3" to "2/3 = (2 × 3)/(3 × 3) = 6/9.",
                    "Kết luận" to "Phân số 6/9 bằng phân số 2/3."
                )
                has(statement, "lớn hơn 1") -> lesson(
                    "So sánh phân số với 1",
                    "- Phân số có Tử số LỚN HƠN Mẫu số thì phân số đó lớn hơn 1.\n- Phân số có Tử số BÉ HƠN Mẫu số thì phân số đó bé hơn 1.\n- Phân số có Tử số BẰNG Mẫu số thì bằng 1.",
                    "Trong các phân số 3/5, 7/4, 2/3, 4/7; phân số nào lớn hơn 1?",
                    "So sánh tử số với mẫu số" to "Chỉ có phân số 7/4 có tử số (7) lớn hơn mẫu số (4).",
                    "Kết luận" to "Phân số lớn hơn 1 là 7/4."
                )
                else -> lesson(
                    "So sánh các phân số khác mẫu số",
                    "Muốn so sánh các phân số khác mẫu số, ta quy đồng mẫu số các phân số đó (đưa về cùng mẫu số chung) rồi so sánh các tử số: phân số nào có tử số lớn hơn thì lớn hơn.",
                    "So sánh hai phân số 2/3 và 3/4.",
                    "Quy đồng mẫu số (MSC = 12)" to "2/3 = 8/12 và 3/4 = 9/12.",
                    "So sánh hai tử số" to "Vì 9 lớn hơn 8 nên 9/12 lớn hơn 8/12. Vậy 3/4 lớn hơn 2/3."
                )
            }

            "G4_07_FRACTIONS_FOUR_OPS" -> fractionOpsLesson(statement)

            "G4_08_ANGLES_PARALLEL_SHAPES" -> when {
                has(statement, "diện tích hình thoi") -> lesson(
                    "Diện tích hình thoi",
                    "Diện tích hình thoi bằng tích độ dài hai đường chéo chia cho 2 (cùng đơn vị đo): S = (m × n) : 2.",
                    "Tính diện tích hình thoi có độ dài hai đường chéo là 6 cm và 8 cm.",
                    "Nhân độ dài hai đường chéo" to "6 × 8 = 48.",
                    "Chia cho 2" to "48 : 2 = 24 (cm²). Diện tích hình thoi là 24 cm²."
                )
                has(statement, "diện tích hình bình hành") -> lesson(
                    "Diện tích hình bình hành",
                    "Diện tích hình bình hành bằng độ dài đáy nhân với chiều cao (cùng đơn vị đo): S = a × h.",
                    "Tính diện tích hình bình hành có độ dài đáy 9 cm và chiều cao 5 cm.",
                    "Áp dụng công thức" to "S = đáy × chiều cao (không chia 2).",
                    "Tính kết quả" to "9 × 5 = 45 (cm²). Diện tích hình bình hành là 45 cm²."
                )
                has(statement, "chu vi") -> lesson(
                    "Chu vi hình bình hành",
                    "Chu vi hình bình hành bằng tổng độ dài hai cạnh liên tiếp nhân với 2: P = (a + b) × 2.",
                    "Hình bình hành có hai cạnh liên tiếp dài 7 cm và 4 cm. Tính chu vi.",
                    "Cộng hai cạnh liên tiếp" to "7 + 4 = 11 (cm).",
                    "Nhân với 2" to "11 × 2 = 22 (cm). Chu vi là 22 cm."
                )
                else -> lesson(
                    "Phân loại góc (nhọn, vuông, tù, bẹt) & Hình thoi, Hình bình hành",
                    "- Góc vuông = 90°.\n- Góc nhọn: bé hơn 90°.\n- Góc tù: lớn hơn 90° và bé hơn 180°.\n- Góc bẹt = 180° (bằng 2 góc vuông).\n- Hình thoi có 2 cặp cạnh đối diện song song và 4 cạnh bằng nhau.",
                    "Góc có số đo 120° là góc gì?",
                    "So sánh với góc vuông (90°) và góc bẹt (180°)" to "120° lớn hơn 90° nhưng bé hơn 180°.",
                    "Kết luận" to "Đó là Góc tù."
                )
            }

            "G4_09_MEASURE_TON_M2_CENTURY" -> if (has(statement, "thế kỷ") && has(statement, "Năm")) {
                lesson(
                    "Cách xác định Thế kỷ của một năm",
                    "Cứ 100 năm là 1 thế kỷ (Thế kỷ I: năm 1 đến 100; Thế kỷ II: năm 101 đến 200...).\nMẹo tính nhanh: Lấy (Năm - 1) chia cho 100 (lấy phần nguyên) rồi cộng thêm 1, sau đó viết bằng chữ số La Mã.",
                    "Năm 1945 thuộc thế kỷ nào?",
                    "Tính số thế kỷ" to "(1945 - 1) : 100 = 19 (dư 44). Lấy 19 + 1 = 20.",
                    "Viết bằng chữ số La Mã" to "Số 20 viết là XX. Vậy năm 1945 thuộc thế kỷ XX."
                )
            } else {
                lesson(
                    "Bảng đơn vị Khối lượng (Yến, Tạ, Tấn), Diện tích (m², dm², cm²) & Thế kỷ",
                    "- Khối lượng: 1 yến = 10 kg; 1 tạ = 100 kg; 1 tấn = 1000 kg.\n- Diện tích (hai đơn vị liền nhau hơn kém nhau 100 lần): 1 m² = 100 dm²; 1 dm² = 100 cm².\n- Thời gian: 1 thế kỷ = 100 năm.",
                    "Đổi: 4 tấn = ... kg và 5 m² = ... dm²",
                    "Đổi tấn sang kg" to "4 × 1000 = 4000 kg.",
                    "Đổi m² sang dm²" to "5 × 100 = 500 dm²."
                )
            }

            "G4_10_BAR_CHART_PROBABILITY" -> when {
                has(statement, "Trung bình") -> lesson(
                    "Tính số trung bình cộng trên Biểu đồ cột",
                    "Bước 1: Đọc số liệu của tất cả các cột rồi cộng lại.\nBước 2: Chia tổng đó cho số cột.",
                    "Biểu đồ cột: Tuần 1 (18 cuốn), Tuần 2 (22 cuốn), Tuần 3 (16 cuốn), Tuần 4 (24 cuốn). Trung bình mỗi tuần đọc bao nhiêu cuốn?",
                    "Tính tổng 4 tuần" to "18 + 22 + 16 + 24 = 80 (cuốn).",
                    "Chia cho 4 tuần" to "80 : 4 = 20 (cuốn). Trung bình mỗi tuần đọc 20 cuốn."
                )
                has(statement, "viên bi", "khả năng") -> lesson(
                    "So sánh khả năng xảy ra khi lấy ngẫu nhiên",
                    "Trong hộp có nhiều loại bi màu khác nhau: màu nào có SỐ LƯỢNG VIÊN BI NHIỀU NHẤT thì khi lấy ngẫu nhiên 1 viên, màu đó có khả năng được lấy ra nhiều nhất.",
                    "Trong hộp có 3 bi đỏ, 9 bi xanh, 2 bi vàng, 5 bi trắng. Màu nào có khả năng được lấy ra nhiều nhất?",
                    "So sánh số lượng từng màu" to "Số lượng bi xanh (9 viên) lớn hơn tất cả các màu còn lại.",
                    "Kết luận" to "Bi màu Xanh có khả năng được lấy ra nhiều nhất."
                )
                else -> lesson(
                    "Đọc và phân tích số liệu trên Biểu đồ cột",
                    "Đọc số liệu từng cột rồi thực hiện phép tính theo câu hỏi: hỏi 'cả hai tuần' thì cộng; hỏi 'nhiều hơn bao nhiêu' thì lấy cột lớn trừ cột bé; hỏi 'nhiều nhất' thì chọn cột có số lớn nhất.",
                    "Biểu đồ cột: Tuần 1 đọc 15 cuốn, Tuần 3 đọc 24 cuốn. Tuần 3 đọc nhiều hơn Tuần 1 bao nhiêu cuốn?",
                    "Lấy số sách Tuần 3 trừ Tuần 1" to "24 - 15 = 9 (cuốn).",
                    "Đáp số" to "9 cuốn."
                )
            }

            // =====================================================================
            // LỚP 5 (10 NODES - ĐA NHÁNH)
            // =====================================================================
            "G5_01_DECIMAL_FRACTIONS_MIXED" -> when {
                FRACTION_OP.containsMatchIn(exprPart(statement)) -> fractionOpsLesson(statement)
                has(statement, "hỗn số") -> lesson(
                    "Chuyển đổi Hỗn số thành Phân số",
                    "Hỗn số gồm phần nguyên và phần phân số. Muốn chuyển hỗn số thành phân số:\n- Tử số = Phần nguyên × Mẫu số + Tử số.\n- Mẫu số = Giữ nguyên mẫu số cũ.",
                    "Viết hỗn số 2 3/5 thành phân số.",
                    "Tính tử số mới" to "2 × 5 + 3 = 13.",
                    "Giữ nguyên mẫu số" to "Ta được phân số 13/5."
                )
                has(statement, "dạng phân số thập phân") -> lesson(
                    "Viết Số thập phân dưới dạng Phân số thập phân",
                    "Đếm xem phần thập phân (sau dấu phẩy) có bao nhiêu chữ số thì mẫu số là 10, 100, 1000... có bấy nhiêu chữ số 0; tử số là số tự nhiên khi bỏ dấu phẩy.",
                    "Viết số thập phân 0,37 dưới dạng phân số thập phân.",
                    "Đếm chữ số sau dấu phẩy" to "0,37 có 2 chữ số ở phần thập phân nên mẫu số là 100.",
                    "Viết phân số" to "0,37 = 37/100."
                )
                else -> lesson(
                    "Viết Phân số thập phân dưới dạng Số thập phân",
                    "Phân số thập phân có mẫu số là 10, 100, 1000... Mẫu số có bao nhiêu chữ số 0 thì phần thập phân (sau dấu phẩy) phải có đúng bấy nhiêu chữ số.",
                    "Viết phân số thập phân 47/100 dưới dạng số thập phân.",
                    "Đếm số chữ số 0 ở mẫu số" to "Mẫu số 100 có 2 chữ số 0 nên sau dấu phẩy có 2 chữ số.",
                    "Đặt dấu phẩy" to "47/100 = 0,47."
                )
            }

            "G5_02_DECIMALS_PLACE_ROUNDING" -> when {
                has(statement, "Làm tròn") -> lesson(
                    "Làm tròn số thập phân",
                    "Xét chữ số ngay bên phải hàng cần làm tròn:\n- Nếu từ 5 trở lên thì cộng thêm 1 vào chữ số hàng cần làm tròn.\n- Nếu bé hơn 5 thì giữ nguyên.\nSau đó bỏ tất cả các chữ số ở bên phải hàng làm tròn.",
                    "Làm tròn số 12,346 đến hàng phần trăm.",
                    "Xét chữ số hàng phần nghìn" to "Chữ số hàng phần trăm là 4; chữ số ngay bên phải là 6 (từ 5 trở lên).",
                    "Làm tròn lên" to "Tăng 4 thành 5 và bỏ chữ số 6: được 12,35."
                )
                has(statement, "lớn nhất", "bé nhất") -> lesson(
                    "So sánh các số thập phân",
                    "Bước 1: So sánh phần nguyên trước (số nào có phần nguyên lớn hơn thì lớn hơn).\nBước 2: Nếu phần nguyên bằng nhau, so sánh lần lượt từng hàng ở phần thập phân từ trái sang phải: hàng phần mười, hàng phần trăm, hàng phần nghìn.",
                    "So sánh hai số thập phân: 7,85 và 7,9.",
                    "So sánh phần nguyên" to "Cả hai số đều có phần nguyên là 7.",
                    "So sánh hàng phần mười" to "7,9 có hàng phần mười là 9; 7,85 có hàng phần mười là 8. Vì 9 lớn hơn 8 nên 7,9 lớn hơn 7,85."
                )
                has(statement, "viết là") -> lesson(
                    "Viết số thập phân theo cấu tạo hàng",
                    "Viết phần nguyên trước, viết dấu phẩy, rồi viết lần lượt chữ số hàng phần mười, hàng phần trăm, hàng phần nghìn.",
                    "Số gồm 5 đơn vị, 3 phần mười và 8 phần trăm viết là gì?",
                    "Ghép các hàng theo thứ tự" to "Phần nguyên là 5, sau dấu phẩy là 3 (phần mười) rồi đến 8 (phần trăm).",
                    "Kết quả" to "5,38."
                )
                else -> lesson(
                    "Các hàng của Số thập phân",
                    "Từ trái sang phải:\n- Trước dấu phẩy (Phần nguyên): ... hàng trăm, hàng chục, hàng đơn vị.\n- Sau dấu phẩy (Phần thập phân): thứ nhất là hàng phần mười, thứ hai là hàng phần trăm, thứ ba là hàng phần nghìn.",
                    "Trong số 25,348, chữ số 4 thuộc hàng nào?",
                    "Đếm vị trí sau dấu phẩy" to "Sau dấu phẩy: chữ số 3 ở vị trí thứ nhất (phần mười), chữ số 4 ở vị trí thứ hai.",
                    "Kết luận" to "Chữ số 4 thuộc hàng phần trăm."
                )
            }

            "G5_03_DECIMALS_FOUR_OPS" -> {
                val e = exprPart(statement)
                when {
                    Regex("""×\s*(10|100|1000)\b""").containsMatchIn(e) -> lesson(
                        "Nhân nhẩm một số thập phân với 10, 100, 1000",
                        "Muốn nhân một số thập phân với 10, 100, 1000... ta chỉ việc chuyển dấu phẩy của số đó sang BÊN PHẢI lần lượt 1, 2, 3... chữ số.",
                        "Tính nhẩm: 3,45 × 10 = ? và 3,45 × 100 = ?",
                        "Nhân với 10 (dịch phải 1 chữ số)" to "3,45 × 10 = 34,5.",
                        "Nhân với 100 (dịch phải 2 chữ số)" to "3,45 × 100 = 345."
                    )
                    e.contains("×") -> lesson(
                        "Nhân một số thập phân với một số tự nhiên",
                        "Thực hiện nhân như nhân các số tự nhiên. Sau đó đếm xem trong phần thập phân của số thập phân có bao nhiêu chữ số thì dùng dấu phẩy tách ở tích ra bấy nhiêu chữ số kể từ phải sang trái.",
                        "Tính: 4,25 × 3 = ?",
                        "Nhân như số tự nhiên" to "425 × 3 = 1275.",
                        "Đặt dấu phẩy ở tích" to "4,25 có 2 chữ số sau dấu phẩy nên tách 2 chữ số từ phải sang trái: được 12,75."
                    )
                    e.contains(" : ") -> lesson(
                        "Chia một số thập phân cho một số tự nhiên",
                        "Chia phần nguyên của số bị chia cho số chia trước. Khi hạ chữ số đầu tiên ở phần thập phân của số bị chia xuống để tiếp tục chia thì phải viết ngay dấu phẩy vào bên phải thương đã tìm được.",
                        "Tính: 7,5 : 3 = ?",
                        "Chia phần nguyên" to "7 : 3 được 2 (dư 1), viết 2 vào thương rồi đánh dấu phẩy (2,).",
                        "Hạ phần thập phân chia tiếp" to "Hạ 5 được 15; 15 : 3 = 5. Vậy 7,5 : 3 = 2,5."
                    )
                    e.contains(" - ") -> lesson(
                        "Trừ hai số thập phân",
                        "Đặt tính sao cho các chữ số ở cùng một hàng và các dấu phẩy đặt thẳng cột với nhau (có thể viết thêm chữ số 0 vào bên phải phần thập phân cho đủ hàng). Trừ như số tự nhiên rồi đặt dấu phẩy ở hiệu thẳng cột.",
                        "Tính: 8,5 - 3,2 = ?",
                        "Trừ phần thập phân và phần nguyên" to "Phần thập phân: 5 - 2 = 3. Phần nguyên: 8 - 3 = 5.",
                        "Đặt dấu phẩy thẳng cột" to "Kết quả là 5,3."
                    )
                    else -> lesson(
                        "Cộng hai số thập phân",
                        "Đặt tính sao cho các chữ số ở cùng một hàng và các dấu phẩy đặt thẳng cột với nhau. Cộng như cộng các số tự nhiên rồi viết dấu phẩy ở tổng thẳng cột với các dấu phẩy của các số hạng.",
                        "Tính: 4,6 + 2,7 = ?",
                        "Cộng từ phải sang trái" to "6 + 7 = 13 viết 3 nhớ 1; 4 + 2 + 1 = 7.",
                        "Đặt dấu phẩy thẳng cột" to "Kết quả là 7,3."
                    )
                }
            }

            "G5_04_SUM_DIFF_RATIO_PROBLEMS" -> when {
                has(statement, "Hiệu") -> lesson(
                    "Tìm hai số khi biết Hiệu và Tỉ số của hai số đó",
                    "Các bước giải:\n- Bước 1: Tìm hiệu số phần bằng nhau.\n- Bước 2: Giá trị 1 phần = Hiệu hai số : Hiệu số phần.\n- Bước 3: Số bé = Giá trị 1 phần × Số phần của số bé; Số lớn = Giá trị 1 phần × Số phần của số lớn.",
                    "Hiệu hai số là 18. Tỉ số của số bé và số lớn là 2/5. Tìm số lớn.",
                    "Bước 1: Tìm hiệu số phần bằng nhau" to "5 - 2 = 3 (phần).",
                    "Bước 2 & 3: Tìm giá trị 1 phần và số lớn" to "Giá trị 1 phần: 18 : 3 = 6. Số lớn là: 6 × 5 = 30."
                )
                has(statement, "Tỉ số của hai số") -> lesson(
                    "Tìm tỉ số của hai số",
                    "Tỉ số của số a và số b là a : b hay a/b (chú ý giữ đúng thứ tự số nào nói trước viết ở tử số, số nào nói sau viết ở mẫu số), sau đó rút gọn về phân số tối giản.",
                    "Tìm tỉ số của hai số 12 và 18.",
                    "Lập phân số tỉ số" to "12/18.",
                    "Rút gọn phân số" to "Chia cả tử và mẫu cho 6: 12/18 = 2/3."
                )
                else -> lesson(
                    "Tìm hai số khi biết Tổng và Tỉ số của hai số đó",
                    "Các bước giải:\n- Bước 1: Tìm tổng số phần bằng nhau.\n- Bước 2: Giá trị 1 phần = Tổng hai số : Tổng số phần.\n- Bước 3: Số bé = Giá trị 1 phần × Số phần của số bé; Số lớn = Giá trị 1 phần × Số phần của số lớn.",
                    "Tổng hai số là 50. Tỉ số của số bé và số lớn là 2/3. Tìm hai số đó.",
                    "Bước 1: Tìm tổng số phần bằng nhau" to "2 + 3 = 5 (phần).",
                    "Bước 2 & 3: Tìm giá trị 1 phần và hai số" to "Giá trị 1 phần: 50 : 5 = 10. Số bé: 10 × 2 = 20; Số lớn: 10 × 3 = 30."
                )
            }

            "G5_05_PERCENTAGE_MAP_SCALE" -> when {
                has(statement, "Bản đồ") -> lesson(
                    "Bài toán về Tỉ lệ bản đồ",
                    "Bản đồ ghi tỉ lệ 1 : n nghĩa là 1 cm trên bản đồ ứng với n cm ngoài thực tế.\n- Bước 1: Độ dài thật (cm) = Độ dài trên bản đồ (cm) × n.\n- Bước 2: Đổi từ cm sang m (chia cho 100) hoặc km.",
                    "Bản đồ có tỉ lệ 1 : 1000. Quãng đường trên bản đồ dài 4 cm. Độ dài thật là bao nhiêu mét?",
                    "Bước 1: Tính độ dài thật theo cm" to "4 × 1000 = 4000 (cm).",
                    "Bước 2: Đổi ra mét" to "4000 cm = 40 m (vì 1 m = 100 cm)."
                )
                has(statement, "giảm giá") -> lesson(
                    "Bài toán tính giá sau khi giảm giá (%)",
                    "- Bước 1: Số tiền được giảm = Giá ban đầu × Phần trăm giảm : 100.\n- Bước 2: Giá sau khi giảm = Giá ban đầu - Số tiền được giảm.",
                    "Một món hàng giá 100 000 đồng được giảm giá 20%. Hỏi sau khi giảm, món hàng có giá bao nhiêu?",
                    "Bước 1: Tính số tiền được giảm" to "100 000 × 20 : 100 = 20 000 (đồng).",
                    "Bước 2: Tính giá sau khi giảm" to "100 000 - 20 000 = 80 000 (đồng)."
                )
                has(statement, "Tỉ số phần trăm") -> lesson(
                    "Tìm tỉ số phần trăm của hai số",
                    "Muốn tìm tỉ số phần trăm của hai số a và b:\n- Bước 1: Tìm thương của a và b.\n- Bước 2: Nhân thương đó với 100 và viết thêm kí hiệu % vào bên phải (hoặc tính a × 100 : b).",
                    "Lớp có 40 học sinh, trong đó có 10 học sinh nữ. Tìm tỉ số phần trăm của số học sinh nữ so với cả lớp.",
                    "Tính thương rồi nhân 100" to "10 × 100 : 40 = 1000 : 40 = 25.",
                    "Viết kí hiệu phần trăm" to "25%."
                )
                else -> lesson(
                    "Tìm giá trị phần trăm của một số cho trước",
                    "Muốn tìm p% của một số A, ta lấy số A nhân với p rồi chia cho 100 (hoặc lấy A chia cho 100 rồi nhân với p).",
                    "Tìm 25% của 80 kg.",
                    "Áp dụng quy tắc" to "80 × 25 : 100 = 2000 : 100 = 20.",
                    "Ghi đơn vị" to "20 kg."
                )
            }

            "G5_06_TRIANGLE_TRAPEZOID_CIRCLE" -> when {
                has(statement, "hình thang") -> lesson(
                    "Diện tích hình thang",
                    "Diện tích hình thang bằng tổng độ dài hai đáy nhân với chiều cao (cùng một đơn vị đo) rồi chia cho 2:\nS = (a + b) × h : 2.",
                    "Tính diện tích hình thang có đáy lớn 10 cm, đáy bé 6 cm và chiều cao 4 cm.",
                    "Bước 1: Tính tổng hai đáy" to "10 + 6 = 16 (cm).",
                    "Bước 2: Nhân với chiều cao rồi chia 2" to "16 × 4 : 2 = 32 (cm²)."
                )
                has(statement, "chu vi hình tròn") -> lesson(
                    "Chu vi hình tròn",
                    "- Nếu biết đường kính d: C = d × 3,14.\n- Nếu biết bán kính r: C = r × 2 × 3,14.\n(Đơn vị chu vi là cm, m — không có số mũ 2).",
                    "Tính chu vi hình tròn có bán kính r = 5 cm.",
                    "Áp dụng công thức" to "C = 5 × 2 × 3,14.",
                    "Tính kết quả" to "10 × 3,14 = 31,4 (cm)."
                )
                has(statement, "diện tích hình tròn") -> lesson(
                    "Diện tích hình tròn",
                    "Muốn tính diện tích của hình tròn, ta lấy bán kính nhân với bán kính rồi nhân với số 3,14:\nS = r × r × 3,14 (đơn vị: cm², m²).",
                    "Tính diện tích hình tròn có bán kính r = 5 cm.",
                    "Tính bán kính nhân bán kính" to "5 × 5 = 25.",
                    "Nhân với 3,14" to "25 × 3,14 = 78,5 (cm²)."
                )
                has(statement, "ha", "km²") -> lesson(
                    "Đơn vị đo diện tích lớn: Héc-ta (ha) và Ki-lô-mét vuông (km²)",
                    "Hai mối quan hệ cần thuộc lòng:\n- 1 ha = 10 000 m²\n- 1 km² = 100 ha = 1 000 000 m².",
                    "Đổi: 5 ha = ... m² và 3 km² = ... ha",
                    "Đổi 5 ha sang m²" to "5 × 10 000 = 50 000 m².",
                    "Đổi 3 km² sang ha" to "3 × 100 = 300 ha."
                )
                else -> lesson(
                    "Diện tích hình tam giác",
                    "Muốn tính diện tích hình tam giác, ta lấy độ dài đáy nhân với chiều cao (cùng một đơn vị đo) rồi chia cho 2:\nS = (a × h) : 2.",
                    "Tính diện tích hình tam giác có độ dài đáy 8 cm và chiều cao 6 cm.",
                    "Nhân đáy với chiều cao" to "8 × 6 = 48.",
                    "Chia cho 2" to "48 : 2 = 24 (cm²). Diện tích tam giác là 24 cm²."
                )
            }

            "G5_07_SURFACE_AREA_VOLUME_3D" -> when {
                has(statement, "lập phương") && has(statement, "toàn phần") -> lesson(
                    "Diện tích toàn phần của Hình lập phương",
                    "Hình lập phương có 6 mặt đều là các hình vuông bằng nhau:\n- Diện tích 1 mặt = cạnh × cạnh.\n- Diện tích toàn phần Stp = cạnh × cạnh × 6 (đơn vị: cm²).",
                    "Tính diện tích toàn phần của hình lập phương có cạnh dài 4 cm.",
                    "Tính diện tích một mặt" to "4 × 4 = 16 (cm²).",
                    "Nhân với 6 mặt" to "16 × 6 = 96 (cm²)."
                )
                has(statement, "lập phương") -> lesson(
                    "Thể tích của Hình lập phương",
                    "Muốn tính thể tích hình lập phương, ta lấy cạnh nhân với cạnh rồi nhân với cạnh:\nV = a × a × a (đơn vị thể tích: cm³, dm³, m³).",
                    "Tính thể tích hình lập phương có cạnh dài 4 cm.",
                    "Áp dụng công thức" to "V = 4 × 4 × 4.",
                    "Tính kết quả" to "16 × 4 = 64 (cm³)."
                )
                has(statement, "xung quanh") -> lesson(
                    "Diện tích xung quanh của Hình hộp chữ nhật",
                    "Diện tích xung quanh hình hộp chữ nhật bằng chu vi mặt đáy nhân với chiều cao (cùng đơn vị đo):\nSxq = (dài + rộng) × 2 × cao.",
                    "Hình hộp chữ nhật có chiều dài 5 cm, chiều rộng 3 cm, chiều cao 4 cm. Tính diện tích xung quanh.",
                    "Bước 1: Tính chu vi đáy" to "(5 + 3) × 2 = 16 (cm).",
                    "Bước 2: Nhân với chiều cao" to "16 × 4 = 64 (cm²)."
                )
                has(statement, "toàn phần") -> lesson(
                    "Diện tích toàn phần của Hình hộp chữ nhật",
                    "Diện tích toàn phần bằng diện tích xung quanh cộng với diện tích 2 mặt đáy:\nStp = Sxq + (dài × rộng) × 2.",
                    "Hình hộp chữ nhật có chiều dài 5 cm, chiều rộng 3 cm, chiều cao 4 cm. Tính diện tích toàn phần.",
                    "Bước 1: Tính diện tích xung quanh" to "(5 + 3) × 2 × 4 = 64 (cm²).",
                    "Bước 2: Cộng diện tích 2 đáy" to "Diện tích 2 đáy: 5 × 3 × 2 = 30 (cm²). Vậy Stp = 64 + 30 = 94 (cm²)."
                )
                else -> lesson(
                    "Thể tích của Hình hộp chữ nhật",
                    "Muốn tính thể tích hình hộp chữ nhật, ta lấy chiều dài nhân với chiều rộng rồi nhân với chiều cao (cùng đơn vị đo):\nV = dài × rộng × cao (đơn vị: cm³, dm³, m³).",
                    "Hình hộp chữ nhật có chiều dài 5 cm, chiều rộng 3 cm, chiều cao 4 cm. Tính thể tích.",
                    "Áp dụng công thức" to "V = 5 × 3 × 4.",
                    "Tính kết quả" to "15 × 4 = 60 (cm³)."
                )
            }

            "G5_08_TIME_OPS_AND_MOTION" -> when {
                has(statement, "ngược chiều") -> lesson(
                    "Bài toán hai xe chuyển động ngược chiều gặp nhau",
                    "Khi hai xe xuất phát cùng lúc từ A và B đi ngược chiều nhau để gặp nhau:\n- Tổng vận tốc = v1 + v2.\n- Quãng đường AB = (v1 + v2) × Thời gian đi để gặp nhau.",
                    "Hai ô tô đi ngược chiều từ A và B với vận tốc 40 km/giờ và 50 km/giờ. Sau 2 giờ hai xe gặp nhau. Tính quãng đường AB.",
                    "Bước 1: Tính tổng vận tốc hai xe" to "40 + 50 = 90 (km/giờ).",
                    "Bước 2: Tính quãng đường AB" to "90 × 2 = 180 (km). Đáp số: 180 km."
                )
                has(statement, "Tính vận tốc") -> lesson(
                    "Công thức tính Vận tốc (v = s : t)",
                    "Muốn tính vận tốc, ta lấy quãng đường chia cho thời gian: v = s : t (đơn vị: km/giờ hoặc m/phút, m/giây).",
                    "Một ô tô đi quãng đường 180 km trong 3 giờ. Tính vận tốc của ô tô.",
                    "Áp dụng công thức" to "v = s : t = 180 : 3.",
                    "Tính kết quả" to "60 km/giờ."
                )
                has(statement, "bao nhiêu giờ") && has(statement, "vận tốc") -> lesson(
                    "Công thức tính Thời gian (t = s : v)",
                    "Muốn tính thời gian đi, ta lấy quãng đường chia cho vận tốc: t = s : v.",
                    "Một ô tô đi với vận tốc 60 km/giờ trên quãng đường 180 km. Hỏi ô tô đi hết bao nhiêu giờ?",
                    "Áp dụng công thức" to "t = s : v = 180 : 60.",
                    "Tính kết quả" to "3 giờ."
                )
                Regex("""\d+ giờ \d+ phút\s*-\s*\d+ giờ \d+ phút""").containsMatchIn(statement) -> lesson(
                    "Trừ số đo thời gian (có đổi đơn vị)",
                    "Trừ phút với phút, giờ với giờ. Nếu số phút của số bị trừ bé hơn số phút của số trừ thì ta mượn 1 giờ ở số bị trừ đổi thành 60 phút cộng thêm vào số phút rồi mới trừ.",
                    "Tính: 5 giờ 20 phút - 2 giờ 45 phút = ?",
                    "Đổi 1 giờ thành 60 phút" to "5 giờ 20 phút = 4 giờ (20 + 60) phút = 4 giờ 80 phút.",
                    "Thực hiện phép trừ" to "4 giờ 80 phút - 2 giờ 45 phút = 2 giờ 35 phút."
                )
                has(statement, "giờ") && has(statement, "phút") && has(statement, "+") -> lesson(
                    "Cộng số đo thời gian (có đổi đơn vị)",
                    "Cộng giờ với giờ, phút với phút. Nếu tổng số phút từ 60 phút trở lên thì ta đổi 60 phút = 1 giờ rồi cộng thêm 1 giờ đó vào phần giờ.",
                    "Tính: 2 giờ 45 phút + 1 giờ 30 phút = ?",
                    "Cộng từng đơn vị" to "2 giờ + 1 giờ = 3 giờ; 45 phút + 30 phút = 75 phút.",
                    "Đổi 75 phút ra giờ" to "75 phút = 1 giờ 15 phút. Cộng vào được: 4 giờ 15 phút."
                )
                has(statement, "Đổi đơn vị thời gian") -> lesson(
                    "Quy đổi đơn vị đo thời gian",
                    "1 giờ = 60 phút; 1 phút = 60 giây; 1 ngày = 24 giờ. Muốn đổi từ giờ ra phút, ta lấy số giờ nhân với 60.",
                    "Đổi: 3 giờ = ... phút",
                    "Nhân với 60" to "3 × 60 = 180.",
                    "Kết quả" to "180 phút."
                )
                else -> lesson(
                    "Công thức tính Quãng đường (s = v × t)",
                    "Muốn tính quãng đường, ta lấy vận tốc nhân với thời gian: s = v × t.",
                    "Một ô tô đi với vận tốc 50 km/giờ trong 3 giờ. Tính quãng đường đi được.",
                    "Áp dụng công thức" to "s = v × t = 50 × 3.",
                    "Tính kết quả" to "150 km."
                )
            }

            "G5_09_PIE_CHART_PROBABILITY_RATIO" -> when {
                has(statement, "Tỉ số giữa") -> lesson(
                    "Tỉ số mô tả số lần lặp lại / số lượng sự kiện",
                    "Tỉ số giữa số bi màu A và tổng số bi trong hộp được viết dưới dạng phân số: Tử số là số bi màu A, Mẫu số là tổng số bi của tất cả các màu trong hộp (rút gọn nếu cần).",
                    "Trong hộp có 3 bi đỏ và 5 bi xanh. Tỉ số giữa số bi đỏ và tổng số bi trong hộp là bao nhiêu?",
                    "Tính tổng số bi trong hộp" to "3 + 5 = 8 (viên bi).",
                    "Viết tỉ số" to "Tỉ số là 3/8."
                )
                has(statement, "chiếm mấy phần") -> lesson(
                    "Đổi tỉ lệ phần trăm trên Biểu đồ quạt tròn ra phân số",
                    "Tỉ lệ p% tương ứng với phân số p/100. Sau đó ta chia cả tử số và mẫu số cho ước chung lớn nhất để đưa về phân số tối giản.",
                    "Biểu đồ quạt tròn: Bơi lội chiếm 25%. Số em thích bơi lội chiếm mấy phần tổng số em?",
                    "Viết dưới dạng phân số phần trăm" to "25% = 25/100.",
                    "Rút gọn về tối giản" to "Chia cả tử và mẫu cho 25: 25/100 = 1/4."
                )
                has(statement, "bao nhiêu em") -> lesson(
                    "Tính số lượng thực tế từ Biểu đồ hình quạt tròn",
                    "Toàn bộ hình tròn biểu thị 100% tổng số học sinh. Số học sinh của một nhóm chiếm p% được tính bằng: Tổng số học sinh × p : 100.",
                    "Lớp có 40 em. Biểu đồ quạt tròn cho biết có 30% số em thích Cầu lông. Hỏi có bao nhiêu em thích Cầu lông?",
                    "Tìm 30% của 40 em" to "40 × 30 : 100 = 1200 : 100 = 12 (em).",
                    "Đáp số" to "12 em."
                )
                else -> lesson(
                    "Đọc và so sánh trên Biểu đồ hình quạt tròn",
                    "Trên biểu đồ hình quạt tròn, tổng các tỉ lệ phần trăm luôn bằng 100%. Nhóm nào có số phần trăm lớn nhất thì được nhiều người yêu thích nhất.",
                    "Biểu đồ quạt tròn: Đá bóng 50%, Bơi lội 30%, Cầu lông 20%. Môn nào được nhiều em thích nhất?",
                    "So sánh các tỉ lệ phần trăm" to "Trong ba mức 50%, 30%, 20% thì 50% là lớn nhất.",
                    "Kết luận" to "Môn Đá bóng được nhiều em thích nhất."
                )
            }

            "G5_10_COMPREHENSIVE_HEURISTICS" -> when {
                has(statement, "gà") -> lesson(
                    "Phương pháp Giả thiết tạm (Bài toán Vừa gà vừa chó)",
                    "Các bước giải:\n- Bước 1: Giả sử tất cả đều là gà (mỗi con 2 chân) → Tính tổng số chân giả thiết.\n- Bước 2: Tìm số chân bị hụt so với thực tế = Tổng chân thực tế - Tổng chân giả thiết.\n- Bước 3: Vì mỗi con chó hơn mỗi con gà 2 chân (4 - 2 = 2), nên Số chó = Số chân bị hụt : 2.\n- Bước 4: Số gà = Tổng số con - Số chó.",
                    "Vừa gà vừa chó có tất cả 10 con và 28 chân. Hỏi có bao nhiêu con gà?",
                    "Giả sử cả 10 con đều là gà" to "Tổng số chân khi đó là: 10 × 2 = 20 (chân). Số chân thiếu là: 28 - 20 = 8 (chân).",
                    "Tìm số chó rồi suy ra số gà" to "Số chó là: 8 : 2 = 4 (con). Số gà là: 10 - 4 = 6 (con)."
                )
                has(statement, "vòi") -> lesson(
                    "Bài toán Công việc chung: Hai vòi nước cùng chảy vào bể",
                    "Coi toàn bộ bể nước đầy là 1 đơn vị:\n- Bước 1: Trong 1 giờ, vòi 1 chảy được 1/a bể, vòi 2 chảy được 1/b bể.\n- Bước 2: Trong 1 giờ, cả hai vòi cùng chảy được: 1/a + 1/b (bể).\n- Bước 3: Thời gian để hai vòi cùng chảy đầy bể = 1 : (1/a + 1/b) (giờ).",
                    "Vòi thứ nhất chảy đầy bể trong 3 giờ, vòi thứ hai chảy đầy bể trong 6 giờ. Cả hai vòi cùng chảy thì sau mấy giờ đầy bể?",
                    "Tìm phần bể hai vòi chảy trong 1 giờ" to "1/3 + 1/6 = 2/6 + 1/6 = 3/6 = 1/2 (bể).",
                    "Tìm thời gian chảy đầy bể" to "1 : 1/2 = 2 (giờ). Đáp số: 2 giờ."
                )
                else -> lesson(
                    "Bài toán Năng suất làm việc chung",
                    "Coi toàn bộ công việc là 1 đơn vị:\n- Người thứ nhất làm xong một mình trong p giờ thì 1 giờ làm được 1/p công việc.\n- Người thứ hai làm xong một mình trong q giờ thì 1 giờ làm được 1/q công việc.\n- Trong 1 giờ cả hai người làm chung được: 1/p + 1/q (công việc).",
                    "Bình làm xong công việc trong 6 giờ, An làm xong trong 3 giờ. Trong 1 giờ cả hai bạn làm được mấy phần công việc?",
                    "Phần việc mỗi người làm trong 1 giờ" to "An làm 1/3 công việc; Bình làm 1/6 công việc.",
                    "Cộng hai phân số" to "1/3 + 1/6 = 2/6 + 1/6 = 3/6 = 1/2 (công việc)."
                )
            }

            // =====================================================================
            // DỰ PHÒNG AN TOÀN
            // =====================================================================
            else -> lesson(
                "Lý thuyết & Bài mẫu phương pháp giải",
                "Đọc kĩ đề bài, xác định các số liệu đã cho và yêu cầu của câu hỏi trước khi lựa chọn phép tính phù hợp.",
                "Ví dụ: Đặt tính rồi tính 12 + 15 = ?",
                "Đặt tính thẳng cột" to "Viết hàng đơn vị thẳng cột hàng đơn vị, hàng chục thẳng cột hàng chục.",
                "Tính từ phải sang trái" to "2 + 5 = 7; 1 + 1 = 2. Kết quả là 27."
            )
        }
    }

    /** Lý thuyết bốn phép tính phân số; chọn phép tính theo biểu thức dạng a/b ○ c/d trong đề. */
    private fun fractionOpsLesson(statement: String): FixedTheoryLesson {
        val op = FRACTION_OP.find(exprPart(statement))?.groupValues?.get(1) ?: "+"
        return when (op) {
            "-" -> lesson(
                "Trừ hai phân số khác mẫu số",
                "Muốn trừ hai phân số khác mẫu số, ta quy đồng mẫu số hai phân số đó, rồi lấy tử số của phân số thứ nhất trừ đi tử số của phân số thứ hai và giữ nguyên mẫu số chung (rút gọn kết quả về tối giản nếu có thể).",
                "Tính: 2/3 - 1/2 = ?",
                "Quy đồng mẫu số (MSC = 6)" to "2/3 = (2 × 2)/(3 × 2) = 4/6; 1/2 = (1 × 3)/(2 × 3) = 3/6.",
                "Trừ hai tử số và giữ nguyên mẫu số" to "4/6 - 3/6 = (4 - 3)/6 = 1/6."
            )
            "×" -> lesson(
                "Nhân hai phân số",
                "Muốn nhân hai phân số, ta lấy tử số nhân với tử số, mẫu số nhân với mẫu số, sau đó rút gọn phân số kết quả về tối giản.",
                "Tính: 2/3 × 4/5 = ?",
                "Nhân tử số với tử số, mẫu số với mẫu số" to "2/3 × 4/5 = (2 × 4)/(3 × 5) = 8/15."
            )
            ":" -> lesson(
                "Chia hai phân số",
                "Muốn chia một phân số cho một phân số khác 0, ta lấy phân số thứ nhất NHÂN với phân số thứ hai ĐẢO NGƯỢC (đổi tử số thành mẫu số, mẫu số thành tử số).",
                "Tính: 2/3 : 3/4 = ?",
                "Tìm phân số đảo ngược của 3/4" to "Phân số đảo ngược của 3/4 là 4/3.",
                "Nhân phân số thứ nhất với phân số đảo ngược" to "2/3 : 3/4 = 2/3 × 4/3 = (2 × 4)/(3 × 3) = 8/9."
            )
            else -> lesson(
                "Cộng hai phân số khác mẫu số",
                "Muốn cộng hai phân số khác mẫu số, ta quy đồng mẫu số hai phân số đó, rồi cộng hai tử số lại với nhau và giữ nguyên mẫu số chung (rút gọn kết quả về tối giản nếu có thể).",
                "Tính: 1/3 + 1/4 = ?",
                "Quy đồng mẫu số (MSC = 12)" to "1/3 = (1 × 4)/(3 × 4) = 4/12; 1/4 = (1 × 3)/(4 × 3) = 3/12.",
                "Cộng hai tử số và giữ nguyên mẫu số" to "4/12 + 3/12 = (4 + 3)/12 = 7/12."
            )
        }
    }
}