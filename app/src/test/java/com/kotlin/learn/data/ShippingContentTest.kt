package com.kotlin.learn.data

import com.kotlin.learn.data.content.Shipping
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShippingContentTest {
    @Test
    fun `模块七有 5 课且 ID 连续`() {
        assertEquals(5, Shipping.lessons.size)
        Shipping.lessons.forEachIndexed { i, lesson ->
            assertEquals("ship-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块七全部示例都有预期输出`() {
        Shipping.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
                assertTrue(
                    "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                    example.expectedOutput.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `模块七全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(Shipping.moduleOf(Shipping.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
