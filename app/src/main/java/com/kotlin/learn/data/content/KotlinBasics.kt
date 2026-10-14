package com.kotlin.learn.data.content

import com.kotlin.learn.data.model.CodeExample
import com.kotlin.learn.data.model.CourseModule
import com.kotlin.learn.data.model.Lesson
import com.kotlin.learn.data.model.QuizQuestion

/** 模块一：Kotlin 语言基础（15 课）。课程数据即学习材料，撰写时保持代码可运行。 */
object KotlinBasics {
    val lessons: List<Lesson> = listOf(
        Lesson(
            id = "kotlin-00",
            title = "Kotlin 语言介绍",
            concept = """
                写代码之前，先弄清楚 **Kotlin 是什么、为什么值得学**。

                ## Kotlin 是什么

                - JetBrains 2011 年发布；Google 2017 年 I/O 宣布官方支持，2019 年定为 Android 首选语言
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
                        import kotlinx.coroutines.*

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
                · Java 版的 `User` 有 9 行样板，Kotlin 的 `data class User(...)` 一行顶全部——getter/equals/hashCode/toString 自动生成。
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
        Lesson(
            id = "kotlin-01",
            title = "变量与基本类型",
            concept = """
                Kotlin 与 C++ 相比，变量声明**更简单也更安全**。这一课讲最常用的声明方式。

                ## val 与 var

                用 `val` 声明**只读变量**（类似 C++ 的 `const`），用 `var` 声明可变变量。
                类型写在变量名后面，很多情况下编译器可以**自动推断**，省略不写。

                ## 数字类型一览

                - `Int`（32 位）、`Long`（64 位）
                - `Double`、`Float`
                - `Boolean`、`Char`

                ## 与 C++ 最大的差异

                基本类型**没有隐式窄化转换**：`Int` 不会自动变成 `Long`，必须显式调用 `.toLong()`。
                有 `UInt`、`ULong`、`UShort`、`UByte` 无符号类型（Kotlin 1.9 起 Stable），但日常开发绝大多数场景仍用有符号的 `Int`/`Long`。

                ## 运算符概览

                - 算术：`+` `-` `*` `/` `%`——注意 **Int 除法截断**：`7 / 2` 的结果是 `3`，想要 `3.5` 得写 `7 / 2.0` 或 `7.0 / 2`
                - 比较：`==` 比较内容（相当于调用 `equals`），`===` 比较是否同一个引用
                - 逻辑：`&&` `||` `!`，与 C++ 用法一致
                - 区间：`1..10` 生成可遍历的区间，配合 `until`、`downTo`、`step` 使用
                - 运算符在 Kotlin 里本质是函数调用，自定义运算符重载与中缀函数见模块四 adv-11
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

                        println("total = ${'$'}total, big = ${'$'}big")   // total = 15, big = 10
                    """.trimIndent(),
                    expectedOutput = "total = 15, big = 10",
                ),
            ),
            cppComparison = """
                · `val` ≈ C++ 的 `const` 局部变量；`var` 是普通变量。
                · Kotlin 的 `Int` 是对象（装箱发生在需要时），不像 C++ 的 `int` 是裸值；但运行时性能接近。
                · 最大的坑：**没有隐式窄化**。C++ 里 `long x = intVar;` 合法，Kotlin 必须写 `intVar.toLong()`。
                · 也没有 `auto` 关键字——不写类型时默认就是**自动推断**。
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
                    question = "日常开发中最常用的 Kotlin 整数类型是？",
                    options = listOf("unsigned int", "uint8_t", "Int", "size_t"),
                    correctIndex = 2,
                    explanation = "Kotlin 提供有符号的 Byte、Short、Int、Long，以及无符号的 UByte、UShort、UInt、ULong（1.9 起 Stable），但日常开发绝大多数场景仍用有符号的 Int；\"unsigned int\"、\"size_t\" 是 C++ 的类型名。",
                ),
                QuizQuestion(
                    question = "7 / 2 的结果是？",
                    options = listOf("3.5", "3", "4", "编译错误"),
                    correctIndex = 1,
                    explanation = "两个 Int 相除结果仍是 Int，向零截断得 3；想要 3.5 需把一方写成 Double（如 7 / 2.0 或 7.0 / 2），Kotlin 不会做隐式提升。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-02",
            title = "字符串与字符串模板",
            concept = """
                Kotlin 的 `String` 与 C++ 的 `std::string` 类似，但**不可变**：任何修改操作都返回新字符串，原对象不变。

                ## 字符串模板

                - `${'$'}变量` 直接把值嵌进字符串，`${'$'}{表达式}` 可以嵌入任意表达式
                - **不需要 printf 式的占位符**

                ## 原始字符串

                三引号（三个连续双引号）创建原始字符串：内容不转义、可跨行，适合写 JSON、正则、SQL 等文本。

                ## 内容比较

                比较字符串内容直接用 `==`，它调用的是 `equals`；要比引用（地址）才用 `===`。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "字符串模板：$ 变量与 $ 表达式：",
                    code = """
                        val name = "Kotlin"
                        val version = 2.0
                        println("正在学习 ${'$'}name")                 // ${'$'}变量名 直接嵌入
                        println("明年版本：${'$'}{version + 1}")        // ${'$'}{...} 里可以写表达式
                        println("共 ${'$'}{name.length} 个字符")        // 6

                        val greeting = "Hi, ${'$'}name！你今年 ${'$'}{2026 - 2011} 岁了"
                        println(greeting)                          // Hi, Kotlin！你今年 15 岁了
                    """.trimIndent(),
                    expectedOutput = "正在学习 Kotlin\n明年版本：3.0\n共 6 个字符\nHi, Kotlin！你今年 15 岁了",
                ),
                CodeExample(
                    description = "原始字符串与不可变性：",
                    code = """
                        val path = "C:\\android\\project"       // 普通字符串要转义反斜杠
                        val regex = "\\d+"                       // 表示正则 \d+，层层转义很痛苦

                        val json = ""${'"'}
                            {
                                "name": "Kotlin",
                                "age": 14
                            }
                        ""${'"'}.trimIndent()                    // 原始字符串：不转义、可跨行

                        val s = "Kotlin"
                        // s[0] = 'k'   // 编译错误：String 不可变
                        val upper = s.uppercase()               // 修改操作返回新字符串

                        println(path)                           // C:\android\project
                        println(upper)                          // KOTLIN
                    """.trimIndent(),
                    expectedOutput = "C:\\android\\project\nKOTLIN",
                ),
            ),
            cppComparison = """
                · `String` **不可变**，像只能整体赋值的 `const std::string`；拼接频繁时注意产生大量中间对象（Kotlin 常直接用模板表达式解决）。
                · 字符串模板 ≈ `std::format`/`fmt::format`，但变量**直接写进字符串**，不用占位符对号入座。
                · `==` 比较内容（调用 equals），`===` 才比较引用——和 C++ 的字符串指针/值语义区分方式不同。
                · 原始字符串对应 C++ 的 `R"( ... )"` 定界符，Kotlin 的三引号里**连引号都不用转义**。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "println(\"共 ${'$'}{list.size} 项\") 中 ${'$'}{...} 的作用是？",
                    options = listOf(
                        "转义美元符号",
                        "把表达式的值嵌入字符串",
                        "声明一个新变量",
                        "对数字做格式化对齐",
                    ),
                    correctIndex = 1,
                    explanation = "${'$'}{...} 是模板表达式：先求值再嵌入结果。只嵌单个变量时可简写为 ${'$'}list；取属性或表达式必须用花括号，如 ${'$'}{list.size}。",
                ),
                QuizQuestion(
                    question = "Kotlin 中 s1 == s2 对字符串比较的是？",
                    options = listOf("引用（地址）", "字符串内容", "长度", "哈希值存储位置"),
                    correctIndex = 1,
                    explanation = "== 调用 equals，比较内容；要比引用用 ===。这与 C++ 中 char* 比较地址、std::string 用 == 比内容的习惯不同，Kotlin 的 == 对所有类型统一走 equals。",
                ),
                QuizQuestion(
                    question = "三引号原始字符串的特点是？",
                    options = listOf(
                        "内容无需转义且可跨行",
                        "内容自动去除所有空格",
                        "只能用于正则表达式",
                        "内容必须写在同一行",
                    ),
                    correctIndex = 0,
                    explanation = "三引号字符串不做转义、允许换行，常配合 trimIndent() 去掉缩进；反斜杠、引号都原样保留。",
                ),
                QuizQuestion(
                    question = "能对 String 实例执行 s[0] = 'A' 吗？",
                    options = listOf(
                        "能，String 支持下标赋值",
                        "不能，String 不可变，只能生成新字符串",
                        "能，但仅限 var 声明的字符串",
                        "能，编译器会自动扩容",
                    ),
                    correctIndex = 1,
                    explanation = "Kotlin 的 String 不可变，s[i] 只读；需要修改时用 replace、uppercase 等返回新串，或用 StringBuilder。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-03",
            title = "条件与循环",
            concept = """
                `if` 和 `when` 都是**表达式**，可以有返回值；`for` 只有 `for (x in 序列)` 一种形式，靠 `in` 遍历区间、数组、集合。

                ## if 与 when

                - 没有 `a ? b : c` 形式的三元运算符，直接写 `val max = if (a > b) a else b`
                - `when` 替代 C++ 的 `switch`，但强大得多：可以匹配常量、区间、类型
                - **分支之间不贯穿**，不需要 `break`

                ## 区间与循环

                - `1..10` 是闭区间，`1 until 10` 是半开区间
                - `downTo` 倒序，`step` 设步长
                - C 风格的三段式循环被区间表达式取代；需要复杂循环条件时再退回 `while`

                ## 跳转与标签

                - `break` 跳出本层循环、`continue` 跳到本层下一次迭代，语义与 C++ 相同
                - 多层嵌套想**直接跳出外层**，用标签：`loop@ for (...) { ... break@loop ... }`，`continue@loop` 则跳到外层下一轮
                - 标签是 Kotlin 对「跳出多层嵌套」这一刚需的受控方案，省掉 flag 变量与 if 判断
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "when 表达式：替代 switch 且更强：",
                    code = """
                        val code = 404
                        val message = when (code) {
                            200 -> "OK"                        // 匹配常量
                            in 400..499 -> "客户端错误"          // 匹配区间
                            else -> "未知状态"                   // 相当于 default
                        }

                        val a = 3
                        val b = 7
                        val max = if (a > b) a else b          // if 表达式，没有三元运算符

                        println(message)                       // 客户端错误：404 命中 400..499 区间
                        println(max)                           // 7
                    """.trimIndent(),
                    expectedOutput = "客户端错误\n7",
                ),
                CodeExample(
                    description = "循环与区间（含标签跳出外层）：",
                    code = """
                        for (i in 1..5) print(i)               // 12345：闭区间
                        println()
                        for (i in 0 until 5) print(i)          // 01234：不含上界
                        println()
                        for (i in 10 downTo 1 step 3) print(i) // 10741：倒序步进
                        println()

                        val langs = listOf("Kotlin", "Java")
                        for (lang in langs) print(lang)        // 直接遍历元素，无需下标
                        println()

                        var n = 0
                        while (n < 3) { print(n); n++ }        // while 用法与 C++ 相同
                        println()

                        // 标签：i*j 一超过 4 就直接跳出外层循环，不用 flag
                        loop@ for (i in 1..3) {
                            for (j in 1..3) {
                                if (i * j > 4) break@loop
                                print("${'$'}{i * j} ")
                            }
                        }
                    """.trimIndent(),
                    expectedOutput = "12345\n01234\n10741\nKotlinJava\n012\n1 2 3 2 4 ",
                ),
            ),
            cppComparison = """
                · `if`/`when` 都是表达式：Kotlin 刻意删掉了 `a ? b : c` 形式的三元运算符，`if` 直接兼任。
                · `when` 分支不贯穿、不用写 `break`；C++ 里忘写 break 的经典 bug 在 Kotlin **不存在**。
                · `for (i in 0 until n)` 对应 `for (int i = 0; i < n; i++)`；`..` 是闭区间，**别按数学半开区间的习惯理解**。
                · 没有 C 风格三段式 `for(;;)`；条件复杂时用 `while`，遍历一律 `for-in`。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 中不存在下列哪种语法？",
                    options = listOf(
                        "三元运算符 a ? b : c",
                        "while 循环",
                        "if-else 分支",
                        "when 匹配区间",
                    ),
                    correctIndex = 0,
                    explanation = "Kotlin 用 if 表达式替代三元运算符：val max = if (a > b) a else b。while、if-else、when 都保留。",
                ),
                QuizQuestion(
                    question = "when 的某个分支执行完后会怎样？",
                    options = listOf(
                        "继续执行下一个分支（贯穿）",
                        "自动结束整个 when，不贯穿",
                        "必须写 break 才能跳出",
                        "回到 when 开头重新匹配",
                    ),
                    correctIndex = 1,
                    explanation = "when 每次只执行命中的那一个分支，不贯穿、无需 break——C++ switch 忘写 break 的坑在这里不存在。",
                ),
                QuizQuestion(
                    question = "区间 1 until 5 包含哪些整数？",
                    options = listOf("1 到 5（含两端）", "1 到 4", "2 到 5", "0 到 5"),
                    correctIndex = 1,
                    explanation = "until 是半开区间 [1, 5)，不含上界；1..5 才是包含 5 的闭区间。做数组下标循环时 until 更常用。",
                ),
                QuizQuestion(
                    question = "for (i in 10 downTo 2 step 3) 依次输出？",
                    options = listOf("10 8 6 4 2", "10 7 4", "2 5 8", "10 7 4 1"),
                    correctIndex = 1,
                    explanation = "downTo 2 step 3 表示从 10 倒数、每次减 3、最低到 2：10、7、4，下一个 1 小于 2 停止。",
                ),
                QuizQuestion(
                    question = "loop@ for (...) { ... break@loop } 中 break@loop 的效果是？",
                    options = listOf(
                        "只跳出当前内层循环",
                        "直接跳出标签标记的外层循环",
                        "跳回外层循环开头继续下一轮",
                        "编译错误：Kotlin 不支持标签",
                    ),
                    correctIndex = 1,
                    explanation = "break@loop 跳出标签 loop@ 标记的那一层循环，多层嵌套时无需 flag 变量即可整体退出；continue@loop 则是跳到外层的下一轮迭代。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-04",
            title = "函数与默认参数",
            concept = """
                函数用 `fun` 声明，参数写 `名字: 类型`，返回类型放在参数列表之后；没有返回值时类型是 `Unit`，通常省略。

                ## 单表达式函数

                函数体只有一句表达式时可以省略花括号和 return，写成 `fun double(x: Int) = x * 2`，返回类型由**编译器推断**。

                ## 默认参数与命名参数

                默认参数 + 命名参数是 Kotlin 的惯用法：调用处 `power(2.0, exponent = 3)` 自带说明，**绝大多数函数重载场景都可以省掉**。

                ## 顶层函数

                函数可以直接写在文件顶层（顶层函数），不必挂在类上，也**没有头文件/实现分离的负担**。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "默认参数、命名参数与单表达式函数：",
                    code = """
                        fun power(base: Double, exponent: Int = 2): Double {
                            var result = 1.0
                            repeat(exponent) { result *= base }   // repeat 是标准库函数
                            return result
                        }

                        fun double(x: Int) = x * 2                // 单表达式函数，返回类型推断为 Int

                        println(power(3.0))                       // 9.0：exponent 用默认值 2
                        println(power(2.0, exponent = 3))         // 8.0：命名参数跳过默认值
                        println(double(21))                       // 42
                    """.trimIndent(),
                    expectedOutput = "9.0\n8.0\n42",
                ),
            ),
            cppComparison = """
                · 语法顺序与 C++ 相反：`fun f(x: Int): Int`，冒号后面是类型，返回类型在参数表之后。
                · 默认参数 C++ 也有，但**命名参数是 C++ 没有的**；两者配合后基本不再需要函数重载。
                · 单表达式函数 `fun f() = expr` 比 C++ 的「单 return 语句函数」更彻底，**连花括号都省了**。
                · **顶层函数 + 无头文件**：写工具函数不需要再建 class 或维护声明/实现两份。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 函数没有返回值时，其返回类型是？",
                    options = listOf("void", "Unit", "Nothing", "Null"),
                    correctIndex = 1,
                    explanation = "Unit 类似 C++ 的 void，是只有一个值 Unit 的类型，通常省略不写。Nothing 表示「永远不会正常返回」（如抛异常）。",
                ),
                QuizQuestion(
                    question = "命名参数主要解决什么问题？",
                    options = listOf(
                        "让函数运行更快",
                        "提升调用处可读性，并允许跳过中间的默认参数",
                        "实现函数重载",
                        "把参数变成可空类型",
                    ),
                    correctIndex = 1,
                    explanation = "power(2.0, exponent = 3) 一眼能看出 3 是什么；没有命名参数时，想给后面的默认参数传值只能把前面的参数都写全或靠重载。",
                ),
                QuizQuestion(
                    question = "fun double(x: Int) = x * 2 的返回类型是？",
                    options = listOf(
                        "必须显式标注，否则编译错误",
                        "Unit",
                        "Int，由编译器推断",
                        "Any",
                    ),
                    correctIndex = 2,
                    explanation = "单表达式函数体的返回类型自动推断为表达式的类型 Int。只有函数体是代码块时才必须显式写返回类型。",
                ),
                QuizQuestion(
                    question = "关于顶层函数，说法正确的是？",
                    options = listOf(
                        "必须定义在某个 class 里",
                        "相当于 C++ 的自由函数，且不需要头文件声明",
                        "只能是 private 的",
                        "不能使用默认参数",
                    ),
                    correctIndex = 1,
                    explanation = "顶层函数直接写在文件里，任何地方 import 后可调用；默认参数等特性全部可用，可见性也可以按需标注。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-05",
            title = "空安全",
            concept = """
                Kotlin 把「能否为 null」编码进类型系统，NPE 从运行时事故变成**编译期拦截**。

                ## 可空类型

                - `String` 永远不为 null，`String?` 才可能为 null
                - 给非空类型赋 null 直接**编译错误**

                ## 安全调用与 Elvis

                - `?.` 是安全调用：`a?.length` 在 a 为 null 时整个表达式返回 null，**不会崩溃**
                - `?:` 是 Elvis 运算符：`a ?: 默认值` 在左侧为 null 时取右侧，常与 `?.` 连用做兜底

                ## !! 与智能转换

                - `!!` 是非空断言：告诉编译器「我确定它非空」，若实际为 null 会抛 NullPointerException，**应尽量少用**
                - 对可空值做过 null 判断（如 `if (x != null)`）后，编译器在同一作用域内自动「**智能转换**」为非空类型，无需手动强转
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "?、?.、?: 三个基础符号：",
                    code = """
                        val input: String? = null              // String? 才可以为 null

                        // ?. 安全调用：input 为 null 时整体结果为 null，不崩溃
                        val length: Int? = input?.length       // 结果是 null

                        // ?: Elvis：左侧为 null 时取右侧默认值
                        val display: String = input ?: "（空）" // display = "（空）"

                        // 组合：非空就取长度，空就用空串兜底
                        val size: Int = (input ?: "").length   // 0

                        println(length)                        // null：?. 在 null 上短路
                        println(display)                       // （空）：Elvis 兜底
                        println(size)                          // 0
                    """.trimIndent(),
                    expectedOutput = "null\n（空）\n0",
                ),
                CodeExample(
                    description = "智能转换、?.let 与 !! 断言：",
                    code = """
                        fun printLength(input: String?) {
                            if (input != null) {
                                println(input.length)          // 已智能转换为非空 String
                            } else {
                                println("输入为空")
                            }
                        }

                        input?.let { value ->
                            println("收到：${'$'}value")        // 仅非空时执行，value 是非空 String
                        }

                        printLength("Kotlin")                  // 6：走非空分支
                        printLength(null)                      // 输入为空：走 else 分支

                        // !! 非空断言：100% 确定非空才用，否则直接崩溃
                        // val mustExist: Int = input!!.length
                    """.trimIndent(),
                    expectedOutput = "6\n输入为空",
                ),
            ),
            cppComparison = """
                · C++ 里任何指针/引用都可能为空且编译器不检查；Kotlin 把可空性写进类型，`String s = null` **直接编译不过**。
                · `String?` 的使用体验接近 C++ 的 `std::optional<std::string>`，但 `?.`、`?:` 让读写语法自然得多。
                · `!!` 相当于 C++ 不加检查直接解引用——代码评审中通常被视为坏味道，**能用 `?.`/`?:` 就别用 `!!`**。
                · 智能转换类似「判空后自动收窄类型」，比 C++ 的 `if (p)` 手动守卫更进一步；注意可变属性可能被别的线程改掉时编译器会**拒绝智能转换**。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "val s: String = null 的结果是？",
                    options = listOf(
                        "编译错误：String 是非空类型",
                        "s 为 null",
                        "s 被置为空字符串 \"\"",
                        "运行时抛 NullPointerException",
                    ),
                    correctIndex = 0,
                    explanation = "String 不接受 null，想可空必须声明为 String?。这正是空安全的核心：把 NPE 拦截在编译期。",
                ),
                QuizQuestion(
                    question = "input?.length 在 input 为 null 时的结果是？",
                    options = listOf("0", "抛出异常", "null", "空字符串 \"\""),
                    correctIndex = 2,
                    explanation = "?. 安全调用在接收者为 null 时短路，整个表达式得到 null（所以结果类型是 Int?），不会崩溃也不会变成 0。",
                ),
                QuizQuestion(
                    question = "val x = a ?: b 等价于？",
                    options = listOf(
                        "if (a == null) b else a",
                        "if (a != null) b else a",
                        "a!!.let { b }",
                        "a?.let { it } ?: b 再加一次判空",
                    ),
                    correctIndex = 0,
                    explanation = "Elvis 运算符：左侧非空取左侧，为 null 取右侧 b。注意方向别记反——a 非空时用的是 a 本身。",
                ),
                QuizQuestion(
                    question = "关于 !! 非空断言，说法正确的是？",
                    options = listOf(
                        "把可空类型永久转换成非空类型",
                        "断言非空：为 null 时抛 NPE，应尽量少用",
                        "编译器会保证它永不崩溃",
                        "效果与 ?. 完全相同",
                    ),
                    correctIndex = 1,
                    explanation = "!! 只是「我确信非空」的承诺，运行时若为 null 立即抛 NullPointerException。优先用 ?.、?: 表达兜底逻辑。",
                ),
                QuizQuestion(
                    question = "fun f(s: String?) { if (s != null) println(s.length) } 为什么 s.length 能直接调用？",
                    options = listOf(
                        "length 是可空属性",
                        "智能转换：判空后编译器把 s 当作 String",
                        "编译器自动插入了 !!",
                        "参数 s 天然不可为 null",
                    ),
                    correctIndex = 1,
                    explanation = "在 s != null 的分支内，编译器把 s 智能转换为非空 String，无需手写强转。这是空安全少写样板代码的关键机制。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-06",
            title = "类与继承",
            concept = """
                Kotlin 的类与 C++ 最大的观念差异：**默认不可扩展，开放才需声明**。

                ## 主构造函数

                类用 `class` 声明，主构造函数直接写在类名后：`class Person(val name: String)`，构造参数可同时声明为属性，省去 C++ 里「成员声明 + 初始化列表」的重复。

                构造细节：

                - 参数可以有**默认值**：`class Person(val name: String, val age: Int = 18)`——默认值即「免费」的重载，不必像 C++ 那样写一串重载构造函数
                - `init { ... }` 块放构造时的初始化逻辑，按声明顺序随主构造函数执行
                - 还有次构造函数 `constructor(...)`，但必须委托到主构造函数——日常优先用「主构造 + 默认参数 + 命名参数」

                ## 默认 final 与 override

                - 类默认是 `final` 的，不能被继承；想被继承必须标 `open`，方法要被覆写同样要标 `open`——**与 C++ 正好相反**（C++ 默认可继承，final 才禁止）
                - 子类覆写必须写 `override`，漏写直接编译错误；动态绑定行为与 C++ 虚函数一致

                ## 继承与接口

                - 不支持类的多继承，但可以实现多个 `interface`
                - 抽象类用 `abstract` 标记，其抽象成员**不需要再写 open**

                ## 接口

                - `interface Shape { fun area(): Double }`：方法可以有**默认实现**，实现类不覆写就直接用默认逻辑
                - 属性也可声明：抽象属性由实现类提供，或接口里写自定义 getter——但接口**没有幕后字段**，不能存状态
                - 取舍：**接口无状态、可多实现**；抽象类可有状态与构造逻辑但只能单继承。组合多种能力用接口，共享状态与模板逻辑用抽象类

                ## 嵌套类与内部类

                - `class A { class B }`：B 是**嵌套类**，不持有外部实例的引用，直接 `A.B()` 使用——与 C++ 嵌套类一致
                - `class A { inner class B }`：B 是**内部类**，持有外部实例引用，必须通过外部实例创建（`a.B()`）；在 B 里引用外部 this 要写 `this@A`
                - 注意与 Java 的差异：Java 的内部类**默认**隐式持有外部实例，Kotlin 默认不持、需要时才显式标 `inner`——同样是「默认不可，开放才声明」的哲学
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "open、override 与主构造函数：",
                    code = """
                        open class Animal(val name: String) {        // open 才允许被继承
                            open fun speak(): String = "..."         // open 才允许被覆写
                            fun introduce() = "我叫 ${'$'}name"       // 非 open，子类不能覆写
                        }

                        class Dog(name: String) : Animal(name) {     // Animal(name) 相当于初始化列表
                            override fun speak() = "汪汪"
                        }

                        val a: Animal = Dog("旺财")
                        println(a.speak())                            // 汪汪：动态绑定与虚函数一致
                        println(a.introduce())                        // 我叫 旺财
                    """.trimIndent(),
                    expectedOutput = "汪汪\n我叫 旺财",
                ),
            ),
            cppComparison = """
                · **默认 final、显式 open**：与 C++ 相反，Kotlin 的哲学是「默认不可扩展，开放才需声明」。
                · `class Dog(name: String) : Animal(name)` 中括号里是在调用父类构造函数，对应 C++ 的初始化列表。
                · override 是强制的：C++ 里 override 可写可不写，Kotlin **漏写直接编译错误**。
                · `val name` 一个主构造参数同时完成「成员声明 + 构造赋值 + getter」，没有 C++ 字段/getter 风格之争。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 中 class 默认能被继承吗？",
                    options = listOf(
                        "能，和 C++ 一样",
                        "不能，默认 final，必须标 open",
                        "能，但子类必须标 final",
                        "不能，只能继承 abstract 类",
                    ),
                    correctIndex = 1,
                    explanation = "Kotlin 类默认 final，显式 open 才可被继承。目的是强迫设计者思考「这个类到底要不要被扩展」。",
                ),
                QuizQuestion(
                    question = "子类覆写父类方法需要哪些标记？",
                    options = listOf(
                        "父类方法标 open，子类方法标 override",
                        "只需要子类写 override",
                        "只需要父类写 open，子类随意",
                        "什么都不用写，同名即覆写",
                    ),
                    correctIndex = 0,
                    explanation = "open 声明「允许覆写」，override 声明「正在覆写」，两边都强制；任何一边漏写都是编译错误，避免意外覆盖。",
                ),
                QuizQuestion(
                    question = "class Person(val name: String) 中的 name 是？",
                    options = listOf(
                        "用完即丢的构造参数",
                        "Person 的只读属性",
                        "伴生对象的静态成员",
                        "局部变量",
                    ),
                    correctIndex = 1,
                    explanation = "主构造参数加 val/var 即成为属性，等于 C++ 里「声明字段 + 初始化列表赋值 + 提供 getter」三件事合一。",
                ),
                QuizQuestion(
                    question = "inner class 与普通嵌套类的核心区别是？",
                    options = listOf(
                        "inner class 持有外部实例的引用，需通过外部实例创建",
                        "inner class 不能访问外部类的成员",
                        "嵌套类持有外部实例，inner class 不持有",
                        "两者完全等价，只是写法不同",
                    ),
                    correctIndex = 0,
                    explanation = "inner class 持有外部 this（用 this@A 引用），必须先有外部实例 a.B()；嵌套类只是写在外部类内部的独立类，不持外部引用——与 C++ 嵌套类一致。",
                ),
                QuizQuestion(
                    question = "接口方法的默认实现意味着？",
                    options = listOf(
                        "实现类必须覆写接口的全部方法",
                        "实现类不覆写时，直接使用接口里的默认逻辑",
                        "接口可以像类一样被实例化",
                        "接口因此可以有幕后字段存状态",
                    ),
                    correctIndex = 1,
                    explanation = "默认实现让接口像「带方法体的抽象类」：实现类可选择覆写，不覆写就用默认。但接口仍无幕后字段、可多实现——这是它与抽象类的分界线。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-07",
            title = "data class",
            concept = """
                `data class` 专为承载数据而生：编译器自动生成 `equals`、`hashCode`、`toString`、`copy` 以及解构用的 `componentN` 函数。

                ## 属性写在主构造函数里

                数据属性直接写在主构造函数里（`val`/`var`），类体经常是空的。

                ## 内容相等

                `==` 比较的是**内容**（调用自动生成的 equals）：两个属性完全相同的对象相等，这正是 C++ 需要手写 `operator==` 才能得到的行为。

                ## copy 与不可变模型

                `copy` 在不动原对象的前提下「改几个字段」生成新实例，配合 `val` 实现不可变数据模型，**是 Kotlin 推荐的状态承载方式**。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "自动生成的 equals/toString 与 ===：",
                    code = """
                        data class User(val name: String, val age: Int)

                        val u1 = User("Ann", 20)
                        val u2 = User("Ann", 20)
                        println(u1 == u2)               // true：比较内容
                        println(u1 === u2)              // false：=== 才比较引用
                        println(u1)                     // User(name=Ann, age=20)：toString 自动生成
                    """.trimIndent(),
                    expectedOutput = "true\nfalse\nUser(name=Ann, age=20)",
                ),
                CodeExample(
                    description = "copy 与解构声明：",
                    code = """
                        val older = u1.copy(age = 21)   // 只覆盖 age，name 原样保留
                        println(older)                  // User(name=Ann, age=21)
                        println(u1.age)                 // 20：原对象不变

                        val (name, age) = u1            // 解构：按主构造参数顺序取出
                        println("${'$'}name 今年 ${'$'}age 岁")   // Ann 今年 20 岁
                    """.trimIndent(),
                    expectedOutput = "User(name=Ann, age=21)\n20\nAnn 今年 20 岁",
                ),
            ),
            cppComparison = """
                · data class ≈ 一个 struct 加上免费的手写 `operator==`、`operator<<` 和 tuple 式解构接口，**样板代码全免**。
                · copy ≈ 拷贝构造 + 指定成员覆盖，但以「生成新对象」的不可变风格表达，**原对象永远不动**。
                · `==` 是内容比较、`===` 才是地址比较——与 C++ 对类类型默认比较地址的语义不同，**习惯要先扭转过来**。
                · 解构声明对应 C++17 结构化绑定，但顺序由 componentN 决定，data class 自动按主构造参数生成。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "data class 会自动生成哪些成员？",
                    options = listOf(
                        "只有 toString",
                        "equals、hashCode、toString、copy、componentN",
                        "虚函数表和析构函数",
                        "静态工厂方法",
                    ),
                    correctIndex = 1,
                    explanation = "编译器按主构造参数生成这一整套：相等性、哈希、可读打印、按字段复制与解构，覆盖 C++ 手写运算符重载的常见场景。",
                ),
                QuizQuestion(
                    question = "两个属性完全相同的 data class 实例 u1 == u2 的结果是？",
                    options = listOf(
                        "true，比较内容",
                        "false，比较地址",
                        "编译错误",
                        "取决于运行时 hashCode",
                    ),
                    correctIndex = 0,
                    explanation = "data class 的 == 调用自动生成的 equals，按构造参数逐字段比较；=== 才比较是否同一个对象。",
                ),
                QuizQuestion(
                    question = "u1.copy(age = 21) 做了什么？",
                    options = listOf(
                        "把 u1 的 age 原地改成 21",
                        "生成一个 age 为 21、其余字段照抄的新实例，u1 不变",
                        "深拷贝整个对象图",
                        "把 val 属性变成 var",
                    ),
                    correctIndex = 1,
                    explanation = "copy 以「部分覆盖」方式产生新对象，是 val 不可变模型下表达「修改」的标准做法，原对象保持不变。",
                ),
                QuizQuestion(
                    question = "val (name, age) = u1 依赖的机制是？",
                    options = listOf(
                        "反射读取字段",
                        "编译器调用自动生成的 component1、component2",
                        "Map 的特殊语法",
                        "运算符重载 operator[]",
                    ),
                    correctIndex = 1,
                    explanation = "解构声明按约定调用 component1()、component2()……data class 按主构造参数顺序自动生成，普通类也可手动定义。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-08",
            title = "枚举与密封类",
            concept = """
                ## 枚举类

                - `enum class` 声明**值固定且穷尽**的一组常量：`enum class Planet { 水星, 金星, 地球 }`
                - `Planet.entries` 拿到全部枚举值（Kotlin 1.9+ 语法，旧写法 `values()`）；`Planet.valueOf("地球")` 按名字取常量，名字不存在会抛异常
                - 每个枚举常量都是**一个实例**，可带属性与方法：`enum class Planet(val gravity: Double) { 水星(3.7), ... }`
                - 与密封类的取舍：**枚举 = 值固定且穷尽，一个值一个实例、不携带不同状态**；**sealed = 子类型可携带不同状态**（Success 带数据、Loading 没有）。值不需要带状态用 enum，需要带不同状态用 sealed

                密封类（`sealed class` / `sealed interface`）把子类集合封闭起来，是表达「有限状态」的**标准工具**——当每个状态要携带不同数据时，它比枚举更合适。

                ## 封闭的子类集合

                所有子类必须定义在与它相同的**模块且同包**内，外部无法派生新子类型。

                ## when 穷尽性检查

                - 编译器因此「认识」全部可能的子类型：用 `when` 逐一处理每个子类，穷尽后可以不写 `else`
                - **漏掉任何一个分支都编译不过**
                - 修改可传播：将来新增一个子类，所有处理它的 `when` 都会立刻编译报错，**编译器替你找全每一处**

                ## 典型用途

                请求的成功/失败/加载、UI 事件等，比 enum + if 链、比 C++ 的 union + switch **安全得多**。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "密封接口 + when 穷尽处理：",
                    code = """
                        sealed interface ApiResult {
                            data class Success(val data: String) : ApiResult   // 子类可携带数据
                            data class Failure(val code: Int) : ApiResult
                            object Loading : ApiResult                          // 无数据的子状态用 object
                        }

                        fun render(result: ApiResult): String = when (result) {
                            is ApiResult.Success -> "数据：${'$'}{result.data}"  // is 判断类型并智能转换
                            is ApiResult.Failure -> "错误码：${'$'}{result.code}"
                            ApiResult.Loading -> "加载中…"
                        }   // 穷尽了所有子类，无需 else；漏写任何一个分支都编译不过

                        println(render(ApiResult.Success("Kotlin")))   // 数据：Kotlin
                        println(render(ApiResult.Failure(404)))        // 错误码：404
                        println(render(ApiResult.Loading))             // 加载中…
                    """.trimIndent(),
                    expectedOutput = "数据：Kotlin\n错误码：404\n加载中…",
                ),
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
                    expectedOutput = "水星：3.7\n金星：8.9\n地球：9.8\ntrue",
                ),
            ),
            cppComparison = """
                · 思路上接近 `std::variant<Success, Failure, Loading>` + `std::visit`，但 Kotlin 是**语言级支持**：每个子类自带字段与行为，不用 std::get_if 拼凑。
                · when 的穷尽性由**编译器检查**；C++ 的 switch-enum 没有「每个 variant 备选项必须处理」的保障，靠警告或运行时。
                · 新增子类时所有相关 when 立刻编译报错——相当于**免费的「修改影响面检查」**。
                · 子类可以是 data class 或 object，状态 + 数据一起建模，比「enum 值 + 平行变量记录结果」干净得多。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "密封类的核心约束是？",
                    options = listOf(
                        "所有子类必须定义在同一模块且同包内",
                        "子类必须写在同一个函数里",
                        "不能有 data class 子类",
                        "最多只能有一个子类",
                    ),
                    correctIndex = 0,
                    explanation = "子类型集合封闭在同模块同包内，编译器才能枚举全部子类，进而检查 when 的穷尽性。",
                ),
                QuizQuestion(
                    question = "when 对密封类型穷尽了所有子类后？",
                    options = listOf(
                        "还必须补一个 else 分支",
                        "可以省略 else，编译器已确认穷尽",
                        "运行时会再校验一次",
                        "必须改成 if-else 链",
                    ),
                    correctIndex = 1,
                    explanation = "穷尽时 else 不仅多余，写了反而会掩盖「新增子类未处理」的编译错误；不写 else 才能让编译器持续把关。",
                ),
                QuizQuestion(
                    question = "给密封类新增一个子类后会发生什么？",
                    options = listOf(
                        "所有处理该密封类的非穷尽 when 全部编译报错",
                        "什么都不会发生",
                        "程序运行到相关分支才出错",
                        "编译器自动生成默认处理",
                    ),
                    correctIndex = 0,
                    explanation = "新子类让原有的 when 不再穷尽，编译器逐一报错——这正是密封类作为状态建模工具的核心价值。",
                ),
                QuizQuestion(
                    question = "枚举与密封类如何取舍？",
                    options = listOf(
                        "值固定且不携带不同状态用枚举；子类型要携带不同状态用密封类",
                        "性能敏感用密封类，其余用枚举",
                        "枚举不能用于 when 匹配，密封类可以",
                        "两者完全等价，选哪个都一样",
                    ),
                    correctIndex = 0,
                    explanation = "枚举每个值是一个固定实例，穷尽但不携带不同状态；密封类的子类可以是携带各自数据的 data class——「成功带数据、失败带错误码、加载中无数据」就得用 sealed。",
                ),
                QuizQuestion(
                    question = "Planet.entries 与 Planet.valueOf(\"地球\") 分别做什么？",
                    options = listOf(
                        "entries 返回全部枚举值；valueOf 按名字取对应常量，名字不存在会抛异常",
                        "entries 返回枚举名列表；valueOf 每次创建一个新实例",
                        "entries 依赖反射实现；valueOf 只能在枚举定义文件内使用",
                        "两者等价，都是遍历枚举",
                    ),
                    correctIndex = 0,
                    explanation = "entries（Kotlin 1.9+，替代 values()）给出全部枚举常量，常用于遍历；valueOf(String) 按名字查找已有实例，传入不存在的名字会抛 IllegalArgumentException。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-09",
            title = "object 与伴生对象",
            concept = """
                Kotlin 没有 `static` 关键字，静态成员与单例分别由 `companion object` 和 `object` 承担。

                ## object 单例

                `object` 声明的是单例对象：全局只有一份、首次访问时**线程安全地初始化**，成员直接用 `ObjectName.member` 访问。

                ## companion object

                - 类内部可以声明 `companion object`（伴生对象），其成员通过类名访问，等价于 Java/C++ 的静态成员
                - 一个类**最多一个**伴生对象

                ## const val 与惯用法

                - 编译期常量用 `const val` 声明，且只能放在顶层或 object/companion 里，相当于 C++ 的 `constexpr` 常量
                - 习惯差异：Kotlin 的工具函数更常写成**顶层函数**，而不是统统塞进某个类的静态区
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "object 单例：",
                    code = """
                        object AppConfig {                     // 全局唯一实例，首次访问时初始化
                            const val VERSION = "1.0"          // 编译期常量
                            var cacheSize: Int = 64            // 普通属性
                            fun describe() = "App v${'$'}VERSION"
                        }

                        println(AppConfig.VERSION)             // 直接用名字访问，无需创建实例
                        AppConfig.cacheSize = 128

                        println(AppConfig.describe())          // App v1.0
                        println(AppConfig.cacheSize)           // 128
                    """.trimIndent(),
                    expectedOutput = "1.0\nApp v1.0\n128",
                ),
                CodeExample(
                    description = "companion object 与私有构造工厂：",
                    code = """
                        class User private constructor(val name: String) {   // 构造私有
                            companion object {
                                fun create(name: String) = User(name)        // 工厂方法
                                const val MAX_AGE = 150
                            }
                        }

                        val u = User.create("Ann")             // 通过伴生对象访问，如同静态成员
                        println(User.MAX_AGE)                  // 150
                        println(u.name)                        // Ann
                    """.trimIndent(),
                    expectedOutput = "150\nAnn",
                ),
            ),
            cppComparison = """
                · `object` ≈ Meyers 单例，但**由语言保证线程安全与全局唯一**，不用自己写双检锁。
                · `companion object` 的成员对应 C++ 的 static 成员；`const val` 对应 constexpr static 常量。
                · 没有 static 关键字——很多「静态工具」在 Kotlin 里写成**顶层函数**反而更符合惯例。
                · 私有构造 + companion 工厂方法是控制实例化的常见组合，等价于 C++ 私有构造函数 + 静态 create。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 用什么机制替代 static 成员？",
                    options = listOf("companion object", "保留的 static 关键字", "global 声明", "singleton 注解"),
                    correctIndex = 0,
                    explanation = "companion object 的成员通过类名访问，等价于静态成员；全局单例则直接用 object 声明。",
                ),
                QuizQuestion(
                    question = "object 声明的对象是？",
                    options = listOf(
                        "每次访问都新建一个实例",
                        "进程内唯一的单例，首次访问时初始化",
                        "必须手动 new 出来",
                        "只存在于编译期",
                    ),
                    correctIndex = 1,
                    explanation = "object 是语言级单例：懒加载且线程安全，等价于写对了的 Meyers 单例，但不用自己操心。",
                ),
                QuizQuestion(
                    question = "const val 可以声明在哪里？",
                    options = listOf(
                        "任何函数内部",
                        "顶层或 object / companion object 中",
                        "只能在 class 的方法外",
                        "只能在 interface 里",
                    ),
                    correctIndex = 1,
                    explanation = "const val 要求编译期内联常量，因此只允许顶层或 object/companion 中；函数内需要常量可用 val。",
                ),
                QuizQuestion(
                    question = "User.create(...) 中定义在 companion object 里的 create 相当于 C++ 的？",
                    options = listOf("虚函数", "静态成员函数", "友元函数", "构造函数重载"),
                    correctIndex = 1,
                    explanation = "companion object 成员通过类名调用、不依赖实例，对应 C++ 的 static 成员函数；此处常配合私有构造实现工厂方法。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-10",
            title = "扩展函数",
            concept = """
                扩展函数让你给任何已有类（包括标准库和第三方库的类）追加新函数，而**不修改其源码、也不写继承**。

                ## 定义与 this

                - 定义 `fun String.shout()` 后，任何 String 都能 `s.shout()` 直接调用
                - 函数体内用 `this` 指代接收者对象

                ## 静态解析（关键细节）

                - 本质仍是普通函数，编译成「接收者作为第一个参数」的静态调用，**没有虚函数开销**
                - 扩展函数按变量的「**声明类型**」静态解析，不是运行时类型——这点与成员函数的动态绑定不同

                ## 扩展属性与生态

                - 还有扩展属性（只有 getter、没有背后字段）和泛型扩展
                - Kotlin 标准库的 `let`、`map`、`filter` 等流畅 API 全靠扩展函数实现
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "给已有类型追加函数：",
                    code = """
                        fun String.isPalindrome(): Boolean =   // 给 String 追加函数
                            this == this.reversed()

                        fun Int.squared(): Int = this * this   // this 指代接收者

                        println("level".isPalindrome())        // true：用起来就像 String 自带的方法
                        println(7.squared())                   // 49
                    """.trimIndent(),
                    expectedOutput = "true\n49",
                ),
                CodeExample(
                    description = "扩展属性与泛型扩展：",
                    code = """
                        val String.wordCount: Int              // 扩展属性：只有 getter
                            get() = trim().split(Regex("\\s+")).size

                        println("hello kotlin world".wordCount)   // 3

                        fun <T> List<T>.secondOrNull(): T? =   // 泛型扩展函数
                            if (size >= 2) this[1] else null

                        println(listOf("a", "b", "c").secondOrNull())   // b
                    """.trimIndent(),
                    expectedOutput = "3\nb",
                ),
            ),
            cppComparison = """
                · C++ 没有这个语法：等价写法是自由函数 `bool isPalindrome(const std::string&)`，但调用形式只能是 `isPalindrome(s)` 而非 `s.isPalindrome()`（统一调用语法 UFCS 至今未进标准）。
                · 扩展函数**不会真正修改类**：不能访问 private 成员，也没有虚函数派发，一切在编译期静态决定。
                · **按声明类型解析**：`val a: Animal = Dog()` 上调用 Animal 的扩展时，即使 a 实际是 Dog 也不会匹配 Dog 的同名扩展——与成员函数行为不同，务必区分。
                · 标准库大量 API（`let`、`map`、`filter`、`use`）都是扩展函数，**读懂扩展函数是读懂 Kotlin 代码的前提**。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "扩展函数能否访问接收者类的 private 成员？",
                    options = listOf(
                        "能，扩展等同成员函数",
                        "不能，它本质是接收者作为首参的外部函数",
                        "能，但需要 open 标注",
                        "能，仅限同一文件内",
                    ),
                    correctIndex = 1,
                    explanation = "扩展函数并不真的进入类的内部：编译后就是第一个参数为接收者的普通函数，只能访问公开 API。",
                ),
                QuizQuestion(
                    question = "扩展函数的调用绑定发生在什么时候？",
                    options = listOf(
                        "运行时按对象实际类型",
                        "编译期按变量声明类型",
                        "由 JVM 动态链接决定",
                        "第一次调用后固定",
                    ),
                    correctIndex = 1,
                    explanation = "扩展函数是静态解析的：看声明类型。与成员函数的动态绑定不同，这是扩展函数最容易被误解的一点。",
                ),
                QuizQuestion(
                    question = "想给 String 追加「取最后一个字符」的能力，正确做法是？",
                    options = listOf(
                        "定义扩展函数 fun String.lastChar(): Char = this[this.length - 1]",
                        "继承 String 并添加方法",
                        "修改 String 类的源码",
                        "把 String 全部换成 StringBuilder",
                    ),
                    correctIndex = 0,
                    explanation = "扩展函数正是为「不能改别人类」的场景设计的；String 是 final 的，继承与改源码都不可行也不必要。",
                ),
                QuizQuestion(
                    question = "关于扩展属性 String.wordCount，正确的说法是？",
                    options = listOf(
                        "编译器自动生成了背后存储字段",
                        "没有背后字段，每次访问执行 getter 计算",
                        "等价于把属性加进 String 类",
                        "扩展属性必须声明为 var",
                    ),
                    correctIndex = 1,
                    explanation = "扩展属性只是「get() 计算逻辑 + 调用语法糖」，没有字段、不能有初始化器，每次访问都重新计算。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-11",
            title = "Lambda 与高阶函数",
            concept = """
                Kotlin 中**函数是一等公民**：可以存进变量、当参数传、当返回值。

                ## 函数类型与 lambda

                - 函数类型写作 `(Int, Int) -> Int`，lambda 用花括号表达：`{ a, b -> a + b }`
                - 高阶函数指「参数或返回值包含函数」的函数，它是集合操作、回调、DSL 的**基石**

                ## 尾随 lambda 与 it

                - lambda 是函数的最后一个参数时可写在括号外（尾随 lambda），这是 Kotlin 代码读起来像自然语言的关键
                - lambda 只有一个参数时可用隐式名 `it`

                ## inline 开销

                与 C++ 的 `std::function` 不同，Kotlin 标准库的高阶函数普遍标记 `inline`，lambda 会被内联，**通常没有额外的堆分配开销**。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "函数类型、尾随 lambda 与函数引用：",
                    code = """
                        fun multiply(a: Int, b: Int) = a * b

                        val square: (Int) -> Int = { x -> x * x }   // lambda 赋给函数类型变量
                        println(square(5))                           // 25

                        fun operate(x: Int, y: Int, op: (Int, Int) -> Int): Int = op(x, y)

                        println(operate(3, 4) { a, b -> a + b })     // 7：尾随 lambda 写在括号外
                        println(operate(3, 4, ::multiply))          // 12：::multiply 函数引用也能传
                    """.trimIndent(),
                    expectedOutput = "25\n7\n12",
                ),
                CodeExample(
                    description = "集合上的 lambda：隐式参数 it：",
                    code = """
                        val names = listOf("Kotlin", "Java", "Go")
                        val lengths = names.map { it.length }        // [6, 4, 2]：it 是唯一参数的隐式名
                        val short = names.filter { it.length <= 4 }  // [Java, Go]
                        println(lengths)                             // [6, 4, 2]
                        println(short)                               // [Java, Go]
                        names.forEach { name -> println(name) }      // 逻辑复杂时显式命名更清晰
                    """.trimIndent(),
                    expectedOutput = "[6, 4, 2]\n[Java, Go]\nKotlin\nJava\nGo",
                ),
            ),
            cppComparison = """
                · 对应 C++ 的 lambda 表达式 + `std::function`，但函数类型是 **Kotlin 语言内置概念**，不需要 std:: 包装。
                · 尾随 lambda 让回调写法接近 DSL：`operate(3, 4) { a, b -> a + b }` 比 `[](int a, int b){...}` 的仪式感少得多。
                · 标准库高阶函数普遍 inline，lambda 直接内联进调用点——**没有 std::function 的间接调用与可能的堆分配**。
                · 单参数隐式名 `it` 类似 boost 的 `_1` 占位符，但无需 bind，拿来即用。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "{ x: Int -> x * x } 是什么？",
                    options = listOf("字符串模板", "lambda 表达式", "匿名 object", "扩展函数"),
                    correctIndex = 1,
                    explanation = "花括号 + 箭头是 lambda 语法：箭头前是参数，后面是函数体。它实现了 (Int) -> Int 类型的函数。",
                ),
                QuizQuestion(
                    question = "list.map { it.length } 中的 it 指什么？",
                    options = listOf(
                        "list 本身",
                        "当前被处理的那个元素",
                        "元素下标",
                        "length 属性",
                    ),
                    correctIndex = 1,
                    explanation = "lambda 只有一个参数时可用隐式名 it，此处 it 依次是列表里的每个字符串，map 返回每个元素的 length 组成的新列表。",
                ),
                QuizQuestion(
                    question = "「尾随 lambda」指的是？",
                    options = listOf(
                        "lambda 作为最后一个参数时可以写在函数括号外",
                        "lambda 必须写在 return 之后",
                        "lambda 自动延迟执行",
                        "递归调用的 lambda",
                    ),
                    correctIndex = 0,
                    explanation = "尾随 lambda 语法：当 lambda 是最后一个实参时移到括号外，operate(3, 4) { a, b -> ... }，是 Kotlin DSL 风格的基础。",
                ),
                QuizQuestion(
                    question = "(Int) -> String 这个类型表示？",
                    options = listOf(
                        "一个类名",
                        "接收 Int、返回 String 的函数类型",
                        "Int 到 String 的隐式转换",
                        "返回可空 String 的方法",
                    ),
                    correctIndex = 1,
                    explanation = "函数类型语法：参数类型列表 -> 返回类型。(Int) -> String 的变量可以存任何符合签名的 lambda 或函数引用。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-12",
            title = "集合操作",
            concept = """
                Kotlin 把集合分成两套接口：只读的 `List`/`Set`/`Map` 与可变的 `MutableList`/`MutableSet`/`MutableMap`。

                ## 只读与可变

                - `listOf` 创建只读列表，`mutableListOf` 创建可变列表，**默认用只读是推荐风格**
                - 只读**不是深度不可变**：`List` 接口上没有 add/remove，但如果它只是可变集合的只读视图，原集合改了它也会跟着变

                ## 常用操作

                标准库用扩展函数提供 `map`、`filter`、`fold`、`groupBy`、`sorted` 等操作，链式组合、返回新集合，**替代手写循环**。

                ## 与 C++20 ranges

                这些操作对应 C++20 ranges 的 transform/filter，但**直接返回具体集合**，不需要 `ranges::to` 那样的收尾。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "常用操作链：",
                    code = """
                        val nums = listOf(5, 3, 8, 1, 9)               // 只读 List<Int>

                        val doubled = nums.map { it * 2 }               // [10, 6, 16, 2, 18]
                        val evens = nums.filter { it % 2 == 0 }         // [8]
                        val total = nums.fold(0) { acc, n -> acc + n }  // 26：带初始值的聚合
                        val sorted = nums.sorted()                      // [1, 3, 5, 8, 9]

                        // nums.add(10)   // 编译错误：只读接口没有 add

                        println(doubled)                                // [10, 6, 16, 2, 18]
                        println(evens)                                  // [8]
                        println(total)                                  // 26
                        println(sorted)                                 // [1, 3, 5, 8, 9]
                    """.trimIndent(),
                    expectedOutput = "[10, 6, 16, 2, 18]\n[8]\n26\n[1, 3, 5, 8, 9]",
                ),
                CodeExample(
                    description = "分组、查找与可变集合：",
                    code = """
                        val words = listOf("Kotlin", "is", "fun")
                        val byLength = words.groupBy { it.length }            // {6=[Kotlin], 2=[is], 3=[fun]}
                        val firstLong = words.firstOrNull { it.length > 3 }   // Kotlin；找不到返回 null

                        val mutable = mutableListOf("a")
                        mutable.add("b")                                       // 写操作只在可变集合上
                        mutable[0] = "A"                                       // 按下标替换

                        println(byLength)                                      // {6=[Kotlin], 2=[is], 3=[fun]}
                        println(firstLong)                                     // Kotlin
                        println(mutable)                                       // [A, b]
                    """.trimIndent(),
                    expectedOutput = "{6=[Kotlin], 2=[is], 3=[fun]}\nKotlin\n[A, b]",
                ),
            ),
            cppComparison = """
                · `listOf` ≈ `const std::vector`，`mutableListOf` 才是可修改的 vector；**写操作只存在于 Mutable 接口**。
                · `map`/`filter` ≈ ranges 的 transform/filter + 收集到新容器，一步到位直接返回集合。
                · `fold(0) { acc, n -> acc + n }` ≈ `std::accumulate(v.begin(), v.end(), 0)`。
                · 只读引用可能只是可变集合的视图：和 C++ 的 const 引用别名问题一样，把 MutableList 当 List 传出去后，原引用的修改会「**穿透**」。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "listOf 返回的 List 能调用 add 吗？",
                    options = listOf(
                        "能，add 对所有 List 开放",
                        "不能，List 接口没有 add，需要 MutableList",
                        "编译能过，运行时静默忽略",
                        "编译能过，运行时抛异常",
                    ),
                    correctIndex = 1,
                    explanation = "List 是只读接口，压根没有 add 方法，编译期就报错。注意：List 可能只是 MutableList 的视图，只读不等于深度不可变。",
                ),
                QuizQuestion(
                    question = "nums.fold(0) { acc, n -> acc + n } 相当于 C++ 的？",
                    options = listOf("std::sort", "std::accumulate", "std::for_each", "std::transform"),
                    correctIndex = 1,
                    explanation = "fold 带初始值逐元素聚合，语义与 std::accumulate 一致：0 是初值，lambda 是二元操作。",
                ),
                QuizQuestion(
                    question = "map、filter 这类操作对原集合的影响是？",
                    options = listOf(
                        "原地修改原集合",
                        "返回新集合，原集合不变",
                        "返回惰性迭代器且立即执行",
                        "没有返回值",
                    ),
                    correctIndex = 1,
                    explanation = "默认的 map/filter 是急切求值并返回新集合，原集合不受影响；需要惰性序列时才用 asSequence()。",
                ),
                QuizQuestion(
                    question = "words.groupBy { it.length }（words 是 List<String>）的返回类型是？",
                    options = listOf(
                        "List<Int>",
                        "Map<Int, List<String>>",
                        "Set<String>",
                        "MutableMap<String, Int>",
                    ),
                    correctIndex = 1,
                    explanation = "groupBy 以 lambda 结果为键、原元素列表为值：键是长度 Int，值是同长度的字符串列表。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-13",
            title = "区间与解构",
            concept = """
                区间与解构让循环和取值更接近数学写法：区间负责「范围」，解构负责「拆包」。

                ## 区间构造

                - `..`（闭区间）、`until`（半开）、`downTo`（倒序）、`step`（步长）
                - 类型如 `IntRange`，本质是**可迭代的序列**

                ## in 做成员判断

                `in` 不只用于 for 循环：`x in 1..100` 直接返回 Boolean，**比 C++ 的两段比较更接近数学写法**。

                ## 解构声明

                - 把一个对象一次拆成多个变量：`val (a, b) = pair`，编译器按约定调用 `component1()`、`component2()`
                - data class 自动生成 componentN，Pair/Triple 内置支持
                - for 循环里也能解构，比如遍历 Map：`for ((k, v) in map)`
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "区间构造与成员判断：",
                    code = """
                        for (i in 1..5) print(i)               // 12345：闭区间 [1, 5]
                        println()
                        for (i in 1 until 5) print(i)          // 1234：不含 5
                        println()
                        for (i in 10 downTo 1 step 3) print(i) // 10741：倒序步进
                        println()

                        val score = 86
                        println(score in 60..100)              // true：in 做区间成员判断
                        println(score !in 0..59)               // true：!in 表示不在区间内
                    """.trimIndent(),
                    expectedOutput = "12345\n1234\n10741\ntrue\ntrue",
                ),
                CodeExample(
                    description = "解构声明：",
                    code = """
                        val (id, score) = Pair("k-01", 98)     // 解构 Pair
                        val (x, y, z) = Triple(1, 2, 3)        // 解构 Triple

                        val users = mapOf("ann" to 20, "bob" to 25)
                        for ((name, age) in users) {           // 遍历 Map 时解构键值
                            println("${'$'}name -> ${'$'}age")
                        }
                    """.trimIndent(),
                    expectedOutput = "ann -> 20\nbob -> 25",
                ),
            ),
            cppComparison = """
                · `for (i in 1..n)` ≈ `for (int i = 1; i <= n; ++i)`；`until` 对应 `i < n`。**闭开区间选错是 off-by-one 的高发区**。
                · 解构声明对应 C++17 结构化绑定 `auto [a, b] = p;`，但 Kotlin 靠 componentN 约定，data class 自动支持且可自定义。
                · `x in 1..100` 比 `x >= 1 && x <= 100` 可读性高，还能用于 when 分支与 while 条件。
                · Pair ≈ std::pair，但正式代码里更推荐用**带字段名的 data class**，Pair 留给临时场合。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "区间 1 until 5 表示？",
                    options = listOf("[1, 5] 闭区间", "[1, 5) 半开区间", "(1, 5) 开区间", "[0, 5) 半开区间"),
                    correctIndex = 1,
                    explanation = "until 是半开区间，含头不含尾：1、2、3、4。数组/列表下标循环惯用 0 until size 避免越界。",
                ),
                QuizQuestion(
                    question = "for ((k, v) in map) 依赖什么机制？",
                    options = listOf(
                        "Map 专属的语法糖，无通用机制",
                        "解构声明 + Map.Entry 的 component1/component2 约定",
                        "反射读取字段名",
                        "迭代器运算符重载",
                    ),
                    correctIndex = 1,
                    explanation = "循环变量在括号里就是解构声明：每次迭代对 Map.Entry 调用 component1()（键）、component2()（值）。",
                ),
                QuizQuestion(
                    question = "x in 1..100 等价于？",
                    options = listOf(
                        "x >= 1 && x <= 100",
                        "x > 1 && x < 100",
                        "x == 1 || x == 100",
                        "x 属于列表 [1, 100]",
                    ),
                    correctIndex = 0,
                    explanation = ".. 是闭区间，in 判断成员，所以两端都包含。.. 可读成「从 1 到 100」。",
                ),
                QuizQuestion(
                    question = "下列哪种类型的实例不能直接解构？",
                    options = listOf("Pair", "Triple", "data class 实例", "没有定义 componentN 的普通类"),
                    correctIndex = 3,
                    explanation = "解构按 componentN 约定进行：Pair/Triple 内置、data class 自动生成；普通类没定义就无法解构（可以手动补上）。",
                ),
            ),
        ),
        Lesson(
            id = "kotlin-14",
            title = "异常处理",
            concept = """
                异常处理的结构与 C++ 一致：`try { } catch (e: XxxException) { } finally { }`，catch 按异常类型匹配，finally 无论成败都执行。

                ## try 与 throw 都是表达式

                - `try` 是表达式，可以有值：`val n = try { text.toInt() } catch (e: NumberFormatException) { 0 }` 把「尝试 + 兜底」写成一行
                - `throw` 也是表达式（类型为 Nothing），因此能出现在 Elvis 右侧：`name ?: throw IllegalArgumentException(...)`

                ## 没有受检异常

                函数签名**不声明会抛什么**，是否捕获由调用者决定。

                ## 资源释放：use

                资源释放不要依赖手写 finally，标准库的 `use` 扩展承担 **RAII** 的角色。
            """.trimIndent(),
            codeExamples = listOf(
                CodeExample(
                    description = "try 作为表达式：",
                    code = """
                        fun parseAge(text: String): Int =
                            try {
                                text.toInt()                       // 成功：返回解析结果
                            } catch (e: NumberFormatException) {
                                0                                  // 失败：返回兜底值
                            } finally {
                                println("解析完成")                 // 无论成败都执行
                            }

                        println(parseAge("18"))                    // 18
                        println(parseAge("abc"))                   // 0
                    """.trimIndent(),
                    expectedOutput = "解析完成\n18\n解析完成\n0",
                ),
                CodeExample(
                    description = "require 前置检查与 throw 表达式：",
                    code = """
                        fun requireAdult(age: Int): Int {
                            require(age >= 0) { "年龄不能为负：${'$'}age" }   // 失败抛 IllegalArgumentException
                            return age
                        }

                        fun nameOrThrow(name: String?): String =
                            name ?: throw IllegalArgumentException("名字不能为空")

                        try {
                            requireAdult(-1)
                        } catch (e: IllegalArgumentException) {
                            println("捕获：${'$'}{e.message}")
                        }
                    """.trimIndent(),
                    expectedOutput = "捕获：年龄不能为负：-1",
                ),
            ),
            cppComparison = """
                · try/catch/finally 的结构与 C++ 相同：按类型匹配、栈展开、finally 必执行（C++ 用 RAII 表达的部分，这里靠 finally）。
                · **没有受检异常**：不像 Java 要写 throws，也没有 C++ noexcept 那样的规范声明——异常是「未检查」的，文档里说明。
                · Kotlin 的 throw 类型是 Nothing（一切类型的子类型），可出现在任何需要值的位置（如 Elvis 右侧）；C++ 的 throw 虽也是表达式但类型为 void，只能在极有限的位置（如条件运算符操作数）使用。**类比结论不变：Kotlin 的写法更灵活**。
                · 关闭资源别手写 finally：`file.use { }` 等价 RAII，**Closeable 资源一律用它**，异常安全且代码更短。
            """.trimIndent(),
            questions = listOf(
                QuizQuestion(
                    question = "Kotlin 中 try 块的性质是？",
                    options = listOf(
                        "纯语句，没有值",
                        "表达式，可以有返回值",
                        "必须搭配两个以上 catch",
                        "只能在 main 函数中使用",
                    ),
                    correctIndex = 1,
                    explanation = "try/catch 是表达式，最后一个表达式的值就是整个 try 的值，常见于「尝试解析、失败给默认」的一行写法。",
                ),
                QuizQuestion(
                    question = "Kotlin 函数需要像 Java 那样在签名里声明 throws 吗？",
                    options = listOf("需要，编译器强制", "不需要，Kotlin 没有受检异常", "只有 IOException 需要", "由 @Throws 注解决定一切"),
                    correctIndex = 1,
                    explanation = "所有异常都不受检：签名不声明、编译器不检查调用方是否捕获（@Throws 只是为了给 Java 互操作生成声明）。",
                ),
                QuizQuestion(
                    question = "name ?: throw IllegalArgumentException(...) 之所以合法，是因为？",
                    options = listOf(
                        "throw 是表达式，类型为 Nothing",
                        "Elvis 运算符接受任意类型作右值",
                        "编译器对 ?: 做了特判",
                        "name 是 var 才行",
                    ),
                    correctIndex = 0,
                    explanation = "throw 的类型是 Nothing，是所有类型的子类型，所以能放进任何需要值的位置——包括 Elvis 右侧。",
                ),
                QuizQuestion(
                    question = "管理需要关闭的资源（如文件流），Kotlin 推荐的做法是？",
                    options = listOf(
                        "手写 finally { close() }",
                        "使用 use 扩展函数，等价于 RAII",
                        "交给 GC 自动关闭",
                        "在 try 里声明即可不用管",
                    ),
                    correctIndex = 1,
                    explanation = "use 保证资源无论正常结束还是抛异常都会被关闭，等价 C++ RAII；手写 finally 容易漏，GC 不负责及时关闭。",
                ),
            ),
        ),
    )

    /** 供聚合与测试使用的模块构造 */
    fun moduleOf(lessons: List<Lesson>) = CourseModule(
        id = "kotlin-basics",
        title = "Kotlin 语言基础",
        emoji = "🅺",
        lessons = lessons,
    )
}
