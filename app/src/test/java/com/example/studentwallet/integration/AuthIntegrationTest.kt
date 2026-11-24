package com.example.studentwallet.integration

import com.example.studentwallet.fakes.FakeFirebaseRepository
import com.example.studentwallet.viewmodel.AuthUiState
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthIntegrationTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeFirebaseRepository
    private lateinit var viewModel: WalletViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFirebaseRepository()
        viewModel = WalletViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun integration_userRegistration_persistsProfile_andEstablishesSession() = runTest {
        var registrationDone = false

        viewModel.register(
            name = "Hajirah Juma",
            email = "hajirah@student.ac.mw",
            phone = "+265 991 234 567",
            pass = "SecurePass123"
        ) { success, _ ->
            registrationDone = success
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(registrationDone)
        assertTrue(viewModel.isLoggedIn.value)
        assertEquals("Hajirah Juma", viewModel.userProfile.value.name)
        assertEquals("hajirah@student.ac.mw", viewModel.userProfile.value.email)
        assertEquals("+265 991 234 567", viewModel.userProfile.value.phone)
    }

    @Test
    fun integration_userLogout_terminatesSession_andClearsProfile() = runTest {
        viewModel.register("Hajirah Juma", "hajirah@student.ac.mw", "+265 991 234 567", "SecurePass123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.isLoggedIn.value)

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.isLoggedIn.value)
        assertEquals("", viewModel.userProfile.value.uid)
        assertEquals(AuthUiState.Idle, viewModel.authUiState.value)
    }

    @Test
    fun integration_userLogin_resumesSession_andLoadsProfile() = runTest {
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.logout()

        var loginDone = false
        viewModel.login("hajirah@gmail.com", "password123") { success, _ ->
            loginDone = success
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(loginDone)
        assertTrue(viewModel.isLoggedIn.value)
        assertEquals("Hajirah Juma", viewModel.userProfile.value.name)
    }
}
