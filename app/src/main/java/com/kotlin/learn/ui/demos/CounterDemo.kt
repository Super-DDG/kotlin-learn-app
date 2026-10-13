package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.IndigoOn
import com.kotlin.learn.ui.theme.Violet

@Composable
fun CounterDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 核心状态：count 变化时，读取它的 Text 自动重组——这正是本 demo 要演示的事
    var count by remember { mutableStateOf(0) }

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Monospace),
                color = IndigoOn,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 24.dp),
            ) {
                OutlinedButton(onClick = { count-- }, shape = RoundedCornerShape(14.dp)) { Text("−1") }
                Button(
                    onClick = { count++ },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Violet),
                ) { Text("+1") }
                OutlinedButton(onClick = { count = 0 }, shape = RoundedCornerShape(14.dp)) { Text("重置") }
            }
        }
        },
    )
}
