package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块四：Kotlin 进阶（13 课）：协程、作用域函数与委托、泛型/Flow/inline/DSL/运算符重载/密封接口与自定义委托/序列化。 */
object KotlinAdvanced {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "adv-01",
            title = "协程：为什么需要",
            concept = """
                应用里大量操作是异步的：网络请求、读写文件、动画帧。异步操作完成后要**接着做下一步**，
                传统做法是回调——而回调嵌套回调，就形成了**回调地狱**。

                ## 回调地狱长什么样

                - 每一步的输入依赖上一步的输出，代码只能不断向内嵌套
                - 「先拿 token，再拿用户，最后拿订单」被拆成三层花括号，读代码变成读迷宫
                - 错误处理要每一层单独传，漏传一层就丢异常

                ## 挂起函数怎么解决

                用 `suspend` 函数把「等结果」写成**顺序代码**：

                - 拿 token、拿用户、拿订单——三行平铺，和同步代码长得一模一样
                - 编译器把挂起点拆成状态机，**顺序结构由语言保证**
                - 异常用普通的 try-catch 一层捕获，不用每层回调各传一份
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "回调风格：三层嵌套才拿到最终结果",
                    code = """
                        // 模拟三个异步接口：真实场景中回调在结果到达时才被触发
                        fun fetchToken(onResult: (String) -> Unit) {
                            onResult("T-1")
                        }

                        fun fetchUser(token: String, onResult: (String) -> Unit) {
                            onResult("Ann")
                        }

                        fun fetchOrders(user: String, onResult: (List<String>) -> Unit) {
                            onResult(listOf("订单A", "订单B"))
                        }

                        fun main() {
                            fetchToken { token ->
                                fetchUser(token) { user ->
                                    fetchOrders(user) { orders ->
                                        println("${'$'}user 的订单：${'$'}orders")
                                    }
                                }
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "Ann 的订单：[订单A, 订单B]",
                ),
                CodeExample(
                    description = "挂起风格：同样的结果，代码完全平铺",
                    code = """
                        import kotlinx.coroutines.*

                        suspend fun fetchToken(): String {
                            delay(100L)                    // 模拟网络延迟
                            return "T-1"
                        }

                        suspend fun fetchUser(token: String): String {
                            delay(100L)
                            return "Ann"
                        }

                        suspend fun fetchOrders(user: String): List<String> {
                            delay(100L)
                            return listOf("订单A", "订单B")
                        }

                        fun main() = runBlocking {
                            val token = fetchToken()       // 三行平铺，与回调版逐层对应
                            val user = fetchUser(token)
                            val orders = fetchOrders(user)
                            println("${'$'}user 的订单：${'$'}orders")
                        }
                    """.trimIndent(),
                    expectedOutput = "Ann 的订单：[订单A, 订单B]",
                ),
            ),
            cppComparison = """
                · C++ 异步常用回调 + 状态机或 `std::async`/`std::future` 链表达；协程把「等待结果」写成顺序代码，等于把 future 链拍平。
                · `suspend fun` ≈ C++20 的 `co_await` 协程：同样由编译器生成状态机，但 Kotlin 不需要 promise_type / awaiter 样板。
                · 回调地狱的 C++ 版是「嵌套 lambda + shared_ptr 保活」；挂起函数没有这些负担，也没有悬垂回调。
                · 注意挂起 ≠ 阻塞：与 `future.get()` 占住线程干等有本质区别，挂起时线程被释放。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "回调地狱最直接的问题是？",
                    options = listOf(
                        "嵌套层级随步骤数增长，顺序逻辑被打散、错误处理难传",
                        "编译速度变慢",
                        "回调函数无法访问外部变量",
                        "运行结果不可预测",
                    ),
                    correctIndex = 0,
                    explanation = "每一步依赖上一步的输出，只能向内嵌套；「A 然后 B 然后 C」被拆成多层花括号，异常与上下文还要逐层手工传递。",
                ),
                QuizQuestion(
                    question = "挂起函数解决回调地狱的关键手段是？",
                    options = listOf(
                        "把嵌套回调改写成顺序的 suspend 调用",
                        "给每个回调加锁",
                        "用多线程并行执行所有步骤",
                        "改用全局变量传递中间结果",
                    ),
                    correctIndex = 0,
                    explanation = "suspend 函数让「等待」以顺序代码表达，编译器在背后生成状态机；结构是平的，语义和同步代码一致。",
                ),
                QuizQuestion(
                    question = "两段示例代码的输出完全相同，这说明？",
                    options = listOf(
                        "回调与挂起只是写法不同，逻辑结果一致，但挂起版结构是平铺的",
                        "回调风格已经没有任何用途",
                        "suspend 函数一定比回调执行得快",
                        "两次运行结果偶然相同",
                    ),
                    correctIndex = 0,
                    explanation = "挂起函数不改变逻辑，只改变「等待」的表达方式：结果一样，代码从三层嵌套变成三行平铺。",
                ),
                QuizQuestion(
                    question = "在挂起风格里，如果 fetchUser 抛出异常，调用方怎么做？",
                    options = listOf(
                        "在 fetchToken 回调里再传一个错误回调",
                        "无法捕获，程序必然崩溃",
                        "用普通的 try-catch 包住调用点即可",
                        "必须为每个 suspend 函数单独写异常处理",
                    ),
                    correctIndex = 2,
                    explanation = "挂起函数就是普通函数，异常沿调用链正常传播；一个 try-catch 覆盖整个顺序流程，不需要逐层回调传错误。",
                ),
            ),
        ),
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
        Lesson(
            id = "adv-03",
            title = "挂起函数与结构化并发",
            concept = """
                协程不是散兵游勇：每个协程都活在一个**作用域**（`CoroutineScope`）里，作用域负责它的生命周期。

                ## 结构化并发

                - 父作用域结束时，**所有子协程一起结束**，不会泄漏
                - 子协程的异常会传播给父作用域，父级必须处理
                - 「谁启动，谁负责」——这是结构化并发的核心约定

                ## Job：协程的把手

