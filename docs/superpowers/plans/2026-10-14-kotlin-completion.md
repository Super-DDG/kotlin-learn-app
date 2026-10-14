# Kotlin 课程补全批次实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** Kotlin 课程补全：模块一新增 kotlin-00 语言介绍（Java/C++ 三方对比）、4 课基础补完（枚举/接口与嵌套类/跳转标签/运算符概览，小节增补 ID 零改动）、模块四新增 7 课（adv-07..13），总数 63 → 71。

**架构：** 全部沿用既有体系（四段式、Markdown、C++ 对比、expectedOutput、CurriculumValidator 守门）；输出核实规则：可运行示例必须提取-编译-运行真实核实（lambda 插值模拟推导不可靠——前序教训），adv-13 序列化为概念示意（库未引入，沿 Room/Retrofit 先例）。

**技术栈：** 既有 Kotlin，零新依赖。

**规格：** `docs/superpowers/specs/2026-10-14-kotlin-completion-design.md`

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **57/57**；收尾以全部 PASS 为准并报告精确总数
- **写作规范（每任务同一条）**：四段式（concept Markdown 2~3 个 `## ` 小节 + 列表 + 加粗 / CodeExample / cppComparison 3~5 条 `·` / 小测 3~5 题）；raw string 字面 `$` 写 `${'$'}`；数字列表序号后有空格；加粗内无未闭合反引号
- **输出核实纪律（前序 Critical 教训）**：含 lambda/插值/集合 toString 的示例输出，一律提取-编译-运行真实核实（生成临时 JUnit 捕获 System.out，经 ./gradlew 执行，用后删除）；模板插值空格、Double 格式、`also`/`it` 接收者是历史踩坑点
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：kotlin-00「Kotlin 语言介绍」（TDD）

**文件：**
- 修改：`app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt`（14→15 课断言，ID kotlin-00..14）
- 修改：`app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt`（lessons 首位插入 kotlin-00）

- [ ] **步骤 1：守门断言更新（先红）**

`KotlinBasicsContentTest` 的课数与 ID 断言改为：

```kotlin
    @Test
    fun `模块一有 15 课且 ID 含 kotlin-00 前插`() {
        assertEquals(15, KotlinBasics.lessons.size)
        assertEquals("kotlin-00", KotlinBasics.lessons[0].id)
        KotlinBasics.lessons.drop(1).forEachIndexed { i, lesson ->
            assertEquals("kotlin-%02d".format(i + 1), lesson.id)
        }
    }
```

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinBasicsContentTest"`
预期：FAIL（14 课）

- [ ] **步骤 3：撰写 kotlin-00 全文（插入 lessons 首位）**

