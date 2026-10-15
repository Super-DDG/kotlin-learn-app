# 工程结构指南 —— 写给熟悉 C++/CMake 的开发者

本文把整个 Android 工程逐目录讲清楚，并在每个概念旁给出它与 C++/CMake 世界的对应物。读完你应该能像看一个 CMake 工程一样"看懂"这个 App 工程。

---

## 0. 一张图总览

```
kotlin_demo/                       ← 仓库根 ≈ C++ 工程根目录
├── settings.gradle.kts            ← ≈ 顶层 CMakeLists.txt：声明有哪些子工程(module)
├── build.gradle.kts               ← ≈ 全局共用的编译选项/插件声明
├── gradle/
│   ├── libs.versions.toml         ← ≈ vcpkg/conan 清单：所有第三方依赖与版本集中在此
│   └── wrapper/                   ← ≈ 固定版本的构建器二进制（保证人人编译环境一致）
├── gradlew(.bat)                  ← ≈ 构建入口脚本（等价于 cmake --build .）
├── gradle.properties              ← ≈ 全局构建开关（JVM 参数、缓存等）
├── local.properties               ← 本机 SDK 路径（= 各自机器的路径，绝不入库）
├── keystore/ + keystore.properties← 签名密钥（≈ 你私人的代码签名证书）
├── app/                           ← 唯一的 application module ≈ 一个 executable target
│   ├── build.gradle.kts           ← ≈ app 自己的 CMakeLists.txt（编译选项、链接依赖）
│   └── src/
│       ├── main/                  ← 正式代码
│       │   ├── java/com/kotlin/learn/   ← Kotlin 源码（按包分层，≈ src/ 子目录）
│       │   ├── res/               ← 资源系统（C++ 没有的概念，见 §3）
│       │   └── AndroidManifest.xml← 应用清单（也是 C++ 没有的概念，见 §3）
│       └── test/                  ← JVM 单元测试（跑在电脑上，≈ 普通 gtest）
├── dist/                          ← 发布产物（重命名后的 APK）
└── docs/                          ← 设计文档与本文
```

---

## 1. 构建系统：Gradle 之于 Android ≈ CMake 之于 C++

Android 不用 Make/Ninja 直接驱动，而是用 **Gradle**。心智映射：

| C++/CMake 世界 | Android/Gradle 世界 | 说明 |
|---|---|---|
| `CMakeLists.txt` | `build.gradle.kts` | 构建脚本，同样是"声明式描述要构建什么" |
| `add_subdirectory(app)` | `settings.gradle.kts` 的 `include(":app")` | 声明子工程 |
| executable / library target | application / library **module** | 一个 module = 一个构建产物（本工程只有 `:app` 一个） |
| vcpkg / conan 清单 | `gradle/libs.versions.toml`（Version Catalog） | 第三方库与版本集中管理，代码里用 `libs.xxx` 引用 |
| `cmake --build . --target app` | `./gradlew.bat :app:assembleDebug` | `:` 即"哪个 module 的哪个任务" |
| CMAKE_BUILD_TYPE=Release | **buildType**：debug / release | 两条完整的构建流水线（见 §5） |
| `cmake --build .` 首次配置 | Gradle 首次运行会下载自身与依赖 | `gradlew` 是 Wrapper：锁死 Gradle 版本，人人一致 |

关键差异：**Gradle 是常驻守护进程**（daemon），首次构建慢是它在下载工具链与依赖；之后的增量构建很快（配置缓存已在本工程开启）。

### 顶层文件逐个说

- **`settings.gradle.kts`**：声明依赖仓库源（`google()`/`mavenCentral()` ≈ vcpkg 的远端索引）和参与构建的 module 列表。
- **根 `build.gradle.kts`**：目前几乎是空的——插件版本统一走 Version Catalog。
- **`gradle/libs.versions.toml`**：所有第三方库的"版本圣经"。改版本只改这里；`app/build.gradle.kts` 里 `libs.androidx.compose.material3` 这种 accessor 就是从它生成的。
- **`local.properties`**：写你本机的 Android SDK 路径（`sdk.dir=D:\...`）。因机器而异，所以被 .gitignore。
- **`gradlew` / `gradlew.bat`**：Wrapper 脚本。它读取 `gradle/wrapper/` 里的版本号，本地没有对应 Gradle 就自动下载——作用类似"用固定版本的预编译 cmake"，保证 CI 和同事的构建可复现。

