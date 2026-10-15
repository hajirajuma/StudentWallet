package com.example.studentwallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.studentwallet.ui.StudentWalletApp
import com.example.studentwallet.ui.theme.StudentWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentWalletTheme {
                StudentWalletApp()
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StudentWalletAppPreview() {
    StudentWalletTheme {
        StudentWalletApp()
    }
}
