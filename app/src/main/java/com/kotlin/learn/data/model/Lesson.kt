package com.kotlin.learn.data.model

/** 一道随堂选择题 */
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

/** 一段代码示例：说明文字 + 代码 */
data class CodeExample(
    val description: String,
    val code: String,
)

/** 一节课 */
data class Lesson(
    val id: String,
    val title: String,
    /** 📖 概念讲解正文 */
    val concept: String,
    /** 💻 代码示例（1~2 段） */
    val codeExamples: List<CodeExample>,
    /** 🆚 给 C++ 开发者：类比 + 陷阱提醒 */
    val cppComparison: String,
    /** ✅ 随堂小测（3~5 题） */
    val questions: List<QuizQuestion>,
)

/** 一个课程模块 */
data class CourseModule(
    val id: String,
    val title: String,
    val emoji: String,
    val lessons: List<Lesson>,
)
