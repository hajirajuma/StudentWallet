package com.example.studentwallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studentwallet.ui.screens.LoginScreen
import com.example.studentwallet.ui.screens.RegisterScreen
import com.example.studentwallet.viewmodel.AuthUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test Case ID: SW_AUTH_REG_001..004 UI Test
     */
    @Test
    fun registerScreen_uiElementsAndErrorDisplay() {
        composeTestRule.setContent {
            RegisterScreen(
                authUiState = AuthUiState.Error("An account with this email address already exists."),
                onRegisterSubmit = { _, _, _, _ -> },
                onNavigateLogin = {}
            )
        }

        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed().assertIsEnabled()

        // Verify error message is displayed
        composeTestRule.onNodeWithText("An account with this email address already exists.").assertIsDisplayed()

        // Perform click
        composeTestRule.onNodeWithText("Sign Up").performClick()
    }

    /**
     * Test Case ID: SW_AUTH_LOG_001..004 UI Test
     */
    @Test
    fun loginScreen_uiElementsAndErrorDisplay() {
        composeTestRule.setContent {
            LoginScreen(
                authUiState = AuthUiState.Error("Incorrect password or invalid email format."),
                onLoginClick = { _, _ -> },
                onNavigateRegister = {}
            )
        }

        composeTestRule.onNodeWithText("Welcome Back").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log In").assertIsDisplayed().assertIsEnabled()

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Incorrect password or invalid email format.").assertIsDisplayed()

        // Perform click
        composeTestRule.onNodeWithText("Log In").performClick()
    }
}
