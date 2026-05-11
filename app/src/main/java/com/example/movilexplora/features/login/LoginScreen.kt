package com.example.movilexplora.features.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToModerator: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val loginResult by viewModel.loginResult.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            val message = when (result) {
                is RequestResult.Success -> result.message
                is RequestResult.Failure -> result.errorMessage
            }
            snackbarHostState.showSnackbar(message)

            viewModel.resetLoginResult()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val isError = (loginResult is RequestResult.Failure)
                Snackbar(
                    containerColor = if (isError) MaterialTheme.colorScheme.error else Turquoise,
                    contentColor = Color.White
                ) { Text(data.visuals.message) }
            }
        },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.loginscreen_back_0),
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
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.login_welcome_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 38.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.login_welcome_desc),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = viewModel.email.value,
                onValueChange = { viewModel.email.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_email_label), color = Turquoise) },
                placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
                trailingIcon = {
                    Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    focusedLabelColor = Turquoise,
                    cursorColor = Turquoise
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = viewModel.email.error != null
            )
            Text(
                text = stringResource(R.string.login_email_helper),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.login_password_label), color = Turquoise) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    focusedLabelColor = Turquoise,
                    cursorColor = Turquoise
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = viewModel.password.error != null
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToForgotPassword,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    color = Turquoise,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = viewModel.isFormValid
            ) {
                Text(
                    text = stringResource(R.string.login_button),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onNavigateToModerator,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Turquoise,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.login_moderator),
                        color = Turquoise,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
