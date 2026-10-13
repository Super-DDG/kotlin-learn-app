# Demo 乐园扩充（4 demo + 4 配套课）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** Demo 乐园新增 4 个可玩 demo（登录表单/对话框弹层/页面骨架/开关滑块）覆盖常见 Material 3 组件，并新增 4 节配套课程（36→40 课），锚点互相打通。

**架构：** 课程内容追加到 ComposeBasics（ID 顺延 compose-11..14，Markdown 排版风格——渲染引擎已生效）；demo 沿用现有模式（DemoCatalog 元数据 + 独立 DemoScreen + DemoPageShell 共享壳 + AppNav when 分支），纯 Compose 状态。

**技术栈：** 既有 Kotlin + Compose + Material 3，零新依赖。

**规格：** `docs/superpowers/specs/2026-10-13-demo-expansion-design.md`

**全局约定：**
- 包名 `com.kotlin.learn`；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **42/42**（任务 1 更新守门断言后仍为 42/42；demo 为纯 UI 不加单测）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- 新课正文用 **Markdown 排版风格**（`## ` 小节、`· `/`- ` 列表、`**加粗**`、`` `code` `` 行内代码——渲染引擎已上线）；raw string 内字面 `$` 写 `${'$'}`；数字列表序号后必须有空格；加粗内不放未闭合反引号
- demo 颜色引用 `ui/theme` 品牌色；Material3 实验性 API（TopAppBar/ModalBottomSheet）加 `@OptIn(ExperimentalMaterial3Api::class)`

---

### 任务 1：新增 4 节配套课 + 守门测试更新（TDD）

**文件：**
- 修改：`app/src/test/java/com/kotlin/learn/data/ComposeBasicsContentTest.kt`（10→14 课断言）
- 修改：`app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt`（36→40 课断言）
- 修改：`app/src/main/java/com/kotlin/learn/data/content/ComposeBasics.kt`（追加 compose-11..14）

- [ ] **步骤 1：更新守门测试（先红）**

`ComposeBasicsContentTest` 两处断言改为：

```kotlin
    @Test
    fun `模块三有 14 课且 ID 连续`() {
        assertEquals(14, ComposeBasics.lessons.size)
        ComposeBasics.lessons.forEachIndexed { i, lesson ->
            assertEquals("compose-%02d".format(i + 1), lesson.id)
        }
    }
```

`CurriculumContentTest` 的总数断言改为：

```kotlin
    @Test
    fun `全课程共 3 模块 40 课`() {
        assertEquals(3, Curriculum.modules.size)
        assertEquals(40, Curriculum.modules.sumOf { it.lessons.size })
        assertEquals(listOf("kotlin-basics", "android-core", "compose-basics"),
            Curriculum.modules.map { it.id })
    }
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ComposeBasicsContentTest" --tests "com.kotlin.learn.data.CurriculumContentTest"`
预期：FAIL（课程数不足 14/40）

- [ ] **步骤 3：追加 4 课（compose-11 全文示例逐字照抄，compose-12..14 按要求撰写）**

`ComposeBasics.lessons` 列表末尾追加。**compose-11 示例锚点（全文）：**

