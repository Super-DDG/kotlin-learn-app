package com.kotlin.learn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.progress.UserProgress
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.Success
import com.kotlin.learn.ui.theme.Violet

@Composable
fun LessonListScreen(
    module: CourseModule,
    progress: UserProgress,
    onLessonClick: (Lesson) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            IconButton(onClick = onBack) { Text("←") }
            Text(
                text = "${module.emoji} ${module.title}",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(48.dp)
                .height(4.dp)
                .background(Brush.linearGradient(listOf(Indigo, Violet)), RoundedCornerShape(2.dp)),
        )
        HorizontalDivider()
        LazyColumn {
            itemsIndexed(module.lessons) { index, lesson ->
                val completed = lesson.id in progress.completedLessonIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLessonClick(lesson) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (completed) IndigoContainer else MaterialTheme.colorScheme.surfaceVariant,
                                CircleShape,
                            ),
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (completed) Indigo else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    )
                    if (completed) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(Success.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text("✓ 已完成", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1E7A52))
                        }
                    }
                }
            }
        }
    }
}
