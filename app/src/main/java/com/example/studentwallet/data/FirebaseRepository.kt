package com.example.studentwallet.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String
    ): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw IllegalStateException("User creation failed.")
            val userProfile = UserProfile(
                uid = user.uid,
                name = name,
                email = email,
                phone = phone,
                createdAt = System.currentTimeMillis()
            )
            saveUserProfile(userProfile).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw IllegalStateException("Login failed.")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    private fun saveUserProfile(profile: UserProfile) =
        firestore.collection("users")
            .document(profile.uid)
            .set(
                mapOf(
                    "uid" to profile.uid,
                    "name" to profile.name,
                    "email" to profile.email,
                    "phone" to profile.phone,
                    "createdAt" to profile.createdAt
                )
            )

    suspend fun updateUserProfile(uid: String, name: String, email: String, phone: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).update(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "phone" to phone
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = UserProfile(
                    uid = snapshot.getString("uid") ?: uid,
                    name = snapshot.getString("name") ?: "",
                    email = snapshot.getString("email") ?: "",
                    phone = snapshot.getString("phone") ?: "",
                    createdAt = snapshot.getLong("createdAt") ?: 0L
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getTransactionsFlow(uid: String): Flow<List<Transaction>> = callbackFlow {
        val colRef = firestore.collection("users")
            .document(uid)
            .collection("transactions")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                val id = doc.id
                val title = doc.getString("title") ?: ""
                val amount = doc.getDouble("amount") ?: (doc.getLong("amount")?.toDouble() ?: 0.0)
                val typeStr = doc.getString("type") ?: "EXPENSE"
                val categoryStr = doc.getString("category") ?: "Other"
                val date = doc.getString("date") ?: ""
                val note = doc.getString("note") ?: ""
                val createdAt = doc.getLong("createdAt") ?: 0L

                val type = try {
                    TransactionType.valueOf(typeStr)
                } catch (e: Exception) {
                    TransactionType.EXPENSE
                }
                val category = CategoryType.fromTitle(categoryStr)

                Transaction(
                    id = id,
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    date = date,
                    note = note,
                    createdAt = createdAt
                )
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addTransaction(uid: String, tx: Transaction): Result<Unit> {
        return try {
            val docRef = if (tx.id.isNotBlank()) {
                firestore.collection("users").document(uid).collection("transactions").document(tx.id)
            } else {
                firestore.collection("users").document(uid).collection("transactions").document()
            }
            val txId = docRef.id
            val txData = mapOf(
                "id" to txId,
                "title" to tx.title,
                "amount" to tx.amount,
                "type" to tx.type.name,
                "category" to tx.category.title,
                "date" to tx.date,
                "note" to tx.note,
                "createdAt" to if (tx.createdAt > 0) tx.createdAt else System.currentTimeMillis()
            )
            docRef.set(txData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTransaction(uid: String, tx: Transaction): Result<Unit> {
        return try {
            val txData = mapOf(
                "id" to tx.id,
                "title" to tx.title,
                "amount" to tx.amount,
                "type" to tx.type.name,
                "category" to tx.category.title,
                "date" to tx.date,
                "note" to tx.note
            )
            firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .document(tx.id)
                .update(txData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(uid: String, transactionId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(uid)
                .collection("transactions")
                .document(transactionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMonthlyBudgetFlow(uid: String): Flow<Double> = callbackFlow {
        val colRef = firestore.collection("users")
            .document(uid)
            .collection("budgets")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(1)

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(100000.0)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                val amt = doc.getDouble("amount") ?: (doc.getLong("amount")?.toDouble() ?: 100000.0)
                trySend(amt)
            } else {
                trySend(100000.0)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun setMonthlyBudget(uid: String, month: Int, year: Int, amount: Double): Result<Unit> {
        return try {
            val budgetId = "${year}_$month"
            val budgetData = mapOf(
                "id" to budgetId,
                "amount" to amount,
                "month" to month,
                "year" to year,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(uid)
                .collection("budgets")
                .document(budgetId)
                .set(budgetData)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
