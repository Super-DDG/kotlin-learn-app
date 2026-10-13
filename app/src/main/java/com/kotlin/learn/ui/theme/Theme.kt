package com.kotlin.learn.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = IndigoOn,
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = VioletContainer,
    onSecondaryContainer = VioletOn,
    tertiary = Success,
    background = GradEnd,
    surface = Color.White,
    surfaceVariant = IndigoContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBCC2FF),
    onPrimary = Color(0xFF1A1A4E),
    secondary = Color(0xFFE3B7FF),
    background = Color(0xFF121318),
    surface = Color(0xFF1C1D24),
)

@Composable
fun KotlinLearnTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 品牌色是教学 App 的视觉标识，禁用系统动态取色（Android 12+ 默认开启会覆盖品牌色）
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
