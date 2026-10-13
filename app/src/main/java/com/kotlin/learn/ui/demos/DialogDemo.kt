package com.kotlin.learn.ui.demos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp),
                    ) {
                        Text(
                            if (deleted) "云端笔记（已删除 🗑）" else "云端笔记 · 1 条待删项",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Button(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("删除笔记（弹确认对话框）")
                }
                OutlinedButton(onClick = { showSheet = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("更多操作（弹底部层）")
                }
            }
        },
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除笔记") },
            text = { Text("删除后无法恢复，确定吗？") },
            confirmButton = {
                TextButton(onClick = {
                    deleted = true
                    showDeleteDialog = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            listOf("⭐ 收藏", "🔗 分享", "✏️ 重命名").forEach { action ->
                ListItem(
                    headlineContent = { Text(action) },
                    modifier = Modifier.clickable { showSheet = false },
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
