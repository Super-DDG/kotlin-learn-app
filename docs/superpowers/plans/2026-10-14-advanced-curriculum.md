# 第二批进阶课程（4 模块 23 课）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增 4 个模块 23 课进阶课程（Kotlin 进阶 6 / 数据与网络 7 / 架构 5 / 工程化与发布 5），课程总数 40→63。

**架构：** 每模块一个内容文件（data/content/ 下新 object）+ 一个守门测试文件；沿用模块一验证过的全部体系（四段式、Markdown 排版、C++ 对比、expectedOutput 离线运行结果、CurriculumValidator 守门）；Curriculum 聚合追加 4 个模块。

**技术栈：** 既有 Kotlin + Compose，零新依赖（课程讲 Retrofit/Room 概念不引入实际依赖）。

**规格：** `docs/superpowers/specs/2026-10-14-advanced-curriculum-design.md`

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 每模块任务 = 守门测试（红）→ 内容（绿）→ commit；守门断言四件套：**课数、ID 连续、全部示例 expectedOutput 非空、通过 CurriculumValidator**
- **课程写作规范（每任务同一条）：** ①四段式：concept（Markdown：导语 + 2~3 个 `## ` 小节 + `· `/`- ` 列表 + `**加粗**`）/ 1~2 段 CodeExample（语法正确、带注释）/ cppComparison（3~5 条 `·` 开头 C++ 类比）/ 3~5 题小测（每题 2~4 选项 + 解析）②**expectedOutput 确定性规则：协程示例一律 `runBlocking` + 顺序可推导场景（单协程顺序执行、delay 时序确定）；并发交错语义用文字讲解不做输出展示** ③raw string 内字面 `$` 写 `${'$'}`；数字列表序号后有空格；加粗内无未闭合反引号 ④剖析本 App 的课（data-02/arch-05/ship-02）先读真实源码再写
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：模块四 Kotlin 进阶（kotlin-advanced，6 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/KotlinAdvanced.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/KotlinAdvancedContentTest.kt`

- [ ] **步骤 1：编写失败的守门测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.content.KotlinAdvanced
import com.kotlin.learn.data.logic.CurriculumValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KotlinAdvancedContentTest {
    @Test
    fun `模块四有 6 课且 ID 连续`() {
        assertEquals(6, KotlinAdvanced.lessons.size)
        KotlinAdvanced.lessons.forEachIndexed { i, lesson ->
            assertEquals("adv-%02d".format(i + 1), lesson.id)
        }
    }

    @Test
    fun `模块四全部示例都有预期输出`() {
        KotlinAdvanced.lessons.forEach { lesson ->
            lesson.codeExamples.forEachIndexed { i, example ->
                assertTrue(
                    "${lesson.id} 示例 ${i + 1} 缺少预期输出",
                    example.expectedOutput.isNotBlank(),
                )
            }
        }
    }

    @Test
    fun `模块四全部课时通过完整性校验`() {
        val problems = CurriculumValidator.validate(
            listOf(KotlinAdvanced.moduleOf(KotlinAdvanced.lessons))
        )
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }
}
```

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinAdvancedContentTest"`
预期：编译失败（`KotlinAdvanced` 不存在）

- [ ] **步骤 3：编写课程内容**

object 骨架（moduleOf 的 id=`"kotlin-advanced"`、title=`"Kotlin 进阶"`、emoji=`"⚡"`）：

```kotlin
package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块四：Kotlin 进阶（6 课），协程为主。 */
object KotlinAdvanced {
    val lessons: List<Lesson> = listOf(
        // adv-01 .. adv-06，按下方课时清单逐课撰写
    )

    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "kotlin-advanced",
        title = "Kotlin 进阶",
        emoji = "⚡",
        lessons = lessons,
    )
}
```

**课时清单**：adv-01 协程：为什么需要 / adv-02 协程基础 / adv-03 挂起函数与结构化并发 / adv-04 调度器与线程切换 / adv-05 协程异常处理 / adv-06 作用域函数与委托属性

**示例锚点（adv-02 协程基础全文，示范 runBlocking 确定性输出规则；其余 5 课照此结构撰写）：**

