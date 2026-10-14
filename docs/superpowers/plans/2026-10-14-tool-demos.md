# Demo 乐园实用工具批次（4 工具）实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** Demo 乐园新增 4 个实用工具（简易计算器/单位换算器/倒计时器/音乐播放器，7→11 项）；计算器与换算器核心逻辑以纯函数 + 真单测 TDD 落地。

**架构：** `CalculatorLogic`/`ConverterLogic` 纯函数状态机放 `data/logic/`（与 QuizScorer/ProgressLogic 同区）；四个工具 UI 放 `ui/tools/`（新包，区别于教学型 ui/demos——工具型与教学型在 DemoCatalog 里并列展示）；音频资产由入库脚本程序化生成。

**技术栈：** 既有 Compose + MediaPlayer（框架 API），零 Gradle 依赖；Python 仅用于资产生成脚本。

**规格：** `docs/superpowers/specs/2026-10-14-tool-demos-design.md`

**全局约定：**
- 工程根 E:\android\kotlin_demo；Windows + Git Bash，gradle 用 `./gradlew.bat`（超时 600000ms）
- 全量单测基线 **57/57**；任务 1/2 各增 8~10 与 6~8 条，收尾以"全部 PASS"为准并报告精确总数
- DemoCatalog 的 DemoItem：lessonIds 用课程锚点（calculator→compose-13、converter→compose-03、timer→adv-02+adv-04、music→compose-04）
- commit message 中文 Conventional Commits，结尾 `Co-Authored-By: Claude Code <noreply@anthropic.com>`

---

### 任务 1：CalculatorLogic（TDD）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/logic/CalculatorLogic.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/CalculatorLogicTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.logic.CalculatorLogic.initial
import com.kotlin.learn.data.logic.CalculatorLogic.onClear
import com.kotlin.learn.data.logic.CalculatorLogic.onDigit
import com.kotlin.learn.data.logic.CalculatorLogic.onEquals
import com.kotlin.learn.data.logic.CalculatorLogic.onOperator
import com.kotlin.learn.data.logic.CalculatorLogic.onSign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CalculatorLogicTest {
    private fun type(s: String) = s.fold(initial()) { acc, c -> onDigit(acc, c) }

    @Test
    fun `四则运算`() {
        assertEquals("8", onEquals(onOperator(onEquals(onOperator(type("3"), '+'), '5'), '×')).display)
        // 逐步语义：3+5=8 → 8×2=16
        assertEquals("16", onEquals(onOperator(onEquals(onOperator(onEquals(onOperator(type("3"), '+'), '5'), '×'), '2'))).display)
    }

    @Test
    fun `连续等号重复上一运算`() {
        var s = onEquals(onOperator(type("5"), '+'), '3')   // 8
        s = onEquals(s)                                     // 8+3=11
        s = onEquals(s)                                     // 11+3=14
        assertEquals("14", s.display)
    }

    @Test
    fun `除零进入错误态且按运算符无效`() {
        var s = onEquals(onOperator(type("7"), '÷'), '0')
        assertNotNull(s.error)
        assertEquals("除数不能为 0", s.error)
        val after = onOperator(s, '+')
        assertEquals(s, after)
    }

    @Test
    fun `C 恢复初始态`() {
        val s = onClear(onEquals(onOperator(type("7"), '÷'), '0'))
        assertNull(s.error)
        assertEquals(initial(), s)
    }

    @Test
    fun `正负号切换`() {
        assertEquals("-5", onSign(type("5")).display)
        assertEquals("5", onSign(onSign(type("5"))).display)
    }

    @Test
    fun `小数输入与运算`() {
        var s = onDigit(type("1"), '.')
        s = onDigit(s, '5')
        s = onEquals(onOperator(s, '+'), '2')
        assertEquals("3.5", s.display)
    }

    @Test
    fun `整数结果不带小数点`() {
        assertEquals("5", onEquals(onOperator(type("2"), '+'), '3').display)
    }

    @Test
    fun `等号前未输第二操作数按当前显示值运算`() {
        assertEquals("5", onEquals(onOperator(type("5"), '+')).display)
    }

    @Test
    fun `小数点不重复`() {
        var s = onDigit(type("1"), '.')
        s = onDigit(s, '.')
        assertEquals("1.", s.display)
    }

    @Test
    fun `错误态下输数字恢复输入`() {
        val s = onEquals(onOperator(type("7"), '÷'), '0')
        val recovered = onDigit(s, '9')
        assertNull(recovered.error)
        assertEquals("9", recovered.display)
    }
}
```

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CalculatorLogicTest"`
预期：编译失败（`CalculatorLogic` 不存在）

