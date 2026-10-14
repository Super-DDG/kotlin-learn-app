package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块七：工程化与发布（5 课），从 Service 与广播到测试实战、构建变体、签名与上架。 */
object Shipping {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "ship-01",
            title = "Service 与 BroadcastReceiver 概览",
            concept = """
                界面退到后台后，下载、播放不能中断；系统状态变化（开机、网络切换）要有人知道。
                这两件事分别归 **Service** 与 **BroadcastReceiver**——四大组件里负责「没有界面的工作」的两位。

                ## Service：没有界面的长时任务

                - Service **不是线程**：onCreate / onStartCommand 都跑在主线程，耗时活必须自己开线程
                - 后台 Service：`startService` 启动，系统在内存紧张时随时可能杀掉
                - 前台 Service：`startForeground` 挂一条常驻通知，系统优先保活——用「用户看得见」换「不容易被杀」
                - 用 `startForegroundService` 启动的前台服务，**5 秒内必须调用 startForeground**，否则系统直接崩溃应用
                - Android 9 起要声明 FOREGROUND_SERVICE 权限；Android 14 起还要声明具体的前台服务类型

                ## BroadcastReceiver：系统事件的收件人

                - 无序广播（`sendBroadcast`）：所有接收者**并发**收到，互相不知道对方，也无法拦截
                - 有序广播（`sendOrderedBroadcast`）：按 priority 从高到低**接力**，前者可 setResult 传结果给后者、可 abortBroadcast 截断
                - 注册方式两种：运行时 `registerReceiver`（跟随 Context 生死）与 Manifest 静态声明——Android 8.0 起大部分隐式广播不再派发给静态接收者

                ## 本课示例的说明

                Service 与 BroadcastReceiver 是 Android 组件，纯 Kotlin 环境跑不了：示例只给骨架代码，
                expectedOutput 写的是真机上的行为描述。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "前台 Service 的骨架：常驻通知 + 自己开线程（真机行为描述）",
                    code = """
                        class DownloadService : Service() {
                            override fun onBind(intent: Intent?): IBinder? = null   // 不做绑定，返回 null

