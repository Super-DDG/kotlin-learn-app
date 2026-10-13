# 交互学习版（测验拆页 + Playground + Demo 反向入口）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 测验拆独立页（课文页专注学习）；模块一 14 课代码示例接入 JetBrains Kotlin Playground 在线运行；模块二/三 8 课课文页直达对应 Demo。

**架构：** 新增 `quiz/{lessonId}` 与 `playground/{lessonId}/{exampleIndex}` 两条路由；QuizSection 组件复用（判分逻辑零改动）；PlaygroundScreen 用 WebView 加载本地 HTML + 官方 kotlin-playground JS 组件，代码经 HTML 转义注入；课文页签名演进为四个回调（quiz/playground/demo）。

**技术栈：** 既有 Compose + WebView（android.webkit）+ 官方 kotlin-playground JS（CDN，运行时加载），仅新增 `INTERNET` 权限，无 Gradle 依赖。

**规格：** `docs/superpowers/specs/2026-10-13-interactive-learning-design.md`

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **44/44**（本迭代纯 UI 层，不变）
- 现状接口：`QuizSection(questions, onSubmit: (correct: Int, total: Int) -> Unit, modifier)`（内部含 submitted/result 状态与交卷按钮）；`LessonScreen(lesson, onSubmitQuiz, modifier)`；AppNav 的 lesson 目的地当前传 `onSubmitQuiz = { correct, total -> scope.launch { progressStore.recordQuizResult(lessonId!!, correct, total) } }`
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：测验拆页

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/QuizScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（Routes + quiz 目的地 + LessonScreen 调用点）
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（移除 QuizSection，改测验入口卡，签名演进）

- [ ] **步骤 1：创建 QuizScreen.kt**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.Lesson

