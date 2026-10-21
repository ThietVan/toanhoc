package com.example.toanhoc.ui.about

/**
 * Giấy phép của ứng dụng và ghi nguồn các thành phần bên thứ ba.
 * Ứng dụng là phần mềm mã nguồn mở theo GNU GPL phiên bản 3 hoặc mới hơn (xem tệp LICENSE và NOTICE.md).
 */
object AppLicense {
    /** Địa chỉ kho mã nguồn công khai. Đổi thành địa chỉ thật trước khi phát hành. */
    const val SOURCE_URL = "https://github.com/TAI-KHOAN/toanhoc"

    const val SUMMARY =
        "Toán Thông Thái là phần mềm mã nguồn mở, phát hành theo giấy phép GNU GPL phiên bản 3 (hoặc mới hơn). " +
            "Mọi người được dùng, sửa và chia sẻ lại ứng dụng theo đúng điều kiện của giấy phép. Ứng dụng được cung cấp miễn phí, không kèm bảo hành."

    /** Tên thành phần – giấy phép. */
    val CREDITS = listOf(
        "Giọng đọc Piper vi_VN-vais1000" to "MIT",
        "Dữ liệu giọng VAIS-1000 (Truong Do)" to "CC BY 4.0",
        "sherpa-onnx (bộ đọc trên máy)" to "Apache 2.0",
        "espeak-ng (đổi chữ sang âm)" to "GPL-3.0",
        "ONNX Runtime" to "MIT",
        "Phông chữ Nunito" to "SIL OFL 1.1",
        "AndroidX, Jetpack Compose, Material" to "Apache 2.0",
        "Kotlin, kotlinx.serialization, coroutines" to "Apache 2.0",
    )
}
