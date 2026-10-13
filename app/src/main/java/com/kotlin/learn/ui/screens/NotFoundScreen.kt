package com.kotlin.learn.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotFoundScreen(modifier: Modifier = Modifier) {
    Text(
        text = "课程不存在或已被移除",
        modifier = modifier.fillMaxSize().padding(16.dp),
    )
}
