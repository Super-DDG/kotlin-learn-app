package com.kotlin.learn.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.markdown.MarkdownParser
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.ui.demos.DemoCatalog
import com.kotlin.learn.ui.render.HighlightedCode
import com.kotlin.learn.ui.render.MarkdownText
import com.kotlin.learn.ui.theme.CodeBg
import com.kotlin.learn.ui.theme.CodeFg
import com.kotlin.learn.ui.theme.IndigoOn
import com.kotlin.learn.ui.theme.WarmContainer
import com.kotlin.learn.ui.theme.WarmOn

@Composable
fun LessonScreen(
    lesson: Lesson,
    onQuizClick: () -> Unit,
    onDemoClick: (demoId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(300)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(16.dp))
            .graphicsLayer { alpha = appear.value },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        SectionCard(
            title = "📖 概念讲解",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = IndigoOn,
        ) { MarkdownText(MarkdownParser.parse(lesson.concept)) }
        lesson.codeExamples.forEachIndexed { exampleIndex, example ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CodeBg),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(3) { i ->
                            Box(
                                Modifier
                                    .size(8.dp)
                                    .background(
                                        listOf(Color(0xFFFF5F56), Color(0xFFFFBD2E), Color(0xFF27C93F))[i],
                                        CircleShape,
                                    ),
                            )
                        }
                    }
                    Text(
                        example.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = CodeFg.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    HighlightedCode(
                        code = example.code,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    if (example.expectedOutput.isNotBlank()) {
                        var showOutput by remember(example.expectedOutput) { mutableStateOf(false) }
                        Text(
                            if (showOutput) "▾ 运行结果" else "▶ 运行示例",
                            style = MaterialTheme.typography.labelMedium,
                            color = CodeFg,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showOutput = !showOutput }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        if (showOutput) {
                            Text(
                                "运行结果",
                                style = MaterialTheme.typography.labelSmall,
                                color = CodeFg.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(
                                example.expectedOutput,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = CodeFg,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
            }
        }
        SectionCard(
            title = "🆚 给 C++ 开发者",
            containerColor = WarmContainer,
            contentColor = WarmOn,
        ) { MarkdownText(MarkdownParser.parse(lesson.cppComparison)) }
        val relatedDemos = DemoCatalog.items.filter { lesson.id in it.lessonIds }
        relatedDemos.forEach { demo ->
            Card(
                onClick = { onDemoClick(demo.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "🖥 去 Demo 乐园试玩：${demo.title}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
        Card(
            onClick = onQuizClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("✅ 开始随堂测验", style = MaterialTheme.typography.titleMedium)
                Text(
                    "共 ${lesson.questions.size} 题 · 交卷即记完成，成绩取历史最好",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

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
