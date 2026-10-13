package com.kotlin.learn.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.logic.QuizScorer
import com.kotlin.learn.data.model.QuizQuestion
import com.kotlin.learn.ui.theme.IndigoContainer

@Composable
fun QuizSection(
    questions: List<QuizQuestion>,
    onSubmit: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selections = remember(questions) { mutableStateMapOf<Int, Int>() }
    var submitted by remember(questions) { mutableStateOf(false) }
    val result = remember(questions, submitted) {
        if (submitted) QuizScorer.score(questions, selections.toMap()) else null
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✅ 随堂小测", style = MaterialTheme.typography.titleLarge)
        questions.forEachIndexed { qi, q ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, IndigoContainer),
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("${qi + 1}. ${q.question}", style = MaterialTheme.typography.titleSmall)
                    q.options.forEachIndexed { oi, option ->
                        val selected = selections[qi] == oi
                        val isCorrect = oi == q.correctIndex
                        val containerColor = when {
                            !submitted -> if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                            selected -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            onClick = { if (!submitted) selections[qi] = oi },
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        ) {
                            Text(option, Modifier.padding(10.dp))
                        }
                    }
                    if (submitted) {
                        Text(
                            "解析：${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
        if (result != null) {
            Text(
                text = "成绩：${result.correctCount} / ${result.total}" +
                    if (result.allCorrect) " 🎉 全对！" else " 再看看解析，可以重新测试。",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!submitted) {
                Button(
                    onClick = {
                        submitted = true
                        onSubmit(
                            QuizScorer.score(questions, selections.toMap()).correctCount,
                            questions.size,
                        )
                    },
                    enabled = selections.size == questions.size,
                ) { Text("交卷") }
            } else {
                OutlinedButton(onClick = {
                    selections.clear()
                    submitted = false
                }) { Text("重新测试") }
            }
        }
    }
}
