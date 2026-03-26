package com.example.movilexplora.features.passwordReset

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.utils.RequestResult
import kotlinx.coroutines.delay

@Composable
fun PasswordResetScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: PasswordResetViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resetResult by viewModel.resetResult.collectAsState()


    LaunchedEffect(resetResult) {
        resetResult?.let { result ->
            val message = when (result) {
                is RequestResult.Success -> result.message
                is RequestResult.Failure -> result.errorMessage
            }
            snackbarHostState.showSnackbar(message)
            if (result is RequestResult.Success) {
                delay(2000)
                onNavigateToLogin()
            }
            viewModel.resetStatus()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isError = (resetResult is RequestResult.Failure)
                Snackbar(
                    containerColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) { Text(data.visuals.message) }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
        ) {
            Text(text = "Restablecer Contraseña", style = MaterialTheme.typography.headlineMedium)

            Text(text = "Ingrese el código de 5 dígitos enviado a su correo", style = MaterialTheme.typography.bodyMedium)

            // Fila con los 5 campos de entrada para el código
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                viewModel.codeFields.forEach { field ->
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = field.value,
                        onValueChange = { if (it.length <= 1) field.onChange(it) },
                        isError = field.error != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }


            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.newPassword.value,
                onValueChange = { viewModel.newPassword.onChange(it) },
                label = { Text("Nueva Contraseña") },
                isError = viewModel.newPassword.error != null,
                supportingText = viewModel.newPassword.error?.let { { Text(it) } },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Button(
                onClick = { viewModel.resetPassword() },
                enabled = viewModel.isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar Contraseña")
            }
        }
    }
}
