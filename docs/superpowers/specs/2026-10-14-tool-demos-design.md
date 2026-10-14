# Demo 乐园实用工具批次（4 工具）设计文档

日期：2026-10-14
状态：已批准
前置规格：`docs/superpowers/specs/2026-10-13-ui-polish-demo-land-design.md`（Demo 乐园架构）

## 背景与目标

Demo 乐园现有 7 个教学 demo。本批新增 4 个**实用工具**（7→11 项），沿用现有架构（DemoCatalog + DemoPageShell + Routes.demo）。特色：计算器与换算器的核心逻辑是纯函数，正式引入 `data/logic/` 真单测 TDD；倒计时器与协程新课实时联动；播放器用真实 MediaPlayer。

## 一、4 个工具规格

### calculator 简易计算器（锚点 compose-13）

- 按键：0-9、小数点、+ − × ÷、=、C（清屏）、±（正负）
- **逐步运算语义**（非表达式解析）：输入数字 → 按运算符 → 再输数字 → 按 = 得结果，结果可继续运算；连续按 = 重复上一运算；除以 0 显示"除数不能为 0"错误态，按 C 恢复
- 核心逻辑抽 `data/logic/CalculatorLogic.kt` 纯函数状态机，TDD

### converter 单位换算器（锚点 compose-03）

- 三栏 Tab：长度（m 基准：mm/cm/m/km）、温度（℃↔℉）、重量（g 基准：g/kg/t）
- 输入数值 + 选择源/目标单位，实时换算显示
- 核心逻辑抽 `data/logic/ConverterLogic.kt` 纯函数（温度线性公式、负温度合法、不设绝对零度截断——教学从简），TDD

### timer 倒计时器（锚点 adv-02/adv-04）

- 设定分/秒（步进按钮），开始/暂停/继续/重置
- `LaunchedEffect` + `delay(1000)` 驱动倒数，环形进度显示剩余比例，归零提示
- 时序 UI 人工验证

### music 音乐播放器（锚点 compose-04）

- 资产：`scripts/generate_melody.py` 程序化生成八音盒旋律 WAV（C 大调音序、22050Hz/16bit 单声道、约 3~5 秒）→ `app/src/main/res/raw/demo_melody.wav`；脚本入库可复现
- MediaPlayer 播放/暂停切换、`Seekbar` 进度可拖动（seekTo）、播放完成自动复位；`DisposableEffect` 离开页面释放 MediaPlayer（arch-01 活例子）
- 状态 UI 人工验证

## 二、测试

- `CalculatorLogicTest`：8~10 条（四则、连续运算、连续等号、除零错误态、±、清屏、小数）
- `ConverterLogicTest`：6~8 条（长度三单位往返、温度 ℃↔℉ 往返与负值、重量三单位、边界值）
- timer/music：纯 UI 时序，人工验证
- DemoCatalog 7→11；基线 57 → 预计 65+ 全绿

## 范围外

科学计算器（括号/表达式解析）、更多单位类别、音频多曲目/循环列表、后台播放。
