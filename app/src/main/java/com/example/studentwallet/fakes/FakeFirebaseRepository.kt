package com.example.studentwallet.fakes

import com.example.studentwallet.data.AuthUser
import com.example.studentwallet.data.FirebaseRepository
import com.example.studentwallet.data.Transaction
import com.example.studentwallet.data.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

open class FakeFirebaseRepository : FirebaseRepository() {

    private val _currentUserId = MutableStateFlow<String?>(null)
    override val currentUserId: String?
        get() = _currentUserId.value

    private val registeredProfiles = mutableMapOf<String, UserProfile>()
    private val registeredPasswords = mutableMapOf<String, String>()

    private val userProfilesState = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    private val transactionsState = MutableStateFlow<Map<String, List<Transaction>>>(emptyMap())
    private val budgetState = MutableStateFlow<Map<String, Double>>(emptyMap())

    var authExceptionToThrow: Exception? = null
    var budgetExceptionToThrow: Exception? = null

    fun setCurrentUserId(uid: String?) {
        _currentUserId.value = uid
    }

    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String
    ): Result<AuthUser> {
        authExceptionToThrow?.let { return Result.failure(it) }

        if (registeredProfiles.containsKey(email.lowercase())) {
            return Result.failure(
                Exception("An account with this email address already exists.")
            )
        }

        val uid = "uid_${email.lowercase().hashCode()}"
        val profile = UserProfile(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            createdAt = System.currentTimeMillis()
        )

        registeredProfiles[email.lowercase()] = profile
        registeredPasswords[email.lowercase()] = pass

        val updatedProfiles = userProfilesState.value.toMutableMap()
        updatedProfiles[uid] = profile
        userProfilesState.value = updatedProfiles

        _currentUserId.value = uid
        return Result.success(AuthUser(uid, email))
    }

    override suspend fun login(email: String, pass: String): Result<AuthUser> {
        authExceptionToThrow?.let { return Result.failure(it) }

        val lowerEmail = email.lowercase()
        val profile = registeredProfiles[lowerEmail]
            ?: return Result.failure(
                Exception("No account found with this email address.")
            )

        val correctPassword = registeredPasswords[lowerEmail]
        if (correctPassword != pass) {
            return Result.failure(
                Exception("Incorrect password or invalid email format.")
            )
        }

        _currentUserId.value = profile.uid
        return Result.success(AuthUser(profile.uid, email))
    }

    override fun logout() {
        _currentUserId.value = null
    }

    override suspend fun updateUserProfile(
        uid: String,
        name: String,
        email: String,
        phone: String
    ): Result<Unit> {
        val currentProfile = userProfilesState.value[uid] ?: UserProfile(uid = uid)
        val updated = currentProfile.copy(
            name = name.ifBlank { currentProfile.name },
            email = email.ifBlank { currentProfile.email },
            phone = phone.ifBlank { currentProfile.phone }
        )

        val profilesMap = userProfilesState.value.toMutableMap()
        profilesMap[uid] = updated
        userProfilesState.value = profilesMap

        registeredProfiles[updated.email.lowercase()] = updated
        return Result.success(Unit)
    }

    override fun getUserProfileFlow(uid: String): Flow<UserProfile?> {
        return userProfilesState.map { it[uid] }
    }

    override fun getTransactionsFlow(uid: String): Flow<List<Transaction>> {
        return transactionsState.map { it[uid] ?: emptyList() }
    }

    override suspend fun addTransaction(uid: String, tx: Transaction): Result<Unit> {
        val currentList = transactionsState.value[uid] ?: emptyList()
        val updatedList = (listOf(tx) + currentList).sortedByDescending { it.createdAt }

        val map = transactionsState.value.toMutableMap()
        map[uid] = updatedList
        transactionsState.value = map
        return Result.success(Unit)
    }

    override suspend fun updateTransaction(uid: String, tx: Transaction): Result<Unit> {
        val currentList = transactionsState.value[uid] ?: emptyList()
        val updatedList = currentList.map { if (it.id == tx.id) tx else it }

        val map = transactionsState.value.toMutableMap()
        map[uid] = updatedList
        transactionsState.value = map
        return Result.success(Unit)
    }

    override suspend fun deleteTransaction(uid: String, transactionId: String): Result<Unit> {
        val currentList = transactionsState.value[uid] ?: emptyList()
        val updatedList = currentList.filter { it.id != transactionId }

        val map = transactionsState.value.toMutableMap()
        map[uid] = updatedList
        transactionsState.value = map
        return Result.success(Unit)
    }

    override fun getMonthlyBudgetFlow(uid: String): Flow<Double> {
        return budgetState.map { it[uid] ?: 0.0 }
    }

    override suspend fun setMonthlyBudget(
        uid: String,
        month: Int,
        year: Int,
        amount: Double
    ): Result<Unit> {
        budgetExceptionToThrow?.let { return Result.failure(it) }

        val map = budgetState.value.toMutableMap()
        map[uid] = amount
        budgetState.value = map
        return Result.success(Unit)
    }
}