                            override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
                                startForeground(1, buildNotification("下载中"))      // 5 秒内必须调用
                                Thread {
                                    downloadFile()                                   // 耗时工作：自己开线程
                                    stopSelf()                                       // 完成后停掉自己
                                }.start()
                                return START_NOT_STICKY
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "（行为描述）启动后通知栏出现常驻通知「下载中」，onStartCommand 在主线程回调、" +
                        "子线程执行下载；任务完成 stopSelf 后服务销毁、通知撤除。" +
                        "若 startForegroundService 之后 5 秒内没调 startForeground，系统崩溃应用" +
                        "（did not then call Service.startForeground）。",
                ),
                CodeExample(
                    description = "BroadcastReceiver：运行时注册收无序广播，sendOrderedBroadcast 按优先级接力（真机行为描述）",
                    code = """
                        val receiver = object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                Toast.makeText(context, "收到：${'$'}{intent.action}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        // 运行时注册（Android 8.0 起隐式广播的推荐方式）：
                        context.registerReceiver(receiver, IntentFilter("com.kotlin.learn.DOWNLOAD_DONE"))

                        // 无序广播：所有匹配的接收者并发收到
                        context.sendBroadcast(Intent("com.kotlin.learn.DOWNLOAD_DONE"))

                        // 有序广播：按 priority 从高到低接力，前者可 setResult 传结果、可 abortBroadcast 截断
                        context.sendOrderedBroadcast(
                            Intent("com.kotlin.learn.DOWNLOAD_DONE").putExtra("file", "lesson.zip"),
                            null,
                        )
                    """.trimIndent(),
                    expectedOutput = "（行为描述）注册后每次 sendBroadcast，接收器的 onReceive 都在主线程被回调并弹 Toast；" +
                        "换成 sendOrderedBroadcast 时，priority 最高的接收器先收到，可用 setResult 把数据传给下一个、" +
                        "也可 abortBroadcast 让后面的接收器收不到。",
                ),
            ),
            cppComparison = """
                · Service ≈ C++ 里「由宿主管理生死的后台工作对象」：C++ 线程只由你自己 join，Service 的生死由系统裁定，要靠前台通知换保活。
                · 「Service 不是线程」≈ Win32 的 WndProc 跑在 UI 线程：回调在主线程进来，耗时活必须自己开线程，否则卡死整个应用。
                · BroadcastReceiver ≈ Qt 信号槽 / 消息总线：发布者不知道谁在听；有序广播像责任链，前者可改写或截断消息。
                · startForeground 的常驻通知 ≈ 让后台工作「可见」的承诺：C++ 守护进程没有任何 UI，Android 要求拿后台执行权就要让用户看得见。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Service 的 onStartCommand 运行在哪个线程？",
                    options = listOf(
                        "系统自动分配的工作线程",
                        "主线程——耗时任务必须自己开线程",
                        "IntentService 传入的后台线程",
                        "取决于调用方所在的线程",
                    ),
                    correctIndex = 1,
                    explanation = "Service 不是线程：onCreate/onStartCommand 都在主线程回调，与 Activity 的生命周期回调同理；下载这类活要自己开线程。",
                ),
                QuizQuestion(
                    question = "前台 Service 与后台 Service 的关键差别是？",
                    options = listOf(
                        "前台 Service 跑得更快",
                        "前台 Service 挂常驻通知，系统优先保活",
                        "后台 Service 不能开线程",
                        "前台 Service 不需要声明权限",
                    ),
                    correctIndex = 1,
                    explanation = "前台 Service 用 startForeground 挂一条用户看得见的通知，系统因此优先保活；Android 9+ 还需声明 FOREGROUND_SERVICE 权限。",
                ),
                QuizQuestion(
                    question = "startForegroundService 启动后，5 秒内没有调用 startForeground 会怎样？",
                    options = listOf(
                        "服务静默失败，什么都不发生",
                        "自动降级为后台 Service",
                        "系统崩溃应用（did not then call Service.startForeground）",
                        "通知自动补发",
                    ),
                    correctIndex = 2,
                    explanation = "startForegroundService 是系统对用户的承诺「马上会有可见通知」：5 秒内不兑现 startForeground，系统直接崩溃应用。",
                ),
                QuizQuestion(
                    question = "有序广播与无序广播的差别是？",
                    options = listOf(
                        "有序广播只能发给系统应用",
                        "无序广播并发送达互不干扰；有序广播按 priority 接力，可传结果、可截断",
                        "有序广播速度更快",
                        "两者没有实质差别",
                    ),
                    correctIndex = 1,
                    explanation = "sendBroadcast 所有接收者并发收到、互不知晓；sendOrderedBroadcast 按 priority 接力，setResult 传递、abortBroadcast 截断。",
                ),
                QuizQuestion(
                    question = "Android 8.0 之后，想在收到隐式系统广播时被回调，推荐的做法是？",
                    options = listOf(
                        "继续在 Manifest 里静态声明（对所有广播仍然有效）",
                        "运行时 registerReceiver 动态注册",
                        "新建一个空 Activity 等广播",
                        "静态声明加 export=true 就一定能收到",
                    ),
                    correctIndex = 1,
                    explanation = "Android 8.0 起大部分隐式广播不再派发给 Manifest 静态接收者（少数例外）；运行时 registerReceiver 不受这条限制。",
                ),
            ),
        ),
        Lesson(
            id = "ship-02",
            title = "单元测试实战",
            concept = """
                模块六说过：纯函数层不依赖 Android 类，才能被单元测试直接覆盖。本课看本 App 的**真实测试**怎么写——
                JUnit 4，`app/src/test/` 下，纯 JVM 运行、不需要模拟器。

                ## AAA：Arrange-Act-Assert

                - **Arrange**：准备数据（造题目、造初始进度）
                - **Act**：调用被测函数——最好就一行，被测行为一目了然
                - **Assert**：断言结果（assertEquals / assertTrue）
                - 一个测试只测一件事：名字说清「什么场景、预期什么」，失败时读名字就知道哪里坏了

                ## 命名：反引号中文测试名

                - Kotlin 允许用反引号包裹任意字符串当标识符：`全对时 allCorrect 为 true` 这样的
                  中文句子可以直接当函数名（反引号写在代码里，渲染时就是行内代码）
                - 本 App 的全部测试都用中文句子命名——测试报告读起来就是需求描述
                - 对应 C++ 的 GoogleTest：TEST(Suite, Name) 拼出来的用例名同样要求可读

                ## 本 App 的真实测试长什么样

                - `QuizScorerTest`：判分逻辑——全对、部分对、未作答、全未答四个场景
                - `ProgressLogicTest`：进度逻辑——首次交卷、重复交卷保留最好成绩、序列化往返
                - 跑法：`./gradlew.bat :app:testDebugUnitTest`，全部纯 JVM 执行

                ## 示例说明

                下面两段是真实测试文件的**摘录**（原文可在仓库打开核对），已真实编译运行通过；
                expectedOutput 记录的是测试运行的结果描述。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "摘录真实测试 QuizScorerTest：AAA 三段与反引号命名（摘录并标注 AAA，真实运行通过）",
                    code = """
                        // ——摘录自 app/src/test/java/com/kotlin/learn/data/QuizScorerTest.kt——
                        // Arrange：3 道题，正确答案分别是选项 0、2、1
                        private val questions = listOf(
                            QuizQuestion("q1", listOf("a", "b"), 0, "e1"),
                            QuizQuestion("q2", listOf("a", "b", "c"), 2, "e2"),
                            QuizQuestion("q3", listOf("a", "b"), 1, "e3"),
                        )

                        @Test
                        fun `全对时 allCorrect 为 true`() {
                            // Act：三题全部答对
                            val r = QuizScorer.score(questions, mapOf(0 to 0, 1 to 2, 2 to 1))
                            // Assert：正确数、总数、全对标志逐个断言
                            assertEquals(3, r.correctCount)
                            assertEquals(3, r.total)
                            assertTrue(r.allCorrect)
                        }
                    """.trimIndent(),
                    expectedOutput = "（摘录·真实通过）该测试在本工程测试套件中运行通过：" +
                        "correctCount=3、total=3、allCorrect=true——三题全答对，判 3/3。",
                ),
                CodeExample(
                    description = "摘录真实测试 ProgressLogicTest：重复交卷保留最好成绩（原文摘录，真实运行通过）",
                    code = """
                        // ——摘录自 app/src/test/java/com/kotlin/learn/data/ProgressLogicTest.kt——
                        @Test
                        fun `重复交卷保留最好成绩`() {
                            var p = ProgressLogic.recordBest(UserProgress.EMPTY, "kotlin-01", 2, 4)
                            p = ProgressLogic.recordBest(p, "kotlin-01", 4, 4)
                            p = ProgressLogic.recordBest(p, "kotlin-01", 1, 4)
                            assertEquals(4 to 4, p.bestScores["kotlin-01"])
                        }
                    """.trimIndent(),
                    expectedOutput = "（摘录·真实通过）该测试在本工程测试套件中运行通过：" +
                        "同一课时交卷三次（2/4、4/4、1/4）后，bestScores 里保留的是最高的 4/4。",
                ),
            ),
            cppComparison = """
                · JUnit 的 @Test ≈ Catch2/GoogleTest 的 TEST 宏：把「一段可独立运行的验证」注册进测试框架，失败即报告并定位。
                · 反引号中文测试名 ≈ GoogleTest 里精心拼的用例名：名字即文档，失败报告第一行就告诉你哪个行为坏了。
                · AAA ≈ 测试的 setup/执行/验证三段惯例：C++ fixture 的 Arrange 同理——准备与断言分干净，失败才好归因。
                · 纯函数才好测 ≈ C++ 「核心库不链接 GUI 框架才进得了 CI」：QuizScorer 不 import Android，JUnit 直接 new 出来就能调。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "AAA 结构里 Act 指的是？",
                    options = listOf(
                        "准备测试数据",
                        "调用被测函数的那一步——最好只有一行",
                        "断言结果",
                        "清理测试环境",
                    ),
                    correctIndex = 1,
                    explanation = "Arrange 准备、Act 调用被测行为、Assert 断言。Act 最好只有一行，被测行为才一眼可辨。",
                ),
                QuizQuestion(
                    question = "QuizScorerTest 不启动模拟器就能跑，原因是？",
                    options = listOf(
                        "JUnit 自带模拟器",
                        "QuizScorer 是纯 Kotlin 类，测试不依赖任何 Android API",
                        "判分逻辑太简单不需要测",
                        "gradle 会自动启动模拟器",
                    ),
                    correctIndex = 1,
                    explanation = "app/src/test 是纯 JVM 单元测试：被测类不 import Android，JUnit 直接实例化调用——这正是模块六分层带来的可测试性。",
                ),
                QuizQuestion(
                    question = "把测试命名为 `全对时 allCorrect 为 true`（反引号中文），好处是？",
                    options = listOf(
                        "编译更快",
                        "测试名即文档：失败报告读名字就知道是哪个行为坏了",
                        "可以少写断言",
                        "反引号名能跳过编译检查",
                    ),
                    correctIndex = 1,
                    explanation = "Kotlin 反引号允许中文句子当函数名；测试失败时报告直接展示这句话——名字可读性就是排查效率。",
                ),
                QuizQuestion(
                    question = "测试 `未作答视为答错` 只传了 mapOf(0 to 0)，另外两题怎么处理？",
                    options = listOf(
                        "跳过不计入 total",
                        "抛异常要求补答",
                        "answers 里没有的键匹配不上 correctIndex，按答错计",
                        "自动选第一个选项",
                    ),
                    correctIndex = 2,
                    explanation = "QuizScorer.score 只在 answers[index] == correctIndex 时计对；缺键就是不等，按答错计，total 始终是题目数。",
                ),
            ),
        ),
        Lesson(
            id = "ship-03",
            title = "构建变体与依赖管理",
            concept = """
                同一份代码，开发时要好调试，发布时要小要快——**buildType** 解决这件事；
                十几个第三方库的坐标与版本写在哪、怎么不改一地——**Version Catalog** 解决这件事。

                ## buildType：debug 与 release

                - debug（默认变体）：debuggable，用自动生成并自动签名的 debug.keystore，不开优化——编译快、可断点
                - release：debuggable 关闭，开启优化。本工程用的是 AGP 9 的新 DSL：`optimization { enable = true }` 并限定了优化作用包范围
                - 本工程的 release **尚未配置 signingConfig**：现在执行 assembleRelease 得到的是未签名包，装不上设备（签名见 ship-04）
                - 本工程没有配置 applicationIdSuffix：debug 与 release 的 applicationId 相同（都是 com.kotlin.learn），会互相覆盖安装
                - 切换方式：Android Studio 左下 Build Variants 面板，或 `./gradlew.bat assembleDebug` / `assembleRelease`

                ## Version Catalog：依赖坐标的唯一清单

                - `gradle/libs.versions.toml` 分三段：[versions] 版本号、[libraries] 坐标别名、[plugins] 插件别名
                - 别名里的连字符在访问器里变成点：别名 androidx-compose-ui → `libs.androidx.compose.ui`
                - 改版本只动 toml 一处；个别库不写 version——Compose 全家的版本由 compose-bom 统一管

                ## 依赖配置的作用域

                - implementation：打进正式包；testImplementation：只在单元测试的 classpath（本工程的 junit 就在这）
                - debugImplementation：只进 debug 变体（本工程的 ui-tooling 预览调试工具）；androidTestImplementation：仪器测试专用
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "本工程 debug/release 的真实差异（纯 Kotlin 复现配置值，真实运行）",
                    code = """
                        // buildType = 同一份代码 + 两套构建配置。以下取自本工程 app/build.gradle.kts 的真实配置：
                        data class Variant(
                            val name: String,
                            val debuggable: Boolean,
                            val signing: String,
                            val optimization: String,
                        )

                        val debug = Variant(
                            name = "debug",
                            debuggable = true,
                            signing = "debug.keystore（构建时自动生成、自动签名）",
                            optimization = "不开优化，编译快、可断点",
                        )
                        val release = Variant(
                            name = "release",
                            debuggable = false,
                            signing = "未配置 signingConfig：当前打出的包未签名，装不上设备",
                            optimization = "开启 optimization（AGP 9 DSL），并限定优化作用包范围",
                        )

                        fun main() {
                            listOf(debug, release).forEach { v ->
                                println("${'$'}{v.name}：debuggable=${'$'}{v.debuggable}")
                                println("  签名：${'$'}{v.signing}")
                                println("  优化：${'$'}{v.optimization}")
                            }
                            println("applicationId 两者相同：com.kotlin.learn（本工程未配置 applicationIdSuffix）")
                        }
                    """.trimIndent(),
                    expectedOutput = "debug：debuggable=true\n" +
                        "  签名：debug.keystore（构建时自动生成、自动签名）\n" +
                        "  优化：不开优化，编译快、可断点\n" +
                        "release：debuggable=false\n" +
                        "  签名：未配置 signingConfig：当前打出的包未签名，装不上设备\n" +
                        "  优化：开启 optimization（AGP 9 DSL），并限定优化作用包范围\n" +
                        "applicationId 两者相同：com.kotlin.learn（本工程未配置 applicationIdSuffix）",
                ),
                CodeExample(
                    description = "Version Catalog 解析：访问器 ↔ 别名 ↔ 真实坐标（条目摘自本工程 libs.versions.toml，真实运行）",
                    code = """
                        // gradle/libs.versions.toml 是依赖坐标的唯一清单（以下全是本工程真实条目）
                        val catalog = mapOf(
                            "androidx-compose-ui" to "androidx.compose.ui:ui（无版本号，由 compose-bom 统一管理）",
                            "androidx-datastore-preferences" to "androidx.datastore:datastore-preferences:1.1.7",
                            "junit" to "junit:junit:4.13.2",
                        )

                        // 访问器把别名里的 - 换成 .：libs.androidx.datastore.preferences ↔ androidx-datastore-preferences
                        fun resolve(accessor: String): String {
                            val alias = accessor.removePrefix("libs.").replace('.', '-')
                            return requireNotNull(catalog[alias]) { "catalog 中没有别名 ${'$'}alias" }
                        }

                        fun main() {
                            println(resolve("libs.androidx.datastore.preferences"))   // implementation(libs.androidx.datastore.preferences)
                            println(resolve("libs.androidx.compose.ui"))              // implementation(libs.androidx.compose.ui)
                            println(resolve("libs.junit"))                            // testImplementation(libs.junit)
                        }
                    """.trimIndent(),
                    expectedOutput = "androidx.datastore:datastore-preferences:1.1.7\n" +
                        "androidx.compose.ui:ui（无版本号，由 compose-bom 统一管理）\n" +
                        "junit:junit:4.13.2",
                ),
            ),
            cppComparison = """
                · buildType ≈ CMake 的 CMAKE_BUILD_TYPE：Debug/Release 同源码两套开关；Android 还允许运行期差异（debuggable、不同签名）。
                · Version Catalog ≈ vcpkg manifest / CMake 预设里的依赖清单：坐标与版本集中一处，别处只引用别名，改一处全局生效。
                · compose-bom ≈ 锁定整套库版本的 baseline：C++ 里用同一个 vcpkg baseline 防止各库版本漂移，BOM 是同一思想。
                · implementation/testImplementation 分组 ≈ target_link_libraries 的私有链接与测试目录单链 gtest：测试依赖不进发布产物。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "本工程 debug 与 release 包的 applicationId 相同吗？",
                    options = listOf(
                        "相同——都是 com.kotlin.learn，因为没配置 applicationIdSuffix",
                        "不同——release 会自动加 .release 后缀",
                        "不同——applicationId 只在 release 里定义",
                        "debug 包没有 applicationId",
                    ),
                    correctIndex = 0,
                    explanation = "本工程未配置 applicationIdSuffix，两种变体的 applicationId 都是 com.kotlin.learn——同时安装两者会互相覆盖，真机联调时要注意。",
                ),
                QuizQuestion(
                    question = "构建脚本里的 libs.androidx.datastore.preferences 对应 Version Catalog 里的哪个别名？",
                    options = listOf(
                        "androidx.datastore.preferences",
                        "androidx-datastore-preferences（访问器把 - 换成了 .）",
                        "libs-androidx-datastore-preferences",
                        "datastore",
                    ),
                    correctIndex = 1,
                    explanation = "别名里的连字符在访问器中变成点：androidx-datastore-preferences → libs.androidx.datastore.preferences，反之亦然。",
                ),
                QuizQuestion(
                    question = "toml 里 androidx-compose-ui 没有写版本，它的版本由谁定？",
                    options = listOf(
                        "Gradle 自动选最新版",
                        "由 compose-bom 统一管理",
                        "编译时随机",
                        "必须手写否则编译失败",
                    ),
                    correctIndex = 1,
                    explanation = "implementation(platform(libs.androidx.compose.bom)) 引入 BOM：Compose 系列库的版本由它统一裁决，个别条目无需再写 version。",
                ),
                QuizQuestion(
                    question = "本工程现在执行 assembleRelease，产物能直接装上设备吗？",
                    options = listOf(
                        "能，release 包自动签名",
                        "能，但要在设置里允许未知来源",
                        "不能——release 未配置 signingConfig，产物未签名，Android 拒绝安装",
                        "能，只是不能上架",
                    ),
                    correctIndex = 2,
                    explanation = "本工程 build.gradle.kts 的 release 块没有 signingConfig：产物是未签名 APK，Android 拒绝安装——上架前要先配签名（ship-04）。",
                ),
            ),
        ),
        Lesson(
            id = "ship-04",
            title = "应用签名与打包",
            concept = """
                发布的第一道门槛是**签名**：Android 拒绝安装没有签名的包；已装应用的更新必须用**同一个密钥库**签，
                否则签名不一致、只能卸载重装。

                ## 密钥库与 keytool

                - keystore = 装着私钥与证书的文件；`keytool -genkey` 生成，常用 RSA 2048、有效期 10000 天（约 27 年）
                - 私钥即身份：**泄漏** = 别人能冒充你发更新；**丢失** = 旧版无法覆盖更新，只能换包名重来
                - keystore 与密码不进 git：走环境变量或本地不入库的配置文件

                ## signingConfig：把密钥库接进构建

                - `signingConfigs { create("release") { storeFile / storePassword / keyAlias / keyPassword } }`
                - 再在 buildTypes.release 里指过去：`signingConfig = signingConfigs.getByName("release")`
                - 本工程尚未配置：这就是 ship-03 说 release 包装不上的原因

                ## APK 与 AAB

                - APK：单文件全量包——不管什么设备，装的都是同一份
                - AAB（`bundleRelease` 产出）：**上传商店的中间格式**；商店按设备的 ABI、屏幕密度、语言拆分下发，用户下载量更小
                - Google Play 要求 AAB；国内多数市场目前仍收 APK——以后台要求为准
                - versionCode 必须单调递增：商店靠这个整数判断「谁是新版本」，忘了加就发不出去
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "keytool 生成密钥库 + signingConfig 接入构建脚本（命令与 DSL 示例，行为描述）",
                    code = """
                        // ① 命令行生成密钥库（一次性，保管好；-validity 10000 天）
                        // keytool -genkey -v -keystore release.keystore -alias learn \
                        //         -keyalg RSA -keysize 2048 -validity 10000

                        // ② app/build.gradle.kts 把密钥库接进 release（密码不进 git）
                        android {
                            signingConfigs {
                                create("release") {
                                    storeFile = file("release.keystore")
                                    storePassword = System.getenv("STORE_PASSWORD")
                                    keyAlias = "learn"
                                    keyPassword = System.getenv("KEY_PASSWORD")
                                }
                            }
                            buildTypes {
                                release {
                                    signingConfig = signingConfigs.getByName("release")
                                }
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "（行为描述）keytool 在当前目录生成 release.keystore（RSA 2048、有效期 10000 天），" +
                        "过程中交互式询问密码与证书信息；配置生效后 assembleRelease 打出的 APK 用它签名，" +
                        "可正常安装并覆盖同密钥库签出的旧版本。",
                ),
                CodeExample(
                    description = "APK 与 AAB：一个全量直装，一个交给商店按设备拆分（两条打包命令，行为描述）",
                    code = """
                        // ./gradlew.bat assembleRelease
                        //   → app/build/outputs/apk/release/app-release.apk
                        //     全设备同一份，直接安装 / 提交收 APK 的市场

                        // ./gradlew.bat bundleRelease
                        //   → app/build/outputs/bundle/release/app-release.aab
                        //     上传 Google Play 等要求 AAB 的商店

                        // 商店拿到 AAB 后按设备拆分下发（示意）：
                        // 设备 A（armeabi-v7a、xxhdpi、中文）→ 基础模块 + 对应 ABI/密度/语言切片
                        // 设备 B（arm64-v8a、xxxhdpi、英文） → 基础模块 + 对应 ABI/密度/语言切片
                    """.trimIndent(),
                    expectedOutput = "（行为描述）assembleRelease 在 outputs/apk/release/ 产出单个全量 APK；" +
                        "bundleRelease 产出 .aab，本机不直接安装，上传后由商店按每台设备的 ABI、屏幕密度与语言只下发需要的切片，" +
                        "用户实际下载体积通常小于全量 APK。",
                ),
            ),
            cppComparison = """
                · 密钥库 ≈ Windows 代码签名证书（.pfx/Authenticode）：私钥即发布者身份，泄漏与丢失的后果同级——更新信任全押在它上面。
                · signingConfig ≈ CI 里的签名步骤：C++ 打包脚本从环境变量读证书密码，这里同样密码不进仓库、只在构建机注入。
                · AAB 按设备拆分 ≈ 按平台分包（x64/ARM64 各发一个安装包）：Android 把裁剪推迟到商店侧，一个 AAB 覆盖所有设备。
                · versionCode 单调递增 ≈ PE 版本资源 + 更新器比对：发行系统靠数字判新旧，忘了递增就发不出去——跨语言同一条坑。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "换了新密钥库给应用发新版本，已装旧版的用户会怎样？",
                    options = listOf(
                        "正常覆盖更新",
                        "签名不一致，无法覆盖安装，只能卸载重装",
                        "手机自动信任新签名",
                        "数据会迁移，应用要重装",
                    ),
                    correctIndex = 1,
                    explanation = "Android 以签名判定「是不是同一个发布者」：密钥库一换签名就变，系统拒绝覆盖安装——密钥库要像生产数据一样保管。",
                ),
                QuizQuestion(
                    question = "keytool 命令里的 -validity 10000 表示？",
                    options = listOf(
                        "密钥长度 10000 位",
                        "证书有效期 10000 天（约 27 年）",
                        "最多签 10000 个应用",
                        "10000 次安装限制",
                    ),
                    correctIndex = 1,
                    explanation = "-validity 单位是天：10000 天约 27 年——证书过期会导致无法再用它发布更新，所以要设得足够长。",
                ),
                QuizQuestion(
                    question = "AAB 与 APK 的本质差别是？",
                    options = listOf(
                        "AAB 压缩率更高",
                        "AAB 是上传商店的中间格式，商店按设备拆分下发；APK 是直接安装的全量包",
                        "AAB 只能装在平板上",
                        "两者只是扩展名不同",
                    ),
                    correctIndex = 1,
                    explanation = "AAB（bundleRelease）不能直接安装：商店拿到后按 ABI/密度/语言切片，每台设备只下载需要的部分；APK 则是谁装都全量。",
                ),
                QuizQuestion(
                    question = "release 密钥库的密码应该放在哪？",
                    options = listOf(
                        "写死在 build.gradle.kts 里，方便同事拿到",
                        "提交到私有 git 仓库",
                        "环境变量或本地不入库的文件，构建时注入",
                        "写进 README 里加密",
                    ),
                    correctIndex = 2,
                    explanation = "密码进了 git 就等于公开：正确做法是环境变量/本地配置注入，仓库里只留占位——与 C++ CI 的证书管理同一条纪律。",
                ),
            ),
        ),
        Lesson(
            id = "ship-05",
            title = "发布渠道与上架",
            concept = """
                签名打完包只是「能安装」，上架是「能被搜到、能被信任」——**材料清单**与**隐私合规**是审核的两道关。

                ## 上架材料清单

                - 应用名称、图标（如 Play 商店页要求 512×512 PNG）、截图（Play 手机端至少 2 张、至多 8 张）、应用描述、分类
                - 隐私政策 URL：**必须可访问**，且与应用实际行为一致——填了不存在的链接是驳回高频原因
                - 版本更新说明（release notes）：随每个版本提交
                - 国内市场（华为、小米、OPPO、vivo 等）另有软件著作权证书、ICP 备案等资质要求——按各市场后台清单逐项准备

                ## 隐私合规：权限声明与用途对应

                - 每声明一条权限，都要能回答「用在哪」：**权限清单是隐私政策与审核问答的逐条依据**
                - 声明了权限却用不上 = 审核风险；用得上的权限也要在政策里写明「收集什么、存在哪、给谁」
                - 本 App 的真实 Manifest **一条 uses-permission 都没有**：课程数据全部存在本地 DataStore，不联网、不收集——
                  隐私政策相应写「本应用不收集任何用户数据」

                ## 渠道差异

                - Google Play：Play Console 表单驱动，审核自动化 + 人工
                - 国内市场：各家后台独立、审核尺度不同，包可能要按渠道加渠道号区分统计
                - 多渠道共用的做法：同一签名密钥库 + 按渠道出包，材料清单做成模板逐渠道微调
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "权限声明与用途对应：Manifest 每条权限都要配得上一句用途说明（示例 + 本工程实况，行为描述）",
                    code = """
                        <!-- 示例：假设将来加入「课程云端更新」，权限声明与用途逐条对应 -->
                        <uses-permission android:name="android.permission.INTERNET" />
                        <!-- 用途：从服务器拉取课程更新（隐私政策相应写明：联网仅下载课程内容，不上传用户数据） -->

                        <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
                        <!-- 用途：下载完成提醒（政策写明：通知仅本地提醒，不含营销推送） -->

                        <!-- 本工程的真实 Manifest：没有上述任何一条——一条 uses-permission 都未声明，
                             课程数据全部存本地 DataStore，不联网、不收集 -->
                    """.trimIndent(),
                    expectedOutput = "（行为描述）审核提交时，市场后台会逐条要求填写权限用途：" +
                        "声明的每条权限都要在材料与隐私政策里找得到对应说明，反之政策写了的收集行为也要有权限支撑。" +
                        "本工程 Manifest 零权限，材料里相应填「不收集任何数据」。",
                ),
                CodeExample(
                    description = "上架材料清单：提交审核前逐项打勾（以 Google Play 为例的通用项，行为描述）",
                    code = """
                        // 提交前的材料清单（Play Console 通用项；国内市场按各自后台增补资质项）
                        data class StoreListing(
                            val appName: String,       // 应用名称（30 字符内）
                            val icon: String,          // 512×512 PNG 图标
                            val screenshots: Int,      // 手机截图：至少 2 张、至多 8 张
                            val description: String,   // 应用描述（简短 + 详细）
                            val privacyUrl: String,    // 隐私政策链接：必须可访问
                            val releaseNotes: String,  // 本版本更新说明
                            val aabFile: String,       // 签名过的 AAB（或市场要求的 APK）
                        )

                        val listing = StoreListing(
                            appName = "Kotlin 学习",
                            icon = "icon-512.png",
                            screenshots = 4,
                            description = "面向 C++ 开发者的 Kotlin 进阶课程",
                            privacyUrl = "https://example.com/privacy",
                            releaseNotes = "新增模块七：工程化与发布",
                            aabFile = "app-release.aab",
                        )
                    """.trimIndent(),
                    expectedOutput = "（行为描述）材料备齐后在市场后台逐项填写、上传 AAB、提交审核；" +
                        "首审通常数个工作日，驳回最常见的原因是隐私政策链接不可访问、权限用途说明缺失、截图与实际功能不符。",
                ),
            ),
            cppComparison = """
                · 权限声明与用途对应 ≈ Windows 应用清单（requestedExecutionLevel）与 UAC 提示：声明即承诺，审核对照清单逐条查。
                · 隐私政策 ≈ GDPR 时代桌面软件同样绕不开的合规门槛：Android 市场把它做成上架必填项，缺失即驳回。
                · 市场材料 ≈ Steam/Epic 的商店页资产：图标、截图、描述决定转化率，跨平台发行的功课是同构的。
                · 多渠道分发 ≈ 多平台发行要适配各商店规则：国内各市场后台独立、资质要求不同——材料模板化才能低成本铺渠道。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "本 App 的 Manifest 声明了几条权限？",
                    options = listOf(
                        "2 条（INTERNET、POST_NOTIFICATIONS）",
                        "1 条（INTERNET）",
                        "0 条——数据全部存本地，不联网",
                        "由 Gradle 自动生成，数量不定",
                    ),
                    correctIndex = 2,
                    explanation = "打开 app/src/main/AndroidManifest.xml 核对：没有任何 uses-permission——课程数据全存本地 DataStore，隐私政策写「不收集任何数据」。",
                ),
                QuizQuestion(
                    question = "隐私政策必须与应用的什么保持一致？",
                    options = listOf(
                        "应用图标风格",
                        "实际收集行为：收集什么、存在哪、给谁",
                        "versionCode",
                        "targetSdk 版本",
                    ),
                    correctIndex = 1,
                    explanation = "政策的可信度来自与行为一致：声明了收集就必须真收集且说明去向，实际没收集就明说不收集——审核与用户投诉都对照这一点。",
                ),
                QuizQuestion(
                    question = "权限清单与隐私政策的关系是？",
                    options = listOf(
                        "互不相干的两份材料",
                        "权限清单是政策与审核问答的逐条依据：每条权限都要能回答用在哪",
                        "权限越多审核越快",
                        "政策里可以不提权限",
                    ),
                    correctIndex = 1,
                    explanation = "审核会逐条要求说明权限用途：权限清单 = 政策的骨架；声明了却说不清用途是驳回与下架的常见原因。",
                ),
                QuizQuestion(
                    question = "国内市场相比 Google Play，通常额外要求什么？",
                    options = listOf(
                        "不需要签名",
                        "软件著作权证书、ICP 备案等资质材料",
                        "必须提交源代码",
                        "只能发 APK 以外的格式",
                    ),
                    correctIndex = 1,
                    explanation = "国内各市场普遍要求软著、备案等资质（按市场后台清单）；Play 则以 Console 表单与 AAB 为主——渠道差异要在排期里留出时间。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "shipping",
        title = "工程化与发布",
        emoji = "🚢",
        lessons = lessons,
    )
}
