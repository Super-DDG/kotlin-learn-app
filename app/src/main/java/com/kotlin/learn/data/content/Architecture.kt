package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块六：架构（5 课），从副作用与状态保存到 ViewModel、StateFlow 与分层。 */
object Architecture {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "arch-01",
            title = "副作用 API",
            concept = """
                Composable 函数会被**反复调用**（每次重组都重跑一遍函数体），所以组合期间不能直接执行副作用——
                打印日志、发请求、注册监听器写进去，就会被重复触发。Android 提供了两个专门的「副作用 API」。

                ## LaunchedEffect：挂起式副作用

                - 进入组合后启动一个协程执行块内代码；离开组合自动取消
                - `LaunchedEffect(Unit)`：整个组合生命周期只跑一次——加载首屏数据的经典位置
                - `LaunchedEffect(key)`：key 变化时**先取消旧协程、再启动新的**——「id 变了重新加载」

                ## DisposableEffect：需要清理的副作用

                - 注册广播、监听器这类「用完必须注销」的副作用用它
                - 块内最后一行返回 `onDispose { ... }`：key 变化或离开组合时**先执行清理，再重新注册**
                - 与 LaunchedEffect 的差别就一点：它给你一个清理的出口

                ## 本课示例的说明

                LaunchedEffect 是 Compose API，纯 Kotlin 环境跑不了；示例用 `runBlocking + launch` 的等价形态演示
                同样的时机语义：协程启动不阻塞主流程、key 变化先取消旧的再启动新的。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "LaunchedEffect(Unit) 的时机语义（runBlocking 等价形态，真实运行）",
                    code = """
                        import kotlinx.coroutines.*

                        fun main() = runBlocking {
                            // LaunchedEffect(Unit) { delay(100); println("加载完成") } 的等价形态：
                            // 进入组合才启动副作用，且不阻塞组合本身
                            launch {
                                delay(100L)                      // 模拟挂起式加载
                                println("加载完成")
                            }
                            println("组合继续绘制，不等副作用")     // 副作用在协程里，组合不被卡住
                        }
                    """.trimIndent(),
                    expectedOutput = "组合继续绘制，不等副作用\n加载完成",
                ),
                CodeExample(
                    description = "key 变化重启副作用与 DisposableEffect 的清理顺序（runBlocking 等价形态，真实运行）",
                    code = """
                        import kotlinx.coroutines.*

                        // 复现 LaunchedEffect(key) 的重启语义与 DisposableEffect 的清理顺序
                        class FakeComposition(private val scope: CoroutineScope) {
                            private var job: Job? = null
                            private var lastKey: Int? = null

                            fun recompose(key: Int) {
                                if (key == lastKey) return                     // key 未变：什么都不做
                                lastKey = key
                                job?.let { old ->
                                    old.cancel()                               // 旧副作用被取消
                                    println("onDispose：清理上一次的注册")       // DisposableEffect 先清理
                                }
                                job = scope.launch {
                                    println("副作用启动：key=${'$'}key")
                                    delay(100L)                  // 模拟副作用仍在进行；被取消就到不了下一行
                                }
                            }
                        }

