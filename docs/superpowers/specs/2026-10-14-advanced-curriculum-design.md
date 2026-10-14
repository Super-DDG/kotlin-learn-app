# 第二批进阶课程（4 模块 23 课）设计文档

日期：2026-10-14
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md` 及后续各迭代规格

## 背景与目标

现有 3 模块 40 课覆盖入门。本批补齐"独立开发一个真实 App"的硬缺口：新增 4 个模块 23 课（40→63 课），沿用成熟的课程生产体系（四段式 + Markdown 排版 + C++ 对比 + 小测 + expectedOutput 离线运行结果 + CurriculumValidator 守门 + 逐模块独立内容审查）。

## 模块结构（追加在现有 3 模块之后，不并入）

### 模块四 Kotlin 进阶（id: kotlin-advanced，emoji: ⚡，6 课）

| id | 标题 | 知识点 |
|---|---|---|
| adv-01 | 协程：为什么需要 | 回调地狱对照、挂起的本质 |
| adv-02 | 协程基础 | runBlocking、launch、delay、suspend |
| adv-03 | 挂起函数与结构化并发 | 作用域、Job、cancel |
| adv-04 | 调度器与线程切换 | Dispatchers、withContext |
| adv-05 | 协程异常处理 | try-catch、CoroutineExceptionHandler |
| adv-06 | 作用域函数与委托属性 | let/run/apply/also、by lazy、Delegates |

### 模块五 数据与网络（id: data-network，emoji: 💾，7 课）

| id | 标题 | 知识点 |
|---|---|---|
| data-01 | 文件存储 | 内部/外部存储、SharedPreferences 概念 |
| data-02 | DataStore 深入 | 剖析本 App 的 ProgressStore 真实源码 |
| data-03 | Room：实体与 DAO | @Entity、@Dao、@Query |
| data-04 | Room：数据库与查询 | @Database、suspend/Flow 查询 |
| data-05 | HTTP 与 JSON 概念 | 请求/响应、序列化/反序列化 |
| data-06 | Retrofit 基础 | 接口定义、挂起函数、JSON 转换 |
| data-07 | 仓库模式 | 串起 Room + Retrofit 的完整数据流 |

### 模块六 架构（id: architecture，emoji: 🏛，5 课）

| id | 标题 | 知识点 |
|---|---|---|
| arch-01 | 副作用 API | LaunchedEffect、DisposableEffect、rememberCoroutineScope 边界 |
| arch-02 | rememberSaveable 与进程死亡恢复 | Bundle、状态保存时机 |
| arch-03 | ViewModel | 职责、生命周期、为什么不用组合层持有状态 |
| arch-04 | StateFlow 与 UI 状态 | 状态流、collectAsState、单向数据流 |
| arch-05 | 分层架构 | data/domain/ui 分层、剖析本 App 自身结构 |

### 模块七 工程化与发布（id: shipping，emoji: 🚢，5 课）

| id | 标题 | 知识点 |
|---|---|---|
| ship-01 | Service 与 BroadcastReceiver 概览 | 四大组件补完、使用场景 |
| ship-02 | 单元测试实战 | 剖析本 App 的 45 个测试、JUnit 断言与结构 |
| ship-03 | 构建变体与依赖管理 | debug/release、Version Catalog 进阶 |
| ship-04 | 应用签名与打包 | 密钥库、APK/AAB 差异 |
| ship-05 | 发布渠道与上架 | 应用市场流程、合规要点 |

总数 40 → 63 课。Fragment 与依赖注入（Hilt）留第三批。

## 内容生产规则

1. **沿用全部既有体系**：四段式（📖 讲解 / 💻 代码示例 / 🆚 C++ 对比 / ✅ 3~5 题小测）、Markdown 排版、CurriculumValidator 守门、每模块独立内容审查
2. **expectedOutput 协程课新规则**：并发输出天然不确定，与"逐段精确输出"体系冲突。裁定：协程示例一律用 `runBlocking` + 顺序可推导场景（单协程顺序执行、`delay` 时序确定）保证输出确定；并发交错语义用文字讲解，不做输出展示
3. **剖析本 App 系列**：data-02（ProgressStore）、arch-05（自身分层）、ship-02（45 个测试）以真实源码为教材
4. 每课示例 expectedOutput 非空（守门断言，同模块一标准）

## 测试与守门影响

- 4 个新内容守门测试文件：`KotlinAdvancedContentTest` / `DataNetworkContentTest` / `ArchitectureContentTest` / `ShippingContentTest`（各自断言课数、ID 连续、全部示例 expectedOutput 非空、通过校验器）
- `CurriculumContentTest`：3→7 模块、40→63 课、模块 ID 列表更新
- 首页/列表 UI 零改动（动态计算）；不新增 Demo（新课配 Demo 留第三批）

## 范围外（第三批候选）

Fragment、依赖注入（Hilt）、新课配套 Demo、学习笔记/收藏、深色模式适配完善。
