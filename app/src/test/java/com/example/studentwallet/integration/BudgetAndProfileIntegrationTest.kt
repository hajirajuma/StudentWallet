package com.example.studentwallet.integration

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
class BudgetAndProfileIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFirebaseRepository
    private lateinit var viewModel: WalletViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_integration_2")
        viewModel = WalletViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun integration_setMonthlyBudget_emitsToStateFlow() = runTest {
        val success = viewModel.setMonthlyBudget(100000.0)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(success)
        assertEquals(100000.0, viewModel.monthlyBudgetLimit.value, 0.01)
        assertTrue(viewModel.budgetSaved.value)
    }

    @Test
    fun integration_updateUserProfile_persistsInRepository_andEmitsFlow() = runTest {
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "Password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateUserProfile("Hajirah Juma Updated", "hajirah@gmail.com", "+265 999 888 777")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Hajirah Juma Updated", viewModel.userProfile.value.name)
        assertEquals("+265 999 888 777", viewModel.userProfile.value.phone)
    }
}
