# Markdown 排版引擎与课程正文重排 设计文档

日期：2026-10-13
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md`

## 背景与目标

「Kotlin 学习」App 现状：课程正文是纯字符串塞进 `Text`，代码块单色渲染，排版拥挤。经统计，现有 36 课内容已自发使用 Markdown 式语法（199 处行内反引号、156 行 "·" 列表、8 处 `**` 加粗），但全部按字面渲染。

本次迭代：

1. **自研轻量 Markdown 渲染引擎**（零新依赖）+ Kotlin 语法高亮，让现有语法立即生效
2. **36 课正文全量重排**：长段落拆为分节小标题 + 列表，统一排版结构

## 一、Markdown 渲染引擎

### 语法子集（以现有内容实际语法为准，YAGNI 不做表格/图片/链接）

| 语法 | 效果 |
|---|---|
| `## ` / `### ` | 分节小标题 |
| `` `code` `` | 行内代码：淡紫圆角底 + 等宽字体 |
| `**text**` | 加粗 |
| `· ` / `- ` | 圆点列表项 |
| `1. ` | 数字列表项 |
| 空行 | 分段 |

不支持嵌套列表：列表项之间的非列表行结束当前列表块；解析器不抛异常，非法输入按字面文本渲染。

### 架构（纯逻辑与 UI 分离）

```
com.kotlin.learn.data.markdown/
  ├── MarkdownParser.kt     // 文本 → List<Block>；Block = Heading/Paragraph/BulletList/NumberList
  │                         // 段内 = List<InlineSpan>（Plain/InlineCode/Bold）
  └── KotlinHighlighter.kt  // Kotlin 代码 → List<CodeToken>（Keyword/String/Number/Comment/Annotation/Plain）
                             // 正则分词器，VS Code Dark+ 配色语义
com.kotlin.learn.ui.render/
  └── MarkdownText.kt       // List<Block> → Composable；行内 span → AnnotatedString
                             // 代码块用高亮 token 渲染 AnnotatedString（替换 LessonScreen 深底卡内的单色 Text）
```

- 解析器与高亮器为纯 Kotlin 逻辑，不依赖 Android，可单元测试
- 高亮配色加入 `ui/theme/Color.kt` 主题（CodeKeyword/CodeString/CodeNumber/CodeComment 等 dark-code 系列色），不散落
- 兜底：未闭合反引号等非法输入按字面文本渲染，解析不抛异常

## 二、36 课正文全量重排

- **concept**：重写为 Markdown 结构——导语句 + 分节小标题 + 关键句加粗 + 枚举列表；现有反引号行内代码保留
- **cppComparison**：`·` 行按列表渲染生效；补充 `**` 强调
- **codeExamples**：代码不动，注释风格统一
- **质量把关**：CurriculumValidator 守门规则不变；每模块一轮独立内容审查（技术准确性为重，与第一批课程同标准）

## 三、测试策略

- **解析器/高亮器 TDD**：先单测后实现。用例覆盖：三种列表、标题层级、行内代码与加粗混排、嵌套边界（`**` 内含 `` ` ``）、未闭合反引号字面兜底、空段合并、字符串/注释/嵌套注释等高亮分词。预计新增 12~18 个测试用例
- **渲染层**：纯 Composable，人工验证（与项目既有策略一致）
- **内容**：守门测试不放宽

## 范围外（后续迭代可加）

表格/图片/链接渲染、暗色主题下代码配色适配、第二批进阶课程内容（ViewModel/Room/网络等）。