```kotlin
        Lesson(
            id = "kotlin-00",
            title = "Kotlin 语言介绍",
            concept = """
                写代码之前，先弄清楚 **Kotlin 是什么、为什么值得学**。

                ## Kotlin 是什么

                - JetBrains 2011 年发布、Google 2017 年定为 Android 官方首选语言
                - **静态类型**、运行在 JVM 上，与 Java **100% 互操作**
                - 设计哲学：**简洁**（消样板）、**安全**（空安全消 NPE）、**现代**（协程、DSL）

                ## 与 Java 相比

                - 空安全把 NullPointerException 拦在编译期
                - `data class`、`when`、扩展函数消掉大量样板
                - Java 的每一行 Kotlin 几乎都能等价改写——老项目可以渐进迁移

                ## 与 C++ 相比

                - 内存托管：无手动 delete、无悬垂指针，代价是失去底层控制
                - 没有宏与模板元编程；泛型比 C++ 模板简单也弱一些
                - 协程替代裸线程管理并发；编译目标除 JVM 外还有 Native/JS
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "同一个\"读用户名并打印欢迎语\"：Kotlin 版（可运行）",
                    code = """
                        data class User(val name: String?, val age: Int)

                        fun greet(user: User?): String =
                            user?.name?.let { "你好，${'$'}it！" } ?: "游客你好"

                        fun main() = runBlocking {
                            println(greet(User("旺财", 3)))
                            println(greet(null))
                        }
                    """.trimIndent(),
                    expectedOutput = "你好，旺财！\n游客你好",
                ),
                CodeExample(
                    description = "同功能 Java 版与 C++ 版（对照阅读，不可在 App 内运行）",
                    code = """
                        // Java 版：注意空检查与 getter 样板
                        public class User {
                            private final String name;
                            private final int age;
                            public User(String name, int age) {
                                this.name = name; this.age = age;
                            }
                            public String getName() { return name; }
                            public int getAge() { return age; }
                        }
                        // String greet(User u) {
                        //     return u != null && u.getName() != null
                        //         ? "你好，" + u.getName() + "！" : "游客你好";
                        // }

                        // C++ 版：注意内存与拷贝语义
                        // struct User { std::string name; int age; };
                        // std::string greet(const User* u) {
                        //     return u ? "你好，" + u->name + "！" : "游客你好";
                        // }  // 调用方负责 u 的生命周期！
                    """.trimIndent(),
                    expectedOutput = "（对照阅读用：Kotlin 版以两行代码完成同等功能）",
                ),
            ),
            cppComparison = """
                · Java 版的 `User` 有 8 行样板，Kotlin 的 `data class User(...)` 一行顶全部——getter/equals/hashCode/toString 自动生成。
                · C++ 的 `const User*` 需要**调用方保证生命周期**；Kotlin 的 `User?` 由编译器和运行时托管，传 null 也不崩。
                · Kotlin 没有 `#include` 与头文件——包结构即声明，冲突靠全限定名。
                · Kotlin 的空安全 `?.`/`?:` ≈ C++ 里到处写 `if (p != nullptr)` 的纪律被编译器强制化了。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 与 Java 的关系是？",
                    options = listOf(
                        "100% 互操作，可渐进迁移",
                        "完全不兼容，只能重写",
                        "Kotlin 是 Java 的超集",
                        "Kotlin 编译为 Java 源码",
                    ),
                    correctIndex = 0,
                    explanation = "Kotlin 编译为同一种 JVM 字节码，Java 类可直接在 Kotlin 中调用，反之亦然——这是\"渐进迁移\"口号的底气。",
                ),
                QuizQuestion(
                    question = "与 C++ 相比，Kotlin 失去了什么能力？",
                    options = listOf(
                        "面向对象",
                        "手动内存管理与底层控制",
                        "函数封装",
                        "泛型",
                    ),
                    correctIndex = 1,
                    explanation = "托管内存换安全：没有手动 delete 与指针运算，也失去了对内存布局的底层控制——这是取舍而非缺陷。",
                ),
                QuizQuestion(
                    question = "Kotlin 官方主打的三大设计卖点不包括？",
                    options = listOf("简洁", "安全", "互操作", "比 Java 运行速度快"),
                    correctIndex = 3,
                    explanation = "Kotlin 与 Java 编译为同种字节码，运行速度同级；官方卖点主要是简洁、安全（空安全）与互操作。",
                ),
            ),
        ),
```

（kotlin-00 的 runBlocking 用法与 adv-02 锚点一致；输出经提取-编译-运行核实。）

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinBasicsContentTest"`
预期：全部 PASS（15 课）

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt
git commit -m "feat: 模块一新增 Kotlin 语言介绍课（kotlin-00，Java/C++ 三方对比）"
```

---

### 任务 2：基础补完（4 课小节增补，ID 零改动）

**文件：**
- 修改：`app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt`（4 处增补）

- [ ] **步骤 1：kotlin-08 改造为「枚举与密封类」**

1. `title` 改为 `"枚举与密封类"`
2. concept 顶部补一个 `## 枚举类` 小节：`enum class` 语法、`values()`/`valueOf()`、每个枚举常量都是实例、可带属性与方法、与 sealed 的取舍（**枚举=值固定且穷尽，sealed=子类型可携带不同状态**）
3. codeExamples 追加一段枚举示例（真实运行核实）：

```kotlin
                CodeExample(
                    description = "枚举：固定的值集合，可带属性与方法",
                    code = """
                        enum class Planet(val gravity: Double) {
                            水星(3.7), 金星(8.9), 地球(9.8)
                        }

                        fun main() {
                            for (p in Planet.entries) println("${'$'}{p.name}：${'$'}{p.gravity}")
                            println(Planet.valueOf("地球") == Planet.地球)
                        }
                    """.trimIndent(),
                    expectedOutput = "<以实际编译运行结果为准，逐字符填写>",
                ),
```