---

## 2. `app` 模块：`app/build.gradle.kts` 逐段讲

```kotlin
android {
    namespace = "com.kotlin.learn"   // 包命名空间 ≈ C++ 的根 namespace
    compileSdk = 37                  // 用哪个版本的 SDK 头文件来"编译"
    defaultConfig {
        applicationId = "com.kotlin.learn"  // 应用在系统里的唯一身份证（安装后不可改）
        minSdk = 24                  // 最低支持 Android 7.0（≈ 最低系统要求）
        targetSdk = 37               // 声明"我按这个版本的系统行为测试过"（影响系统对 App 的策略）
        versionCode / versionName    // 整数递增的内部版本号 / 给人看的版本字符串
    }
    signingConfigs { ... }           // 发布签名（读 keystore.properties，凭据不入库）
    buildTypes { release { ... } }   // debug 与 release 两条流水线
}
dependencies {
    implementation(libs.androidx.compose.material3)  // 链接第三方库
    testImplementation(libs.junit)                   // 只进测试编译
}
```

三个"Sdk"的区分是 Android 新手第一大坑：

| 名称 | 类比 C++ | 改它会怎样 |
|---|---|---|
| compileSdk | 编译时用的 SDK 头文件版本 | 可以最高；用新 API 需要它够高 |
| minSdk | 最低系统要求 | 低于它的设备装不上 |
| targetSdk | "我适配到这个版本的系统行为" | 影响系统对你启用哪些新策略（如运行时权限），**与 compileSdk 独立** |

**buildType** 对比：

| | debug | release |
|---|---|---|
| 优化 | 无（快速构建，可断点调试） | **R8**：裁剪+混淆+优化 ≈ `-O2 + strip + LTO`（见 §6） |
| 签名 | 自动 debug 密钥 | 需要 release 密钥（`signingConfig`，本工程已配） |
| 体积 | 大（含调试信息） | 小（本工程约 2 MB） |
| 用途 | 日常开发 | 分发发布 |

---

## 3. C++ 没有的两个概念：资源系统与应用清单

### 3.1 `res/` 资源系统

Android 要求 UI 相关的东西不写死在代码里，而放在 `res/` 下按类型分目录，构建时由 **AAPT2** 工具编译并生成一个 `R` 类（`com.kotlin.learn.R`），代码里用 `R.string.app_name` 这样引用：

```
res/
├── values/strings.xml      ← 字符串（方便做多语言）
├── drawable/               ← 矢量图/图形（如我们的 Logo 前景）
├── mipmap-anydpi-v26/      ← 应用图标（自适应图标描述）
├── raw/demo_melody.wav     ← 原样打包的音频文件（播放器用）
└── xml/                    ← 备份规则等配置
```

目录名可以带**限定符后缀**：`values-zh/`（中文）、`mipmap-xxhdpi/`（高清屏）、`values-v31/`（Android 12+）——系统在运行时按设备自动挑选，这是 C++ 里要手写一堆 `#ifdef` 才能实现的多平台适配。

### 3.2 `AndroidManifest.xml` 应用清单

每个 Android 应用的"户口本"，系统认识它的唯一途径：

- 声明 `MainActivity` 是入口（带 `MAIN`/`LAUNCHER` intent 的那个 Activity 会在桌面生成图标）
- 声明权限（本工程刻意保持零权限——没有任何 `uses-permission`）
- 声明应用图标/名称/主题

C++ 世界没有对应物：你的 exe 不需要向操作系统"注册自己有哪些窗口"。Android 的每个界面组件（Activity）都必须在清单里登记，否则启动时直接崩。

---

## 4. Kotlin 源码包结构（本工程的真实分层）

包（package）≈ C++ 的 namespace + 目录结构强制一致。本工程按"数据在下、界面在上"分层，**依赖方向只允许向下**：

