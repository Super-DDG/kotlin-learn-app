# Kotlin & Android 学习 App 设计文档

日期：2026-10-12
状态：已批准

## 背景与目标

用户是有 C++ 背景、初次接触 Kotlin 和 Android 开发的学习者。本项目在现有模板工程 `kotlin_demo`（包名 `com.kotlin.learn`）上构建一个**交互式课程 App**：内置完整中文课程，装在手机上随时学习，帮助快速入门安卓开发。

### 成功标准

- App 内置 3 大模块、约 36 课的中文课程，每课 3~5 分钟阅读量
- 每课包含：概念讲解、代码示例、C++ 对比框、随堂小测（3~5 道选择题，答错显示解析）
- 首页显示学习进度（已学/总课数），课程列表标记已完成/未学
- 进度数据本地持久化，重启 App 不丢失
- 课程数据与 App 源码本身即是可读的 Kotlin 学习材料

## 技术选型

| 项 | 选择 | 理由 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | 现代主流，课程内容之一 |
| 架构 | 单 Activity + Navigation Compose | 简单清晰，适合入门 |
| 课程内容 | Kotlin `data class` / `sealed class` 硬编码数据 | 代码即内容；无 JSON 转义与解析样板 |
| 进度存储 | DataStore Preferences | 官方推荐方案，顺便学习现代组件 |
| 网络/数据库 | 无 | YAGNI，纯本地静态内容 |

否决的备选：JSON assets（转义痛苦、解析样板多）、Room（静态内容用数据库属过度设计）。

## 课程结构

### 模块一：Kotlin 语言基础（14 课）

变量与类型 → 字符串模板 → 条件与循环 → 函数与默认参数 → 空安全 → 类与继承 → data class → 密封类 → 对象与伴生对象 → 扩展函数 → Lambda 与高阶函数 → 集合操作 → 区间与解构 → 异常处理

### 模块二：Android 核心概念（12 课）

什么是 Activity → 生命周期 → 布局与 Compose 的关系 → Intent 与页面跳转 → 资源系统（res/）→ Manifest → 权限模型 → Context 是什么 → App 进程与主线程 → Gradle 构建体系 → 调试技巧 → 常见术语表

### 模块三：Compose UI 入门（10 课）

声明式 UI 思想 → Composable 函数 → 常用组件 → 状态与重组 → 状态提升 → LazyColumn 列表 → Material 3 主题 → 导航 → 本 App 自身架构剖析 → 下一步学习路线

### 每课统一结构

1. 📖 概念讲解（短）
2. 💻 代码示例（一两段短代码，带逐行注释）
3. 🆚 给 C++ 开发者（C++ 类比框 + 陷阱提醒）
4. ✅ 随堂小测（3~5 道选择题，答错显示解析）

## 架构与组件

```
com.kotlin.learn
├── MainActivity.kt              // 单 Activity 入口
├── data/
│   ├── model/Lesson.kt          // Lesson/Course/QuizQuestion 数据模型
│   ├── content/Curriculum.kt    // 课程内容入口（按模块分文件）
│   └── progress/ProgressStore.kt // DataStore 进度读写
├── ui/
│   ├── navigation/AppNav.kt     // Navigation Compose 路由
│   └── screens/
│       ├── HomeScreen.kt        // 首页：模块列表 + 进度总览
│       ├── LessonListScreen.kt  // 课程列表（已完成✓/未学标记）
│       ├── LessonScreen.kt      // 课文页（讲解/代码/对比/小测）
│       └── QuizSection.kt       // 小测组件（嵌入课文页底部）
```

## 数据流

- **课程数据**：静态 Kotlin 对象，UI 层直接读取，按 ID 查询。
- **进度数据**：`ProgressStore` 通过 DataStore 暴露 `Flow<Progress>`（已完成课时 ID 集合 + 每课测验最好成绩）；首页/列表页收集 Flow 自动刷新；小测交卷后写入。
- **路由**：`home` → `module/{moduleId}` → `lesson/{lessonId}`，无效 ID 显示"课程不存在"兜底页。

## 错误处理

纯本地静态数据，无网络/数据库异常路径。仅有的错误路径是路由无效 ID → 兜底提示页。

## 测试策略

单元测试（JUnit）覆盖：

1. **课程数据完整性**：每课有非空正文、C++ 对比、测验题；每题有且仅有一个正确选项；模块/课时 ID 唯一。
2. **测验判分逻辑**：全对/部分对/全错的计分与解析显示。
3. **进度存储逻辑**：完成课时、记录最好成绩、重复完成不回退。

UI 层以人工验证为主（学习项目，YAGNI）。

## 范围外（后续迭代可加）

可运行示例页面、进阶主题（ViewModel/Room/网络）、深色模式定制、在线更新课程。
