# Logo 与系统启动屏 设计文档

日期：2026-10-13
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-12-kotlin-learning-app-design.md`

## 背景与目标

应用当前使用 Android Studio 模板默认图标、无启动屏。本次迭代：(1) 设计品牌 Logo 应用图标（方向 B：渐变底 + 白色代码符号 `</>`，用户在四方案对比中选定）；(2) 接入系统级启动屏（用户选定，无人工延迟的品牌启动体验）。零新依赖、零位图。

## 一、Logo（自适应图标）

### 设计定稿

- 形状：自适应图标标准（108dp 画布，安全区中央 66dp）
- 背景：品牌渐变 #5B5BD6→#9A4DD6（135°）
- 前景：白色 `</>` 符号，等宽字体风格笔画（矢量路径绘制）
- **专业裁定**：方向 B 示意中的小字 "KOTLIN" 在正式图标中去除——启动器图标通常仅 48dp，小文字不可读；系统启动屏也只显示图标。应用内无需要 Logo 字样的场景，不保留（YAGNI）。

### 技术落地（全矢量，无位图）

- `res/drawable/ic_launcher_background.xml`：替换模板绿色底为品牌渐变 vector
- `res/drawable/ic_launcher_foreground.xml`：白色 `</>` 矢量路径（安全区内）
- `res/mipmap-anydpi-v26/ic_launcher.xml` + `ic_launcher_round.xml`：自适应图标引用上述 drawable
- **API 24~25 兼容**（minSdk 24）：`res/mipmap-anydpi/` 放普通 vector（渐变圆角方 + 白色符号）作 legacy 图标；v26 限定符在 26+ 自动优先
- 删除模板生成的各密度位图（mipmap-mdpi..xxxhdpi 的 webp）

## 二、系统级启动屏（零新依赖）

- **API 31+**：`themes.xml` 增加 `android:windowSplashScreenBackground`（纯色 #5B5BD6——系统启动屏背景不支持渐变）+ `android:windowSplashScreenAnimatedIcon`（白色 `</>` 矢量）+ `android:postSplashScreenTheme`（指回现有主题）；冷启动瞬间显示，系统控制淡出
- **API 24~30**：无系统启动屏；`windowBackground` 改为 layer-list（品牌主色底 + 居中白色符号），冷启动不白屏
- 不引入 androidx.core-splashscreen（保持零依赖；用主题属性手写等效实现）
- v31 主题属性放 `values-v31/themes.xml`

## 测试与验证

纯资源改动：44/44 单测不受影响；验证 = assembleDebug 构建 + 目视检查（自适应图标形状、legacy 图标、启动屏显示与淡出）。

## 范围外（后续迭代可加）

应用内 Logo 展示位（首页/关于页）、带动画的品牌启动页、图标动态主题（Material You themed icon）。
