# 界面美化 + Demo 乐园 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 全 App 换为"渐变活力"品牌风格（靛紫渐变、大圆角卡片、进度徽章），并新增"Demo 乐园"（计数器/待办列表/动画按钮 3 个可玩迷你示例，附讲解与课程锚点）。

**架构：** 主题层重写 Color/Theme（关闭 dynamicColor 以固定品牌色）；Demo 乐园为纯 Compose 状态的新 `ui/demos/` 包，通过 Routes 挂进现有 Navigation；各屏幕美化只改 UI 层，不动 data/ 与既有测试。

**技术栈：** 既有 Compose + Material 3 + Navigation Compose，零新依赖。

**规格：** `docs/superpowers/specs/2026-10-13-ui-polish-demo-land-design.md`

**全局约定：**
- 包名 `com.kotlin.learn`；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 每任务验证：`./gradlew.bat :app:assembleDebug` BUILD SUCCESSFUL + `./gradlew.bat :app:testDebugUnitTest` 22/22 不变（UI 无新单测，与既有"UI 人工验证"策略一致）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- UI 代码中颜色一律引用 `com.kotlin.learn.ui.theme` 的品牌色，不得写散落色值

---

### 任务 1：品牌主题重构（Color.kt / Theme.kt）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/theme/Color.kt`（整文件替换）
- 修改：`app/src/main/java/com/kotlin/learn/ui/theme/Theme.kt`（整文件替换）

- [ ] **步骤 1：整文件替换 `Color.kt`**

```kotlin
package com.kotlin.learn.ui.theme

import androidx.compose.ui.graphics.Color

// 品牌色：靛紫渐变系（风格 B）
val Indigo = Color(0xFF5B5BD6)
val Violet = Color(0xFF9A4DD6)
val IndigoContainer = Color(0xFFE8ECFF)
val VioletContainer = Color(0xFFF3E8FF)
val IndigoOn = Color(0xFF2A2A6E)
val VioletOn = Color(0xFF4A1E6E)

// 渐变背景与功能色
val GradStart = Color(0xFFE8ECFF)
val GradMid = Color(0xFFF7F2FF)
val GradEnd = Color(0xFFFFFFFF)
val WarmContainer = Color(0xFFFFF3E0)   // 🆚 C++ 对比卡淡暖底
val WarmOn = Color(0xFF6E4A1E)
val CodeBg = Color(0xFF1E1E2E)          // 代码块深底
val CodeFg = Color(0xFFD6D6F0)          // 代码块浅字
val Success = Color(0xFF3DDC97)         // 完成徽章
```

- [ ] **步骤 2：整文件替换 `Theme.kt`**

```kotlin
package com.kotlin.learn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOn,
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = VioletOn,
    tertiary = Success,
    background = GradEnd,
    surface = Color.White,
    surfaceVariant = IndigoContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBCC2FF),
    onPrimary = Color(0xFF1A1A4E),
    secondary = Color(0xFFE3B7FF),
    background = Color(0xFF121318),
    surface = Color(0xFF1C1D24),
)

@Composable
fun KotlinLearnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 品牌色是教学 App 的视觉标识，禁用系统动态取色（Android 12+ 默认开启会覆盖品牌色）
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

注意：删除模板遗留的 `Purple80/PurpleGrey40` 等旧色值与 `dynamicColorScheme`/`LocalContext`/`Build` 相关代码；`Color.kt` 中的旧色名无其他引用（全仓 grep `Purple80|PurpleGrey|Pink40|Pink80` 确认为 0 后再提交）。

- [ ] **步骤 3：验证**

运行：`./gradlew.bat :app:assembleDebug` 预期 BUILD SUCCESSFUL；`./gradlew.bat :app:testDebugUnitTest` 预期 22/22。

- [ ] **步骤 4：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui/theme
git commit -m "feat: 品牌主题重构为靛紫渐变系并关闭动态取色"
```

---

### 任务 2：Demo 骨架（目录数据 + 共享壳 + 列表页 + 路由 + 首页入口）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/DemoListScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（Routes + 两个目的地）
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/HomeScreen.kt`（渐变入口卡）

- [ ] **步骤 1：创建 `DemoCatalog.kt`（数据 + 共享页面壳 + 讲解卡）**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
            Spacer(Modifier.height(0.dp))
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
```

- [ ] **步骤 2：创建 `DemoListScreen.kt`**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo

