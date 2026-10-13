package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 临时占位屏：任务 9~11 会用真实目的地替换。 */
@Composable
fun PlaceholderScreen(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier.fillMaxSize())
}