- [ ] **步骤 3：编写实现**

```kotlin
package com.kotlin.learn.data.logic

/**
 * 简易计算器的逐步运算状态机（纯逻辑，供 UI 层驱动与单元测试）。
 * 语义：输入数字 → 按运算符 → 再输数字 → 按 = 得结果，结果可继续运算；
 * 连续按 = 重复上一运算；除零进入错误态，仅 C 可恢复。
 */
object CalculatorLogic {

    data class State(
        val display: String = "0",
        val accumulator: Double? = null,   // 累加值（第一操作数）
        val pendingOp: Char? = null,       // 待执行运算符
        val enteringNumber: Boolean = false, // 正在输入数字
        val lastOp: Char? = null,          // 连续等号用：上一运算
        val lastOperand: Double? = null,
        val error: String? = null,
    )

    fun initial() = State()

    private fun apply(a: Double, op: Char, b: Double): Double = when (op) {
        '+' -> a + b
        '-' -> a - b
        '×' -> a * b
        '÷' -> a / b
        else -> b
    }

    private fun format(v: Double): String =
        if (v == v.toLong().toDouble() && kotlin.math.abs(v) < 1e15) v.toLong().toString() else v.toString()

    fun onDigit(s: State, d: Char): State {
        if (s.error != null) return onClear(s).let { onDigit(it, d) }
        val base = if (s.enteringNumber) s.display else "0"
        val next = when {
            d == '.' && base.contains('.') -> s                     // 小数点不重复
            d == '.' && !s.enteringNumber -> "0."
            d == '0' && base == "0" -> s                            // 前导零
            !s.enteringNumber -> d.toString()
            base.length >= 12 -> s                                  // 长度上限
            else -> base + d
        }
        return s.copy(display = next, enteringNumber = true, error = null)
    }

    fun onOperator(s: State, op: Char): State {
        if (s.error != null) return s
        val current = s.display.toDouble()
        val acc = when {
            s.accumulator != null && s.enteringNumber ->
                apply(s.accumulator, s.pendingOp ?: op, current)     // 3+5×：先算 3+5
            s.accumulator != null -> s.accumulator                   // 5 + − ×：替换运算符
            else -> current                                          // 首个运算符
        }
        return s.copy(
            display = format(acc),
            accumulator = acc,
            pendingOp = op,
            enteringNumber = false,
        )
    }

    fun onEquals(s: State): State {
        if (s.error != null) return s
        return when {
            s.pendingOp != null && s.enteringNumber -> {             // 常规：a op b
                val b = s.display.toDouble()
                val a = s.accumulator ?: 0.0
                compute(s, a, s.pendingOp, b)
            }
            s.pendingOp != null && s.accumulator != null -> {        // = 前未输第二数：op 累加值
                val a = s.accumulator
                val b = s.accumulator ?: 0.0
                compute(s, a, s.pendingOp, b)
            }
            s.lastOp != null && s.lastOperand != null ->             // 连续 =：重复上一运算
                compute(s.copy(accumulator = s.display.toDouble()), s.display.toDouble(), s.lastOp, s.lastOperand)
            else -> s
        }
    }

    private fun compute(s: State, a: Double, op: Char, b: Double): State {
        if (op == '÷' && b == 0.0) {
            return State(error = "除数不能为 0")
        }
        val r = apply(a, op, b)
        return State(
            display = format(r),
            lastOp = op,
            lastOperand = b,
            accumulator = null,
            enteringNumber = false,
        )
    }

    fun onSign(s: State): State {
        if (s.error != null) return s
        val v = s.display.toDoubleOrNull() ?: return s
        return s.copy(display = format(-v))
    }

    fun onClear(s: State): State = State()
}
```