（输出以真实运行结果为准——`Planet.entries` 为 Kotlin 1.9+ 语法；若运行结果与预期不符，以运行结果为准。）
4. 小测追加 1~2 题（枚举与 sealed 的取舍、entries/valueOf）
5. 原"密封类"讲解保持，concept 增加一句"与枚举的取舍"过渡

- [ ] **步骤 2：kotlin-06 增补（接口 + 嵌套类/内部类 + 构造器细节）**

concept 补两个 `## ` 小节（插在继承小节之后）：
1. `## 接口`：`interface` 声明、默认实现、属性；与抽象类取舍（接口无状态、多实现）
2. `## 嵌套类与内部类`：`class A.B`（嵌套，不持外部引用）vs `inner class A.B`（内部，持外部引用、需 `this@A`）；对照 C++ 嵌套类的差异（Kotlin 嵌套类默认不持外部 this，C++ 也如此，inner 是 Kotlin 特有）
3. 主次构造器细节补进原有类小节（`init` 块、默认值即省略重载）

小测追加 1~2 题（inner 持有外部引用、接口默认实现）。

- [ ] **步骤 3：kotlin-03 增补（跳转标签）**

concept 补 `## 跳转与标签` 小节：break/continue 常规 + **标签** `loop@ for { break@loop }`；codeExamples 追加 1 段标签示例（真实运行核实）：

```kotlin
                CodeExample(
                    description = "标签跳转：直接跳出外层循环",
                    code = """
                        fun main() {
                            loop@ for (i in 1..3) {
                                for (j in 1..3) {
                                    if (i * j > 4) break@loop
                                    print("${'$'}{i * j} ")
                                }
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "<以实际编译运行结果为准，逐字符填写>",
                ),
```

小测追加 1 题（标签语义）。

- [ ] **步骤 4：kotlin-01 增补（运算符概览）**

concept 补 `## 运算符概览` 小节：算术（+ - * / %，**Int 除法截断**）、比较（== 是 equals，=== 是引用）、逻辑、区间（`..`）；一句话指向"自定义运算符见模块四 adv-11"。

小测追加 1 题（Int 除法截断：`7 / 2 == 3`）。

- [ ] **步骤 5：验证**

`./gradlew.bat :app:testDebugUnitTest`（全部 PASS，报告精确总数——新增段守门会拉高 expectedOutput 非空覆盖）
**新增示例输出必须提取-编译-运行真实核实（kotlin-00/08/03 共 4 段），临时文件用后删除。**

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinBasics.kt app/src/test/java/com/kotlin/learn/data/KotlinBasicsContentTest.kt
git commit -m "feat: 基础补完（枚举/接口与嵌套类/跳转标签/运算符概览）并新增语言介绍课"
```

（若任务 1、2 拆分提交，可分两个 commit——实现者按逻辑归属决定，报告说明。）

---

### 任务 3：模块四新增 7 课（adv-07..adv-13，TDD）

**文件：**
- 修改：`app/src/test/java/com/kotlin/learn/data/KotlinAdvancedContentTest.kt`（6→13 课断言，先红）
- 修改：`app/src/main/java/com/kotlin/learn/data/content/KotlinAdvanced.kt`（追加 adv-07..13）

- [ ] **步骤 1：守门断言更新（先红）**

```kotlin
    @Test
    fun `模块四有 13 课且 ID 连续`() {
        assertEquals(13, KotlinAdvanced.lessons.size)
        KotlinAdvanced.lessons.forEachIndexed { i, lesson ->
            assertEquals("adv-%02d".format(i + 1), lesson.id)
        }
    }
```

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinAdvancedContentTest"`
预期：FAIL（6 课）

- [ ] **步骤 3：撰写 7 课（adv-02/06 既有课为结构锚点）**

课时与撰写要点：

