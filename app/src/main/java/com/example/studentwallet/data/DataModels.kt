package com.example.studentwallet.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.studentwallet.ui.theme.CategoryAccommodation
import com.example.studentwallet.ui.theme.CategoryAccommodationBg
import com.example.studentwallet.ui.theme.CategoryAirtime
import com.example.studentwallet.ui.theme.CategoryAirtimeBg
import com.example.studentwallet.ui.theme.CategoryEducation
import com.example.studentwallet.ui.theme.CategoryEducationBg
import com.example.studentwallet.ui.theme.CategoryEntertainment
import com.example.studentwallet.ui.theme.CategoryEntertainmentBg
import com.example.studentwallet.ui.theme.CategoryFood
import com.example.studentwallet.ui.theme.CategoryFoodBg
import com.example.studentwallet.ui.theme.CategoryOther
import com.example.studentwallet.ui.theme.CategoryOtherBg
import com.example.studentwallet.ui.theme.CategoryTransport
import com.example.studentwallet.ui.theme.CategoryTransportBg
import com.example.studentwallet.ui.theme.IncomeGreen
import com.example.studentwallet.ui.theme.IncomeGreenBg

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class CategoryType(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color
) {
    FOOD("Food", Icons.Default.Restaurant, CategoryFood, CategoryFoodBg),
    TRANSPORT("Transport", Icons.Default.DirectionsBus, CategoryTransport, CategoryTransportBg),
    ACCOMMODATION("Accommodation", Icons.Default.Home, CategoryAccommodation, CategoryAccommodationBg),
    EDUCATION("Education", Icons.Default.School, CategoryEducation, CategoryEducationBg),
    AIRTIME_DATA("Airtime/Data", Icons.Default.PhoneAndroid, CategoryAirtime, CategoryAirtimeBg),
    ENTERTAINMENT("Entertainment", Icons.Default.SportsEsports, CategoryEntertainment, CategoryEntertainmentBg),
    OTHER("Other", Icons.Default.MoreHoriz, CategoryOther, CategoryOtherBg),
    ALLOWANCE("Allowance", Icons.Default.AccountBalanceWallet, IncomeGreen, IncomeGreenBg),
    SALARY("Salary", Icons.Default.AttachMoney, IncomeGreen, IncomeGreenBg);

    companion object {
        fun fromTitle(title: String): CategoryType {
            return entries.find { it.title.equals(title, ignoreCase = true) } ?: OTHER
        }
    }
}

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: CategoryType = CategoryType.OTHER,
    val date: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class MonthlyBudget(
    val id: String = "",
    val amount: Double = 100000.0,
    val month: Int = 3,
    val year: Int = 2026,
    val updatedAt: Long = System.currentTimeMillis()
)

data class CategoryBudget(
    val category: CategoryType,
    val spentAmount: Double,
    val totalBudget: Double
)
