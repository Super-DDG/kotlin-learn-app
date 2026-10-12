# Kotlin & Android 学习 App 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在现有模板工程上构建交互式课程 App：3 大模块 36 课中文课程（每课含讲解/代码示例/C++ 对比/小测）+ DataStore 进度追踪。

**架构：** 单 Activity + Navigation Compose；课程内容为静态 Kotlin 数据（data class）；进度用 DataStore Preferences，纯逻辑部分抽出为可单测的对象。

**技术栈：** Kotlin 2.2.10、Jetpack Compose（BOM 2026.02.01）、Material 3、Navigation Compose、DataStore Preferences、JUnit 4。

**规格：** `docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md`

**全局约定：**
- 包名 `com.kotlin.learn`；Windows + Git Bash 环境，gradle 命令用 `./gradlew.bat`
- 单元测试运行：`./gradlew.bat :app:testDebugUnitTest --tests "<类名>"`；构建验证：`./gradlew.bat :app:assembleDebug`
- 所有课程内容为中文；commit message 用中文 Conventional Commits，结尾加 `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- 已完成判定：**交卷即记完成**（不限全对），成绩记录历史最好（答对题数/总题数）

---

### 任务 1：添加依赖（Navigation Compose、DataStore）

**文件：**
- 修改：`gradle/libs.versions.toml`
- 修改：`app/build.gradle.kts`

- [ ] **步骤 1：在 `libs.versions.toml` 的 `[versions]` 段追加两行**

```toml
navigationCompose = "2.9.0"
datastore = "1.1.7"
```

- [ ] **步骤 2：在 `[libraries]` 段追加**

```toml
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
```

- [ ] **步骤 3：在 `app/build.gradle.kts` 的 `dependencies` 块追加**

```kotlin
implementation(libs.androidx.navigation.compose)
implementation(libs.androidx.datastore.preferences)
```

- [ ] **步骤 4：验证构建通过**

运行：`./gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat: 添加 Navigation Compose 与 DataStore 依赖"
```

---

### 任务 2：数据模型 + 测验判分逻辑（TDD）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/model/Lesson.kt`
- 创建：`app/src/main/java/com/kotlin/learn/data/logic/QuizScorer.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/QuizScorerTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.logic.QuizScorer
import com.kotlin.learn.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuizScorerTest {
    private val questions = listOf(
        QuizQuestion("q1", listOf("a", "b"), 0, "e1"),
        QuizQuestion("q2", listOf("a", "b", "c"), 2, "e2"),
        QuizQuestion("q3", listOf("a", "b"), 1, "e3"),
    )

    @Test
    fun `全对时 allCorrect 为 true`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0, 1 to 2, 2 to 1))
        assertEquals(3, r.correctCount)
        assertEquals(3, r.total)
        assertTrue(r.allCorrect)
    }

    @Test
    fun `部分答对计正确数`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0, 1 to 0, 2 to 1))
        assertEquals(2, r.correctCount)
        assertFalse(r.allCorrect)
    }

    @Test
    fun `未作答视为答错`() {
        val r = QuizScorer.score(questions, mapOf(0 to 0))
        assertEquals(1, r.correctCount)
        assertEquals(3, r.total)
    }

    @Test
    fun `全部未答得零分`() {
        val r = QuizScorer.score(questions, emptyMap())
        assertEquals(0, r.correctCount)
        assertFalse(r.allCorrect)
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.QuizScorerTest"`
预期：编译失败（`QuizScorer`、`QuizQuestion` 不存在）

- [ ] **步骤 3：编写数据模型 `Lesson.kt`**

```kotlin
package com.kotlin.learn.data.model

/** 一道随堂选择题 */
data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)

/** 一段代码示例：说明文字 + 代码 */
data class CodeExample(
    val description: String,
    val code: String,
)

/** 一节课 */
data class Lesson(
    val id: String,
    val title: String,
    /** 📖 概念讲解正文 */
    val concept: String,
    /** 💻 代码示例（1~2 段） */
    val codeExamples: List<CodeExample>,
    /** 🆚 给 C++ 开发者：类比 + 陷阱提醒 */
    val cppComparison: String,
    /** ✅ 随堂小测（3~5 题） */
    val questions: List<QuizQuestion>,
)

/** 一个课程模块 */
data class CourseModule(
    val id: String,
    val title: String,
    val emoji: String,
    val lessons: List<Lesson>,
)
```

- [ ] **步骤 4：编写最少实现 `QuizScorer.kt`**

