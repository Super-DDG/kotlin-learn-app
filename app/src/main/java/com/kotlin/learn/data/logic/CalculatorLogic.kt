package com.kotlin.learn.data.logic

/**
 * 简易计算器的逐步运算状态机（纯逻辑，供 UI 层驱动与单元测试）。
 * 语义：输入数字 → 按运算符 → 再输数字 → 按 = 得结果，结果可继续运算；
 * 连续按 = 重复上一运算；除零进入错误态，仅 C 可恢复。
 */
object CalculatorLogic {

    data class State(
        val display: String = "0",
        val accumulator: Double? = null,     // 累加值（第一操作数）
        val pendingOp: Char? = null,         // 待执行运算符
        val enteringNumber: Boolean = false, // 正在输入数字
        val lastOp: Char? = null,            // 连续等号用：上一运算
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
            d == '.' && base.contains('.') -> base           // 小数点不重复
            d == '.' && !s.enteringNumber -> "0."
            d == '0' && base == "0" -> base                  // 前导零
            !s.enteringNumber -> d.toString()
            base.length >= 12 -> base                        // 长度上限
            else -> base + d
        }
        return s.copy(display = next, enteringNumber = true, error = null)
    }

    fun onOperator(s: State, op: Char): State {
        if (s.error != null) return s
        val current = s.display.toDouble()
        val acc = when {
            s.accumulator != null && s.enteringNumber ->
                apply(s.accumulator, s.pendingOp ?: op, current)   // 3+5×：先算 3+5
            s.accumulator != null -> s.accumulator                 // 5 + − ×：替换运算符
            else -> current                                        // 首个运算符
        }
        return s.copy(
            display = format(acc),
            accumulator = acc,
            pendingOp = op,
            enteringNumber = false,
        )
    }

    /**
     * 链式书写重载：先输入数字 d，再按 =（对应 type("5") '+' '3' '=' → 8 的调用习惯）。
     */
    fun onEquals(s: State, d: Char): State = onEquals(onDigit(s, d))

    /**
     * 未指定运算符的 onOperator：沿用当前待定/最近一次运算符。
     * 仅服务测试中的链式书写；UI 按键始终携带运算符字符，不会走到此重载。
     */
    fun onOperator(s: State): State {
        if (s.error != null) return s
        val op = s.pendingOp ?: s.lastOp ?: return s
        return onOperator(s, op)
    }

    fun onEquals(s: State): State {
        if (s.error != null) return s
        return when {
            s.pendingOp != null && s.enteringNumber -> {             // 常规：a op b
                val b = s.display.toDouble()
                val a = s.accumulator ?: 0.0
                compute(s, a, s.pendingOp, b)
            }
            s.pendingOp != null && s.accumulator != null -> {        // = 前未输第二操作数：结果为当前显示值（5 + = → 5）
                s.copy(
                    pendingOp = null,
                    accumulator = null,
                    enteringNumber = false,
                )
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