```
com.kotlin.learn/
├── MainActivity.kt        ← 入口宿主（唯一 Activity，≈ Win32 的窗口宿主）
├── data/                  ← 【数据层】不依赖任何 ui/
│   ├── model/             ← 纯数据类型（Lesson、QuizQuestion…≈ POD struct）
│   ├── content/           ← 课程内容本体（7 个 object，纯 Kotlin 数据）
│   ├── logic/             ← 可单测的纯逻辑（判分、进度合并、计算器状态机、换算）
│   ├── progress/          ← DataStore 持久化（学习进度存档）
│   ├── markdown/          ← 自研 Markdown 解析器 + 语法高亮分词器
│   └── ...
├── ui/                    ← 【界面层】
│   ├── screens/           ← 页面：首页/课程列表/课文页/测验页
│   ├── demos/             ← Demo 乐园目录与教学型 demo
│   ├── tools/             ← 实用工具屏（计算器/换算器/倒计时/播放器）
│   ├── render/            ← Markdown → 界面渲染
│   ├── theme/             ← 品牌颜色与主题
│   └── navigation/        ← 页面路由（单 Activity 内的多"页"切换）
```

两个与 C++ 不同的要点：

1. **没有头文件**：Kotlin 一个 `.kt` 文件即声明即实现，跨文件直接用（同包免 import）。所以没有 `.h/.cpp` 分离，也没有 CMake 的 target 链接关系——分层靠**依赖纪律**而非链接器。
2. **入口是 Activity 而非 `main()`**：`MainActivity.onCreate()` 由系统在合适时机调用（≈ 框架反向调用你的代码）。`setContent { ... }` 把整个界面交给 Compose 框架，之后界面 = 状态的函数，你不再手动"画"或"刷新"。

---

## 5. 测试与构建产物流水线

**测试分两处**：

- `app/src/test/`：**JVM 单元测试**，跑在你电脑的普通 JVM 上（≈ gtest），快，占绝大多数（本工程 74 个）
- `app/src/androidTest/`：仪器测试，要装到设备上跑（本工程只有模板示例，未使用）

**一次 release 构建的完整流水线**（`./gradlew.bat :app:assembleRelease` 时发生的事）：

```
.kt 源码 ──kotlinc──→ JVM 字节码(.class/.jar)
                          │
res/ 资源 ──AAPT2──→ 编译后的资源 + R 类
                          │
                          ▼
              R8（≈ 优化 + 裁剪 + 混淆 + 重命名）
              ├─ 删掉没被引用的类/方法（≈ --gc-sections）
              ├─ 短名字混淆（a/b/c…），产出 mapping.txt ≈ 符号表
              └─ 优化内联（≈ -O2）
                          │
                          ▼
              d8 转成 Android 专属的 .dex 字节码
                          │
              AAPT2 打包资源 + dex + Manifest → 未签名 APK（本质是 zip）
                          │
              apksigner 签名（v2/v3 方案）──→ 可安装的 APK
```

装到设备后由 **ART**（Android 运行时，≈ 一个自带 JIT/AOT 的 JVM）执行。这也解释了本次 Release 闪退的根因：R8 处理过的字节码被改名重组，一旦 keep 规则不当，运行时反射/继承就会断链——Debug 版不走 R8，所以永远不复现。

---

## 6. 术语表

| 术语 | 一句话解释 |
|---|---|
| SDK | Android 系统头文件+工具集（编译时用，按 compileSdk 选） |
| ART | Android 运行时，设备上执行 dex 字节码的虚拟机 |
| AAPT2 | 资源打包工具（编译 res/、生成 R 类） |
| R8 | 裁剪/优化/混淆器（≈ -O2 + strip + rename 的合体） |
| APK / AAB | 安装包格式 / 上架用格式（AAB 由商店按设备拆分） |
| ADB | 电脑与设备通信的命令行工具（安装/日志/ shell） |
| Compose | 声明式 UI 框架（界面 = 状态的函数） |
| Activity | 单个"屏幕"的宿主组件，系统托管其生命周期 |
| ViewModel | 活过屏幕旋转的数据持有者（架构课详述） |
| DataStore | 键值持久化存储（本工程存学习进度） |

## 7. 常用命令速查

```bash
./gradlew.bat :app:assembleDebug     # 打 Debug APK
./gradlew.bat :app:assembleRelease   # 打签名 Release APK
./gradlew.bat :app:testDebugUnitTest # 跑全部单元测试
./gradlew.bat :app:installDebug      # 装到连着的设备
adb logcat                           # 看设备日志（崩溃排查必备）
adb shell am start -n com.kotlin.learn/.MainActivity   # 启动应用
```