                        fun main() = runBlocking {
                            val composition = FakeComposition(this)
                            composition.recompose(1)     // 首次进入组合：启动
                            yield()                      // 让副作用协程跑到一半（挂在 delay 上）
                            composition.recompose(1)     // 重组但 key 没变：不重启
                            composition.recompose(2)     // key 变了：先取消进行中的旧副作用，再启动新的
                            composition.recompose(2)     // key 仍为 2：不重启
                        }
                    """.trimIndent(),
                    expectedOutput = "副作用启动：key=1\nonDispose：清理上一次的注册\n副作用启动：key=2",
                ),
            ),
            cppComparison = """
                · LaunchedEffect ≈ 把「完成后的回调」换成一个可取消的协程任务；C++ GUI 框架里对应投递异步任务，但取消要自己管 Job/handle。
                · DisposableEffect 的 onDispose ≈ RAII 的析构：资源获取与释放配对出现，注册与注销写在同一处，不会漏。
                · 「重组会重跑函数体」是声明式 UI 与 C++ 手写事件循环最大的思维差异：C++ 里 setup 代码只跑一次是常态，Compose 里必须显式说「跑一次」。
                · LaunchedEffect(Unit) 的「只跑一次」意图，类似 C++ 构造函数里的一次性初始化——但它是异步的、可取消的。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "LaunchedEffect(Unit) 的协程什么时候启动？",
                    options = listOf(
                        "组合期间同步执行，跑完才继续组合",
                        "组合完成后启动一次，不阻塞组合继续进行",
                        "每次重组都重新启动一次",
                        "进程启动时立即启动",
                    ),
                    correctIndex = 1,
                    explanation = "副作用在进入组合后以协程启动：组合本身不等它，照常绘制；整个组合生命周期只跑一次。",
                ),
                QuizQuestion(
                    question = "LaunchedEffect(userId) 中 userId 从 1 变成 2，会发生什么？",
                    options = listOf(
                        "两个协程并行跑，互不影响",
                        "什么都不发生，要手动重启",
                        "旧协程被取消，启动新协程",
                        "抛出异常要求重试",
                    ),
                    correctIndex = 2,
                    explanation = "key 变化 = 重启：先取消旧协程再启动新的，保证块内逻辑始终对应最新的 key——「id 变了重新加载」一行实现。",
                ),
                QuizQuestion(
                    question = "注册广播接收器这类「必须注销」的副作用该用哪个 API？",
                    options = listOf(
                        "remember",
                        "DisposableEffect（块内返回 onDispose 做注销）",
                        "SideEffect 就够了",
                        "直接写在 Composable 函数体里",
                    ),
                    correctIndex = 1,
                    explanation = "DisposableEffect 要求块内返回 onDispose：key 变化或离开组合时先执行清理再重新注册，注册与注销永不失配。",
                ),
                QuizQuestion(
                    question = "为什么不能把「发请求」直接写在 Composable 函数体里？",
                    options = listOf(
                        "请求太耗时，编译器不允许",
                        "重组会反复执行函数体，副作用被重复触发",
                        "Composable 里访问不了网络权限",
                        "可以写，只是风格不好",
                    ),
                    correctIndex = 1,
                    explanation = "Composable 是可重入的：每次重组都重跑。副作用必须交给 LaunchedEffect/DisposableEffect 这类有时机保证的 API。",
                ),
            ),
        ),
        Lesson(
            id = "arch-02",
            title = "rememberSaveable 与进程死亡恢复",
            concept = """
                状态能活多久，取决于它存在哪里。`remember` 只活在**内存里的组合**中：
                重组不丢，但离开组合（导航返回）、配置变更（旋转）、进程死亡（后台内存不足被系统杀掉）全都归零。

                ## 系统给的机会：Bundle

                - 进程可能被杀前，系统回调 `onSaveInstanceState`，让你把**小体积关键状态**写进 Bundle
                - Activity 重建时把 Bundle 还给你——这就是「进程死亡恢复」的全部机制
                - `rememberSaveable` = remember + 自动存取 Bundle：值一变自动写入，重建后自动恢复，用法与 remember 一模一样

                ## 两者的差别一览

                - 重组：remember 存活；离开组合/旋转/进程死亡：remember 归零
                - rememberSaveable：旋转、进程死亡都能恢复；只有「离开组合」才丢
                - 开关、输入框草稿、选中标签——这类小状态用 rememberSaveable 最省心

                ## 限制：Bundle 装不下一切

                - Bundle 只收**原始类型与 Parcelable/Serializable**；自定义类要先能「打包」，否则运行时崩溃
                - 大数据别塞 Bundle（有 TransactionTooLarge 上限风险）——需要持久化就交给模块五的 DataStore/Room
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "进程死亡恢复：remember 归零，rememberSaveable 从 Bundle 取回（真实运行）",
                    code = """
                        // 纯 Kotlin 复现进程死亡恢复：remember 在内存里，rememberSaveable 在 Bundle 里
                        class FakeBundle {
                            private val store = mutableMapOf<String, Any>()
                            operator fun set(key: String, value: Any) { store[key] = value }   // 只收原始类型/Parcelable
                            operator fun get(key: String): Any? = store[key]
                        }

                        fun main() {
                            val bundle = FakeBundle()
                            // 使用中：两个状态都有值
                            var rememberedCount = 3
                            var saveableCount = 7
                            bundle["saveable_count"] = saveableCount   // rememberSaveable：值一变自动写入 Bundle
                            // rememberedCount 没有 Bundle 份——remember 只活在内存里

                            // ——系统杀掉进程，随后重建 Activity——
                            rememberedCount = 0                                  // remember：内存随进程消失，重建归零
                            saveableCount = bundle["saveable_count"] as Int? ?: 0 // rememberSaveable：从 Bundle 恢复
                            println("remember 恢复后：${'$'}rememberedCount")
                            println("rememberSaveable 恢复后：${'$'}saveableCount")
                        }
                    """.trimIndent(),
                    expectedOutput = "remember 恢复后：0\nrememberSaveable 恢复后：7",
                ),
                CodeExample(
                    description = "Bundle 的类型限制：原始类型可进，未打包的自定义类型被拒（真实运行）",
                    code = """
                        data class Draft(val title: String)      // 自定义类型：未实现 Parcelable

                        // rememberSaveable 的底层规则：能写进 Bundle 的类型才能自动恢复
                        fun bundleSupports(value: Any): Boolean = when (value) {
                            is Int, is Long, is Float, is Boolean, is String -> true
                            is List<*> -> value.filterNotNull().all { bundleSupports(it) }   // 列表逐项检查
                            else -> false                                    // 真实场景：非 Parcelable 类型运行时直接崩溃
                        }

                        fun main() {
                            listOf(42, "搜索词", true, Draft("草稿"), listOf(1, 2))
                                .forEach { value ->
                                    println("${'$'}{value::class.simpleName} 可进 Bundle：${'$'}{bundleSupports(value)}")
                                }
                        }
                    """.trimIndent(),
                    expectedOutput = "Int 可进 Bundle：true\nString 可进 Bundle：true\nBoolean 可进 Bundle：true\nDraft 可进 Bundle：false\nArrayList 可进 Bundle：true",
                ),
            ),
            cppComparison = """
                · Bundle 自动存取 ≈ C++ 手写「序列化关键状态到文件、启动时读回」：Compose 把模板代码藏进了 rememberSaveable。
                · onSaveInstanceState 的时机 ≈ 收到「即将被终止」信号的 checkpoint：C++ 桌面程序要自己设计崩溃/退出恢复，Android 给了标准钩子。
                · Parcelable ≈ 可序列化的承诺：C++ 里等价于为类型手写 serialize/deserialize，忘了处理某个字段就是静默丢数据。
                · Bundle 体积上限 ≈ Binder 事务 1MB 限制的投影：C++ 进程间传大数据走共享内存，Android 大数据也该走磁盘（DataStore/Room）。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "旋转屏幕（配置变更）后，remember 的值会怎样？",
                    options = listOf(
                        "原样保留",
                        "归零——组合随 Activity 销毁重建，内存状态丢失",
                        "自动复制到新组合",
                        "变成默认值类型初始量",
                    ),
                    correctIndex = 1,
                    explanation = "remember 只活在内存里的组合中；旋转销毁重建整棵组合树，remember 全部归零——这正是 rememberSaveable 存在的理由。",
                ),
                QuizQuestion(
                    question = "rememberSaveable 与 remember 的本质差别是？",
                    options = listOf(
                        "rememberSaveable 更快",
                        "rememberSaveable 能存任意对象",
                        "rememberSaveable 把值自动写进 Bundle，进程死亡后可恢复",
                        "remember 只能存字符串",
                    ),
                    correctIndex = 2,
                    explanation = "rememberSaveable = remember + Bundle 自动存取：值变化自动写入，重建自动恢复；用法一致，恢复能力天差地别。",
                ),
                QuizQuestion(
                    question = "下面哪个值能直接交给 rememberSaveable 并在进程死亡后恢复？",
                    options = listOf(
                        "一个未实现 Parcelable 的自定义数据类",
                        "Int 计数、Boolean 开关、String 搜索词",
                        "一个打开的数据库连接",
                        "一个包含匿名 lambda 的对象",
                    ),
                    correctIndex = 1,
                    explanation = "Bundle 只收原始类型与 Parcelable/Serializable；装不下的类型运行时崩溃，大数据应该走 DataStore/Room。",
                ),
                QuizQuestion(
                    question = "用户写了 2MB 的长文草稿，正确的保存方式是？",
                    options = listOf(
                        "塞进 rememberSaveable，反正会自动恢复",
                        "存进 DataStore/文件，Bundle 只放「有草稿」这样的标记",
                        "存进一个全局单例变量",
                        "旋转前弹窗提醒用户复制",
                    ),
                    correctIndex = 1,
                    explanation = "Bundle 有体积上限（TransactionTooLarge），大文本崩溃风险高；持久化走磁盘，进程死亡恢复才真正可靠。",
                ),
            ),
        ),
        Lesson(
            id = "arch-03",
            title = "ViewModel",
            concept = """
                上一课看到：旋转会让整棵组合树销毁重建。网络请求、列表数据这些**贵重状态**不能住在组合里——
                它们需要一个比组合活得久的家，这就是 ViewModel。

                ## ViewModel 的生命周期

                - 随屏幕（Activity/导航条目）创建，**配置变更间存活**，直到屏幕真正退出才清除
                - 旋转：Activity 重建，ViewModel 原封不动——请求不会重发、数据不会重拉
                - `viewModelScope`：与 ViewModel 同寿命的协程作用域，ViewModel 清除时自动取消全部协程

                ## 职责边界

                - 该做：持有 UI 状态、调度业务（调仓库的挂起函数）、把结果整理成 UI 能直接显示的形态
                - 不该做：持有 Context/View 引用（会泄漏）、import 任何 Composable——它是纯 Kotlin 类，可单测

                ## 为什么组合层不该持有状态

                - 组合随时可能销毁重建：状态放那里，一次旋转就丢
                - 请求放组合里，旋转就**发两次**；放 viewModelScope，转十次也只发一次
                - 组合层只留「这一帧怎么画」，状态与逻辑全部上移到 ViewModel
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "配置变更存活：组合重建归零，ViewModel 原封不动（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // ViewModel 等价物：普通 Kotlin 类，生命周期独立于「组合」
                        class CounterViewModel {
                            private val _count = MutableStateFlow(0)
                            val count: StateFlow<Int> = _count.asStateFlow()   // 只读状态：UI 只能观察
                            fun increment() { _count.value += 1 }              // 事件入口
                        }

                        fun main() = runBlocking {
                            val viewModel = CounterViewModel()
                            viewModel.increment()
                            viewModel.increment()
                            repeat(2) { attempt ->          // 模拟两次「配置变更 → 组合销毁重建」
                                val rememberedInComposition = 0   // remember { mutableStateOf(0) }：随组合重建归零
                                println(
                                    "第 ${'$'}{attempt + 1} 次组合：" +
                                        "remember=${'$'}rememberedInComposition，ViewModel=${'$'}{viewModel.count.value}"
                                )
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "第 1 次组合：remember=0，ViewModel=2\n第 2 次组合：remember=0，ViewModel=2",
                ),
                CodeExample(
                    description = "viewModelScope：请求只发一次，组合重建不打断它（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // viewModelScope 的等价物：作用域与 ViewModel 同寿命，组合重建不会触碰它
                        class ProfileViewModel {
                            private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
                            private val _status = MutableStateFlow("加载中")
                            val status: StateFlow<String> = _status.asStateFlow()

                            init {
                                scope.launch {            // 只在 ViewModel 创建时发一次请求
                                    delay(200L)           // 模拟网络请求
                                    _status.value = "数据到达"
                                }
                            }
                        }

                        fun main() = runBlocking {
                            val viewModel = ProfileViewModel()
                            repeat(2) { attempt ->        // 两次进入屏幕（组合重建）：请求没有被重新触发
                                println("第 ${'$'}{attempt + 1} 次进入屏幕：状态=${'$'}{viewModel.status.value}")
                            }
                            delay(300L)                   // 等进行中的请求完成
                            println("完成后状态：${'$'}{viewModel.status.value}")
                        }
                    """.trimIndent(),
                    expectedOutput = "第 1 次进入屏幕：状态=加载中\n第 2 次进入屏幕：状态=加载中\n完成后状态：数据到达",
                ),
            ),
            cppComparison = """
                · ViewModel ≈ C++ 文档/视图结构里的 document：视图（窗口）随用户操作关闭重建，文档对象持有数据继续活着。
                · viewModelScope ≈ 与对象同寿命的线程池 + 析构时 cancel 全部任务；C++ 要手写 stop_source 与析构顺序。
                · 不持 Context/View ≈ 不让长寿命对象引用短寿命对象：C++ 里这是悬垂指针，Android 里是内存泄漏，本质同一条纪律。
                · 纯 Kotlin 可单测 ≈ 业务逻辑与 UI 解耦后，C++ 里「不依赖 GUI 框架的核心库」才可能写单元测试——架构是可测试性的前提。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "旋转屏幕后，ViewModel 会怎样？",
                    options = listOf(
                        "随 Activity 一起销毁重建",
                        "存活，状态原封保留",
                        "被序列化进 Bundle",
                        "自动清空防止泄漏",
                    ),
                    correctIndex = 1,
                    explanation = "ViewModel 的生命周期独立于组合与配置变更：旋转重建的是 Activity 与组合树，ViewModel 原封不动，直到屏幕真正退出。",
                ),
                QuizQuestion(
                    question = "为什么网络请求不该放在 Composable 函数里？",
                    options = listOf(
                        "Composable 里不能调用挂起函数",
                        "请求必须跑在主线程",
                        "组合销毁重建会重复触发请求，结果也无处安放",
                        "网络请求会阻塞重组",
                    ),
                    correctIndex = 2,
                    explanation = "组合可随时重建：状态在组合里就随旋转丢失，请求放组合里就重复发送。请求归 viewModelScope，只发一次、结果有处安放。",
                ),
                QuizQuestion(
                    question = "ViewModel 里持有 Activity 的引用，风险是？",
                    options = listOf(
                        "编译不过",
                        "没有任何风险",
                        "让 ViewModel 变慢",
                        "ViewModel 比界面活得久，会泄漏整个界面",
                    ),
                    correctIndex = 3,
                    explanation = "ViewModel 在配置变更间存活：它攥着旧 Activity 不放，新界面建了旧界面还回收不掉——内存泄漏的经典来源。",
                ),
                QuizQuestion(
                    question = "viewModelScope 里的协程什么时候被取消？",
                    options = listOf(
                        "组合离开屏幕时",
                        "ViewModel 清除（onCleared）时，全部一起取消",
                        "永不取消",
                        "每次重组时",
                    ),
                    correctIndex = 1,
                    explanation = "viewModelScope 与 ViewModel 同寿命：onCleared 时作用域取消，所有在途协程（包括网络请求）一起终止，不会泄漏。",
                ),
            ),
        ),
        Lesson(
            id = "arch-04",
            title = "StateFlow 与 UI 状态",
            concept = """
                ViewModel 有了，状态怎么送到 UI？答案是 **StateFlow**——一个永远持有「当前值」的流。

                ## StateFlow 的两个关键语义

                - 新订阅者**立刻收到当前值**：界面进入就有数据可显示，不会白屏等待
                - 快速连发会**合并（conflate）**：收集者处理不过来时直接跳到最新值——UI 只关心「现在长什么样」

                ## collectAsState：把流接进组合

                - `state.collectAsStateWithLifecycle(initialValue)` 把 StateFlow 变成 Compose State：值一变自动重组
                - WithLifecycle 版本在界面不可见时**停止收集**，省电省资源
                - 本 App 的真实用法：AppNav 里 `progressStore.progress.collectAsStateWithLifecycle(...)`——进度一变，所有界面自动刷新

                ## 单向数据流（UDF）闭环

                - **状态下行**：ViewModel 以只读 StateFlow 暴露状态，UI 只能观察、不能改
                - **事件上行**：UI 调用 ViewModel 的方法报告「发生了什么」，由它决定状态怎么变
                - 全部状态变更集中在一处：可追踪、可测试——本课示例就是这条闭环的等价复现
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "单向数据流闭环：状态下行、事件上行（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // 单向数据流：状态只从 ViewModel 流向 UI，事件只从 UI 流向 ViewModel
                        class LessonViewModel {
                            private val _completed = MutableStateFlow(listOf<String>())
                            val completed: StateFlow<List<String>> = _completed.asStateFlow()   // 下行：只读

                            fun onQuizPassed(lessonId: String) {        // 上行：事件入口，唯一改状态的路径
                                _completed.value = _completed.value + lessonId
                            }
                        }

                        fun main() = runBlocking {
                            val viewModel = LessonViewModel()
                            val ui = launch {                           // collectAsStateWithLifecycle 的等价物
                                viewModel.completed.collect { done ->
                                    println("界面刷新：已完成 ${'$'}done")
                                }
                            }
                            yield()                                     // 等界面挂上订阅
                            viewModel.onQuizPassed("arch-01")           // 用户提交答卷 → 事件上行
                            yield()                                     // 状态下行 → 界面重组
                            viewModel.onQuizPassed("arch-02")
                            yield()
                            ui.cancel()
                        }
                    """.trimIndent(),
                    expectedOutput = "界面刷新：已完成 []\n界面刷新：已完成 [arch-01]\n界面刷新：已完成 [arch-01, arch-02]",
                ),
                CodeExample(
                    description = "StateFlow 持有当前值与合并语义：慢收集者只拿到最新值（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        fun main() = runBlocking {
                            val state = MutableStateFlow(0)
                            var uiState: Int = -1                // collectAsState 的 initialValue 角色：先有值可显示
                            val collector = launch {
                                state.collect { value ->
                                    uiState = value
                                    println("UI 收到：${'$'}value")
                                    delay(100L)                  // 模拟界面处理/渲染耗时
                                }
                            }
                            delay(50L)                           // 订阅已建立（新订阅者立刻收到当前值 0）
                            state.value = 1
                            state.value = 2
                            state.value = 3                      // 快速连发：StateFlow 只保留最新值
                            delay(500L)                          // 等收集者处理完
                            collector.cancel()
                            println("最终 UI 状态：${'$'}uiState")
                        }
                    """.trimIndent(),
                    expectedOutput = "UI 收到：0\nUI 收到：3\n最终 UI 状态：3",
                ),
            ),
            cppComparison = """
                · StateFlow ≈ Qt 的 property + signal：值变化自动通知观察者；区别是 StateFlow 天然携带「当前值」，新观察者即到即得。
                · collectAsState ≈ 把数据绑定声明出来：C++ GUI 里 connect 槽函数手刷控件，Compose 里值变即重组，不用手写刷新。
                · 单向数据流 ≈ 「模型改、视图跟」的 MVC 纪律：C++ 里视图直接改模型的野路子很常见，UDF 从结构上杜绝了双向缠绕。
                · conflate ≈ 事件队列丢旧保新：对 UI 渲染是正确取舍（只画最新帧），C++ 游戏循环里也是同样的处理。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "StateFlow 与普通 Flow 最关键的差别是？",
                    options = listOf(
                        "StateFlow 更快",
                        "StateFlow 总是持有当前值，新订阅者立刻收到",
                        "普通 Flow 不能在协程里收集",
                        "StateFlow 只能存 Int",
                    ),
                    correctIndex = 1,
                    explanation = "普通 Flow 是「来了才算」，无值可取；StateFlow 永远有当前值，新订阅者即到即得——这正是 UI 需要的语义。",
                ),
                QuizQuestion(
                    question = "collectAsStateWithLifecycle 相比 collectAsState 的额外好处是？",
                    options = listOf(
                        "收集速度更快",
                        "界面不可见时自动停止收集，省电省资源",
                        "能收集任意 Flow",
                        "不需要初始值",
                    ),
                    correctIndex = 1,
                    explanation = "WithLifecycle 版本跟随界面生命周期：界面退到后台就停止收集，回到前台自动恢复——不可见时不做无用功。",
                ),
                QuizQuestion(
                    question = "单向数据流里，UI 想改变「已完成列表」的正确路径是？",
                    options = listOf(
                        "拿到 StateFlow 的引用直接改",
                        "调用自己的 setState 复制一份改",
                        "调用 ViewModel 的事件方法，由它更新状态",
                        "UI 不该触发任何状态变化",
                    ),
                    correctIndex = 2,
                    explanation = "UI 只上行事件（onQuizPassed 这类方法），状态变更集中在 ViewModel 一处；asStateFlow 暴露的只读引用从类型上禁止 UI 直改。",
                ),
                QuizQuestion(
                    question = "收集者处理很慢，期间 StateFlow 被连赋 3 个值，收集者会？",
                    options = listOf(
                        "三个值逐个补齐处理",
                        "跳过中间值，直接处理最新的那个",
                        "崩溃报错",
                        "从头重新收集",
                    ),
                    correctIndex = 1,
                    explanation = "StateFlow 合并（conflate）：只保留最新值。UI 只关心「现在长什么样」，旧帧没画完就被新帧覆盖是正确行为。",
                ),
            ),
        ),
        Lesson(
            id = "arch-05",
            title = "分层架构",
            concept = """
                前面四课的零件——状态保存、ViewModel、StateFlow——要放进一张**总图**才不会乱。这张图就是分层。

                ## 本工程的真实分层（app/src/main/java/com/kotlin/learn/）

                ```
                ui/navigation   AppNav：把屏幕接成导航图
                     │
                ui/screens      HomeScreen / LessonListScreen / LessonScreen / QuizScreen
                ui/demos        7 个可交互 Compose Demo
                ui/render       MarkdownText：markdown → Compose 渲染
                     │
                data/content    课程数据（各模块的内容文件）
                data/progress   ProgressStore：DataStore 封装，进度唯一写入口
                data/logic      QuizScorer / CurriculumValidator：纯函数逻辑
                data/markdown   MarkdownParser / KotlinHighlighter：纯文本处理
                     │
                data/model      Lesson / QuizQuestion 等纯数据类
                ```

                ## 只有一条规则：依赖方向向下

                - 上层知道下层，**下层不知道上层**——data/ 里没有任何一个文件 import ui/
                - 纯函数层（logic/markdown）不依赖 Android 类，所以本 App 的单元测试能直接测它们
                - ui/screens 只做组装：把 data 层的数据交给 render 渲染，不碰存储细节
                - 跨层走窄接口：UI 依赖 ProgressStore 而非 DataStore 本身——实现可替换，UI 不动

                ## 一次答题的完整闭环

                QuizScreen 收到提交 → data/logic 判分 → data/progress 写入 → Flow 向上回流 → 界面自动刷新。
                事件自上而下、状态自下而上，与 arch-04 的单向数据流一脉相承。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "复现本工程一次答题的分层闭环：事件下行、状态回流（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // data/progress 层：唯一写入口，状态以 StateFlow 向上暴露（对应 ProgressStore）
                        class FakeStore {
                            private val _completed = MutableStateFlow(listOf<String>())
                            val completed: StateFlow<List<String>> = _completed.asStateFlow()
                            suspend fun recordQuizResult(lessonId: String) {
                                _completed.value = _completed.value + lessonId
                            }
                        }

                        // data/logic 层：纯函数，可直接单测（对应 QuizScorer）
                        fun score(answers: List<Boolean>): Int = answers.count { it }

                        // ui/screens 层：只调用相邻下层，不越过它们摸底层
                        class QuizScreen(private val store: FakeStore) {
                            suspend fun onSubmit(answers: List<Boolean>) {
                                val correct = score(answers)               // → 逻辑层判分
                                store.recordQuizResult("arch-05")          // → 存储层写入
                                println("界面层：答对 ${'$'}correct/${'$'}{answers.size}，已提交记录")
                            }
                        }

                        fun main() = runBlocking {
                            val store = FakeStore()
                            val screen = QuizScreen(store)
                            val watcher = launch {                         // 对应 collectAsStateWithLifecycle
                                store.completed.collect { done ->
                                    println("界面层：进度刷新，已完成 ${'$'}{done.size} 课")
                                }
                            }
                            yield()                                        // 让观察者先挂上
                            screen.onSubmit(listOf(true, true, false, true))
                            yield()                                        // Flow 回流 → 界面刷新
                            screen.onSubmit(listOf(true, true, true, true))
                            yield()
                            watcher.cancel()
                        }
                    """.trimIndent(),
                    expectedOutput = "界面层：进度刷新，已完成 0 课\n界面层：答对 3/4，已提交记录\n界面层：进度刷新，已完成 1 课\n界面层：答对 4/4，已提交记录\n界面层：进度刷新，已完成 2 课",
                ),
                CodeExample(
                    description = "依赖方向检查器：核对摘自真实 import 的 12 条依赖全部向下（真实运行）",
                    code = """
                        // 本工程包之间的真实依赖（摘自各文件的 import 语句）
                        val dependsOn = mapOf(
                            "ui/navigation" to listOf("ui/screens", "ui/demos", "data/progress", "data/content"),
                            "ui/screens" to listOf("ui/render", "ui/demos", "data/content", "data/logic", "data/markdown", "data/model", "data/progress"),
                            "ui/demos" to listOf("data/content"),
                            "ui/render" to listOf("data/markdown"),
                            "data/content" to listOf("data/model"),
                            "data/progress" to listOf<String>(),
                            "data/logic" to listOf("data/model"),
                            "data/markdown" to listOf<String>(),
                            "data/model" to listOf<String>(),
                        )

                        fun layer(pkg: String): Int = if (pkg.startsWith("ui/")) 2 else 1   // ui 在上，data 在下

                        fun main() {
                            val violations = dependsOn.flatMap { (pkg, deps) ->
                                deps.filter { layer(it) > layer(pkg) }.map { "${'$'}pkg → ${'$'}it" }
                            }
                            println("检查 ${'$'}{dependsOn.size} 个包、${'$'}{dependsOn.values.sumOf { it.size }} 条依赖")
                            println("违反「只准向下依赖」的条目：${'$'}{if (violations.isEmpty()) "0 条" else violations.joinToString()}")
                        }
                    """.trimIndent(),
                    expectedOutput = "检查 9 个包、15 条依赖\n违反「只准向下依赖」的条目：0 条",
                ),
            ),
            cppComparison = """
                · 分层 ≈ C++ 工程的 .h/.cpp 分离：头文件是「对下的接口」，源文件是层内实现；链接期按依赖顺序组合。
                · include 是文本粘贴，C++ 的分层纪律靠前置声明、include 守卫与 code review 人守；Kotlin 的包与可见性（public/internal）由编译器强制。
                · 循环 include 直接编译失败；Kotlin 同一模块内循环依赖不报编译错，分层靠结构纪律与纯函数层（可单测）来守住。
                · data/ 不 import ui/ ≈ 核心库不链接 GUI 框架：C++ 里 core 库脱离 Qt 才能进 CI 跑测试，这里 logic/markdown 脱离 Android 才能被 JUnit 直接测。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "本工程把 QuizScorer 写成纯函数（不依赖 Android 类），直接收益是？",
                    options = listOf(
                        "运行速度更快",
                        "单元测试可以直接覆盖它，不依赖界面与模拟器",
                        "能少写几个文件",
                        "Android 官方要求必须纯函数",
                    ),
                    correctIndex = 1,
                    explanation = "纯函数层不依赖 Android 类，JUnit 直接实例化调用——本 App 的 QuizScorerTest、CurriculumValidatorTest 就是这么测的。",
                ),
                QuizQuestion(
                    question = "分层架构里唯一的依赖规则是？",
                    options = listOf(
                        "下层可以依赖上层，方便回调",
                        "所有包互相依赖，灵活优先",
                        "上层可依赖下层，下层不知道上层",
                        "只允许 ui 依赖 ui、data 依赖 data",
                    ),
                    correctIndex = 2,
                    explanation = "依赖方向只有一条：向下。ui/ 知道 data/，data/ 里没有任何文件 import ui/——下层不知道上层，才可能被独立测试与替换。",
                ),
                QuizQuestion(
                    question = "UI 层依赖 ProgressStore 而不是直接使用 DataStore，好处是？",
                    options = listOf(
                        "存储实现可以整体替换，UI 一行不用改",
                        "能减少一次磁盘读写",
                        "DataStore 有使用限制必须包一层",
                        "只是命名更短",
                    ),
                    correctIndex = 0,
                    explanation = "ProgressStore 是数据访问的窄接口：哪天换 Room 或远程同步，改的只有这一层——与 data-07 仓库模式是同一条纪律。",
                ),
                QuizQuestion(
                    question = "与 Kotlin 的包分层相比，C++ 工程的分层有什么不同？",
                    options = listOf(
                        "C++ 没有任何分层机制",
                        "C++ 靠头文件/源文件分离与链接组织分层，但 include 是文本粘贴，纪律主要靠人",
                        "C++ 的依赖方向由编译器强制向下",
                        "两者完全相同",
                    ),
                    correctIndex = 1,
                    explanation = "C++ 用 .h/.cpp 与链接器组织分层：机制存在，但 include 本质是文本粘贴，循环依赖靠 include guard 与审查拦截；Kotlin 的包与可见性由编译器兜底。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "architecture",
        title = "架构",
        emoji = "🏛",
        lessons = lessons,
    )
}
