package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块五：数据与网络（7 课），从本地存储到网络请求与仓库模式。 */
object DataNetwork {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "data-01",
            title = "文件存储",
            concept = """
                最直接的数据持久化就是把内容写进文件。Android 里先搞清楚**存到哪里**，再动手写代码。

                ## 存到哪里

                - 内部存储：`context.filesDir` 下的私有目录，**无需任何权限**，卸载 App 即清空——首选
                - 外部存储：共享空间，需要权限，适合导出给其他 App 使用的大文件

                ## File 的常用读写

                - `writeText`：覆盖写入；`appendText`：追加写入——打开、写入、关闭一步完成，不用手动 close
                - `readText`：一次性读出全文；`readLines`：按行读成 `List<String>`
                - `useLines`：流式逐行读取，大文件不占整块内存

                ## 适用边界

                - 配置说明、缓存、日志、导出数据——文件很合适
                - 需要按条件查询、频繁局部更新、跨版本演进的结构化数据——交给下一课的 DataStore 或 Room
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "覆盖写入、追加写入与按行读回",
                    code = """
                        import java.io.File

                        fun main() {
                            val file = File("notes.txt")               // App 里用 context.filesDir 下的真实路径
                            file.writeText("第一行：Kotlin 文件存储\n")  // 覆盖写入
                            file.appendText("第二行：追加一行")          // 追加写入
                            println("文件存在吗：${'$'}{file.exists()}")
                            val lines = file.readLines()               // 按行读成 List<String>
                            println("共 ${'$'}{lines.size} 行")
                            lines.forEachIndexed { i, line -> println("行 ${'$'}{i + 1}：${'$'}line") }
                        }
                    """.trimIndent(),
                    expectedOutput = "文件存在吗：true\n共 2 行\n行 1：第一行：Kotlin 文件存储\n行 2：第二行：追加一行",
                ),
                CodeExample(
                    description = "流式逐行读取与文件删除",
                    code = """
                        import java.io.File

