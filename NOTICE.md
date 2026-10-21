# Ghi nguồn và giấy phép

**Toán Thông Thái** – ứng dụng học toán lớp 1–5 theo chương trình *Kết nối tri thức với cuộc sống*.
Copyright (C) 2026 Nhóm phát triển Toán Thông Thái

Chương trình này là phần mềm tự do: bạn có thể phân phối lại và/hoặc sửa đổi nó theo các điều khoản
của Giấy phép Công cộng GNU (GNU General Public License) do Quỹ Phần mềm Tự do công bố, phiên bản 3
hoặc (tuỳ bạn chọn) bất kỳ phiên bản nào mới hơn. Chương trình được phân phối với hy vọng sẽ hữu ích
nhưng KHÔNG KÈM BẤT KỲ BẢO HÀNH NÀO. Xem toàn văn giấy phép trong tệp `LICENSE`.

> This program is free software: you can redistribute it and/or modify it under the terms of the
> GNU General Public License as published by the Free Software Foundation, either version 3 of the
> License, or (at your option) any later version. This program is distributed in the hope that it
> will be useful, but WITHOUT ANY WARRANTY. See `LICENSE` for details.

## Thành phần bên thứ ba

| Thành phần | Dùng để | Giấy phép | Nguồn |
|---|---|---|---|
| Piper voice `vi_VN-vais1000-medium` | Giọng đọc tiếng Việt | MIT | https://huggingface.co/rhasspy/piper-voices |
| VAIS-1000 Vietnamese Speech Synthesis Corpus – Truong Do | Dữ liệu huấn luyện giọng đọc | CC BY 4.0 | https://ieee-dataport.org/documents/vais-1000-vietnamese-speech-synthesis-corpus |
| sherpa-onnx (k2-fsa) | Chạy giọng đọc trên máy | Apache 2.0 | https://github.com/k2-fsa/sherpa-onnx |
| espeak-ng | Đổi chữ sang âm vị | GPL-3.0 | https://github.com/espeak-ng/espeak-ng |
| ONNX Runtime | Chạy mô hình | MIT | https://github.com/microsoft/onnxruntime |
| Nunito (Vernon Adams và cộng sự) | Phông chữ | SIL Open Font License 1.1 | https://github.com/googlefonts/nunito |
| AndroidX, Jetpack Compose, Material Components | Giao diện | Apache 2.0 | https://developer.android.com/jetpack |
| Kotlin, kotlinx.serialization, kotlinx.coroutines | Ngôn ngữ, thư viện | Apache 2.0 | https://kotlinlang.org |
| Room | Lưu dữ liệu trên máy | Apache 2.0 | https://developer.android.com/jetpack/androidx/releases/room |

Ghi chú về giọng đọc: giọng `vi_VN-vais1000` được tinh chỉnh từ giọng tiếng Anh `en_US-lessac` của dự án Piper.

Hiệu ứng âm thanh `app/src/main/res/raw/sfx_*.ogg` do nhóm phát triển tự tạo và phát hành cùng giấy phép GPL-3.0 với ứng dụng.

Nội dung bài học và câu hỏi do nhóm phát triển tự biên soạn theo khung chương trình; tên bài theo sách
*Kết nối tri thức với cuộc sống* chỉ dùng để đối chiếu chương trình. Ứng dụng không liên kết với nhà xuất bản.
