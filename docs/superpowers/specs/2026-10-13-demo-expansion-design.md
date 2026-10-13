# Demo 乐园扩充（4 demo + 4 配套课）设计文档

日期：2026-10-13
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md`、`2026-10-13-ui-polish-demo-land-design.md`

## 背景与目标

Demo 乐园现有 3 个 demo（计数器/待办列表/动画按钮）。本次迭代把常见 Material 3 UI 组件补齐：新增 4 个可玩 demo（输入/弹层/骨架/控制四类组件）+ 模块三新增 4 节配套课程（36→40 课），课程锚点与 demo 讲解卡互相打通。

## 一、4 个新 demo（沿用现有模式）

组织方式：沿用现有模式——每个 demo 独立页 + `DemoCatalog.items` 追加元数据 + AppNav `when` 补分支；`DemoListScreen` 数据驱动自动变 7 张卡；全部走 `DemoPageShell` 共享壳（讲解卡 + 课程锚点跳转）。纯 Compose 状态，无新依赖、无 ViewModel。

| demoId | 名称 | 交互内容 | 课程锚点 |
|---|---|---|---|
| login-form | 登录表单 | `OutlinedTextField` 用户名 + 密码框（`visualTransformation` 可见切换）、`Checkbox` 记住我、登录 `Button`（非空校验、错误提示）、`Snackbar` 欢迎反馈 | compose-13 |
| dialogs | 对话框与弹层 | `AlertDialog` 确认操作演示 + `ModalBottomSheet`（内容列表、拖拽关闭） | compose-11 |
| scaffold | 页面骨架 | 内嵌 `Scaffold`：`TopAppBar` + `TabRow` 三标签切换 + `FAB`（点击触发 `Snackbar`）；内嵌 Scaffold 置于 DemoPageShell 的 content 槽内 | compose-12 |
| controls | 开关与滑块 | `Switch` 开关、`Slider` 滑块（联动预览卡透明度）、`FilterChip` 多选标签组 | compose-14 |

新文件放 `ui/demos/`：`LoginFormDemo.kt`、`DialogDemo.kt`、`ScaffoldDemo.kt`、`ControlsDemo.kt`。

## 二、4 节配套新课（模块三追加，ID 顺延 compose-11..14）

| id | 标题 | 知识点 |
|---|---|---|
| compose-11 | 对话框与底部弹层 | AlertDialog 参数结构、ModalBottomSheet、临时 UI 层概念 |
| compose-12 | 页面骨架 Scaffold | 脚手架思想（对照 C++ 窗口框架）、TopAppBar/FAB/Snackbar 组合 |
| compose-13 | 输入组件与表单 | TextField 状态绑定（onValueChange 单向数据流）、键盘类型、错误提示 |
| compose-14 | 开关控制组件 | Switch/Slider/Chip 的选中状态模型 |

每课四段式（📖 讲解 / 💻 代码示例 / 🆚 C++ 对比 / ✅ 3~5 题小测），质量要求与前 36 课同标准：CurriculumValidator 守门 + 独立内容审查（技术准确性重点）。

## 三、测试与守门影响

- 既有守门测试**更新数据事实**（非放宽）：`ComposeBasicsContentTest` 10→14 课且 ID 连续到 compose-14；`CurriculumContentTest` 总数 36→40
- `HomeScreen` 进度文案用 `Curriculum.totalLessonCount` 动态计算，无需改动
- demo 为纯 UI 人工验证（与既有策略一致）；解析器/高亮器测试不变，全量基线仍为 42/42（守门测试只改断言不加用例，新课的小测由既有守门规则统一覆盖）
- 收尾验证：全量单测 + assembleDebug

## 范围外（后续迭代可加）

图片/图标组件 demo、PullToRefresh、暗色主题适配、更多进阶组件（Pager/WebView）。
