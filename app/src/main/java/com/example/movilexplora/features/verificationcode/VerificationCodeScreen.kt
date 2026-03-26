package com.example.movilexplora.features.verificationcode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    onNavigateBack: () -> Unit,
    onVerifySuccess: () -> Unit,
    viewModel: VerificationCodeViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    val verificationResult by viewModel.verificationResult.collectAsState()

    LaunchedEffect(verificationResult) {
        if (verificationResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            onVerifySuccess()
            viewModel.resetResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Verificar Código",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Verificar Código",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Introduce el código de 6 dígitos enviado a tu correo electrónico para recuperar tu contraseña.",
                fontSize = 16.sp,
                color = GrayText,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 6-digit code input
            CodeInput(
                value = code,
                onValueChange = { if (it.length <= 6) code = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¿No recibiste el código?",
                fontSize = 14.sp,
                color = GrayText
            )

            TextButton(onClick = { viewModel.resendCode() }) {
                Text(
                    text = "Reenviar código",
                    color = Turquoise,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.verifyCode(code) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = code.length == 6
            ) {
                Text(
                    text = "Verificar Código",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CodeInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(6) { index ->
                    val char = when {
                        index < value.length -> value[index].toString()
                        else -> ""
                    }
                    val isFocused = index == value.length

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .border(
                                width = 1.dp,
                                color = if (isFocused) Turquoise else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(DarkBlue.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(2.dp)
                                    .background(Color.LightGray.copy(alpha = 0.5f))
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
