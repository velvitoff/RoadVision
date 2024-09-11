package com.velvit.roadvision.ui.screens.camera_screen

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.velvit.roadvision.util.detector.BoundingBox
import java.text.DecimalFormat

@Composable
fun OverlayBoxes(
    boundingBoxes: List<BoundingBox>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        boundingBoxes.forEach { box ->
            val left = box.x1 * size.width
            val top = box.y1 * size.height
            val right = box.x2 * size.width
            val bottom = box.y2 * size.height

            // Draw bounding box
            drawRect(
                color = Color.Red, // Adjust color as needed
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 8f)
            )

            val decimalFormat = DecimalFormat("##.##")
            val formattedConfidence = decimalFormat.format(box.cnf)
            val drawableText = "${box.clsName} $formattedConfidence"
            val textPaint = TextStyle(
                color = Color.White,
                fontSize = 20.sp // Smaller text size
            )
            val textBackgroundPaint = TextStyle(
                color = Color.Black,
                fontSize = 20.sp // Smaller text size
            )

            // Calculate text width and height
            val textWidth = textPaint.fontSize.toPx() * drawableText.length
            val textHeight = textPaint.fontSize.toPx()

            // Draw text background (with padding)
            drawRect(
                color = textBackgroundPaint.color,
                topLeft = Offset(left, top - textHeight - BOUNDING_RECT_TEXT_PADDING), // Adjust to place text above the box
                size = Size(textWidth + 2 * BOUNDING_RECT_TEXT_PADDING, textHeight + 2 * BOUNDING_RECT_TEXT_PADDING)
            )

            // Draw text
            drawContext.canvas.nativeCanvas.drawText(
                drawableText,
                left + BOUNDING_RECT_TEXT_PADDING,
                top - BOUNDING_RECT_TEXT_PADDING,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = textPaint.fontSize.toPx()
                }
            )
        }
    }
}

private const val BOUNDING_RECT_TEXT_PADDING = 8