                        fun main() {
                            val file = File("words.csv")
                            file.writeText("id,word\n1,协程\n2,挂起\n")
                            val header = file.useLines { lines -> lines.first() }   // 流式读取，只取首行
                            println("表头：${'$'}header")
                            val rows = file.readLines().drop(1).filter { it.isNotBlank() }
                            println("数据 ${'$'}{rows.size} 行")
                            rows.forEach { row ->
                                val (id, word) = row.split(',')                     // 解构：按逗号拆成两段
                                println("第 ${'$'}id 条：${'$'}word")
                            }
                            file.delete()
                            println("删除后还存在吗：${'$'}{file.exists()}")
                        }
                    """.trimIndent(),
                    expectedOutput = "表头：id,word\n数据 2 行\n第 1 条：协程\n第 2 条：挂起\n删除后还存在吗：false",
                ),
            ),
            cppComparison = """
                · `File.writeText/readText` ≈ C++ 的 `std::ofstream/ifstream`：Kotlin 一步完成「打开-写-关」，没有忘 close 导致句柄泄漏的问题。
                · 编码差异要留心：Kotlin 文本 API 默认 UTF-8；C++ 的 fstream 写的是原始字节，编码全靠自己负责。
                · `useLines` ≈ `std::getline` 循环流式读取：逐行处理、不整块载入，大文件内存占用恒定。
                · Android 里文件路径必须来自 context（filesDir/cacheDir），不能像桌面程序那样随手用当前目录。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "App 里存几 KB 的私有配置文件，首选哪个位置？",
                    options = listOf(
                        "context.filesDir 下的内部存储：私有、免权限、卸载即清空",
                        "外部公共目录：权限越多越安全",
                        "SD 卡根目录",
                        "进程内存里最稳妥",
                    ),
                    correctIndex = 0,
                    explanation = "内部存储私有且无需权限，随 App 卸载一起清空；示例代码用相对路径只为独立运行，真实项目要用 context.filesDir。",
                ),
                QuizQuestion(
                    question = "writeText 与 appendText 的区别是？",
                    options = listOf(
                        "writeText 覆盖原有内容，appendText 在文件末尾追加",
                        "一个写文本、一个写二进制",
                        "writeText 更快，appendText 更慢",
                        "没有区别，只是名字不同",
                    ),
                    correctIndex = 0,
                    explanation = "writeText 把文件从头重写，appendText 保留已有内容接着写；两者都会自动完成打开与关闭。",
                ),
                QuizQuestion(
                    question = "统计一个 500MB 日志文件里 ERROR 出现的次数，最合适的做法是？",
                    options = listOf(
                        "readText 读进内存再统计",
                        "useLines 流式逐行处理，内存占用恒定",
                        "反复调用 readLines，每次取一行",
                        "把文件整个读进 ByteArray 再遍历",
                    ),
                    correctIndex = 1,
                    explanation = "readText/readLines 会把全文载入内存，500MB 直接撑爆；useLines 逐行流式读取，用完即弃。",
                ),
                QuizQuestion(
                    question = "以下哪类需求不适合直接用文件存储？",
                    options = listOf(
                        "导出一份运行日志给用户",
                        "缓存一张网络图片",
                        "需要按条件查询、频繁局部更新的结构化数据",
                        "写一份配置说明文档",
                    ),
                    correctIndex = 2,
                    explanation = "结构化数据的条件查询、局部更新、版本演进是 DataStore/Room 的强项；文件适合整块读写的场景。",
                ),
            ),
        ),
        Lesson(
            id = "data-02",
            title = "DataStore 深入",
            concept = """
                文件存「整块」数据，而学习进度这类**结构化键值数据**，Android 官方推荐 DataStore。
                本课剖析本项目的真实源码 `ProgressStore.kt`（app/src/main/java/com/kotlin/learn/data/progress/）。

                ## preferencesDataStore 委托

                - `private val Context.dataStore by preferencesDataStore(name = "progress")`
                - 属性委托：第一次访问时按 name 创建实例，之后全局复用**同一个**
                - 同名 DataStore 全进程只能有一个实例，委托从根上杜绝重复创建

                ## 用 Flow 暴露进度

                - `val progress: Flow<UserProgress> = context.dataStore.data.map { prefs -> ... }`
                - `data` 本身是一个 Flow：每次磁盘数据变化都会**重新发射**
                - map 把原始键值对解码成 UserProgress——UI collect 这个 Flow，进度一变界面自动刷新

                ## edit：原子写

                - `context.dataStore.edit { prefs -> ... }` 把「读出旧值 → 计算新值 → 写回」包在一个事务里
                - 块内对 prefs 的所有修改在挂起完成后**一次性生效**，外界不会看到半新半旧的中间态
                - `recordQuizResult` 正是这样记录每课最好成绩的：decode 当前进度 → recordBest 合并 → 整体写回
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "纯 Kotlin 复现 ProgressStore 的 Flow 暴露机制（data.map 解码，真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        // 用纯 Kotlin 模拟 DataStore：data 是一个数据变化就重新发射的 Flow
                        class FakeDataStore(initial: Map<String, String>) {
                            private val state = MutableStateFlow(initial)
                            val data: Flow<Map<String, String>> = state     // 对应 context.dataStore.data
                        }

                        data class UserProgress(val completedLessonIds: Set<String>)

                        fun main() = runBlocking {
                            val dataStore = FakeDataStore(mapOf("completed" to "adv-01,adv-02"))

                            // 与真实源码相同的 map 变换：原始键值对 → UserProgress
                            val progress: Flow<UserProgress> = dataStore.data.map { prefs ->
                                UserProgress(completedLessonIds = prefs["completed"]?.split(',')?.toSet() ?: emptySet())
                            }

                            val snapshot = progress.first()   // 取一次当前值（对应 loadOnce() 的做法）
                            println("已完成：${'$'}{snapshot.completedLessonIds}")
                        }
                    """.trimIndent(),
                    expectedOutput = "已完成：[adv-01, adv-02]",
                ),
                CodeExample(
                    description = "纯 Kotlin 复现 recordQuizResult 的 edit 块与 recordBest 合并规则（真实运行）",
                    code = """
                        import kotlinx.coroutines.*

                        // 与 ProgressStore.kt 中 recordBest 相同的合并规则：只保留更好的成绩
                        fun recordBest(old: Pair<Int, Int>?, correct: Int, total: Int): Pair<Int, Int> =
                            if (old == null || correct * old.second > old.first * total) correct to total else old

                        class FakeDataStore {
                            // 模拟 DataStore 的键值存储：已记过 data-01 的最好成绩 2/3
                            private val prefs = mutableMapOf("completed" to "data-01", "score_data-01" to "2/3")

                            // 模拟 edit：读-改-写整体完成，外界看不到中间态
                            suspend fun edit(transform: (MutableMap<String, String>) -> Unit) {
                                transform(prefs)
                            }

                            override fun toString() = prefs.entries.joinToString("\n") { "${'$'}{it.key}=${'$'}{it.value}" }
                        }

                        fun main() = runBlocking {
                            val store = FakeDataStore()
                            store.edit { prefs ->
                                val old = prefs["score_data-01"]?.split('/')?.map { it.toInt() }?.let { it[0] to it[1] }
                                val best = recordBest(old, correct = 3, total = 4)
                                prefs["completed"] = prefs["completed"]!! + ",data-02"
                                prefs["score_data-01"] = "${'$'}{best.first}/${'$'}{best.second}"
                            }
                            println(store)
                        }
                    """.trimIndent(),
                    expectedOutput = "completed=data-01,data-02\nscore_data-01=3/4",
                ),
            ),
            cppComparison = """
                · DataStore.data 的 Flow ≈ 「文件一变就自动重读」的观察者；C++ 用 fstream 每次都要手动重新打开读取。
                · edit 的原子性 ≈ SQLite 事务的 BEGIN/COMMIT；C++ 手写「读-改-写文件」中途崩溃会留下半截数据，DataStore 保证要么全成、要么不动。
                · stringPreferencesKey ≈ 带类型的列名；C++ 手写键值解析要自己处理类型转换与缺省值。
                · UI 只依赖 ProgressStore 的分层，与 C++ 里「数据访问层与界面解耦」是同一条纪律。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "by preferencesDataStore(name = \"progress\") 这个属性委托保证了什么？",
                    options = listOf(
                        "每次访问都新建一个 DataStore",
                        "同名 DataStore 全进程只有一个实例，不会重复创建",
                        "数据自动加密",
                        "跨进程共享数据",
                    ),
                    correctIndex = 1,
                    explanation = "属性委托在首次访问时创建实例并缓存，之后复用同一个；同名实例重复创建是 DataStore 明确禁止的，委托从根上避免了。",
                ),
                QuizQuestion(
                    question = "val progress: Flow<UserProgress> 这种暴露方式的价值是？",
                    options = listOf(
                        "只能读一次，读完就失效",
                        "自动把数据上传服务器",
                        "磁盘数据一变化 Flow 就自动重新发射，UI collect 即响应式刷新",
                        "省掉了所有挂起函数",
                    ),
                    correctIndex = 2,
                    explanation = "data 每次写盘都会触发重发射：collect 一挂，进度一变界面自动更新，不需要手动刷新。",
                ),
                QuizQuestion(
                    question = "recordQuizResult 把「读旧进度 → 合并 → 写回」全放进 edit 块，靠什么保证不出错？",
                    options = listOf(
                        "edit 加了全局锁，让 UI 卡住等待",
                        "每次都清空重写整个存储",
                        "有网络同步兜底",
                        "edit 是事务：块内读改写一次性原子生效，外界看不到中间态",
                    ),
                    correctIndex = 3,
                    explanation = "edit 的 transform 里读到的 prefs 与写回的 prefs 在同一事务内完成；中途失败则整体不生效，不会出现半新半旧。",
                ),
                QuizQuestion(
                    question = "本项目让 UI 层只依赖 ProgressStore 而不直接接触 DataStore，好处是？",
                    options = listOf(
                        "存储实现可以整体替换，UI 代码一行不用动",
                        "能让答题判定更准确",
                        "可以少写一个类",
                        "DataStore 官方要求必须这样",
                    ),
                    correctIndex = 0,
                    explanation = "ProgressStore 是存储的唯一出口：哪天换 Room 或远程同步，改的只有这一层——这正是本模块末尾仓库模式的前奏。",
                ),
            ),
        ),
        Lesson(
            id = "data-03",
            title = "Room：实体与 DAO",
            concept = """
                Room 是 Android 官方的 SQLite 对象映射库：把数据库操作写成**注解 + Kotlin 接口**，实现由 Room 生成。

                ## Room 三件套

                - `@Entity`：一个数据类映射一张表，字段即列
                - `@Dao`：数据访问对象，声明「能做什么」，SQL 写在 `@Query` 里
                - `@Database`：把实体与 DAO 装配成数据库（下一课详讲）

                ## 实体注解

                - `@PrimaryKey(autoGenerate = true)`：自增主键
                - `@ColumnInfo(name = "...")`：自定义列名

                ## DAO 的两条铁律

                - 写操作（`@Insert`/`@Update`/`@Delete`）声明为 `suspend`：挂起执行，不碰主线程
                - 读操作返回 `Flow<List<T>>`：表一有变化自动重新发射，UI 天然响应式
                - SQL 在**编译期校验**：表名、列名写错直接编译失败，不用等运行时崩溃
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "Room 实体与 DAO 的标准写法（概念示意，非真实运行输出）",
                    code = """
                        // 示意：展示 Room 注解的写法与含义（需 Room 依赖才能真实编译）
                        @Entity(tableName = "words")
                        data class Word(
                            @PrimaryKey(autoGenerate = true) val id: Long = 0,
                            @ColumnInfo(name = "text") val text: String,
                        )

                        @Dao
                        interface WordDao {
                            @Insert
                            suspend fun insert(word: Word)          // 写：挂起执行，不碰主线程

                            @Query("SELECT * FROM words ORDER BY id")
                            fun all(): Flow<List<Word>>             // 读：表一变化就重新发射
                        }

                        // Room 会为 WordDao 生成全部实现；SQL 在编译期校验，表名列名写错即编译失败
                    """.trimIndent(),
                    expectedOutput = "（示意）words 表建立完成；insert 写入一条记录后，all() 的 Flow 立即重新发射，UI 拿到包含新词的列表",
                ),
                CodeExample(
                    description = "纯 Kotlin 复现 Room 的 Flow 查询语义：表变化自动重新发射（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        data class Word(val id: Int, val text: String)

                        // 模拟 Room 的 Flow 查询语义：数据源变化时，查询自动重新发射
                        class FakeWordDao {
                            private val table = MutableStateFlow(listOf(Word(1, "协程"), Word(2, "挂起")))

                            fun all(): Flow<List<Word>> = table                     // 对应 @Query("SELECT * FROM words")
                            suspend fun insert(word: Word) { table.value = table.value + word }   // 对应 @Insert
                        }

                        fun main() = runBlocking {
                            val dao = FakeWordDao()
                            val collector = launch {
                                dao.all().take(2).collect { words ->                // 只收两次：初始 + 插入后
                                    println("查询发射 ${'$'}{words.size} 行：[${'$'}{words.joinToString { it.text }}]")
                                }
                            }
                            delay(100L)
                            dao.insert(Word(3, "Room"))
                            collector.join()
                            println("collect 已正常结束")
                        }
                    """.trimIndent(),
                    expectedOutput = "查询发射 2 行：[协程, 挂起]\n查询发射 3 行：[协程, 挂起, Room]\ncollect 已正常结束",
                ),
            ),
            cppComparison = """
                · @Entity ≈ 手写 CREATE TABLE：Room 从数据类生成建表语句，字段加注解即列。
                · @Dao + @Query ≈ sqlite3_prepare/step/finalize 那一整套：只需注解声明，实现全部生成。
                · SQL 编译期校验是 Room 独有的安全网；C++ 拼 SQL 字符串写错了要等运行时才发现。
                · Flow 查询自动重新发射；C++ 里要自己轮询或在每次写入后手动触发重查。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "@Entity 注解的作用是？",
                    options = listOf(
                        "标注一个数据类映射一张表，字段即列",
                        "声明一条 SQL 查询语句",
                        "创建数据库文件",
                        "替代主键约束",
                    ),
                    correctIndex = 0,
                    explanation = "@Entity 把数据类变成表结构：tableName 是表名，属性是列，@PrimaryKey 与 @ColumnInfo 再补充细节。",
                ),
                QuizQuestion(
                    question = "DAO 的读方法为什么返回 Flow<List<T>> 而不是一次性的 List？",
                    options = listOf(
                        "表一有变化 Flow 就自动重新发射，UI 天然响应式",
                        "Flow 比 List 更省内存",
                        "Room 不支持返回 List",
                        "为了在主线程执行查询",
                    ),
                    correctIndex = 0,
                    explanation = "返回 Flow 后每次表数据变化都会重新查询并发射；一次性 List 只能反映调用那一刻的数据。",
                ),
                QuizQuestion(
                    question = "把 @Query 里的表名拼错一个字母，什么时候暴露？",
                    options = listOf(
                        "运行到这条查询时崩溃",
                        "永远不会暴露",
                        "打包上架后被系统拦截",
                        "编译期就失败——Room 会校验 SQL 与表结构",
                    ),
                    correctIndex = 3,
                    explanation = "Room 在编译期核对 SQL 引用的表与列：写错直接编译失败，把运行时事故提前到敲代码的瞬间。",
                ),
                QuizQuestion(
                    question = "@Insert 方法声明为 suspend 的原因是？",
                    options = listOf(
                        "suspend 能让写入更快",
                        "数据库写入是耗时 IO，挂起执行避免阻塞主线程",
                        "不加 suspend 无法返回值",
                        "Room 的注解处理要求必须挂起",
                    ),
                    correctIndex = 1,
                    explanation = "写库是磁盘 IO：suspend 让调用方协程挂起等待、线程被释放，主线程绝不会卡在数据库写入上。",
                ),
            ),
        ),
        Lesson(
            id = "data-04",
            title = "Room：数据库与查询",
            concept = """
                有了实体与 DAO，还需要一个**数据库类**把它们总装起来。

                ## @Database：数据库的总装

                - 抽象类继承 `RoomDatabase`，`@Database(entities = [...], version = N)` 列出全部表与版本号
                - 暴露抽象方法 `fun wordDao(): WordDao`，实现由 Room 生成

                ## 构建与单例

                - `Room.databaseBuilder(context, AppDatabase::class.java, "app.db").build()`
                - 构建有成本：全 App 只建**一个实例**，随进程存活——通常包在依赖注入容器里

                ## 版本与迁移

                - 改了表结构必须把 version +1，并提供 `Migration` 描述怎么从旧版迁到新版
                - `fallbackToDestructiveMigration()` 会清库重建——图省事的方案，仅限开发期

                ## 查询要点

                - 参数化查询用占位符：`@Query("SELECT * FROM words WHERE id = :id")`，参数名与方法参数对应
                - 查不到时按签名返回 null 或空列表
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "Room 数据库类的总装写法（概念示意，非真实运行输出）",
                    code = """
                        // 示意：展示 Room 数据库类的标准写法（需 Room 依赖才能真实编译）
                        @Database(entities = [Word::class], version = 1)
                        abstract class AppDatabase : RoomDatabase() {
                            abstract fun wordDao(): WordDao       // Room 生成实现，调用即得 DAO
                        }

                        // 使用处（Android 项目内）：
                        // val db = Room.databaseBuilder(context, AppDatabase::class.java, "learn.db")
                        //     .addMigrations(...)               // 版本演进时提供迁移方案
                        //     .build()                          // 全 App 只 build 一次
                    """.trimIndent(),
                    expectedOutput = "（示意）learn.db 建立完成；wordDao() 返回可用的 DAO，之后的增删查改全部经它进行",
                ),
                CodeExample(
                    description = "纯 Kotlin 复现 suspend 写入与按主键的 Flow 查询（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        data class Word(val id: Int, val text: String)

                        // 模拟一张 Room 表：suspend 写入 + 按主键的 Flow 查询
                        class FakeDb {
                            private val rows = MutableStateFlow(listOf(Word(1, "协程")))

                            suspend fun insert(word: Word) { rows.value = rows.value + word }               // 对应 suspend @Insert
                            fun byId(id: Int): Flow<Word?> = rows.map { list -> list.find { it.id == id } } // 对应 WHERE id = :id
                        }

                        fun main() = runBlocking {
                            val db = FakeDb()
                            db.insert(Word(2, "结构化并发"))
                            val hit = db.byId(2).first()          // first()：取一次查询结果
                            println("按 id=2 查到：${'$'}{hit?.text}")
                            val miss = db.byId(9).first()
                            println("按 id=9 查到：${'$'}miss")
                        }
                    """.trimIndent(),
                    expectedOutput = "按 id=2 查到：结构化并发\n按 id=9 查到：null",
                ),
            ),
            cppComparison = """
                · RoomDatabase 单例 ≈ 全进程共用一个 `sqlite3*` 句柄：别到处 open，统一从一个入口访问。
                · version + Migration ≈ schema 演进时手写 ALTER TABLE：Room 把「从几版到几版、怎么改」结构化地登记在代码里。
                · 主线程禁止数据库操作 ≈ UI 线程不能做 sqlite3_step；Room 直接抛异常，强制你走挂起。
                · byId(id) 的 `:id` 占位符 ≈ sqlite3_bind_int 绑定参数：值与语句分离，天然防 SQL 注入。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "声明 @Database 时必不可少的两项是？",
                    options = listOf(
                        "entities（表清单）与 version（版本号）",
                        "表名与文件名",
                        "DAO 实现类与迁移脚本",
                        "用户名与密码",
                    ),
                    correctIndex = 0,
                    explanation = "entities 告诉 Room 有哪些表，version 是迁移机制的依据；两者缺一，编译期就报错。",
                ),
                QuizQuestion(
                    question = "为什么数据库实例要做全 App 单例？",
                    options = listOf(
                        "构建成本高，多实例浪费连接与内存，还容易出现数据视图不一致",
                        "Room 规定一个 App 只能有一张表",
                        "单例能让查询结果翻倍",
                        "不单例就无法定义 DAO",
                    ),
                    correctIndex = 0,
                    explanation = "databaseBuilder().build() 有开销；多处各建实例既浪费又难管理生命周期，惯例是一个入口持有、随进程存活。",
                ),
                QuizQuestion(
                    question = "version 从 1 升到 2 但没提供 Migration，运行时会发生什么？",
                    options = listOf(
                        "Room 自动生成迁移语句",
                        "旧数据自动兼容新结构",
                        "默认直接抛异常；要么提供 Migration，要么开发期用 fallbackToDestructiveMigration 清库重建",
                        "数据库文件被加密",
                    ),
                    correctIndex = 2,
                    explanation = "Room 不知道怎么改表就拒绝打开数据库；fallbackToDestructiveMigration 会删库重建，用户数据全部丢失，仅限开发期。",
                ),
                QuizQuestion(
                    question = "按主键查询的正确写法是？",
                    options = listOf(
                        "把 id 直接字符串拼接进 SQL",
                        "@Query(\"SELECT * FROM words WHERE id = :id\")，占位符与方法参数同名对应",
                        "查出全部记录再在内存里过滤，这是唯一方式",
                        "@Query 不支持条件查询",
                    ),
                    correctIndex = 1,
                    explanation = ":id 占位符与方法参数绑定，值与语句分离；等价于 C++ 里 sqlite3_bind 绑定参数，安全且高效。",
                ),
            ),
        ),
        Lesson(
            id = "data-05",
            title = "HTTP 与 JSON 概念",
            concept = """
                请求网络前先看清两样东西的本来面目：**HTTP 报文**与 **JSON**。

                ## 一条 HTTP 请求长什么样

                - 请求行：`方法 路径 版本`，如 `GET /users/42 HTTP/1.1`
                - 头部：一行一个 `键: 值`，说明接受什么类型、目标主机是谁
                - 体：GET 通常没有；POST 把数据放在这里

                ## 响应的结构

                - 状态行：`HTTP/1.1 200 OK`——2xx 成功、4xx 客户端错（如 404）、5xx 服务端错
                - 头部 + 体：体通常是 JSON

                ## JSON：数据交换的通用格式

                - 对象 `{}`：键值对集合；数组 `[]`：有序列表；值可为字符串、数字、布尔、null 或嵌套结构
                - Android 用 kotlinx.serialization 等库把 JSON 一行映射成 Kotlin 数据类——**不要手写字符串切割**去解析
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "解析 HTTP 请求的请求行与头部（真实运行）",
                    code = """
                        fun main() {
                            val request = listOf(                    // 一条最小的 HTTP 请求报文
                                "GET /users/42 HTTP/1.1",
                                "Host: api.example.com",
                                "Accept: application/json",
                            ).joinToString("\n")
                            val lines = request.lines()

                            val (method, path, version) = lines.first().split(' ')   // 请求行：方法 路径 版本
                            println("${'$'}method ${'$'}path ${'$'}version")

                            val headers = lines.drop(1).filter { it.isNotBlank() }   // 头部：每行「键: 值」
                                .associate { line ->
                                    val (key, value) = line.split(':', limit = 2)
                                    key to value.trim()
                                }
                            val host = headers["Host"]
                            val accept = headers["Accept"]
                            println("Host 是 ${'$'}host")
                            println("可接受类型是 ${'$'}accept")
                        }
                    """.trimIndent(),
                    expectedOutput = "GET /users/42 HTTP/1.1\nHost 是 api.example.com\n可接受类型是 application/json",
                ),
                CodeExample(
                    description = "手工解析一段扁平 JSON，体会为什么该用库（真实运行）",
                    code = """
                        fun main() {
                            // 简化的扁平 JSON；真实项目交给 kotlinx.serialization 等库（嵌套、转义手写不可靠）
                            val json = "{\"id\": 42, \"name\": \"Ann\", \"level\": 3}"
                            val body = json.removePrefix("{").removeSuffix("}")

                            body.split(',').map { it.trim() }.forEach { field ->
                                val (key, value) = field.split(':', limit = 2).map { it.trim() }
                                val isString = value.startsWith('"')          // 带引号的是字符串，其余是数字/布尔/null
                                println("${'$'}{key.trim('"')} -> ${'$'}{if (isString) value.trim('"') else value}")
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "id -> 42\nname -> Ann\nlevel -> 3",
                ),
            ),
            cppComparison = """
                · 手拼 HTTP 报文 ≈ C++ 用 libcurl 设置 header/body：Android 里这些交给 OkHttp/Retrofit。
                · 解析 JSON ≈ C++ 的 nlohmann::json 或 rapidjson；Kotlin 用 kotlinx.serialization 直接映射到数据类。
                · 报文是纯文本、结构简单——示例里手动 split 只为看清结构；真实项目（两种语言都一样）一律用库。
                · 状态码分类（2xx/4xx/5xx）是协议层的约定，与语言无关，两个生态通用。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "收到响应状态码 500，问题出在哪一侧？",
                    options = listOf(
                        "客户端，请求格式有误",
                        "服务端内部错误——5xx 都指向服务端",
                        "网络被劫持",
                        "JSON 解析失败",
                    ),
                    correctIndex = 1,
                    explanation = "2xx 成功、4xx 客户端错（如 404 路径不存在）、5xx 服务端错（如 500 内部异常）。先看状态码首位就能分流排查。",
                ),
                QuizQuestion(
                    question = "GET 与 POST 最典型的区别是？",
                    options = listOf(
                        "GET 参数在 URL/查询串、用于获取数据；POST 数据放请求体、用于提交修改",
                        "GET 更快，因为走不同的协议",
                        "POST 只能返回 JSON，GET 只能返回文本",
                        "两者没有区别，随意使用",
                    ),
                    correctIndex = 0,
                    explanation = "语义上 GET 取数据、参数暴露在 URL；POST 提交数据、内容在请求体里。这也决定了缓存与重试策略的不同。",
                ),
                QuizQuestion(
                    question = "JSON 里 \"tags\": [\"a\", \"b\"] 是什么结构？",
                    options = listOf(
                        "对象里嵌套了一个数组",
                        "数组的数组",
                        "一个字符串",
                        "键值对循环引用",
                    ),
                    correctIndex = 0,
                    explanation = "外层 {} 是对象，tags 的值是 [] 数组，元素是字符串——JSON 允许任意嵌套组合，这正是手写解析容易出错的地方。",
                ),
                QuizQuestion(
                    question = "为什么不建议手写字符串切割解析 JSON？",
                    options = listOf(
                        "嵌套结构、字符串内的逗号与转义都会把 naive 的 split 骗过，库处理了全部边界",
                        "手写解析跑得慢",
                        "库解析出来的数据更准确",
                        "JSON 格式不允许程序自己解析",
                    ),
                    correctIndex = 0,
                    explanation = "引号里可以有逗号、冒号和转义符，值还能嵌套对象数组——split 一刀切必错；kotlinx.serialization 等库处理了全部规则。",
                ),
            ),
        ),
        Lesson(
            id = "data-06",
            title = "Retrofit 基础",
            concept = """
                Retrofit 是 Android 最主流的**声明式 HTTP 客户端**：只写「接口 + 注解」，实现由 Retrofit 生成。

                ## 声明式是什么意思

                - 你声明「要什么」：`@GET("users/{id}") suspend fun user(@Path("id") id: Long): User`
                - Retrofit 负责「怎么做」：拼 URL、发请求、挂起等待、把响应映射成返回类型

                ## 常用注解

                - `@Path`：替换路径里的 `{占位符}`；`@Query`：追加 `?key=value` 查询参数
                - `@Body`：把对象序列化成请求体；`@POST`/`@PUT`：携带体发起写操作

                ## 挂起 + 转换器

                - suspend 函数：调用即挂起，响应到达才恢复，不阻塞线程
                - 转换器（如 kotlinx.serialization）：响应 JSON **自动映射**成返回值类型的 Kotlin 对象
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "Retrofit 接口声明的标准写法（概念示意，非真实运行输出）",
                    code = """
                        // 示意：Retrofit 接口声明（需 Retrofit 依赖才能真实编译）
                        data class User(val id: Long, val name: String)

                        interface ApiService {
                            @GET("users/{id}")
                            suspend fun user(@Path("id") id: Long): User              // GET /users/42 → User

                            @GET("users")
                            suspend fun users(@Query("page") page: Int): List<User>   // GET /users?page=1 → List<User>

                            @POST("users")
                            suspend fun createUser(@Body user: User): User            // 请求体携带 JSON
                        }

                        // 构建处（Android 项目内）：
                        // val api = Retrofit.Builder()
                        //     .baseUrl("https://api.example.com/")
                        //     .addConverterFactory(...)       // JSON 自动映射成 Kotlin 对象
                        //     .build()
                        //     .create(ApiService::class.java) // 运行时生成 ApiService 的实现
                    """.trimIndent(),
                    expectedOutput = "（示意）调用 api.user(42)：Retrofit 拼出 GET https://api.example.com/users/42，挂起等待；响应 JSON 经转换器映射成 User 对象返回",
                ),
                CodeExample(
                    description = "纯 Kotlin 复现 Retrofit 的路径占位符与查询参数拼接（真实运行）",
                    code = """
                        // 用纯 Kotlin 模拟 Retrofit 的注解行为：路径模板 + 参数 → 完整 URL
                        fun resolvePath(template: String, vararg params: Pair<String, String>): String =
                            params.fold(template) { path, (key, value) -> path.replace("{${'$'}key}", value) }

                        fun main() {
                            val base = "https://api.example.com/"
                            val path = resolvePath("users/{id}/posts/{postId}", "id" to "42", "postId" to "7")
                            println("GET ${'$'}base${'$'}path?page=2")     // ?page=2 就是 @Query 追加的查询参数
                        }
                    """.trimIndent(),
                    expectedOutput = "GET https://api.example.com/users/42/posts/7?page=2",
                ),
            ),
            cppComparison = """
                · Retrofit 接口 ≈ C++ 里给每个端点手写一个请求函数：注解声明一次，实现由 Retrofit 运行时生成。
                · 转换器 ≈ nlohmann::json 的 from_json：JSON → 对象的映射交给库，不用手写字段提取。
                · suspend 挂起等待响应 ≈ 回调或 future.get()，但代码是平铺的、线程不阻塞。
                · baseUrl + 路径拼接规则简单固定——示例 2 用纯 Kotlin 复现了它，C++ 里这就是手写 URL 拼装的那部分。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "@Path(\"id\") 与 @Query(\"page\") 的区别是？",
                    options = listOf(
                        "@Path 替换路径中的 {id} 占位符，@Query 追加 ?page=... 查询参数",
                        "两者等价，可以互换",
                        "@Path 用于请求体，@Query 用于响应体",
                        "@Query 只能用在 POST 上",
                    ),
                    correctIndex = 0,
                    explanation = "@Path 填的是路径段（/users/42），@Query 拼的是 URL 问号后的键值对（?page=2），位置与语义都不同。",
                ),
                QuizQuestion(
                    question = "suspend fun user(@Path(\"id\") id: Long): User 说明了什么？",
                    options = listOf(
                        "调用会挂起等待网络响应，返回的 JSON 已自动映射成 User 对象",
                        "user() 会立即返回一个空 User",
                        "网络请求跑在主线程上",
                        "必须手动解析 JSON 再组装 User",
                    ),
                    correctIndex = 0,
                    explanation = "suspend 负责不阻塞地等待；返回类型 User 配合转换器，让响应 JSON 直接变成 Kotlin 对象。",
                ),
                QuizQuestion(
                    question = "@Body 注解的作用是？",
                    options = listOf(
                        "把参数对象序列化成请求体（如 JSON）随请求发送",
                        "读取响应体的前几行",
                        "声明 HTTP 版本号",
                        "给 URL 追加哈希片段",
                    ),
                    correctIndex = 0,
                    explanation = "POST/PUT 需要携带数据：@Body 把 Kotlin 对象交给转换器序列化成请求体，与响应方向的映射互为逆操作。",
                ),
                QuizQuestion(
                    question = "ApiService 接口的实现从哪里来？",
                    options = listOf(
                        "需要自己手写实现类",
                        "编译器自动为所有接口生成 HTTP 实现",
                        "运行时由 Retrofit.create(ApiService::class.java) 动态生成",
                        "来自 Android 系统服务",
                    ),
                    correctIndex = 2,
                    explanation = "接口只写声明；create() 在运行时生成代理实现——这就是「声明式」的落点，你描述接口，它负责执行。",
                ),
            ),
        ),
        Lesson(
            id = "data-07",
            title = "仓库模式",
            concept = """
                数据来自多个地方：Room、DataStore、Retrofit……**UI 不该知道也不该关心**。仓库模式（Repository）就是收口的那一层。

                ## 仓库的定位

                - Repository 是**唯一数据出口**：上层只依赖它，内部调度本地与远程数据源
                - 数据源实现（API、数据库）藏在仓库后面，随时可替换

                ## 单一真相源

                - 网络拉到的数据**回写本地**，UI 只观察本地数据库的 Flow
                - 数据只存一份、只有一个写入入口，界面之间自然保持一致

                ## 面向接口

                - 仓库依赖「数据源接口」而非具体实现：测试时换成假数据源，无需真网络
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "仓库串接远程与本地数据源：网络回写本地，UI 只看本地（真实运行）",
                    code = """
                        import kotlinx.coroutines.*
                        import kotlinx.coroutines.flow.*

                        data class User(val id: Int, val name: String)

                        // 远程数据源：模拟 Retrofit API
                        class RemoteApi {
                            suspend fun fetchUsers(): List<User> {
                                delay(100L)                                   // 模拟网络延迟
                                return listOf(User(1, "Ann"), User(2, "Bob"))
                            }
                        }

                        // 本地数据源：模拟 Room 表
                        class LocalDb {
                            private val rows = MutableStateFlow<List<User>>(emptyList())
                            val users: Flow<List<User>> = rows                // UI 唯一观察点
                            suspend fun saveAll(list: List<User>) { rows.value = list }
                        }

                        // 仓库：唯一数据出口——网络拉取后回写本地，UI 只看本地
                        class UserRepository(private val api: RemoteApi, private val db: LocalDb) {
                            val users: Flow<List<User>> = db.users
                            suspend fun refresh() = db.saveAll(api.fetchUsers())
                        }

                        fun main() = runBlocking {
                            val repo = UserRepository(RemoteApi(), LocalDb())
                            val collector = launch {
                                repo.users.take(2).collect { list ->
                                    println("UI 收到 ${'$'}{list.size} 人：[${'$'}{list.joinToString { it.name }}]")
                                }
                            }
                            delay(100L)
                            println("发起刷新")
                            repo.refresh()
                            collector.join()
                        }
                    """.trimIndent(),
                    expectedOutput = "UI 收到 0 人：[]\n发起刷新\nUI 收到 2 人：[Ann, Bob]",
                ),
                CodeExample(
                    description = "面向接口的数据源：缓存优先、云端回退（真实运行）",
                    code = """
                        import kotlinx.coroutines.*

                        data class User(val id: Int, val name: String)

                        // 面向接口：调用方只认识抽象，实现可以替换（测试时换成假数据源即可）
                        interface UserSource {
                            suspend fun user(id: Int): User?
                        }

                        class CloudSource : UserSource {                      // 模拟 Retrofit 实现
                            override suspend fun user(id: Int): User? {
                                delay(50L)
                                return if (id == 1) User(1, "云端 Ann") else null
                            }
                        }

                        class CachedSource(private val fallback: UserSource) : UserSource {
                            private val cache = mutableMapOf<Int, User>()
                            override suspend fun user(id: Int): User? {
                                cache[id]?.let { cached ->
                                    println("命中缓存：${'$'}{cached.name}")
                                    return cached
                                }
                                val fresh = fallback.user(id) ?: return null
                                cache[id] = fresh
                                println("从云端拉取并缓存：${'$'}{fresh.name}")
                                return fresh
                            }
                        }

                        fun main() = runBlocking {
                            val source: UserSource = CachedSource(CloudSource())
                            println(source.user(1)?.name)     // 第一次：走云端
                            println(source.user(1)?.name)     // 第二次：命中缓存
                            println("id=99 查询：${'$'}{source.user(99)}")
                        }
                    """.trimIndent(),
                    expectedOutput = "从云端拉取并缓存：云端 Ann\n云端 Ann\n命中缓存：云端 Ann\n云端 Ann\nid=99 查询：null",
                ),
            ),
            cppComparison = """
                · 仓库模式不是 Android 专属：C++ 大型项目同样用「数据访问层」隔离界面与存储。
                · 单一真相源 ≈ 全局只有一个权威数据副本；C++ 多处缓存各自为政是经典 bug 来源。
                · 面向接口 ≈ C++ 的抽象基类注入：测试时注入假实现，不用真开网络或数据库。
                · 网络回写本地再通知 UI ≈ 「写穿缓存」策略：顺序固定，才不会出现缓存与源头不一致。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Repository 在分层中的定位是？",
                    options = listOf(
                        "唯一数据出口：上层只依赖它，内部调度本地与远程数据源",
                        "负责绘制列表 UI",
                        "替代 ViewModel 管理生命周期",
                        "Retrofit 的另一个名字",
                    ),
                    correctIndex = 0,
                    explanation = "UI → 仓库 → 数据源：仓库把「数据从哪来」封装起来，上层面对的是统一的接口与 Flow。",
                ),
                QuizQuestion(
                    question = "「单一真相源」在本课示例里是怎么落地的？",
                    options = listOf(
                        "UI 直接 collect 网络回调的结果",
                        "网络数据回写本地库，UI 只观察本地库的 Flow",
                        "每台设备各存一份，定期对比",
                        "把所有数据都放内存里",
                    ),
                    correctIndex = 1,
                    explanation = "refresh() 拉到网络数据后 saveAll 进本地库；UI 只观察本地 Flow——数据只有一个权威存放点，各界面自然一致。",
                ),
                QuizQuestion(
                    question = "UserRepository 依赖 UserSource 接口而不是具体实现，直接收益是？",
                    options = listOf(
                        "代码行数更少",
                        "运行速度提升",
                        "可以少写一个类",
                        "测试时可注入假数据源，无需真网络/真数据库",
                    ),
                    correctIndex = 3,
                    explanation = "面向接口 = 可替换：单元测试传一个内存假实现即可覆盖全流程，这正是示例 2 里 CachedSource 的结构。",
                ),
                QuizQuestion(
                    question = "缓存数据源里查不到 id=99 时，示例的处理是？",
                    options = listOf(
                        "返回缓存里最近的一条",
                        "无限重试直到成功",
                        "回退到 fallback（云端）查询，结果为 null 就如实返回 null",
                        "抛出异常终止程序",
                    ),
                    correctIndex = 2,
                    explanation = "缓存未命中走 fallback，查不到就返回 null——数据源的失败被如实上交，由上层决定空态怎么展示。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "data-network",
        title = "数据与网络",
        emoji = "💾",
        lessons = lessons,
    )
}
