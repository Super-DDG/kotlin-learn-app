# Logo 与系统启动屏 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 应用图标替换为品牌 Logo（渐变底 + 白色 `</>` 矢量符号，B 方向），并接入零依赖的系统级启动屏。

**架构：** 全矢量资源——替换现有 adaptive-icon 引用的两个 drawable（背景渐变/前景符号），补 `mipmap-anydpi/` legacy vector 兼容 API 24~25，删除模板位图；启动屏用主题属性（v31 用 `windowSplashScreen*`，v24~30 用 `windowBackground` layer-list），清单与代码零改动。

**技术栈：** Android 资源（vector drawable / adaptive icon / 主题属性），零新依赖。

**规格：** `docs/superpowers/specs/2026-10-13-logo-splash-design.md`

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **44/44**（本迭代纯资源改动，不应变化）
- 清单 `android:icon="@mipmap/ic_launcher"` 保持不动（资源解析自动切换）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：品牌 Logo 图标（全矢量）

**文件：**
- 修改：`app/src/main/res/drawable/ic_launcher_background.xml`（整文件替换）
- 修改：`app/src/main/res/drawable/ic_launcher_foreground.xml`（整文件替换）
- 创建：`app/src/main/res/mipmap-anydpi/ic_launcher.xml`
- 创建：`app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`
- 删除：`app/src/main/res/mipmap-mdpi|hdpi|xhdpi|xxhdpi|xxxhdpi/ic_launcher.webp` 与 `ic_launcher_round.webp`（10 个文件）
- 创建：`app/src/main/res/values/colors.xml` 追加品牌色（供启动屏与 legacy 图标引用）

- [ ] **步骤 1：`colors.xml` 追加品牌色（追加在 `</resources>` 前）**

```xml
    <color name="brand_indigo">#FF5B5BD6</color>
    <color name="brand_violet">#FF9A4DD6</color>
    <color name="brand_grad_start">#FF5B5BD6</color>
    <color name="brand_grad_end">#FF9A4DD6</color>
```

- [ ] **步骤 2：整文件替换 `drawable/ic_launcher_background.xml`（渐变底）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr xmlns:aapt="http://schemas.android.com/aapt" name="android:fillColor">
            <gradient
                android:startX="0" android:startY="0"
                android:endX="108" android:endY="108"
                android:type="linear">
                <item android:offset="0.0" android:color="#FF5B5BD6" />
                <item android:offset="1.0" android:color="#FF9A4DD6" />
            </gradient>
        </aapt:attr>
    </path>
</vector>
```

- [ ] **步骤 3：整文件替换 `drawable/ic_launcher_foreground.xml`（白色 `</>` 符号，安全区内）**

自适应图标画布 108dp、安全区中央 66dp（即 21~87 范围）。符号设计：`<` 与 `>` 两个折线笔画 + 中间斜杠，等宽字体风格、白色、笔画宽 4dp、圆头。

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- `<`：折线 (40,44) → (30,54) → (40,64) -->
    <path
        android:pathData="M40,44 L30,54 L40,64"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000" />
    <!-- `/`：斜线 (48,68) → (60,40) -->
    <path
        android:pathData="M48,68 L60,40"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:fillColor="#00000000" />
    <!-- `>`：折线 (68,44) → (78,54) → (68,64) -->
    <path
        android:pathData="M68,44 L78,54 L68,64"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000" />
</vector>
```

- [ ] **步骤 4：创建 legacy 图标（API 24~25，普通 vector：圆角方渐变 + 符号）**

`mipmap-anydpi/ic_launcher.xml` 与 `mipmap-anydpi/ic_launcher_round.xml` 两文件内容相同：

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- 渐变圆角方底（覆盖全画布，系统按应用方形展示） -->
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr xmlns:aapt="http://schemas.android.com/aapt" name="android:fillColor">
            <gradient
                android:startX="0" android:startY="0"
                android:endX="108" android:endY="108"
                android:type="linear">
                <item android:offset="0.0" android:color="#FF5B5BD6" />
                <item android:offset="1.0" android:color="#FF9A4DD6" />
            </gradient>
        </aapt:attr>
    </path>
    <!-- `<` -->
    <path
        android:pathData="M40,44 L30,54 L40,64"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000" />
    <!-- `/` -->
    <path
        android:pathData="M48,68 L60,40"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:fillColor="#00000000" />
    <!-- `>` -->
    <path
        android:pathData="M68,44 L78,54 L68,64"
        android:strokeColor="#FFFFFFFF"
        android:strokeWidth="4"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:fillColor="#00000000" />
