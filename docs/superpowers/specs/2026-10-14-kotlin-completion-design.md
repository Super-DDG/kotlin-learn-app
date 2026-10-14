# Kotlin 课程补全批次（语言介绍 + 基础补完 + 进阶 7 课）设计文档

日期：2026-10-14
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-14-advanced-curriculum-design.md`

## 背景与目标

对照菜鸟教程 Kotlin 目录查漏 + 用户点名的进阶缺口，本批三项（总课数 63 → 71，模块数 7 不变）：

1. **模块一新增 kotlin-00「Kotlin 语言介绍」**（含 Java/C++ 三方对比），插在最前，其余 14 课 ID 不动
2. **基础补完**（对照菜鸟教程的 1 硬缺口 + 3 薄点，全部以小节增补并入现有课，ID 零改动）
3. **模块四新增 7 课**（adv-07..adv-13），补齐进阶缺口

## 一、kotlin-00「Kotlin 语言介绍」（模块一前插）

- concept：Kotlin 是什么/设计哲学（简洁、空安全、100% Java 互操作）→ **与 Java 对比**（空安全消 NPE、data class 消样板、函数是一等公民）→ **与 C++ 对比**（托管内存 vs 手动、无宏/模板元编程、协程 vs 线程、编译目标 JVM/Native/JS）→ 适用场景
- codeExamples：同一段逻辑的 **Kotlin/Java/C++ 三方对照**（两段示例：①Kotlin vs Java 同功能对照 ②Kotlin vs C++ 同功能对照，Kotlin 部分真实运行核实）
- cppComparison：本课即对比课，照常 3~5 条
- ID：`kotlin-00`，lessons 列表首位；守门改 15 课（kotlin-00..14）

## 二、基础补完（小节增补，ID 零改动）

| 课 | 增补内容 | 对应菜鸟章节 |
|---|---|---|
| kotlin-08 改造 | 标题改「枚举与密封类」；concept 补枚举语法小节（enum class、values/valueOf、枚举实现接口）；codeExamples 补枚举示例（真实运行）；小测补 1~2 题；与 sealed 的取舍对照（枚举=值固定实例，sealed=子类型任意布局） | 枚举类 |
| kotlin-06 增补 | concept 补两个小节：接口系统讲解（interface、默认实现、属性）、嵌套类与内部类（nested/inner 差异）、主次构造器细节；小测补 1~2 题 | 类和对象/接口 |
| kotlin-03 增补 | concept 补循环跳转小节：break/continue 与**标签跳转**（break@loop）；codeExamples 补 1 段标签示例（真实运行） | 循环控制 |
| kotlin-01 增补 | concept 补基础运算符概览小节（算术/比较/逻辑/区间运算符表；重载指向 adv-11） | 运算符 |

## 三、模块四新增 7 课（adv-07..adv-13）

| id | 标题 | 要点 | 输出核实 |
|---|---|---|---|
| adv-07 | 泛型与型变 | `in/out` 型变、reified、星投影；对照 C++ 模板 | 真实运行 |
| adv-08 | Flow 进阶 | 冷/热流、combine/zip、SharedFlow；对照回调流 | 真实运行（kotlinx.coroutines 在依赖中） |
| adv-09 | inline 与非局部返回 | inline/noinline/crossinline、性能与非局部 return；对照 C++ inline | 真实运行 |
| adv-10 | lambda with receiver 与 DSL | 接收者 lambda、@DslMarker；Compose 语法的原理 | 真实运行 |
| adv-11 | 运算符重载与中缀函数 | plus/get/rangeTo/invoke、infix；对照 C++ operator | 真实运行 |
| adv-12 | 密封接口与自定义委托 | sealed interface、自定义 ReadOnlyProperty、Delegates.observable | 真实运行 |
| adv-13 | 序列化 | kotlinx.serialization @Serializable、与手写解析对照 | **概念示意**（库未引入依赖，沿 Room/Retrofit 先例） |

模块四 6 → 13 课；标题「Kotlin 进阶」不变。

## 测试与守门影响

- `KotlinBasicsContentTest`：15 课（kotlin-00..14）、全部示例 expectedOutput 非空（新增段落同样守门）
- `KotlinAdvancedContentTest`：13 课（adv-01..13）
- `CurriculumContentTest`：7 模块 71 课（15+12+14+13+7+5+5）
- 收尾：全量 PASS + 输出真实性抽检（真实编译运行）

## 范围外（第三批候选）

Fragment、Hilt 依赖注入、新课配套 Demo、环境搭建课（App 内学习不需要）。
