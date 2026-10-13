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
        // 冷启动后把窗口背景换成应用常态背景（白）：启动图案只服务冷启动首帧，
        // 之后窗口背景必须回到常态色，否则无自绘背景的页面（课文/列表等）会透出品牌色
        window.setBackgroundDrawableResource(R.color.white)
        setContent {
            KotlinLearnTheme {
                val progressStore = remember { ProgressStore(applicationContext) }
                AppNav(progressStore = progressStore)
            }
        }
    }
}
