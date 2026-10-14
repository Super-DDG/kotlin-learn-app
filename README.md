# Kotlin Learn 📱

一个帮助 **C++ 程序员快速入门 Kotlin 与 Android 开发** 的中文学习 App。

全部课程内容内置离线可用，配套 11 个可玩 Demo 与实用工具，学习进度本地保存。App 的源码本身就是教材——"剖析本 App"系列课程直接以本项目真实代码为教学素材。

## ✨ 功能特性

- 📚 **7 大模块 63 课**，从零基础到独立开发：
  - **Kotlin 基础**（14 课）：变量、空安全、类、集合、Lambda……每课都有"给 C++ 开发者"对比框
  - **Android 核心概念**（12 课）：Activity、生命周期、Intent、资源系统、Gradle
  - **Compose UI 入门**（14 课）：声明式 UI、状态重组、导航、Material 3
  - **Kotlin 进阶**（6 课）：协程与挂起函数、结构化并发、作用域函数
  - **数据与网络**（7 课）：文件、DataStore、Room、Retrofit、仓库模式
  - **架构**（5 课）：副作用 API、ViewModel、StateFlow、分层架构
  - **工程化与发布**（5 课）：测试实战、构建变体、签名打包、上架流程
- 🧪 **每课随堂小测**：3~5 道选择题，答错显示解析，独立测验页
- ▶ **离线运行结果**：模块一每段示例代码附精确的预期输出（逐段人工推导并经真实编译运行核实）
- 🎪 **Demo 乐园 11 个可玩示例**：计数器、待办列表、动画、对话框、Scaffold、登录表单、以及 4 个真实可用的工具（计算器/单位换算器/倒计时器/音乐播放器）——课 ↔ Demo 双向跳转
- 💾 **学习进度追踪**：交卷即记完成、最好成绩留存（DataStore 持久化）
- 🆚 **C++ 视角讲解**：每个概念都从 C++ 程序员的已知知识切入，并指出迁移陷阱

## 📱 安装

从 [Releases](../../releases) 页面下载最新版 `KotlinLearn-v*.apk`（约 2.3 MB），传输到 Android 手机直接安装（需允许"安装未知应用"）。支持 Android 7.0（API 24）及以上。

## 🛠 从源码构建

```bash
git clone <本仓库地址>
cd kotlin_demo
./gradlew.bat :app:assembleDebug     # Windows（Git Bash）
./gradlew :app:assembleDebug         # Linux / macOS
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

要求：Android Studio（或命令行 Android SDK）+ JDK 21+。项目使用 Gradle Version Catalog 管理依赖，AGP 9 / Kotlin 2.2 / Compose BOM 2026.02。

## 📂 项目结构

```
app/src/main/java/com/kotlin/learn/
├── data/
│   ├── model/        # 课程数据模型（Lesson/QuizQuestion…）
│   ├── content/      # 7 大模块课程内容（纯 Kotlin 数据，即教材本体）
│   ├── logic/        # 可单测的纯逻辑（判分、进度、校验器、Markdown 解析…）
│   ├── progress/     # DataStore 学习进度存储
│   └── markdown/     # 自研轻量 Markdown 解析器 + Kotlin 语法高亮
├── ui/
│   ├── screens/      # 首页/课程列表/课文/测验页
│   ├── demos/        # Demo 乐园（教学 demo + 实用工具目录）
│   ├── tools/        # 实用工具屏（计算器/换算器/倒计时/播放器）
│   ├── render/       # Markdown 渲染层
│   └── navigation/   # 单 Activity 导航
scripts/              # 可复现资产脚本（生成内置旋律 WAV）
docs/superpowers/     # 设计文档与实现计划（规格驱动开发全过程留档）
```

## 🧪 测试

```bash
./gradlew.bat :app:testDebugUnitTest
```

74 个单元测试覆盖：测验判分、进度存储、课程数据完整性守门（每课结构/题数/输出非空）、Markdown 解析器、Kotlin 语法高亮分词器（无损覆盖不变量）、计算器状态机、单位换算。

## 🗺 学习路线建议

1. 按模块顺序学习，每课 3~5 分钟
2. 学完一课做随堂小测，交卷即记完成
3. 模块一的代码示例点"▶ 运行示例"对照输出理解
4. 模块二/三学完去 Demo 乐园玩对应示例（课文页有直达入口）
5. 学到 compose-09 时对照本仓库源码阅读——你现在用的 App 就是案例

## 📄 许可证

[MIT](LICENSE)
