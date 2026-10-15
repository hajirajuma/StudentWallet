package com.example.studentwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.ui.components.DonutChart
import com.example.studentwallet.ui.components.DonutSlice
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextPrimary
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

data class CategorySummaryItem(
    val category: CategoryType,
    val percentage: Int,
    val amount: Double
)

@Composable
fun SpendingSummaryScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit
) {
    val totalExpenses = viewModel.getTotalExpenses()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Categories, 1 = Day/Week/Month

    val summaryItems = listOf(
        CategorySummaryItem(CategoryType.FOOD, 42, 14700.0),
        CategorySummaryItem(CategoryType.TRANSPORT, 17, 5950.0),
        CategorySummaryItem(CategoryType.ACCOMMODATION, 14, 4900.0),
        CategorySummaryItem(CategoryType.EDUCATION, 11, 3850.0),
        CategorySummaryItem(CategoryType.ENTERTAINMENT, 9, 3150.0),
        CategorySummaryItem(CategoryType.OTHER, 7, 2450.0)
    )

    val slices = summaryItems.map {
        DonutSlice(
            category = it.category,
            amount = it.amount,
            percentage = it.percentage.toFloat(),
            color = it.category.color
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NavyBlue
                    )
                }
                Text(
                    text = "Spending Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Toggle Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 0) OrangePrimary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Categories",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) Color.White else TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedTab == 1) OrangePrimary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Day/Week/Month",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) Color.White else TextSecondary
                    )
                }
            }
        }

        // Donut Chart Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DonutChart(
                        slices = slices,
                        centerTitle = "Total Expenses",
                        centerValue = "MK ${String.format("%,.0f", totalExpenses)}",
                        size = 200.dp,
                        strokeWidth = 30.dp
                    )
                }
            }
        }

        // Breakdown List Header
        item {
            Text(
                text = "Category Breakdown",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }

        // Breakdown Items
        items(summaryItems) { item ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(item.category.color)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item.category.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.percentage}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "MK ${String.format("%,.0f", item.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SpendingSummaryScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        SpendingSummaryScreen(
            viewModel = vm,
            onBackClick = {}
        )
    }
}
