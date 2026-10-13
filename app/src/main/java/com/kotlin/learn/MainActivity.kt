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
        setContent {
            KotlinLearnTheme {
                val progressStore = remember { ProgressStore(applicationContext) }
                AppNav(progressStore = progressStore)
            }
        }
    }
}
