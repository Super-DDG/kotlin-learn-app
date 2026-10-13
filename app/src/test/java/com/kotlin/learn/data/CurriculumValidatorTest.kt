package com.kotlin.learn.data

import com.kotlin.learn.data.logic.CurriculumValidator
import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumValidatorTest {
    private fun lesson(
        id: String = "m-01",
        concept: String = "正文",
        code: List<CodeExample> = listOf(CodeExample("说明", "val x = 1")),
        cpp: String = "对比",
        questions: List<QuizQuestion> = listOf(
            QuizQuestion("q", listOf("a", "b"), 0, "e")
        ),
    ) = Lesson(id, "标题", concept, code, cpp, questions)

    private fun module(id: String = "mod", lessons: List<Lesson>) =
        CourseModule(id, "模块", "📘", lessons)

    @Test
    fun `合法课程返回空问题列表`() {
        // 合法课时需满足 3~5 题；简报默认 helper 仅 1 题，会使"合法"用例误报"随堂小测至少 3 题"
        val validQuestions = listOf(
            QuizQuestion("q1", listOf("a", "b"), 0, "e"),
            QuizQuestion("q2", listOf("a", "b"), 1, "e"),
            QuizQuestion("q3", listOf("a", "b"), 0, "e"),
        )
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(questions = validQuestions))))
        )
        assertTrue(problems.isEmpty())
    }

    @Test
    fun `课时 ID 重复被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson("x"), lesson("x"))))
        )
        assertTrue(problems.any { it.contains("课时 ID 重复") && it.contains("x") })
    }

    @Test
    fun `空正文或空对比被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(concept = "  ", cpp = ""))))
        )
        assertEquals(2, problems.count { it.contains("不得为空") })
    }

    @Test
    fun `无代码示例或题目数量不足被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(code = emptyList(), questions = emptyList()))))
        )
        assertTrue(problems.any { it.contains("至少 1 段代码示例") })
        assertTrue(problems.any { it.contains("随堂小测至少 3 题") })
    }

    @Test
    fun `正确选项下标越界被检出`() {
        val bad = QuizQuestion("q", listOf("a", "b"), 5, "e")
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(questions = listOf(bad)))))
        )
        assertTrue(problems.any { it.contains("correctIndex 越界") })
    }

    @Test
    fun `模块 ID 重复被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module("m", listOf(lesson())), module("m", listOf(lesson("y"))))
        )
        assertTrue(problems.any { it.contains("模块 ID 重复") })
    }
}