```kotlin
package com.kotlin.learn.data.logic

import com.kotlin.learn.data.model.QuizQuestion

data class QuizResult(val correctCount: Int, val total: Int) {
    val allCorrect: Boolean get() = total > 0 && correctCount == total
}

object QuizScorer {
    /**
     * 判分。answers：题目下标 -> 用户选择的选项下标；未作答的题按答错计。
     */
    fun score(questions: List<QuizQuestion>, answers: Map<Int, Int>): QuizResult {
        var correct = 0
        questions.forEachIndexed { index, q ->
            if (answers[index] == q.correctIndex) correct++
        }
        return QuizResult(correct, questions.size)
    }
}
```

- [ ] **步骤 5：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.QuizScorerTest"`
预期：4 个测试全部 PASS

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data app/src/test/java/com/kotlin/learn/data
git commit -m "feat: 课程数据模型与测验判分逻辑"
```

---

### 任务 3：课程数据完整性校验器（TDD）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/logic/CurriculumValidator.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/CurriculumValidatorTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.logic.CurriculumValidator
import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumValidatorTest {
    private fun lesson(
        id: String = "m-01",
        concept: String = "正文",
        code: List<CodeExample> = listOf(CodeExample("说明", "val x = 1")),
        cpp: String = "对比",
        questions: List<QuizQuestion> = listOf(
            QuizQuestion("q", listOf("a", "b"), 0, "e")
        ),
    ) = Lesson(id, "标题", concept, code, cpp, questions)

    private fun module(id: String = "mod", lessons: List<Lesson>) =
        CourseModule(id, "模块", "📘", lessons)

    @Test
    fun `合法课程返回空问题列表`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson())))
        )
        assertTrue(problems.isEmpty())
    }

    @Test
    fun `课时 ID 重复被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson("x"), lesson("x"))))
        )
        assertTrue(problems.any { it.contains("课时 ID 重复") && it.contains("x") })
    }

    @Test
    fun `空正文或空对比被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(concept = "  ", cpp = ""))))
        )
        assertEquals(2, problems.count { it.contains("不得为空") })
    }

    @Test
    fun `无代码示例或题目数量不足被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(code = emptyList(), questions = emptyList()))))
        )
        assertTrue(problems.any { it.contains("至少 1 段代码示例") })
        assertTrue(problems.any { it.contains("随堂小测至少 3 题") })
    }

    @Test
    fun `正确选项下标越界被检出`() {
        val bad = QuizQuestion("q", listOf("a", "b"), 5, "e")
        val problems = CurriculumValidator.validate(
            listOf(module(lessons = listOf(lesson(questions = listOf(bad)))))
        )
        assertTrue(problems.any { it.contains("correctIndex 越界") })
    }

    @Test
    fun `模块 ID 重复被检出`() {
        val problems = CurriculumValidator.validate(
            listOf(module("m", listOf(lesson())), module("m", listOf(lesson("y"))))
        )
        assertTrue(problems.any { it.contains("模块 ID 重复") })
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumValidatorTest"`
预期：编译失败（`CurriculumValidator` 不存在）

- [ ] **步骤 3：编写实现**

```kotlin
package com.kotlin.learn.data.logic

import com.kotlin.learn.data.model.CourseModule

/**
 * 课程数据完整性校验：返回问题描述列表，空列表表示通过。
 * 规则：模块/课时 ID 全局唯一；正文、对比、解析非空；每课 1~2 段代码示例；
 * 每课 3~5 题、每题 2~4 个选项、correctIndex 在范围内。
 */
object CurriculumValidator {
    fun validate(modules: List<CourseModule>): List<String> = buildList {
        val moduleIds = mutableSetOf<String>()
        val lessonIds = mutableSetOf<String>()
        modules.forEach { m ->
            if (!moduleIds.add(m.id)) add("模块 ID 重复：${m.id}")
            if (m.lessons.isEmpty()) add("模块 ${m.id} 没有任何课时")
            m.lessons.forEach { l ->
                if (!lessonIds.add(l.id)) add("课时 ID 重复：${l.id}")
                if (l.title.isBlank()) add("课时 ${l.id} 标题不得为空")
                if (l.concept.isBlank()) add("课时 ${l.id} 概念正文不得为空")
                if (l.cppComparison.isBlank()) add("课时 ${l.id} C++ 对比不得为空")
                if (l.codeExamples.isEmpty()) add("课时 ${l.id} 至少 1 段代码示例")
                if (l.codeExamples.size > 2) add("课时 ${l.id} 代码示例最多 2 段")
                if (l.questions.size < 3) add("课时 ${l.id} 随堂小测至少 3 题")
                if (l.questions.size > 5) add("课时 ${l.id} 随堂小测最多 5 题")
                l.questions.forEachIndexed { qi, q ->
                    if (q.question.isBlank()) add("课时 ${l.id} 第 ${qi + 1} 题题干不得为空")
                    if (q.options.size < 2 || q.options.size > 4) {
                        add("课时 ${l.id} 第 ${qi + 1} 题选项须 2~4 个")
                    }
                    if (q.correctIndex !in q.options.indices) {
                        add("课时 ${l.id} 第 ${qi + 1} 题 correctIndex 越界")
                    }
                    if (q.explanation.isBlank()) add("课时 ${l.id} 第 ${qi + 1} 题解析不得为空")
                }
            }
        }
    }
}
```

注意：`空正文或空对比被检出` 测试断言 `problems.count { it.contains("不得为空") } == 2`——`concept = "  "` 与 `cpp = ""` 各产出一行"不得为空"，其余字段合法。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumValidatorTest"`
预期：6 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/logic/CurriculumValidator.kt app/src/test/java/com/kotlin/learn/data/CurriculumValidatorTest.kt
git commit -m "feat: 课程数据完整性校验器"
```

---

### 任务 4：模块一课程数据 KotlinBasics（14 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt`

**课时清单（id 前缀 `kotlin-`，顺序即列表顺序）：**

| # | id | 标题 |
|---|----|------|
| 1 | kotlin-01 | 变量与基本类型 |
| 2 | kotlin-02 | 字符串与字符串模板 |
| 3 | kotlin-03 | 条件与循环 |
| 4 | kotlin-04 | 函数与默认参数 |
| 5 | kotlin-05 | 空安全 |
| 6 | kotlin-06 | 类与继承 |
| 7 | kotlin-07 | data class |
| 8 | kotlin-08 | 密封类 |
| 9 | kotlin-09 | object 与伴生对象 |
| 10 | kotlin-10 | 扩展函数 |
| 11 | kotlin-11 | Lambda 与高阶函数 |
| 12 | kotlin-12 | 集合操作 |
| 13 | kotlin-13 | 区间与解构 |
| 14 | kotlin-14 | 异常处理 |

- [ ] **步骤 1：编写失败的测试（验证 14 课齐全且通过校验器）**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.content.KotlinBasics
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinBasicsContentTest {
    @Test
    fun `模块一有 14 课且 ID 连续`() {
        assertEquals(14, KotlinBasics.lessons.size)
        KotlinBasics.lessons.forEachIndexed { i, lesson ->
            assertEquals("kotlin-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块一全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(KotlinBasics.moduleOf(KotlinBasics.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinBasicsContentTest"`
预期：编译失败（`KotlinBasics` 不存在）

- [ ] **步骤 3：编写课程数据（示例课全文如下，其余 13 课按同一结构撰写）**

```kotlin
package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块一：Kotlin 语言基础（14 课）。课程数据即学习材料，撰写时保持代码可运行。 */
object KotlinBasics {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "kotlin-01",
            title = "变量与基本类型",
            concept = """
                Kotlin 用 `val` 声明只读变量（类似 C++ 的 `const`），用 `var` 声明可变变量。
                类型写在变量名后面，很多情况下编译器可以自动推断，省略不写。

                基本类型没有 C++ 那样的隐式窄化转换：`Int` 不会自动变成 `Long`，必须显式调用 `.toLong()`。
                数字类型：`Int`（32 位）、`Long`（64 位）、`Double`、`Float`、`Boolean`、`Char`。
                没有 `unsigned` 类型。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "val 与 var、类型推断与显式转换：",
                    code = """
                        val count = 10          // 推断为 Int，之后不能重新赋值
                        var total = 0           // var 可以重新赋值
                        total = count + 5

                        val big: Long = count.toLong()   // 显式转换，count.toLong() 不能省
                        val name: String = "Kotlin"      // 类型也可以显式标注

                        // count = 20  // 编译错误：val 不能重新赋值
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · `val` ≈ C++ 的 `const` 局部变量；`var` 是普通变量。
                · Kotlin 的 `Int` 是对象（装箱发生在需要时），不像 C++ 的 `int` 是裸值；但运行时性能接近。
                · 最大的坑：没有隐式窄化。C++ 里 `long x = intVar;` 合法，Kotlin 必须写 `intVar.toLong()`。
                · 也没有 `auto` 关键字——不写类型时默认就是自动推断。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "下列哪行代码能通过编译？",
                    options = listOf(
                        "val a: Long = 42",
                        "val a: Long = 42.toInt()",
                        "val a = 42; val b: Long = a",
                        "val a: Int = 42; val b: Long = a",
                    ),
                    correctIndex = 0,
                    explanation = "字面量 42 可以直接赋给 Long；但 Int 变量赋给 Long 必须显式 a.toLong()，Kotlin 没有隐式窄化转换。",
                ),
                QuizQuestion(
                    question = "val 声明的变量相当于 C++ 中的？",
                    options = listOf("普通变量", "const 变量", "指针", "引用"),
                    correctIndex = 1,
                    explanation = "val 是只读引用，初始化后不能重新赋值，类似 C++ 的 const。注意：val 引用的对象本身如果是可变类型，内容仍可变。",
                ),
                QuizQuestion(
                    question = "Kotlin 中存在哪种整数类型？",
                    options = listOf("unsigned int", "uint8_t", "Int", "size_t"),
                    correctIndex = 2,
                    explanation = "Kotlin 只有有符号整数类型：Byte、Short、Int、Long，没有无符号类型（实验性的 UInt 不用于常规开发）。",
                ),
            ),
        ),
        // …… 其余 13 课：kotlin-02 至 kotlin-14，按上方课时清单逐课撰写。
        // 每课必须包含：concept（📖 3~6 句中文讲解）、1~2 段 CodeExample（代码可运行、逐行注释）、
        // cppComparison（🆚 3~5 条 C++ 类比/陷阱，用 · 列表）、questions（3~5 题，含解析）。
        // 撰写完成后必须通过 CurriculumValidator 校验（本任务步骤 1 的测试即守门测试）。
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "kotlin-basics",
        title = "Kotlin 语言基础",
        emoji = "🅺",
        lessons = lessons,
    )
}
```

撰写要求（其余 13 课）：每个概念从 C++ 程序员视角切入；空安全一课（kotlin-05）是重点课，正文需覆盖 `?` 类型、`?.`、`?:`、`!!` 与智能转换；代码示例中的 Kotlin 代码必须是语法正确的。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinBasicsContentTest"`
预期：2 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt
git commit -m "feat: 模块一 Kotlin 语言基础课程数据（14 课）"
```

---

### 任务 5：模块二课程数据 AndroidCore（12 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/AndroidCore.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/AndroidCoreContentTest.kt`

**课时清单（id 前缀 `android-`）：**

| # | id | 标题 |
|---|----|------|
| 1 | android-01 | 什么是 Activity |
| 2 | android-02 | Activity 生命周期 |
| 3 | android-03 | 布局系统与 Compose 的关系 |
| 4 | android-04 | Intent 与页面跳转 |
| 5 | android-05 | 资源系统（res/） |
| 6 | android-06 | AndroidManifest.xml |
| 7 | android-07 | 权限模型 |
| 8 | android-08 | Context 是什么 |
| 9 | android-09 | App 进程与主线程 |
| 10 | android-10 | Gradle 构建体系 |
| 11 | android-11 | 调试技巧 |
| 12 | android-12 | 常见术语表 |

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.content.AndroidCore
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidCoreContentTest {
    @Test
    fun `模块二有 12 课且 ID 连续`() {
        assertEquals(12, AndroidCore.lessons.size)
        AndroidCore.lessons.forEachIndexed { i, lesson ->
            assertEquals("android-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块二全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(AndroidCore.moduleOf(AndroidCore.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.AndroidCoreContentTest"`
预期：编译失败（`AndroidCore` 不存在）

- [ ] **步骤 3：编写课程数据**

结构同任务 4（`object AndroidCore`，`moduleOf` 的 id 为 `"android-core"`、title 为 `"Android 核心概念"`、emoji 为 `"🤖"`）。按上方课时清单逐课撰写，质量要求同任务 4 步骤 3 的撰写要求。面向 C++ 程序员的类比角度示例：Activity ≈ 一个带系统托管生命周期的"窗口类"；Context ≈ "系统服务访问句柄"；主线程 ≈ 事件循环中的 UI 线程（类似 Win32 消息循环）。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.AndroidCoreContentTest"`
预期：2 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/AndroidCore.kt app/src/test/java/com/kotlin/learn/data/AndroidCoreContentTest.kt
git commit -m "feat: 模块二 Android 核心概念课程数据（12 课）"
```

---

### 任务 6：模块三课程数据 + Curriculum 聚合（10 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/ComposeBasics.kt`
- 创建：`app/src/main/java/com/kotlin/learn/data/content/Curriculum.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt`

**课时清单（id 前缀 `compose-`）：**

| # | id | 标题 |
|---|----|------|
| 1 | compose-01 | 声明式 UI 思想 |
| 2 | compose-02 | Composable 函数 |
| 3 | compose-03 | 常用组件：Text/Button/Column/Row |
| 4 | compose-04 | 状态与重组 |
| 5 | compose-05 | 状态提升 |
| 6 | compose-06 | LazyColumn 列表 |
| 7 | compose-07 | Material 3 主题 |
| 8 | compose-08 | 导航 |
| 9 | compose-09 | 本 App 源码剖析 |
| 10 | compose-10 | 下一步学习路线 |

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumContentTest {
    @Test
    fun `全课程共 3 模块 36 课`() {
        assertEquals(3, Curriculum.modules.size)
        assertEquals(36, Curriculum.modules.sumOf { it.lessons.size })
        assertEquals(listOf("kotlin-basics", "android-core", "compose-basics"),
            Curriculum.modules.map { it.id })
    }

    @Test
    fun `课时 ID 全局唯一且全部通过校验`() {
        val problems = CurriculumValidator.validate(Curriculum.modules)
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun `按 ID 查询课时`() {
        val lesson = Curriculum.findLesson("compose-01")
        assertEquals("声明式 UI 思想", lesson?.title)
        assertEquals(null, Curriculum.findLesson("not-exist"))
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumContentTest"`
预期：编译失败（`Curriculum` 不存在）

- [ ] **步骤 3：编写 ComposeBasics.kt（结构同任务 4，moduleOf 的 id 为 `"compose-basics"`、title 为 `"Compose UI 入门"`、emoji 为 `"🎨"`）**

撰写要求同前。compose-09（源码剖析）课的 codeExamples 直接引用本工程真实文件结构（data/model、data/content、ui/screens 分层），cppComparison 类比 C++ 工程的头文件/源文件分层。

- [ ] **步骤 4：编写聚合入口 Curriculum.kt**

```kotlin
package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson

/** 全部课程内容入口。 */
object Curriculum {
    val modules: List<CourseModule> = listOf(
        KotlinBasics.moduleOf(KotlinBasics.lessons),
        AndroidCore.moduleOf(AndroidCore.lessons),
        ComposeBasics.moduleOf(ComposeBasics.lessons),
    )

    /** 按课时 ID 查找课程；找不到返回 null（由 UI 显示兜底页）。 */
    fun findLesson(lessonId: String): Lesson? =
        modules.asSequence()
            .flatMap { it.lessons }
            .firstOrNull { it.id == lessonId }

    /** 按课时 ID 查找所属模块。 */
    fun findModuleOf(lessonId: String): CourseModule? =
        modules.firstOrNull { m -> m.lessons.any { it.id == lessonId } }

    val totalLessonCount: Int get() = modules.sumOf { it.lessons.size }
}
```

- [ ] **步骤 5：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumContentTest"`
预期：3 个测试全部 PASS

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content
git add app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt
git commit -m "feat: 模块三 Compose UI 入门课程数据与 Curriculum 聚合"
```

---

### 任务 7：进度存储（TDD 纯逻辑 + DataStore 封装）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/progress/ProgressStore.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/ProgressLogicTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.progress.ProgressLogic
import com.kotlin.learn.data.progress.UserProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressLogicTest {
    @Test
    fun `首次交卷记完成并记录成绩`() {
        val p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", correct = 3, total = 4)
        assertTrue("kotlin-01" in p.completedLessonIds)
        assertEquals(3 to 4, p.bestScores["kotlin-01"])
    }

    @Test
    fun `重复交卷保留最好成绩`() {
        var p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 2, 4)
        p = ProgressLogic.recordBest(p, "kotlin-01", 4, 4)
        p = ProgressLogic.recordBest(p, "kotlin-01", 1, 4)
        assertEquals(4 to 4, p.bestScores["kotlin-01"])
    }

    @Test
    fun `交卷不影响其他课时记录`() {
        var p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 3, 4)
        p = ProgressLogic.recordBest(p, "kotlin-02", 2, 5)
        assertEquals(setOf("kotlin-01", "kotlin-02"), p.completedLessonIds)
        assertEquals(3 to 4, p.bestScores["kotlin-01"])
        assertEquals(2 to 5, p.bestScores["kotlin-02"])
    }

    @Test
    fun `序列化往返一致`() {
        val p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 3, 4)
        val encoded = ProgressLogic.encode(p)
        val decoded = ProgressLogic.decode(encoded)
        assertEquals(p, decoded)
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ProgressLogicTest"`
预期：编译失败（`ProgressLogic`、`UserProgress` 不存在）

- [ ] **步骤 3：编写实现（纯逻辑 + DataStore 封装在同一文件）**

```kotlin
package com.kotlin.learn.data.progress

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** 用户学习进度：已完成课时 + 每课最好成绩（答对数 to 总题数）。 */
data class UserProgress(
    val completedLessonIds: Set<String> = emptySet(),
    val bestScores: Map<String, Pair<Int, Int>> = emptyMap(),
) {
    companion object { val EMPTY = UserProgress() }
}

/** 纯逻辑：进度合并与序列化，便于单元测试（不依赖 Android）。 */
object ProgressLogic {
    fun recordBest(current: UserProgress, lessonId: String, correct: Int, total: Int): UserProgress {
        if (total <= 0) return current
        val old = current.bestScores[lessonId]
        val newBest = if (old == null || correct * old.second > old.first * total) {
            correct to total
        } else old
        return current.copy(
            completedLessonIds = current.completedLessonIds + lessonId,
            bestScores = current.bestScores + (lessonId to newBest),
        )
    }

    /** 编码为键值对，供 DataStore 存储。 */
    fun encode(progress: UserProgress): Map<String, String> = buildMap {
        put("completed", progress.completedLessonIds.joinToString(","))
        progress.bestScores.forEach { (id, score) -> put("score_$id", "${score.first}/${score.second}") }
    }

    /** 从键值对解码。 */
    fun decode(prefs: Map<String, String>): UserProgress {
        val completed = prefs["completed"]?.split(',')?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        val scores = prefs.filterKeys { it.startsWith("score_") }.mapKeys { it.key.removePrefix("score_") }
            .mapValues { (_, v) ->
                val (c, t) = v.split('/').map { it.toInt() }
                c to t
            }
        return UserProgress(completed, scores)
    }
}

private val Context.dataStore by preferencesDataStore(name = "progress")

/** DataStore 封装：UI 层只依赖此类。 */
class ProgressStore(private val context: Context) {
    private val completedKey = stringPreferencesKey("completed")

    val progress: Flow<UserProgress> = context.dataStore.data.map { prefs ->
        val map = buildMap {
            prefs.asMap().forEach { (key, value) -> if (value is String) put(key.name, value) }
        }
        ProgressLogic.decode(map)
    }

    suspend fun recordQuizResult(lessonId: String, correct: Int, total: Int) {
        context.dataStore.edit { prefs ->
            val current = ProgressLogic.decode(
                buildMap {
                    prefs.asMap().forEach { (key, value) -> if (value is String) put(key.name, value) }
                }
            )
            val updated = ProgressLogic.recordBest(current, lessonId, correct, total)
            prefs[completedKey] = updated.completedLessonIds.joinToString(",")
            updated.bestScores.forEach { (id, score) ->
                prefs[stringPreferencesKey("score_$id")] = "${score.first}/${score.second}"
            }
        }
    }

    /** 仅供测试/初始化使用：读取一次当前进度。 */
    suspend fun loadOnce(): UserProgress = progress.first()
}
```

注意：`prefs.asMap()` 的键类型是 `Preferences.Key<*>`，其 `name` 属性即字符串键名。文件顶部需 `import androidx.datastore.preferences.core.Preferences`（如编译器提示）。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ProgressLogicTest"`
预期：4 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/progress app/src/test/java/com/kotlin/learn/data/ProgressLogicTest.kt
git commit -m "feat: 学习进度存储（DataStore）与纯逻辑判分合并"
```

---

### 任务 8：导航骨架 + MainActivity 接入

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`
- 修改：`app/src/main/java/com/kotlin/learn/MainActivity.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/PlaceholderScreens.kt`（临时占位屏，任务 9~11 替换）

- [ ] **步骤 1：编写 AppNav.kt（路由：home / module/{moduleId} / lesson/{lessonId}）**

```kotlin
package com.kotlin.learn.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.progress.ProgressStore

object Routes {
    const val HOME = "home"
    const val MODULE = "module/{moduleId}"
    const val LESSON = "lesson/{lessonId}"
    fun module(moduleId: String) = "module/$moduleId"
    fun lesson(lessonId: String) = "lesson/$lessonId"
}

@Composable
fun AppNav(progressStore: ProgressStore) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            // 任务 9 替换为 HomeScreen(modules, progress, onModuleClick)
        }
        composable(Routes.MODULE) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            // 任务 10 替换为 LessonListScreen
        }
        composable(Routes.LESSON) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            // 任务 11 替换为 LessonScreen
        }
    }
}
```

- [ ] **步骤 2：编写临时占位屏（保证项目可编译可运行）**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PlaceholderScreen(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier.fillMaxSize())
}
```

三个 composable 目的地临时各渲染 `PlaceholderScreen`（home / module / lesson 各一）。

- [ ] **步骤 3：修改 MainActivity 使用 AppNav**

`MainActivity.kt` 的 `setContent` 块改为：

```kotlin
setContent {
    KotlinLearnTheme {
        val progressStore = remember { ProgressStore(applicationContext) }
        AppNav(progressStore = progressStore)
    }
}
```

删除模板遗留的 `Greeting` 与 `GreetingPreview`。所需导入：`androidx.compose.runtime.remember`、`com.kotlin.learn.data.progress.ProgressStore`、`com.kotlin.learn.ui.navigation.AppNav`。

- [ ] **步骤 4：验证构建通过**

运行：`./gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn
git commit -m "feat: Navigation 导航骨架与 MainActivity 接入"
```

---

### 任务 9：HomeScreen 首页（模块列表 + 进度总览）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/HomeScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（home 目的地接入真实屏）

- [ ] **步骤 1：编写 HomeScreen**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.content.Curriculum
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.progress.UserProgress

@Composable
fun HomeScreen(
    progress: UserProgress,
    onModuleClick: (CourseModule) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Text("Kotlin 学习", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = "已学 ${progress.completedLessonIds.size} / ${Curriculum.totalLessonCount} 课",
                    style = MaterialTheme.typography.bodyMedium,
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
            Card(onClick = { onModuleClick(module) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${module.emoji} ${module.title}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${done} / ${module.lessons.size} 课",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
```

- [ ] **步骤 2：接入 AppNav 的 home 目的地**

```kotlin
composable(Routes.HOME) { backStackEntry ->
    val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
    HomeScreen(
        progress = progress,
        onModuleClick = { module -> navController.navigate(Routes.module(module.id)) },
    )
}
```

需导入：`androidx.lifecycle.compose.collectAsStateWithLifecycle`（lifecycle-runtime-compose；若 BOM 未传递提供，则在 `libs.versions.toml` 增加 `androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleRuntimeKtx" }` 并加入依赖）、`androidx.compose.runtime.getValue`、`com.kotlin.learn.data.progress.UserProgress`。`HOME` 路由切换课时后需刷新：Navigation 会在返回时重建 composable，`collectAsStateWithLifecycle` 自动拿到最新值。

- [ ] **步骤 3：验证构建通过**

运行：`./gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 首页模块列表与学习进度总览"
```

---

### 任务 10：LessonListScreen 课程列表

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/LessonListScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（module 目的地接入真实屏）
- 删除：`app/src/main/java/com/kotlin/learn/ui/screens/PlaceholderScreens.kt` 中的对应占位

- [ ] **步骤 1：编写 LessonListScreen**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.progress.UserProgress

@Composable
fun LessonListScreen(
    module: CourseModule,
    progress: UserProgress,
    onLessonClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "${module.emoji} ${module.title}",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()
        LazyColumn {
            itemsIndexed(module.lessons) { index, lesson ->
                val completed = lesson.id in progress.completedLessonIds
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLessonClick(lesson) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(
                        text = "${index + 1}. ${lesson.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    Text(text = if (completed) "✅" else "◻️")
                }
            }
        }
    }
}
```

- [ ] **步骤 2：接入 AppNav 的 module 目的地**

```kotlin
composable(Routes.MODULE) { backStackEntry ->
    val moduleId = backStackEntry.arguments?.getString("moduleId")
    val module = Curriculum.modules.firstOrNull { it.id == moduleId }
    val progress by progressStore.progress.collectAsStateWithLifecycle(initialValue = UserProgress.EMPTY)
    if (module == null) {
        NotFoundScreen() // 任务 11 定义
    } else {
        LessonListScreen(
            module = module,
            progress = progress,
            onLessonClick = { lesson -> navController.navigate(Routes.lesson(lesson.id)) },
        )
    }
}
```

- [ ] **步骤 3：验证构建通过**

运行：`./gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 4：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 课程列表页与完成标记"
```

---

### 任务 11：LessonScreen 课文页 + QuizSection 小测 + 兜底页

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/LessonScreen.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/QuizSection.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/screens/NotFoundScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（lesson 目的地接入）
- 修改：`app/src/main/java/com/kotlin/learn/ui/screens/LessonListScreen.kt`（顶部返回按钮）

- [ ] **步骤 1：编写 QuizSection**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.logic.QuizScorer
import com.kotlin.learn.data.model.QuizQuestion

@Composable
fun QuizSection(
    questions: List<QuizQuestion>,
    onSubmit: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selections = remember(questions) { mutableStateMapOf<Int, Int>() }
    var submitted by remember(questions) { mutableStateOf(false) }
    val result = remember(questions, submitted) {
        if (submitted) QuizScorer.score(questions, selections.toMap()) else null
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("✅ 随堂小测", style = MaterialTheme.typography.titleLarge)
        questions.forEachIndexed { qi, q ->
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("${qi + 1}. ${q.question}", style = MaterialTheme.typography.titleSmall)
                    q.options.forEachIndexed { oi, option ->
                        val selected = selections[qi] == oi
                        val isCorrect = oi == q.correctIndex
                        val containerColor = when {
                            !submitted -> if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            isCorrect -> MaterialTheme.colorScheme.tertiaryContainer
                            selected -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            onClick = { if (!submitted) selections[qi] = oi },
                            colors = CardDefaults.cardColors(containerColor = containerColor),
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        ) {
                            Text(option, Modifier.padding(10.dp))
                        }
                    }
                    if (submitted) {
                        Text(
                            "解析：${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
        if (result != null) {
            Text(
                text = "成绩：${result.correctCount} / ${result.total}" +
                    if (result.allCorrect) " 🎉 全对！" else " 再看看解析，可以重新测试。",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!submitted) {
                Button(
                    onClick = {
                        submitted = true
                        onSubmit(
                            QuizScorer.score(questions, selections.toMap()).correctCount,
                            questions.size,
                        )
                    },
                    enabled = selections.size == questions.size,
                ) { Text("交卷") }
            } else {
                OutlinedButton(onClick = {
                    selections.clear()
                    submitted = false
                }) { Text("重新测试") }
            }
        }
    }
}
```

- [ ] **步骤 2：编写 LessonScreen**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.model.Lesson

@Composable
fun LessonScreen(
    lesson: Lesson,
    onSubmitQuiz: (correct: Int, total: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(lesson.title, style = MaterialTheme.typography.headlineSmall)
        SectionCard(title = "📖 概念讲解", body = lesson.concept)
        lesson.codeExamples.forEach { example ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(example.description, style = MaterialTheme.typography.titleSmall)
                    Text(
                        example.code,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        SectionCard(
            title = "🆚 给 C++ 开发者",
            body = lesson.cppComparison,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        )
        QuizSection(questions = lesson.questions, onSubmit = onSubmitQuiz)
    }
}

@Composable
private fun SectionCard(
    title: String,
    body: String,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
        }
    }
}
```

- [ ] **步骤 3：编写 NotFoundScreen 并接入 lesson 目的地**

```kotlin
package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotFoundScreen(modifier: Modifier = Modifier) {
    Text(
        text = "课程不存在或已被移除",
        modifier = modifier.fillMaxSize().padding(16.dp),
    )
}
```

AppNav 的 lesson 目的地：

```kotlin
composable(Routes.LESSON) { backStackEntry ->
    val lessonId = backStackEntry.arguments?.getString("lessonId")
    val lesson = Curriculum.findLesson(lessonId.orEmpty())
    if (lesson == null) {
        NotFoundScreen()
    } else {
        LessonScreen(
            lesson = lesson,
            onSubmitQuiz = { correct, total ->
                // 交卷即记完成；写库放在协程里
            },
        )
    }
}
```

交卷写进度需协程：在 `AppNav` 顶部 `val scope = rememberCoroutineScope()`，`onSubmitQuiz = { correct, total -> scope.launch { progressStore.recordQuizResult(lessonId!!, correct, total) } }`。需导入 `androidx.compose.runtime.rememberCoroutineScope`、`kotlinx.coroutines.launch`。

- [ ] **步骤 4：LessonListScreen 加返回按钮**

顶部标题行外层包 `Row`，加入 `IconButton(onClick = onBack) { Text("←") }`；`LessonListScreen` 增加 `onBack: () -> Unit` 参数，AppNav 传 `navController.popBackStack()`。`HomeScreen` 无需返回按钮。

- [ ] **步骤 5：删除 PlaceholderScreens.kt 并验证构建**

删除 `app/src/main/java/com/kotlin/learn/ui/screens/PlaceholderScreens.kt`。
运行：`./gradlew.bat :app:assembleDebug`
预期：BUILD SUCCESSFUL

- [ ] **步骤 6：全量测试 + 最终验证**

运行：
```bash
./gradlew.bat :app:testDebugUnitTest
```
预期：全部 PASS（QuizScorerTest、CurriculumValidatorTest、KotlinBasicsContentTest、AndroidCoreContentTest、CurriculumContentTest、ProgressLogicTest）。

- [ ] **步骤 7：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui
git commit -m "feat: 课文页、随堂小测与进度写入"
```

---

## 自检记录

- **规格覆盖度**：3 模块 36 课（任务 4/5/6）✓；每课四段式结构（模型 + LessonScreen）✓；小测判分与解析（任务 2/11）✓；进度追踪与最好成绩（任务 7）✓；首页进度总览与完成标记（任务 9/10）✓；无效 ID 兜底页（任务 11）✓；测试策略三要素（任务 2/3/7 + 内容守门测试）✓。
- **占位符扫描**：任务 4/5/6 的"其余课程按清单撰写"不是占位符——课时清单逐课列出、结构有示例课全文锚定、有守门测试验收；其余步骤均含完整代码。
- **类型一致性**：`QuizQuestion.correctIndex`、`QuizScorer.score(questions, answers)`、`ProgressLogic.recordBest(current, lessonId, correct, total)`、`UserProgress(completedLessonIds, bestScores)`、`Curriculum.findLesson/findModuleOf` 在各任务间签名一致；`moduleOf` 工厂在三个内容文件中同名同构。
