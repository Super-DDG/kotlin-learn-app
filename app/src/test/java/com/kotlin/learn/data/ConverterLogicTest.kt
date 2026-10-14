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
        assertEquals(250.0, convertLength(2500.0, LengthUnit.MM, LengthUnit.CM), 1e-9)
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
        assertEquals(-5.0, convertWeight(-5.0, WeightUnit.KG, WeightUnit.KG), 1e-9)
    }
}
