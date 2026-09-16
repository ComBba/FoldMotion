package com.foldmotion.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class PreviewApp(
    val label: String,
    val color: Color,
)

private val HOME_APPS = listOf(
    PreviewApp("전화", Color(0xFF3DDC84)),
    PreviewApp("메시지", Color(0xFF4FC3F7)),
    PreviewApp("브라우저", Color(0xFFFFB74D)),
    PreviewApp("갤러리", Color(0xFFCE93D8)),
    PreviewApp("캘린더", Color(0xFFEF5350)),
    PreviewApp("설정", Color(0xFF90A4AE)),
    PreviewApp("메모", Color(0xFFFFF176)),
    PreviewApp("음악", Color(0xFF7986CB)),
)

private val DOCK_APPS = HOME_APPS.take(4)

@Composable
fun FakeHomeGrid(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B2744), Color(0xFF0C1220)),
                ),
            )
            .padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 196.dp),
    ) {
        Text(
            text = "10:13",
            color = Color.White,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "FoldMotion Preview",
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 28.sp,
        )
        Text(
            text = "One UI를 흉내 낸 인앱 홈",
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(28.dp))
        AppRow(apps = HOME_APPS.take(4))
        Spacer(modifier = Modifier.height(20.dp))
        AppRow(apps = HOME_APPS.drop(4))
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            DOCK_APPS.forEach { app ->
                PreviewIcon(app = app, showLabel = false)
            }
        }
    }
}

@Composable
private fun AppRow(apps: List<PreviewApp>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        apps.forEach { app ->
            PreviewIcon(app = app, showLabel = true)
        }
    }
}

@Composable
private fun PreviewIcon(
    app: PreviewApp,
    showLabel: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(app.color),
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 12.sp,
            )
        }
    }
}
