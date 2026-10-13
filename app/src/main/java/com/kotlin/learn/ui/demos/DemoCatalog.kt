package com.kotlin.learn.ui.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.IndigoOn

/** 一个迷你 demo 的元数据。 */
data class DemoItem(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    /** 对应课程锚点（可多个，均可点击跳转）。 */
    val lessonIds: List<String>,
    /** 🔍 背后发生了什么：2~4 句中文讲解。 */
    val explanation: String,
)

/** Demo 目录：首批 3 个（counter/todo/animation），后续迭代再加。 */
object DemoCatalog {
    val items: List<DemoItem> = listOf(
        DemoItem(
            id = "counter",
            title = "计数器",
            emoji = "🔢",
            description = "点按钮改数字，看状态如何驱动界面",
            lessonIds = listOf("compose-04"),
            explanation = "点击按钮时 count 的值变化 → 记住它的 Composable 自动重组 → Text 显示新值。" +
                "你不需要手动刷新界面——这就是声明式 UI。没有 count 这个状态，按钮点击就什么都不会发生。",
        ),
        DemoItem(
            id = "todo",
            title = "待办列表",
            emoji = "✅",
            description = "添加、勾选、删除，列表随数据自动增减",
            lessonIds = listOf("compose-03", "compose-06"),
            explanation = "整个列表只是一份 List<TodoItem> 状态。增删改都只改数据，" +
                "LazyColumn 依据数据自动增减行项——列表 UI 是数据的一个函数，这就是你在这里看到的全部。",
        ),
        DemoItem(
            id = "animation",
            title = "动画按钮",
            emoji = "✨",
            description = "切换三种动画，感受 animate*AsState",
            lessonIds = listOf("compose-03", "compose-07"),
            explanation = "animateFloatAsState / animateColorAsState 让你只声明\"目标值\"" +
                "，中间的每一帧插值由库补齐。点一下盒子，目标值翻转，动画自动发生——没有手动计时器。",
        ),
        DemoItem(
            id = "dialogs",
            title = "对话框与弹层",
            emoji = "💬",
            description = "AlertDialog 确认框与 ModalBottomSheet 底部弹层",
            lessonIds = listOf("compose-11"),
            explanation = "对话框和底部弹层都是\"临时 UI\"：不在常规布局里占位，出现与否由一个布尔状态决定。" +
                "点遮罩关闭触发 onDismissRequest，你必须在那里把状态翻回 false——和计数器是同一个状态驱动思想。",
        ),
        DemoItem(
            id = "scaffold",
            title = "页面骨架",
            emoji = "🏗",
            description = "Scaffold + TopAppBar + TabRow + FAB + Snackbar",
            lessonIds = listOf("compose-12"),
            explanation = "Scaffold 把一个标准页面的\"骨架槽位\"（顶栏/悬浮按钮/提示条/内容区）分好，" +
                "你往槽里放组件即可，不必自己算布局。FAB 点击通过 SnackbarHostState 弹出提示——提示条也是状态驱动的。",
        ),
    )

    /** 按demoId 查找；无效 id 返回 null（UI 显示兜底页）。 */
    fun find(demoId: String): DemoItem? = items.firstOrNull { it.id == demoId }
}

/** demo 页共享壳：顶部（返回 + 标题 + 课程徽章）+ 内容槽 + 底部讲解卡。 */
@Composable
fun DemoPageShell(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBack)
                    .padding(8.dp),
            )
            Text("${item.emoji} ${item.title}", style = MaterialTheme.typography.titleLarge)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            item.lessonIds.forEach { lessonId ->
                val lesson = Curriculum.findLesson(lessonId)
                Card(
                    onClick = { onLessonClick(lessonId) },
                    colors = CardDefaults.cardColors(containerColor = IndigoContainer, contentColor = IndigoOn),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = lesson?.title ?: lessonId,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Column(Modifier.weight(1f)) { content() }
        Card(
            colors = CardDefaults.cardColors(containerColor = IndigoContainer, contentColor = IndigoOn),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("🔍 背后发生了什么", style = MaterialTheme.typography.titleSmall)
                Text(
                    item.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    item.lessonIds.forEach { lessonId ->
                        Text(
                            text = "→ 相关课：${Curriculum.findLesson(lessonId)?.title ?: lessonId}",
                            color = Indigo,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onLessonClick(lessonId) }
                                .padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}
