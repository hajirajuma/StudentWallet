package com.example.studentwallet.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.CategoryBudget
import com.example.studentwallet.ui.components.CircularProgressRing
import com.example.studentwallet.ui.components.CustomTextField
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextPrimary
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

@Composable
fun MonthlyBudgetScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit
) {
    val budgetLimit by viewModel.monthlyBudgetLimit.collectAsState()
    val totalExpenses = viewModel.getTotalExpenses()
    val budgetUsedPercent = ((totalExpenses / budgetLimit) * 100).toInt().coerceIn(0, 100)
    val categoryBudgets = viewModel.getCategoryBudgets()

    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var newBudgetText by remember { mutableStateOf("") }

    if (showSetBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { Text("Set Monthly Budget", fontWeight = FontWeight.Bold, color = NavyBlue) },
            text = {
                Column {
                    Text("Enter your new monthly budget limit (MK):", fontSize = 14.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    CustomTextField(
                        value = newBudgetText,
                        onValueChange = { newBudgetText = it },
                        label = "Budget Amount (MK)",
                        placeholder = "e.g. 100000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newLimit = newBudgetText.toDoubleOrNull() ?: budgetLimit
                    viewModel.setMonthlyBudget(newLimit)
                    showSetBudgetDialog = false
                }) {
                    Text("Save", color = OrangePrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    text = "Monthly Budget",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Circular Progress Section Card
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
                    CircularProgressRing(
                        progressPercent = budgetUsedPercent,
                        centerSubtitle = "used",
                        size = 190.dp,
                        strokeWidth = 22.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MK ${String.format("%,.0f", totalExpenses)} / ${String.format("%,.0f", budgetLimit)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "Set New Budget",
                        onClick = {
                            newBudgetText = String.format("%.0f", budgetLimit)
                            showSetBudgetDialog = true
                        }
                    )
                }
            }
        }

        // Budget Overview Header
        item {
            Text(
                text = "Budget Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )
        }

        // Category Budgets Breakdown List
        items(categoryBudgets) { catBudget ->
            CategoryBudgetRowItem(catBudget)
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CategoryBudgetRowItem(catBudget: CategoryBudget) {
    val percent = ((catBudget.spentAmount / catBudget.totalBudget) * 100).toInt().coerceIn(0, 100)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(catBudget.category.bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = catBudget.category.icon,
                            contentDescription = catBudget.category.title,
                            tint = catBudget.category.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = catBudget.category.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "MK ${String.format("%,.0f", catBudget.spentAmount)} / ${String.format("%,.0f", catBudget.totalBudget)}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { percent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = catBudget.category.color,
                trackColor = Color(0xFFF1F5F9)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MonthlyBudgetScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        MonthlyBudgetScreen(
            viewModel = vm,
            onBackClick = {}
        )
    }
}
