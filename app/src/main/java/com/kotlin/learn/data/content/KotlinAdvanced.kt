package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块四：Kotlin 进阶（6 课），协程为主。 */
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
                            delay(500L)                          // 给子协程时间触发异常
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

                        val list = mutableListOf(1, 2, 3).also {   // also：附加操作，返回接收者
                            println("列表共 ${'$'}it 个元素")
                        }
                        println(list)                              // [1, 2, 3]
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
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "kotlin-advanced",
        title = "Kotlin 进阶",
        emoji = "⚡",
        lessons = lessons,
    )
}
