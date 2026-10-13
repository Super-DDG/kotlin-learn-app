package com.kotlin.learn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.Lesson

@Composable
fun QuizScreen(
    lesson: Lesson,
    onBack: () -> Unit,
    onSubmitQuiz: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(8.dp),
            )
            Text("✅ 随堂测验 · ${lesson.title}", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "共 ${lesson.questions.size} 题 · 交卷即记完成，成绩取历史最好",
            style = MaterialTheme.typography.bodySmall,
        )
        QuizSection(questions = lesson.questions, onSubmit = onSubmitQuiz)
    }
}
