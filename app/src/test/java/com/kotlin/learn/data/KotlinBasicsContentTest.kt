package com.kotlin.learn.data

import com.kotlin.learn.data.content.KotlinBasics
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinBasicsContentTest {
    @Test
    fun `模块一有 14 课且 ID 连续`() {
        assertEquals(14, KotlinBasics.lessons.size)
        KotlinBasics.lessons.forEachIndexed { i, lesson ->
            assertEquals("kotlin-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块一全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(KotlinBasics.moduleOf(KotlinBasics.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