@Composable
fun DemoListScreen(
    onDemoClick: (DemoItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
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
            Text("🎪 Demo 乐园", style = MaterialTheme.typography.headlineSmall)
        }
        HorizontalDivider()
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)) {
            items(DemoCatalog.items) { demo ->
                Card(
                    onClick = { onDemoClick(demo) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${demo.emoji} ${demo.title}", style = MaterialTheme.typography.titleLarge)
                        Text(
                            demo.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Indigo,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            text = demo.lessonIds.joinToString(" / ") { "compose 课" },
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **步骤 3：AppNav 加路由与目的地**

`Routes` object 追加：

```kotlin
const val DEMO_LAND = "demo-land"
const val DEMO = "demo/{demoId}"
fun demo(demoId: String) = "demo/$demoId"
```

NavHost 内（LESSON 目的地之后）追加两个目的地：

```kotlin
composable(Routes.DEMO_LAND) {
    DemoListScreen(
        onDemoClick = { demo -> navController.navigate(Routes.demo(demo.id)) },
        onBack = { navController.popBackStack() },
    )
}
composable(Routes.DEMO) { backStackEntry ->
    val demoId = backStackEntry.arguments?.getString("demoId")
    val item = DemoCatalog.find(demoId.orEmpty())
    val onLessonClick: (String) -> Unit = { lessonId ->
        navController.navigate(Routes.lesson(lessonId))
    }
    val onBack = { navController.popBackStack() }
    when (item?.id) {
        "counter" -> CounterDemo(item, onBack, onLessonClick)
        "todo" -> TodoDemo(item, onBack, onLessonClick)
        "animation" -> AnimationDemo(item, onBack, onLessonClick)
        else -> NotFoundScreen()
    }
}
```

导入：`com.kotlin.learn.ui.demos.DemoCatalog`、`DemoListScreen`、`CounterDemo`、`TodoDemo`、`AnimationDemo`（任务 3~5 创建前先不导入后三个——本任务先只接 DEMO_LAND 与 DEMO 的 when 骨架会导致编译失败，因此本任务 DEMO 目的地先只渲染 `NotFoundScreen()` 占位，when 分支在任务 3/4/5 各自完成时补上。本任务步骤 3 的最终形态：

```kotlin
composable(Routes.DEMO) { backStackEntry ->
    val demoId = backStackEntry.arguments?.getString("demoId")
    val item = DemoCatalog.find(demoId.orEmpty())
    if (item == null) NotFoundScreen() else DemoListPlaceholder(item)
}
```

临时 `DemoListPlaceholder` 放在 AppNav.kt 文件底部（私有 @Composable，仅渲染 item 标题文字），任务 3 创建 CounterDemo 时把 "counter" 分支换掉，任务 4/5 同理，任务 6 收尾时删除 Placeholder 与上述说明。）

- [ ] **步骤 4：HomeScreen 加渐变入口卡**

在 `HomeScreen` 的 `items(Curriculum.modules)` 块之后追加一个 item：

```kotlin
item {
    Card(
        onClick = onDemoLandClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(GradStart, GradMid))),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("🎪 Demo 乐园", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(
                "3 个可玩的迷你示例 · 边玩边学 Compose",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}
```

`HomeScreen` 签名追加参数 `onDemoLandClick: () -> Unit`；背景渐变：在 LazyColumn 外层（`modifier` 链）加 `.background(Brush.verticalGradient(listOf(GradStart, GradMid, GradEnd)))`。AppNav 的 HOME 目的地传 `onDemoLandClick = { navController.navigate(Routes.DEMO_LAND) }`。导入：`Brush`、`RoundedCornerShape`、主题色 `GradStart/GradMid/GradEnd`。入口卡的正式渐变视觉在任务 6（首页美化）统一打磨，本任务先做到"可点、可进"。

- [ ] **步骤 5：验证**

运行：`./gradlew.bat :app:assembleDebug` 预期 BUILD SUCCESSFUL；`./gradlew.bat :app:testDebugUnitTest` 预期 22/22。

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园骨架（目录/共享壳/列表页/路由/首页入口）"
```

---

### 任务 3：计数器 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/CounterDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（DEMO when 的 counter 分支）

- [ ] **步骤 1：创建 `CounterDemo.kt`**

```kotlin
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
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
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

    DemoPageShell(item = item, onBack = onBack, onLessonClick = onLessonClick, modifier = modifier) {
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
    }
}
```

- [ ] **步骤 2：AppNav 的 DEMO 目的地替换为 when 形态（counter 分支生效）**

DEMO 目的地改为：

```kotlin
composable(Routes.DEMO) { backStackEntry ->
    val demoId = backStackEntry.arguments?.getString("demoId")
    val item = DemoCatalog.find(demoId.orEmpty())
    val onLessonClick: (String) -> Unit = { navController.navigate(Routes.lesson(it)) }
    val onBack = { navController.popBackStack() }
    when (item?.id) {
        "counter" -> CounterDemo(item!!, onBack, onLessonClick)
        else -> NotFoundScreen()
    }
}
```

删除临时 `DemoListPlaceholder`（若其仍被引用则保留到任务 5 再删，以"编译通过"为准）。

- [ ] **步骤 3：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园计数器（状态与重组演示）"
```

---

### 任务 4：待办列表 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/TodoDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（when 的 todo 分支）

- [ ] **步骤 1：创建 `TodoDemo.kt`**

```kotlin
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

    DemoPageShell(item = item, onBack = onBack, onLessonClick = onLessonClick, modifier = modifier) {
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
    }
}
```

- [ ] **步骤 2：AppNav when 补 todo 分支**

`when (item?.id)` 增加一行：`"todo" -> TodoDemo(item!!, onBack, onLessonClick)`，导入 `TodoDemo`。

- [ ] **步骤 3：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园待办列表（LazyColumn 与状态演示）"
```

---

### 任务 5：动画按钮 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/AnimationDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（when 的 animation 分支，删除 Placeholder）

- [ ] **步骤 1：创建 `AnimationDemo.kt`**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.Violet

private enum class AnimKind(val label: String) { SCALE("缩放"), COLOR("颜色"), ALPHA("透明度") }

@Composable
fun AnimationDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(AnimKind.SCALE) }

    // 目标值三选一：只声明目标，中间帧由 animate*AsState 补齐
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.35f else 1f,
        animationSpec = tween(400),
        label = "scale",
    )
    val color by animateColorAsState(
        targetValue = if (pressed) Violet else Indigo,
        animationSpec = tween(400),
        label = "color",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.3f else 1f,
        animationSpec = tween(400),
        label = "alpha",
    )

    DemoPageShell(item = item, onBack = onBack, onLessonClick = onLessonClick, modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimKind.entries.forEach { k ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (kind == k) Violet else IndigoContainer)
                            .clickable { kind = k }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            k.label,
                            color = if (kind == k) Color.White else Indigo,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(140.dp)
                    .let { m ->
                        when (kind) {
                            AnimKind.SCALE -> m.graphicsLayer { scaleX = scale; scaleY = scale }
                            AnimKind.ALPHA -> m.graphicsLayer { this.alpha = alpha }
                            AnimKind.COLOR -> m
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (kind) {
                            AnimKind.COLOR -> color
                            else -> Indigo
                        }
                    )
                    .clickable { pressed = !pressed },
            ) {
                Text(
                    if (pressed) "松开我" else "点我",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                "当前动画：${kind.label} · 目标值 " + when (kind) {
                    AnimKind.SCALE -> if (pressed) "1.35" else "1.0"
                    AnimKind.COLOR -> if (pressed) "Violet" else "Indigo"
                    AnimKind.ALPHA -> if (pressed) "0.3" else "1.0"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
    }
}
```

注意需要导入 `androidx.compose.ui.graphics.graphicsLayer`。

- [ ] **步骤 2：AppNav when 补 animation 分支并删除 Placeholder**

`when (item?.id)` 增加一行：`"animation" -> AnimationDemo(item!!, onBack, onLessonClick)`；此时 `when` 覆盖全部三个 id，删除 AppNav.kt 底部的临时 `DemoListPlaceholder` 与 `DemoListScreen` 的占位说明（如仍存在）。

- [ ] **步骤 3：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园动画按钮（animate*AsState 演示）与骨架收尾"
```

---

### 任务 6：首页美化

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/HomeScreen.kt`

- [ ] **步骤 1：重写 HomeScreen 为渐变活力风格**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.TextStyle
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
```

签名新增 `onDemoLandClick: () -> Unit`（任务 2 已在 AppNav 传入，签名变化时确认 AppNav 编译通过）。

- [ ] **步骤 2：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui/screens/HomeScreen.kt
git commit -m "feat: 首页渐变活力风格（渐变标题/进度徽章/继续学习行/Demo 入口）"
```

---

### 任务 7：课程列表页美化

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonListScreen.kt`

- [ ] **步骤 1：列表行升级为序号圆底 + 完成徽章**

每课一行的 Row 内改造（保留既有 clickable/结构，替换序号 Text 与 ✅ Text 两处）：

```kotlin
// 序号圆底（替换原 "${index + 1}. ${lesson.title}" 的纯文本序号部分）
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
// 完成徽章（替换原 ✅/◻️ 文本）
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
```

顶部标题行下加渐变下划线装饰：

```kotlin
Box(
    modifier = Modifier
        .padding(start = 16.dp)
        .width(48.dp)
        .height(4.dp)
        .background(Brush.linearGradient(listOf(Indigo, Violet)), RoundedCornerShape(2.dp)),
)
```

导入：`CircleShape`、`Box`、`size`、`width`、`height`、`Brush`、主题色 `Indigo/IndigoContainer/Success/Violet`。

- [ ] **步骤 2：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui/screens/LessonListScreen.kt
git commit -m "feat: 课程列表渐变活力风格（序号圆底/完成徽章/标题装饰）"
```

---

### 任务 8：课文页美化

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`

- [ ] **步骤 1：四段卡分区着色 + 代码块深底**

修改 `LessonScreen` 与私有 `SectionCard`：

1. 📖 概念卡：`containerColor = MaterialTheme.colorScheme.primaryContainer`（即 IndigoContainer），contentColor 用 `IndigoOn`。
2. 💻 代码示例卡改为深底代码块（替换现有 codeExamples.forEach 内的 Card）：

```kotlin
Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = CodeBg),
    modifier = Modifier.fillMaxWidth(),
) {
    Column(Modifier.padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .size(8.dp)
                        .background(
                            listOf(Color(0xFFFF5F56), Color(0xFFFFBD2E), Color(0xFF27C93F))[i],
                            CircleShape,
                        ),
                )
            }
        }
        Text(
            example.description,
            style = MaterialTheme.typography.labelMedium,
            color = CodeFg.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 6.dp),
        )
        Text(
            example.code,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = CodeFg,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
```

3. 🆚 C++ 对比卡：`containerColor = WarmContainer`，`contentColor = WarmOn`。
4. ✅ 小测卡保持白底描边：`CardDefaults.cardColors(containerColor = Color.White)` + `border = BorderStroke(1.dp, IndigoContainer)`（Card 用 `border` 参数需 `Card(shape=..., colors=..., border=...)` 的重载）。

导入：`CodeBg/CodeFg/WarmContainer/WarmOn/Indigo/IndigoContainer/IndigoOn`、`CircleShape`、`BorderStroke`、`RoundedCornerShape`、`FontFamily`。

- [ ] **步骤 2：验证 + Commit**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（22/22）。

```bash
git add app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt
git commit -m "feat: 课文页分区着色与深底代码块"
```

---

### 任务 9：过渡动画 + 最终验证

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（内容淡入）

- [ ] **步骤 1：NavHost 加页面过渡**

`NavHost(...)` 调用追加过渡参数：

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.HOME,
    enterTransition = { fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 24 } },
    exitTransition = { fadeOut(tween(180)) },
    popEnterTransition = { fadeIn(tween(220)) },
    popExitTransition = { fadeOut(tween(180)) },
) {
```

导入：`androidx.compose.animation.core.tween`、`androidx.compose.animation.fadeIn`、`fadeOut`、`slideInVertically`。

- [ ] **步骤 2：LessonScreen 内容淡入（本身即动画教学素材）**

`LessonScreen` 的外层 Column 前加：

```kotlin
val appear = remember { Animatable(0f) }
LaunchedEffect(Unit) { appear.animateTo(1f, tween(300)) }
```

外层 Column 的 modifier 追加 `.graphicsLayer { alpha = appear.value }`。导入：`androidx.compose.animation.core.Animatable`、`androidx.compose.runtime.LaunchedEffect`、`androidx.compose.ui.graphics.graphicsLayer`。

- [ ] **步骤 3：全量验证**

```bash
./gradlew.bat :app:testDebugUnitTest   # 预期 22/22
./gradlew.bat :app:assembleDebug       # 预期 BUILD SUCCESSFUL
```

- [ ] **步骤 4：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 页面过渡动画与课文内容淡入"
```

---

## 自检记录

- **规格覆盖度**：主题色与 dynamicColor 关闭（任务 1）✓；首页（任务 2+6）✓；列表页（任务 7）✓；课文页四段着色与深底代码块（任务 8）✓；过渡与淡入动画（任务 9）✓；Demo 乐园入口/列表/三个 demo/讲解卡/课程锚点跳转（任务 2/3/4/5）✓；纯 Compose 状态、无新依赖、无 ViewModel ✓。
- **占位符扫描**：任务 2 步骤 3 的 `DemoListPlaceholder` 是**计划明确定义的临时编译桥**（有完整代码与明确的替换时间表：任务 3/4/5 逐分支替换、任务 5 全部删除），非占位符。
- **类型一致性**：`DemoItem(id/title/emoji/description/lessonIds/explanation)`、`DemoCatalog.find/items`、`DemoPageShell(item, onBack, onLessonClick, content)`、`CounterDemo/TodoDemo/AnimationDemo(item, onBack, onLessonClick)`、`Routes.demo(demoId)`、`HomeScreen(..., onDemoLandClick)` 各任务间签名一致；品牌色名 Indigo/Violet/GradStart/GradMid/GradEnd/IndigoContainer/IndigoOn/WarmContainer/WarmOn/CodeBg/CodeFg/Success 全部在任务 1 定义且后续任务只引用不重复定义。
