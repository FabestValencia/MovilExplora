package com.example.movilexplora.features.forgotpassword

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onNavigateToVerifyCode: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val emailValue = viewModel.email.value
    val requestResult by viewModel.requestResult.collectAsState()

    LaunchedEffect(requestResult) {
        if (requestResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            onNavigateToVerifyCode()
            viewModel.resetResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.forgotpasswordscreen_explora_0),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.forgotpasswordscreen_back_5),
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
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Icon box
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Turquoise.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = Turquoise,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¿Olvidaste tu\ncontraseña?",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.forgotpasswordscreen_introduce_tu_correo_electr_nic_1),
                fontSize = 16.sp,
                color = GrayText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.forgotpasswordscreen_correo_electr_nico_2),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = emailValue,
                onValueChange = { viewModel.email.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(text = stringResource(R.string.forgotpasswordscreen_ejemplo_correo_com_3), color = GrayText.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Mail,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Turquoise,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true,
                isError = viewModel.email.error != null
            )
            if (viewModel.email.error != null) {
                Text(
                    text = viewModel.email.error!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.sendResetLink() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = viewModel.isFormValid
            ) {
                Text(
                    text = stringResource(R.string.forgotpasswordscreen_enviar_enlace_4),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