```kotlin
        Lesson(
            id = "compose-11",
            title = "对话框与底部弹层",
            concept = """
                有些 UI 是"临时"的：确认弹窗、分享面板。它们**不在常规布局里占位**，出现与否完全由状态决定。

                ## AlertDialog：确认对话框

                - `showDialog` 为 true 时才组合出对话框
                - `onDismissRequest` 处理点遮罩/返回键的关闭
                - `confirmButton` 与 `dismissButton` 放操作按钮

                ## ModalBottomSheet：底部弹层

                从屏幕底部滑出，支持拖拽关闭，常放一组操作列表。本质与对话框相同：**一个布尔状态控制显隐**。

                ## 与普通页面的区别

                对话框不进导航栈——它是叠加在当前页面上的一层，关闭后原页面状态原样保留。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "一个由状态控制的确认对话框：",
                    code = """
                        var showDialog by remember { mutableStateOf(false) }

                        Button(onClick = { showDialog = true }) { Text("删除") }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                title = { Text("删除笔记") },
                                text = { Text("删除后无法恢复，确定吗？") },
                                confirmButton = {
                                    TextButton(onClick = { showDialog = false }) { Text("删除") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) { Text("取消") }
                                },
                            )
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · C++ 桌面开发的模态对话框（如 Qt 的 `QDialog::exec()`）会**阻塞事件循环**；Compose 的对话框不阻塞，只是组合树里多了一层。
                · Qt 的对话框通常 new 一个对象、用完 delete；Compose 里没有对话框对象，只有 `showDialog` 这个状态。
                · 点遮罩关闭 ≈ C++ 里拒绝模态返回码，但这里是回调 `onDismissRequest`，你自己决定怎么更新状态。
                · 临时 UI 不进导航栈——类比 C++ 里"函数内的临时窗口"而非"新注册的窗口类"。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Compose 中控制对话框出现/消失的机制是？",
                    options = listOf(
                        "一个 remember 的布尔状态",
                        "调用 exec() 阻塞等待",
                        "全局对话框管理器",
                        "手动 addView/removeView",
                    ),
                    correctIndex = 0,
                    explanation = "对话框与计数器是同一个思想：状态变化驱动重组。showDialog = true 时 AlertDialog 进入组合，false 时移出。",
                ),
                QuizQuestion(
                    question = "用户点击对话框外的遮罩时，会触发哪个回调？",
                    options = listOf("confirmButton", "onDismissRequest", "dismissButton", "onClick"),
                    correctIndex = 1,
                    explanation = "onDismissRequest 在用户以\"非按钮方式\"（遮罩、返回键）关闭时调用，你必须在这里把状态翻回 false，否则对话框关不上。",
                ),
                QuizQuestion(
                    question = "关闭对话框后，被它遮挡的页面状态会怎样？",
                    options = listOf("重新初始化", "原样保留", "部分丢失", "取决于对话框类型"),
                    correctIndex = 1,
                    explanation = "对话框不进导航栈，只是叠加层；关闭后原组合树与状态原样保留。这与导航到新页面再返回不同。",
                ),
                QuizQuestion(
                    question = "下列哪个场景更适合 ModalBottomSheet 而不是 AlertDialog？",
                    options = listOf(
                        "删除前二次确认",
                        "展示一组可选操作（收藏/分享/重命名）",
                        "输入密码",
                        "显示错误提示",
                    ),
                    correctIndex = 1,
                    explanation = "底部弹层适合承载一组并列操作列表；AlertDialog 适合简短的确认/警示。内容多且需要滚动时弹层体验更好。",
                ),
            ),
        ),
```

**compose-12..14 撰写要求**（同上面示例的结构与 Markdown 排版风格）：

| id | 标题 | concept 覆盖点 | 代码示例主题 | C++ 类比角度 |
|---|---|---|---|---|
| compose-12 | 页面骨架 Scaffold | 脚手架思想；topBar/floatingActionButton/snackbarHost 槽位；Snackbar 与状态 | Scaffold + TopAppBar + FAB + SnackbarHost 组合 | Qt QMainWindow 的 menuBar/toolbar/statusBar 槽位布局 |
| compose-13 | 输入组件与表单 | onValueChange 单向数据流（状态是唯一事实源）；isError 与 supportingText 错误提示；KeyboardOptions 键盘类型；PasswordVisualTransformation | 登录表单（用户名/密码/错误提示） | C++ GUI 信号槽 vs 单向数据流；getState/setState 之别 |
| compose-14 | 开关控制组件 | Switch/Slider/FilterChip 的选中状态模型（布尔/浮点/集合成员）；Slider 的 valueRange；Chip 多选 = 集合增删 | Switch + Slider（联动透明度）+ FilterChip 多选 | C++ 位标志（bitmask）vs 集合状态；滑块值 vs 变量直改 |

