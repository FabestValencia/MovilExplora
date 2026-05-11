package com.example.movilexplora.features.moderator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Badge
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.R
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorScreen(
    onNavigateBack: () -> Unit,
    onAccessGranted: () -> Unit,
    viewModel: ModeratorViewModel = hiltViewModel()
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val accessResult by viewModel.accessResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(accessResult) {
        accessResult?.let { result ->
            if (result is com.example.movilexplora.core.utils.RequestResult.Success) {
                onAccessGranted()
                viewModel.resetResult()
            } else if (result is com.example.movilexplora.core.utils.RequestResult.Failure) {
                snackbarHostState.showSnackbar(result.errorMessage)
                viewModel.resetResult()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = MaterialTheme.colorScheme.onSurface
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
            Spacer(modifier = Modifier.height(40.dp))

            // Icon + Title Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Turquoise,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.moderator_access_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

            // Description
            Text(
                text = stringResource(R.string.moderator_access_desc),
                style = MaterialTheme.typography.bodyLarge,
                color = GrayText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email Field
            OutlinedTextField(
                value = viewModel.email.value,
                onValueChange = { viewModel.email.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.email_placeholder), color = GrayText.copy(alpha = 0.5f)) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = GrayText.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = viewModel.email.error != null
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.password_placeholder), color = GrayText.copy(alpha = 0.5f)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = null, tint = GrayText.copy(alpha = 0.6f))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                isError = viewModel.password.error != null
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Login Admin Button
            Button(
                onClick = { viewModel.loginAdmin() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = viewModel.isFormValid
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.admin_login_button),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Text
            Text(
                text = stringResource(R.string.moderator_footer),
                style = MaterialTheme.typography.bodySmall,
                color = GrayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            )
        }
    }
}
