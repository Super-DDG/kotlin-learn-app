package com.kotlin.learn.data

import com.kotlin.learn.data.markdown.Block
import com.kotlin.learn.data.markdown.InlineSpan
import com.kotlin.learn.data.markdown.MarkdownParser
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownParserTest {
    @Test
    fun `普通单段落为 Paragraph`() {
        val blocks = MarkdownParser.parse("一行普通文字")
        assertEquals(listOf<Block>(Block.Paragraph(listOf(InlineSpan.Plain("一行普通文字")))), blocks)
    }

    @Test
    fun `空行分隔为两个段落`() {
        val blocks = MarkdownParser.parse("第一段\n\n第二段")
        assertEquals(2, blocks.size)
        assertEquals(Block.Paragraph(listOf(InlineSpan.Plain("第二段"))), blocks[1])
    }

    @Test
    fun `行内代码解析为 InlineCode`() {
        val blocks = MarkdownParser.parse("用 `val` 声明变量")
        val spans = (blocks[0] as Block.Paragraph).spans
        assertEquals(
            listOf(InlineSpan.Plain("用 "), InlineSpan.InlineCode("val"), InlineSpan.Plain(" 声明变量")),
            spans,
        )
    }

    @Test
    fun `加粗解析为 Bold`() {
        val blocks = MarkdownParser.parse("这是 **重点** 内容")
        val spans = (blocks[0] as Block.Paragraph).spans
        assertEquals(
            listOf(InlineSpan.Plain("这是 "), InlineSpan.Bold(listOf(InlineSpan.Plain("重点"))), InlineSpan.Plain(" 内容")),
            spans,
        )
    }

    @Test
    fun `加粗内可含行内代码`() {
        val blocks = MarkdownParser.parse("**用 `val` 声明**")
        val spans = (blocks[0] as Block.Paragraph).spans
        assertEquals(
            listOf(InlineSpan.Bold(listOf(InlineSpan.Plain("用 "), InlineSpan.InlineCode("val"), InlineSpan.Plain(" 声明")))),
            spans,
        )
    }

    @Test
    fun `未闭合反引号整体按字面渲染`() {
        val blocks = MarkdownParser.parse("这里有 ` 个反引号")
        val spans = (blocks[0] as Block.Paragraph).spans
        assertEquals(listOf<InlineSpan>(InlineSpan.Plain("这里有 ` 个反引号")), spans)
    }

    @Test
    fun `未闭合加粗按字面渲染`() {
        val blocks = MarkdownParser.parse("两个 ** 星号")
        val spans = (blocks[0] as Block.Paragraph).spans
        assertEquals(listOf<InlineSpan>(InlineSpan.Plain("两个 ** 星号")), spans)
    }

    @Test
    fun `二级与三级标题`() {
        val blocks = MarkdownParser.parse("## 小节 A\n### 子节 B")
        assertEquals(listOf<Block>(
            Block.Heading(2, listOf(InlineSpan.Plain("小节 A"))),
            Block.Heading(3, listOf(InlineSpan.Plain("子节 B"))),
        ), blocks)
    }

    @Test
    fun `连续点号与短横线行合并为一个 BulletList 且去掉前缀`() {
        val blocks = MarkdownParser.parse("· 第一\n- 第二\n· 第三")
        assertEquals(listOf<Block>(Block.BulletList(listOf(
            listOf(InlineSpan.Plain("第一")),
            listOf(InlineSpan.Plain("第二")),
            listOf(InlineSpan.Plain("第三")),
        ))), blocks)
    }

    @Test
    fun `连续数字行合并为 NumberList 且去掉序号`() {
        val blocks = MarkdownParser.parse("1. 先做\n2. 后做\n10. 收尾")
        assertEquals(listOf<Block>(Block.NumberList(listOf(
            listOf(InlineSpan.Plain("先做")),
            listOf(InlineSpan.Plain("后做")),
            listOf(InlineSpan.Plain("收尾")),
        ))), blocks)
    }

    @Test
    fun `列表遇到普通段落即结束`() {
        val blocks = MarkdownParser.parse("· 甲\n结尾行")
        assertEquals(2, blocks.size)
        assertEquals(Block.BulletList(listOf(listOf(InlineSpan.Plain("甲")))), blocks[0])
        assertEquals(Block.Paragraph(listOf(InlineSpan.Plain("结尾行"))), blocks[1])
    }

    @Test
    fun `列表项内支持行内代码与加粗`() {
        val blocks = MarkdownParser.parse("· 用 `val` 是 **只读**")
        val items = (blocks[0] as Block.BulletList).items
        assertEquals(
            listOf(InlineSpan.Plain("用 "), InlineSpan.InlineCode("val"), InlineSpan.Plain(" 是 "), InlineSpan.Bold(listOf(InlineSpan.Plain("只读")))),
            items[0],
        )
    }
}
