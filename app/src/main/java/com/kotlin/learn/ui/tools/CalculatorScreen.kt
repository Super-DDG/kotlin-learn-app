package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (key in listOf("÷", "×", "−", "+", "=")) Violet else Indigo,
                            )
                        }
                    }
                }
                // 末行只有 2 个按键：补齐剩余 2 列，保持 4 列网格对齐
                repeat(4 - rowKeys.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private fun onKey(state: CalculatorLogic.State, key: String) =
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
