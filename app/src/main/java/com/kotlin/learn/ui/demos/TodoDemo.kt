package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Violet

private data class TodoItem(val id: Int, val text: String, val done: Boolean)

@Composable
fun TodoDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 列表数据是唯一事实源：增删改只动这个列表，UI 自动跟随
    val todos = remember { mutableStateListOf<TodoItem>() }
    var input by remember { mutableStateOf("") }
    var nextId by remember { mutableIntStateOf(1) }

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("想做点什么？") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            todos.add(TodoItem(nextId, input.trim(), false))
                            nextId++
                            input = ""
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Violet),
                ) { Text("添加") }
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                items(todos, key = { it.id }) { todo ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(
                            checked = todo.done,
                            onCheckedChange = { checked ->
                                val index = todos.indexOfFirst { it.id == todo.id }
                                todos[index] = todo.copy(done = checked)
                            },
                        )
                        Text(
                            text = todo.text,
                            style = MaterialTheme.typography.bodyLarge,
                            textDecoration = if (todo.done) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { todos.removeAll { it.id == todo.id } }) {
                            Text("🗑", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            if (todos.isEmpty()) {
                Text(
                    "列表为空——添加一条试试，注意 UI 如何自动跟随数据变化。",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
        },
    )
}