注意：`onEquals` 的第二分支（= 前未输第二数）语义为"op 累加值"（5 + = → 10）；若实现/测试出现矛盾，以测试断言为准并在报告说明。

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.CalculatorLogicTest"`
预期：10 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/logic/CalculatorLogic.kt app/src/test/java/com/kotlin/learn/data/CalculatorLogicTest.kt
git commit -m "feat: 计算器逐步运算纯逻辑（TDD）"
```

---

### 任务 2：ConverterLogic（TDD）

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/data/logic/ConverterLogic.kt`
- 测试：`app/src/test/java/com/kotlin/learn/data/ConverterLogicTest.kt`

- [ ] **步骤 1：编写失败的测试**

```kotlin
package com.kotlin.learn.data

import com.kotlin.learn.data.logic.ConverterLogic.LengthUnit
import com.kotlin.learn.data.logic.ConverterLogic.TempUnit
import com.kotlin.learn.data.logic.ConverterLogic.WeightUnit
import com.kotlin.learn.data.logic.ConverterLogic.convertLength
import com.kotlin.learn.data.logic.ConverterLogic.convertTemp
import com.kotlin.learn.data.logic.ConverterLogic.convertWeight
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterLogicTest {
    @Test
    fun `长度换算以米为基准`() {
        assertEquals(1000.0, convertLength(1.0, LengthUnit.M, LengthUnit.MM), 1e-9)
        assertEquals(0.001, convertLength(1.0, LengthUnit.MM, LengthUnit.M), 1e-12)
        assertEquals(100000.0, convertLength(1.0, LengthUnit.KM, LengthUnit.CM), 1e-6)
        assertEquals(2.5, convertLength(2500.0, LengthUnit.MM, LengthUnit.CM), 1e-9)
    }

    @Test
    fun `温度换算摄氏华氏`() {
        assertEquals(212.0, convertTemp(100.0, TempUnit.C, TempUnit.F), 1e-9)
        assertEquals(0.0, convertTemp(32.0, TempUnit.F, TempUnit.C), 1e-9)
    }

    @Test
    fun `负四十度两种单位相等`() {
        assertEquals(-40.0, convertTemp(-40.0, TempUnit.C, TempUnit.F), 1e-9)
        assertEquals(-40.0, convertTemp(-40.0, TempUnit.F, TempUnit.C), 1e-9)
    }

    @Test
    fun `重量换算以克为基准`() {
        assertEquals(1.0, convertWeight(1000.0, WeightUnit.G, WeightUnit.KG), 1e-9)
        assertEquals(1000000.0, convertWeight(1.0, WeightUnit.T, WeightUnit.G), 1e-6)
    }

    @Test
    fun `同单位换算是恒等`() {
        assertEquals(3.14, convertLength(3.14, LengthUnit.CM, LengthUnit.CM), 1e-12)
        assertEquals(-5.0, convertWeight(-5.0, WeightUnit.KG, WeightUnit.G), 1e-9)
    }
}
```

- [ ] **步骤 2：运行验证失败**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ConverterLogicTest"`
预期：编译失败

- [ ] **步骤 3：编写实现**

```kotlin
package com.kotlin.learn.data.logic

/** 单位换算纯逻辑：长度以米、重量以克为基准做系数换算；温度按线性公式。 */
object ConverterLogic {
    enum class Category { LENGTH, TEMPERATURE, WEIGHT }
    enum class LengthUnit(val toMeters: Double) { MM(0.001), CM(0.01), M(1.0), KM(1000.0) }
    enum class TempUnit { C, F }
    enum class WeightUnit(val toGrams: Double) { G(1.0), KG(1000.0), T(1_000_000.0) }

    fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double =
        value * from.toMeters / to.toMeters

    fun convertWeight(value: Double, from: WeightUnit, to: WeightUnit): Double =
        value * from.toGrams / to.toGrams

    fun convertTemp(value: Double, from: TempUnit, to: TempUnit): Double = when (from to to) {
        TempUnit.C to TempUnit.F -> value * 9 / 5 + 32
        TempUnit.F to TempUnit.C -> (value - 32) * 5 / 9
        else -> value
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`./gradlew.bat :app:testDebugUnitTest --tests "com.kotlin.learn.data.ConverterLogicTest"`
预期：5 个测试全部 PASS

- [ ] **步骤 5：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/data/logic/ConverterLogic.kt app/src/test/java/com/kotlin/learn/data/ConverterLogicTest.kt
git commit -m "feat: 单位换算纯逻辑（TDD）"
```