每课 3~5 题、每题 2~4 个选项、解析讲清"为什么"；`·` 开头的 C++ 对比 3~5 条；概念讲解用 `## ` 分节 + 列表 + 加粗。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest`
预期：42/42 全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/ComposeBasics.kt app/src/test/java/com/kotlin/learn/data
git commit -m "feat: 模块三新增对话框/脚手架/表单/控制组件 4 课（36→40）"
```

---

### 任务 2：对话框与弹层 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/DialogDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`（items 追加）
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（when 补分支）

- [ ] **步骤 1：DemoCatalog.items 追加元数据（追加在 animation 之后）**

```kotlin
        DemoItem(
            id = "dialogs",
            title = "对话框与弹层",
            emoji = "💬",
            description = "AlertDialog 确认框与 ModalBottomSheet 底部弹层",
            lessonIds = listOf("compose-11"),
            explanation = "对话框和底部弹层都是\"临时 UI\"：不在常规布局里占位，出现与否由一个布尔状态决定。" +
                "点遮罩关闭触发 onDismissRequest，你必须在那里把状态翻回 false——和计数器是同一个状态驱动思想。",
        ),
```

- [ ] **步骤 2：创建 `DialogDemo.kt`**

```kotlin
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
```

- [ ] **步骤 3：AppNav when 补分支**

`when (item?.id)` 增加一行 `"dialogs" -> DialogDemo(item!!, onBack, onLessonClick)`，导入 `DialogDemo`。

- [ ] **步骤 4：验证**

`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）+ `./gradlew.bat :app:testDebugUnitTest`（42/42）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园对话框与底部弹层"
```

---

### 任务 3：页面骨架 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/ScaffoldDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`

- [ ] **步骤 1：DemoCatalog.items 追加**

```kotlin
        DemoItem(
            id = "scaffold",
            title = "页面骨架",
            emoji = "🏗",
            description = "Scaffold + TopAppBar + TabRow + FAB + Snackbar",
            lessonIds = listOf("compose-12"),
            explanation = "Scaffold 把一个标准页面的\"骨架槽位\"（顶栏/悬浮按钮/提示条/内容区）分好，" +
                "你往槽里放组件即可，不必自己算布局。FAB 点击通过 SnackbarHostState 弹出提示——提示条也是状态驱动的。",
        ),
```

- [ ] **步骤 2：创建 `ScaffoldDemo.kt`**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaffoldDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tabs = listOf("首页", "发现", "我的")

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
            Scaffold(
                topBar = { TopAppBar(title = { Text("组件页") }) },
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        scope.launch { snackbarHostState.showSnackbar("FAB 被点击了！") }
                    }) { Text("+", style = MaterialTheme.typography.titleLarge) }
                },
                modifier = Modifier.fillMaxSize(),
            ) { padding ->
                Column(Modifier.fillMaxSize().padding(padding)) {
                    TabRow(selectedTabIndex = tab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(selected = tab == index, onClick = { tab = index }, text = { Text(title) })
                        }
                    }
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "这是「${tabs[tab]}」标签的内容",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        },
    )
}
```

注意：内嵌 Scaffold 的 `fillMaxSize()` 是相对 DemoPageShell 内容槽（weight(1f)）的剩余空间，底部讲解卡仍在最下方可见。

- [ ] **步骤 3：AppNav when 补分支**

`when (item?.id)` 增加一行 `"scaffold" -> ScaffoldDemo(item!!, onBack, onLessonClick)`，导入 `ScaffoldDemo`。

- [ ] **步骤 4：验证**：`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（42/42）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园页面骨架（Scaffold/TopAppBar/TabRow/FAB/Snackbar）"
```

---

### 任务 4：登录表单 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/LoginFormDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`

- [ ] **步骤 1：DemoCatalog.items 追加**

```kotlin
        DemoItem(
            id = "login-form",
            title = "登录表单",
            emoji = "📝",
            description = "TextField/Checkbox/Button/Snackbar 表单全家桶",
            lessonIds = listOf("compose-13"),
            explanation = "输入框没有\"自己记住内容\"：text 状态是唯一事实源，onValueChange 把新值写回状态，" +
                "输入框只是状态的投影。校验在点击登录时集中做，错误也是一条状态——整个表单没有一处\"直接改 UI\"。",
        ),
```

