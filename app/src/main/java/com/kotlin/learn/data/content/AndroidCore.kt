package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块二：Android 核心概念（12 课）。面向会 C++ 的初学者，讲解 Android 平台基石。 */
object AndroidCore {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "android-01",
            title = "什么是 Activity",
            concept = """
                Activity 是 Android 四大组件之一，代表一个可以与用户交互的"单一屏幕"。它既是 UI 容器，也是系统的管理单元：你只是声明一个类继承 ComponentActivity，但它的创建、恢复、销毁都由系统决定，不是你自己 new 出来的。
                与 C++ 桌面程序最大的差别是：Activity 的实例随时可能被系统销毁重建（如旋转屏幕、内存不足时回收），所以不能依赖成员变量长期保存界面状态。
                本 App 使用 Jetpack Compose，Activity 退化为"宿主"：在 onCreate 里调用 setContent { ... } 挂载 Compose 界面，屏幕上的内容全部交给 Composable 函数渲染。
                每个 Activity 必须在 AndroidManifest.xml 中声明，漏声明会导致启动时直接崩溃。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "一个最小的 Compose Activity：",
                    code = """
                        class MainActivity : ComponentActivity() {   // 继承组件宿主 Activity
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)   // 必须先调用父类实现
                                setContent {                         // 挂载 Compose UI
                                    Text(text = "你好，Activity！")  // 最简单的 Composable
                                }
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "在 Manifest 中注册 Activity（漏写就启动不了）：",
                    code = """
                        <application ...>
                            <activity
                                android:name=".MainActivity"        <!-- 类的相对路径 -->
                                android:exported="true">            <!-- 对外暴露的组件必须显式声明 -->
                                <intent-filter>                     <!-- 声明这是应用入口 -->
                                    <action android:name="android.intent.action.MAIN" />
                                    <category android:name="android.intent.category.LAUNCHER" />
                                </intent-filter>
                            </activity>
                        </application>
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Activity ≈ 一个带系统托管生命周期的"窗口类"：类似 MFC 的 CWnd 或 Qt 的 QWidget，但对象的构造与析构时机由操作系统决定。
                · 不能 `new MainActivity()` 然后自己显示——必须通过 Intent 向系统发起请求，由系统负责实例化。
                · 旋转屏幕默认销毁重建 Activity：相当于"你的窗口对象随时可能被析构再重新构造一次"，成员变量的直觉在这里失效。
                · 想跨"重建"保存状态，用 ViewModel 或 onSaveInstanceState，而不是成员变量。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Activity 的实例由谁创建？",
                    options = listOf(
                        "开发者手动 new 出来",
                        "系统根据 Intent 请求实例化",
                        "通过单例模式全局唯一",
                        "由 Gradle 编译期静态生成",
                    ),
                    correctIndex = 1,
                    explanation = "Activity 由系统（AMS/框架层）按 Intent 请求实例化，开发者只声明类，不自己 new。",
                ),
                QuizQuestion(
                    question = "在使用 Jetpack Compose 的项目里，Activity 的主要职责是？",
                    options = listOf(
                        "用 XML 描述全部界面",
                        "手动管理每个控件的重绘",
                        "调用 setContent 提供 Compose UI 的宿主窗口",
                        "替代 ViewModel 保存全部状态",
                    ),
                    correctIndex = 2,
                    explanation = "Compose 项目中 Activity 退化为入口宿主：onCreate 里 setContent 挂载界面，内容交给 Composable 渲染。",
                ),
                QuizQuestion(
                    question = "忘记在 AndroidManifest.xml 中声明某 Activity，启动它会怎样？",
                    options = listOf(
                        "系统自动创建默认配置",
                        "正常运行，只是没有主题",
                        "抛出异常导致应用崩溃",
                        "编译期直接报错",
                    ),
                    correctIndex = 2,
                    explanation = "Manifest 是组件的注册表，未声明的 Activity 无法被系统启动，startActivity 时会抛 ActivityNotFoundException。",
                ),
                QuizQuestion(
                    question = "默认配置下旋转屏幕，Activity 会发生什么？",
                    options = listOf(
                        "什么都不会发生",
                        "销毁并重建，onCreate 重新执行",
                        "只重走 onResume",
                        "进程被立即杀死",
                    ),
                    correctIndex = 1,
                    explanation = "旋转属于配置变更，默认销毁重建 Activity；未保存的界面状态会丢失，需用 ViewModel 或 rememberSaveable 持有。",
                ),
            ),
        ),
        Lesson(
            id = "android-02",
            title = "Activity 生命周期",
            concept = """
                Activity 从生到死走一条固定的回调链：onCreate() → onStart() → onResume() → 运行中 → onPause() → onStop() → onDestroy()。onCreate 只执行一次，适合做初始化；onStart/onStop 对应"是否可见"，onResume/onPause 对应"是否在前台可交互"。
                生命周期由系统托管：来电时 onPause → onStop，返回时 onRestart → onStart → onResume，用户退出或系统回收时 onDestroy。
                最常见的坑是配置变更（如旋转屏幕）默认销毁并重建整个 Activity，onCreate 会重新执行，界面临时数据丢失。
                Compose 时代的对策：跨重建的数据放 ViewModel，小体量 UI 状态用 rememberSaveable；调试时在每条回调里打日志观察顺序。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "完整生命周期回调（建议亲手打日志看顺序）：",
                    code = """
                        class DetailActivity : ComponentActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                Log.d(TAG, "创建：只执行一次，做初始化")
                            }
                            override fun onStart() { super.onStart(); Log.d(TAG, "可见但还不能交互") }
                            override fun onResume() { super.onResume(); Log.d(TAG, "前台可交互") }
                            override fun onPause() { super.onPause(); Log.d(TAG, "失去焦点，此回调要快") }
                            override fun onStop() { super.onStop(); Log.d(TAG, "完全不可见") }
                            override fun onDestroy() { super.onDestroy(); Log.d(TAG, "销毁，释放资源") }
                            companion object { private const val TAG = "Lifecycle" }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "用 rememberSaveable 让 UI 状态扛过屏幕旋转：",
                    code = """
                        @Composable
                        fun Counter() {
                            // rememberSaveable：跨配置变更自动保存/恢复
                            var count by rememberSaveable { mutableStateOf(0) }
                            Button(onClick = { count++ }) {
                                Text("已点击 ${'$'}count 次")   // 普通变量在重建后会归零
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 生命周期是一整套"系统回调你的虚函数"：你只 override，调用方永远是系统而不是你——和 C++ 里你自己控制构造/析构顺序完全不同。
                · onResume/onPause ≈ 窗口获得/失去激活；onStop ≈ 窗口被完全遮挡。
                · 每个回调都必须先调用 super.xxx()，等价于"必须调用基类实现"，漏掉会直接崩溃。
                · 旋转重建 ≈ 析构后立刻重新构造同一个窗口：C++ 中"成员变量还在"的假设在 Android 上不成立。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Activity 首次启动时，回调的正确顺序是？",
                    options = listOf(
                        "onResume → onStart → onCreate",
                        "onCreate → onStart → onResume",
                        "onCreate → onResume → onStart",
                        "onStart → onCreate → onResume",
                    ),
                    correctIndex = 1,
                    explanation = "顺序固定为 onCreate（创建）→ onStart（可见）→ onResume（前台可交互）。",
                ),
                QuizQuestion(
                    question = "什么情况下 Activity 会经历 onPause 但不经历 onStop？",
                    options = listOf(
                        "用户按下 Home 键回到桌面",
                        "被一个不完全遮挡的对话框样式 Activity 覆盖",
                        "用户按返回键退出",
                        "系统杀死进程",
                    ),
                    correctIndex = 1,
                    explanation = "部分可见（如半透明或对话框样式的 Activity 覆盖在上面）时只触发 onPause；完全不可见才会走到 onStop。",
                ),
                QuizQuestion(
                    question = "onPause 回调中不适合做什么？",
                    options = listOf(
                        "停止动画",
                        "暂停视频播放",
                        "执行耗时的数据写入",
                        "记录日志",
                    ),
                    correctIndex = 2,
                    explanation = "上一个界面不完成 onPause，下一个界面就无法显示，所以在 onPause 里做重活会直接拖慢界面切换，甚至引发卡顿。",
                ),
                QuizQuestion(
                    question = "旋转屏幕后想保留一个计数器的值，最合适的做法是？",
                    options = listOf(
                        "存在 Activity 的成员变量里",
                        "用 rememberSaveable（或 ViewModel）保存",
                        "存在静态全局变量里",
                        "没法保留，只能重新输入",
                    ),
                    correctIndex = 1,
                    explanation = "成员变量随 Activity 重建而丢失；rememberSaveable 负责小体量 UI 状态，跨界面/跨重建的数据放 ViewModel。",
                ),
            ),
        ),
        Lesson(
            id = "android-03",
            title = "布局系统与 Compose 的关系",
            concept = """
                Android 历史上有两套 UI 方案：老的命令式 View/XML 体系（LinearLayout、ConstraintLayout 等布局写在 XML 里，代码中 findViewById 拿到控件再逐个改属性），以及新的声明式 Jetpack Compose（直接用 Kotlin 函数描述界面）。
                Compose 没有"布局文件"的概念，界面就是一个个 @Composable 函数：状态变化时框架自动重新执行受影响的函数（重组），UI 随数据自动更新。
                对 C++ 程序员来说，这更像"UI = f(state)"：你只声明状态到界面的映射，不再手动调用 setText() 之类的命令式更新。
                本 App 完全使用 Compose；但阅读老项目时仍会遇到 View 体系——RecyclerView、Fragment、findViewById 这些术语都来自那套系统。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "声明式 Compose：改状态，界面自动更新：",
                    code = """
                        @Composable
                        fun Greeting(name: String) {              // UI 就是普通 Kotlin 函数
                            var clicked by remember { mutableStateOf(false) }  // 状态
                            Column {                              // 垂直排列，类似 LinearLayout(vertical)
                                Text(if (clicked) "你好，${'$'}name！" else "点我打招呼")
                                Button(onClick = { clicked = true }) {  // 改状态 → 自动重组刷新
                                    Text("打招呼")
                                }
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "命令式（老 View 体系）与声明式（Compose）的对比：",
                    code = """
                        // 命令式：先找到控件，再手动把数据推给每个控件
                        val label = findViewById<TextView>(R.id.label)
                        label.text = "你好"          // 数据一变就要自己更新，控件多了极易漏

                        // 声明式：状态是唯一事实来源，UI 自动跟随
                        Text("你好，${'$'}{user.name}")   // user.name 变了，这一行自动重画
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Compose 之于 View，类似即时模式 UI（如 Dear ImGui）之于传统 retained 控件：函数被反复调用来描述界面，而不是构造一次就常驻的控件树。
                · @Composable 函数不是"构造一次就固定"的 C++ 对象——它会在重组时被再次调用，别在里面写一次性的副作用。
                · remember ≈ 框架管理的缓存槽：把变量放进去，重组时值保留；普通局部变量每次重组都会重置。
                · 重组是"乐观"的，可能被中断取消：耗时操作和副作用要放到事件回调或协程里，不要直接写在 Composable 函数体中。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Compose 中描述 UI 的基本单位是？",
                    options = listOf("XML 布局文件", "@Composable 函数", "Activity", "ViewHolder"),
                    correctIndex = 1,
                    explanation = "Compose 用 Kotlin 函数（@Composable）声明界面，不再需要独立的 XML 布局文件。",
                ),
                QuizQuestion(
                    question = "状态变化后，Compose 会做什么？",
                    options = listOf(
                        "自动重新执行（重组）受影响的 Composable",
                        "手动调用每个控件的 setText",
                        "重启整个 Activity",
                        "什么都不做，等下次点击",
                    ),
                    correctIndex = 0,
                    explanation = "声明式 UI 的核心：状态是唯一事实来源，状态变化触发相关函数自动重组，界面随数据更新。",
                ),
                QuizQuestion(
                    question = "Column 组件的排列方向与老 View 体系中的哪个控件最接近？",
                    options = listOf("垂直方向的 LinearLayout", "RelativeLayout", "FrameLayout", "RecyclerView"),
                    correctIndex = 0,
                    explanation = "Column 自上而下排列子项，等价于 vertical 的 LinearLayout；水平排列则用 Row。",
                ),
                QuizQuestion(
                    question = "关于重组（recomposition），正确的说法是？",
                    options = listOf(
                        "重组一定会执行且不可中断",
                        "重组是乐观的、可能被取消，不应在 Composable 里写副作用",
                        "重组只发生在应用启动时一次",
                        "任何局部变量都能在重组之间保留",
                    ),
                    correctIndex = 1,
                    explanation = "重组可能被打断或丢弃，Composable 函数必须保持无副作用；要跨重组保留数据用 remember。",
                ),
            ),
        ),
        Lesson(
            id = "android-04",
            title = "Intent 与页面跳转",
            concept = """
                Intent 是一个描述"想做什么"的消息对象：既用来在本 App 内启动另一个 Activity（显式 Intent），也用来请求任意 App 完成某个动作，例如"打开这个网址"（隐式 Intent）。
                显式 Intent 直接指名目标类：Intent(this, DetailActivity::class.java)；隐式 Intent 只描述 action 和数据，由系统去匹配能处理它的组件。
                携带数据用 putExtra(key, value)，目标页通过 intent.getStringExtra("key") 等读取——底层是一个可在进程边界传递的 Bundle 键值对。
                接收"下一个页面返回的结果"要用 Activity Result API（registerForActivityResult + launch）；老教程里的 startActivityForResult / onActivityResult 已废弃，不要学。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "显式跳转与隐式跳转：",
                    code = """
                        // 显式：在本 App 内打开详情页（内部跳转都这样写）
                        val intent = Intent(this, DetailActivity::class.java)
                        intent.putExtra("lessonId", "android-04")    // 携带键值对数据
                        startActivity(intent)

                        // 隐式：只描述"要看网页"，让系统找能处理的 App（通常是浏览器）
                        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.android.com"))
                        startActivity(web)
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "Activity Result API：接收下一页的返回值（替代已废弃写法）：",
                    code = """
                        // 在 Activity/Composable 可组合作用域中提前注册回调
                        val launcher = registerForActivityResult(
                            ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            if (result.resultCode == RESULT_OK) {
                                val name = result.data?.getStringExtra("name")  // 读取返回数据
                                Log.d("Jump", "收到返回值：${'$'}name")
                            }
                        }
                        // 在按钮点击等时机发起跳转
                        launcher.launch(Intent(this, PickerActivity::class.java))
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Intent ≈ 发给操作系统的消息：像投递一条请求让系统去创建目标窗口，而不是自己 new 出目标对象。
                · 显式 Intent 是"点名调用"，隐式 Intent 是"按能力查找"——类似用接口契约找实现类。
                · putExtra 的数据放进 Bundle 跨组件边界传递：只能是基本类型、String、Parcelable 等可序列化类型，不能随手塞一个对象指针。
                · 老教程的 startActivityForResult/onActivityResult 已废弃；现代写法一律是 Activity Result API 的注册回调模式。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "在自己 App 内打开详情页，应该用哪种 Intent？",
                    options = listOf(
                        "隐式 Intent，只写 ACTION_VIEW",
                        "显式 Intent，指明目标 Activity 的类",
                        "必须经过浏览器中转",
                        "用 ContentProvider 转发",
                    ),
                    correctIndex = 1,
                    explanation = "App 内部跳转用显式 Intent（Intent(context, Target::class.java)），目标明确、不依赖系统匹配。",
                ),
                QuizQuestion(
                    question = "Intent(ACTION_VIEW, Uri.parse(\"https://...\")) 属于哪种跳转？",
                    options = listOf(
                        "显式跳转",
                        "隐式跳转，由系统匹配处理组件",
                        "进程内函数调用",
                        "非法写法，会编译失败",
                    ),
                    correctIndex = 1,
                    explanation = "只描述动作和数据、不指定组件的是隐式 Intent，系统会找能打开该网址的应用（如浏览器）来处理。",
                ),
                QuizQuestion(
                    question = "官方现行推荐的「取回下一页结果」的方式是？",
                    options = listOf(
                        "startActivityForResult",
                        "重写 onActivityResult",
                        "Activity Result API（registerForActivityResult）",
                        "用全局静态变量传回",
                    ),
                    correctIndex = 2,
                    explanation = "startActivityForResult/onActivityResult 已废弃，现代写法是 registerForActivityResult 注册回调后 launch。",
                ),
                QuizQuestion(
                    question = "putExtra 放入的数据实际存储在？",
                    options = listOf("Bundle 键值对", "SQLite 数据库", "SharedPreferences", "JNI 层的全局引用"),
                    correctIndex = 0,
                    explanation = "Extra 保存在 Bundle 中，跨组件（可能跨进程）传递，因此只支持可序列化类型。",
                ),
            ),
        ),
        Lesson(
            id = "android-05",
            title = "资源系统（res/）",
            concept = """
                res/ 目录存放一切非代码资产：values/strings.xml 字符串、drawable/ 图片与矢量图、mipmap/ 应用图标、values/ 颜色与主题、xml/ 配置文件等。构建时编译器为每个资源生成整数 ID，代码通过 R 类引用（如 R.string.app_name）。
                资源系统的精髓是"限定符变体"：values-zh/ 放中文文案、drawable-xxhdpi/ 放高分辨率图片，系统运行时按设备语言、屏幕密度自动选择，代码里不用写任何 if。
                尺寸单位要用 dp（密度无关像素）和 sp（还会随用户字体大小缩放，专用于文字），不要用 px 硬编码，否则在不同屏幕上观感不一致。
                Compose 中通过 stringResource(R.string.title)、painterResource(R.drawable.icon) 等 API 访问资源。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "字符串资源：文案集中管理，天然支持多语言：",
                    code = """
                        <!-- res/values/strings.xml（默认语言） -->
                        <resources>
                            <string name="app_name">Kotlin 学习</string>
                            <string name="start_button">开始学习</string>
                        </resources>

                        <!-- res/values-en/strings.xml（英文变体，文件名相同） -->
                        <resources>
                            <string name="start_button">Start</string>
                        </resources>
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "Compose 中使用资源与正确的尺寸单位：",
                    code = """
                        Text(
                            text = stringResource(R.string.start_button),  // 引用字符串资源
                            fontSize = 16.sp,   // 文字用 sp：随系统字体设置缩放
                        )
                        Icon(
                            painter = painterResource(R.drawable.ic_star), // 引用矢量图
                            contentDescription = null,
                        )
                        Modifier.width(48.dp)   // 尺寸用 dp：与屏幕密度无关
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · R 类 ≈ 编译期自动生成的资源 ID 常量表，类似构建工具替你维护的枚举；引用的资源不存在会直接编译报错，比运行期查字符串安全。
                · 限定符变体 ≈ "运行时按环境自动选择的资源重载"：把 if(语言/密度) 的分支从代码里挪进了文件系统。
                · dp/sp 与 px 的关系，类似逻辑坐标与物理像素的关系——Win32 里用 DPI 缩放解决的是同一个问题。
                · 资源 ID 是 int 不是指针：真正的数据由系统在打包后按需加载。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "把界面文案放进 strings.xml 的主要好处是？",
                    options = listOf(
                        "让 APK 体积更小",
                        "集中管理文案并便于多语言本地化",
                        "字符串在运行时速度更快",
                        "编译器要求必须这样做",
                    ),
                    correctIndex = 1,
                    explanation = "字符串资源化后可按语言提供 values-zh、values-en 等变体，系统自动选择，也避免文案散落在代码各处。",
                ),
                QuizQuestion(
                    question = "设置文字大小的推荐单位是？",
                    options = listOf("px", "dp", "sp", "pt"),
                    correctIndex = 2,
                    explanation = "sp（scale-independent pixels）除密度外还会跟随用户的系统字体大小设置缩放，是无障碍的正确姿势。",
                ),
                QuizQuestion(
                    question = "drawable-mdpi/、drawable-xxhdpi/ 这类目录的作用是？",
                    options = listOf(
                        "存放不同版本的 APK",
                        "系统按屏幕密度自动挑选合适的图片",
                        "按 Android 版本区分 API",
                        "只是普通文件夹，无特殊含义",
                    ),
                    correctIndex = 1,
                    explanation = "密度限定符目录让系统为当前设备挑选分辨率最合适的图片，代码中统一引用 R.drawable.xxx 即可。",
                ),
                QuizQuestion(
                    question = "代码中引用名为 start_button 的字符串资源的写法是？",
                    options = listOf("R.string.start_button", "strings.start_button", "@string/start_button", "res.string.start_button"),
                    correctIndex = 0,
                    explanation = "Kotlin/Java 代码中用 R.string.start_button；@string/start_button 是 XML 里的引用语法，两者别混。",
                ),
            ),
        ),
        Lesson(
            id = "android-06",
            title = "AndroidManifest.xml",
            concept = """
                AndroidManifest.xml 是 App 的"身份证 + 说明书"：固定放在 app/src/main/ 下，每个模块必有一份。系统在安装和运行前都靠它了解这个 App 有哪些组件、需要什么权限、入口在哪里。
                四大组件（Activity、Service、BroadcastReceiver、ContentProvider）都必须在这里声明，漏声明的组件无法启动。对外可被其他 App 启动的组件还要显式设置 android:exported="true"。
                <intent-filter> 声明组件能响应哪些隐式 Intent；应用入口就是那个带 MAIN action 与 LAUNCHER category 的 Activity，它决定了桌面上有没有图标。
                权限（<uses-permission>）、硬件特性（<uses-feature>）也声明在这里；applicationId、minSdk 等则写在 build.gradle.kts 中，构建时两者会合并。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "Manifest 的典型结构（节选自本 App）：",
                    code = """
                        <!-- app/src/main/AndroidManifest.xml -->
                        <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                            <uses-permission android:name="android.permission.INTERNET" />  <!-- 声明所需权限 -->

                            <application
                                android:icon="@mipmap/ic_launcher"
                                android:label="@string/app_name"
                                android:theme="@style/Theme.App">

                                <!-- 入口 Activity：MAIN + LAUNCHER 才会出现在桌面 -->
                                <activity android:name=".MainActivity" android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>

                                <!-- 内部页面：只被本 App 启动，exported 保持 false -->
                                <activity android:name=".DetailActivity" android:exported="false" />
                            </application>
                        </manifest>
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Manifest ≈ 链接期符号表 + 运行配置清单：组件不声明就等于"符号未链接"，一启动就崩。
                · <intent-filter> ≈ 把"我能处理这类请求"注册进系统能力表，其他 App 才能按契约找到你。
                · 在 Manifest 声明权限只是"申请意愿"，危险权限还需要运行时用户点头（见下一课），两步缺一不可。
                · applicationId、minSdk 在 build.gradle.kts 里配置而不在 Manifest 里——构建时 Gradle 会把配置合并注入。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "AndroidManifest.xml 的核心作用是？",
                    options = listOf(
                        "存放界面布局",
                        "向系统声明组件、权限与应用入口",
                        "保存用户设置",
                        "定义 Gradle 构建任务",
                    ),
                    correctIndex = 1,
                    explanation = "Manifest 是系统的「组件注册表」：所有组件、权限、入口 intent-filter 都要在里面声明。",
                ),
                QuizQuestion(
                    question = "App 桌面图标入口由什么决定？",
                    options = listOf(
                        "applicationId 的名字",
                        "带 MAIN action + LAUNCHER category 的 intent-filter",
                        "Manifest 文件名",
                        "图标文件的分辨率",
                    ),
                    correctIndex = 1,
                    explanation = "系统把带 MAIN/LAUNCHER intent-filter 的 Activity 作为启动入口，创建对应的桌面快捷方式。",
                ),
                QuizQuestion(
                    question = "下列哪项配置不在 Manifest 中，而在 build.gradle.kts 里？",
                    options = listOf("uses-permission", "activity 声明", "applicationId（包名）", "intent-filter"),
                    correctIndex = 2,
                    explanation = "applicationId、minSdk、targetSdk、依赖等都写在 build.gradle.kts，构建时合并进最终清单。",
                ),
                QuizQuestion(
                    question = "只被本 App 内部启动的 Activity，exported 应设为？",
                    options = listOf(
                        "true，方便调试",
                        "false，不对外暴露更安全",
                        "必须省略此属性",
                        "设为 null",
                    ),
                    correctIndex = 1,
                    explanation = "targetSdk 31+ 要求带 intent-filter 的组件显式声明 exported；仅内部使用的组件保持 false 是安全基线。",
                ),
            ),
        ),
        Lesson(
            id = "android-07",
            title = "权限模型",
            concept = """
                Android 权限分几类：普通权限（如 INTERNET）在 Manifest 声明后自动授予；危险权限（定位、相机、麦克风、通讯录等敏感数据）从 Android 6.0 起必须在运行时向用户弹窗请求，用户可以拒绝。
                标准流程三步：Manifest 里 <uses-permission> 声明 → 用 ContextCompat.checkSelfPermission 检查 → 未授权则用 Activity Result API 发起请求。
                Android 10 起进一步收紧：位置只支持"仅在使用该应用时允许"，后台定位要单独申请；Android 11 起位置对话框多了"仅这一次"的一次性授权选项。权限模型逐年趋严，设计功能时要按"随时会被拒"来做降级方案。
                Compose 中用 rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) 发起请求，根据授权结果切换 UI。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "运行时请求相机权限的完整三步：",
                    code = """
                        // 第 1 步：AndroidManifest.xml 声明（没有这句，运行时请求也无效）
                        // <uses-permission android:name="android.permission.CAMERA" />

                        // 第 2、3 步：检查 + 请求（Compose 写法）
                        val launcher = rememberLauncherForActivityResult(
                            ActivityResultContracts.RequestPermission()
                        ) { granted ->                       // 用户选择后的回调
                            if (granted) { /* 打开相机 */ } else { /* 引导去设置或降级 */ }
                        }

                        Button(onClick = {
                            val granted = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) { /* 已授权，直接用 */ }
                            else launcher.launch(Manifest.permission.CAMERA)  // 弹系统对话框
                        }) { Text("使用相机") }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 危险权限像"每次使用前要用户签字的授权书"，不是 C++ 桌面程序"装完即全权"的思路。
                · checkSelfPermission ≈ 运行时能力探测：永远先查询再使用，不要凭"我申请过"的假设。
                · 权限对话框由系统弹出，用户可能永远拒绝——业务逻辑必须按"拒绝也能优雅降级"设计。
                · INTERNET 属于普通权限：Manifest 声明即可用，不会弹窗——新手常见误解是以为联网也要运行时请求。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "危险权限的运行时请求机制从哪个 Android 版本开始？",
                    options = listOf("Android 4.4", "Android 6.0", "Android 10", "Android 13"),
                    correctIndex = 1,
                    explanation = "Android 6.0（API 23）引入运行时权限：安装时不再自动授予危险权限，必须在运行时弹窗征得同意。",
                ),
                QuizQuestion(
                    question = "INTERNET 权限的正确处理方式是？",
                    options = listOf(
                        "运行时弹窗请求用户同意",
                        "仅在 Manifest 声明即可，自动授予",
                        "无需任何声明即可联网",
                        "需要用户在设置里手动开启",
                    ),
                    correctIndex = 1,
                    explanation = "INTERNET 是普通权限：Manifest 声明后安装时自动授予，不需要运行时请求。",
                ),
                QuizQuestion(
                    question = "只写了运行时请求代码、却忘了在 Manifest 声明 <uses-permission>，结果是？",
                    options = listOf(
                        "请求正常弹出",
                        "权限直接被判为拒绝，功能不可用",
                        "编译失败",
                        "系统自动补上声明",
                    ),
                    correctIndex = 1,
                    explanation = "Manifest 声明是前提，缺失时系统不会授予该权限，checkSelfPermission 永远返回 denied。",
                ),
                QuizQuestion(
                    question = "用户拒绝了相机权限，比较恰当的后续做法是？",
                    options = listOf(
                        "立刻再弹一次对话框",
                        "死循环请求直到同意",
                        "解释用途并引导去系统设置，同时提供降级体验",
                        "静默崩溃",
                    ),
                    correctIndex = 2,
                    explanation = "反复骚扰式弹窗会被系统限制且招致反感；正确做法是说明用途、提供跳设置的入口，并在无权限时优雅降级。",
                ),
            ),
        ),
        Lesson(
            id = "android-08",
            title = "Context 是什么",
            concept = """
                Context 是 Android 最核心的抽象之一：它是"应用环境/系统服务访问句柄"，启动 Activity、读取资源、获取系统服务（通知、布局、数据库）都要经过它。
                常见实现：Activity 本身就是 Context，Application 是进程内全局唯一的 Context，Service 也是。Activity 的 Context 带主题与界面能力；applicationContext 的生命周期与进程一样长。
                经典大坑：把 Activity 的 Context 存进静态变量或长生命周期单例，Activity 销毁后仍被持有而无法回收——这就是 Android 最常见的内存泄漏。
                原则：需要长期持有的场景一律用 applicationContext；Compose 里需要 Context 时用 LocalContext.current 取。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "通过 Context 访问系统服务与资源：",
                    code = """
                        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)   // 系统服务
                                as ConnectivityManager
                        val appName = context.getString(R.string.app_name)     // 读字符串资源
                        val screenW = context.resources.displayMetrics.widthPixels  // 屏幕信息

                        // 启动组件也依赖 Context（所以 Intent 第一个参数是它）
                        context.startActivity(Intent(context, DetailActivity::class.java))
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "单例中应该持有哪种 Context？",
                    code = """
                        object UserCache {
                            // lateinit var ctx: Context   // 坑：若存的是 Activity，会把它泄漏
                            lateinit var appContext: Context   // 对：Application Context 与进程同寿命
                            fun init(context: Context) {
                                appContext = context.applicationContext   // 显式取应用级 Context
                            }
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Context ≈ "系统服务访问句柄"：类似持有指向 OS 服务管理器的指针，几乎一切系统能力都从它取。
                · Activity/Service/Application 都是 Context 的子类——像一组生命周期长短不同的句柄，用错层级就出事。
                · 静态持有 Activity Context ≈ 把栈对象地址存进全局指针：对象析构后你仍握着它，内存泄漏。
                · getApplicationContext() 相当于官方提供的"长寿命句柄"，是跨对象持有时唯一正确的选择。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Context 在 Android 中的角色最接近于？",
                    options = listOf(
                        "UI 控件容器",
                        "访问应用环境与系统服务的句柄",
                        "数据库连接池",
                        "网络请求队列",
                    ),
                    correctIndex = 1,
                    explanation = "Context 提供应用环境信息与系统服务的统一入口：取资源、起组件、拿服务都靠它。",
                ),
                QuizQuestion(
                    question = "长生命周期的单例应该持有哪种 Context？",
                    options = listOf(
                        "Activity 传来的 this",
                        "applicationContext",
                        "View 的 context",
                        "随便哪种都行",
                    ),
                    correctIndex = 1,
                    explanation = "Application Context 与进程同寿命，不会拖住任何界面对象；持有 Activity Context 会造成内存泄漏。",
                ),
                QuizQuestion(
                    question = "静态变量持有 Activity 的 Context 会发生什么？",
                    options = listOf(
                        "没有影响",
                        "Activity 销毁后无法回收，造成内存泄漏",
                        "编译报错",
                        "Context 自动变空",
                    ),
                    correctIndex = 1,
                    explanation = "静态引用的存活期比 Activity 长，Activity 退役后仍被引用，GC 无法回收——这是最经典的 Android 内存泄漏。",
                ),
                QuizQuestion(
                    question = "在 Composable 函数中获取 Context 的惯用写法是？",
                    options = listOf(
                        "LocalContext.current",
                        "new Context()",
                        "Context.getInstance()",
                        "全局静态变量",
                    ),
                    correctIndex = 0,
                    explanation = "CompositionLocal 提供 Compose 树中的环境对象，LocalContext.current 即当前 Composable 所在的 Context。",
                ),
            ),
        ),
        Lesson(
            id = "android-09",
            title = "App 进程与主线程",
            concept = """
                每个 App 默认运行在自己独立的 Linux 进程里，进程内有一个 ART 虚拟机实例；系统会按内存压力随时杀掉后台进程，用户毫无感知——所以永远不要假设进程常驻，重要数据要主动持久化。
                主线程（UI 线程）上跑着一个事件循环：Looper 不停地从 MessageQueue 取消息并分发，输入事件、生命周期回调、界面绘制全部在这条线程处理。这个模型与 Win32 的消息循环几乎同构。
                在主线程做网络请求会直接抛 NetworkOnMainThreadException；做耗时磁盘 IO 则可能触发 ANR（应用无响应）：输入事件 5 秒内没处理完，系统就弹出"无响应"对话框。
                现代解法是 Kotlin 协程：耗时工作放到 Dispatchers.IO/Default，完成后切回 Dispatchers.Main 更新 UI。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "协程标准套路：IO 线程干活，主线程更新 UI：",
                    code = """
                        lifecycleScope.launch {                        // 作用域绑定生命周期，默认主线程
                            val user = withContext(Dispatchers.IO) {   // 切到 IO 线程池
                                loadUserFromDatabase()                 // 网络/数据库等耗时操作
                            }
                            userName = user.name                       // 自动回到主线程，安全更新 UI
                        }

                        // 反面教材：主线程直接联网，运行即抛异常
                        // val body = URL("https://example.com").readText()
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · 主线程 ≈ Win32 消息循环线程：GetMessage/DispatchMessage 换成了 Looper/Handler，本质是同一个事件循环模型。
                · ANR ≈ 消息队列被堵死超过 5 秒，系统判定窗口僵死；C++ GUI 里长任务卡界面是同一种病，药方也一样——别堵消息循环。
                · 进程随时可能被杀 ≈ "操作系统可能随时析构你的整个进程"：持久化必须在关键时机主动完成，不能等"退出时保存"。
                · 协程 ≈ 轻量级可挂起的任务：比裸 pthread 线程池便宜得多，作用域自动传播取消，避免手写线程同步。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Android 主线程的运行模型最类似于？",
                    options = listOf(
                        "Win32 消息循环",
                        "多进程共享内存",
                        "无锁无队列的裸循环",
                        "GPU 渲染管线",
                    ),
                    correctIndex = 0,
                    explanation = "主线程用 Looper 不断从 MessageQueue 取消息分发，与 Win32 的 GetMessage/DispatchMessage 消息循环同构。",
                ),
                QuizQuestion(
                    question = "在主线程直接发起网络请求会怎样？",
                    options = listOf(
                        "正常运行",
                        "抛出 NetworkOnMainThreadException",
                        "自动切到后台线程",
                        "只是变慢",
                    ),
                    correctIndex = 1,
                    explanation = "Android 明确禁止主线程网络操作，运行时直接抛 NetworkOnMainThreadException，逼迫开发者异步化。",
                ),
                QuizQuestion(
                    question = "关于 ANR（应用无响应），正确的说法是？",
                    options = listOf(
                        "输入事件约 5 秒未处理即触发",
                        "只在应用崩溃时出现",
                        "只发生在后台进程上",
                        "是内存不足导致的死机",
                    ),
                    correctIndex = 0,
                    explanation = "主线程无法及时处理输入（约 5 秒）等事件时，系统弹出 ANR 对话框，根因几乎总是主线程被耗时操作堵住。",
                ),
                QuizQuestion(
                    question = "更新 UI 必须在哪个线程进行？",
                    options = listOf("任意线程", "主线程（UI 线程）", "IO 线程", "新建的子线程"),
                    correctIndex = 1,
                    explanation = "UI 不是线程安全的，一切界面更新都必须发生在主线程；子线程算完要用 Dispatchers.Main 切回来。",
                ),
            ),
        ),
        Lesson(
            id = "android-10",
            title = "Gradle 构建体系",
            concept = """
                Gradle 是 Android 官方构建系统：用 Kotlin 脚本（build.gradle.kts）声明"怎么构建"，构建时按依赖图调度任务——角色类似 C++ 世界的 CMake，但配置本身就是代码。
                模块级 build.gradle.kts 里最关键的配置：compileSdk（用哪个版本的 API 编译）、minSdk（最低支持到哪个系统版本）、targetSdk（声明已适配到的版本，直接影响系统对你的行为约束）、以及依赖列表。
                新项目用 Version Catalog（gradle/libs.versions.toml）集中管理依赖与版本号，多模块共享一份"版本真相"，避免版本漂移。
                buildTypes 构建变体默认有 debug（可调试、自动用 debug 签名）与 release（优化混淆、需正式签名）：同一份代码打包出的不同产物，上架商店用的是 release。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "app/build.gradle.kts 的核心配置（节选）：",
                    code = """
                        android {
                            namespace = "com.kotlin.learn"
                            compileSdk = 37                    // 用 API 37 的 SDK 编译

                            defaultConfig {
                                applicationId = "com.kotlin.learn"  // 应用唯一标识
                                minSdk = 24                // 最低支持 Android 7.0
                                targetSdk = 37             // 声明已适配的系统版本
                                versionCode = 1            // 内部版本号，整数递增
                                versionName = "1.0"        // 用户可见的版本名
                            }
                            buildTypes {
                                release {
                                    isMinifyEnabled = true // 开启代码压缩与混淆
                                }
                            }
                        }

                        dependencies {
                            implementation("androidx.core:core-ktx:1.13.1")  // 添加依赖
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "Version Catalog：版本集中管理（gradle/libs.versions.toml）：",
                    code = """
                        [versions]
                        coreKtx = "1.13.1"

                        [libraries]
                        androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }

                        # 之后在 build.gradle.kts 中引用：
                        # implementation(libs.androidx.core.ktx)
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Gradle 之于 Android ≈ CMake 之于 C++：都是声明式构建脚本；区别是 Gradle 的配置即代码（Kotlin/Groovy），且自带依赖管理与中央仓库。
                · minSdk ≈ 最低支持的 OS 版本；compileSdk ≈ 用哪套"头文件"编译；targetSdk 决定系统对你启用哪些新行为约束——三者各管一头，别混。
                · buildTypes ≈ Debug/Release 构建配置；productFlavors（多渠道）类似交叉编译的多目标平台。
                · Version Catalog ≈ 集中的版本号头文件：所有模块 include 同一份，杜绝各自写版本导致的漂移。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "minSdk = 24 的含义是？",
                    options = listOf(
                        "用 API 24 编译",
                        "应用最低支持 Android 7.0 的设备",
                        "应用至少占 24MB 内存",
                        "需要 24 位色深屏幕",
                    ),
                    correctIndex = 1,
                    explanation = "minSdk 声明可安装的最低系统版本，低于该版本的设备无法安装；compileSdk 才是「用什么 API 编译」。",
                ),
                QuizQuestion(
                    question = "debug 与 release 构建变体的主要区别是？",
                    options = listOf(
                        "debug 可调试、未优化；release 优化混淆、需正式签名",
                        "只是名字不同，产物完全一样",
                        "debug 用于上架，release 用于开发",
                        "release 不能安装到手机",
                    ),
                    correctIndex = 0,
                    explanation = "debug 变体带调试信息、用 debug 密钥签名；release 开启优化混淆、必须用正式签名才能上架。",
                ),
                QuizQuestion(
                    question = "Version Catalog 对应的文件是？",
                    options = listOf(
                        "gradle.properties",
                        "gradle/libs.versions.toml",
                        "settings.gradle.kts",
                        "local.properties",
                    ),
                    correctIndex = 1,
                    explanation = "新项目默认在 gradle/libs.versions.toml 集中声明版本与依赖坐标，各模块通过 libs.xxx 引用。",
                ),
                QuizQuestion(
                    question = "应用在系统与商店中的唯一标识是哪个配置项？",
                    options = listOf("namespace", "applicationId", "versionName", "minSdk"),
                    correctIndex = 1,
                    explanation = "applicationId 是应用安装后在系统与商店里的唯一包名标识；namespace 是代码的包命名空间，两者可以不同。",
                ),
            ),
        ),
        Lesson(
            id = "android-11",
            title = "调试技巧",
            concept = """
                Logcat 是 Android 最重要的调试输出通道：android.util.Log 的五级日志 Log.v / Log.d / Log.i / Log.w / Log.e（冗余→错误）都汇集于此，可按 tag、级别、包名过滤。
                惯例是每个类配一个 TAG 常量（private const val TAG = "MainActivity"）；注意 Log.d 这类调用在 release 包里不会自动消失，敏感日志要自己控制或交给统一日志库。
                断点调试体验与 C++ 基本一致：Android Studio 里点行号挂断点、Debug 模式启动，命中后可查看变量、单步执行，还支持条件断点与日志断点（命中时只打日志不停下）。
                进阶工具：StrictMode 抓"主线程做 IO"这类违规操作，Profiler 看内存/CPU/网络，Layout Inspector 与 Compose Preview 检查界面。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "规范的日志用法：类内 TAG + 分级输出：",
                    code = """
                        class DetailActivity : ComponentActivity() {
                            companion object { private const val TAG = "DetailActivity" }  // 一类一 TAG

                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                Log.d(TAG, "onCreate: id=${'$'}{intent.getStringExtra("id")}")  // 调试信息
                                Log.w(TAG, "数据为空，使用默认值")                     // 警告
                                // Log.e(TAG, "加载失败", e)                             // 错误 + 异常堆栈
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "StrictMode：让主线程违规操作立刻暴露：",
                    code = """
                        // 在 debug 包的 Application/入口处开启
                        if (BuildConfig.DEBUG) {
                            StrictMode.setThreadPolicy(
                                StrictMode.ThreadPolicy.Builder()
                                    .detectAll()        // 检测主线程磁盘 IO、网络等
                                    .penaltyLog()       // 违规时打日志（而非直接崩溃）
                                    .build()
                            )
                        }
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Logcat ≈ 系统级日志总线：不只你的 App，系统进程与其他应用的日志也能看到，比单纯看自己程序的控制台输出信息量大得多。
                · TAG 过滤 ≈ 结构化的日志分类，替代在巨型日志文件里 grep。
                · StrictMode ≈ Debug 版运行时检查器：把"主线程干重活"这类坏味道变成显式日志，思路类似打开更严格的运行时检查。
                · ANR 时系统会留下 trace（记录卡住时的调用栈）≈ 轻量版的核心转储，直接看主线程卡在哪一行。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "android.util.Log 日志级别从低到高排列正确的是？",
                    options = listOf(
                        "V → D → I → W → E",
                        "E → W → I → D → V",
                        "D → V → I → E → W",
                        "I → V → D → W → E",
                    ),
                    correctIndex = 0,
                    explanation = "Verbose、Debug、Info、Warn、Error 由轻到重排列，过滤时可按级别截取。",
                ),
                QuizQuestion(
                    question = "每个类定义 TAG 常量的目的是？",
                    options = listOf(
                        "提高日志输出速度",
                        "在 Logcat 中快速过滤定位该类的日志",
                        "编译器强制要求",
                        "给崩溃日志加密",
                    ),
                    correctIndex = 1,
                    explanation = "按 TAG 过滤是 Logcat 最高效的用法，一类一 TAG 能立刻聚焦目标模块的输出。",
                ),
                QuizQuestion(
                    question = "想自动发现「在主线程做了磁盘读写」这类问题，用什么工具？",
                    options = listOf("StrictMode", "Lint", "R8", "AAPT"),
                    correctIndex = 0,
                    explanation = "StrictMode 在运行时检测主线程磁盘/网络等违规操作并告警，是抓「堵主线程」问题的利器。",
                ),
                QuizQuestion(
                    question = "想持续观察 App 的内存与 CPU 占用，应该用？",
                    options = listOf("Logcat", "Android Studio Profiler", "device Monitor 的截图", "Gradle 面板"),
                    correctIndex = 1,
                    explanation = "Profiler 提供内存、CPU、网络、能耗的实时曲线与采样分析，是性能排查的入口工具。",
                ),
            ),
        ),
        Lesson(
            id = "android-12",
            title = "常见术语表",
            concept = """
                模块二到此收尾。下面是 Android 开发最高频的术语，每个一句话，建议当检查清单用：
                · Activity：一个可交互的"屏幕"，生命周期由系统托管。
                · Fragment：Activity 内可复用的子界面模块，有自己的生命周期；Compose 时代新项目已少用。
                · View / ViewGroup：老 UI 体系的控件与容器（TextView、LinearLayout 等）。
                · Composable：Compose 声明式 UI 的函数单位，在 Compose 世界里替代 View 的角色。
                · Context：应用环境句柄，访问系统服务、资源、启动组件的统一入口。
                · Intent：组件之间"想做什么"的消息对象，用于启动 Activity/Service、发送广播。
                · Toast / Snackbar：轻量提示；Toast 系统级弹出自动消失，Snackbar 在界面内、可带操作按钮。
                · APK / AAB：Android 安装包与上架格式；AAB 交给商店按设备拆分成更小的下载包。
                · SDK / NDK：官方开发工具包（Java/Kotlin API）；NDK 用于 C/C++ 开发，通过 JNI 与 Kotlin 互调。
                · ART：Android 运行时，执行 DEX 字节码；前身是 Dalvik。
                · AAR / JAR：Android 库包（代码 + 资源）与纯 Java 库包。
                · Gradle / AGP：官方构建工具，以及其上的 Android 构建插件。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "这些术语在真实代码里的位置：",
                    code = """
                        class MainActivity : ComponentActivity() {   // Activity：屏幕入口
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)   // Context 的生命周期回调
                                setContent {
                                    Button(onClick = {
                                        Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
                                    }) { Text("保存") }              // Toast：轻量提示
                                }
                            }
                        }
                    """.trimIndent(),
                ),
                CodeExample(
                    description = "构建产物里的 APK 与 AAB：",
                    code = """
                        app/build/outputs/
                        ├── apk/debug/app-debug.apk         ← 本地调试直接安装（APK）
                        └── bundle/release/app-release.aab  ← 上架 Google Play（AAB）
                    """.trimIndent(),
                ),
            ),
            cppComparison = """
                · Context ≈ 系统服务句柄，Intent ≈ 组件间消息，Activity ≈ 系统托管的窗口类——这三个词是 Android 的一切底座。
                · SDK vs NDK ≈ 用官方高级 API vs 用 C/C++ 贴近底层（走 JNI 绑定），类似 Windows 上 C# 与 C++ 的分工。
                · ART ≈ Android 版的 JVM/CLR：DEX 字节码按 AOT + JIT 混合执行；Dalvik 是它的前任。
                · APK/AAB ≈ 安装包格式：APK 是可直接安装的成品，AAB 是"源包"，商店按设备 CPU/语言/屏幕拆分后下发。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "想用 C/C++ 编写 Android 代码，需要使用的是？",
                    options = listOf("SDK", "NDK", "AAR", "AGP"),
                    correctIndex = 1,
                    explanation = "NDK（Native Development Kit）提供 C/C++ 工具链，通过 JNI 与 Kotlin/Java 互调。",
                ),
                QuizQuestion(
                    question = "ART 的前身是？",
                    options = listOf("JVM", "Dalvik", "Hotspot", "V8"),
                    correctIndex = 1,
                    explanation = "Dalvik 是 Android 早期的运行时（1.0 起就存在）；ART 在 4.4 引入（实验性），5.0 起全面取代 Dalvik。",
                ),
                QuizQuestion(
                    question = "上架 Google Play 时应提交的格式是？",
                    options = listOf("APK", "AAB", "JAR", "DEX"),
                    correctIndex = 1,
                    explanation = "Google Play 要求上传 AAB（Android App Bundle），商店按设备配置拆分生成优化的 APK 下发。",
                ),
                QuizQuestion(
                    question = "Toast 与 Snackbar 的区别是？",
                    options = listOf(
                        "Toast 在界面内可带操作按钮；Snackbar 系统级弹出",
                        "Toast 系统级弹出自动消失；Snackbar 在界面内、可带操作按钮",
                        "两者完全相同",
                        "Snackbar 只能在后台线程显示",
                    ),
                    correctIndex = 1,
                    explanation = "Toast 由系统弹出、不依赖界面且不可交互；Snackbar 属于界面的一部分，可附带「撤销」等操作按钮。",
                ),
                QuizQuestion(
                    question = "JNI 的作用是？",
                    options = listOf(
                        "压缩安装包体积",
                        "让 Java/Kotlin 代码与 C/C++ 本地代码互相调用",
                        "加密 Manifest",
                        "生成 R 类",
                    ),
                    correctIndex = 1,
                    explanation = "JNI（Java Native Interface）是 JVM 系平台的标准桥接层，Kotlin/C++ 互调、NDK 开发都经过它。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "android-core",
        title = "Android 核心概念",
        emoji = "🤖",
        lessons = lessons,
    )
}
