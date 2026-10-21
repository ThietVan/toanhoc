# Toán Thông Thái

Ứng dụng Android miễn phí giúp học sinh lớp 1–5 học toán theo chương trình *Kết nối tri thức với cuộc sống*:
luyện tập theo bài, toán tư duy, bài giảng có giọng đọc tiếng Việt chạy ngay trên máy (không cần mạng),
và khu vực cho bố mẹ theo dõi.

## Build

1. Mở thư mục này bằng Android Studio (bản mới, JDK 17+).
2. Bấm **Sync** rồi **Run**. Lần build đầu tiên cần mạng: Gradle tự tải thư viện `sherpa-onnx` và giọng đọc
   `vi_VN-vais1000` (khoảng 100 MB, chỉ một lần). Nếu không tải được, ứng dụng vẫn chạy bằng giọng Google trên máy.

Tệp khoá ký ứng dụng không nằm trong kho mã. Muốn phát hành bản của riêng bạn, hãy dùng khoá của bạn.

## Giấy phép

Mã nguồn phát hành theo **GNU GPL phiên bản 3 hoặc mới hơn** – xem [`LICENSE`](LICENSE).
Ghi nguồn các thành phần bên thứ ba (giọng đọc, thư viện, phông chữ) – xem [`NOTICE.md`](NOTICE.md).
