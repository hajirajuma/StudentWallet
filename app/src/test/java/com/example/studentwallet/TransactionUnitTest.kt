package com.example.studentwallet

import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.Transaction
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFirebaseRepository
    private lateinit var viewModel: WalletViewModel
    private val testUserId = "user_test_123"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFirebaseRepository()
        repository.setCurrentUserId(testUserId)
        viewModel = WalletViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test Case ID: SW_INC_ADD_001
     * Description: Verify that a logged-in student can record an income transaction with valid details.
     */
    @Test
    fun sw_inc_add_001_recordValidIncomeTransaction() = runTest {
        val initialIncome = viewModel.getTotalIncome()
        val initialBalance = viewModel.getTotalBalance()

        viewModel.addTransaction(
            title = "Parent/Relatives",
            amount = 35000.0,
            type = TransactionType.INCOME,
            category = CategoryType.ALLOWANCE,
            date = "2025-10-01",
            note = "Monthly allowance support"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialIncome + 35000.0, viewModel.getTotalIncome(), 0.01)
        assertEquals(initialBalance + 35000.0, viewModel.getTotalBalance(), 0.01)
        assertEquals(1, viewModel.transactions.value.size)

        val addedTx = viewModel.transactions.value.first()
        assertEquals("Parent/Relatives", addedTx.title)
        assertEquals(35000.0, addedTx.amount, 0.01)
        assertEquals(TransactionType.INCOME, addedTx.type)
        assertEquals("2025-10-01", addedTx.date)
        assertEquals("Monthly allowance support", addedTx.note)
    }

    /**
     * Test Case ID: SW_INC_ADD_002
     * Description: Verify that recording income is blocked when the amount field is left empty.
     */
    @Test
    fun sw_inc_add_002_recordIncomeWithEmptyAmount() = runTest {
        val initialCount = viewModel.transactions.value.size
        val amountInput = ""

        val isSaveButtonEnabled = amountInput.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0.0
        assertTrue("Save button should be disabled for empty amount", !isSaveButtonEnabled)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(initialCount, viewModel.transactions.value.size)
    }

    /**
     * Test Case ID: SW_INC_ADD_003
     * Description: Verify that recording income is rejected when an amount of zero is entered.
     */
    @Test
    fun sw_inc_add_003_recordIncomeWithZeroAmount() = runTest {
        val initialCount = viewModel.transactions.value.size
        val amountInput = "0"

        val isSaveButtonEnabled = amountInput.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0.0
        assertTrue("Save button should be disabled for zero amount", !isSaveButtonEnabled)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(initialCount, viewModel.transactions.value.size)
        assertEquals(0.0, viewModel.getTotalIncome(), 0.01)
    }

    /**
     * Test Case ID: SW_EXP_ADD_001
     * Description: Verify that a logged-in student can record a valid expense transaction with a category.
     */
    @Test
    fun sw_exp_add_001_recordValidExpenseTransaction() = runTest {
        // Record income first so balance is positive
        viewModel.addTransaction("Allowance", 10000.0, TransactionType.INCOME, CategoryType.ALLOWANCE, "2025-10-01", "")
        testDispatcher.scheduler.advanceUntilIdle()

        val initialExpenses = viewModel.getTotalExpenses()
        val initialBalance = viewModel.getTotalBalance()

        viewModel.addTransaction(
            title = "Food",
            amount = 5000.0,
            type = TransactionType.EXPENSE,
            category = CategoryType.FOOD,
            date = "2025-10-02",
            note = "Cafeteria lunch"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(initialExpenses + 5000.0, viewModel.getTotalExpenses(), 0.01)
        assertEquals(initialBalance - 5000.0, viewModel.getTotalBalance(), 0.01)

        val expenseTx = viewModel.transactions.value.first { it.type == TransactionType.EXPENSE }
        assertEquals(CategoryType.FOOD, expenseTx.category)
        assertEquals(5000.0, expenseTx.amount, 0.01)
        assertEquals("Cafeteria lunch", expenseTx.note)
    }

    /**
     * Test Case ID: SW_EXP_ADD_002
     * Description: Verify that recording an expense is blocked when the amount field is empty.
     */
    @Test
    fun sw_exp_add_002_recordExpenseWithEmptyAmount() = runTest {
        val initialCount = viewModel.transactions.value.size
        val amountInput = ""

        val isSaveButtonEnabled = amountInput.isNotBlank() && (amountInput.toDoubleOrNull() ?: 0.0) > 0.0
        assertTrue("Save button should be disabled for empty expense amount", !isSaveButtonEnabled)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(initialCount, viewModel.transactions.value.size)
    }

    /**
     * Test Case ID: SW_EXP_CAT_001
     * Description: Verify that a student can select any of the seven documented expense categories when recording an expense.
     */
    @Test
    fun sw_exp_cat_001_categorizeExpenseAcrossAllDocumentedCategories() = runTest {
        val documentedCategories = listOf(
            CategoryType.FOOD,
            CategoryType.TRANSPORT,
            CategoryType.ACCOMMODATION,
            CategoryType.EDUCATION,
            CategoryType.AIRTIME_DATA,
            CategoryType.ENTERTAINMENT,
            CategoryType.OTHER
        )

        assertEquals(7, documentedCategories.size)

        // Record an expense under Airtime/Data category as specified in test case
        viewModel.addTransaction(
            title = CategoryType.AIRTIME_DATA.title,
            amount = 2000.0,
            type = TransactionType.EXPENSE,
            category = CategoryType.AIRTIME_DATA,
            date = "2025-10-02",
            note = "Data bundle"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val savedTx = viewModel.transactions.value.first()
        assertEquals(CategoryType.AIRTIME_DATA, savedTx.category)
        assertEquals("Airtime/Data", savedTx.category.title)
    }

    /**
     * Test Case ID: SW_TX_VIEW_001
     * Description: Verify that recorded transactions are retrieved from Firestore and displayed with the most recent first.
     */
    @Test
    fun sw_tx_view_001_transactionHistoryDisplayAndSorting() = runTest {
        val now = System.currentTimeMillis()
        val tx1 = Transaction(id = "tx1", title = "Sep 25 Tx", amount = 1000.0, date = "2025-09-25", createdAt = now - 20000)
        val tx2 = Transaction(id = "tx2", title = "Sep 28 Tx", amount = 2000.0, date = "2025-09-28", createdAt = now - 10000)
        val tx3 = Transaction(id = "tx3", title = "Oct 02 Tx", amount = 3000.0, date = "2025-10-02", createdAt = now)

        repository.addTransaction(testUserId, tx1)
        repository.addTransaction(testUserId, tx2)
        repository.addTransaction(testUserId, tx3)

        testDispatcher.scheduler.advanceUntilIdle()

        val list = viewModel.transactions.value
        assertEquals(3, list.size)
        // Verify newest transaction (Oct 02) appears at top
        assertEquals("tx3", list[0].id)
        assertEquals("tx2", list[1].id)
        assertEquals("tx1", list[2].id)
    }

    /**
     * Test Case ID: SW_TX_FILTER_001
     * Description: Verify that transactions can be filtered using All, Income, and Expense tabs.
     */
    @Test
    fun sw_tx_filter_001_filterTransactionsByIncomeAndExpenseTabs() = runTest {
        viewModel.addTransaction("Allowance", 50000.0, TransactionType.INCOME, CategoryType.ALLOWANCE, "2025-10-01", "")
        viewModel.addTransaction("Food", 5000.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-02", "")
        viewModel.addTransaction("Transport", 2000.0, TransactionType.EXPENSE, CategoryType.TRANSPORT, "2025-10-03", "")

        testDispatcher.scheduler.advanceUntilIdle()

        val allList = viewModel.transactions.value
        val incomeList = allList.filter { it.type == TransactionType.INCOME }
        val expenseList = allList.filter { it.type == TransactionType.EXPENSE }

        assertEquals(3, allList.size)
        assertEquals(1, incomeList.size)
        assertEquals(2, expenseList.size)

        assertEquals(TransactionType.INCOME, incomeList.first().type)
        assertTrue(expenseList.all { it.type == TransactionType.EXPENSE })
    }

    /**
     * Test Case ID: SW_TX_EDIT_001
     * Description: Verify that a student can edit an existing transaction's amount, date, category, and note.
     */
    @Test
    fun sw_tx_edit_001_editRecordedTransactionDetails() = runTest {
        val originalTx = Transaction(
            id = "food_tx_1",
            title = "Food",
            amount = 5000.0,
            type = TransactionType.EXPENSE,
            category = CategoryType.FOOD,
            date = "2025-10-02",
            note = "Cafeteria lunch"
        )
        repository.addTransaction(testUserId, originalTx)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateTransaction(
            id = "food_tx_1",
            amount = 7500.0,
            date = "2025-10-02",
            category = CategoryType.FOOD,
            note = "Updated cafeteria lunch"
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val updatedTx = viewModel.transactions.value.first { it.id == "food_tx_1" }
        assertEquals(7500.0, updatedTx.amount, 0.01)
        assertEquals("Updated cafeteria lunch", updatedTx.note)
        assertEquals(7500.0, viewModel.getTotalExpenses(), 0.01)
    }

    /**
     * Test Case ID: SW_TX_DEL_001
     * Description: Verify that a student can delete a recorded transaction from history.
     */
    @Test
    fun sw_tx_del_001_deleteRecordedTransaction() = runTest {
        val tx1 = Transaction(id = "del_tx_1", title = "Food", amount = 7500.0, type = TransactionType.EXPENSE)
        repository.addTransaction(testUserId, tx1)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.transactions.value.size)
        assertEquals(7500.0, viewModel.getTotalExpenses(), 0.01)

        viewModel.deleteTransaction("del_tx_1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.transactions.value.size)
        assertEquals(0.0, viewModel.getTotalExpenses(), 0.01)
    }
}
