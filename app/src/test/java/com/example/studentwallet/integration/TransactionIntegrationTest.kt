package com.example.studentwallet.integration

import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.fakes.FakeFirebaseRepository
import com.example.studentwallet.viewmodel.WalletViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFirebaseRepository
    private lateinit var viewModel: WalletViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_integration_1")
        viewModel = WalletViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun integration_addIncomeAndExpense_recalculatesTotalsAndBalances() = runTest {
        viewModel.addTransaction(
            title = "Parent/Relatives",
            amount = 100000.0,
            type = TransactionType.INCOME,
            category = CategoryType.ALLOWANCE,
            date = "2025-10-01",
            note = "Monthly allowance"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addTransaction(
            title = "Food",
            amount = 20000.0,
            type = TransactionType.EXPENSE,
            category = CategoryType.FOOD,
            date = "2025-10-02",
            note = "Groceries"
        )
        viewModel.addTransaction(
            title = "Transport",
            amount = 10000.0,
            type = TransactionType.EXPENSE,
            category = CategoryType.TRANSPORT,
            date = "2025-10-03",
            note = "Bus fare"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.transactions.value.size)
        assertEquals(100000.0, viewModel.getTotalIncome(), 0.01)
        assertEquals(30000.0, viewModel.getTotalExpenses(), 0.01)
        assertEquals(70000.0, viewModel.getTotalBalance(), 0.01)
    }

    @Test
    fun integration_editTransaction_updatesRepositoryAndViewModelState() = runTest {
        viewModel.addTransaction("Food", 5000.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-02", "Cafeteria lunch")
        testDispatcher.scheduler.advanceUntilIdle()

        val txId = viewModel.transactions.value.first().id

        viewModel.updateTransaction(
            id = txId,
            amount = 7500.0,
            date = "2025-10-02",
            category = CategoryType.FOOD,
            note = "Updated cafeteria lunch"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTx = viewModel.transactions.value.first { it.id == txId }
        assertEquals(7500.0, updatedTx.amount, 0.01)
        assertEquals("Updated cafeteria lunch", updatedTx.note)
        assertEquals(7500.0, viewModel.getTotalExpenses(), 0.01)
    }

    @Test
    fun integration_deleteTransaction_removesFromRepositoryAndRecalculates() = runTest {
        viewModel.addTransaction("Food", 7500.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-02", "Lunch")
        testDispatcher.scheduler.advanceUntilIdle()

        val txId = viewModel.transactions.value.first().id
        assertEquals(1, viewModel.transactions.value.size)

        viewModel.deleteTransaction(txId)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.transactions.value.size)
        assertEquals(0.0, viewModel.getTotalExpenses(), 0.01)
    }
}
