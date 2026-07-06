package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ShadowDarkBottom
import com.example.ui.theme.ShadowDarkTop
import com.example.ui.theme.ShadowLightBottom
import com.example.ui.theme.ShadowLightTop

fun Modifier.neumorphicFlat(
    cornerRadius: Dp = 20.dp,
    offset: Dp = 6.dp,
    blurRadius: Dp = 12.dp
) = composed {
    val isDark = isSystemInDarkTheme()
    val topShadow = if (isDark) ShadowDarkTop else ShadowLightTop
    val bottomShadow = if (isDark) ShadowDarkBottom else ShadowLightBottom
    
    this.drawBehind {
        val cornerRadiusPx = cornerRadius.toPx()
        val offsetPx = offset.toPx()
        val blurRadiusPx = blurRadius.toPx()
        
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = android.graphics.Color.TRANSPARENT
            
            // Bottom Right Dark Shadow
            frameworkPaint.setShadowLayer(
                blurRadiusPx,
                offsetPx,
                offsetPx,
                bottomShadow.toArgb()
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadiusPx,
                radiusY = cornerRadiusPx,
                paint = paint
            )
            
            // Top Left Light Shadow
            frameworkPaint.setShadowLayer(
                blurRadiusPx,
                -offsetPx,
                -offsetPx,
                topShadow.toArgb()
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = cornerRadiusPx,
                radiusY = cornerRadiusPx,
                paint = paint
            )
        }
    }
}