1. **adv-07 泛型与型变**：`<T>`、`out`（生产者）/`in`（消费者）、星投影 `List<*>`、`reified` 配 inline；codeExamples：泛型函数 + reified 类型判断（真实运行）；C++ 对照：模板 vs 型变（C++ 无型变，靠 SFINAE/concepts）
2. **adv-08 Flow 进阶**：冷流（每次收集重新执行）vs `MutableStateFlow`（热）、`combine`/`zip`、`conflate`；codeExamples：combine 合并两流（runBlocking + delay，真实运行）；C++ 对照：观察者/信号槽
3. **adv-09 inline 与非局部返回**：inline 让 lambda 内 `return` 直接返回外层函数（非局部返回）、noinline/crossinline；codeExamples：inline vs 普通高阶函数的非局部返回对照（真实运行）
4. **adv-10 lambda with receiver 与 DSL**：`带接收者的函数类型 T.() -> Unit`、`apply` 的原理、`@DslMarker`；codeExamples：手写一个迷你 DSL（如 html/buildList 风格 builder，真实运行）
5. **adv-11 运算符重载与中缀函数**：`operator fun plus/get/rangeTo/invoke/contains`、`infix fun`；codeExamples：向量类重载 `plus`/`get` + `infix`（真实运行）
6. **adv-12 密封接口与自定义委托**：`sealed interface`（vs sealed class：接口无构造状态、枚举类实现）、自定义 `ReadOnlyProperty/ReadWriteProperty`、`Delegates.observable`；codeExamples：自定义 observable 委托（真实运行）
7. **adv-13 序列化**：`@Serializable` 注解与 `Json.encodeToString` 的机制（编译器插件生成序列化器）、与手写 toString/parse 对照——**概念示意**（库未引入依赖，description 注明"概念示意，非真实运行输出"，沿 data-03/06 先例）；C++ 对照：手写 operator<</>> 序列化

撰写要求：四段式、Markdown、C++ 类比 3~5 条、小测 3~5 题；**除 adv-13 外全部示例输出经提取-编译-运行真实核实**（lambda/插值段为高风险点，模拟推导不可靠）；adv-13 的"示意"标注照 data-03/06 格式。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.KotlinAdvancedContentTest"`
预期：全部 PASS（13 课）

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/content/KotlinAdvanced.kt app/src/test/java/com/kotlin/learn/data/KotlinAdvancedContentTest.kt
git commit -m "feat: 模块四新增泛型/Flow/inline/DSL/运算符/委托/序列化 7 课"
```

---

### 任务 4：Curriculum 聚合更新（TDD）

**文件：**
- 修改：`app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt`
- 修改：`app/src/main/java/com/kotlin/learn/data/content/Curriculum.kt`

- [ ] **步骤 1：守门断言更新（先红）**

```kotlin
    @Test
    fun `全课程共 7 模块 71 课`() {
        assertEquals(7, Curriculum.modules.size)
        assertEquals(71, Curriculum.modules.sumOf { it.lessons.size })
    }
```

（模块 ID 列表不变——无新模块。）

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CurriculumContentTest"`
预期：FAIL（63 课）

- [ ] **步骤 3：确认 Curriculum.kt 无需改动**

各模块的 `lessons` 列表已被 `moduleOf` 引用，聚合是动态的——`Curriculum.kt` 本身**零改动**即生效（这是既有架构的优点）。若运行仍 FAIL，说明某个 content 文件没有真正保存修改，回去检查。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest`
预期：全部 PASS（57 基线 + 任务 1/2/3 新增断言，报告精确总数，预期 ≈ 58~60）

- [ ] **步骤 5：Commit**

```bash
git add app/src/test/java/com/kotlin/learn/data/CurriculumContentTest.kt
git commit -m "test: 全课程守门更新为 71 课"
```

---

### 任务 5：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest --rerun`（全部 PASS，报告精确总数）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：模块一 15 课（kotlin-00..14）；模块四 13 课（adv-01..13）；总数 71 对账（15+12+14+13+7+5+5）；首页动态计算零改动；本批新增示例 expectedOutput 全非空且真实运行段有核实记录

---

## 自检记录

- **规格覆盖度**：kotlin-00（任务 1）✓；基础补完 4 处（任务 2：枚举/接口嵌套/标签/运算符）✓；进阶 7 课（任务 3，规格表逐条对应）✓；聚合 71 课（任务 4）✓；收尾（任务 5）✓；adv-13 概念示意裁定 ✓；输出真实运行纪律（全局约定）✓。
- **占位符扫描**：任务 2 步骤 1/3 的 `"<以实际编译运行结果为准>"` 不是占位符——是"输出以真实运行为准"的裁定标记，实现者必须替换为实际运行结果并在报告记录；任务 3 adv-13 概念示意同先例。
- **类型一致性**：ID 方案 kotlin-00..14 / adv-01..13 与守门断言一致；`runBlocking` 锚点与 adv-02 一致；71 对账（15+12+14+13+7+5+5）与守门一致；提取-编译-运行方法与前序任务一致。
