package com.example.studentwallet

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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetAndCalculationUnitTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFirebaseRepository
    private lateinit var viewModel: WalletViewModel
    private val testUserId = "user_calc_123"

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
     * Test Case ID: SW_DASH_CALC_001
     * Description: Verify that Total Income, Total Expenses, and Remaining Balance are calculated accurately according to project requirements.
     */
    @Test
    fun sw_dash_calc_001_financialBalanceCalculationAccuracy() = runTest {
        // Record Income of MK 100,000
        viewModel.addTransaction("Allowance", 100000.0, TransactionType.INCOME, CategoryType.ALLOWANCE, "2025-10-01", "Parent/Relatives")
        // Record Expense of MK 20,000 for Food
        viewModel.addTransaction("Food", 20000.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-01", "Food expense")
        // Record Expense of MK 10,000 for Transport
        viewModel.addTransaction("Transport", 10000.0, TransactionType.EXPENSE, CategoryType.TRANSPORT, "2025-10-01", "Transport expense")

        testDispatcher.scheduler.advanceUntilIdle()

        val totalIncome = viewModel.getTotalIncome()
        val totalExpenses = viewModel.getTotalExpenses()
        val totalBalance = viewModel.getTotalBalance()

        assertEquals(100000.0, totalIncome, 0.01)
        assertEquals(30000.0, totalExpenses, 0.01)
        assertEquals(70000.0, totalBalance, 0.01) // 100,000 - 30,000 = 70,000
    }

    /**
     * Test Case ID: SW_DASH_TOGGLE_001
     * Description: Verify that tapping the eye icon toggles total balance display between numerical amount and hidden mask.
     */
    @Test
    fun sw_dash_toggle_001_totalBalancePrivacyToggle() = runTest {
        // Initial state: showBalance is true
        assertTrue(viewModel.showBalance.value)

        // First tap: toggles to false (hidden mask "MK ****")
        viewModel.toggleShowBalance()
        assertFalse(viewModel.showBalance.value)

        // Second tap: restores numerical display
        viewModel.toggleShowBalance()
        assertTrue(viewModel.showBalance.value)
    }

    /**
     * Test Case ID: SW_BUDGET_SET_001
     * Description: Verify that a student can set and save a valid monthly budget limit.
     */
    @Test
    fun sw_budget_set_001_setValidMonthlyBudgetLimit() = runTest {
        val success = viewModel.setMonthlyBudget(100000.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(success)
        assertEquals(100000.0, viewModel.monthlyBudgetLimit.value, 0.01)
        assertTrue(viewModel.budgetSaved.value)
        assertEquals(null, viewModel.budgetError.value)
    }

    /**
     * Test Case ID: SW_BUDGET_SET_002
     * Description: Verify that setting a budget with a zero or negative value is rejected.
     */
    @Test
    fun sw_budget_set_002_setBudgetWithZeroOrNegativeValue() = runTest {
        // First set valid budget of MK 100,000
        viewModel.setMonthlyBudget(100000.0)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(100000.0, viewModel.monthlyBudgetLimit.value, 0.01)

        // Attempt to set invalid negative budget -5000
        val success = viewModel.setMonthlyBudget(-5000.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(success)
        assertNotNull(viewModel.budgetError.value)
        // Previous valid budget limit is preserved
        assertEquals(100000.0, viewModel.monthlyBudgetLimit.value, 0.01)
    }

    /**
     * Test Case ID: SW_BUDGET_PROG_001
     * Description: Verify budget progress ring display when total expenses equal or exceed the monthly budget limit.
     */
    @Test
    fun sw_budget_prog_001_monthlyBudgetProgressOverspendingIndicator() = runTest {
        // Set monthly budget to MK 50,000
        viewModel.setMonthlyBudget(50000.0)
        // Record expenses totaling MK 55,000
        viewModel.addTransaction("Food", 30000.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-01", "")
        viewModel.addTransaction("Rent", 25000.0, TransactionType.EXPENSE, CategoryType.ACCOMMODATION, "2025-10-02", "")

        testDispatcher.scheduler.advanceUntilIdle()

        val limit = viewModel.monthlyBudgetLimit.value
        val totalExpenses = viewModel.getTotalExpenses()
        val budgetUsedPercent = ((totalExpenses / limit) * 100).toInt().coerceIn(0, 100)
        val remainingBudget = (limit - totalExpenses).coerceAtLeast(0.0)

        assertEquals(50000.0, limit, 0.01)
        assertEquals(55000.0, totalExpenses, 0.01)
        assertEquals(100, budgetUsedPercent) // Progress capped at 100%
        assertEquals(0.0, remainingBudget, 0.01) // Remaining displays MK 0
    }

    /**
     * Test Case ID: SW_SUM_VIEW_001
     * Description: Verify that spending summary displays category breakdown percentages and total amounts correctly.
     */
    @Test
    fun sw_sum_view_001_viewCategorySpendingSummaryReport() = runTest {
        // Record expenses: Food MK 14,700, Transport MK 5,950, Accommodation MK 4,900
        viewModel.addTransaction("Food", 14700.0, TransactionType.EXPENSE, CategoryType.FOOD, "2025-10-01", "")
        viewModel.addTransaction("Bus", 5950.0, TransactionType.EXPENSE, CategoryType.TRANSPORT, "2025-10-01", "")
        viewModel.addTransaction("Rent", 4900.0, TransactionType.EXPENSE, CategoryType.ACCOMMODATION, "2025-10-01", "")

        testDispatcher.scheduler.advanceUntilIdle()

        val totalExpenses = viewModel.getTotalExpenses()
        assertEquals(25550.0, totalExpenses, 0.01)

        val foodSpent = viewModel.transactions.value.filter { it.category == CategoryType.FOOD }.sumOf { it.amount }
        val transportSpent = viewModel.transactions.value.filter { it.category == CategoryType.TRANSPORT }.sumOf { it.amount }
        val accSpent = viewModel.transactions.value.filter { it.category == CategoryType.ACCOMMODATION }.sumOf { it.amount }

        val foodPct = ((foodSpent / totalExpenses) * 100).toInt()
        val transportPct = ((transportSpent / totalExpenses) * 100).toInt()
        val accPct = ((accSpent / totalExpenses) * 100).toInt()

        assertEquals(14700.0, foodSpent, 0.01)
        assertEquals(5950.0, transportSpent, 0.01)
        assertEquals(4900.0, accSpent, 0.01)

        assertEquals(57, foodPct) // 14700 / 25550 = 57.5% -> 57%
        assertEquals(23, transportPct) // 5950 / 25550 = 23.2% -> 23%
        assertEquals(19, accPct) // 4900 / 25550 = 19.1% -> 19%
    }

    /**
     * Test Case ID: SW_PROF_EDIT_001
     * Description: Verify that a student can update their profile name and phone number.
     */
    @Test
    fun sw_prof_edit_001_editStudentProfileInformation() = runTest {
        // Register initial profile
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "Password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        // Update phone number
        viewModel.updateUserProfile("Hajirah Juma", "hajirah@gmail.com", "+265 999 888 777")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("+265 999 888 777", viewModel.userProfile.value.phone)
        assertEquals("Hajirah Juma", viewModel.userProfile.value.name)
    }

    /**
     * Test Case ID: SW_PROF_OUT_001
     * Description: Verify that logging out terminates the Firebase Auth session and redirects to Login screen.
     */
    @Test
    fun sw_prof_out_001_userLogoutAndSessionTermination() = runTest {
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "Password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.isLoggedIn.value)

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("", viewModel.userProfile.value.uid)
        assertTrue(viewModel.transactions.value.isEmpty())
    }
}
