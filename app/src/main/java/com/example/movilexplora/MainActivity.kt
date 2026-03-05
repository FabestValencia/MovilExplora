package com.example.movilexplora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.movilexplora.features.home.HomeScreen
import com.example.movilexplora.features.login.LoginScreen
import com.example.movilexplora.features.passwordReset.PasswordResetScreen
import com.example.movilexplora.features.registrer.RegisterScreen
import com.example.movilexplora.ui.theme.MovilExploraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasswordResetScreen(
                onNavigateToLogin = {}
            )

        }
    }

}