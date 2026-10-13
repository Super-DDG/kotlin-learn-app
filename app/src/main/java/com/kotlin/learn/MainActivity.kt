package com.kotlin.learn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import com.kotlin.learn.data.progress.ProgressStore
import com.kotlin.learn.ui.navigation.AppNav
import com.kotlin.learn.ui.theme.KotlinLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // 冷启动后把窗口背景换成纯色，避免 windowBackground 的启动图案在导航转场淡入淡出时透出
        window.setBackgroundDrawableResource(R.color.brand_indigo)
        setContent {
            KotlinLearnTheme {
                val progressStore = remember { ProgressStore(applicationContext) }
                AppNav(progressStore = progressStore)
            }
        }
    }
}
