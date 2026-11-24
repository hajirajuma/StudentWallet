package com.example.studentwallet.viewmodel

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

    private val _isLoggedIn = MutableStateFlow(repository.currentUserId != null)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _showBalance = MutableStateFlow(true)
    val showBalance: StateFlow<Boolean> = _showBalance.asStateFlow()

    // 0.0 means the user has not set a budget yet
    private val _monthlyBudgetLimit = MutableStateFlow(0.0)
    val monthlyBudgetLimit: StateFlow<Double> = _monthlyBudgetLimit.asStateFlow()

    private val _budgetError = MutableStateFlow<String?>(null)
    val budgetError: StateFlow<String?> = _budgetError.asStateFlow()

    private val _budgetSaved = MutableStateFlow(false)
    val budgetSaved: StateFlow<Boolean> = _budgetSaved.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoadingTransactions = MutableStateFlow(false)
    val isLoadingTransactions: StateFlow<Boolean> = _isLoadingTransactions.asStateFlow()

    private var profileJob: Job? = null
    private var transactionsJob: Job? = null
    private var budgetJob: Job? = null

    init {
        repository.currentUserId?.let { uid ->
            attachUserListeners(uid)
        }
    }

    private fun attachUserListeners(uid: String) {
        profileJob?.cancel()
        transactionsJob?.cancel()
        budgetJob?.cancel()

        _isLoadingTransactions.value = true
        _monthlyBudgetLimit.value = 0.0 // Reset per user before loading their saved budget
        _budgetError.value = null
        _budgetSaved.value = false

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
            exception is FirebaseAuthInvalidCredentialsException ||
            msg.contains("Incorrect password", ignoreCase = true) -> {
                "Incorrect password or invalid email format."
            }
            exception is FirebaseAuthInvalidUserException ||
            msg.contains("No account found", ignoreCase = true) -> {
                "No account found with this email address."
            }
            exception is FirebaseAuthUserCollisionException ||
            msg.contains("already exists", ignoreCase = true) -> {
                "An account with this email address already exists."
            }
            exception is FirebaseAuthWeakPasswordException ||
            msg.contains("Password is too weak", ignoreCase = true) -> {
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

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        return emailRegex.matches(email)
    }

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedEmail = email.trim()
        val trimmedPass = pass.trim()

        if (trimmedEmail.isEmpty()) {
            val err = "Please enter your email address."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (!isValidEmail(trimmedEmail)) {
            val err = "Incorrect password or invalid email format."
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
            val err = "Please enter your full name."
            _authUiState.value = AuthUiState.Error(err)
            onResult(false, err)
            return
        }
        if (trimmedEmail.isEmpty() || !isValidEmail(trimmedEmail)) {
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
            val err = "Password must be at least 6 characters long."
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
        _monthlyBudgetLimit.value = 0.0
        _budgetError.value = null
        _budgetSaved.value = false
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
        val uid = repository.currentUserId ?: return
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
        val uid = repository.currentUserId ?: return
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
        val uid = repository.currentUserId ?: return
        viewModelScope.launch {
            repository.deleteTransaction(uid, id)
        }
    }

    private fun getReadableBudgetErrorMessage(exception: Throwable): String {
        val msg = exception.message ?: ""
        return when {
            exception is FirebaseNetworkException ||
            msg.contains("network", ignoreCase = true) ||
            msg.contains("unavailable", ignoreCase = true) ||
            msg.contains("timeout", ignoreCase = true) -> {
                "No internet connection. Please check your network and try again."
            }
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ||
            msg.contains("insufficient permissions", ignoreCase = true) ||
            msg.contains("permission denied", ignoreCase = true) -> {
                // Report what Firestore actually said instead of guessing a cause.
                "Could not save your budget: Firestore denied the write. " +
                    "Details: ${if (msg.isBlank()) "PERMISSION_DENIED" else msg}"
            }
            else -> "Could not save your budget: ${msg.ifBlank { "unknown error" }}"
        }
    }

    /**
     * Saves the monthly budget for the current month.
     * Returns false when the amount is invalid or the user is not signed in.
     * The write itself is confirmed asynchronously: [budgetSaved] turns true only after
     * Firestore accepts it, otherwise [budgetError] explains the failure.
     */
    fun setMonthlyBudget(newLimit: Double): Boolean {
        if (newLimit <= 0) {
            _budgetError.value = "Enter a budget amount greater than 0."
            return false
        }
        val uid = repository.currentUserId
        if (uid == null) {
            _budgetError.value = "You need to be signed in to save a budget."
            return false
        }
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        val previousLimit = _monthlyBudgetLimit.value
        _budgetError.value = null
        _budgetSaved.value = false
        _monthlyBudgetLimit.value = newLimit // Show the new budget immediately

        viewModelScope.launch {
            repository.setMonthlyBudget(uid, currentMonth, currentYear, newLimit)
                .onSuccess {
                    _budgetSaved.value = true
                }
                .onFailure { exception ->
                    _monthlyBudgetLimit.value = previousLimit
                    _budgetSaved.value = false
                    _budgetError.value = getReadableBudgetErrorMessage(exception)
                }
        }
        return true
    }

    fun clearBudgetError() {
        _budgetError.value = null
        _budgetSaved.value = false
    }

    fun updateUserProfile(name: String, email: String, phone: String) {
        val uid = repository.currentUserId ?: return
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
