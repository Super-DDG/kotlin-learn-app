package com.kotlin.learn.ui.demos

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.kotlin.learn.ui.theme.Indigo
import com.kotlin.learn.ui.theme.IndigoContainer
import com.kotlin.learn.ui.theme.Violet

private enum class AnimKind(val label: String) { SCALE("缩放"), COLOR("颜色"), ALPHA("透明度") }

@Composable
fun AnimationDemo(
    item: DemoItem,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pressed by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(AnimKind.SCALE) }

    // 目标值三选一：只声明目标，中间帧由 animate*AsState 补齐
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.35f else 1f,
        animationSpec = tween(400),
        label = "scale",
    )
    val color by animateColorAsState(
        targetValue = if (pressed) Violet else Indigo,
        animationSpec = tween(400),
        label = "color",
    )
    val alpha by animateFloatAsState(
        targetValue = if (pressed) 0.3f else 1f,
        animationSpec = tween(400),
        label = "alpha",
    )

    DemoPageShell(
        item = item,
        onBack = onBack,
        onLessonClick = onLessonClick,
        modifier = modifier,
        content = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AnimKind.entries.forEach { k ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (kind == k) Violet else IndigoContainer)
                            .clickable { kind = k }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(
                            k.label,
                            color = if (kind == k) Color.White else Indigo,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 32.dp)
                    .size(140.dp)
                    .let { m ->
                        when (kind) {
                            AnimKind.SCALE -> m.graphicsLayer { scaleX = scale; scaleY = scale }
                            AnimKind.ALPHA -> m.graphicsLayer { this.alpha = alpha }
                            AnimKind.COLOR -> m
                        }
                    }
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (kind) {
                            AnimKind.COLOR -> color
                            else -> Indigo
                        }
                    )
                    .clickable { pressed = !pressed },
            ) {
                Text(
                    if (pressed) "松开我" else "点我",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Text(
                "当前动画：${kind.label} · 目标值 " + when (kind) {
                    AnimKind.SCALE -> if (pressed) "1.35" else "1.0"
                    AnimKind.COLOR -> if (pressed) "Violet" else "Indigo"
                    AnimKind.ALPHA -> if (pressed) "0.3" else "1.0"
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 20.dp),
            )
        }
        },
    )
}
