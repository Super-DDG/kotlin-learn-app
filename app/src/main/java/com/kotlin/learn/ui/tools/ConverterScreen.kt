package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    // 套 DemoPageShell 后内容槽高度受限（weight(1f)），长度/重量类别下结果行较多，加 verticalScroll 防溢出
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
