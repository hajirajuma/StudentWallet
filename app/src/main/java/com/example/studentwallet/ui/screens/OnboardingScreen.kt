package com.example.studentwallet.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextSecondary

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badge1: String,
    val badge2: String
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Take Control\nof Your Money",
            description = "Record your income, track your expenses, and build better money habits.",
            icon = Icons.Default.Payments,
            badge1 = "Income",
            badge2 = "Expenses"
        ),
        OnboardingPageData(
            title = "Stay on Track",
            description = "See your total income, expenses and remaining balance at a glance.",
            icon = Icons.Default.Analytics,
            badge1 = "Balance",
            badge2 = "Summary"
        ),
        OnboardingPageData(
            title = "Reach Your Goals",
            description = "Set a monthly budget and make smarter spending choices.",
            icon = Icons.Default.TrackChanges,
            badge1 = "Budget",
            badge2 = "Achieve"
        )
    )

    val page = pages[currentPage]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Indicator Dots
        Row(
            modifier = Modifier
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (index == currentPage) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(CircleShape)
                        .background(if (index == currentPage) OrangePrimary else Color(0xFFE5E7EB))
                )
            }
        }

        // Illustration Area
        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(12.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = OrangePrimary.copy(alpha = 0.1f), radius = size.width * 0.42f)
                drawCircle(color = NavyBlue.copy(alpha = 0.05f), radius = size.width * 0.32f)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(NavyBlue)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(page.badge1, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(OrangePrimary)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(page.badge2, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Text Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = page.title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = page.description,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Bottom Action Area
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinishOnboarding()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (currentPage < pages.size - 1) {
                TextButton(onClick = onFinishOnboarding) {
                    Text("Skip", color = TextSecondary, fontSize = 14.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    StudentWalletTheme {
        OnboardingScreen(onFinishOnboarding = {})
    }
}
