package com.kotlin.learn.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.ui.theme.CodeBg
import com.kotlin.learn.ui.theme.CodeFg
import com.kotlin.learn.ui.theme.IndigoOn
import com.kotlin.learn.ui.theme.WarmContainer
import com.kotlin.learn.ui.theme.WarmOn

@Composable
fun LessonScreen(
    lesson: Lesson,
    onSubmitQuiz: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        SectionCard(
            title = "📖 概念讲解",
            body = lesson.concept,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = IndigoOn,
        )
        lesson.codeExamples.forEach { example ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CodeBg),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
                    Text(
                        example.code,
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = CodeFg,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        SectionCard(
            title = "🆚 给 C++ 开发者",
            body = lesson.cppComparison,
            containerColor = WarmContainer,
            contentColor = WarmOn,
        )
        QuizSection(questions = lesson.questions, onSubmit = onSubmitQuiz)
    }
}

@Composable
private fun SectionCard(
    title: String,
    body: String,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = Color.Unspecified,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor.takeOrElse { contentColorFor(containerColor) },
        ),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