```kotlin
        Lesson(
            id = "adv-02",
            title = "协程基础",
            concept = """
                协程是**可挂起的轻量级线程**。这一课只讲三个最核心的构件。

                ## 三个核心构件

                - `runBlocking`：把普通世界和协程世界连起来的桥（测试与示例用）
                - `launch`：启动一个协程，不返回结果
                - `delay`：挂起等待，不阻塞线程

                ## suspend：挂起函数

                带 `suspend` 关键字的函数可以调用 `delay` 等挂起操作，
                也**只能在协程或其他挂起函数里调用**——编译器强制约束。

                ## 为什么轻量

                挂起时线程不被占用，可同时跑成千上万个协程；
                线程（C++ 的 `std::thread`）动辄占 MB 级栈内存。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "launch 与 delay：顺序可推导的输出",
                    code = """
                        import kotlinx.coroutines.*

                        fun main() = runBlocking {
                            launch {
                                delay(500L)
                                println("世界！")
                            }
                            print("你好，")
                            delay(1000L)
                        }
                    """.trimIndent(),
                    expectedOutput = "你好，世界！",
                ),
                CodeExample(
                    description = "suspend 函数：挂起地拿结果",
                    code = """
                        suspend fun doWork(): String {
                            delay(300L)
                            return "完成"
                        }

                        fun main() = runBlocking {
                            println(doWork())
                        }
                    """.trimIndent(),
                    expectedOutput = "完成",
                ),
            ),
            cppComparison = """
                · C++ 的 `std::async`/`std::future` 是线程级等待；协程挂起时**不占用线程**，这是最本质的差别。
                · Kotlin 的 `suspend` 函数 ≈ C++20 的 `co_await` 协程函数，但 Kotlin 无需 promise/future 样板。
                · `runBlocking` ≈ "在普通函数里等协程跑完再走"，类似在 main 里 `future.get()`。
                · 千个协程 vs 千个线程：内存差两个数量级，这正是 Android 选协程的原因。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "下列哪个函数可以在普通函数里直接调用？",
                    options = listOf("delay(1000L)", "runBlocking { ... }", "suspend fun doWork()", "launch { ... }"),
                    correctIndex = 1,
                    explanation = "delay/launch/挂起函数都必须在协程作用域内；runBlocking 正是把普通世界接入协程世界的桥。",
                ),
                QuizQuestion(
                    question = "挂起时线程在做什么？",
                    options = listOf("阻塞等待", "被释放去干别的活", "被销毁", "轮询等待"),
                    correctIndex = 1,
                    explanation = "挂起的本质是\"到点再回来\"，线程被释放去执行其他协程——这是协程轻量的根源，与线程阻塞等待完全不同。",
                ),
                QuizQuestion(
                    question = "suspend 函数的调用约束是？",
                    options = listOf(
                        "只能在协程或挂起函数中调用",
                        "任何地方都能调用",
                        "只能在主线程调用",
                        "只能调用一次",
                    ),
                    correctIndex = 0,
                    explanation = "编译器强制：suspend 调用点必须在协程作用域内。这是防止\"挂起了却没人接\"的编译期保护。",
                ),
            ),
        ),
```

撰写要求（其余 5 课 + 后续模块同此标准）：adv-01 用"回调地狱"对照（嵌套回调 vs 顺序挂起代码，纯 Kotlin 可运行示例）；adv-03 覆盖作用域/Job/cancel；adv-04 覆盖 Dispatchers.Main/IO/Default 与 withContext（示例用 runBlocking + withContext(Dispatchers.Default) 保证确定输出）；adv-05 覆盖 try-catch 与 CoroutineExceptionHandler；adv-06 覆盖 let/run/apply/also 与 by lazy。C++ 类比角度：std::async/future、C++20 co_await、RAII、static 局部变量（lazy）。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinAdvancedContentTest"`
预期：3 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinAdvanced.kt app/src/test/java/com/kotlin/learn/data/KotlinAdvancedContentTest.kt
git commit -m "feat: 模块四 Kotlin 进阶课程数据（6 课）"
```

---

### 任务 2：模块五 数据与网络（data-network，7 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/DataNetwork.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/DataNetworkContentTest.kt`

- [ ] **步骤 1：编写失败的守门测试**

结构同任务 1 步骤 1（类名 `DataNetworkContentTest`，断言 `DataNetwork.lessons`：**7 课**、ID `data-%02d` 连续、expectedOutput 非空、通过校验器；moduleOf 的 id=`"data-network"`、title=`"数据与网络"`、emoji=`"💾"`）。

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.DataNetworkContentTest"`
预期：编译失败

- [ ] **步骤 3：编写课程内容（7 课按规格课表）**

