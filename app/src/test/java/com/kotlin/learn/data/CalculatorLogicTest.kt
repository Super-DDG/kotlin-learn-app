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
    fun `等号前未输第二操作数后连按等号保持当前值`() {
        var s = onEquals(onOperator(type("3"), '+'))   // 3 + = → 3（无可重复运算）
        s = onEquals(s)                                // 3
        s = onEquals(s)                                // 3
        assertEquals("3", s.display)
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