</vector>
```

- [ ] **步骤 5：删除模板位图**

```bash
rm app/src/main/res/mipmap-mdpi/ic_launcher.webp app/src/main/res/mipmap-mdpi/ic_launcher_round.webp \
   app/src/main/res/mipmap-hdpi/ic_launcher.webp app/src/main/res/mipmap-hdpi/ic_launcher_round.webp \
   app/src/main/res/mipmap-xhdpi/ic_launcher.webp app/src/main/res/mipmap-xhdpi/ic_launcher_round.webp \
   app/src/main/res/mipmap-xxhdpi/ic_launcher.webp app/src/main/res/mipmap-xxhdpi/ic_launcher_round.webp \
   app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp
rmdir app/src/main/res/mipmap-mdpi app/src/main/res/mipmap-hdpi app/src/main/res/mipmap-xhdpi app/src/main/res/mipmap-xxhdpi app/src/main/res/mipmap-xxxhdpi
```

- [ ] **步骤 6：验证**

运行：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL，资源链接通过）+ `./gradlew.bat :app:testDebugUnitTest`（44/44 不变）。

- [ ] **步骤 7：Commit**

```bash
git add app/src/main/res
git commit -m "feat: 品牌 Logo 图标（渐变底 + 代码符号，全矢量自适应图标）"
```

---

### 任务 2：系统级启动屏（主题属性）

**文件：**
- 创建：`app/src/main/res/values-v31/themes.xml`
- 修改：`app/src/main/res/values/themes.xml`（windowBackground layer-list）
- 创建：`app/src/main/res/drawable/splash_window_background.xml`（layer-list，供 v24~30 windowBackground）

- [ ] **步骤 1：创建 `drawable/splash_window_background.xml`（v24~30 冷启动窗口底：主色 + 居中符号）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/brand_indigo" />
    <item
        android:drawable="@drawable/ic_launcher_foreground"
        android:gravity="center" />
</layer-list>
```

说明：`ic_launcher_foreground` 是 108dp 画布、符号居中于安全区的 vector，作为窗口背景居中放置时视觉尺寸合理；若真机目视感觉偏小/偏大，可后续再调（规格已声明目视验证）。

- [ ] **步骤 2：`values/themes.xml` 追加 windowBackground（v24~30 生效；v31 会被 values-v31 覆盖）**

整文件替换为：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.KotlinLearn" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@drawable/splash_window_background</item>
    </style>
</resources>
```

- [ ] **步骤 3：创建 `values-v31/themes.xml`（Android 12+ 系统启动屏）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>

    <style name="Theme.KotlinLearn" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@drawable/splash_window_background</item>
        <item name="android:windowSplashScreenBackground">@color/brand_indigo</item>
        <item name="android:windowSplashScreenAnimatedIcon">@drawable/ic_launcher_foreground</item>
        <item name="android:postSplashScreenTheme">@style/Theme.KotlinLearn</item>
    </style>
</resources>
```

说明：系统启动屏背景只支持纯色，用品牌主色 #5B5BD6；图标用白色 `</>` 前景 vector；`postSplashScreenTheme` 指回自身（启动屏退出后应用照常渲染）。`windowSplashScreenAnimatedIcon` 名含 "Animated" 但接受静态 drawable。

- [ ] **步骤 4：验证**

运行：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）+ `./gradlew.bat :app:testDebugUnitTest`（44/44 不变）。
目视验证（如有设备/模拟器）：冷启动不白屏、显示品牌底 + `</>`；Android 12+ 显示系统启动屏并自动淡出。

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/res
git commit -m "feat: 系统级启动屏（品牌主色 + 代码符号，零依赖主题实现）"
```

---

## 自检记录

- **规格覆盖度**：Logo 渐变底/白色符号矢量（任务 1 步骤 2/3）✓；anydpi-v26 自适应引用不变（清单未动，自动生效）✓；API 24~25 legacy vector（任务 1 步骤 4）✓；删除模板位图（任务 1 步骤 5）✓；v31 系统启动屏三属性（任务 2 步骤 3）✓；v24~30 windowBackground layer-list（任务 2 步骤 1/2）✓；零新依赖 ✓；44/44 不受影响 ✓。
- **占位符扫描**：无 TBD/TODO；目视验证为规格明文要求的验证方式而非占位符。
- **类型一致性**：颜色资源名 `brand_indigo/brand_violet/brand_grad_start/brand_grad_end` 在 colors.xml 定义（任务 1 步骤 1）且启动屏引用 `@color/brand_indigo`（任务 2）；`splash_window_background`/`ic_launcher_foreground` 资源名跨任务一致；清单 `@mipmap/ic_launcher` 不动，由 mipmap-anydpi(-v26) 资源解析承载。
