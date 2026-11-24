package com.example.studentwallet.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

open class FirebaseRepository {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    open val currentUser: FirebaseUser?
        get() = auth.currentUser

    open val currentUserId: String?
        get() = currentUser?.uid

    open suspend fun register(
        name: String,
        email: String,
        phone: String,
        pass: String
    ): Result<AuthUser> {
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
            Result.success(AuthUser(user.uid, user.email ?: email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun login(email: String, pass: String): Result<AuthUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val user = authResult.user ?: throw IllegalStateException("Login failed.")
            Result.success(AuthUser(user.uid, user.email ?: email))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open fun logout() {
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

    open suspend fun updateUserProfile(uid: String, name: String, email: String, phone: String): Result<Unit> {
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

    open fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
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

    open fun getTransactionsFlow(uid: String): Flow<List<Transaction>> = callbackFlow {
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

    open suspend fun addTransaction(uid: String, tx: Transaction): Result<Unit> {
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

    open suspend fun updateTransaction(uid: String, tx: Transaction): Result<Unit> {
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

    open suspend fun deleteTransaction(uid: String, transactionId: String): Result<Unit> {
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

    open fun getMonthlyBudgetFlow(uid: String): Flow<Double> = callbackFlow {
        val colRef = firestore.collection("users")
            .document(uid)
            .collection("budgets")

        val cal = Calendar.getInstance()
        val currentMonthId = "${cal.get(Calendar.YEAR)}_${cal.get(Calendar.MONTH) + 1}"

        val listener = colRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Keep the value already being shown instead of dropping the user's budget
                return@addSnapshotListener
            }
            val docs = snapshot?.documents ?: emptyList()
            // Prefer the document written for the current month, then fall back to the
            // most recently updated budget saved by this user.
            val doc = docs.firstOrNull { it.id == currentMonthId }
                ?: docs.maxByOrNull { it.getLong("updatedAt") ?: 0L }
            val amount = doc?.let { budgetDoc ->
                // `limit` is the field the Firestore rules validate. `amount` is only read as a
                // fallback so budgets saved before this field name existed still load.
                budgetDoc.getDouble("limit")
                    ?: budgetDoc.getLong("limit")?.toDouble()
                    ?: budgetDoc.getDouble("amount")
                    ?: budgetDoc.getLong("amount")?.toDouble()
            } ?: 0.0
            trySend(amount)
        }
        awaitClose { listener.remove() }
    }

    open suspend fun setMonthlyBudget(uid: String, month: Int, year: Int, amount: Double): Result<Unit> {
        val signedInUid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("You need to be signed in to save a budget."))
        if (signedInUid != uid) {
            return Result.failure(
                IllegalStateException("A budget can only be saved for the signed-in user.")
            )
        }
        if (month !in 1..12 || year <= 0 || amount < 0.0) {
            return Result.failure(IllegalArgumentException("Invalid budget values."))
        }
        return try {
            val budgetId = "${year}_$month"
            val budgetData = mapOf(
                "id" to budgetId,
                // The Firestore rules validate `limit`. `amount` is written alongside it so
                // existing budgets/tools that use the older field name stay consistent.
                "limit" to amount,
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
