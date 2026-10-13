package com.kotlin.learn.data

import com.kotlin.learn.data.content.AndroidCore
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCoreContentTest {
    @Test
    fun `模块二有 12 课且 ID 连续`() {
        assertEquals(12, AndroidCore.lessons.size)
        AndroidCore.lessons.forEachIndexed { i, lesson ->
            assertEquals("android-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块二全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(AndroidCore.moduleOf(AndroidCore.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