---

### 任务 3：计算器与换算器 UI + 目录注册

**文件：**
- 创建：`app/src/main/java/com/kotlin/learn/ui/tools/CalculatorScreen.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/tools/ConverterScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`（items 追加 2 项）
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（when 补 2 分支）

- [ ] **步骤 1：DemoCatalog.items 追加（animation 之后，顺序 calculator/converter）**

```kotlin
        DemoItem(
            id = "calculator",
            title = "简易计算器",
            emoji = "🧮",
            description = "四则运算，逐步计算，真实可用",
            lessonIds = listOf("compose-13"),
            explanation = "计算器没有\"输入框\"：每个按键都是一次状态事件，" +
                "display 是状态机的当前值。核心逻辑抽成了纯函数 CalculatorLogic，" +
                "这正是\"UI 只是状态的一个函数\"的工程化写法。",
        ),
        DemoItem(
            id = "converter",
            title = "单位换算器",
            emoji = "📏",
            description = "长度/温度/重量，输入即换算",
            lessonIds = listOf("compose-03"),
            explanation = "输入一个数字，结果实时变化——因为换算结果每次重组时" +
                "都由 ConverterLogic 从（数值, 源单位, 目标单位）三元组重新算出，没有中间存储。",
        ),
```

- [ ] **步骤 2：创建 CalculatorScreen.kt（ui/tools 包）**

```kotlin
package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.logic.CalculatorLogic
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.Violet

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var state by remember { mutableStateOf(CalculatorLogic.initial()) }

    Column(modifier = modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.error ?: state.display,
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Monospace),
                color = if (state.error != null) MaterialTheme.colorScheme.error else Indigo,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }
        val rows = listOf(
            listOf("C", "±", "÷", "7", "8", "9", "×", "4", "5", "6"),
            listOf("−", "1", "2", "3", "+", "0", ".", "="),
        )
        // 布局：4 列网格，逐行铺
        val grid = listOf(
            listOf("C", "±", "÷", "7"),
            listOf("8", "9", "×", "4"),
            listOf("5", "6", "−", "1"),
            listOf("2", "3", "+", "0"),
            listOf(".", "="),
        )
        grid.forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowKeys.forEach { key ->
                    Card(
                        onClick = { state = onKey(state, key) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).aspectRatio(1.4f),
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (key in listOf("÷", "×", "−", "+", "=")) Violet else Indigo,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun onKey(state: com.kotlin.learn.data.logic.CalculatorLogic.State, key: String) =
    when (key) {
        "C" -> CalculatorLogic.onClear(state)
        "±" -> CalculatorLogic.onSign(state)
        "÷" -> CalculatorLogic.onOperator(state, '÷')
        "×" -> CalculatorLogic.onOperator(state, '×')
        "−" -> CalculatorLogic.onOperator(state, '-')
        "+" -> CalculatorLogic.onOperator(state, '+')
        "=" -> CalculatorLogic.onEquals(state)
        else -> CalculatorLogic.onDigit(state, key.first())
    }
```

注意：上面 `rows` 变量是草稿残留，**实现时不要包含**（以 `grid` 为准）；`import com.kotlin.learn.data.logic.CalculatorLogic` 用 import 形式而非全限定（`onKey` 里 `com.kotlin.learn.data.logic.CalculatorLogic.State` 全限定可保留或改 import，统一用 import）。`aspectRatio` 需导入 `androidx.compose.foundation.layout.aspectRatio`。数字卡文字建议居中显示（Text 外层 Card 内容对齐可加 `Modifier.padding` + `Text` 默认 start 即可，视觉微调留给实现者，报告说明）。

- [ ] **步骤 3：创建 ConverterScreen.kt**

