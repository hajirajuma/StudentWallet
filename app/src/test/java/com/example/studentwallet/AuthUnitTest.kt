package com.example.studentwallet

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
class AuthUnitTest {

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

    /**
     * Test Case ID: SW_AUTH_REG_001
     * Description: Verify that a new student can register a personal account with valid registration details.
     */
    @Test
    fun sw_auth_reg_001_successfulUserRegistration() = runTest {
        var callbackSuccess = false
        var callbackError: String? = null

        viewModel.register(
            name = "Hajirah Juma",
            email = "hajirah@student.ac.mw",
            phone = "+265 991 234 567",
            pass = "SecurePass123"
        ) { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackSuccess)
        assertEquals(null, callbackError)
        assertTrue(viewModel.isLoggedIn.value)
        assertTrue(viewModel.authUiState.value is AuthUiState.Success)
        assertEquals("Registration successful.", (viewModel.authUiState.value as AuthUiState.Success).message)
    }

    /**
     * Test Case ID: SW_AUTH_REG_002
     * Description: Verify that registration fails when an email address that is already registered is entered.
     */
    @Test
    fun sw_auth_reg_002_registrationWithAlreadyRegisteredEmail() = runTest {
        // First registration
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "Password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        // Second registration with same email
        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "Password123") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("An account with this email address already exists.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
        assertEquals(
            "An account with this email address already exists.",
            (viewModel.authUiState.value as AuthUiState.Error).message
        )
    }

    /**
     * Test Case ID: SW_AUTH_REG_003
     * Description: Verify that registration is rejected when a password shorter than 6 characters is entered.
     */
    @Test
    fun sw_auth_reg_003_registrationWithWeakShortPassword() = runTest {
        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.register("Hajirah Juma", "newstudent@gmail.com", "+265 990 000 111", "12345") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("Password must be at least 6 characters long.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
    }

    /**
     * Test Case ID: SW_AUTH_REG_004
     * Description: Verify that registration is blocked when required input fields are left blank.
     */
    @Test
    fun sw_auth_reg_004_registrationWithEmptyMandatoryFields() = runTest {
        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.register("", "", "", "") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("Please enter your full name.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
    }

    /**
     * Test Case ID: SW_AUTH_LOG_001
     * Description: Verify that a registered student can successfully log in with valid credentials.
     */
    @Test
    fun sw_auth_log_001_successfulUserLogin() = runTest {
        // Register account
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()

        // Logout
        viewModel.logout()
        assertFalse(viewModel.isLoggedIn.value)

        // Login
        var callbackSuccess = false
        var callbackError: String? = null

        viewModel.login("hajirah@gmail.com", "password123") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(callbackSuccess)
        assertEquals(null, callbackError)
        assertTrue(viewModel.isLoggedIn.value)
        assertTrue(viewModel.authUiState.value is AuthUiState.Success)
    }

    /**
     * Test Case ID: SW_AUTH_LOG_002
     * Description: Verify that login fails when a registered email is entered with an incorrect password.
     */
    @Test
    fun sw_auth_log_002_loginWithIncorrectPassword() = runTest {
        // Register account
        viewModel.register("Hajirah Juma", "hajirah@gmail.com", "+265 991 234 567", "password123") { _, _ -> }
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.logout()

        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.login("hajirah@gmail.com", "WrongPassword99") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("Incorrect password or invalid email format.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
    }

    /**
     * Test Case ID: SW_AUTH_LOG_003
     * Description: Verify that login is rejected when an email address without a valid "@" format is entered.
     */
    @Test
    fun sw_auth_log_003_loginWithInvalidEmailFormat() = runTest {
        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.login("hajirahgmail.com", "password123") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("Incorrect password or invalid email format.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
    }

    /**
     * Test Case ID: SW_AUTH_LOG_004
     * Description: Verify that login is blocked when both email and password fields are left empty.
     */
    @Test
    fun sw_auth_log_004_loginWithEmptyEmailAndPassword() = runTest {
        var callbackSuccess = true
        var callbackError: String? = null

        viewModel.login("", "") { success, error ->
            callbackSuccess = success
            callbackError = error
        }

        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(callbackSuccess)
        assertEquals("Please enter your email address.", callbackError)
        assertTrue(viewModel.authUiState.value is AuthUiState.Error)
    }
}
