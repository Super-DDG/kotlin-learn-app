package com.kotlin.learn.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    // 套 DemoPageShell 后内容槽高度受限（weight(1f)），加 verticalScroll 防按键网格溢出
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.error ?: state.display,
                style = MaterialTheme.typography.displaySmall.copy(fontFamily = FontFamily.Monospace),
                color = if (state.error != null) MaterialTheme.colorScheme.error else Indigo,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
            )
        }
        // 布局：标准计算器 4 列网格（末行 0 键跨两列）
        val grid = listOf(
            listOf("C", "±", "÷", "×"),
            listOf("7", "8", "9", "−"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
        )
        grid.forEach { rowKeys ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowKeys.forEach { key ->
                    Card(
                        onClick = { state = onKey(state, key) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(52.dp),
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
            }
        }
        // 末行：0 跨两列，"." 与 "=" 各占一列
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Card(
                onClick = { state = onKey(state, "0") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(2f).height(52.dp),
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "0", style = MaterialTheme.typography.titleLarge, color = Indigo)
                }
            }
            listOf(".", "=").forEach { key ->
                Card(
                    onClick = { state = onKey(state, key) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (key == "=") Violet else Indigo,
                        )
                    }
                }
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
