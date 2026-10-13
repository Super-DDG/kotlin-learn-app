# 交互学习版（测验拆页 + 在线运行 + Demo 反向入口）设计文档

日期：2026-10-13
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md` 及后续各迭代规格

## 背景与目标

借鉴菜鸟教程的"概念 → 实例 → 试一试 → 输出"学习节奏，本轮三项：

1. **课程/测验拆页**：课文页专注学习，测验独立成页，降低单页长度与压迫感
2. **Kotlin Playground 在线运行**（模块一 14 课）：JetBrains 官方组件内嵌 WebView，示例代码可编辑、可运行、显示输出——菜鸟教程同款体验
3. **Demo 乐园反向入口**（模块二/三 8 课）：课文页直达对应 demo，补齐"课 ↔ demo"双向互通

## 一、课程/测验拆页

- 新路由 `quiz/{lessonId}` → `QuizScreen`：顶部返回 + "✅ 随堂测验 · {课名}" + 复用现有 `QuizSection`（判分/解析/重测逻辑零改动）
- `recordQuizResult` 交卷写进度逻辑从 LessonScreen 移入 QuizScreen；进度语义不变（交卷即记完成、保留最好成绩、首页/列表自动刷新）
- LessonScreen 移除 QuizSection，底部改为测验入口卡："✅ 开始随堂测验（N 题）→" 导航到测验页；LessonListScreen 完成标记数据不变

## 二、模块一示例"运行结果"（v2 修订：废弃在线 Playground）

> v1 曾实现 WebView + JetBrains Playground 在线运行；实机验证在中国大陆网络下组件加载不完整（编辑器不出现、运行无响应），且服务端在境外不可控。经用户裁定改为全离线的静态方案。

- `CodeExample` 模型新增 `expectedOutput: String` 字段（默认空串，模块二/三的 Compose/Android 示例无 stdout 输出，留空即不显示运行按钮）
- 模块一 14 课全部代码示例人工核对并填入**预期输出**（源自代码推导，多数示例的注释中已有输出线索）
- 代码卡交互：示例有输出时显示"▶ 运行示例"按钮，点击展开"运行结果"区（等宽字体，模拟终端）
- 完全离线：移除 PlaygroundScreen、playground 路由与 `INTERNET`/`ACCESS_NETWORK_STATE` 权限

## 三、Demo 反向入口

- 反查 `DemoCatalog.items`：`lessonIds` 含当前课的 demo 在课文页底部显示"🖥 去 Demo 乐园试玩：{demo 名}"按钮，点击经 `Routes.demo(demoId)` 直达
- 覆盖课：compose-03（animation/todo 两个入口）、compose-04（counter）、compose-06（todo）、compose-07（animation）、compose-11（dialogs）、compose-12（scaffold）、compose-13（login-form）、compose-14（controls），共 8 课

## 测试与验证

- QuizSection 判分逻辑不变，44/44 基线不动；拆页/Playground/Demo 入口为 UI 层，人工验证为主
- 验证：assembleDebug + 目视（测验页计分、在线运行需联网真机、离线降级提示）

## 范围外（后续迭代可加）

学习笔记/收藏、深色模式、每课要点小结卡、第二批进阶课程。
