package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.progress.UserProgress

@Composable
fun HomeScreen(
    progress: UserProgress,
    onModuleClick: (CourseModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("Kotlin 学习", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "已学 ${progress.completedLessonIds.size} / ${Curriculum.totalLessonCount} 课",
                    style = MaterialTheme.typography.bodyMedium,
                )
                LinearProgressIndicator(
                    progress = {
                        if (Curriculum.totalLessonCount == 0) 0f
                        else progress.completedLessonIds.size.toFloat() / Curriculum.totalLessonCount
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
        items(Curriculum.modules) { module ->
            val done = module.lessons.count { it.id in progress.completedLessonIds }
            Card(onClick = { onModuleClick(module) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${module.emoji} ${module.title}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${done} / ${module.lessons.size} 课",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
