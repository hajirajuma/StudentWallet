package com.example.studentwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.ui.components.CustomTextField
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.components.SecondaryOutlinedButton
import com.example.studentwallet.ui.theme.ExpenseRed
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

@Composable
fun EditTransactionScreen(
    transactionId: String,
    viewModel: WalletViewModel,
    selectedCategory: CategoryType?,
    onOpenCategorySelector: () -> Unit,
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val currentTx = transactions.find { it.id == transactionId } ?: transactions.firstOrNull()

    var amountText by remember(currentTx) { mutableStateOf(currentTx?.amount?.let { String.format("%.0f", it) } ?: "5000") }
    var dateText by remember(currentTx) { mutableStateOf(currentTx?.date ?: "2025-09-28") }
    var noteText by remember(currentTx) { mutableStateOf(currentTx?.note ?: "Lunch") }
    var categoryState by remember(currentTx, selectedCategory) {
        mutableStateOf(selectedCategory ?: currentTx?.category ?: CategoryType.FOOD)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction", fontWeight = FontWeight.Bold, color = NavyBlue) },
            text = { Text("Are you sure you want to delete this transaction?", fontSize = 14.sp, color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    currentTx?.id?.let { viewModel.deleteTransaction(it) }
                    showDeleteConfirm = false
                    onDeleteSuccess()
                }) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
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
                text = "Edit Transaction",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Transaction Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount Field
        CustomTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount (MK)",
            placeholder = "5000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Field
        CustomTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = "Date",
            placeholder = "2025-09-28",
            trailingIcon = Icons.Default.CalendarToday
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenCategorySelector() }
        ) {
            CustomTextField(
                value = categoryState.title,
                onValueChange = {},
                label = "Category",
                placeholder = "Food",
                trailingIcon = Icons.Default.KeyboardArrowDown,
                readOnly = true,
                onClick = onOpenCategorySelector
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note Field
        CustomTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = "Note",
            placeholder = "Lunch"
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Buttons
        PrimaryButton(
            text = "Update",
            onClick = {
                currentTx?.id?.let { id ->
                    val amt = amountText.toDoubleOrNull() ?: currentTx.amount
                    viewModel.updateTransaction(
                        id = id,
                        amount = amt,
                        date = dateText,
                        category = categoryState,
                        note = noteText
                    )
                }
                onBackClick()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryOutlinedButton(
            text = "Cancel",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Delete Transaction", color = ExpenseRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditTransactionScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        EditTransactionScreen(
            transactionId = "1",
            viewModel = vm,
            selectedCategory = CategoryType.FOOD,
            onOpenCategorySelector = {},
            onBackClick = {},
            onDeleteSuccess = {}
        )
    }
}
