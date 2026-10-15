package com.example.studentwallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.TextMuted

enum class NavTab {
    HOME,
    TRANSACTIONS,
    BUDGET,
    PROFILE
}

@Composable
fun StudentWalletBottomNav(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    isSelected = selectedTab == NavTab.HOME,
                    onClick = { onTabSelected(NavTab.HOME) }
                )
                NavItem(
                    label = "Transactions",
                    icon = Icons.Default.SwapVert,
                    isSelected = selectedTab == NavTab.TRANSACTIONS,
                    onClick = { onTabSelected(NavTab.TRANSACTIONS) }
                )

                // Placeholder space for centered floating add button
                Spacer(modifier = Modifier.size(52.dp))

                NavItem(
                    label = "Budget",
                    icon = Icons.Default.PieChart,
                    isSelected = selectedTab == NavTab.BUDGET,
                    onClick = { onTabSelected(NavTab.BUDGET) }
                )
                NavItem(
                    label = "Profile",
                    icon = Icons.Default.Person,
                    isSelected = selectedTab == NavTab.PROFILE,
                    onClick = { onTabSelected(NavTab.PROFILE) }
                )
            }
        }

        // Floating Center Add Button
        Box(
            modifier = Modifier
                .offset(y = (-20).dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(OrangePrimary)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = OrangePrimary
    val inactiveColor = TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
