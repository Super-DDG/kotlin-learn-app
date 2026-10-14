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
