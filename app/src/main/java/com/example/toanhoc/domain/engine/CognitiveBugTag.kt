package com.example.toanhoc.domain.engine

/**
 * Primary Math Cognitive Misconception Bug Tags.
 * Encapsulates misconception category, tag code, and Socratic feedback explanation.
 */
enum class CognitiveBugTag(
    val tagCode: String,
    val titleVi: String,
    val socraticExplanationVi: String
) {
    ERR_PLACE_VALUE_ALIGNMENT(
        tagCode = "ERR_PLACE_VALUE_ALIGNMENT",
        titleVi = "Đặt lệch hàng đơn vị/chục/trăm",
        socraticExplanationVi = "Hãy kiểm tra lại vị trí các chữ số khi đặt tính dọc. Các chữ số ở cùng một hàng đơn vị, hàng chục phải nằm thẳng cột với nhau."
    ),

    ERR_FORGOT_CARRY_OR_BORROW(
        tagCode = "ERR_FORGOT_CARRY_OR_BORROW",
        titleVi = "Quên nhớ hoặc quên mượn",
        socraticExplanationVi = "Phép tính qua 10 yêu cầu cộng thêm 1 vào hàng tiếp theo hoặc bớt 1 ở hàng vừa mượn. Hãy chú ý ghi nhớ các chữ số nhớ."
    ),

    ERR_ORDER_OF_OPERATIONS(
        tagCode = "ERR_ORDER_OF_OPERATIONS",
        titleVi = "Sai thứ tự thực hiện phép tính",
        socraticExplanationVi = "Biểu thức toán học ưu tiên thực hiện phép nhân và phép chia trước. Các phép cộng và phép trừ thực hiện sau từ trái sang phải."
    ),

    ERR_FRACTION_ADD_BOTH_NUM_DEN(
        tagCode = "ERR_FRACTION_ADD_BOTH_NUM_DEN",
        titleVi = "Cộng cả tử số và mẫu số phân số",
        socraticExplanationVi = "Khi cộng hai phân số, tuyệt đối không cộng hai mẫu số với nhau. Cần quy đồng mẫu số rồi chỉ thực hiện cộng các tử số."
    ),

    ERR_FRACTION_SUB_BOTH_NUM_DEN(
        tagCode = "ERR_FRACTION_SUB_BOTH_NUM_DEN",
        titleVi = "Trừ cả tử số và mẫu số phân số",
        socraticExplanationVi = "Khi trừ hai phân số khác mẫu, tuyệt đối không lấy mẫu số trừ mẫu số. Cần quy đồng về cùng mẫu số chung rồi mới thực hiện trừ hai tử số."
    ),

    ERR_FENCEPOST_OFF_BY_ONE(
        tagCode = "ERR_FENCEPOST_OFF_BY_ONE",
        titleVi = "Nhầm lẫn giữa số khoảng cách và số cây",
        socraticExplanationVi = "Trồng cây trên đường thẳng hở có số cây nhiều hơn số khoảng cách đúng 1 cây. Hãy vẽ sơ đồ để xác định chính xác số khoảng cách."
    ),

    ERR_PERIMETER_AREA_CONFUSION(
        tagCode = "ERR_PERIMETER_AREA_CONFUSION",
        titleVi = "Nhầm lẫn giữa chu vi và diện tích",
        socraticExplanationVi = "Chu vi là tổng độ dài đường viền bao quanh hình. Diện tích là độ rộng của bề mặt bên trong hình."
    ),

    ERR_RELATIVE_SPEED_DIRECTION(
        tagCode = "ERR_RELATIVE_SPEED_DIRECTION",
        titleVi = "Nhầm lẫn tổng và hiệu vận tốc chuyển động",
        socraticExplanationVi = "Hai chuyển động ngược chiều đuổi nhau thì vận tốc triệt tiêu khoảng cách bằng tổng vận tốc. Chuyển động cùng chiều dùng hiệu vận tốc."
    );

    companion object {
        fun fromTagCode(code: String): CognitiveBugTag? {
            return entries.firstOrNull { it.tagCode.equals(code, ignoreCase = true) }
        }
    }
}
