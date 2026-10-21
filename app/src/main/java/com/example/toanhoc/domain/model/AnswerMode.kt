package com.example.toanhoc.domain.model

/**
 * Answer interaction modes for primary math problems.
 */
enum class AnswerMode {
    MULTIPLE_CHOICE_4,      // Trắc nghiệm 4 phương án A, B, C, D (Lớp 1-2 trọng tâm 80%, Lớp 3-5 60%)
    DIRECT_KEYPAD_INPUT,    // Điền đáp số trực tiếp qua AcademicMathKeypad
    STRUCTURED_ESSAY_STEP   // Tự luận có cấu trúc từng bước (Thẻ câu lời giải + Lập phép tính & kết quả qua Keypad)
}