@Composable
fun QuizScreen(
    lesson: Lesson,
    onBack: () -> Unit,
    onSubmitQuiz: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .androidx.compose.foundation.clickable(onClick = onBack)
                    .padding(8.dp),
            )
            Text("✅ 随堂测验 · ${lesson.title}", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "共 ${lesson.questions.size} 题 · 交卷即记完成，成绩取历史最好",
            style = MaterialTheme.typography.bodySmall,
        )
        QuizSection(questions = lesson.questions, onSubmit = onSubmitQuiz)
    }
}
```

注意：`clickable` 的正确导入是 `androidx.compose.foundation.clickable`——上面 Modifier 链里写全限定会编译失败，实现时用 import 形式：`import androidx.compose.foundation.clickable` 然后链中写 `.clickable(onClick = onBack)`。

- [ ] **步骤 2：Routes 追加**

```kotlin
const val QUIZ = "quiz/{lessonId}"
fun quiz(lessonId: String) = "quiz/$lessonId"
```

- [ ] **步骤 3：AppNav 追加 quiz 目的地（LESSON 目的地之后）**

```kotlin
composable(Routes.QUIZ) { backStackEntry ->
    val lessonId = backStackEntry.arguments?.getString("lessonId")
    val lesson = Curriculum.findLesson(lessonId.orEmpty())
    if (lesson == null) {
        NotFoundScreen()
    } else {
        QuizScreen(
            lesson = lesson,
            onBack = { navController.popBackStack() },
            onSubmitQuiz = { correct, total ->
                scope.launch { progressStore.recordQuizResult(lessonId!!, correct, total) }
            },
        )
    }
}
```

导入 `QuizScreen`。

- [ ] **步骤 4：LessonScreen 移除 QuizSection 并加测验入口卡**

1. 签名从 `(lesson, onSubmitQuiz, modifier)` 改为：

```kotlin
@Composable
fun LessonScreen(
    lesson: Lesson,
    onQuizClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

2. 删除底部 `QuizSection(questions = lesson.questions, onSubmit = onSubmitQuiz)` 调用，原位置改为：

```kotlin
        Card(
            onClick = onQuizClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("✅ 开始随堂测验", style = MaterialTheme.typography.titleMedium)
                Text(
                    "共 ${lesson.questions.size} 题 · 交卷即记完成，成绩取历史最好",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
```

3. 清理不再使用的 import（QuizSection 调用点消失，但 QuizSection.kt 文件保留——QuizScreen 在用）。

- [ ] **步骤 5：AppNav 的 LESSON 目的地改接新签名**

```kotlin
LessonScreen(
    lesson = lesson,
    onQuizClick = { navController.navigate(Routes.quiz(lesson.id)) },
)
```

- [ ] **步骤 6：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（44/44）。

- [ ] **步骤 7：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 测验拆独立页，课文页改为入口卡"
```

---

### 任务 2：Kotlin Playground 在线运行

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/PlaygroundScreen.kt`
- 修改：`app/src/main/AndroidManifest.xml`（INTERNET 权限）
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（Routes + playground 目的地）
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（代码卡加"试一试"，签名加 onPlaygroundClick）

- [ ] **步骤 1：Manifest 加权限（`<manifest>` 根元素下、`<application>` 之前）**

```xml
    <uses-permission android:name="android.permission.INTERNET" />
```

- [ ] **步骤 2：Routes 追加**

```kotlin
const val PLAYGROUND = "playground/{lessonId}/{exampleIndex}"
fun playground(lessonId: String, exampleIndex: Int) = "playground/$lessonId/$exampleIndex"
```

- [ ] **步骤 3：创建 PlaygroundScreen.kt**

```kotlin
package com.kotlin.learn.ui.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.webkit.WebView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kotlin.learn.ui.theme.CodeBg
import com.kotlin.learn.ui.theme.CodeFg

private fun escapeHtml(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

private fun playgroundHtml(code: String): String {
    val body = escapeHtml(code)
    return """
        <!DOCTYPE html>
        <html>
        <head>
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <script src="https://unpkg.com/@kotlinplayground/kotlin-playground@1.6.0"></script>
        <style>body{margin:0;padding:0;background:#1E1E2E;}</style>
        </head>
        <body>
        <div class="kotlin-code" data-theme="idea" data-autocomplete="false" data-highlight-only="false">$body</div>
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun PlaygroundScreen(
    code: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val connected = remember {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "←",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(8.dp),
            )
            Text("▶ 在线试一试", style = MaterialTheme.typography.titleLarge)
        }
        if (connected) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadDataWithBaseURL(
                            "https://play.kotlinlang.org",
                            playgroundHtml(code),
                            "text/html",
                            "utf-8",
                            null,
                        )
                    }
                },
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                Text("当前离线，在线运行需要网络连接。以下是本课示例的静态代码：", style = MaterialTheme.typography.bodyMedium)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .androidx.compose.foundation.background(CodeBg)
                        .padding(12.dp),
                ) {
                    Text(
                        code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                        color = CodeFg,
                    )
                }
            }
        }
    }
}
```

注意两点：① `LocalContext` 需导入 `androidx.compose.ui.platform.LocalContext`；② 离线分支的 `Modifier` 链中 `androidx.compose.foundation.background` 须改为 import 形式 `.background(CodeBg)`（照抄全限定会编译失败——实现时统一用 import）。`import androidx.compose.foundation.background` 需补入导入清单。

- [ ] **步骤 4：AppNav 追加 playground 目的地**

```kotlin
composable(Routes.PLAYGROUND) { backStackEntry ->
    val lessonId = backStackEntry.arguments?.getString("lessonId")
    val index = backStackEntry.arguments?.getString("exampleIndex")?.toIntOrNull()
    val example = Curriculum.findLesson(lessonId.orEmpty())?.codeExamples?.getOrNull(index ?: -1)
    if (example == null) {
        NotFoundScreen()
    } else {
        PlaygroundScreen(
            code = example.code,
            onBack = { navController.popBackStack() },
        )
    }
}
```

导入 `PlaygroundScreen`。

- [ ] **步骤 5：LessonScreen 代码卡加"试一试"**

签名追加参数：

```kotlin
@Composable
fun LessonScreen(
    lesson: Lesson,
    onQuizClick: () -> Unit,
    onPlaygroundClick: (exampleIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
)
```

`lesson.codeExamples` 的遍历改用 `forEachIndexed { exampleIndex, example -> ... }`；代码卡头部行（三圆点 Row）改为：

```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    // ……原有三个圆点 Box 保持不动……
    if (lesson.id.startsWith("kotlin-")) {
        Spacer(Modifier.weight(1f))
        Text(
            "▶ 在线试一试",
            style = MaterialTheme.typography.labelMedium,
            color = CodeFg,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onPlaygroundClick(exampleIndex) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
```

所需导入：`androidx.compose.foundation.layout.Spacer`、`androidx.compose.foundation.layout.weight`（RowScope 成员，无需 import weight）、`androidx.compose.ui.draw.clip`、`androidx.compose.foundation.clickable`（任务 1 已可能引入）。注意 `weight` 是 RowScope 成员扩展，**不要** import `androidx.compose.foundation.layout.weight`（无法解析）。

- [ ] **步骤 6：AppNav 的 LessonScreen 调用点追加回调**

```kotlin
LessonScreen(
    lesson = lesson,
    onQuizClick = { navController.navigate(Routes.quiz(lesson.id)) },
    onPlaygroundClick = { index -> navController.navigate(Routes.playground(lesson.id, index)) },
)
```

- [ ] **步骤 7：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（44/44）。

- [ ] **步骤 8：Commit**

```bash
git add app/src/main
git commit -m "feat: 模块一接入 Kotlin Playground 在线运行（WebView + 官方组件）"
```

---

### 任务 3：Demo 乐园反向入口

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（测验入口卡上方加 demo 按钮，签名加 onDemoClick）
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`

- [ ] **步骤 1：LessonScreen 签名追加 onDemoClick**

```kotlin
@Composable
fun LessonScreen(
    lesson: Lesson,
    onQuizClick: () -> Unit,
    onPlaygroundClick: (exampleIndex: Int) -> Unit,
    onDemoClick: (demoId: String) -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 2：测验入口卡上方插入 demo 按钮区**

在测验入口 Card 之前插入：

```kotlin
        val relatedDemos = DemoCatalog.items.filter { lesson.id in it.lessonIds }
        relatedDemos.forEach { demo ->
            Card(
                onClick = { onDemoClick(demo.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "🖥 去 Demo 乐园试玩：${demo.title}",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(14.dp),
                )
            }
        }
```

导入 `com.kotlin.learn.ui.demos.DemoCatalog`。模块一课程 `relatedDemos` 为空，不渲染任何按钮（Playground 是它们的入口）。

- [ ] **步骤 3：AppNav 的 LessonScreen 调用点追加回调**

```kotlin
LessonScreen(
    lesson = lesson,
    onQuizClick = { navController.navigate(Routes.quiz(lesson.id)) },
    onPlaygroundClick = { index -> navController.navigate(Routes.playground(lesson.id, index)) },
    onDemoClick = { demoId -> navController.navigate(Routes.demo(demoId)) },
)
```

- [ ] **步骤 4：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（44/44）。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 课文页直达 Demo 乐园反向入口"
```

---

### 任务 4：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest`（预期 44/44）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：Routes 含 HOME/MODULE/LESSON/QUIZ/PLAYGROUND/DEMO_LAND/DEMO 七条；Manifest 含 INTERNET 权限；LessonScreen 签名四回调齐全；QuizScreen 复用 QuizSection 且 QuizSection.kt 判分逻辑零改动（git diff 确认该文件未被本迭代触碰）

---

## 自检记录

- **规格覆盖度**：测验拆页（任务 1）✓；Playground 模块一限定 + INTERNET + 离线降级（任务 2）✓；Demo 反向入口 8 课（任务 3）✓；进度语义不变（recordQuizResult 移至 QuizScreen，交卷即记/最好成绩逻辑在 ProgressLogic 未动）✓；收尾验证（任务 4）✓。
- **占位符扫描**：无 TBD/TODO；所有代码步骤含完整代码。
- **类型一致性**：`QuizScreen(lesson, onBack, onSubmitQuiz, modifier)`、`PlaygroundScreen(code, onBack, modifier)`、`LessonScreen(lesson, onQuizClick, onPlaygroundClick, onDemoClick, modifier)` 跨任务签名一致（任务 1 定 onQuizClick → 任务 2 增 onPlaygroundClick → 任务 3 增 onDemoClick，每任务结束时均为可编译形态）；`Routes.quiz(lessonId)`/`Routes.playground(lessonId, exampleIndex)` 与 composable 占位符一致；QuizSection 签名与调用零改动。
