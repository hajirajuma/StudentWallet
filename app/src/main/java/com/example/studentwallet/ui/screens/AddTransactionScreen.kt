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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.ui.components.CustomTextField
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.components.SecondaryOutlinedButton
import com.example.studentwallet.ui.theme.ExpenseRed
import com.example.studentwallet.ui.theme.ExpenseRedBg
import com.example.studentwallet.ui.theme.IncomeGreen
import com.example.studentwallet.ui.theme.IncomeGreenBg
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.viewmodel.WalletViewModel

@Composable
fun AddTransactionScreen(
    transactionType: TransactionType,
    viewModel: WalletViewModel,
    selectedCategory: CategoryType?,
    onOpenCategorySelector: () -> Unit,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("Today") }
    var noteText by remember { mutableStateOf("") }

    val isExpense = transactionType == TransactionType.EXPENSE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
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
                text = if (isExpense) "Add Expense" else "Add Income",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circle Type Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(if (isExpense) ExpenseRedBg else IncomeGreenBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isExpense) ExpenseRed else IncomeGreen,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (isExpense) "Record Expense" else "Record Income",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Amount Field
        CustomTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = "Amount (MK)",
            placeholder = if (isExpense) "e.g. 5000" else "e.g. 20000",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date Field
        CustomTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = "Date",
            placeholder = "Select date",
            trailingIcon = Icons.Default.CalendarToday
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Selector (if Expense)
        if (isExpense) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCategorySelector() }
            ) {
                CustomTextField(
                    value = selectedCategory?.title ?: "",
                    onValueChange = {},
                    label = "Category",
                    placeholder = "Select category",
                    trailingIcon = Icons.Default.KeyboardArrowDown,
                    readOnly = true,
                    onClick = onOpenCategorySelector
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Note Field
        CustomTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = "Note (optional)",
            placeholder = if (isExpense) "e.g. Lunch" else "e.g. Allowance"
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Action Buttons
        PrimaryButton(
            text = "Save",
            enabled = amountText.isNotBlank(),
            onClick = {
                val amountVal = amountText.toDoubleOrNull() ?: 0.0
                val category = if (isExpense) (selectedCategory ?: CategoryType.OTHER) else CategoryType.ALLOWANCE
                viewModel.addTransaction(
                    title = if (isExpense) category.title else "Income",
                    amount = amountVal,
                    type = transactionType,
                    category = category,
                    date = dateText,
                    note = noteText
                )
                onSaveSuccess()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryOutlinedButton(
            text = "Cancel",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddExpenseScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        AddTransactionScreen(
            transactionType = TransactionType.EXPENSE,
            viewModel = vm,
            selectedCategory = CategoryType.FOOD,
            onOpenCategorySelector = {},
            onBackClick = {},
            onSaveSuccess = {}
        )
    }
}
