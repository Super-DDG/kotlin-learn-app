package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.Violet
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    var totalSeconds by remember { mutableIntStateOf(60) }
    var remaining by remember { mutableIntStateOf(60) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000)
            remaining -= 1
            if (remaining == 0) running = false
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { if (totalSeconds == 0) 0f else remaining.toFloat() / totalSeconds },
                modifier = Modifier.size(180.dp),
                color = Violet,
            )
            Text(
                "%02d:%02d".format(remaining / 60, remaining % 60),
                style = MaterialTheme.typography.displayMedium,
                color = Indigo,
            )
        }
        if (!running && remaining == 0) {
            Text("⏰ 时间到！", style = MaterialTheme.typography.titleMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { running = !running }, enabled = remaining > 0) {
                Text(if (running) "暂停" else "开始")
            }
            OutlinedButton(
                onClick = {
                    remaining = (remaining + 30).coerceAtMost(3599)
                    totalSeconds = remaining
                },
                enabled = !running,
            ) { Text("+30 秒") }
            OutlinedButton(onClick = {
                running = false
                remaining = 60
                totalSeconds = 60
            }) { Text("重置 1 分") }
        }
        Text("剩余 $remaining 秒 · 协程每秒挂起一次", style = MaterialTheme.typography.bodySmall)
    }
}
