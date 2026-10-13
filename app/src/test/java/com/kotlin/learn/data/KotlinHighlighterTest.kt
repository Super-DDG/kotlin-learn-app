package com.kotlin.learn.data

import com.kotlin.learn.data.markdown.KotlinHighlighter
import com.kotlin.learn.data.markdown.TokenType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinHighlighterTest {
    private fun types(code: String) = KotlinHighlighter.tokenize(code).map { it.type }
    private fun texts(code: String) = KotlinHighlighter.tokenize(code).map { it.text }

    @Test
    fun `关键字识别为 KEYWORD`() {
        assertEquals(listOf(TokenType.KEYWORD, TokenType.PLAIN, TokenType.KEYWORD), types("val count if"))
    }

    @Test
    fun `行注释整体为 COMMENT`() {
        assertEquals(listOf("// 这是注释"), texts("// 这是注释"))
        assertEquals(listOf(TokenType.COMMENT), types("// 这是注释"))
    }

    @Test
    fun `块注释整体为 COMMENT`() {
        assertEquals(listOf("/* 多行\n注释 */"), texts("/* 多行\n注释 */"))
        assertEquals(listOf(TokenType.COMMENT), types("/* 多行\n注释 */"))
    }

    @Test
    fun `字符串整体为 STRING`() {
        assertEquals(listOf("\"Hello Kotlin\""), texts("val s = \"Hello Kotlin\"".substringAfter("= ")))
        assertEquals(listOf(TokenType.STRING), types("\"Hello Kotlin\""))
    }

    @Test
    fun `数字与小数为 NUMBER`() {
        assertTrue(texts("x = 1.35").containsAll(listOf("1.35")))
        assertTrue(types("1.35").contains(TokenType.NUMBER))
    }

    @Test
    fun `注解为 ANNOTATION`() {
        assertEquals(listOf("@Composable"), texts("@Composable"))
        assertEquals(listOf(TokenType.ANNOTATION), types("@Composable"))
    }

    @Test
    fun `普通标识符与符号为 PLAIN 且不丢字符`() {
        val tokens = KotlinHighlighter.tokenize("total = count + 5")
        assertEquals("total = count + 5", tokens.joinToString("") { it.text })
        assertTrue(tokens.none { it.type == TokenType.KEYWORD })
    }

    @Test
    fun `混合代码覆盖完整不丢字`() {
        val code = "val name = \"Kotlin\" // 名字\nval big = name.length + 42"
        val tokens = KotlinHighlighter.tokenize(code)
        assertEquals(code, tokens.joinToString("") { it.text })
        assertTrue(tokens.any { it.type == TokenType.STRING })
        assertTrue(tokens.count { it.type == TokenType.KEYWORD } >= 2)
        assertTrue(tokens.any { it.type == TokenType.COMMENT })
        assertTrue(tokens.any { it.type == TokenType.NUMBER })
    }
}
