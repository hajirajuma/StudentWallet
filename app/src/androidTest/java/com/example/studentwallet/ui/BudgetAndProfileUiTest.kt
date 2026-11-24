package com.example.studentwallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studentwallet.fakes.FakeFirebaseRepository
import com.example.studentwallet.ui.screens.EditProfileScreen
import com.example.studentwallet.ui.screens.MonthlyBudgetScreen
import com.example.studentwallet.ui.screens.ProfileScreen
import com.example.studentwallet.ui.screens.SpendingSummaryScreen
import com.example.studentwallet.viewmodel.WalletViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetAndProfileUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test Case ID: SW_BUDGET_SET_001..002 & SW_BUDGET_PROG_001 UI Test
     */
    @Test
    fun monthlyBudgetScreen_uiElementsAndSetDialogTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_budget_ui")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            MonthlyBudgetScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("Monthly Budget").assertIsDisplayed()
        composeTestRule.onNodeWithText("used").assertIsDisplayed()
        composeTestRule.onNodeWithText("Set New Budget").assertIsDisplayed()
        composeTestRule.onNodeWithText("Budget Overview").assertIsDisplayed()

        // Click "Set New Budget" to open dialog
        composeTestRule.onNodeWithText("Set New Budget").performClick()
        composeTestRule.onNodeWithText("Enter your new monthly budget limit (MK):").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Test Case ID: SW_SUM_VIEW_001 UI Test
     */
    @Test
    fun spendingSummaryScreen_uiTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_summary_ui")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            SpendingSummaryScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("Spending Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Expenses").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category Breakdown").assertIsDisplayed()
    }

    /**
     * Test Case ID: SW_PROF_EDIT_001 & SW_PROF_OUT_001 UI Test
     */
    @Test
    fun profileAndEditProfileScreen_uiTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_profile_ui")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            ProfileScreen(
                viewModel = viewModel,
                onNavigateEditProfile = {},
                onNavigateSpendingSummary = {},
                onNavigateBudgetSettings = {},
                onNavigateNotifications = {},
                onNavigateHelpSupport = {},
                onLogoutClick = {},
            )
        }

        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Spending Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Budget Settings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log Out").assertIsDisplayed()

        // Click "Log Out" and verify confirmation dialog appears
        composeTestRule.onNodeWithText("Log Out").performClick()
        composeTestRule.onNodeWithText("Are you sure you want to log out of StudentWallet?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Test Case ID: EditProfileScreen UI Test
     */
    @Test
    fun editProfileScreen_uiElementsTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_edit_prof")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            EditProfileScreen(
                viewModel = viewModel,
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("Edit Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email Address").assertIsDisplayed()
        composeTestRule.onNodeWithText("Phone Number").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save Changes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }
}
