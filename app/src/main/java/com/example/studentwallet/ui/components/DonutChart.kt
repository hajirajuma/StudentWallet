package com.example.studentwallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.TextSecondary

data class DonutSlice(
    val category: CategoryType,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 28.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val total = slices.sumOf { it.amount }
            if (total == 0.0) {
                drawCircle(
                    color = Color(0xFFE5E7EB),
                    style = Stroke(width = strokeWidth.toPx())
                )
            } else {
                var startAngle = -90f
                slices.forEach { slice ->
                    val sweepAngle = (slice.amount / total * 360f).toFloat()
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerTitle,
                fontSize = 12.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = centerValue,
                fontSize = 18.sp,
                color = NavyBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CircularProgressRing(
    progressPercent: Int, // e.g. 70
    centerSubtitle: String, // "used" or "MK 70,000 / 100,000"
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 20.dp,
    ringColor: Color = OrangePrimary,
    bgColor: Color = Color(0xFFE2E8F0)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Background ring
            drawCircle(
                color = bgColor,
                style = Stroke(width = strokePx)
            )
            // Progress arc
            val sweep = (progressPercent / 100f) * 360f
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$progressPercent%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
            Text(
                text = centerSubtitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}
