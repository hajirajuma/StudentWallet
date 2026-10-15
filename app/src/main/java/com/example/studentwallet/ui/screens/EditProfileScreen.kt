package com.example.studentwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.ui.components.CustomTextField
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.components.SecondaryOutlinedButton
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.viewmodel.WalletViewModel

@Composable
fun EditProfileScreen(
    viewModel: WalletViewModel,
    onBackClick: () -> Unit
) {
    val currentProfile by viewModel.userProfile.collectAsState()

    var nameText by remember { mutableStateOf(currentProfile.name) }
    var emailText by remember { mutableStateOf(currentProfile.email) }
    var phoneText by remember { mutableStateOf(currentProfile.phone) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NavyBlue
                )
            }
            Text(
                text = "Edit Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        CustomTextField(
            value = nameText,
            onValueChange = { nameText = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = emailText,
            onValueChange = { emailText = it },
            label = "Email Address",
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        CustomTextField(
            value = phoneText,
            onValueChange = { phoneText = it },
            label = "Phone Number",
            leadingIcon = Icons.Default.Phone
        )

        Spacer(modifier = Modifier.height(36.dp))

        PrimaryButton(
            text = "Save Changes",
            onClick = {
                viewModel.updateUserProfile(nameText, emailText, phoneText)
                onBackClick()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        SecondaryOutlinedButton(
            text = "Cancel",
            onClick = onBackClick
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EditProfileScreenPreview() {
    val vm = remember { WalletViewModel() }
    StudentWalletTheme {
        EditProfileScreen(
            viewModel = vm,
            onBackClick = {}
        )
    }
}
