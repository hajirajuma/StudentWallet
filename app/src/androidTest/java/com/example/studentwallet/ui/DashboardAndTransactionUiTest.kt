package com.example.studentwallet.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.fakes.FakeFirebaseRepository
import com.example.studentwallet.ui.screens.AddTransactionScreen
import com.example.studentwallet.ui.screens.DashboardScreen
import com.example.studentwallet.ui.screens.EditTransactionScreen
import com.example.studentwallet.ui.screens.SelectCategoryScreen
import com.example.studentwallet.ui.screens.TransactionsScreen
import com.example.studentwallet.viewmodel.WalletViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardAndTransactionUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Test Case ID: SW_DASH_CALC_001 & SW_DASH_TOGGLE_001 UI Test
     */
    @Test
    fun dashboardScreen_balanceCardAndPrivacyToggleUiTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_dash_test")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            DashboardScreen(
                viewModel = viewModel,
                onViewAllTransactions = {},
                onTransactionClick = {},
                onNotificationClick = {},
            )
        }

        composeTestRule.onNodeWithText("Total Balance").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monthly Budget").assertIsDisplayed()

        // Privacy Toggle
        composeTestRule.onNodeWithContentDescription("Toggle Balance").performClick()
        composeTestRule.onNodeWithText("MK ****").assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("Toggle Balance").performClick()
        composeTestRule.onNodeWithText("MK 0").assertIsDisplayed()
    }

    /**
     * Test Case ID: SW_INC_ADD_001..003 & SW_EXP_ADD_001..002 UI Test
     */
    @Test
    fun addTransactionScreen_uiElementsTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_add_test")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            AddTransactionScreen(
                transactionType = TransactionType.EXPENSE,
                viewModel = viewModel,
                selectedCategory = CategoryType.FOOD,
                onOpenCategorySelector = {},
                onBackClick = {},
                onSaveSuccess = {},
            )
        }

        composeTestRule.onNodeWithText("Add Expense").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amount (MK)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Note (optional)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    /**
     * Test Case ID: SW_EXP_CAT_001 UI Test
     */
    @Test
    fun selectCategoryScreen_displaysAll7CategoriesUiTest() {
        composeTestRule.setContent {
            SelectCategoryScreen(
                onCategorySelected = {},
                onBackClick = {},
            )
        }

        composeTestRule.onNodeWithText("Select Category").assertIsDisplayed()
        composeTestRule.onNodeWithText("Food").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transport").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accommodation").assertIsDisplayed()
        composeTestRule.onNodeWithText("Education").assertIsDisplayed()
        composeTestRule.onNodeWithText("Airtime/Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Entertainment").assertIsDisplayed()
        composeTestRule.onNodeWithText("Other").assertIsDisplayed()
    }

    /**
     * Test Case ID: SW_TX_VIEW_001 & SW_TX_FILTER_001 UI Test
     */
    @Test
    fun transactionsScreen_filterTabsUiTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_tx_test")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            TransactionsScreen(
                viewModel = viewModel,
                onBackClick = {},
                onTransactionClick = {},
            )
        }

        composeTestRule.onNodeWithText("Transactions").assertIsDisplayed()
        composeTestRule.onNodeWithText("All").assertIsDisplayed()
        composeTestRule.onNodeWithText("Income").assertIsDisplayed()
        composeTestRule.onNodeWithText("Expense").assertIsDisplayed()

        composeTestRule.onNodeWithText("Income").performClick()
        composeTestRule.onNodeWithText("Expense").performClick()
        composeTestRule.onNodeWithText("All").performClick()
    }

    /**
     * Test Case ID: SW_TX_EDIT_001 & SW_TX_DEL_001 UI Test
     */
    @Test
    fun editTransactionScreen_uiTest() {
        val repository = FakeFirebaseRepository()
        repository.setCurrentUserId("user_edit_test")
        val viewModel = WalletViewModel(repository)

        composeTestRule.setContent {
            EditTransactionScreen(
                transactionId = "tx1",
                viewModel = viewModel,
                selectedCategory = CategoryType.FOOD,
                onOpenCategorySelector = {},
                onBackClick = {},
                onDeleteSuccess = {},
            )
        }

        composeTestRule.onNodeWithText("Edit Transaction").assertIsDisplayed()
        composeTestRule.onNodeWithText("Amount (MK)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Transaction").assertIsDisplayed()

        composeTestRule.onNodeWithText("Delete Transaction").performClick()
        composeTestRule.onNodeWithText("Are you sure you want to delete this transaction?").assertIsDisplayed()
    }
}
