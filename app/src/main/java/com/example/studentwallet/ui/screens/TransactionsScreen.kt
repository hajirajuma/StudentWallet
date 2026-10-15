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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.Transaction
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

enum class FilterType {
    ALL,
    INCOME,
    EXPENSE
}

@Composable
fun TransactionsScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit
) {
    val allTransactions by viewModel.transactions.collectAsState()
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }

    val filteredList = when (selectedFilter) {
        FilterType.ALL -> allTransactions
        FilterType.INCOME -> allTransactions.filter { it.type == TransactionType.INCOME }
        FilterType.EXPENSE -> allTransactions.filter { it.type == TransactionType.EXPENSE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
    ) {
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
                text = "Transactions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterTabItem(
                label = "All",
                isSelected = selectedFilter == FilterType.ALL,
                onClick = { selectedFilter = FilterType.ALL },
                modifier = Modifier.weight(1f)
            )
            FilterTabItem(
                label = "Income",
                isSelected = selectedFilter == FilterType.INCOME,
                onClick = { selectedFilter = FilterType.INCOME },
                modifier = Modifier.weight(1f)
            )
            FilterTabItem(
                label = "Expense",
                isSelected = selectedFilter == FilterType.EXPENSE,
                onClick = { selectedFilter = FilterType.EXPENSE },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction History List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No transactions found", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { transaction ->
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
    }
}

@Composable
private fun FilterTabItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) OrangePrimary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TransactionsScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        TransactionsScreen(
            viewModel = vm,
            onBackClick = {},
            onTransactionClick = {}
        )
    }
}
