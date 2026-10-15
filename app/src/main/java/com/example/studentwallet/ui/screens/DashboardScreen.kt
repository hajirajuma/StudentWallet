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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.Transaction
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.ui.theme.IncomeGreen
import com.example.studentwallet.ui.theme.IncomeGreenBg
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.ExpenseRed
import com.example.studentwallet.ui.theme.ExpenseRedBg
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextPrimary
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

@Composable
fun DashboardScreen(
    viewModel: WalletViewModel,
    onViewAllTransactions: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onNotificationClick: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val showBalance by viewModel.showBalance.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgetLimit by viewModel.monthlyBudgetLimit.collectAsState()

    val totalIncome = viewModel.getTotalIncome()
    val totalExpenses = viewModel.getTotalExpenses()
    val totalBalance = viewModel.getTotalBalance()
    val budgetUsedPercent = ((totalExpenses / budgetLimit) * 100).toInt().coerceIn(0, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Greeting Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${userProfile.name.split(" ").firstOrNull() ?: "Student"} 👋",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyBlue
                    )
                    Text(
                        text = "Keep going! You're doing great.",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = NavyBlue
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        // Total Balance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Balance",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        IconButton(
                            onClick = { viewModel.toggleShowBalance() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (showBalance) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Balance",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (showBalance) "MK ${String.format("%,.0f", totalBalance)}" else "MK ****",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Total Income Badge Box
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreenBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Total Income", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = if (showBalance) "MK ${String.format("%,.0f", totalIncome)}" else "****",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Total Expenses Badge Box
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Total Expenses", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                                Text(
                                    text = if (showBalance) "MK ${String.format("%,.0f", totalExpenses)}" else "****",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Monthly Budget Progress Bar Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
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
                        Text(
                            text = "Monthly Budget",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyBlue
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF0E6))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$budgetUsedPercent% used",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { budgetUsedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = OrangePrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MK ${String.format("%,.0f", totalExpenses)} / ${String.format("%,.0f", budgetLimit)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Remaining: MK ${String.format("%,.0f", (budgetLimit - totalExpenses).coerceAtLeast(0.0))}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Recent Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
                Text(
                    text = "View all",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OrangePrimary,
                    modifier = Modifier.clickable { onViewAllTransactions() }
                )
            }
        }

        // Recent Transactions List
        items(transactions.take(4)) { transaction ->
            TransactionRowItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: Transaction,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(transaction.category.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.category.icon,
                        contentDescription = transaction.category.title,
                        tint = transaction.category.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = transaction.date,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "${if (transaction.type == TransactionType.INCOME) "+ " else "- "}MK ${String.format("%,.0f", transaction.amount)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        DashboardScreen(
            viewModel = vm,
            onViewAllTransactions = {},
            onTransactionClick = {},
            onNotificationClick = {}
        )
    }
}
