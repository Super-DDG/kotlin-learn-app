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
import com.kotlin.learn.ui.theme.CodeFg
import com.kotlin.learn.ui.theme.CodeComment
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