课时清单：data-01 文件存储 / data-02 DataStore 深入 / data-03 Room：实体与 DAO / data-04 Room：数据库与查询 / data-05 HTTP 与 JSON 概念 / data-06 Retrofit 基础 / data-07 仓库模式

撰写要点：
- **data-02 必须先读真实源码** `app/src/main/java/com/kotlin/learn/data/progress/ProgressStore.kt`：concept 剖析 `preferencesDataStore` 委托、`Flow` 暴露、`edit` 原子写；codeExamples 直接摘录 ProgressStore 的真实片段（`val progress: Flow<UserProgress>` 与 `recordQuizResult` 的 edit 块）；cppComparison 对照 C++ 的文件读写与 SQLite C API
- data-03/04：Room 的 @Entity/@Dao/@Database 与 suspend/Flow 查询（示例展示注解与接口定义，运行输出为"概念示意"——Room 不能在纯 Kotlin 示例中真跑，expectedOutput 填该示例**如果运行会**产生的行为描述，如 `"数据库创建完成\n查到 2 条记录"` 并在 description 中注明"示意"）；此类"概念示意"仅限 Room/Retrofit 两主题，协程与文件/DataStore 主题必须真实可推
- data-05/06：HTTP 请求响应结构、Retrofit 接口定义（`@GET("users/{id}") suspend fun user(...)`）
- data-07：仓库模式接口串接（伪代码式 Kotlin，expectedOutput 为行为描述）

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.DataNetworkContentTest"`
预期：3 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/DataNetwork.kt app/src/test/java/com/kotlin/learn/data/DataNetworkContentTest.kt
git commit -m "feat: 模块五 数据与网络课程数据（7 课）"
```

---

### 任务 3：模块六 架构（architecture，5 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/Architecture.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/ArchitectureContentTest.kt`

- [ ] **步骤 1：编写失败的守门测试**

结构同任务 1（类名 `ArchitectureContentTest`：**5 课**、ID `arch-%02d` 连续、expectedOutput 非空、通过校验器；moduleOf 的 id=`"architecture"`、title=`"架构"`、emoji=`"🏛"`）。

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ArchitectureContentTest"`
预期：编译失败

- [ ] **步骤 3：编写课程内容（5 课按规格课表）**

课时清单：arch-01 副作用 API / arch-02 rememberSaveable 与进程死亡恢复 / arch-03 ViewModel / arch-04 StateFlow 与 UI 状态 / arch-05 分层架构

撰写要点：
- arch-01：LaunchedEffect(Unit/keys) 与 DisposableEffect 的时机语义；示例用 `LaunchedEffect(Unit) { delay(100); println("加载完成") }` 形态配 runBlocking 输出（同 adv 规则）
- arch-02：Bundle 保存/恢复时机、`rememberSaveable` vs `remember` 的差异与限制（Parcelable/原始类型）
- arch-03：ViewModel 职责（配置变更存活）、为什么组合层不该持有状态
- arch-04：StateFlow/collectAsState、单向数据流闭环（可引用本 App 的 `progressStore.progress` 真实 Flow）
- **arch-05 必须先读本工程真实结构**（data/model、data/content、data/progress、data/logic、data/markdown、ui/render、ui/screens、ui/demos、ui/navigation）：concept 画分层图（文字版），codeExamples 摘录真实分层片段；cppComparison 对照 C++ 工程的头文件/源文件/链接分层

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ArchitectureContentTest"`
预期：3 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/Architecture.kt app/src/test/java/com/kotlin/learn/data/ArchitectureContentTest.kt
git commit -m "feat: 模块六 架构课程数据（5 课）"
```

---

### 任务 4：模块七 工程化与发布（shipping，5 课）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/content/Shipping.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/ShippingContentTest.kt`

- [ ] **步骤 1：编写失败的守门测试**

结构同任务 1（类名 `ShippingContentTest`：**5 课**、ID `ship-%02d` 连续、expectedOutput 非空、通过校验器；moduleOf 的 id=`"shipping"`、title=`"工程化与发布"`、emoji=`"🚢"`）。

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ShippingContentTest"`
预期：编译失败

- [ ] **步骤 3：编写课程内容（5 课按规格课表）**

课时清单：ship-01 Service 与 BroadcastReceiver 概览 / ship-02 单元测试实战 / ship-03 构建变体与依赖管理 / ship-04 应用签名与打包 / ship-05 发布渠道与上架

