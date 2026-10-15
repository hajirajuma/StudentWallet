package com.example.studentwallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentwallet.ui.components.CustomTextField
import com.example.studentwallet.ui.components.PrimaryButton
import com.example.studentwallet.ui.components.StudentWalletHeaderLogo
import com.example.studentwallet.ui.theme.NavyBlue
import com.example.studentwallet.ui.theme.OrangePrimary
import com.example.studentwallet.ui.theme.StudentWalletTheme
import com.example.studentwallet.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, pass: String) -> Unit,
    onNavigateRegister: () -> Unit
) {
    var emailOrUsername by remember { mutableStateOf("hajirah@gmail.com") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            StudentWalletHeaderLogo(iconSize = 52.dp)

            Spacer(modifier = Modifier.height(36.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome Back",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyBlue
                )
                Text(
                    text = "Log in to your account",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            CustomTextField(
                value = emailOrUsername,
                onValueChange = { emailOrUsername = it },
                label = "Email or Username",
                placeholder = "e.g. student@school.ac.mw",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                trailingIcon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "Log In",
                onClick = { onLoginSuccess(emailOrUsername, password) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { /* Forgot Password */ }) {
                Text("Forgot password?", color = TextSecondary, fontSize = 13.sp)
            }
        }

        // Footer link
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = TextSecondary, fontSize = 14.sp)
            Text(
                text = "Sign Up",
                color = OrangePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onNavigateRegister() }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    StudentWalletTheme {
        LoginScreen(
            onLoginSuccess = { _, _ -> },
            onNavigateRegister = {}
        )
    }
}