```kotlin
package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.kotlin.learn.data.logic.ConverterLogic
import com.kotlin.learn.ui.theme.Indigo

@Composable
fun ConverterScreen(modifier: Modifier = Modifier) {
    var category by remember { mutableStateOf(ConverterLogic.Category.LENGTH) }
    var input by remember { mutableStateOf("1") }
    val value = input.toDoubleOrNull()

    Column(modifier = modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ConverterLogic.Category.entries.forEach { c ->
                FilterChip(
                    selected = category == c,
                    onClick = { category = c },
                    label = {
                        Text(
                            when (c) {
                                ConverterLogic.Category.LENGTH -> "长度"
                                ConverterLogic.Category.TEMPERATURE -> "温度"
                                ConverterLogic.Category.WEIGHT -> "重量"
                            }
                        )
                    },
                )
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("输入数值") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (value == null) {
            Text("请输入有效数字", style = MaterialTheme.typography.bodySmall)
        } else {
            when (category) {
                ConverterLogic.Category.LENGTH -> {
                    ConverterLogic.LengthUnit.entries.forEach { from ->
                        ConverterLogic.LengthUnit.entries.forEach { to ->
                            if (from != to) {
                                ConversionRow(
                                    label = "${from.name} → ${to.name}",
                                    result = ConverterLogic.convertLength(value, from, to).toString(),
                                )
                            }
                        }
                    }
                }
                ConverterLogic.Category.TEMPERATURE -> {
                    ConversionRow("℃ → ℉", ConverterLogic.convertTemp(value, ConverterLogic.TempUnit.C, ConverterLogic.TempUnit.F).toString())
                    ConversionRow("℉ → ℃", ConverterLogic.convertTemp(value, ConverterLogic.TempUnit.F, ConverterLogic.TempUnit.C).toString())
                }
                ConverterLogic.Category.WEIGHT -> {
                    ConverterLogic.WeightUnit.entries.forEach { from ->
                        ConverterLogic.WeightUnit.entries.forEach { to ->
                            if (from != to) {
                                ConversionRow(
                                    label = "${from.name} → ${to.name}",
                                    result = ConverterLogic.convertWeight(value, from, to).toString(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversionRow(label: String, result: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(result, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace), color = Indigo)
    }
}
```

- [ ] **步骤 4：AppNav when 补分支**

`when (item?.id)` 增加：

```kotlin
        "calculator" -> CalculatorScreen()
        "converter" -> ConverterScreen()
```

导入两个 Screen。

- [ ] **步骤 5：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（预期 67/67 = 57 + 10 + 5，以实际为准）。

- [ ] **步骤 6：Commit**

```bash
git add app/src/main/java/com/kotlin/learn/ui app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt
git commit -m "feat: 简易计算器与单位换算器工具"
```

---

### 任务 4：倒计时器 + 音乐播放器 + 音频资产

**文件：**
- 创建：`scripts/generate_melody.py`
- 创建：`app/src/main/res/raw/demo_melody.wav`（由脚本生成）
- 创建：`app/src/main/java/com/kotlin/learn/ui/tools/TimerScreen.kt`
- 创建：`app/src/main/java/com/kotlin/learn/ui/tools/MusicPlayerScreen.kt`
- 修改：`app/src/main/java/com/kotlin/learn/ui/demos/DemoCatalog.kt`（items 追加 2 项）
- 修改：`app/src/main/java/com/kotlin/learn/ui/navigation/AppNav.kt`（when 补 2 分支）

- [ ] **步骤 1：编写并运行音频生成脚本**

`scripts/generate_melody.py`（C 大调八音盒旋律，22050Hz/16bit 单声道，正弦波 + 指数衰减包络）：

