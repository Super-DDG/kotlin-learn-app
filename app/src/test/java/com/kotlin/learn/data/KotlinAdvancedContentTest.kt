package com.kotlin.learn.data

import com.kotlin.learn.data.content.KotlinAdvanced
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinAdvancedContentTest {
    @Test
    fun `模块四有 13 课且 ID 连续`() {
        assertEquals(13, KotlinAdvanced.lessons.size)
        KotlinAdvanced.lessons.forEachIndexed { i, lesson ->
            assertEquals("adv-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块四全部示例都有预期输出`() {
        KotlinAdvanced.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
                assertTrue(
                    "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                    example.expectedOutput.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `模块四全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(KotlinAdvanced.moduleOf(KotlinAdvanced.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
