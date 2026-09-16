package com.foldmotion.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.foldmotion.app.hinge.FoldFxParams
import kotlin.math.max

@Composable
fun FoldFxOverlay(
    params: FoldFxParams,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                if (params.dim > 0f) {
                    drawRect(Color.Black.copy(alpha = params.dim))
                }
                if (params.blur > 0f) {
                    drawRect(Color(0xFFD7DEE8).copy(alpha = params.blur * 0.22f))
                }
                if (params.hingeShadow > 0f) {
                    val band = size.width * 0.14f
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = params.hingeShadow * 0.62f),
                                Color.Transparent,
                            ),
                            startX = size.width / 2f - band,
                            endX = size.width / 2f + band,
                        ),
                    )
                }
                if (params.vignette > 0f) {
                    val radius = max(size.width, size.height) * 0.74f
                    drawRect(
                        brush = Brush.radialGradient(
                            0.42f to Color.Transparent,
                            1f to Color.Black.copy(alpha = params.vignette * 0.80f),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = radius,
                        ),
                    )
                }
                if (params.blackout > 0f) {
                    drawRect(Color.Black.copy(alpha = params.blackout))
                }
            },
    )
}