```python
"""生成 Demo 乐园音乐播放器用的八音盒旋律 WAV（可复现，无外部依赖）。"""
import math
import struct
import wave

SR = 22050  # 采样率
NOTE = 0.35  # 每个音的秒数
# C 大调旋律（音名, 拍数）：小星星前两句
MELODY = [
    ("C5", 1), ("C5", 1), ("G5", 1), ("G5", 1), ("A5", 1), ("A5", 1), ("G5", 2),
    ("F5", 1), ("F5", 1), ("E5", 1), ("E5", 1), ("D5", 1), ("D5", 1), ("C5", 2),
]
FREQ = {"C5": 523.25, "D5": 587.33, "E5": 659.25, "F5": 698.46, "G5": 783.99, "A5": 880.0}

samples = []
for name, beats in MELODY:
    dur = NOTE * beats
    f = FREQ[name]
    n = int(SR * dur)
    for i in range(n):
        t = i / SR
        env = math.exp(-3.0 * t / dur)  # 指数衰减包络，八音盒音色
        if t > dur - 0.01:              # 音符间留 10ms 静音防爆音
            env = 0.0
        samples.append(int(32767 * 0.5 * env * math.sin(2 * math.pi * f * t)))

with wave.open("app/src/main/res/raw/demo_melody.wav", "w") as w:
    w.setnchannels(1)
    w.setsampwidth(2)
    w.setframerate(SR)
    w.writeframes(b"".join(struct.pack("<h", s) for s in samples))
print(f"生成完成：{len(samples)} 采样，约 {len(samples)/SR:.1f} 秒")
```

运行前先 `mkdir -p app/src/main/res/raw`；运行：`python scripts/generate_melody.py`（若 `python` 不可用改试 `py`）。生成的 wav 入库。

- [ ] **步骤 2：DemoCatalog.items 追加 2 项（calculator/converter 之后）**

```kotlin
        DemoItem(
            id = "timer",
            title = "倒计时器",
            emoji = "⏱",
            description = "分秒设定、暂停续走，协程驱动",
            lessonIds = listOf("adv-02", "adv-04"),
            explanation = "倒数不是定时器轮询：每剩 1 秒由 LaunchedEffect + delay(1000) 挂起等待，" +
                "暂停只是取消该协程、继续是重启——adv-02/04 学的挂起语义在这里真实工作。",
        ),
        DemoItem(
            id = "music",
            title = "音乐播放器",
            emoji = "🎵",
            description = "MediaPlayer 播放内置旋律，进度可拖",
            lessonIds = listOf("compose-04"),
            explanation = "播放/暂停是状态，进度条是状态的投影；离开页面时" +
                "DisposableEffect 释放 MediaPlayer——compose-01/03 学的生命周期语义在真实资源上的应用。",
        ),
```

- [ ] **步骤 3：创建 TimerScreen.kt**

```kotlin
package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.Violet

@Composable
fun TimerScreen(modifier: Modifier = Modifier) {
    var totalSeconds by remember { mutableIntStateOf(60) }
    var remaining by remember { mutableIntStateOf(60) }
    var running by remember { mutableStateOf(false) }

    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            kotlinx.coroutines.delay(1000)
            remaining -= 1
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { if (totalSeconds == 0) 0f else remaining.toFloat() / totalSeconds },
                modifier = Modifier.size(180.dp),
                color = Violet,
            )
            Text(
                "%02d:%02d".format(remaining / 60, remaining % 60),
                style = MaterialTheme.typography.displayMedium,
                color = Indigo,
            )
        }
        if (!running && remaining == 0) {
            Text("⏰ 时间到！", style = MaterialTheme.typography.titleMedium)
        }
        if (!running) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = {
                    if (remaining > 0) { totalSeconds = remaining; running = true } 
                }, enabled = remaining > 0) { Text(if (remaining == 0) "再来一次" else "开始") }
                OutlinedButton(onClick = {
                    remaining = (remaining + 30).coerceAtMost(3599)
                    totalSeconds = remaining
                }) { Text("+30 秒") }
                OutlinedButton(onClick = { remaining = 60; totalSeconds = 60 }) { Text("重置 1 分") }
            }
        } else {
            Button(onClick = { running = false }) { Text("暂停") }
        }
        Text("剩余 $remaining 秒 · 协程每秒挂起一次", style = MaterialTheme.typography.bodySmall)
    }
}
```

实现说明：`remaining == 0` 后再点"再来一次"逻辑较绕——实现者可简化为：开始前允许 `+30 秒` 调总时长，"开始"置 running=true；归零自动停止（running=false 由 `LaunchedEffect` 中 `remaining -= 1` 后 `if (remaining == 0) running = false` 实现，此行更清晰，**采用此版**：把归零自动停写进 LaunchedEffect，按钮区只有"开始/暂停（同一按钮切换文案）"+"+30 秒"+"重置"）。以语义清晰、可编译为准，报告中说明最终交互形态。

