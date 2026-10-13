package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块三：Compose UI 入门（10 课）。面向会 C++ 的初学者，讲解 Jetpack Compose 声明式 UI。 */
object ComposeBasics {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "compose-01",
            title = "声明式 UI 思想",
            concept = """
                传统 Android View 体系是"命令式"的，Jetpack Compose 是"声明式"的——你只描述"界面在当前状态下应该长什么样"，状态一变，界面自动刷新到最新。

                ## 命令式 vs 声明式

                - 命令式：你要拿到 TextView 的引用，手动调用 `setText()`，界面状态**散落在几十处赋值语句里**，改一处漏一处就出 bug
                - 声明式：状态一变，Compose **自动重新执行相关函数**并把界面刷新到最新——这个自动重画的过程叫**重组（Recomposition）**

                ## 核心公式：UI = f(state)

                - 界面是**状态的函数**，状态是**唯一的事实来源**（Single Source of Truth）
                - 你不再写"怎么改界面"，只写"界面长什么样"

                ## 不止 Android 独有

                - React、Flutter、SwiftUI 全是同一套思想，学会 Compose 再看其他框架几乎是"换门语法"
                - 本 App 从这一模块起**全面使用 Compose**：整个界面由 Composable 函数搭建，**没有一行 XML 布局**
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "同一个需求，命令式与声明式的写法对比——点击按钮改文字：",
                    code = """
                        // 命令式（旧 View 体系）：描述"怎么改"
                        var count = 0
                        button.setOnClickListener {
                            count++
                            textView.text = "已点击 ${'$'}count 次"   // 手动找控件、手动赋值
                        }

