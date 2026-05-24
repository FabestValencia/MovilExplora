package com.example.movilexplora.features.verificationcode

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.domain.model.User
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationCodeScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onVerifySuccess: (User) -> Unit,
    viewModel: VerificationCodeViewModel = hiltViewModel()
) {
    val verificationResult by viewModel.verificationResult.collectAsState()
    val resendResult by viewModel.resendResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.initUserId(userId)
    }

    LaunchedEffect(verificationResult) {
        val result = verificationResult
        if (result is RequestResult.Success) {
            val user = result.data as? User
            if (user != null) {
                onVerifySuccess(user)
                viewModel.resetResult()
            }
        }
    }

    LaunchedEffect(resendResult) {
        resendResult?.let { result ->
            if (result is RequestResult.Success) {
                snackbarHostState.showSnackbar(result.message)
                viewModel.resetResult()
            } else if (result is RequestResult.Failure) {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.resetResult()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

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
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (verificationResult is RequestResult.Failure) {
                Text(
                    text = (verificationResult as RequestResult.Failure).errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            Button(
                onClick = {
                    viewModel.checkVerificationStatus()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = verificationResult !is RequestResult.Loading
            ) {
                if (verificationResult is RequestResult.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = stringResource(R.string.verificationcodescreen_verificar_c_digo_5),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.background
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { viewModel.resendCode() }) {
                Text(
                    text = stringResource(R.string.verificationcodescreen_reenviar_c_digo_4),
                    color = Turquoise
                )
            }
        }
    }
}

