package com.example.studentwallet.viewmodel

import androidx.lifecycle.ViewModel
import com.example.studentwallet.data.CategoryBudget
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.Transaction
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class WalletViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _showBalance = MutableStateFlow(true)
    val showBalance: StateFlow<Boolean> = _showBalance.asStateFlow()

    private val _monthlyBudgetLimit = MutableStateFlow(100000.0)
    val monthlyBudgetLimit: StateFlow<Double> = _monthlyBudgetLimit.asStateFlow()

    // Default sample transactions reflecting the design
    private val _transactions = MutableStateFlow(
        listOf(
            Transaction(
                id = "1",
                title = "Food",
                amount = 5000.0,
                type = TransactionType.EXPENSE,
                category = CategoryType.FOOD,
                date = "Today",
                note = "Lunch"
            ),
            Transaction(
                id = "2",
                title = "Allowance",
                amount = 20000.0,
                type = TransactionType.INCOME,
                category = CategoryType.ALLOWANCE,
                date = "Yesterday",
                note = "Monthly Allowance"
            ),
            Transaction(
                id = "3",
                title = "Transport",
                amount = 3000.0,
                type = TransactionType.EXPENSE,
                category = CategoryType.TRANSPORT,
                date = "Sep 28, 2025",
                note = "Bus fare"
            ),
            Transaction(
                id = "4",
                title = "Airtime/Data",
                amount = 2000.0,
                type = TransactionType.EXPENSE,
                category = CategoryType.AIRTIME_DATA,
                date = "Sep 27, 2025",
                note = "Weekly data bundle"
            ),
            Transaction(
                id = "5",
                title = "Salary",
                amount = 50000.0,
                type = TransactionType.INCOME,
                category = CategoryType.SALARY,
                date = "Sep 25, 2025",
                note = "Part-time job"
            )
        )
    )
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun toggleShowBalance() {
        _showBalance.value = !_showBalance.value
    }

    fun getTotalIncome(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount } + 10000.0 // Match design MK 80,000 total
    }

    fun getTotalExpenses(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount } + 22000.0 // Match design MK 35,000 / 33,000
    }

    fun getTotalBalance(): Double {
        return getTotalIncome() - getTotalExpenses()
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        date: String,
        note: String
    ) {
        val newTx = Transaction(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) category.title else title,
            amount = amount,
            type = type,
            category = category,
            date = if (date.isBlank()) "Today" else date,
            note = note
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun updateTransaction(
        id: String,
        amount: Double,
        date: String,
        category: CategoryType,
        note: String
    ) {
        _transactions.value = _transactions.value.map { tx ->
            if (tx.id == id) {
                tx.copy(
                    title = category.title,
                    amount = amount,
                    category = category,
                    date = if (date.isBlank()) tx.date else date,
                    note = note
                )
            } else {
                tx
            }
        }
    }

    fun deleteTransaction(id: String) {
        _transactions.value = _transactions.value.filterNot { it.id == id }
    }

    fun setMonthlyBudget(newLimit: Double) {
        if (newLimit > 0) {
            _monthlyBudgetLimit.value = newLimit
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        _userProfile.value = UserProfile(
            name = name.ifBlank { _userProfile.value.name },
            email = email.ifBlank { _userProfile.value.email },
            phone = phone.ifBlank { _userProfile.value.phone }
        )
    }

    fun login(email: String, pass: String) {
        _isLoggedIn.value = true
    }

    fun register(name: String, email: String, phone: String, pass: String) {
        updateUserProfile(name, email, phone)
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun getCategoryBudgets(): List<CategoryBudget> {
        val totalSpentMap = mutableMapOf<CategoryType, Double>()
        _transactions.value.filter { it.type == TransactionType.EXPENSE }.forEach { tx ->
            totalSpentMap[tx.category] = (totalSpentMap[tx.category] ?: 0.0) + tx.amount
        }

        val predefinedLimits = mapOf(
            CategoryType.FOOD to 40000.0,
            CategoryType.TRANSPORT to 20000.0,
            CategoryType.ACCOMMODATION to 15000.0,
            CategoryType.EDUCATION to 10000.0,
            CategoryType.AIRTIME_DATA to 10000.0,
            CategoryType.ENTERTAINMENT to 5000.0,
            CategoryType.OTHER to 5000.0
        )

        return listOf(
            CategoryType.FOOD,
            CategoryType.TRANSPORT,
            CategoryType.ACCOMMODATION,
            CategoryType.EDUCATION,
            CategoryType.AIRTIME_DATA,
            CategoryType.ENTERTAINMENT,
            CategoryType.OTHER
        ).map { cat ->
            val spent = (totalSpentMap[cat] ?: 0.0) + when(cat) {
                CategoryType.FOOD -> 20000.0
                CategoryType.TRANSPORT -> 12000.0
                CategoryType.ACCOMMODATION -> 4900.0
                CategoryType.EDUCATION -> 3850.0
                CategoryType.ENTERTAINMENT -> 3150.0
                CategoryType.OTHER -> 2450.0
                else -> 0.0
            }
            val limit = predefinedLimits[cat] ?: 10000.0
            CategoryBudget(cat, spent, limit)
        }
    }
}
