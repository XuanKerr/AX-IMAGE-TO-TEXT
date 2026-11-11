AX IMAGE TO TEXT
================

Ứng dụng Android nhận dạng chữ trong ảnh (OCR), hỗ trợ tiếng Việt và tiếng Anh.
- Chọn ảnh từ thư viện hoặc chụp ảnh bằng camera.
- Nhận dạng chữ offline bằng mô hình ML Kit Latin được đóng gói trong APK.
- Có thể sửa nội dung nhận dạng, sao chép hoặc lưu thành TXT.

CÁCH TẠO APK BẰNG GITHUB ACTIONS
1. Tạo repository GitHub mới, ví dụ: AX-IMAGE-TO-TEXT.
   Không tải đè dự án AX-PERSONAL-PLANNER hiện có.
2. Giải nén toàn bộ thư mục AX_IMAGE_TO_TEXT.
3. Trên GitHub, mở repository mới → Add file → Upload files.
4. Tải lên toàn bộ nội dung bên trong thư mục AX_IMAGE_TO_TEXT (bao gồm app và .github).
5. Commit changes.
6. Mở tab Actions → Build AX IMAGE TO TEXT APK → Run workflow.
7. Khi chạy thành công, mở lần chạy đó và tải artifact AX-IMAGE-TO-TEXT-APK.
8. Giải nén artifact để lấy app-debug.apk, sau đó chuyển APK sang điện thoại và cài đặt.

LƯU Ý
- Nhận dạng được thực hiện trên thiết bị, không gửi ảnh lên máy chủ.
- Mô hình OCR được đóng gói trong APK nên APK lớn hơn một ứng dụng không có OCR.
- Độ chính xác phụ thuộc độ nét, ánh sáng, góc chụp và chất lượng ảnh.
- Bản debug APK dùng để cài đặt thử nghiệm cá nhân.
