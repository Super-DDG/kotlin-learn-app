# Markdown 排版引擎与课程正文重排 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 自研轻量 Markdown 渲染引擎 + Kotlin 语法高亮（零新依赖），并把 36 课正文全量重排为分节/列表/强调结构。

**架构：** 解析器与高亮器为纯 Kotlin 逻辑（`data/markdown/`，可 TDD）；渲染层 `ui/render/MarkdownText.kt` 将 Block 转为 AnnotatedString；LessonScreen 的 📖/🆚 卡接 MarkdownText、💻 代码卡接 HighlightedCode。内容重排只改 data/content 的字符串，守门规则不变。

**技术栈：** 既有 Kotlin + Compose，零新依赖。

**规格：** `docs/superpowers/specs/2026-10-13-markdown-typography-design.md`

**全局约定：**
- 包名 `com.kotlin.learn`；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 单测运行：`./gradlew.bat :app:testDebugUnitTest --tests "<类名>"`；全量基线 **22/22**（新增解析器/高亮器测试后基线上调）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- 高亮颜色一律引用 `ui/theme/Color.kt` 的主题色，不得散落

---

### 任务 1：MarkdownParser（TDD，纯逻辑）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/markdown/MarkdownParser.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/MarkdownParserTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
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
        assertEquals(listOf<Block>(Block.Paragraph(listOf(InlineSpan.Plain("第二段")))), blocks[1])
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
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.MarkdownParserTest"`
预期：编译失败（`MarkdownParser`/`Block`/`InlineSpan` 不存在）

- [ ] **步骤 3：编写实现**

```kotlin
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

    /** 行内解析：先按反引号切分（优先级最高，保护行内代码内的星号），再在纯文本段内解析加粗。 */
    internal fun parseInline(text: String): List<InlineSpan> {
        val open = text.indexOf('`')
        if (open == -1) return parseBold(text)
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
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.MarkdownParserTest"`
预期：12 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/markdown app/src/test/java/com/kotlin/learn/data/MarkdownParserTest.kt
git commit -m "feat: 轻量 Markdown 解析器（纯逻辑，TDD）"
```

---

### 任务 2：KotlinHighlighter（TDD，纯逻辑）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/markdown/KotlinHighlighter.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/KotlinHighlighterTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
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
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinHighlighterTest"`
预期：编译失败（`KotlinHighlighter`/`TokenType` 不存在）

- [ ] **步骤 3：编写实现**

```kotlin
package com.kotlin.learn.data.markdown

/** 代码 token 类型（渲染时映射到 VS Code Dark+ 语义的主题色）。 */
enum class TokenType { KEYWORD, STRING, NUMBER, COMMENT, ANNOTATION, PLAIN }

data class CodeToken(val text: String, val type: TokenType)

/** Kotlin 代码正则分词器：token 序列必须无损覆盖输入全文。 */
object KotlinHighlighter {
    private val KEYWORDS = setOf(
        "package", "import", "class", "object", "interface", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return", "break", "continue",
        "is", "in", "as", "true", "false", "null", "this", "super",
        "private", "public", "internal", "protected",
        "data", "sealed", "enum", "companion", "override", "open", "abstract",
        "suspend", "by", "typealias", "constructor", "init",
        "try", "catch", "finally", "throw",
    )

    // 备选顺序即优先级：注释 → 字符串 → 注解 → 数字（含小数）→ 标识符/关键字 → 单字符兜底
    private val TOKEN = Regex(
        "//[^\n]*" +
            "|/\\*[\\s\\S]*?\\*/" +
            "|\"[^\"\\n]*\"" +
            "|@[A-Za-z_]\\w*" +
            "|\\b\\d[\\d_]*(\\.\\d+)?\\b" +
            "|[A-Za-z_][A-Za-z0-9_]*" +
            "|[\\s\\S]",
    )

    fun tokenize(code: String): List<CodeToken> =
        TOKEN.findAll(code).map { m ->
            val t = m.value
            val type = when {
                t.startsWith("//") || t.startsWith("/*") -> TokenType.COMMENT
                t.startsWith("\"") -> TokenType.STRING
                t.startsWith("@") -> TokenType.ANNOTATION
                t[0].isDigit() -> TokenType.NUMBER
                t in KEYWORDS -> TokenType.KEYWORD
                else -> TokenType.PLAIN
            }
            CodeToken(t, type)
        }.toList()
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinHighlighterTest"`
预期：8 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/markdown app/src/test/java/com/kotlin/learn/data/KotlinHighlighterTest.kt
git commit -m "feat: Kotlin 语法高亮分词器（纯逻辑，TDD）"
```

---

### 任务 3：渲染层 + LessonScreen 接线

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/render/MarkdownText.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/theme/Color.kt`（追加 4 个高亮色）
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（📖/🆚 卡接 MarkdownText，💻 卡接 HighlightedCode）

