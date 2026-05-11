package com.example.movilexplora.features.verificationcode

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    onNavigateBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isCodeValid = code.length == 4

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.verificationcodescreen_back_6),
                            tint = MaterialTheme.colorScheme.onBackground
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.verificationcodescreen_verificar_c_digo_0),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.verificationcodescreen_introduce_el_c_digo_de_6_d_git_2),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 4-digit code input
            CodeInput(
                value = code,
                onValueChange = { if (it.length <= 4) code = it }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (isCodeValid) {
                        onVerifySuccess()
                    } else {
                        errorMessage = "El código ingresado es incorrecto"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = isCodeValid
            ) {
                Text(
                    text = stringResource(R.string.verificationcodescreen_verificar_c_digo_5),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.background
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
                repeat(4) { index ->
                    val char = value.getOrNull(index)?.toString() ?: ""
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(64.dp)
                            .background(
                                color = if (char.isNotEmpty()) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (char.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (char.isNotEmpty()) {
                            Text(
                                text = char,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        } else if (index == value.length) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(24.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    )
}
