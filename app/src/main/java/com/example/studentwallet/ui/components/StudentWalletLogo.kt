package com.example.studentwallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary

@Composable
fun StudentWalletLogoIcon(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Main Orange Wallet Body
        drawRoundRect(
            color = OrangePrimary,
            topLeft = Offset(0f, h * 0.2f),
            size = Size(w, h * 0.75f),
            cornerRadius = CornerRadius(w * 0.2f, h * 0.2f)
        )

        // White Card / Document inside wallet
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.15f, h * 0.05f),
            size = Size(w * 0.7f, h * 0.35f),
            cornerRadius = CornerRadius(w * 0.1f, h * 0.1f)
        )

        // Navy Blue Strap / Clasp
        drawRoundRect(
            color = NavyBlue,
            topLeft = Offset(w * 0.6f, h * 0.42f),
            size = Size(w * 0.4f, h * 0.32f),
            cornerRadius = CornerRadius(w * 0.12f, h * 0.12f)
        )

        // White Button on Clasp
        drawCircle(
            color = Color.White,
            radius = w * 0.06f,
            center = Offset(w * 0.8f, h * 0.58f)
        )
    }
}

@Composable
fun StudentWalletHeaderLogo(
    iconSize: Dp = 44.dp,
    textColor: Color = NavyBlue,
    showTagline: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        StudentWalletLogoIcon(size = iconSize)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = if (textColor == Color.White) Color.White else NavyBlue, fontWeight = FontWeight.Bold)) {
                        append("Student")
                    }
                    withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold)) {
                        append("Wallet")
                    }
                },
                fontSize = (iconSize.value * 0.55).sp,
                lineHeight = (iconSize.value * 0.6).sp
            )
            if (showTagline) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Plan  •  Track  •  Achieve",
                    color = if (textColor == Color.White) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280),
                    fontSize = (iconSize.value * 0.26).sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