- [ ] **步骤 1：Color.kt 追加高亮色（追加在文件末尾，已有色不动）**

```kotlin
// 代码高亮（VS Code Dark+ 语义）
val CodeKeyword = Color(0xFFC586C0)
val CodeString = Color(0xFFCE9178)
val CodeNumber = Color(0xFFB5CEA8)
val CodeComment = Color(0xFF6A9955)
```

- [ ] **步骤 2：创建 `ui/render/MarkdownText.kt`**

```kotlin
package com.kotlin.learn.ui.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlin.learn.data.markdown.Block
import com.kotlin.learn.data.markdown.InlineSpan
import com.kotlin.learn.data.markdown.KotlinHighlighter
import com.kotlin.learn.data.markdown.TokenType
import com.kotlin.learn.ui.theme.CodeBg
import com.kotlin.learn.ui.theme.CodeComment
import com.kotlin.learn.ui.theme.CodeFg
import com.kotlin.learn.ui.theme.CodeKeyword
import com.kotlin.learn.ui.theme.CodeNumber
import com.kotlin.learn.ui.theme.CodeString
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.IndigoOn

/** Markdown 块列表 → 纵向排版。行内代码/加粗/标题/列表全部生效。 */
@Composable
fun MarkdownText(
    blocks: List<Block>,
    modifier: Modifier = Modifier,
    baseStyle: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is Block.Heading -> Text(
                    text = annotated(block.spans),
                    style = baseStyle.copy(
                        fontSize = if (block.level == 2) 16.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Indigo,
                    ),
                )
                is Block.Paragraph -> Text(text = annotated(block.spans), style = baseStyle)
                is Block.BulletList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEach { item ->
                        Row {
                            Text(text = "•  ", style = baseStyle, color = Indigo)
                            Text(text = annotated(item), style = baseStyle)
                        }
                    }
                }
                is Block.NumberList -> Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    block.items.forEachIndexed { index, item ->
                        Row {
                            Text(text = "${index + 1}.  ", style = baseStyle, color = Indigo)
                            Text(text = annotated(item), style = baseStyle)
                        }
                    }
                }
            }
        }
    }
}

private fun annotated(spans: List<InlineSpan>): AnnotatedString = buildAnnotatedString {
    appendSpans(spans)
}

private fun AnnotatedString.Builder.appendSpans(spans: List<InlineSpan>, bold: Boolean = false) {
    spans.forEach { span ->
        when (span) {
            is InlineSpan.Plain -> withStyle(SpanStyle(fontWeight = if (bold) FontWeight.Bold else null)) {
                append(span.text)
            }
            is InlineSpan.InlineCode -> withStyle(
                SpanStyle(
                    background = IndigoContainer,
                    color = IndigoOn,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = if (bold) FontWeight.Bold else null,
                ),
            ) { append(span.code) }
            is InlineSpan.Bold -> appendSpans(span.spans, bold = true)
        }
    }
}

/** Kotlin 代码高亮渲染（深底代码卡内使用，字色映射主题高亮色）。 */
@Composable
fun HighlightedCode(
    code: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    val annotated = remember(code) { highlightAnnotated(code) }
    Text(
        text = annotated,
        style = style.copy(fontFamily = FontFamily.Monospace, color = CodeFg),
        modifier = modifier,
    )
}

fun highlightAnnotated(code: String): AnnotatedString = buildAnnotatedString {
    KotlinHighlighter.tokenize(code).forEach { token ->
        val color = when (token.type) {
            TokenType.KEYWORD, TokenType.ANNOTATION -> CodeKeyword
            TokenType.STRING -> CodeString
            TokenType.NUMBER -> CodeNumber
            TokenType.COMMENT -> CodeComment
            TokenType.PLAIN -> CodeFg
        }
        withStyle(SpanStyle(color = color)) { append(token.text) }
    }
}
```

