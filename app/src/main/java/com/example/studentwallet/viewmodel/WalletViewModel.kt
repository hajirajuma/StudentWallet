package com.example.studentwallet.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentwallet.data.CategoryBudget
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.FirebaseRepository
import com.example.studentwallet.data.Transaction
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.data.UserProfile
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class WalletViewModel(
    private val repository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.currentUser != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _showBalance = MutableStateFlow(true)
    val showBalance: StateFlow<Boolean> = _showBalance.asStateFlow()

    private val _monthlyBudgetLimit = MutableStateFlow(100000.0)
    val monthlyBudgetLimit: StateFlow<Double> = _monthlyBudgetLimit.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoadingTransactions = MutableStateFlow(false)
    val isLoadingTransactions: StateFlow<Boolean> = _isLoadingTransactions.asStateFlow()

    private var profileJob: Job? = null
    private var transactionsJob: Job? = null
    private var budgetJob: Job? = null

    init {
        repository.currentUser?.let { user ->
            attachUserListeners(user.uid)
        }
    }

    private fun attachUserListeners(uid: String) {
        profileJob?.cancel()
        transactionsJob?.cancel()
        budgetJob?.cancel()

        _isLoadingTransactions.value = true
        _monthlyBudgetLimit.value = 100000.0 // Reset to default state per user before loading

        profileJob = viewModelScope.launch {
            repository.getUserProfileFlow(uid).collect { profile ->
                if (profile != null) {
                    _userProfile.value = profile
                }
            }
        }

        transactionsJob = viewModelScope.launch {
            repository.getTransactionsFlow(uid).collect { list ->
                _transactions.value = list
                _isLoadingTransactions.value = false
            }
        }

        budgetJob = viewModelScope.launch {
            repository.getMonthlyBudgetFlow(uid).collect { limit ->
                _monthlyBudgetLimit.value = limit
            }
        }
    }

    private fun getReadableAuthErrorMessage(exception: Throwable): String {
        val msg = exception.message ?: ""
        return when {
            exception is FirebaseNetworkException ||
            msg.contains("network", ignoreCase = true) ||
            msg.contains("connection", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) ||
            msg.contains("unreachable", ignoreCase = true) -> {
                "No internet connection. Please check your network and try again."
            }
            exception is FirebaseAuthInvalidCredentialsException -> {
                "Invalid email address or password."
            }
            exception is FirebaseAuthInvalidUserException -> {
                "No account found with this email address."
            }
            exception is FirebaseAuthUserCollisionException -> {
                "An account with this email address already exists."
            }
            exception is FirebaseAuthWeakPasswordException -> {
                "Password is too weak. Please use at least 6 characters."
            }
            else -> {
                "Authentication failed. Please check your credentials and try again."
            }
        }
    }

    fun clearAuthUiState() {
        _authUiState.value = AuthUiState.Idle
    }

    fun toggleShowBalance() {
        _showBalance.value = !_showBalance.value
    }

    fun getTotalIncome(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    fun getTotalExpenses(): Double {
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    fun getTotalBalance(): Double {
        return getTotalIncome() - getTotalExpenses()
    }

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            val err = "Please enter a valid email address."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (trimmedPass.isEmpty()) {
            val err = "Please enter your password."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }

        _authUiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.login(trimmedEmail, trimmedPass)
            result.onSuccess { user ->
                _isLoggedIn.value = true
                attachUserListeners(user.uid)
                _authUiState.value = AuthUiState.Success("Login successful.")
                onResult(true, null)
            }.onFailure { exception ->
                val errorMessage = getReadableAuthErrorMessage(exception)
                _authUiState.value = AuthUiState.Error(errorMessage)
                onResult(false, errorMessage)
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String,
        pass: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val trimmedPass = pass.trim()

        if (trimmedName.isEmpty()) {
            val err = "Full name is required."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (trimmedEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            val err = "A valid email address is required."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (trimmedPhone.isEmpty()) {
            val err = "Phone number is required."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (trimmedPass.length < 6) {
            val err = "Password must be at least 6 characters."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }

        _authUiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val result = repository.register(trimmedName, trimmedEmail, trimmedPhone, trimmedPass)
            result.onSuccess { user ->
                _isLoggedIn.value = true
                attachUserListeners(user.uid)
                _authUiState.value = AuthUiState.Success("Registration successful.")
                onResult(true, null)
            }.onFailure { exception ->
                val errorMessage = getReadableAuthErrorMessage(exception)
                _authUiState.value = AuthUiState.Error(errorMessage)
                onResult(false, errorMessage)
            }
        }
    }

    fun logout() {
        profileJob?.cancel()
        transactionsJob?.cancel()
        budgetJob?.cancel()

        repository.logout()
        _isLoggedIn.value = false
        _userProfile.value = UserProfile()
        _transactions.value = emptyList()
        _monthlyBudgetLimit.value = 100000.0
        _authUiState.value = AuthUiState.Idle
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: CategoryType,
        date: String,
        note: String
    ) {
        val uid = repository.currentUser?.uid ?: return
        val newTx = Transaction(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) category.title else title,
            amount = amount,
            type = type,
            category = category,
            date = if (date.isBlank()) "Today" else date,
            note = note,
            createdAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.addTransaction(uid, newTx)
        }
    }

    fun updateTransaction(
        id: String,
        amount: Double,
        date: String,
        category: CategoryType,
        note: String
    ) {
        val uid = repository.currentUser?.uid ?: return
        val existingTx = _transactions.value.find { it.id == id } ?: return
        val updatedTx = existingTx.copy(
            title = category.title,
            amount = amount,
            category = category,
            date = if (date.isBlank()) existingTx.date else date,
            note = note
        )
        viewModelScope.launch {
            repository.updateTransaction(uid, updatedTx)
        }
    }

    fun deleteTransaction(id: String) {
        val uid = repository.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.deleteTransaction(uid, id)
        }
    }

    fun setMonthlyBudget(newLimit: Double) {
        if (newLimit <= 0) return
        val uid = repository.currentUser?.uid ?: return
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        _monthlyBudgetLimit.value = newLimit
        viewModelScope.launch {
            repository.setMonthlyBudget(uid, currentMonth, currentYear, newLimit)
        }
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        val uid = repository.currentUser?.uid ?: return
        val newName = name.ifBlank { _userProfile.value.name }
        val newEmail = email.ifBlank { _userProfile.value.email }
        val newPhone = phone.ifBlank { _userProfile.value.phone }

        viewModelScope.launch {
            repository.updateUserProfile(uid, newName, newEmail, newPhone)
        }
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

        val approvedCategories = listOf(
            CategoryType.FOOD,
            CategoryType.TRANSPORT,
            CategoryType.ACCOMMODATION,
            CategoryType.EDUCATION,
            CategoryType.AIRTIME_DATA,
            CategoryType.ENTERTAINMENT,
            CategoryType.OTHER
        )

        return approvedCategories.map { cat ->
            val spent = totalSpentMap[cat] ?: 0.0
            val limit = predefinedLimits[cat] ?: 10000.0
            CategoryBudget(cat, spent, limit)
        }
    }
}
