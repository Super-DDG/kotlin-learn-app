package com.kotlin.learn.data.markdown

/** 行内片段：纯文本 / 行内代码 / 加粗（加粗内可再嵌行内代码）。 */
sealed class InlineSpan {
    data class Plain(val text: String) : InlineSpan()
    data class InlineCode(val code: String) : InlineSpan()
    data class Bold(val spans: List<InlineSpan>) : InlineSpan()
}

/** 块级元素。 */
sealed class Block {
    data class Heading(val level: Int, val spans: List<InlineSpan>) : Block()
    data class Paragraph(val spans: List<InlineSpan>) : Block()
    data class BulletList(val items: List<List<InlineSpan>>) : Block()
    data class NumberList(val items: List<List<InlineSpan>>) : Block()
}

/**
 * 轻量 Markdown 解析器：只支持本项目课程所需的语法子集
 * （## / ### 小标题、`code` 行内代码、**加粗**、· / - 列表、1. 数字列表、空行分段）。
 * 解析不抛异常：未闭合的反引号/星号按字面文本渲染。
 */
object MarkdownParser {
    private val NUMBERED = Regex("""^\d+\.\s+(.*)$""")

    fun parse(text: String): List<Block> = buildList {
        val lines = text.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            when {
                line.isBlank() -> i++
                line.startsWith("### ") -> {
                    add(Block.Heading(3, parseInline(line.removePrefix("### ").trim())))
                    i++
                }
                line.startsWith("## ") -> {
                    add(Block.Heading(2, parseInline(line.removePrefix("## ").trim())))
                    i++
                }
                line.startsWith("· ") || line.startsWith("- ") -> {
                    val items = mutableListOf<List<InlineSpan>>()
                    while (i < lines.size && (lines[i].startsWith("· ") || lines[i].startsWith("- "))) {
                        items.add(parseInline(lines[i].substring(2).trim()))
                        i++
                    }
                    add(Block.BulletList(items))
                }
                NUMBERED.containsMatchIn(line) -> {
                    val items = mutableListOf<List<InlineSpan>>()
                    while (i < lines.size) {
                        val m = NUMBERED.find(lines[i]) ?: break
                        items.add(parseInline(m.groupValues[1].trim()))
                        i++
                    }
                    add(Block.NumberList(items))
                }
                else -> {
                    val para = mutableListOf<String>()
                    while (i < lines.size && lines[i].isNotBlank() && !isMarker(lines[i])) {
                        para.add(lines[i].trim())
                        i++
                    }
                    add(Block.Paragraph(parseInline(para.joinToString("\n"))))
                }
            }
        }
    }

    private fun isMarker(line: String): Boolean =
        line.startsWith("## ") || line.startsWith("### ") ||
            line.startsWith("· ") || line.startsWith("- ") ||
            NUMBERED.containsMatchIn(line)

    /** 行内解析：加粗起点若先于反引号则先解析加粗；否则先按反引号切分（保护行内代码内的星号），再在纯文本段内解析加粗。 */
    internal fun parseInline(text: String): List<InlineSpan> {
        val open = text.indexOf('`')
        val boldOpen = text.indexOf("**")
        if (open == -1 || (boldOpen != -1 && boldOpen < open)) return parseBold(text)
        val close = text.indexOf('`', open + 1)
        if (close == -1) return listOf(InlineSpan.Plain(text))
        return buildList {
            if (open > 0) addAll(parseInline(text.substring(0, open)))
            add(InlineSpan.InlineCode(text.substring(open + 1, close)))
            if (close + 1 < text.length) addAll(parseInline(text.substring(close + 1)))
        }
    }

    private fun parseBold(text: String): List<InlineSpan> {
        val open = text.indexOf("**")
        if (open == -1) return if (text.isEmpty()) emptyList() else listOf(InlineSpan.Plain(text))
        val close = text.indexOf("**", open + 2)
        if (close == -1) return listOf(InlineSpan.Plain(text))
        return buildList {
            if (open > 0) add(InlineSpan.Plain(text.substring(0, open)))
            add(InlineSpan.Bold(parseInline(text.substring(open + 2, close))))
            val rest = text.substring(close + 2)
            if (rest.isNotEmpty()) addAll(parseInline(rest))
        }
    }
}