`CodeBg` 导入在渲染高亮代码背景色时如未被直接使用则省略（背景色仍由 LessonScreen 的 Card containerColor = CodeBg 提供）。

- [ ] **步骤 3：LessonScreen 接线**

`LessonScreen.kt` 修改三处（保持对外签名与整体结构不变）：

1. 📖 概念卡与 🆚 对比卡：私有 `SectionCard` 的 `body: String` 参数改为 `content: @Composable () -> Unit`：

```kotlin
@Composable
private fun SectionCard(
    title: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    content: @Composable () -> Unit,
) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(Modifier.padding(top = 6.dp)) { content() }
        }
    }
}
```

调用点改为：

```kotlin
SectionCard(
    title = "📖 概念讲解",
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = IndigoOn,
) { MarkdownText(MarkdownParser.parse(lesson.concept)) }

SectionCard(
    title = "🆚 给 C++ 开发者",
    containerColor = WarmContainer,
    contentColor = WarmOn,
) { MarkdownText(MarkdownParser.parse(lesson.cppComparison)) }
```

2. 💻 代码卡内：原单色 `Text(example.code, ...)` 替换为 `HighlightedCode(code = example.code)`（Card 深底 `CodeBg`、三圆点装饰、说明文字保持不变）。

3. 导入：`com.kotlin.learn.data.markdown.MarkdownParser`、`com.kotlin.learn.ui.render.MarkdownText`、`com.kotlin.learn.ui.render.HighlightedCode`、主题色 `IndigoOn/WarmContainer/WarmOn`。

- [ ] **步骤 4：验证**

运行：`./gradlew.bat :app:testDebugUnitTest`（预期 22 + 12 + 8 = **42/42**）+ `./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui app/src/main/java/com/kotlin/learn/ui/theme/Color.kt
git commit -m "feat: Markdown 渲染层与课文页排版接线"
```

---

### 任务 4：模块一正文重排（14 课）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt`（仅 concept/cppComparison 字符串）

**重排规则（14 课统一执行）：**
1. **信息零丢失**：原正文的所有技术事实、反引号行内代码、数字必须保留，不得删减或改写技术内容
2. **结构**：1 句导语 → 2~3 个 `## ` 小节 → 枚举内容改为 `· ` 或 `- ` 列表 → 关键结论 `**加粗**`
3. **密度**：每段不超过 3 行；列表项一行一事
4. **cppComparison**：`·` 行保留原样（已是列表语法）；可补 `**` 强调，但不得改技术内容
5. **codeExamples 完全不动**
6. 守门规则不放宽：title/concept/cppComparison 非空、每课 3~5 题

**示例锚点（kotlin-01 的 concept 重排后的目标形态，其余 13 课照此风格）：**

