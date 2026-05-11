package com.example.movilexplora.features.registrer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val registerResult by viewModel.registerResult.collectAsState()

    LaunchedEffect(registerResult) {
        when (registerResult) {
            is RequestResult.Success -> {
                viewModel.resetRegisterResult()
                onNavigateToLogin()
            }
            is RequestResult.Failure -> {
                // Handle error if needed
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.registrerscreen_back_0))
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
            Text(
                text = stringResource(R.string.register_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.register_desc),
                fontSize = 16.sp,
                color = GrayText,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            RegisterField(label = stringResource(R.string.register_name_label), placeholder = stringResource(R.string.register_name_placeholder), field = viewModel.nombre)
            RegisterField(label = stringResource(R.string.register_email_label), placeholder = stringResource(R.string.register_email_placeholder), field = viewModel.email, keyboardType = KeyboardType.Email)
            RegisterField(label = stringResource(R.string.register_password_label), placeholder = stringResource(R.string.register_password_placeholder), field = viewModel.password, isPassword = true)
            RegisterField(label = stringResource(R.string.register_confirm_password_label), placeholder = stringResource(R.string.register_confirm_password_placeholder), field = viewModel.confirmPassword, isPassword = true)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Text(text = stringResource(R.string.register_button), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.register_login_button), color = Turquoise, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RegisterField(
    label: String,
    placeholder: String,
    field: ValidatedField<String>,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(text = label, fontSize = 14.sp, color = GrayText.copy(alpha = 0.8f), modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
        
        OutlinedTextField(
            value = field.value,
            onValueChange = { field.onChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder, color = GrayText.copy(alpha = 0.5f)) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Turquoise,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedContainerColor = Color(0xFFF7F8F9),
                unfocusedContainerColor = Color(0xFFF7F8F9)
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = GrayText.copy(alpha = 0.6f))
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = field.error != null
        )
        if (field.error != null) {
            Text(text = field.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 2.dp))
        }
    }
}