- [ ] **步骤 2：创建 `LoginFormDemo.kt`**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LoginFormDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                SnackbarHost(snackbarHostState)
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; error = null },
                    label = { Text("用户名") },
                    singleLine = true,
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    label = { Text("密码") },
                    singleLine = true,
                    isError = error != null,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "🙈" else "👁")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("记住我", style = MaterialTheme.typography.bodyMedium)
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Button(
                    onClick = {
                        when {
                            username.isBlank() -> error = "用户名不能为空"
                            password.length < 6 -> error = "密码至少 6 位"
                            else -> scope.launch {
                                snackbarHostState.showSnackbar("欢迎回来，$username！")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("登录") }
            }
        },
    )
}
```

- [ ] **步骤 3：AppNav when 补分支**

`when (item?.id)` 增加一行 `"login-form" -> LoginFormDemo(item!!, onBack, onLessonClick)`，导入 `LoginFormDemo`。

- [ ] **步骤 4：验证**：`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（42/42）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园登录表单（输入组件与校验反馈）"
```

---

### 任务 5：开关与滑块 demo

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/demos/ControlsDemo.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`

- [ ] **步骤 1：DemoCatalog.items 追加**

```kotlin
        DemoItem(
            id = "controls",
            title = "开关与滑块",
            emoji = "🎛",
            description = "Switch/Slider/FilterChip 三种控制状态模型",
            lessonIds = listOf("compose-14"),
            explanation = "三种组件对应三种状态模型：Switch 是布尔、Slider 是区间内的浮点、" +
                "FilterChip 多选是集合的增删。注意预览卡的透明度直接由 Slider 的值算出——" +
                "数据一变，UI 自动跟着变，没有任何\"刷新\"代码。",
        ),
```

- [ ] **步骤 2：创建 `ControlsDemo.kt`**

```kotlin
package com.kotlin.learn.ui.demos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
    val selectedTags = remember { mutableStateListOf("学习") }
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
                                if (tag in selectedTags) selectedTags.remove(tag) else selectedTags.add(tag)
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
```

- [ ] **步骤 3：AppNav when 补分支**

`when (item?.id)` 增加一行 `"controls" -> ControlsDemo(item!!, onBack, onLessonClick)`，导入 `ControlsDemo`。此时 when 覆盖全部 7 个 id。

- [ ] **步骤 4：验证**：`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（42/42）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: Demo 乐园开关与滑块（Switch/Slider/FilterChip）"
```

---

### 任务 6：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest`（预期 42/42）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：`DemoCatalog.items` 恰 7 项且 id 唯一（counter/todo/animation/dialogs/scaffold/login-form/controls）；AppNav when 覆盖全部 7 个 id；`Curriculum.findLesson` 对 4 个新锚点（compose-11..14）均非空；ComposeBasics 14 课每课题数 3~5

---

## 自检记录

- **规格覆盖度**：4 demo（任务 2~5）✓；4 配套课 compose-11..14（任务 1）✓；组织方式沿用现有模式（catalog 追加 + when 分支 + DemoPageShell）✓；守门测试更新为 14/40 且不放宽（任务 1 步骤 1）✓；HomeScreen 动态计数无需改动（totalLessonCount 动态求值）✓；收尾验证（任务 6）✓。
- **占位符扫描**：compose-12..14 为"要求表 + 结构规则 + 同构示例锚点"的受控内容创作（与第一批课程计划同模式），有守门测试与任务 6 静态自查验收，非占位符。
- **类型一致性**：`DemoItem` 字段、`DemoPageShell(item, onBack, onLessonClick, content, modifier)`（content 具名传参——沿用任务 3 确立的修正模式）、`DemoScreen(item, onBack, onLessonClick, modifier)` 四个新签名与既有三个 demo 一致；新 lesson id compose-11..14 与锚点一致；`SnackbarHostState/showSnackbar` 用法跨任务一致。
