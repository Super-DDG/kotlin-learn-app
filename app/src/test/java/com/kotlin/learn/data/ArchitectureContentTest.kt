package com.kotlin.learn.data

import com.kotlin.learn.data.content.Architecture
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchitectureContentTest {
    @Test
    fun `模块六有 5 课且 ID 连续`() {
        assertEquals(5, Architecture.lessons.size)
        Architecture.lessons.forEachIndexed { i, lesson ->
            assertEquals("arch-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块六全部示例都有预期输出`() {
        Architecture.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
                assertTrue(
                    "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                    example.expectedOutput.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `模块六全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(Architecture.moduleOf(Architecture.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
