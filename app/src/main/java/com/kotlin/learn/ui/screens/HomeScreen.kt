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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.progress.UserProgress
import com.kotlin.learn.ui.theme.GradEnd
import com.kotlin.learn.ui.theme.GradMid
import com.kotlin.learn.ui.theme.GradStart
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.IndigoOn
import com.kotlin.learn.ui.theme.Violet

private val BrandGradient = Brush.linearGradient(listOf(Indigo, Violet))

@Composable
fun HomeScreen(
    progress: UserProgress,
    onModuleClick: (CourseModule) -> Unit,
    onDemoLandClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(GradStart, GradMid, GradEnd))),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text(
                    "Kotlin 学习",
                    style = MaterialTheme.typography.headlineMedium.copy(brush = BrandGradient),
                )
                Text(
                    text = "已学 ${progress.completedLessonIds.size} / ${Curriculum.totalLessonCount} 课",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
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
            val pct = if (module.lessons.isEmpty()) 0 else done * 100 / module.lessons.size
            val nextIndex = module.lessons.indexOfFirst { it.id !in progress.completedLessonIds }
            Card(
                onClick = { onModuleClick(module) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = IndigoOn,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("${module.emoji} ${module.title}", style = MaterialTheme.typography.titleLarge)
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            "$done / ${module.lessons.size} 课",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                            modifier = Modifier
                                .background(IndigoContainer, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text("$pct%", style = MaterialTheme.typography.labelMedium, color = Indigo)
                        }
                    }
                    Text(
                        text = if (nextIndex == -1) "🎉 全部完成" else "继续第 ${nextIndex + 1} 课 →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Indigo,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        item {
            Card(
                onClick = onDemoLandClick,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().background(BrandGradient, RoundedCornerShape(16.dp)),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🎪 Demo 乐园", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(
                        "3 个可玩的迷你示例 · 边玩边学 Compose",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
