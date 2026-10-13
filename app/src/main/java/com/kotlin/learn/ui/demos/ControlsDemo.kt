package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.IndigoOn

@Composable
fun ControlsDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var soundOn by remember { mutableStateOf(true) }
    var brightness by remember { mutableStateOf(0.7f) }
    var selectedTags by remember { mutableStateOf(setOf("学习")) }
    val allTags = listOf("学习", "运动", "音乐")

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("音效", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = soundOn, onCheckedChange = { soundOn = it })
                }
                Text(
                    "预览卡透明度：%.0f%%".format(brightness * 100),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = brightness,
                    onValueChange = { brightness = it },
                    valueRange = 0.2f..1f,
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndigoContainer.copy(alpha = brightness)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        Text("预览卡", color = IndigoOn, style = MaterialTheme.typography.titleMedium)
                    }
                }
                Text("兴趣标签（多选）", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = tag in selectedTags,
                            onClick = {
                                selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag) },
                        )
                    }
                }
                Text(
                    text = "已选：" + selectedTags.joinToString("、").ifEmpty { "无" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Indigo,
                )
            }
        },
    )
}