撰写要点：
- **ship-02 必须先读真实测试** `app/src/test/java/com/kotlin/learn/data/QuizScorerTest.kt` 与 `ProgressLogicTest.kt`：concept 讲 AAA（Arrange-Act-Assert）结构与命名（反引号中文测试名），codeExamples 摘录真实测试片段
- ship-01：Service 前台/后台、BroadcastReceiver 有序/无序广播，服务端语义用文字讲（无可运行输出，expectedOutput 填行为描述）
- ship-03：buildType 差异（本 App 的 debug/release）、Version Catalog 引用语法
- ship-04：keytool 生成密钥库、signingConfig、APK vs AAB（AAB 按设备拆分）
- ship-05：应用市场材料清单、隐私合规要点（权限声明与用途对应）

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ShippingContentTest"`
预期：3 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/Shipping.kt app/src/test/java/com/kotlin/learn/data/ShippingContentTest.kt
git commit -m "feat: 模块七 工程化与发布课程数据（5 课）"
```

---

### 任务 5：Curriculum 聚合 + 守门更新（TDD）

**文件：**
- 修改：`app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt`
- 修改：`app/src/main/java/com/kotlin/learn/data/content/Curriculum.kt`

- [ ] **步骤 1：更新守门断言（先红）**

```kotlin
    @Test
    fun `全课程共 7 模块 63 课`() {
        assertEquals(7, Curriculum.modules.size)
        assertEquals(63, Curriculum.modules.sumOf { it.lessons.size })
        assertEquals(
            listOf(
                "kotlin-basics", "android-core", "compose-basics",
                "kotlin-advanced", "data-network", "architecture", "shipping",
            ),
            Curriculum.modules.map { it.id },
        )
    }
```

（其余两个测试不变：ID 全局唯一校验与 findLesson 行为。）

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumContentTest"`
预期：FAIL（3 模块 40 课）

- [ ] **步骤 3：聚合 4 个新模块**

`Curriculum.modules` 改为：

```kotlin
    val modules: List<CourseModule> = listOf(
        KotlinBasics.moduleOf(KotlinBasics.lessons),
        AndroidCore.moduleOf(AndroidCore.lessons),
        ComposeBasics.moduleOf(ComposeBasics.lessons),
        KotlinAdvanced.moduleOf(KotlinAdvanced.lessons),
        DataNetwork.moduleOf(DataNetwork.lessons),
        Architecture.moduleOf(Architecture.lessons),
        Shipping.moduleOf(Shipping.lessons),
    )
```

（findLesson/findModuleOf/totalLessonCount 不动。）

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest`
预期：**48/48**（45 + 4 个新模块守门文件各 3 条 = 45+12-3……以实际为准：45 既有 + 12 新增 - 1 条 CurriculumContentTest 原计数断言不变条数——实现以"全部 PASS、无失败"为准并报告精确总数）

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/Curriculum.kt app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt
git commit -m "feat: Curriculum 聚合 7 模块 63 课"
```

---

### 任务 6：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest --rerun`（全部 PASS，报告精确总数）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：7 个模块 id 全局唯一；63 = 14+12+14+6+7+5+5；首页进度文案动态计算无需改动；`expectedOutput =` 全仓出现次数较上批净增 ≥23

---

## 自检记录

- **规格覆盖度**：4 模块 23 课课表（任务 1~4 课时清单与规格逐条对应）✓；expectedOutput 协程确定性规则（全局约定 + adv-02 锚点示范）✓；剖析本 App 三课指定真实源码文件（任务 2/3/4 撰写要点）✓；Curriculum 聚合与守门更新（任务 5）✓；收尾（任务 6）✓；Fragment/Hilt 留第三批（未排入，符合规格范围外）✓。
- **占位符扫描**：任务 1~4 的"其余课按清单撰写"非占位符——每任务有完整课时表、adv-02 全文锚点、统一写作规范与守门验收；Room/Retrofit"概念示意"输出有明确裁定（仅限两主题，协程/文件/DataStore 必须真实可推）。
- **类型一致性**：`moduleOf(lessons)` 工厂四模块同名同构；id/emoji 与规格一致（kotlin-advanced ⚡ / data-network 💾 / architecture 🏛 / shipping 🚢）；ID 前缀 adv-/data-/arch-/ship- 与守门断言 format 一致；聚合顺序与断言列表一致。
