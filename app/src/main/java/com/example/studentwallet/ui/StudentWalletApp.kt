package com.example.studentwallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studentwallet.data.CategoryType
import com.example.studentwallet.data.TransactionType
import com.example.studentwallet.ui.components.NavTab
import com.example.studentwallet.ui.components.StudentWalletBottomNav
import com.example.studentwallet.ui.screens.AddTransactionScreen
import com.example.studentwallet.ui.screens.DashboardScreen
import com.example.studentwallet.ui.screens.EditProfileScreen
import com.example.studentwallet.ui.screens.EditTransactionScreen
import com.example.studentwallet.ui.screens.LoginScreen
import com.example.studentwallet.ui.screens.MonthlyBudgetScreen
import com.example.studentwallet.ui.screens.OnboardingScreen
import com.example.studentwallet.ui.screens.ProfileScreen
import com.example.studentwallet.ui.screens.RegisterScreen
import com.example.studentwallet.ui.screens.SelectCategoryScreen
import com.example.studentwallet.ui.screens.SpendingSummaryScreen
import com.example.studentwallet.ui.screens.SplashScreen
import com.example.studentwallet.ui.screens.TransactionsScreen
import com.example.studentwallet.ui.theme.ExpenseRed
import com.example.studentwallet.ui.theme.ExpenseRedBg
import com.example.studentwallet.ui.theme.IncomeGreen
import com.example.studentwallet.ui.theme.IncomeGreenBg
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextPrimary
import com.example.studentwallet.ui.theme.TextSecondary
import com.example.studentwallet.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentWalletApp(
    viewModel: WalletViewModel = viewModel()
) {
    val navController = rememberNavController()

    var selectedTab by remember { mutableStateOf(NavTab.HOME) }
    var showAddOptionsSheet by remember { mutableStateOf(false) }
    var tempCategorySelection by remember { mutableStateOf<CategoryType?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf("dashboard", "transactions", "budget", "profile")
    val showBottomNav = currentRoute in bottomBarRoutes

    if (showAddOptionsSheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showAddOptionsSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "What would you like to record?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Add Income Option
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAddOptionsSheet = false
                            navController.navigate("add_income")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(IncomeGreenBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = IncomeGreen)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Record Income", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Add money received like allowance or salary", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add Expense Option
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAddOptionsSheet = false
                            navController.navigate("add_expense")
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(ExpenseRedBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = ExpenseRed)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Record Expense", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Add money spent on food, transport, data, etc.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                StudentWalletBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        when (tab) {
                            NavTab.HOME -> navController.navigate("dashboard") { launchSingleTop = true }
                            NavTab.TRANSACTIONS -> navController.navigate("transactions") { launchSingleTop = true }
                            NavTab.BUDGET -> navController.navigate("budget") { launchSingleTop = true }
                            NavTab.PROFILE -> navController.navigate("profile") { launchSingleTop = true }
                        }
                    },
                    onAddClick = { showAddOptionsSheet = true }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onNavigateNext = {
                        navController.navigate("onboarding") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            composable("onboarding") {
                OnboardingScreen(
                    onFinishOnboarding = {
                        navController.navigate("login") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("login") {
                LoginScreen(
                    onLoginSuccess = { email, pass ->
                        viewModel.login(email, pass)
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateRegister = {
                        navController.navigate("register")
                    }
                )
            }

            composable("register") {
                RegisterScreen(
                    onRegisterSuccess = { name, email, phone, pass ->
                        viewModel.register(name, email, phone, pass)
                        navController.navigate("dashboard") {
                            popUpTo("register") { inclusive = true }
                        }
                    },
                    onNavigateLogin = {
                        navController.popBackStack()
                    }
                )
            }

            composable("dashboard") {
                selectedTab = NavTab.HOME
                DashboardScreen(
                    viewModel = viewModel,
                    onViewAllTransactions = {
                        selectedTab = NavTab.TRANSACTIONS
                        navController.navigate("transactions")
                    },
                    onTransactionClick = { tx ->
                        navController.navigate("edit_transaction/${tx.id}")
                    },
                    onNotificationClick = { }
                )
            }

            composable("transactions") {
                selectedTab = NavTab.TRANSACTIONS
                TransactionsScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onTransactionClick = { tx ->
                        navController.navigate("edit_transaction/${tx.id}")
                    }
                )
            }

            composable("budget") {
                selectedTab = NavTab.BUDGET
                MonthlyBudgetScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("profile") {
                selectedTab = NavTab.PROFILE
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateEditProfile = { navController.navigate("edit_profile") },
                    onNavigateSpendingSummary = { navController.navigate("spending_summary") },
                    onNavigateBudgetSettings = { navController.navigate("budget") },
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("add_income") {
                AddTransactionScreen(
                    transactionType = TransactionType.INCOME,
                    viewModel = viewModel,
                    selectedCategory = null,
                    onOpenCategorySelector = { },
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }

            composable("add_expense") {
                AddTransactionScreen(
                    transactionType = TransactionType.EXPENSE,
                    viewModel = viewModel,
                    selectedCategory = tempCategorySelection,
                    onOpenCategorySelector = {
                        navController.navigate("select_category")
                    },
                    onBackClick = {
                        tempCategorySelection = null
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        tempCategorySelection = null
                        navController.popBackStack()
                    }
                )
            }

            composable("select_category") {
                SelectCategoryScreen(
                    onCategorySelected = { cat ->
                        tempCategorySelection = cat
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("spending_summary") {
                SpendingSummaryScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "edit_transaction/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("transactionId") ?: ""
                EditTransactionScreen(
                    transactionId = txId,
                    viewModel = viewModel,
                    selectedCategory = tempCategorySelection,
                    onOpenCategorySelector = { navController.navigate("select_category") },
                    onBackClick = {
                        tempCategorySelection = null
                        navController.popBackStack()
                    },
                    onDeleteSuccess = {
                        tempCategorySelection = null
                        navController.popBackStack()
                    }
                )
            }

            composable("edit_profile") {
                EditProfileScreen(
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainAppPreview() {
    StudentWalletTheme {
        StudentWalletApp()
    }
}
