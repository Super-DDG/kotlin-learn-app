package com.kotlin.learn.data

import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumContentTest {
    @Test
    fun `全课程共 7 模块 71 课`() {
        assertEquals(7, Curriculum.modules.size)
        assertEquals(71, Curriculum.modules.sumOf { it.lessons.size })
        assertEquals(
            listOf(
                "kotlin-basics", "android-core", "compose-basics",
                "kotlin-advanced", "data-network", "architecture", "shipping",
            ),
            Curriculum.modules.map { it.id },
        )
    }

    @Test
    fun `课时 ID 全局唯一且全部通过校验`() {
        val problems = CurriculumValidator.validate(Curriculum.modules)
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun `按 ID 查询课时`() {
        val lesson = Curriculum.findLesson("compose-01")
        assertEquals("声明式 UI 思想", lesson?.title)
        assertEquals(null, Curriculum.findLesson("not-exist"))
    }
}