                - `launch` 返回一个 `Job`，可以 `join()` 等它完成、`cancel()` 请求取消
                - 取消是**协作式**的：协程在挂起点（如 `delay`）收到取消信号并退出
                - 纯 CPU 死循环不会响应取消，要主动检查 `isActive`
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "Job 与 join：等一个子协程干完活",
                    code = """
                        import kotlinx.coroutines.*

                        fun main() = runBlocking {
                            val job = launch {                 // launch 返回 Job
                                repeat(3) { i ->
                                    println("子任务 ${'$'}i 进行中")
                                    delay(200L)
                                }
                            }
                            println("已启动，等待完成")
                            job.join()                         // 挂起等待，不阻塞线程
                            println("结束后 job 是否活跃：${'$'}{job.isActive}")
                        }
                    """.trimIndent(),
                    expectedOutput = "已启动，等待完成\n子任务 0 进行中\n子任务 1 进行中\n子任务 2 进行中\n结束后 job 是否活跃：false",
                ),
                CodeExample(
                    description = "cancel：协作式取消在挂起点生效",
                    code = """
                        import kotlinx.coroutines.*

                        fun main() = runBlocking {
                            val job = launch {
                                repeat(5) { i ->
                                    println("执行 ${'$'}i")
                                    delay(300L)
                                }
                            }
                            delay(1000L)                       // 让子协程先跑一会儿
                            println("发起取消")
                            job.cancel()                       // 请求取消
                            job.join()                         // 等协程真正退出
                            println("已退出，是否活跃：${'$'}{job.isActive}")
                        }
                    """.trimIndent(),
                    expectedOutput = "执行 0\n执行 1\n执行 2\n执行 3\n发起取消\n已退出，是否活跃：false",
                ),
            ),
            cppComparison = """
                · `Job` ≈ `std::future`/`std::jthread` 的句柄：join() 语义一致，但 Kotlin 的 join() 是**挂起**等待，不占用线程。
                · 结构化并发没有直接的 C++ 对应物：C++ 靠 `jthread` 的 RAII 在作用域结束时自动 join，Kotlin 由作用域自动取消全部子协程。
                · cancel() 是协作式取消，类似检查 `std::stop_token` 的 stop_requested()；区别是 Kotlin 在**每个挂起点自动检查**，不用手写。
                · 子协程异常传给父级统一处理；C++ 线程里未捕获的异常会直接 `std::terminate`，行为完全不同。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "结构化并发的核心保证是？",
                    options = listOf(
                        "父作用域结束时，所有子协程一起被结束，不会泄漏",
                        "子协程的运行速度一定更快",
                        "所有协程自动跑在不同线程上",
                        "子协程可以比父作用域活得更久",
                    ),
                    correctIndex = 0,
                    explanation = "协程按层级组织：父等子、子失败通知父、父取消连带子。协程不会悄悄逃出作用域造成泄漏。",
                ),
                QuizQuestion(
                    question = "job.join() 与 C++ 的 thread.join() 最大的区别是？",
                    options = listOf(
                        "没有区别，都是死等",
                        "join() 挂起当前协程并释放线程，thread.join() 阻塞占用线程",
                        "join() 会立即返回，不会等待",
                        "join() 会自动取消目标协程",
                    ),
                    correctIndex = 1,
                    explanation = "join() 是挂起函数：等待期间线程被释放去干别的活。thread.join() 则让当前线程阻塞干等。",
                ),
                QuizQuestion(
                    question = "「取消是协作式的」意味着？",
                    options = listOf(
                        "cancel() 后协程在任何位置立即终止",
                        "协程在挂起点（如 delay）响应取消信号；纯计算循环要主动检查 isActive",
                        "取消需要对方同意，否则无效",
                        "cancel() 会抛出异常终止整个程序",
                    ),
                    correctIndex = 1,
                    explanation = "取消通过 CancellationException 在挂起点生效；长时间纯计算不经过挂起点，需要手动检查 isActive 或插入 yield()。",
                ),
                QuizQuestion(
                    question = "job.cancel() 之后，正卡在 delay(300L) 的协程会？",
                    options = listOf(
                        "把剩下的 300ms 睡完再结束",
                        "立即从 delay 处以 CancellationException 结束",
                        "永远挂起在那里",
                        "自动重新开始执行",
                    ),
                    correctIndex = 1,
                    explanation = "delay 等挂起函数都支持取消：cancel() 一调用，挂起点立即抛 CancellationException，协程随即退出，不用等计时结束。",
                ),
            ),
        ),
        Lesson(
            id = "adv-04",
            title = "调度器与线程切换",
            concept = """
                协程跑在哪个线程上，由**调度器**（Dispatcher）决定。协程不是线程，而是「可以在多个线程之间迁移的任务」。

                ## 三个常用调度器

                - `Dispatchers.Main`：Android 主线程，只碰 UI
                - `Dispatchers.IO`：网络、磁盘等阻塞型 IO，线程池按需伸缩
                - `Dispatchers.Default`：CPU 密集计算，线程数约等于 CPU 核数

                ## withContext：切线程不回调

                `withContext(Dispatchers.IO) { ... }` 把块内代码调度到指定线程池执行，完成后**带着结果切回原来的上下文**，写出来仍是顺序代码。

                ## 与阻塞的区别

                `delay` 挂起不占线程；`Thread.sleep` 让线程干等。切到哪个调度器，都不要用 sleep「等结果」——用挂起函数。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "withContext：切出去执行，再自动切回来",
                    code = """
                        import kotlinx.coroutines.*

                        fun main() = runBlocking {
                            val mainThread = Thread.currentThread().name

                            withContext(Dispatchers.Default) {      // 切到默认线程池
                                println("切到别的线程了吗：${'$'}{Thread.currentThread().name != mainThread}")
                            }                                       // 块结束，自动切回原上下文

                            println("切回原线程了吗：${'$'}{Thread.currentThread().name == mainThread}")
                        }
                    """.trimIndent(),
                    expectedOutput = "切到别的线程了吗：true\n切回原线程了吗：true",
                ),
                CodeExample(
                    description = "withContext 带回结果：主流程保持顺序可读",
                    code = """
                        import kotlinx.coroutines.*

                        suspend fun loadProfile(): String {
                            delay(100L)
                            return "Ann"
                        }

                        suspend fun loadPosts(): List<String> {
                            delay(100L)
                            return listOf("帖1", "帖2")
                        }

                        fun main() = runBlocking {
                            // Android 上实际用 Dispatchers.IO；示例用 Default 保证可独立运行
                            val profile = withContext(Dispatchers.Default) { loadProfile() }
                            val posts = withContext(Dispatchers.Default) { loadPosts() }
                            println("${'$'}profile 的帖子：${'$'}posts")
                        }
                    """.trimIndent(),
                    expectedOutput = "Ann 的帖子：[帖1, 帖2]",
                ),
            ),
            cppComparison = """
                · withContext ≈ 「任务提交线程池 + `future.get()` 拿结果」，但等待是挂起，不占用当前线程。
                · Dispatchers ≈ std::execution 的执行器思想（标准库直至 C++26 才落地），Kotlin 直接内置 Main/IO/Default 三档。
                · delay vs `Thread.sleep`：挂起让出线程 vs 阻塞占着线程；在错误的地方 sleep 是性能事故高发区。
                · 不需要手写线程池与任务队列：调度器自带池化和负载均衡，切换成本远低于新建线程。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Android 开发中 Dispatchers.Main 的正确用法是？",
                    options = listOf(
                        "执行网络请求和文件读写",
                        "更新 UI；耗时工作交给其他调度器",
                        "所有代码都用它，最安全",
                        "专门用来跑死循环计算",
                    ),
                    correctIndex = 1,
                    explanation = "Main 是唯一能碰 UI 的调度器；网络/磁盘走 IO，CPU 密集走 Default，最后回 Main 更新界面。",
                ),
                QuizQuestion(
                    question = "withContext(Dispatchers.IO) { ... } 块执行完毕后？",
                    options = listOf(
                        "协程留在 IO 线程上继续跑",
                        "带着块的结果切回原来的协程上下文",
                        "整个程序退出",
                        "必须手动调用代码切回",
                    ),
                    correctIndex = 1,
                    explanation = "withContext 是挂起函数：块在目标调度器执行完，把最后一个表达式的值带回原上下文继续，无需回调。",
                ),
                QuizQuestion(
                    question = "delay(1000L) 与 Thread.sleep(1000) 的本质区别？",
                    options = listOf(
                        "没有区别，只是写法不同",
                        "delay 挂起协程并释放线程；sleep 让线程阻塞干等",
                        "delay 更快，因为不等待",
                        "sleep 可以在协程里替代 delay",
                    ),
                    correctIndex = 1,
                    explanation = "delay 是挂起：线程这 1 秒里可以去跑别的协程。sleep 占住线程 1 秒什么都不干，高并发下浪费严重。",
                ),
                QuizQuestion(
                    question = "对图片做 CPU 密集的压缩计算，应该用哪个调度器？",
                    options = listOf("Dispatchers.Main", "Dispatchers.IO", "Dispatchers.Default", "不指定，随便哪个"),
                    correctIndex = 2,
                    explanation = "Default 的线程数约等于 CPU 核数，适合计算型任务；IO 面向阻塞等待型操作，线程更多但用于计算反而增加切换开销。",
                ),
            ),
        ),
        Lesson(
            id = "adv-05",
            title = "协程异常处理",
            concept = """
                协程不会吞异常：异常沿结构化并发的层级**向上传播**，父作用域取消其余子协程后，把异常抛给调用者。

                ## 普通 try-catch 就够用

                挂起函数就是普通函数：调用它可能抛异常，用 try-catch 包住即可，与同步代码写法完全一致。

                ## coroutineScope 会转发异常

                - `coroutineScope { }` 里任何一个子协程失败，该作用域会取消其余子协程，并把异常**重新抛给调用者**
                - 所以在 runBlocking 里用 try 包住整个作用域，就能接住内部全部失败

                ## CoroutineExceptionHandler

                - 只对**根协程**的未捕获异常生效，用于兜底记录日志，不承载业务逻辑
                - 装在子协程上无效——异常在到达它之前就被父作用域接走了
                - Android 中通常装在最外层作用域，防止未处理异常直接崩溃
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "挂起函数的异常：普通 try-catch 直接接住",
                    code = """
                        import kotlinx.coroutines.*

                        suspend fun riskyLoad(): String {
                            delay(100L)                          // 模拟网络请求
                            throw IllegalStateException("网络异常")
                        }

                        fun main() = runBlocking {
                            try {
                                println(riskyLoad())
                            } catch (e: IllegalStateException) {
                                println("捕获：${'$'}{e.message}")
                            }
                            println("程序继续运行")
                        }
                    """.trimIndent(),
                    expectedOutput = "捕获：网络异常\n程序继续运行",
                ),
                CodeExample(
                    description = "CoroutineExceptionHandler：根协程的兜底",
                    code = """
                        import kotlinx.coroutines.*

                        // 只对根协程的未捕获异常生效；这里装在 GlobalScope 启动的根协程上
                        val handler = CoroutineExceptionHandler { _, e ->
                            println("兜底处理器：${'$'}{e.message}")
                        }

                        fun main() = runBlocking {
                            GlobalScope.launch(handler) {
                                delay(100L)
                                throw ArithmeticException("根协程未处理异常")
                            }
                            delay(500L)                          // 给根协程时间触发异常
                            println("main 正常结束")
                        }
                    """.trimIndent(),
                    expectedOutput = "兜底处理器：根协程未处理异常\nmain 正常结束",
                ),
            ),
            cppComparison = """
                · 挂起函数的 try-catch 与同步代码一致；C++20 协程的异常也经 co_await 传播，但要手写 promise 的 unhandled_exception。
                · 子协程异常传给父作用域 ≈ 异常沿调用栈向上抛；C++ 线程里未捕获异常直接 `std::terminate`，没有「传给父级」一说。
                · CoroutineExceptionHandler ≈ 温柔版的全局 `std::set_terminate`：只兜底记录，不参与正常业务流程。
                · CancellationException 表达「取消」而非「错误」，不要在 catch 里顺手吞掉它——类似线程的中断标志要重新抛出。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "coroutineScope 里某个 launch 子协程抛出异常，会发生什么？",
                    options = listOf(
                        "被自动忽略，作用域正常返回",
                        "取消该作用域内所有子协程，并把异常重新抛给调用者",
                        "转成 CancellationException 后继续运行",
                        "只终止出错的那个协程，其余不受影响",
                    ),
                    correctIndex = 1,
                    explanation = "普通 coroutineScope 不吞异常：一个子协程失败，作用域取消其余子协程，异常最终抛到调用者手里。",
                ),
                QuizQuestion(
                    question = "CoroutineExceptionHandler 在什么情况下会被调用？",
                    options = listOf(
                        "任何协程抛异常时",
                        "只在根协程出现未捕获异常时",
                        "只在子协程抛异常时",
                        "只在 cancel() 被调用时",
                    ),
                    correctIndex = 1,
                    explanation = "handler 只接住「没有父级会处理」的根协程异常；装在子协程上时，异常在到达它之前就先被父作用域接走了。",
                ),
                QuizQuestion(
                    question = "调用可能抛异常的挂起函数，惯用做法是？",
                    options = listOf(
                        "必须装 CoroutineExceptionHandler",
                        "用普通 try-catch 包住调用点",
                        "无法处理，只能让它崩溃",
                        "把函数改成返回 Boolean 表示成败",
                    ),
                    correctIndex = 1,
                    explanation = "挂起函数与普通函数共享同一套异常机制：try-catch 原样可用，与同步代码写法完全一致。",
                ),
                QuizQuestion(
                    question = "关于 CancellationException，正确的态度是？",
                    options = listOf(
                        "它是错误，出现就要记录日志",
                        "它是取消信号，catch 到后应让它继续传播，不要吞掉",
                        "它等于 IllegalStateException 的别名",
                        "它在任何情况下都不会被抛出",
                    ),
                    correctIndex = 1,
                    explanation = "取消机制靠 CancellationException 工作；在宽泛的 catch（如 catch (e: Exception)）里吞掉它，会破坏结构化并发的取消。",
                ),
            ),
        ),
        Lesson(
            id = "adv-06",
            title = "作用域函数与委托属性",
            concept = """
                作用域函数用更短的语法表达「对某个对象做一组操作」，区别只在两件事：**返回什么**、**块内指代接收者用谁**。

                ## 四个常用作用域函数

                - `let`：块内用 `it`，返回 lambda 结果——配合 `?.` 做空检查后取值
                - `run`：块内用 `this`，返回 lambda 结果——组织一段计算
                - `apply`：块内用 `this`，返回接收者本身——初始化/配置对象
                - `also`：块内用 `it`，返回接收者本身——附加副作用（日志、校验）

                ## 委托属性 by lazy

                `val x by lazy { ... }` 表示**第一次访问时才计算，之后用缓存**：

                - 默认线程安全，单例/重对象初始化的经典写法
                - 计算成本高、又不一定用得上时最划算
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "let / run / apply / also 一图流",
                    code = """
                        data class Config(var host: String = "", var port: Int = 0)

                        fun main() {
                            val name: String? = "Kotlin"

                            val length = name?.let { it.length }       // let：仅非空时执行，返回 lambda 结果
                            println(length)                            // 6

                            val area = run {                           // run：块内组织计算，返回块结果
                                val a = 3
                                val b = 4
                                a * b
                            }
                            println(area)                              // 12

                            val config = Config().apply {              // apply：块内 this 是接收者
                                host = "127.0.0.1"                     // 省略 this. 直接配置
                                port = 8080
                            }
                            println(config)                            // Config(host=127.0.0.1, port=8080)

                            val list = mutableListOf(1, 2, 3).also {   // also：附加操作，it 是接收者本身
                                println("列表共 ${'$'}{it.size} 个元素")
                            }
                            println(list)                              // [1, 2, 3]
                        }
                    """.trimIndent(),
                    expectedOutput = "6\n12\nConfig(host=127.0.0.1, port=8080)\n列表共 3 个元素\n[1, 2, 3]",
                ),
                CodeExample(
                    description = "by lazy：首次访问才计算，之后走缓存",
                    code = """
                        val heavyConfig: String by lazy {
                            println("（昂贵计算执行了一次）")
                            "生产配置"
                        }

                        fun main() {
                            println("main 开始")
                            println(heavyConfig)                   // 第一次访问：触发计算
                            println(heavyConfig)                   // 第二次访问：直接用缓存
                        }
                    """.trimIndent(),
                    expectedOutput = "main 开始\n（昂贵计算执行了一次）\n生产配置\n生产配置",
                ),
            ),
            cppComparison = """
                · `apply` 配置对象 ≈ 构造后对成员连续赋值；它返回接收者本身，比 C++ 的 setter 链（返回 *this）更通用、可嵌在任何表达式里。
                · `?.let { }` 的空检查 ≈ `if (p != nullptr)` 守卫：作用域封闭、非空值直接可用，结果一并带出。
                · `by lazy` ≈ C++ 函数内 `static` 局部变量（首次初始化 + 缓存），但 Kotlin 默认**线程安全**，且可用于任意属性。
                · 四个作用域函数都是 inline 扩展函数，调用没有额外对象开销，效果类似 C++ lambda 被内联。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Config().apply { host = \"x\" } 的返回值是？",
                    options = listOf(
                        "lambda 里最后一个表达式的值",
                        "配置好的 Config 实例本身",
                        "Unit",
                        "host 字符串",
                    ),
                    correctIndex = 1,
                    explanation = "apply 返回接收者本身，专门用于初始化/配置：new 完直接接上配置块，结果还是那个对象。",
                ),
                QuizQuestion(
                    question = "let 与 also 的关键区别是？",
                    options = listOf(
                        "let 返回 lambda 结果，also 返回接收者本身",
                        "let 用 this，also 用 it",
                        "let 只能用于空检查",
                        "also 不能访问接收者",
                    ),
                    correctIndex = 0,
                    explanation = "两者块内都用 it；差别在返回值：let 用于转换取值，also 做完附加操作后把原对象原样交回。",
                ),
                QuizQuestion(
                    question = "val x by lazy { ... } 中 lambda 什么时候执行？",
                    options = listOf(
                        "声明时立即执行",
                        "第一次访问 x 时执行，之后用缓存结果",
                        "每次访问都重新执行",
                        "程序退出前执行",
                    ),
                    correctIndex = 1,
                    explanation = "by lazy 是惰性初始化：首次访问触发计算并缓存，后续访问直接取缓存；默认线程安全。",
                ),
                QuizQuestion(
                    question = "name?.let { println(it.length) } 这个组合的价值是？",
                    options = listOf(
                        "把「非空才执行」和「块内拿非空值」合并成一句",
                        "把 name 变成非空类型",
                        "length 为 null 时返回 0",
                        "比 if 判空执行更快",
                    ),
                    correctIndex = 0,
                    explanation = "?. 短路掉 null，let 保证块内的 it 一定是非空 String——空检查与使用一步到位，等价于一段 if 判空守卫。",
                ),
            ),
        ),
        Lesson(
            id = "adv-07",
            title = "泛型与型变",
            concept = """
                泛型让「一套逻辑服务多种类型」。这一课的重点不是语法，而是**型变**——泛型之间「谁是谁的子类型」的规则。

                ## 基本语法与擦除

                - `fun <T> f(x: T)`：泛型函数；`class Box<T>(val v: T)`：泛型类
                - JVM 泛型**运行时擦除**：`List<Int>` 和 `List<String>` 运行时都只是 `List`

                ## out 与 in：型变

                - `out T`（生产者）：T 只出现在**输出**位置 → `Producer<String>` 可以当 `Producer<Any>` 用（协变）
                - `in T`（消费者）：T 只出现在**输入**位置 → 超类型可代入（逆变）
                - 记法 **PECS**：Producer out，Consumer in

                ## 星投影 List<*>

                元素类型未知时的安全只读视图：能 `size`、能遍历（元素当 `Any?` 读），不能往里写。

                ## reified：把类型贴回运行时

                擦除让 `value is T` 在普通泛型函数里不可能——`inline fun <reified T>` 把函数体复制进调用点，T 被替换成实际类型，`is T`、`T::class` 全部可行。注意边界：`List<Int>` 的实参 `Int` 仍被擦除，reified 只能看到 `List`。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "out 型变与星投影：生产者可以被更大的类型引用",
                    code = """
                        // out：生产者——只往外输出 T，Producer<String> 可以当 Producer<Any> 用
                        interface Producer<out T> {
                            fun produce(): T
                        }

                        class GreetingProducer : Producer<String> {
                            override fun produce() = "你好"
                        }

                        // 星投影：不知道也不关心元素类型时用 List<*>
                        fun describe(list: List<*>): String = "共 ${'$'}{list.size} 个元素"

                        fun main() {
                            val p: Producer<Any> = GreetingProducer()   // String 是 Any 的子类型 → 型变允许
                            println(p.produce())
                            println(describe(listOf(1, "二", 3.0)))
                        }
                    """.trimIndent(),
                    expectedOutput = "你好\n共 3 个元素",
                ),
                CodeExample(
                    description = "reified：inline 让 T 在运行时可见",
                    code = """
                        // reified 必须配 inline：内联后 T 被替换成实际类型，运行时才看得见
                        inline fun <reified T> checkType(value: Any): Boolean = value is T

                        inline fun <reified T> describe(): String = "T 在运行时是 ${'$'}{T::class.simpleName}"

                        fun main() {
                            println(checkType<String>("你好"))
                            println(checkType<Int>("你好"))
                            println(describe<List<Int>>())      // 注意：List<Int> 也只能看到 List（擦除的边界）
                        }
                    """.trimIndent(),
                    expectedOutput = "true\nfalse\nT 在运行时是 List",
                ),
            ),
            cppComparison = """
                · C++ 模板没有声明处型变：`vector<string>` 不能赋给 `vector<object>`，约束靠实例化点检查、SFINAE 或 C++20 concepts；Kotlin 用 out/in 在**声明处**一次性表达，所有使用点自动获得协变/逆变规则。
                · Kotlin 泛型运行时擦除（JVM），reified + inline 是把类型信息「贴回调用点」的官方手段；C++ 模板是编译期按类型逐份实例化，类型信息反而过全，代价是代码膨胀。
                · `List<*>` ≈ 「不知道 T 的 List」：类似装 void* 的容器，但保留类型安全的只读视图。
                · `value is T` 在 C++ 里对应 typeid 比较——那是运行时 RTTI；Kotlin 靠内联把类型判断在编译期定型。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Producer<out T> 中的 out 表示？",
                    options = listOf(
                        "T 只出现在输入位置：Producer<Any> 可以当作 Producer<String> 使用",
                        "T 只出现在输出位置：Producer<String> 可以当作 Producer<Any> 使用",
                        "Producer 不能再被继承",
                        "T 在运行时会被替换成 Any",
                    ),
                    correctIndex = 1,
                    explanation = "out = 协变/生产者：只生产 T，String 是 Any 的子类型，因此 Producer<String> 也是 Producer<Any>。in（消费者）则方向相反。",
                ),
                QuizQuestion(
                    question = "List<*> 星投影的含义是？",
                    options = listOf(
                        "等价于 List<Any>，可以往里放任何类型",
                        "等价于 List<Int>，只是写法更短",
                        "元素类型未知的安全只读视图：能拿 size、把元素当 Any? 读，不能往里写",
                        "表示空列表",
                    ),
                    correctIndex = 2,
                    explanation = "List<*> ≈ List<out Any?>：读出来当 Any? 用；因为不知道元素类型，任何写入（包括 null）都被编译器禁止——Kotlin 星投影比 Java 通配符的「仅 null 可写」更严格。",
                ),
                QuizQuestion(
                    question = "reified 为什么必须配合 inline？",
                    options = listOf(
                        "JVM 泛型在运行时被擦除；inline 把函数体复制进调用点，T 被替换成实际类型，is T / T::class 才可行",
                        "reified 是 inline 的别名，只是写法不同",
                        "不内联的话 reified 函数运行太慢",
                        "inline 让函数变成 public",
                    ),
                    correctIndex = 0,
                    explanation = "擦除使普通泛型函数运行时看不到 T；内联后调用点直接持有实参类型，类型判断才能落地。注意 List<Int> 的实参也被擦成 List——reified 看不到内层类型。",
                ),
                QuizQuestion(
                    question = "C++ 模板与 Kotlin 泛型型变的对比，正确的是？",
                    options = listOf(
                        "C++ 的 vector<string> 可以直接赋给 vector<object>",
                        "C++ 模板没有声明处型变，靠实例化点检查/SFINAE/concepts 约束；Kotlin 用 out/in 在声明处表达型变",
                        "C++ 模板运行时擦除类型，Kotlin 不擦除",
                        "两者完全等价，只是语法不同",
                    ),
                    correctIndex = 1,
                    explanation = "C++ 模板按实例化逐份生成并检查（使用点检查）；Kotlin 一处声明，靠型变注解约束所有使用点（声明点声明）。方向正好相反。",
                ),
            ),
        ),
        Lesson(
            id = "adv-08",
            title = "Flow 进阶：冷热与组合",
            concept = """
                Flow 是协程世界的**异步数据流**：一串按时间陆续到达的值。这一课讲清冷热之分与组合算子。

                ## 冷流：collect 是开关

                - `flow { }` 构建块只在**收集时**执行；每次 collect 各自完整跑一遍
                - 两个收集者互不影响，像两份独立的订阅

                ## 热流：MutableStateFlow

                - 独立于收集者存在：随时读写 `value`，有没有人收集都一样
                - 新收集者一上来**立刻拿到最新值**——UI 状态容器的标准语义
                - SharedFlow 同为热流，但不保存状态，适合「事件广播」而不是「状态保存」

                ## 组合算子

                - `combine`：任一方发射，就用**双方最新值**组合出一条新数据（两方各自缓存最新）
                - `zip`：严格按位置一对一配对，慢的一方拖住整体
                - `conflate`：收集太慢时只保留最新值，跳过中间值——UI 只关心最新帧
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "冷流每次收集重新执行；StateFlow 热流随时可读（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // 冷流：flow { } 构建块只在收集时执行，每次收集都重新执行
                        fun coldCounter(): Flow<Int> = flow {
                            println("（构建块开始执行）")
                            emit(1)
                            emit(2)
                        }

                        fun main() = runBlocking {
                            println("冷流·第一次收集")
                            coldCounter().collect { println("收到 ${'$'}it") }
                            println("冷流·第二次收集——构建块重新执行")
                            coldCounter().collect { println("收到 ${'$'}it") }

                            val hot = MutableStateFlow(0)          // 热流：不依赖收集者，随时可读
                            hot.value = 42
                            val latest = hot.first()               // 新收集者立刻拿到最新值
                            println("热流·没收集过它，latest = ${'$'}latest")
                        }
                    """.trimIndent(),
                    expectedOutput = "冷流·第一次收集\n（构建块开始执行）\n收到 1\n收到 2\n冷流·第二次收集——构建块重新执行\n（构建块开始执行）\n收到 1\n收到 2\n热流·没收集过它，latest = 42",
                ),
                CodeExample(
                    description = "combine：任一方发射就用双方最新值组合（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        fun main() = runBlocking {
                            val numbers = flow {
                                delay(100L); emit(1)
                                delay(100L); emit(2)
                            }
                            val letters = flow {
                                delay(150L); emit("A")
                                delay(150L); emit("B")
                            }
                            // combine：任一方发射，就用「双方的最新值」组合出一条新数据
                            numbers.combine(letters) { n, l -> "${'$'}n${'$'}l" }
                                .collect { println("combine 发射：${'$'}it") }
                        }
                    """.trimIndent(),
                    expectedOutput = "combine 发射：1A\ncombine 发射：2A\ncombine 发射：2B",
                ),
            ),
            cppComparison = """
                · Flow 冷流 ≈ 惰性生产者：collect 才执行，类似 C++ range 适配器被遍历时才计算；StateFlow/SharedFlow 热流 ≈ 观察者模式里的被观察主体，广播最新状态。
                · combine ≈ Qt 信号槽把两路信号汇成一路：任一路更新就触发；zip ≈ 严格成对消费的双队列。
                · conflate ≈ 「只保留最新一帧」：类似双缓冲渲染丢弃中间帧，UI 场景的标准取舍。
                · Room DAO 返回 Flow<List<T>>，表一变自动重发射——C++ 里数据库回调要手动接线和保活，Flow 把订阅生命周期交给协程作用域管理。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "「Flow 是冷流」意味着？",
                    options = listOf(
                        "Flow 创建时就自动开始发射，不管有没有收集者",
                        "多个收集者共享同一份发射序列",
                        "每次 collect 都重新执行 flow 构建块，收集者之间互不影响",
                        "Flow 收集一次之后就再也不能收集",
                    ),
                    correctIndex = 2,
                    explanation = "冷流 = 代码在收集时才执行，collect 是开关。要多个收集者共享一份热数据流，用 StateFlow/SharedFlow。",
                ),
                QuizQuestion(
                    question = "MutableStateFlow「热」体现在？",
                    options = listOf(
                        "它独立于收集者存在：随时可读写当前值，新收集者一上来立刻拿到最新值",
                        "它运行在更快的线程上",
                        "它的发射速度比普通 Flow 快",
                        "它必须先有收集者才能赋值",
                    ),
                    correctIndex = 0,
                    explanation = "StateFlow 持有状态：赋值不依赖任何收集者，collect 只是订阅状态变化——这正是 UI 状态容器的语义。",
                ),
                QuizQuestion(
                    question = "combine 与 zip 的区别？",
                    options = listOf(
                        "没有区别，都是配对",
                        "combine 任一方发射就用双方最新值组合发射；zip 严格按位置一对一配对，慢的一方拖住整体",
                        "combine 只取第一个值，zip 取所有值",
                        "zip 会丢弃慢流的所有数据",
                    ),
                    correctIndex = 1,
                    explanation = "combine 的每方各自缓存最新值，随时再组合；zip 讲「第 n 个配第 n 个」。示例输出 1A/2A/2B 正是 combine 语义。",
                ),
                QuizQuestion(
                    question = "conflate 的适用场景是？",
                    options = listOf(
                        "把两条流合并成一条",
                        "让发射端变快",
                        "收集端处理太慢时，只保留最新值、跳过中间值——UI 刷新只关心最新帧",
                        "保证一个数据都不丢",
                    ),
                    correctIndex = 2,
                    explanation = "conflate 是收集端的取舍：丢中间、保最新。要求零丢失的场景不能用，改用 buffer 缓冲或 zip 配对。",
                ),
            ),
        ),
        Lesson(
            id = "adv-09",
            title = "inline 与非局部返回",
            concept = """
                inline 不只是性能优化——它改变了 lambda 的**返回语义**。

                ## 内联是什么

                `inline fun` 把函数体和 lambda 体一起复制进调用点：没有闭包对象、没有函数调用开销。`let`/`apply` 等作用域函数都是 inline 的。

                ## 非局部返回

                lambda 体被复制进调用者函数体后，里面的 `return` 就像写在调用者里——**直接从外层函数返回**：

                - 普通（非 inline）高阶函数的 lambda 里写裸 `return` 是**编译错**
                - 只能 `return@函数名` 结束本次 lambda，循环继续

                ## 两个修饰符

                - `noinline`：让某个 lambda 参数保持对象身份（可存储、可转递）
                - `crossinline`：lambda 将被放进另一个执行上下文（如另起线程）调用时，仍内联但禁止非局部返回
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "inline lambda 里的 return 直接返回外层函数（真实运行）",
                    code = """
                        inline fun forEachInline(nums: List<Int>, action: (Int) -> Unit) {
                            for (n in nums) action(n)
                        }

                        fun firstNegative(nums: List<Int>): Int? {
                            forEachInline(nums) {
                                if (it < 0) return it          // 非局部返回：直接从 firstNegative 返回
                            }
                            return null
                        }

                        fun main() {
                            println("第一个负数：${'$'}{firstNegative(listOf(3, 1, -5, 8))}")
                        }
                    """.trimIndent(),
                    expectedOutput = "第一个负数：-5",
                ),
                CodeExample(
                    description = "普通高阶函数：裸 return 是编译错，只能 return@标签 结束本次 lambda（真实运行）",
                    code = """
                        fun process(nums: List<Int>, action: (Int) -> Unit) {
                            for (n in nums) action(n)
                        }

                        fun main() {
                            process(listOf(1, -2, 3)) {
                                if (it < 0) {
                                    println("遇到负数 ${'$'}it：结束本次 lambda，而不是 main")
                                    return@process
                                }
                                println("处理 ${'$'}it")
                            }
                            println("main 继续执行到最后一行")
                        }
                    """.trimIndent(),
                    expectedOutput = "处理 1\n遇到负数 -2：结束本次 lambda，而不是 main\n处理 3\nmain 继续执行到最后一行",
                ),
            ),
            cppComparison = """
                · 非局部返回是 Kotlin 独有语义：C++ lambda 里 return 只退出 lambda 本身，无法从调用者函数返回——除非把返回值带出去再判断，inline 让 Kotlin 免去这道手续。
                · inline 高阶函数 ≈ C++ 模板/宏展开：函数体进调用点，没有闭包对象分配；Kotlin 编译器自动完成，普通函数仍保持对象语义。
                · noinline ≈ 显式要求「这个参数保持 std::function 对象身份」：需要存储或转递时使用。
                · crossinline 对应「lambda 将被放进其他执行上下文执行」的契约：仍内联，但放弃非局部返回。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "「非局部返回」指？",
                    options = listOf(
                        "从多层循环里跳出",
                        "inline 函数的 lambda 参数里写 return，直接从调用它的外层函数返回",
                        "函数的返回值类型是 Nothing",
                        "函数返回后局部变量仍然存活",
                    ),
                    correctIndex = 1,
                    explanation = "内联把 lambda 体复制进调用点，return 就像写在调用者函数体里——forEachInline { if (...) return it } 直接从 firstNegative 返回。",
                ),
                QuizQuestion(
                    question = "普通（非 inline）高阶函数的 lambda 里写裸 return 会？",
                    options = listOf(
                        "直接从外层函数返回",
                        "运行时抛异常",
                        "编译失败：lambda 不是调用者函数体的一部分，只能 return@标签 结束本次 lambda",
                        "被静默忽略",
                    ),
                    correctIndex = 2,
                    explanation = "非 inline 的 lambda 是独立对象，裸 return 无处可去，编译器直接报错；return@process 只结束当次调用，循环继续。",
                ),
                QuizQuestion(
                    question = "noinline 的用途是？",
                    options = listOf(
                        "让某个 lambda 参数不内联，保留为真实对象，可存进变量或传给非 inline 函数",
                        "禁止函数被调用",
                        "让函数不参与编译器优化",
                        "把 inline 函数整体变回普通函数",
                    ),
                    correctIndex = 0,
                    explanation = "inline 参数的 lambda 没有对象身份，不能当对象存储；需要存储或转递时，用 noinline 单独标注该参数。",
                ),
                QuizQuestion(
                    question = "crossinline 解决什么问题？",
                    options = listOf(
                        "内联的 lambda 会被放到另一个执行上下文（如另起线程）里调用时，禁止非局部返回但仍内联",
                        "让 lambda 跨线程共享数据",
                        "交叉编译多平台目标",
                        "强制 lambda 返回 Unit",
                    ),
                    correctIndex = 0,
                    explanation = "若内联 lambda 被包进另一个对象异步执行，非局部 return 就不成立了——crossinline 声明「不许 return，但仍内联」。",
                ),
            ),
        ),
        Lesson(
            id = "adv-10",
            title = "lambda with receiver 与 DSL",
            concept = """
                DSL（领域特定语言）的底层只有一个构件：**带接收者的函数类型**。

                ## T.() -> Unit

                - 普通 `(T) -> Unit`：块内用 `it` 指代 T
                - 带接收者 `T.() -> Unit`：块内 `this` 就是 T——成员直接访问，像「在 T 内部写代码」

                ## apply 的原理

                `T.apply(block: T.() -> Unit): T` = 用带接收者类型执行 block，然后返回接收者本身。本课手写一个 `myApply` 验证：作用域函数没有任何魔法（与 adv-06 的 apply 用法衔接）。

                ## DSL 怎么长出来

                - 顶层入口函数接收 `Root.() -> Unit`
                - 每层接收者上定义扩展函数作为「子结构」
                - 嵌套块里 `this` 指向**最近一层**接收者

                ## @DslMarker：防作用域泄漏

                嵌套时外层接收者的成员在内层也能直接调，容易写错还不报错。用 `@DslMarker` 标注的注解标记接收者类型后，编译器禁止在内层直接访问外层接收者的成员。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "手写迷你 html DSL：lambda with receiver 的真实运行",
                    code = """
                        class Node(val name: String) {
                            val children = mutableListOf<Node>()
                            var text = ""

                            fun render(): String {
                                val inner = text + children.joinToString("") { it.render() }
                                return "<${'$'}name>${'$'}inner</${'$'}name>"
                            }
                        }

                        // 带接收者的函数类型 Node.() -> Unit：块内 this 是 Node
                        fun html(block: Node.() -> Unit): String = Node("html").apply(block).render()

                        fun Node.body(block: Node.() -> Unit) {
                            children += Node("body").apply(block)
                        }

                        fun Node.p(text: String) {
                            children += Node("p").apply { this.text = text }
                        }

                        fun main() {
                            val page = html {
                                body {
                                    p("你好，DSL")
                                    p("this 指向最近的接收者")
                                }
                            }
                            println(page)
                        }
                    """.trimIndent(),
                    expectedOutput = "<html><body><p>你好，DSL</p><p>this 指向最近的接收者</p></body></html>",
                ),
                CodeExample(
                    description = "手写 myApply：apply 的全部原理（真实运行）",
                    code = """
                        // apply 的原理：带接收者的函数类型 + 返回接收者本身
                        fun <T> T.myApply(block: T.() -> Unit): T {
                            this.block()
                            return this
                        }

                        data class Server(var host: String = "", var port: Int = 0)

                        fun main() {
                            val s = Server().myApply {
                                host = "10.0.0.1"          // this 是 Server，可直接访问成员
                                port = 9090
                            }
                            println(s)
                        }
                    """.trimIndent(),
                    expectedOutput = "Server(host=10.0.0.1, port=9090)",
                ),
            ),
            cppComparison = """
                · 带接收者的函数类型 `Node.() -> Unit` ≈ C++ 成员函数指针 + 绑定对象的组合：块内 this 自动指向接收者，无需显式传参。
                · apply 的原理就是一个普通函数，C++ 可用「返回 *this 的链式配置」模仿；但 DSL 嵌套（块内再嵌块）没有对应物——C++ 要手写 builder 对象层层命名。
                · @DslMarker ≈ 编译期的「作用域隔离」：C++ 没有对应机制，靠命名约定和 code review 防止嵌套 builder 的 this 混用。
                · `html { body { p("...") } }` 与手写字符串拼接相比：结构由类型保证——本例标签由 render 自动闭合，漏写结构是编译错误而不是运行时的坏 HTML。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "T.() -> Unit 与 (T) -> Unit 的区别是？",
                    options = listOf(
                        "没有区别，只是写法不同",
                        "前者不能访问 T 的成员",
                        "前者块内 this 是 T，成员可直接访问；后者块内用 it 指代 T",
                        "后者只能用于扩展函数",
                    ),
                    correctIndex = 2,
                    explanation = "带接收者的函数类型让调用块像「在 T 内部写代码」——apply 和各类 DSL 都建立在这上面。",
                ),
                QuizQuestion(
                    question = "apply 的原理可以用一句话概括为？",
                    options = listOf(
                        "T.apply(block: T.() -> Unit): T —— 用带接收者类型执行 block，然后返回接收者本身",
                        "T.apply(block: (T) -> Unit): T —— 用 it 执行 block，返回 block 的结果",
                        "T.apply(block: T.() -> Unit): Unit",
                        "apply 是编译器魔法，无法手写等价物",
                    ),
                    correctIndex = 0,
                    explanation = "本课手写的 myApply 与 apply 行为一致：this.block() 执行配置块，return this 交回对象。作用域函数没有任何魔法。",
                ),
                QuizQuestion(
                    question = "DSL 嵌套块里写 p(\"...\") 时，this 指向？",
                    options = listOf(
                        "最外层的 html",
                        "最近一层的接收者（body 的 Node），所以 p 能被解析为该 Node 的扩展",
                        "main 函数",
                        "this 未定义，必须显式写出",
                    ),
                    correctIndex = 1,
                    explanation = "每层 lambda with receiver 都有自己的隐式 this，内层遮蔽外层——这是 DSL 嵌套书写的原理，也是混淆的来源（@DslMarker 因此而生）。",
                ),
                QuizQuestion(
                    question = "@DslMarker 的作用是？",
                    options = listOf(
                        "标注注解类后，被它标记的接收者在外层接收者的 lambda 中不可直接访问，防止作用域泄漏",
                        "让 DSL 运行更快",
                        "自动生成 DSL 文档",
                        "禁止在 DSL 里调用标准库",
                    ),
                    correctIndex = 0,
                    explanation = "没有它，html {} 里能直接调到外层接收者的所有成员，嵌套写错也不报错；@DslMarker 把这类错误变成编译错误。",
                ),
            ),
        ),
        Lesson(
            id = "adv-11",
            title = "运算符重载与中缀函数",
            concept = """
                运算符不是特权：`+`、`[]`、`in` 都对应一个**约定命名的函数**，实现了就能用。

                ## 常用运算符约定

                - `a + b` → `operator fun plus(other)`
                - `a * k` → `operator fun times(k)`
                - `-a` → `operator fun unaryMinus()`
                - `a[i]` → `operator fun get(i)`；`a[i] = v` → `set(i, v)`
                - `"x" in s` → `operator fun contains(x)`
                - `a..b` → `operator fun rangeTo(b)`
                - `obj(...)` → `operator fun invoke(...)`

                ## 规矩

                - 函数标 `operator` 关键字，**名字必须匹配约定**——不能给 `+` 绑一个叫 add 的函数
                - 扩展函数也能定义运算符：可以给**第三方类**补运算符

                ## 中缀函数 infix

                单参数的成员/扩展函数标 `infix`，调用时省点号和括号：`2 pow 10`。标准库的 `1 to 2`（构造 Pair）就是 infix 扩展。它不是运算符——没有符号，只是更自然的读法。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "向量类：plus/times/unaryMinus/get 全部真实运行",
                    code = """
                        data class Vector2(val x: Int, val y: Int) {
                            operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
                            operator fun times(k: Int) = Vector2(x * k, y * k)
                            operator fun unaryMinus() = Vector2(-x, -y)
                            operator fun get(i: Int) =
                                if (i == 0) x else if (i == 1) y else throw IndexOutOfBoundsException("只支持 0/1")

                            override fun toString() = "(${'$'}x, ${'$'}y)"
                        }

                        fun main() {
                            val a = Vector2(1, 2)
                            val b = Vector2(3, 4)
                            println("a + b = ${'$'}{a + b}")
                            println("a * 3 = ${'$'}{a * 3}")
                            println("-a    = ${'$'}{-a}")
                            println("a[0] = ${'$'}{a[0]}, a[1] = ${'$'}{a[1]}")
                        }
                    """.trimIndent(),
                    expectedOutput = "a + b = (4, 6)\na * 3 = (3, 6)\n-a    = (-1, -2)\na[0] = 1, a[1] = 2",
                ),
                CodeExample(
                    description = "contains/invoke/infix：三个约定的真实运行",
                    code = """
                        class TokenStore(private val tokens: MutableSet<String> = mutableSetOf()) {
                            operator fun contains(token: String) = token in tokens   // 让 "x" in store 可用
                            fun grant(token: String) { tokens += token }
                            override fun toString() = "已授权 ${'$'}{tokens.size} 个"
                        }

                        class TokenIssuer {
                            operator fun invoke(user: String) = "token-${'$'}user"   // 实例当函数调用
                        }

                        // 中缀函数：单参数扩展函数，调用时省点号和括号
                        infix fun Int.pow(n: Int): Int {
                            var result = 1
                            repeat(n) { result *= this }
                            return result
                        }

                        fun main() {
                            val store = TokenStore()
                            store.grant("A-1")
                            println("A-1" in store)
                            println("B-2" in store)
                            println(store)

                            val issue = TokenIssuer()
                            println(issue("Ann"))

                            println("2 pow 10 = ${'$'}{2 pow 10}")
                        }
                    """.trimIndent(),
                    expectedOutput = "true\nfalse\n已授权 1 个\ntoken-Ann\n2 pow 10 = 1024",
                ),
            ),
            cppComparison = """
                · operator plus/get ≈ C++ 的 operator+/operator[]：同名机制；Kotlin 还能用**扩展函数**给第三方类补运算符，C++ 只能改类内或写全局 operator（且要求至少一个类类型参数）。
                · 中缀函数 `2 pow 10` 在 C++ 没有对应物：想要「读起来像运算符」，只能真重载运算符或写成普通函数调用。
                · invoke 让实例像函数一样调用 ≈ C++ 的 operator()（仿函数）：语义一致，Kotlin 还能把 invoke 定义成扩展。
                · contains 之于 `in` ≈ C++ 的 std::find / set::count 惯用法：成员判断在 Kotlin 有统一语法入口。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "operator fun plus(other) 让哪个语法可用？",
                    options = listOf("a == b", "a + b", "a in b", "a[i]"),
                    correctIndex = 1,
                    explanation = "plus 对应二元加号 a + b；== 对应 equals、in 对应 contains、a[i] 对应 get——运算符与函数名一一约定。",
                ),
                QuizQuestion(
                    question = "a[i]（下标访问）对应的运算符函数是？",
                    options = listOf(
                        "operator fun indexOf",
                        "operator fun at",
                        "operator fun get(index: Int)",
                        "operator fun bracket",
                    ),
                    correctIndex = 2,
                    explanation = "get/set 成对：a[i] 调 get，a[i] = v 调 set(i, v)。",
                ),
                QuizQuestion(
                    question = "自定义 infix 函数的条件是？",
                    options = listOf(
                        "只有一个参数的成员函数或扩展函数，调用时省点号和括号",
                        "任何函数加 infix 关键字都行",
                        "只能给 Int 类型定义",
                        "必须同时标注 operator",
                    ),
                    correctIndex = 0,
                    explanation = "infix 要求单参数的成员/扩展函数；标准库的 1 to 2 就是 infix 扩展。运算符则用 operator + 约定名。",
                ),
                QuizQuestion(
                    question = "\"A-1\" in store 展开后调用的是？",
                    options = listOf(
                        "store.get(\"A-1\")",
                        "store.contains(\"A-1\")",
                        "store.invoke(\"A-1\")",
                        "store.plus(\"A-1\")",
                    ),
                    correctIndex = 1,
                    explanation = "in 运算符约定到 contains；本课 TokenStore 实现了 operator contains，所以 `\"A-1\" in store` 可用。",
                ),
            ),
        ),
        Lesson(
            id = "adv-12",
            title = "密封接口与自定义委托",
            concept = """
                ## 密封接口 sealed interface

                与 sealed class 一样：**子类型集合封闭**（同模块内声明），when 可以穷尽检查。差异：

                - 接口**不携带构造状态**，子类实现更自由
                - **枚举类也能实现**密封接口——sealed class 做不到（枚举已经继承了 Enum）
                - 每个子类型可以是 data class（带数据）、object（单例）或 enum（固定取值集合）

                ## 自定义委托

                `by` 背后是一个普通对象（adv-06 的 by lazy 是「读取侧」委托）：

                - `ReadOnlyProperty`：实现 `getValue`，服务 `val`
                - `ReadWriteProperty`：再加 `setValue`，服务 `var`
                - `Delegates.observable(initial) { old, new -> ... }`：每次赋值都回调（即使值相同）——本课手写一个等价物
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "密封接口：数据类/object/枚举三类子类型，when 穷尽检查（真实运行）",
                    code = """
                        sealed interface UiState
                        data class Loading(val attempt: Int) : UiState
                        data class Ready(val items: List<String>) : UiState
                        object Error : UiState

                        enum class SyncStatus : UiState { IDLE, SYNCING, DONE }   // 枚举类也能实现密封接口

                        // when 穷尽所有子类型：不用 else，漏写分支直接编译失败
                        fun label(state: UiState): String = when (state) {
                            is Loading -> "加载中（第 ${'$'}{state.attempt} 次）"
                            is Ready -> "就绪：${'$'}{state.items.size} 条"
                            Error -> "出错了"
                            is SyncStatus -> "同步状态：${'$'}state"
                        }

                        fun main() {
                            println(label(Loading(2)))
                            println(label(Ready(listOf("A", "B"))))
                            println(label(Error))
                            println(label(SyncStatus.SYNCING))
                        }
                    """.trimIndent(),
                    expectedOutput = "加载中（第 2 次）\n就绪：2 条\n出错了\n同步状态：SYNCING",
                ),
                CodeExample(
                    description = "手写 observable 委托：ReadWriteProperty 的真实运行",
                    code = """
                        import kotlin.properties.ReadWriteProperty
                        import kotlin.reflect.KProperty

                        // 等价于 Delegates.observable：每次赋值都回调（即使值相同）
                        class Observable<T>(
                            initial: T,
                            private val onChange: (old: T, new: T) -> Unit,
                        ) : ReadWriteProperty<Any?, T> {
                            private var value = initial
                            override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
                            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                                val old = this.value
                                this.value = value
                                onChange(old, value)
                            }
                        }

                        fun main() {
                            var volume by Observable(30) { old, new -> println("音量 ${'$'}old → ${'$'}new") }
                            volume = 50
                            volume = 50
                            volume = 10
                            println("当前音量：${'$'}volume")
                        }
                    """.trimIndent(),
                    expectedOutput = "音量 30 → 50\n音量 50 → 50\n音量 50 → 10\n当前音量：10",
                ),
            ),
            cppComparison = """
                · sealed interface ≈ C++ 的 std::variant + visitor：封闭类型集合 + 穷尽处理；variant 漏分支靠编译警告或库层断言，Kotlin 的 when 穷尽性是硬性编译错误。
                · 枚举类实现密封接口：C++ 枚举不能继承接口，只能往 variant 里塞枚举再写转换层；Kotlin 一行声明。
                · 自定义委托（ReadWriteProperty）≈ C++ 没有属性语法，getter/setter 手写或宏生成；Kotlin 把「存取拦截」抽象成可复用对象，一处实现处处 by。
                · Delegates.observable ≈ 手写 setter 里发通知（观察者模式样板）；Kotlin 一行 by 声明，拦截逻辑可复用。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "sealed interface 相比 sealed class 的特点是？",
                    options = listOf(
                        "sealed interface 允许模块外的类实现",
                        "sealed class 不能有 object 子类",
                        "接口不携带构造状态，子类实现更自由（数据类/枚举/object 都能实现）；sealed class 能有状态，但子类受单继承限制",
                        "两者完全等价，没有区别",
                    ),
                    correctIndex = 2,
                    explanation = "共同点是「子类型集合封闭在模块内」；差异在状态携带与继承约束。本课 SyncStatus 用枚举实现密封接口，正是 sealed class 做不到的写法。",
                ),
                QuizQuestion(
                    question = "密封类型（interface/class）最大的收益是？",
                    options = listOf(
                        "when 穷尽性检查：所有分支覆盖后可省 else，新增子类型时漏写直接编译失败",
                        "运行速度更快",
                        "自动生成 toString",
                        "子类可以放在任何文件",
                    ),
                    correctIndex = 0,
                    explanation = "编译器知道全部子类型，分支覆盖即可编译通过；加一种新状态，所有相关 when 立刻编译报错提醒补齐——状态机的安全网。",
                ),
                QuizQuestion(
                    question = "ReadOnlyProperty 与 ReadWriteProperty 的方法契约是？",
                    options = listOf(
                        "ReadOnlyProperty 也要实现 setValue",
                        "ReadOnlyProperty 只有 getValue；ReadWriteProperty 多一个 setValue，对应 var 委托",
                        "两者都必须实现 getValue/setValue/属性名三个方法",
                        "ReadWriteProperty 只能用于 val",
                    ),
                    correctIndex = 1,
                    explanation = "val 用 ReadOnlyProperty（只读）；var 用 ReadWriteProperty（读 + 写）。本课 Observable 实现 ReadWriteProperty，所以 volume 可以是 var。",
                ),
                QuizQuestion(
                    question = "自定义 observable 委托与 adv-06 的 by lazy 分工是？",
                    options = listOf(
                        "两者等价，可以互换",
                        "observable 也做缓存",
                        "by lazy 管「首次读取才计算」（读取侧）；observable 管「每次写入时收到通知」（写入侧），二者互补",
                        "by lazy 每次赋值都会回调",
                    ),
                    correctIndex = 2,
                    explanation = "adv-06 的 lazy 是读取侧惰性初始化；本课委托是写入侧监听（标准库 Delegates.observable 每次赋值都回调，即使值相同）。委托属性是同一机制的两个方向。",
                ),
            ),
        ),
        Lesson(
            id = "adv-13",
            title = "序列化",
            concept = """
                ## kotlinx.serialization：编译器插件方案

                - `@Serializable` 标注数据类，**编译器插件**在编译期生成 `User.serializer()`——运行时零反射
                - `Json.encodeToString(user)`：对象 → JSON，字段按**声明顺序**输出，字段名即属性名（`@SerialName` 可改名）
                - `Json.decodeFromString<User>(json)`：JSON → 对象；缺无默认值的必填字段直接抛异常
                - 因为没有反射，R8/ProGuard 裁剪后依然可用——Android 网络层的主流搭配

                ## 与手写 toString/parse 对照

                手写 `toJson()/fromJson()` 与 C++ 手写 `operator<<`/`operator>>` 同病：每个类一份样板，字段增减要同步多处，还容易「缺了就静默给默认值」。序列化器直接对接构造函数，字段完整性在解析时强制校验。

                （本课示例 1 为概念示意——工程未引入该依赖；示例 2 用纯 Kotlin 复现「序列化 ↔ 反序列化」语义，真实可跑。）
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "kotlinx.serialization 的标准写法（概念示意，非真实运行输出）",
                    code = """
                        // 示意：展示 kotlinx.serialization 的写法与含义（需引入编译器插件与依赖才能真实编译）
                        import kotlinx.serialization.Serializable
                        import kotlinx.serialization.json.Json

                        @Serializable                       // 编译器插件据此生成 User.serializer()
                        data class User(val name: String, val age: Int)

                        fun main() {
                            val json = Json.encodeToString(User("Ann", 20))   // 序列化：对象 → JSON 字符串
                            println(json)
                            val back = Json.decodeFromString<User>(json)      // 反序列化：JSON → 对象
                            println(back)
                        }
                    """.trimIndent(),
                    expectedOutput = "（示意）{\"name\":\"Ann\",\"age\":20}\nUser(name=Ann, age=20)",
                ),
                CodeExample(
                    description = "纯 Kotlin 复现「序列化 ↔ 反序列化」语义：这就是库替你生成的代码（真实运行）",
                    code = """
                        data class User(val name: String, val age: Int) {
                            fun toJson(): String = "{\"name\":\"${'$'}name\",\"age\":${'$'}age}"

                            companion object {
                                fun fromJson(json: String): User {
                                    val body = json.removePrefix("{").removeSuffix("}")
                                    val fields = body.split(",").associate { pair ->
                                        val parts = pair.split(":", limit = 2)
                                        parts[0].trim().removePrefix("\"").removeSuffix("\"") to
                                            parts[1].trim().removePrefix("\"").removeSuffix("\"")
                                    }
                                    return User(fields["name"] ?: "", (fields["age"] ?: "0").toInt())
                                }
                            }
                        }

                        fun main() {
                            val user = User("Ann", 20)
                            val json = user.toJson()
                            println(json)
                            println(User.fromJson(json))
                        }
                    """.trimIndent(),
                    expectedOutput = "{\"name\":\"Ann\",\"age\":20}\nUser(name=Ann, age=20)",
                ),
            ),
            cppComparison = """
                · 手写 toJson/fromJson ≈ C++ 手写 operator<< / operator>>：每个类一份样板，字段增减要同步两处；@Serializable 由编译器插件生成，永远与类定义同步。
                · C++ 没有反射，序列化靠宏/模板元编程或三方库（nlohmann/json、cereal）；kotlinx.serialization 是「官方编译器插件」路线，运行时零反射，对 R8/ProGuard 裁剪友好。
                · 缺字段即抛错：编译期生成的序列化器直接对接构造函数——C++ 手写 parse 常见的「缺了就默认值静默继续」在这里被构造校验拒绝。
                · Android 实战：Retrofit + kotlinx.serialization（或 Moshi）转换器，网络层进出自动完成 JSON ↔ data class。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "@Serializable 的序列化器由谁生成？",
                    options = listOf(
                        "运行时反射读取注解动态生成",
                        "编译器插件在编译期为标注的类生成 serializer，运行时零反射",
                        "需要手写 companion object",
                        "JVM 内置 JSON 支持",
                    ),
                    correctIndex = 1,
                    explanation = "kotlinx.serialization 是编译器插件方案：编译期生成 User.serializer()，运行期无反射——这也是它适合被 R8/ProGuard 裁剪的 Android 的原因。",
                ),
                QuizQuestion(
                    question = "Json.encodeToString(User(\"Ann\", 20)) 的输出字段顺序是？",
                    options = listOf(
                        "按字母序",
                        "随机顺序",
                        "按主构造属性的声明顺序：{\"name\":\"Ann\",\"age\":20}",
                        "总是倒序",
                    ),
                    correctIndex = 2,
                    explanation = "默认按声明顺序输出，字段名即属性名；需要改名时用 @SerialName。",
                ),
                QuizQuestion(
                    question = "反序列化时 JSON 缺少无默认值的必填字段会？",
                    options = listOf(
                        "抛出序列化异常：编译期生成的序列化器按构造函数校验字段完整性",
                        "返回 null 对象",
                        "自动补 0/空串继续",
                        "静默忽略该字段",
                    ),
                    correctIndex = 0,
                    explanation = "序列化器直接对接构造函数，缺字段就构造不出来——比手写解析「缺了就给默认值」更早暴露问题。",
                ),
                QuizQuestion(
                    question = "C++ 里对应 JSON 序列化的惯用做法是？",
                    options = listOf(
                        "标准库自带 @Serializable",
                        "用反射遍历成员自动生成",
                        "手写 operator<< / operator>>（或第三方库如 nlohmann/json），没有语言级序列化机制",
                        "C++ 编译器自动生成 JSON 代码",
                    ),
                    correctIndex = 2,
                    explanation = "C++ 无反射：序列化要么手写流运算符，要么宏/模板/三方库生成。Kotlin 的编译器插件相当于「官方替你写好了 operator<</>>」。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "kotlin-advanced",
        title = "Kotlin 进阶",
        emoji = "⚡",
        lessons = lessons,
    )
}
