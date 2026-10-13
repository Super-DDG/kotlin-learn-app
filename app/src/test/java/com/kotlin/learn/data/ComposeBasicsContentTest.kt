package com.kotlin.learn.data

import com.kotlin.learn.data.content.ComposeBasics
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeBasicsContentTest {
    @Test
    fun `模块三有 14 课且 ID 连续`() {
        assertEquals(14, ComposeBasics.lessons.size)
        ComposeBasics.lessons.forEachIndexed { i, lesson ->
            assertEquals("compose-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块三全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(ComposeBasics.moduleOf(ComposeBasics.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