- [ ] **步骤 4：创建 MusicPlayerScreen.kt**

```kotlin
package com.kotlin.learn.ui.tools

import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlin.learn.R
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.Violet

@Composable
fun MusicPlayerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var position by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        onDispose { player?.release() }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(16.dp),
    ) {
        Text("🎵 八音盒旋律（内置演示音频）", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            if (player == null) {
                MediaPlayer.create(context, R.raw.demo_melody)?.let { mp ->
                    player = mp
                    duration = mp.duration
                    mp.setOnCompletionListener {
                        isPlaying = false
                        position = 0
                    }
                    mp.start()
                    isPlaying = true
                }
            } else {
                player?.let { mp ->
                    if (mp.isPlaying) { mp.pause(); isPlaying = false }
                    else { mp.start(); isPlaying = true }
                }
            }
        }) { Text(if (isPlaying) "⏸ 暂停" else "▶ 播放") }
        Slider(
            value = if (duration > 0) position.toFloat() / duration else 0f,
            onValueChange = { fraction ->
                position = (fraction * duration).toInt()
                player?.seekTo(position)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = duration > 0,
        )
        Text(
            "%02d:%02d / %02d:%02d".format(position / 1000 / 60, position / 1000 % 60, duration / 1000 / 60, duration / 1000 % 60),
            style = MaterialTheme.typography.bodySmall,
            color = Indigo,
        )
    }
}
```

进度轮询：在播放中每 200ms 更新 position——把上面 Slider 部分外再补：

```kotlin
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            player?.let { position = it.currentPosition }
            kotlinx.coroutines.delay(200)
        }
    }
```

- [ ] **步骤 5：AppNav when 补分支**

```kotlin
        "timer" -> TimerScreen()
        "music" -> MusicPlayerScreen()
```

导入两个 Screen。

- [ ] **步骤 6：验证**

`./gradlew.bat :app:assembleDebug` + `./gradlew.bat :app:testDebugUnitTest`（45+新增逻辑测试，全部 PASS；报告精确总数）。
真机目视（如可行）：四工具可玩、播放器有声音、倒计时归零提示。

- [ ] **步骤 7：Commit**

```bash
git add scripts app/src/main
git commit -m "feat: 倒计时器与音乐播放器工具（含可复现音频资产生成脚本）"
```

---

### 任务 5：收尾全量验证

**文件：** 无新改动（纯验证）

- [ ] **步骤 1：全量单测**：`./gradlew.bat :app:testDebugUnitTest --rerun`（全部 PASS，报告精确总数，预期 ≥70）
- [ ] **步骤 2：构建**：`./gradlew.bat :app:assembleDebug`（BUILD SUCCESSFUL）
- [ ] **步骤 3：静态自查**：DemoCatalog.items 恰 11 项且 id 唯一（原 7 + calculator/converter/timer/music）；AppNav when 覆盖全部 11 个 id；Manifest 仍零网络权限；`scripts/generate_melody.py` 入库且 res/raw/demo_melody.wav 存在

---

## 自检记录

- **规格覆盖度**：calculator 逐步运算 + 除零/±/连续等号（任务 1）✓；converter 三类换算（任务 2）✓；两工具 UI + 注册（任务 3）✓；timer/music UI + 音频资产脚本（任务 4）✓；纯函数 TDD（任务 1/2）✓；DemoCatalog 7→11（任务 3/4）✓；收尾（任务 5）✓。
- **占位符扫描**：无 TBD/TODO；步骤 3 的 `rows` 草稿残留已在步骤内明文标注"实现时不要包含"；timer 交互形态给出裁定（归零自动停写进 LaunchedEffect）。
- **类型一致性**：`CalculatorLogic.State/initial/onDigit/onOperator/onEquals/onSign/onClear` 与 `ConverterLogic.Category/LengthUnit/TempUnit/WeightUnit/convert*` 签名跨任务一致；DemoItem id 与 AppNav when 分支一致；`Routes.demo` 复用现有路由零改动。
