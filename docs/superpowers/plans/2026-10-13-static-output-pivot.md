# 静态输出修正（废弃在线 Playground）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 废弃在线 Playground（大陆网络不可用），改为完全离线的"代码 + 点击显示预期输出"：模型加 `expectedOutput` 字段，模块一 14 课示例人工核对补输出，代码卡加"▶ 运行示例"交互，移除 Playground 全套（页面/路由/权限）。

**架构：** `CodeExample` 加默认空串的 `expectedOutput`（模块二/三不填即不显示按钮，无需改动其内容）；守门测试新增模块一断言；LessonScreen 签名回退为 `(lesson, onQuizClick, onDemoClick, modifier)`。

**技术栈：** 既有 Compose，零新依赖，且**移除** INTERNET/ACCESS_NETWORK_STATE 权限（回归无网络权限的应用）。

**规格：** `docs/superpowers/specs/2026-10-13-interactive-learning-design.md`（v2 修订节）

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **44/44** + 任务 1 新增 1 条断言（并入既有测试方法）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：expectedOutput 字段 + 模块一输出内容（TDD）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/data/model/Lesson.kt`（CodeExample 加字段）
- 修改：`app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt`（新增断言）
- 修改：`app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt`（14 课示例补输出）

- [ ] **步骤 1：CodeExample 加字段（默认空串保持模块二/三零改动）**

```kotlin
/** 一段代码示例：说明文字 + 代码 + 预期输出（无 stdout 输出的示例留空串） */
data class CodeExample(
    val description: String,
    val code: String,
    val expectedOutput: String = "",
)
```

- [ ] **步骤 2：守门断言（先红）**

在 `KotlinBasicsContentTest` 中新增测试方法：

```kotlin
    @Test
    fun `模块一全部代码示例都有预期输出`() {
        KotlinBasics.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
            assertTrue(
                "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                example.expectedOutput.isNotBlank(),
            )
        }
    }
```

注意：上面代码块的括号配平以编译器为准——实现时确保方法完整闭合。

- [ ] **步骤 3：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinBasicsContentTest"`
预期：FAIL（`缺少预期输出`）

- [ ] **步骤 4：为模块一 14 课的全部代码示例填 `expectedOutput`**

撰写规则（逐条执行）：
1. **逐段人工推导**：通读示例代码，给出精确的 stdout 输出；多数示例的注释已含输出线索（如 kotlin-03 的 `// 10 7 4 1`、kotlin-11 的 `// 12：::multiply 函数引用也能传`），以注释为准但须核实其正确性——发现注释输出与代码实际行为不符时，以实际行为为准并修复注释（在报告中列出）
2. 多行输出用 `\n` 写在普通字符串中（如 `"10\n7\n4\n1"`）或 `${'$'}{...}` 不需要——直接用 `"第一行\n第二行"` 形式；无输出的示例（如纯声明类片段）填 `"(无输出)"` 之外的**真实行为描述**——若确无打印则输出留空并改守门断言？**不允许**：本批 14 课示例均含 println，全部应有真实输出
3. `expectedOutput` 作为命名参数追加在 `code = ...` 之后
4. 模块二/三（AndroidCore/ComposeBasics）不动——其 CodeExample 未传该参数，走默认空串

- [ ] **步骤 5：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest`
预期：44/44 + 新断言 = 45/45 全部 PASS

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt
git commit -m "feat: 代码示例模型加预期输出并补全模块一（14 课）"
```

---

### 任务 2：运行结果交互 + 移除 Playground 全套

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`（运行按钮/结果区 + 移除 onPlaygroundClick + 移除试一试）
- 删除：`app/src/main/java/com/kotlin/learn/ui/screens/PlaygroundScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（删 PLAYGROUND 路由与目的地、LessonScreen 调用点回退）
- 修改：`app/src/main/AndroidManifest.xml`（删除两条网络权限）

- [ ] **步骤 1：LessonScreen 签名回退**

```kotlin
@Composable
fun LessonScreen(
    lesson: Lesson,
    onQuizClick: () -> Unit,
    onDemoClick: (demoId: String) -> Unit,
    modifier: Modifier = Modifier,
)
```

- [ ] **步骤 2：代码卡改造（forEachIndexed 保留）**

1. 移除头部行的"▶ 在线试一试"（含 kotlin- 门控的整段）
2. 代码 Text 之后、Card 收尾之前，加入运行交互（仅当示例有输出时）：

```kotlin
                    if (example.expectedOutput.isNotBlank()) {
                        var showOutput by remember(example.expectedOutput) { mutableStateOf(false) }
                        Text(
                            if (showOutput) "▾ 运行结果" else "▶ 运行示例",
                            style = MaterialTheme.typography.labelMedium,
                            color = CodeFg,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showOutput = !showOutput }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                        if (showOutput) {
                            Text(
                                "运行结果",
                                style = MaterialTheme.typography.labelSmall,
                                color = CodeFg.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(
                                example.expectedOutput,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                ),
                                color = CodeFg,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
```

所需导入：`androidx.compose.runtime.getValue`、`androidx.compose.runtime.setValue`、`androidx.compose.runtime.mutableStateOf`、`androidx.compose.runtime.remember`、`androidx.compose.ui.text.font.FontFamily`（若此前无）。同时清理因移除 Playground 不再使用的 import（如 WebView 相关在 PlaygroundScreen 内、随文件删除）。

- [ ] **步骤 3：删除 PlaygroundScreen.kt 并清理 AppNav**

1. `rm app/src/main/java/com/kotlin/learn/ui/screens/PlaygroundScreen.kt`
2. AppNav：删除 `PLAYGROUND` 常量、`fun playground(...)`、整个 `composable(Routes.PLAYGROUND) { ... }` 目的地、`PlaygroundScreen` 导入
3. LessonScreen 调用点回退：

```kotlin
LessonScreen(
    lesson = lesson,
    onQuizClick = { navController.navigate(Routes.quiz(lesson.id)) },
    onDemoClick = { demoId -> navController.navigate(Routes.demo(demoId)) },
)
```

- [ ] **步骤 4：删除网络权限**

从 AndroidManifest.xml 删除 `<uses-permission android:name="android.permission.INTERNET" />` 与 `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />` 两行（应用回归零网络权限）。

- [ ] **步骤 5：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（45/45）。

- [ ] **步骤 6：Commit**

```bash
git add app/src/main
git commit -m "feat: 代码示例离线运行结果交互，移除在线 Playground 全套"
```

---

### 任务 3：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest --rerun`（预期 45/45）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：AppNav Routes 不再含 PLAYGROUND（共六条）；Manifest 无任何 uses-permission；PlaygroundScreen.kt 已删且全仓 grep 无引用；KotlinBasics 14 课示例 expectedOutput 全非空（守门测试已覆盖）

---

## 自检记录

- **规格覆盖度**：expectedOutput 字段（任务 1）✓；模块一输出内容（任务 1 步骤 4）✓；运行交互（任务 2 步骤 2）✓；Playground 全套移除（任务 2 步骤 3/4）✓；收尾（任务 3）✓；模块二/三零改动（默认参数 + 无网络权限回归）✓。
- **占位符扫描**：无 TBD/TODO；步骤 4 的输出推导为受控内容工作，有守门断言与审查验收。
- **类型一致性**：`CodeExample(description, code, expectedOutput = "")` 默认参数保证 AndroidCore/ComposeBasics 零改动编译；LessonScreen 最终签名 `(lesson, onQuizClick, onDemoClick, modifier)` 与 AppNav 调用一致；任务 2 完成后 Routes 无 PLAYGROUND。