```kotlin
concept = """
    Kotlin 与 C++ 相比，变量声明**更简单也更安全**。这一课讲最常用的声明方式。

    ## val 与 var

    用 `val` 声明**只读变量**（类似 C++ 的 `const`），用 `var` 声明可变变量。

    ## 数字类型一览

    - `Int`（32 位）、`Long`（64 位）
    - `Double`、`Float`
    - `Boolean`、`Char`

    ## 与 C++ 最大的差异

    基本类型**没有隐式窄化转换**：`Int` 不会自动变成 `Long`，必须显式调用 `.toLong()`。
    有 `UInt`、`ULong`、`UShort`、`UByte` 无符号类型（Kotlin 1.9 起 Stable），但日常开发绝大多数场景仍用有符号的 `Int`/`Long`。
""".trimIndent(),
```

- [ ] **步骤 1：按规则重排 kotlin-01 至 kotlin-14 的 concept 与 cppComparison**
- [ ] **步骤 2：验证**

运行：`./gradlew.bat :app:testDebugUnitTest`（42/42，守门测试确保结构与完整性）
预期：全部 PASS

- [ ] **步骤 3：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt
git commit -m "feat: 模块一课程正文 Markdown 重排（14 课）"
```

---

### 任务 5：模块二正文重排（12 课）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/data/content/AndroidCore.kt`（仅 concept/cppComparison 字符串）

- [ ] **步骤 1：按任务 4 的重排规则（逐字相同的 6 条规则）重排 android-01 至 android-12 的 concept 与 cppComparison**
- [ ] **步骤 2：验证**：`./gradlew.bat :app:testDebugUnitTest`（42/42）
- [ ] **步骤 3：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/AndroidCore.kt
git commit -m "feat: 模块二课程正文 Markdown 重排（12 课）"
```

---

### 任务 6：模块三正文重排（10 课）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/data/content/ComposeBasics.kt`（仅 concept/cppComparison 字符串）

- [ ] **步骤 1：按任务 4 的重排规则（逐字相同的 6 条规则）重排 compose-01 至 compose-10 的 concept 与 cppComparison**
- [ ] **步骤 2：验证**：`./gradlew.bat :app:testDebugUnitTest`（42/42）
- [ ] **步骤 3：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/ComposeBasics.kt
git commit -m "feat: 模块三课程正文 Markdown 重排（10 课）"
```

---

### 任务 7：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest`（预期 42/42）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：grep 确认三个内容文件中 `## ` 小节总数 ≥ 60（36 课 × 2 节为下限参考）；`**` 使用 > 0；无未转义破坏编译的裸 `$`（raw string 中若新增 `$` 插值必须显式 `${'$'}`）

---

## 自检记录

- **规格覆盖度**：语法子集（任务 1：标题/行内代码/加粗/三种列表/空行分段）✓；解析器与高亮器纯逻辑 TDD（任务 1/2）✓；高亮色入主题（任务 3 步骤 1）✓；渲染层 AnnotatedString 与 LessonScreen 三卡接线（任务 3）✓；36 课重排 + 质量把关 + 守门不放宽（任务 4/5/6）✓；未闭合兜底不抛异常（任务 1 实现 + 测试）✓；全量验证（任务 7）✓。
- **占位符扫描**：任务 4/5/6 的"按任务 4 的重排规则"不是占位符——规则在任务 4 逐条列出（6 条），且信息零丢失/守门测试/示例锚点完备。
- **类型一致性**：`Block`（Heading/Paragraph/BulletList/NumberList）、`InlineSpan`（Plain/InlineCode/Bold）、`MarkdownParser.parse(text): List<Block>`、`TokenType`（六值）、`CodeToken(text, type)`、`KotlinHighlighter.tokenize(code): List<CodeToken>`、`MarkdownText(blocks, modifier, baseStyle)`、`HighlightedCode(code, modifier, style)`、主题色 `CodeKeyword/CodeString/CodeNumber/CodeComment` 各任务间签名一致；任务 3 的 42/42 基线 = 22 既有 + 12 解析器 + 8 高亮器。
