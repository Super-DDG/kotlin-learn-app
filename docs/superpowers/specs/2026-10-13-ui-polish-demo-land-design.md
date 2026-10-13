# 界面美化 + Demo 乐园 设计文档

日期：2026-10-13
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md`

## 背景与目标

「Kotlin 学习」App 已完成（3 模块 36 课 + 测验 + 进度）。本次迭代两项内容：

1. **界面美化**：当前 UI 是 Material 3 默认样式，缺乏视觉层次。改为"渐变活力"风格（用户在三方案对比中选定），提升学习动力感。
2. **Demo 乐园**：新增一个可玩的迷你示例集合（用户在四种候选中选定 3 个），每个 demo 展示一个已学知识点在真机上的实际效果，附"背后发生了什么"讲解与课程锚点。对应前置规格"范围外"清单中的"可运行示例页面"。

## 一、界面美化（风格 B：渐变活力）

### 主题色

- `ui/theme/Color.kt`：靛紫渐变系——主色 `#5B5BD6`（靛蓝）、辅色 `#9A4DD6`（紫）、渐变背景 `#E8ECFF → #F7F2FF → #FFFFFF`。
- `ui/theme/Theme.kt`：Material 3 `lightColorScheme` 定制（primary=#5B5BD6、secondary=#9A4DD6、tertiaryContainer 等派生色）。
- 不引入新依赖，不新增第三方库。

### 各屏幕

- **首页 HomeScreen**：标题渐变文字；进度条圆角；模块卡片大圆角 + 柔和阴影 + 右侧进度百分比徽章 + "继续第 N 课 →"（已开课时）或"从第 1 课开始 →"（未开课时）引导行；底部渐变色 "🎪 Demo 乐园" 入口卡。
- **课程列表 LessonListScreen**：课程序号圆形底色；已完成 ✅ 徽章样式化；标题渐变下划线装饰。
- **课文页 LessonScreen**：四段卡片区分别着色——📖 概念=主色淡底、💻 代码=深色代码块（深底浅字等宽字体、顶部装饰）、🆚 C++ 对比=暖色淡底、✅ 小测=白底描边。
- **动画**：Navigation 默认页面过渡；卡片内容用 `animateFloatAsState` 淡入（本身即动画教学素材）。

## 二、Demo 乐园

### 入口与结构

- 路由：`home` 首页新增渐变入口卡 → `demo-land`（DemoListScreen）→ `demo/{demoId}`（三个 demo 页）。
- 新代码放 `ui/demos/` 包：`DemoListScreen.kt`、`CounterDemo.kt`、`TodoDemo.kt`、`AnimationDemo.kt`。

### 三个迷你示例（首批）

| demoId | 名称 | 知识点 | 课程锚点 | 交互 |
|---|---|---|---|---|
| counter | 计数器 | remember / mutableStateOf | compose-04 | 大数字 + −1 / +1 / 重置按钮 |
| todo | 待办列表 | LazyColumn + 状态 | compose-03 / 06 | 输入框添加、勾选完成、删除 |
| animation | 动画按钮 | animate*AsState | compose-03 / 07 | 点击触发，可切换缩放 / 颜色 / 透明度三种动画 |

### Demo 页统一结构

1. 顶部：返回 + demo 名称 + 对应课程标签徽章（多锚点的 demo 如 todo 会显示两个徽章，均可点击跳转到对应课程）
2. 中部：可玩的交互区
3. 底部固定"🔍 背后发生了什么"卡：2~4 句中文讲解 + 课程锚点链接

### 架构约束

- 纯 Compose 状态（`remember` / `mutableStateOf` / `animate*AsState`），不引入 ViewModel（进阶主题，范围外），不做插件式注册框架（YAGNI，当前 3 个 demo）。
- demo 状态逻辑内联在 Composable 中，与现有"UI 以人工验证为主"的测试策略一致；单元测试不覆盖 demo UI。
- 课程锚点跳转复用现有 `Routes.lesson(lessonId)`。

## 范围外（后续迭代可加）

颜色调节器 demo、更多 demo、深色模式、ViewModel 版 demo、在线内容更新。