                        // 声明式（Compose）：描述"长什么样"
                        @Composable
                        fun Counter() {
                            var count by remember { mutableStateOf(0) }  // 状态
                            Button(onClick = { count++ }) {               // 事件只改状态
                                Text("已点击 ${'$'}count 次")              // 界面永远是状态的函数
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 类比 C++ 的"数据驱动重绘"：就像 Qt 里你自己 `connect` 信号到 `update()`，而 Compose 把"监听变化 + 局部重绘"全部自动化了。
                · 命令式 UI ≈ 手写汇编，声明式 UI ≈ 高级语言：你声明意图，编译器（框架）负责最优执行。
                · `UI = f(state)` 很像纯函数思想：同样的输入（state）永远得到同样的输出（界面），没有隐藏的可变中间状态。
                · 陷阱：**不要在 Composable 里缓存"上一次的界面值"去做增量修改**——那是命令式思维，交给 Compose 重算即可。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "声明式 UI 的核心公式是？",
                    options = listOf(
                        "UI = f(state)",
                        "UI = new View()",
                        "UI = XML + findViewById",
                        "State = f(UI)",
                    ),
                    correctIndex = 0,
                    explanation = "声明式 UI 把界面看作状态的函数：状态变，Compose 自动重组刷新界面。",
                ),
                QuizQuestion(
                    question = "状态变化后，界面更新由谁完成？",
                    options = listOf(
                        "开发者手动调用 setText/invalidate",
                        "Compose 自动重组相关部分",
                        "需要重启 Activity",
                        "由 Handler 发消息延迟刷新",
                    ),
                    correctIndex = 1,
                    explanation = "Compose 追踪状态读取位置，状态变化时只重组读取了该状态的 Composable，无需手动刷新。",
                ),
                QuizQuestion(
                    question = "以下哪种说法符合“单一事实来源”原则？",
                    options = listOf(
                        "界面状态同时存在 TextView 与成员变量里",
                        "界面状态只保存在一处，界面由它派生",
                        "每个控件各自维护自己的状态副本",
                        "状态保存在 XML 属性里",
                    ),
                    correctIndex = 1,
                    explanation = "状态只存一处（如 mutableStateOf），界面是它的投影，避免了多处赋值不同步的经典 bug。",
                ),
                QuizQuestion(
                    question = "重组（Recomposition）指的是？",
                    options = listOf(
                        "重新编译整个 App",
                        "重新执行读取了已变化状态的 Composable 函数并刷新界面",
                        "重建整个 Activity",
                        "重新加载 XML 布局文件",
                    ),
                    correctIndex = 1,
                    explanation = "重组是 Compose 重新执行受状态变化影响的 Composable 函数、把输出与界面差异应用上去的过程。",
                ),
            ),
        ),
        Lesson(
            id = "compose-02",
            title = "Composable 函数",
            concept = """
                Compose 界面的基本单位是 **Composable 函数**：一个标注 `@Composable` 的普通 Kotlin 函数，接收参数、输出界面描述。

                ## 三大特点

                - 函数名用**名词**（如 `Greeting` 而不是 `ShowGreeting`）
                - 可以有参数；**没有返回值**——它"发射"UI 而不是构造对象

                ## @Composable 不是魔法

                - 它只是一个标记，告诉编译器"这个函数会被 Compose 运行时调度调用，**可能被反复执行（重组），也可能被跳过**"
                - 所以 Composable 里**不要做耗时操作、不要有副作用**（如写文件、改全局变量）

                ## 调用约束与 @Preview 预览

                - Composable **只能被其他 Composable 调用**，这个约束靠编译器强制——类似 suspend 函数只能在 suspend 上下文里调用
                - 在函数上加 `@Preview` 注解，Android Studio 能**不装 App 直接渲染**这个函数的效果，改一改立刻看到——这是 Compose 开发效率的最大来源
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "一个带参数的 Composable 函数，以及它的组合调用：",
                    code = """
                        @Composable
                        fun Greeting(name: String) {          // 名词命名，参数驱动
                            Text(text = "你好，${'$'}name！")
                        }

                        @Composable
                        fun WelcomeScreen() {
                            Column {                          // Composable 里组合其他 Composable
                                Greeting("Kotlin")
                                Greeting("Compose")
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "用 @Preview 在 IDE 里直接预览：",
                    code = """
                        @Preview(showBackground = true, name = "浅色预览")
                        @Composable
                        fun GreetingPreview() {
                            KotlinLearnTheme {
                                Greeting("预览模式")     // 不启动模拟器也能看效果
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · `@Composable` 类似给函数打上"执行上下文"标记，和 C++20 用 `co_await`/`co_yield`/`co_return` 关键字标记可挂起函数的思路、C# 的 `async` 关键字同一脉络：编译器据此改写调用约定。
                · "只能被 Composable 调用"的检查在 C++ 里要靠 code review，在 Kotlin 里编译器直接报错——**语言级约束更可靠**。
                · Composable 函数可能被调用 N 次、以任意顺序调用，**不要假设"调用次数 = 界面刷新次数"**，这和 C++ 里"函数调用一次执行一次"的直觉相反。
                · UI 即函数调用树：`Column { Text() }` ≈ 构造一棵节点树，但节点对象由框架复用，你不持有引用。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Composable 函数的命名惯例是？",
                    options = listOf(
                        "动词开头，如 showGreeting()",
                        "名词，如 Greeting()",
                        "必须以 on 开头",
                        "必须与 XML 文件同名",
                    ),
                    correctIndex = 1,
                    explanation = "官方指南：Composable 用名词命名，因为它描述“界面是什么”，而不是“执行什么动作”。",
                ),
                QuizQuestion(
                    question = "在 Composable 函数里做下面哪件事是错误的？",
                    options = listOf(
                        "调用其他 Composable",
                        "根据参数用 if/for 生成不同界面",
                        "在其中执行网络请求等耗时副作用",
                        "使用 Modifier 定制布局",
                    ),
                    correctIndex = 2,
                    explanation = "Composable 会被频繁重组，副作用（网络、写文件）可能执行多次或被跳过，应放到 ViewModel/LaunchedEffect。",
                ),
                QuizQuestion(
                    question = "@Preview 注解的作用是？",
                    options = listOf(
                        "让函数在 Release 包中不可见",
                        "在 Android Studio 中不运行 App 即可渲染该函数的 UI",
                        "自动生成单元测试",
                        "开启混淆保护",
                    ),
                    correctIndex = 1,
                    explanation = "@Preview 让 IDE 直接渲染 Composable，可多配置预览（深浅色、字号），大幅缩短反馈循环。",
                ),
                QuizQuestion(
                    question = "为什么 Composable 里要用 if/for 而不是手动增删控件？",
                    options = listOf(
                        "语法限制，没有别的写法",
                        "界面由数据驱动，条件与循环直接表达“什么状态下有什么界面”",
                        "if/for 运行更快",
                        "手动增删控件在 Kotlin 里做不到",
                    ),
                    correctIndex = 1,
                    explanation = "声明式思想：列表数据多少就循环渲染多少，条件成立才渲染，框架负责对应增删节点。",
                ),
            ),
        ),
        Lesson(
            id = "compose-03",
            title = "常用组件：Text/Button/Column/Row",
            concept = """
                Compose 没有预置的"几十种控件"，只有**少量原子组件 + Modifier（修饰符）**组合出一切，最常用的是四件套。

                ## 最常用四件套

                - `Text` 显示文字
                - `Button` 触发点击——内容也是 Composable，所以按钮里能放**图标 + 文字**
                - `Column` 纵向排列子项；`Row` 横向排列子项

                ## Modifier：万能参数

                - `padding`、`fillMaxSize`、`background`、`clickable`、`weight`……按**链式顺序生效**，顺序不同结果不同
                - `.padding(8.dp).background(红)` 和 `.background(红).padding(8.dp)` 画出来的**大小不一样**

                ## 剩余空间、对齐与默认样式

                - 子项可以用 `weight(1f)` 按"**剩余空间的比例**"分配，类似前端的 flex-grow；对齐方式用 `verticalAlignment` / `horizontalAlignment` 参数指定
                - 这些组件的默认样式来自 Material 3（见本模块第 7 课）：`Button` 自带**圆角、涟漪反馈与主题色**，你几乎不用自己画
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "四件套组合出一个简单的登录卡片：",
                    code = """
                        @Composable
                        fun LoginCard() {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),                     // 外边距
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("欢迎回来", style = MaterialTheme.typography.headlineSmall)
                                Button(
                                    onClick = { /* 处理登录 */ },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                ) {
                                    Text("登录")                          // Button 的内容也是 Composable
                                }
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "Row + weight 实现等分与 Modifier 顺序差异：",
                    code = """
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("左", modifier = Modifier.weight(1f))   // 占 1 份剩余空间
                            Text("右", modifier = Modifier.weight(2f))   // 占 2 份，宽度是左的两倍
                        }

                        // Modifier 顺序影响效果：
                        Box(Modifier.padding(8.dp).background(Color.Red))    // 先留边再涂色：红色块小
                        Box(Modifier.background(Color.Red).padding(8.dp))    // 先涂色再留边：红色块大
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Modifier 链 ≈ 管道式变换：像 C++ 里 `std::views::filter | transform` 的顺序敏感组合，前后互换结果不同。
                · `Column`/`Row` ≈ Qt 的 `QVBoxLayout`/`QHBoxLayout`，但"布局参数"（weight、对齐）直接是子项的 Modifier，不另设 LayoutParams 对象。
                · **没有控件引用可拿**：你不能像 C++/Qt 那样 `button->setText()`——想改变外观，改传入的数据，让重组去刷新。
                · 原子组件 + 组合的思路 ≈ 标准库容器 + 算法：少量正交原语，胜过庞大但僵硬的类库。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Column 和 Row 的排列方向分别是？",
                    options = listOf(
                        "Row 纵向，Column 横向",
                        "Column 纵向，Row 横向",
                        "两者都由参数自由指定",
                        "Column 横向，Row 由系统决定",
                    ),
                    correctIndex = 1,
                    explanation = "Column 纵向（列）排列子项，Row 横向（行）排列子项；需要二维自由布局用 Box。",
                ),
                QuizQuestion(
                    question = "Modifier.padding(8.dp).background(Color.Red) 与 background(Color.Red).padding(8.dp) 的区别是？",
                    options = listOf(
                        "完全相同",
                        "前者红色区域不含内边距，后者红色区域包含内边距",
                        "后者会编译报错",
                        "前者内边距失效",
                    ),
                    correctIndex = 1,
                    explanation = "Modifier 按链式顺序包裹：先 background 再 padding 会让背景延伸到 padding 区域内。",
                ),
                QuizQuestion(
                    question = "想让 Row 中两个子项按 1:2 分配剩余宽度，应该用？",
                    options = listOf(
                        "分别设置 width(100.dp)",
                        "Modifier.weight(1f) 与 Modifier.weight(2f)",
                        "horizontalAlignment = 1f/2f",
                        "用 Spacer 手动占位",
                    ),
                    correctIndex = 1,
                    explanation = "weight 按比例分配剩余空间，类似 flex-grow，是自适应布局的首选。",
                ),
                QuizQuestion(
                    question = "关于 Button，下列说法正确的是？",
                    options = listOf(
                        "只能显示文字，不能包含图标",
                        "点击回调参数是 onDoubleClick",
                        "内容是 Composable lambda，可放任意组合；点击回调是 onClick",
                        "必须配合 XML 布局使用",
                    ),
                    correctIndex = 2,
                    explanation = "Button(onClick = { ... }) { ... }：尾 lambda 是内容槽位，可以放 Text、Icon 等任意 Composable。",
                ),
            ),
        ),
        Lesson(
            id = "compose-04",
            title = "状态与重组",
            concept = """
                Compose 里"会变的界面数据"必须是 `State<T>`——界面由可观察状态驱动。

                ## 什么是状态

                - 用 `mutableStateOf(0)` 创建**可观察状态**，再包一层 `remember` 让它在重组之间**记住上一次的值**
                - 没有 `remember`，每次重组都会重新初始化为 0，界面看起来"点了没反应"
                - 惯用写法是配 delegate：`var count by remember { mutableStateOf(0) }`，之后读写 `count` 就像普通变量，但**框架在暗中观察**

                ## 什么是重组

                - 只有**读取了这个 State** 的 Composable 会在状态变化时重组，其他部分不受影响
                - Compose 在**编译期插桩、运行期记录**谁读了哪个状态，实现**最小范围刷新**
                - `remember` 有个变体 `rememberSaveable`：能在 Activity 因旋转等配置变更被销毁重建时**自动保存恢复**（内部走 `Bundle`）；`remember` 在重建后会丢失

                ## 常见误区

                - 忘写 `remember` 导致状态不生效
                - 在 if/for 中直接 `remember` 导致**位置混乱**（应使用 `key`）
                - 把可变状态对象当普通对象传递导致**观察失效**——始终用 `by` 委托或把 State 往上传
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "remember + mutableStateOf 的最小计数器（本模块核心套路）：",
                    code = """
                        @Composable
                        fun Counter() {
                            var count by remember { mutableStateOf(0) }   // 可观察 + 跨重组记忆
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("当前：${'$'}count")                  // 这里读取了 count
                                Button(onClick = { count++ }) {           // 这里只写不读
                                    Text("加一")
                                }
                            }
                            // count 变化 → 只重组 Text 所在部分，Button 不会被重算
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "remember 与 rememberSaveable 在旋转后的差异：",
                    code = """
                        var a by remember { mutableStateOf("") }          // 旋转后清空
                        var b by rememberSaveable { mutableStateOf("") }  // 旋转后保留
                        // rememberSaveable 要求值能放进 Bundle（基本类型/String/Parcelable 等）
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · `mutableStateOf` ≈ 一个自带观察者模式的变量：赋值即"发布变更"，读取处自动"订阅"——C++ 里你得手写 `std::function` 回调或信号槽才能做到。
                · `remember` ≈ 框架维护的静态局部变量：按"调用位置"作为 key 存储上次的值，与 C++ `static` 局部变量按定义点唯一化的机制神似。
                · `by` 委托 ≈ 运算符重载：`getValue`/`setValue` 让 State 用起来和普通变量无异，本质还是对象的 get/set。
                · 陷阱直觉迁移：C++ 里"局部变量每次进入作用域都重新初始化"，`remember` 恰恰相反——**它是为跨重组持久而生**，两者别混淆。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "var count by mutableStateOf(0) 不加 remember 会怎样？",
                    options = listOf(
                        "编译不通过",
                        "每次重组都被重置为 0，点击看起来无效",
                        "状态自动持久化到磁盘",
                        "会引发死循环重组",
                    ),
                    correctIndex = 1,
                    explanation = "没有 remember，每次重组都重新创建 State 并初始化为 0，改动随即被覆盖。",
                ),
                QuizQuestion(
                    question = "某 Composable 读取了 state A，另一个没有读取。A 变化时谁会重组？",
                    options = listOf(
                        "整个 Activity 全部重组",
                        "两个都重组",
                        "只有读取了 A 的那个重组",
                        "都不重组",
                    ),
                    correctIndex = 2,
                    explanation = "Compose 按读取范围做最小化重组：未读取 A 的 Composable 被跳过，这是它性能好的关键。",
                ),
                QuizQuestion(
                    question = "想让一个输入框内容在屏幕旋转后仍然保留，应该用？",
                    options = listOf(
                        "remember",
                        "rememberSaveable",
                        "全局变量",
                        "静态常量",
                    ),
                    correctIndex = 1,
                    explanation = "rememberSaveable 通过 Bundle 在配置变更后自动恢复；remember 会在重建后丢失。",
                ),
                QuizQuestion(
                    question = "mutableStateOf(0) 返回的对象类型是？",
                    options = listOf(
                        "MutableList<Int>",
                        "MutableState<Int>",
                        "Int 包装的 final 类，不可写",
                        "LiveData<Int>",
                    ),
                    correctIndex = 1,
                    explanation = "mutableStateOf 返回 MutableState<T>，其 value 可写且被观察；by 委托只是省去 .value 的语法糖。",
                ),
            ),
        ),
        Lesson(
            id = "compose-05",
            title = "状态提升",
            concept = """
                状态提升（State Hoisting）是 Compose **最重要的架构模式**：把某个组件私有的状态"上提"到它的调用方，组件本身变成**无状态（Stateless）**。

                ## 惯用签名：三件套

                - `value: T`——当前值传入
                - `onValueChange: (T) -> Unit`——回调请求改值
                - 以及可选的其他事件回调

                组件内部不持有状态，只展示 `value`、把用户操作通过回调上报——**数据向下流、事件向上流**，这就是"单向数据流"（UDF）。

                ## 为什么要提升状态

                - 状态上提后可以有**多个使用者共享同一份事实来源**（如输入框与提交按钮都依赖同一文本）
                - 无状态组件更容易**预览、测试和复用**
                - 状态的生命周期与"拥有它的层级"一致，**避免泄漏**

                ## 经验法则与本 App 实践

                - 写到 `remember` 之前先问"**谁需要这个状态**"：只有自己用就留在原地；多个兄弟组件要用，就提到它们**最近的公共父级**
                - 本 App 的屏幕层就是典型：`QuizScreen` 持有"当前选择"，答题结果通过回调交给上层与 ProgressStore 打交道，屏幕自身**不关心持久化细节**
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "把输入状态从子组件提升到父级，实现共享：",
                    code = """
                        // 无状态（Stateless）子组件：只收值、报事件
                        @Composable
                        fun NameInput(
                            name: String,
                            onNameChange: (String) -> Unit,
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = onNameChange,
                                label = { Text("姓名") },
                            )
                        }

                        // 有状态（Stateful）父组件：状态唯一事实来源
                        @Composable
                        fun ProfileScreen() {
                            var name by remember { mutableStateOf("") }
                            Column {
                                NameInput(name = name, onNameChange = { name = it })
                                Text("你好，${'$'}name")   // 与输入框共享同一状态
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 状态提升 ≈ 依赖注入：组件不自己 `new` 状态，由上层"传进来"，测试时可以随便造数据。
                · 单向数据流 ≈ C++ 的"const 引用入参 + 回调出参"接口设计：数据只读向下，修改请求通过回调向上，**禁止子组件反向篡改父级数据**。
                · 无状态组件 ≈ 纯函数：相同输入必得相同输出，`@Preview` 和单元测试都因此可行；有状态组件 ≈ 带内部可变状态的对象，**慎用、且尽量薄**。
                · 与 MVC 里 View 直接改 Model 的老写法对比：UDF 把"谁能改状态"限制在单一层级，和 Rust 所有权思想一样，是在**消灭"到处可写"的混乱**。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "状态提升后，子组件通常变成什么样的组件？",
                    options = listOf(
                        "有状态组件",
                        "无状态组件",
                        "单例组件",
                        "抽象类",
                    ),
                    correctIndex = 1,
                    explanation = "状态上提到父级后，子组件只接收 value、发出 onValueChange 回调，成为无状态组件，更易复用与测试。",
                ),
                QuizQuestion(
                    question = "状态提升惯用的组件签名三件套是？",
                    options = listOf(
                        "context、attribute、style",
                        "value、onValueChange、其他事件回调",
                        "getter、setter、listener",
                        "state、effect、reducer",
                    ),
                    correctIndex = 1,
                    explanation = "value 传入当前值，onValueChange 上报修改请求，必要时附加额外事件回调，构成标准 UDF 接口。",
                ),
                QuizQuestion(
                    question = "两个兄弟组件需要读写同一份数据，状态应放在哪里？",
                    options = listOf(
                        "各自组件里各存一份",
                        "它们最近的公共父级",
                        "Application 全局变量",
                        "XML 资源里",
                    ),
                    correctIndex = 1,
                    explanation = "提升到最近的公共父级，作为唯一事实来源分发给两个子组件，避免双份状态不同步。",
                ),
                QuizQuestion(
                    question = "关于单向数据流（UDF），下列说法正确的是？",
                    options = listOf(
                        "子组件可以直接修改父组件持有的状态变量",
                        "数据向下传递，事件向上回调",
                        "数据向上传递，事件向下回调",
                        "状态必须在每个组件内自建自销",
                    ),
                    correctIndex = 1,
                    explanation = "UDF：状态（数据）沿层级向下分发，用户操作以事件/回调形式向上请求变更，修改权始终在状态拥有者手里。",
                ),
            ),
        ),
        Lesson(
            id = "compose-06",
            title = "LazyColumn 列表",
            concept = """
                展示长列表时**不能用 for 循环套 Column**——那样所有条目会一次性全部创建和布局，1000 条数据就卡死了。

                ## LazyColumn：按需组合

                - 只组合并绘制"**当前可见 + 少量缓存**"的条目，滚到哪里建到哪里，滚出屏幕的条目被回收
                - 性能与 RecyclerView **同级**，而代码量只有几行

                ## 基本用法与 key 的收益

                - `items(list) { item -> ... }` 逐条渲染；`itemsIndexed` 提供下标；`item { }` 插入独立的头部/尾部条目
                - 给 items 传 `key = { it.id }`（稳定且唯一的 ID）能带来两个收益：滚动中**插入/删除条目时动画正确**、重组时**正确复用与记住状态**

                ## 常用配套与状态配合

                - `Arrangement` 控制主轴排列（`spacedBy(8.dp)` 设条目间距）、`contentPadding` 给列表整体留边；反向的 `LazyRow` 同理，横向滚动
                - 典型场景：列表数据来自 ViewModel/上层传入的 List，条目点击**通过回调向上报告 ID**——LazyColumn 自身**保持无状态**，正好复习上一课的状态提升
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "带 key、间距与点击回调的课程列表（本 App 真实用法）：",
                    code = """
                        data class Course(val id: String, val title: String)

                        @Composable
                        fun CourseList(
                            courses: List<Course>,
                            onCourseClick: (String) -> Unit,
                        ) {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(courses, key = { it.id }) { course ->
                                    ListItem(
                                        headlineContent = { Text(course.title) },
                                        modifier = Modifier.clickable {
                                            onCourseClick(course.id)   // 事件向上
                                        },
                                    )
                                }
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · LazyColumn ≈ 虚拟列表/视图回收：和 Qt 的 `QListView` + Model/Delegate、Web 前端的 virtual scroll 是同一性能思路——**只为可见项买单**。
                · `key = { it.id }` ≈ `std::map` 的稳定键：没有 key，框架只能按"位置"对号入座，插入删除会导致状态串位（第 3 行的勾选跑到第 4 行）。
                · `items(courses) { ... }` ≈ 范围 for 循环 + 按需求值：表面上像在遍历生成 UI，实际是**惰性流**。
                · 传 `List` 进 LazyColumn 而 LazyColumn 不改它 ≈ `const std::vector&`：数据不可变地向下流，修改意图走回调。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "为什么长列表要用 LazyColumn 而不是 Column + for 循环？",
                    options = listOf(
                        "LazyColumn 支持更多动画",
                        "LazyColumn 只组合可见条目，数据多时不会卡顿/内存暴涨",
                        "Column 不能嵌套 Text",
                        "LazyColumn 会自动联网加载数据",
                    ),
                    correctIndex = 1,
                    explanation = "Column 会一次性组合全部子项；LazyColumn 按需组合回收可见项，性能与数据量解耦。",
                ),
                QuizQuestion(
                    question = "给 items 传稳定 key 的主要好处是？",
                    options = listOf(
                        "让列表滚动速度翻倍",
                        "插入/删除时状态与动画按条目身份正确对应，避免串位",
                        "key 是必填参数，不传无法编译",
                        "自动去重",
                    ),
                    correctIndex = 1,
                    explanation = "key 标识条目身份：增删后框架按 key 匹配，条目的 remember 状态、动画归属都保持正确。",
                ),
                QuizQuestion(
                    question = "在 LazyColumn 中插入一个“列表头部”，应使用？",
                    options = listOf(
                        "header = { ... } 参数",
                        "item { ... } 独立条目",
                        "在 LazyColumn 外再套一个 Column",
                        "title 参数",
                    ),
                    correctIndex = 1,
                    explanation = "LazyColumn 里用 item { } 添加独立条目（如头部/底部），items 处理列表数据。",
                ),
                QuizQuestion(
                    question = "条目间距 8.dp 用哪个参数设置？",
                    options = listOf(
                        "verticalArrangement = Arrangement.spacedBy(8.dp)",
                        "itemSpacing = 8.dp",
                        "Modifier.margin(8.dp)",
                        "padding 会自动继承",
                    ),
                    correctIndex = 0,
                    explanation = "Arrangement.spacedBy 控制子项间距；Compose 里没有 margin，边距统一用 padding 表达。",
                ),
            ),
        ),
        Lesson(
            id = "compose-07",
            title = "Material 3 主题",
            concept = """
                Material 3（Material You）是 Google 当前设计规范，Compose 里对应 `androidx.compose.material3` 库。

                ## 主题如何生效

                - 核心是 `MaterialTheme(colorScheme, typography, shapes) { ... }`：把颜色、字体、形状包进 **CompositionLocal**，向下传给所有组件
                - `Button`、`Text` 等**自动取用**，你不用手动传色值

                ## colorScheme 与 onXxx 命名

                - 颜色不再是 primary/secondary 两个词，而是一整套 **colorScheme**：`primary`、`onPrimary`（叠在 primary 上的内容色）、`surface`、`background`、`error`……
                - "onXxx"命名表示"**画在 Xxx 之上的颜色**"，对比度已由规范保证

                ## 动态取色与用法纪律

                - **动态取色（Dynamic Color）**是 Android 12+ 的亮点：用 `dynamicLightColorScheme(context)` 从用户壁纸提取色板，App 自动融入系统配色
                - 本 App 的 `KotlinLearnTheme` 就按"Android 12+ 且允许时用动态色，否则回落到自定义深浅色方案"来实现
                - 用法纪律：界面代码永远写 `MaterialTheme.colorScheme.primary`、`MaterialTheme.typography.titleMedium`，**绝不硬编码 `Color(0xFFxxxxxx)`**——这样深色模式、动态色、品牌换肤才是一条命令的事
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "本 App 的 Theme 函数（简化自 ui/theme/Theme.kt）：",
                    code = """
                        @Composable
                        fun KotlinLearnTheme(
                            darkTheme: Boolean = isSystemInDarkTheme(),
                            dynamicColor: Boolean = true,
                            content: @Composable () -> Unit,
                        ) {
                            val colorScheme = when {
                                // Android 12+ 从壁纸取色
                                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                                    if (darkTheme) dynamicDarkColorScheme(LocalContext.current)
                                    else dynamicLightColorScheme(LocalContext.current)
                                darkTheme -> DarkColorScheme
                                else -> LightColorScheme
                            }
                            MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "组件自动消费主题，文字颜色永远与背景保持对比：",
                    code = """
                        @Composable
                        fun ScoreBadge(score: Int) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = "得分 ${'$'}score",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · `MaterialTheme` ≈ 编译期注入的全局配置：像把一份 const 配置结构体放进"作用域"，子树内随便读；CompositionLocal 相当于隐式上下文参数。
                · colorScheme 的 onXxx ≈ 前景/背景成对出现的约束：**规范替你保证了对比度**，比 C++ 里手动算 RGB 亮度靠谱。
                · 硬编码色值 ≈ 魔法数字：换主题/深色模式时全是雷，**主题令牌（colorScheme.xxx）就是消灭魔法数字的命名常量**。
                · 动态取色没有 C++ 对应物——它是"运行期读系统资源生成配置"，概念上接近读取注册表/环境变量来初始化全局配置。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "MaterialTheme.colorScheme.onPrimary 表示？",
                    options = listOf(
                        "主色本身",
                        "画在 primary 之上的内容颜色",
                        "关闭主色",
                        "主色的透明版",
                    ),
                    correctIndex = 1,
                    explanation = "onXxx 系列 = 叠放在 Xxx 色上面的内容色（文字/图标），规范保证两者对比度达标。",
                ),
                QuizQuestion(
                    question = "Android 12+ 上的动态取色（Dynamic Color）来源是？",
                    options = listOf(
                        "应用图标颜色",
                        "用户当前壁纸自动提取的色板",
                        "随机生成",
                        "系统主题 XML 文件",
                    ),
                    correctIndex = 1,
                    explanation = "dynamicLightColorScheme/dynamicDarkColorScheme 基于壁纸生成色板，让 App 配色随系统个性化。",
                ),
                QuizQuestion(
                    question = "界面里想用主题标题字体，正确写法是？",
                    options = listOf(
                        "Text(fontSize = 20.sp)",
                        "Text(style = MaterialTheme.typography.titleLarge)",
                        "Text(font = Theme.title)",
                        "必须自定义 TextStyle",
                    ),
                    correctIndex = 1,
                    explanation = "typography 定义整套字号/字重令牌，组件引用令牌即可随主题联动，不要散落硬编码字号。",
                ),
                QuizQuestion(
                    question = "为什么不能在 Composable 里硬编码 Color(0xFF2196F3)？",
                    options = listOf(
                        "编译报错",
                        "深色模式与主题切换下无法适配，颜色不会随 colorScheme 联动",
                        "性能太差",
                        "颜色值必须存 XML",
                    ),
                    correctIndex = 1,
                    explanation = "硬编码色值绕过主题，深浅色切换、动态取色时会出现对比度问题；应引用 colorScheme 令牌。",
                ),
            ),
        ),
        Lesson(
            id = "compose-08",
            title = "导航",
            concept = """
                Compose 时代的页面跳转用官方库 `androidx.navigation:navigation-compose`（当前稳定线 2.9.x）。

                ## 三个核心角色

                - `NavHost`：承载页面的**容器**
                - `NavController`：控制器，`navigate`/`popBackStack` 都由它**发号施令**
                - 一组"**路由字符串 → Composable 屏幕**"的映射

                ## 最小骨架

                - `rememberNavController()` 创建控制器
                - `NavHost(navController, startDestination = "home")` 声明起始页
                - `composable("route") { XxxScreen() }` 注册路由
                - 跳转用 `navController.navigate("route")`，返回用 `navController.popBackStack()` 或**系统返回键**

                ## 传参与栈状态

                - 传参用占位路由：`composable("lesson/{lessonId}") { backStackEntry -> val id = backStackEntry.arguments?.getString("lessonId") }`，跳转时 `navigate("lesson/compose-01")`——本 App 就是这么把课时 ID 从课程列表传到课程详情页的
                - 每个路由栈条目是一个**独立 backStackEntry**，其上的 `rememberSaveable` 状态自动保存——切页再返回，输入内容还在
                - 复杂参数（对象）**不要塞路由字符串**，放到共享的 ViewModel 或轻量序列化即可
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "导航最小骨架（路由定义 + 跳转 + 传参）：",
                    code = """
                        dependencies {
                            implementation("androidx.navigation:navigation-compose:2.9.5")
                        }

                        @Composable
                        fun AppNav() {
                            val navController = rememberNavController()
                            NavHost(navController, startDestination = "home") {
                                composable("home") {
                                    HomeScreen(
                                        onOpenLesson = { id -> navController.navigate("lesson/${'$'}id") }
                                    )
                                }
                                composable("lesson/{lessonId}") { entry ->
                                    val lessonId = entry.arguments?.getString("lessonId")
                                    LessonScreen(lessonId = lessonId)
                                }
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · `NavController` + 路由字符串 ≈ 命令行参数启动新进程：页面之间不持有彼此引用，只约定"路由协议"，**耦合度比 C++ 窗口类互相 new 低得多**。
                · back stack ≈ 栈数据结构：`navigate` 是 push，`popBackStack` 是 pop，系统返回键默认 pop——和函数调用栈一个道理。
                · 路由传参 ≈ `printf` 风格的格式化 + 解析：占位符 `{lessonId}` 与 `getString` 解析必须配套写对，**类型不匹配会在运行期炸**，类似 `sscanf` 的脆弱性。
                · 每个路由是独立 backStackEntry ≈ 每层栈帧有独立局部状态：`rememberSaveable` 绑定栈帧，**出栈即销毁**。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Compose 导航三要素是？",
                    options = listOf(
                        "Activity、Fragment、Intent",
                        "NavHost、NavController、路由映射",
                        "XML、Adapter、ViewHolder",
                        "Scaffold、TopBar、Drawer",
                    ),
                    correctIndex = 1,
                    explanation = "NavHost 是容器，NavController 负责跳转与回退，composable(\"route\") 声明路由到屏幕的映射。",
                ),
                QuizQuestion(
                    question = "路由 \"lesson/{lessonId}\" 中 {lessonId} 的作用是？",
                    options = listOf(
                        "注释，无实际意义",
                        "路径参数占位符，跳转时替换为实际值并可在目标页读取",
                        "可选参数，必须为空",
                        "表示该页面只能打开一次",
                    ),
                    correctIndex = 1,
                    explanation = "占位符定义路径参数：navigate(\"lesson/compose-01\") 后，目标页从 backStackEntry.arguments 取出 \"compose-01\"。",
                ),
                QuizQuestion(
                    question = "返回上一页应调用 NavController 的哪个方法？",
                    options = listOf(
                        "navigateBack()",
                        "popBackStack()",
                        "finish()",
                        "dispose()",
                    ),
                    correctIndex = 1,
                    explanation = "popBackStack() 弹出当前条目回到上一路由；系统返回键默认行为也是它。",
                ),
                QuizQuestion(
                    question = "关于跨页面传参，正确做法是？",
                    options = listOf(
                        "把复杂对象 toString 后塞进路由",
                        "简单值用路由参数；复杂对象放共享 ViewModel 或可序列化存储",
                        "用全局静态变量最方便",
                        "Compose 不支持传参",
                    ),
                    correctIndex = 1,
                    explanation = "路由字符串适合简短的 ID 类参数；复杂对象应放 ViewModel 等共享层，避免解析脆弱与体积超限。",
                ),
            ),
        ),
        Lesson(
            id = "compose-09",
            title = "本 App 源码剖析",
            concept = """
                学完组件、状态、导航，我们把镜头转向这个学习 App 本身——它就是一份**按官方分层组织的 Compose 工程**。

                ## 包结构：按职责四层

                - `data/model` 定义**纯数据类**（Lesson、CodeExample、QuizQuestion、CourseModule）
                - `data/content` 存放**全部课程内容**（KotlinBasics、AndroidCore、ComposeBasics 与聚合入口 Curriculum）
                - `data/progress` 的 ProgressStore 负责**学习进度存取**
                - `ui` 层按 screens（Home/Course/Lesson/Quiz 四个屏幕）与 navigation（AppNav）组织界面

                ## 单向数据流实战

                - 用户在 `QuizScreen` 选择答案 → **事件回调向上** → 与 ProgressStore 交互记录进度 → **状态变化向下回流** → HomeScreen 的进度条重组刷新
                - UI 层**从不直接读写存储**，全部经由数据层的接口

                ## 关键文件对照

                - `data/model/Lesson.kt` 是"一节课"的定义（concept 正文 + 代码示例 + C++ 对比 + 随堂测验）
                - `data/content/Curriculum.kt` 把三个模块聚合成 modules 列表并提供 `findLesson(lessonId)` 查询——导航层拿课时 ID 一查，就能把数据喂给 LessonScreen
                - 另外值得注意的是 `ui/theme`：Theme.kt 的 `KotlinLearnTheme` 包住整个 `setContent` 内容树，Material 3 令牌从根向下生效——正是上一课"主题包住子树"的**实战版**
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "本工程的包结构（数据与 UI 分层）：",
                    code = """
                        app/src/main/java/com/kotlin/learn/
                        ├── MainActivity.kt          // 入口：setContent { KotlinLearnTheme { AppNav() } }
                        ├── data/
                        │   ├── model/Lesson.kt      // 纯数据：Lesson / CodeExample / QuizQuestion / CourseModule
                        │   ├── content/             // 课程内容
                        │   │   ├── KotlinBasics.kt  //   模块一（14 课）
                        │   │   ├── AndroidCore.kt   //   模块二（12 课）
                        │   │   ├── ComposeBasics.kt //   模块三（10 课）
                        │   │   └── Curriculum.kt    //   聚合：modules + findLesson + totalLessonCount
                        │   ├── logic/               // 校验器、判分等纯逻辑
                        │   └── progress/ProgressStore.kt  // 学习进度持久化
                        └── ui/
                            ├── theme/               // Material 3 主题
                            ├── screens/             // Home / Course / Lesson / Quiz 四个屏幕
                            └── navigation/AppNav.kt // NavHost 路由定义
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "聚合入口 Curriculum.kt（真实代码）：三个模块 + 按 ID 查找：",
                    code = """
                        object Curriculum {
                            val modules: List<CourseModule> = listOf(
                                KotlinBasics.moduleOf(KotlinBasics.lessons),
                                AndroidCore.moduleOf(AndroidCore.lessons),
                                ComposeBasics.moduleOf(ComposeBasics.lessons),
                            )

                            fun findLesson(lessonId: String): Lesson? =
                                modules.asSequence()
                                    .flatMap { it.lessons }
                                    .firstOrNull { it.id == lessonId }
                        }
                        // 导航层用法：navController.navigate("lesson/${'$'}{lesson.id}")
                        // LessonScreen 拿到 ID：Curriculum.findLesson(id) ?: 显示兜底页
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Kotlin 的 `package` 按目录分层，但不需要头文件：声明与实现都在 `.kt` 里，没有 `.h`/`.cpp` 分离——`import` 就是唯一的"#include"。
                · C++ 工程常见 include/、src/、lib/ 分层；本工程 data/（数据）、ui/（界面）、logic 的拆分思路一致：**接口与实现、数据与表现解耦**。
                · `object Curriculum` 单例 ≈ C++ 的静态类/命名空间级常量表：全局唯一、首次访问时才懒初始化（线程安全，无需手动加锁），用它做"课程注册表"正合适。
                · `data class` 的一行定义 ≈ C++ 里手写 struct + 构造函数 + 比较运算符 + 拷贝的全部样板——编译器替你生成了 equals/hashCode/copy。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "本 App 中，“全部课程内容的聚合入口”是哪个对象？",
                    options = listOf(
                        "ProgressStore",
                        "Curriculum",
                        "AppNav",
                        "KotlinLearnTheme",
                    ),
                    correctIndex = 1,
                    explanation = "data/content/Curriculum.kt 聚合三个模块的 lessons，并提供 findLesson(lessonId) 查询。",
                ),
                QuizQuestion(
                    question = "LessonScreen 要展示某节课，应如何拿到 Lesson 数据？",
                    options = listOf(
                        "重新 new 一个 Lesson",
                        "从导航参数取 lessonId，调用 Curriculum.findLesson(lessonId)，查不到则显示兜底页",
                        "把全部课程内容写死在屏幕文件里",
                        "从 XML 资源读取",
                    ),
                    correctIndex = 1,
                    explanation = "路由只传 ID，屏幕用 ID 查数据源；查不到返回 null，UI 显示兜底页——数据与导航彻底解耦。",
                ),
                QuizQuestion(
                    question = "下列哪一项属于本工程 data/progress 层的职责？",
                    options = listOf(
                        "定义 Lesson 数据类",
                        "保存与恢复学习进度（ProgressStore）",
                        "定义 NavHost 路由",
                        "提供 Material 3 配色",
                    ),
                    correctIndex = 1,
                    explanation = "ProgressStore 负责进度持久化；模型在 data/model，路由在 ui/navigation，主题在 ui/theme。",
                ),
                QuizQuestion(
                    question = "与 C++ 工程相比，本工程“没有头文件”意味着？",
                    options = listOf(
                        "无法跨文件调用",
                        "声明与实现同文件，import 即可引用其他文件的公开声明",
                        "必须把所有代码写进一个文件",
                        "编译速度必然更慢",
                    ),
                    correctIndex = 1,
                    explanation = "Kotlin 无 .h/.cpp 分离：类与函数的声明即定义，跨文件访问只需 import 对应包路径。",
                ),
            ),
        ),
        Lesson(
            id = "compose-10",
            title = "下一步学习路线",
            concept = """
                学到这里，你已能用 Compose 搭出完整的多页面 App：**声明式思维、状态管理、列表、主题、导航**。接下来按"优先级从高到低"给出路线。

                ## 三个优先级

                - **第一优先：副作用与协程**——`LaunchedEffect`、`rememberCoroutineScope`，以及 Kotlin 协程（`suspend`、`Flow`）；界面要**联网/读库**时绕不开它们，Flow 之于协程 ≈ 观察者流的官方实现，**值得系统学一遍**
                - **第二优先：架构组件**——`ViewModel`（**配置变更后状态不丢**、与 UI 分离）与 Repository 模式，再进阶到依赖注入（Hilt）；把本 App 的"数据层 + UI 层"重构为 ViewModel 驱动，是**极佳的练手项目**
                - **第三优先：性能与动画**——`derivedStateOf`、`remember` 的 key、immutability 与 Stability；动画从 `animate*AsState` 入门，再学 `AnimatedVisibility` 与转场

                ## 学习方法论

                - **以项目驱动**：给本 App 加功能（深色模式开关、搜索课程、错题本、用 DataStore 换掉内存进度存储），每加一个功能就把对应知识点学透
                - 配合官方文档 compose.google.cn 与 Android Basics with Compose 课程
                - C++ 背景是你的优势：把"**内存模型、生命周期、所有权**"的严谨直觉迁移过来，你比纯新手**少走一半弯路**
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "下一步会频繁遇到的副作用写法（先混个脸熟）：",
                    code = """
                        // LaunchedEffect：进入组合时启动协程，做一次性副作用
                        @Composable
                        fun CourseDetail(lessonId: String) {
                            var lesson by remember { mutableStateOf<Lesson?>(null) }
                            LaunchedEffect(lessonId) {          // key 变化时重新执行
                                lesson = Curriculum.findLesson(lessonId)   // 示意：实际可换成挂起加载
                            }
                            // rememberCoroutineScope：在回调里安全启动协程
                            val scope = rememberCoroutineScope()
                            Button(onClick = { scope.launch { /* 如：保存进度 */ } }) { Text("完成学习") }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 协程 `suspend` ≈ 有栈/无栈协程：比 C++20 coroutine 的 `co_await` 机制封装得更"语言级"，写起来像同步代码。
                · `Flow` ≈ ReactiveX / `std::observable` 流：**冷流惰性求值**，类似 C++ ranges 的 view——订阅才产生数据。
                · `ViewModel` ≈ 拥有比"栈帧（屏幕）"更长生命周期的对象：类似把数据从函数局部提升到进程级智能指针托管，但**框架定义了确切的作用域**。
                · 学路线图的忠告同 C++：**不要试图一次学完整个生态**——先让手上项目跑起来，再按需深挖，标准库哲学"先能用，再优化"在 Android 同样适用。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "在 Composable 的点击回调里执行挂起函数，应该用哪个 API 启动协程？",
                    options = listOf(
                        "LaunchedEffect",
                        "rememberCoroutineScope",
                        "GlobalScope.launch",
                        "Thread().start()",
                    ),
                    correctIndex = 1,
                    explanation = "rememberCoroutineScope 返回与组合绑定的作用域，供事件回调中 launch；LaunchedEffect 适合组合期自动执行的一次性副作用。",
                ),
                QuizQuestion(
                    question = "LaunchedEffect(lessonId) { ... } 中传入 lessonId 作为 key 的含义是？",
                    options = listOf(
                        "限制只执行一次",
                        "lessonId 变化时取消旧协程并重新执行",
                        "给协程命名",
                        "设置协程优先级",
                    ),
                    correctIndex = 1,
                    explanation = "key 变化会取消上一次的协程并重启 effect 块，保证副作用与最新参数一致。",
                ),
                QuizQuestion(
                    question = "本路线图中，推荐优先学习的内容是？",
                    options = listOf(
                        "动画与转场",
                        "副作用与协程（LaunchedEffect、suspend、Flow）",
                        "跨平台 Kotlin Multiplatform",
                        "NDK 与 JNI",
                    ),
                    correctIndex = 1,
                    explanation = "联网、读库都依赖协程与 Flow，是最先堵住“能搭界面但做不了功能”的一环，故列为第一优先。",
                ),
                QuizQuestion(
                    question = "关于后续学习方法，下列说法最符合课程建议？",
                    options = listOf(
                        "把所有 API 文档背完再写代码",
                        "以给本 App 加功能为驱动，按需深入学习对应知识点",
                        "只看视频不动手",
                        "直接学 Kotlin Multiplatform 跳过 Android 基础",
                    ),
                    correctIndex = 1,
                    explanation = "项目驱动 + 按需深挖是最有效的路径：每个真实功能都会逼你把一个知识点学透并立即用上。",
                ),
            ),
        ),
    )

    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "compose-basics",
        title = "Compose UI 入门",
        emoji = "🎨",
        lessons = lessons,
    )
}
