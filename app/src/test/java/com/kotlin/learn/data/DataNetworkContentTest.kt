package com.kotlin.learn.data

import com.kotlin.learn.data.content.DataNetwork
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DataNetworkContentTest {
    @Test
    fun `模块五有 7 课且 ID 连续`() {
        assertEquals(7, DataNetwork.lessons.size)
        DataNetwork.lessons.forEachIndexed { i, lesson ->
            assertEquals("data-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块五全部示例都有预期输出`() {
        DataNetwork.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
                assertTrue(
                    "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                    example.expectedOutput.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `模块五全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(DataNetwork.moduleOf(DataNetwork.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
