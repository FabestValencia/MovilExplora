package com.example.movilexplora.features.registrer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.movilexplora.core.component.DropdownMenu
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.features.onboarding.OnboardingViewModel
import com.example.movilexplora.features.onboarding.PermissionType
import com.example.movilexplora.core.component.OnboardingPermissionDialog

@Composable
fun ConfirmAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.confirm_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToVerifyCode: (String) -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val registerResult by viewModel.registerResult.collectAsState()
    val locationPermissionDenied by viewModel.locationPermissionDenied.collectAsState()
    val onboardingState by onboardingViewModel.state.collectAsState()
    
    var showConfirmDialog by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    val gpsLabel = stringResource(R.string.register_gps_city_label)
    val locationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { 
                        viewModel.updateLocation(it.latitude, it.longitude)
                        viewModel.city.onChange(gpsLabel)
                    }
                }
            } catch (_: SecurityException) {}
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    val triggerLocationRequest = {
        val permissions = arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        locationPermissionLauncher.launch(permissions)
    }

    LaunchedEffect(registerResult) {
        when (registerResult) {
            is RequestResult.Success -> {
                val uid = (registerResult as RequestResult.Success).data as? String
                if (uid != null) {
                    onNavigateToVerifyCode(uid)
                } else {
                    onNavigateToLogin()
                }
                viewModel.resetRegisterResult()
            }
            is RequestResult.Failure -> {
                // Handle error if needed
            }
            else -> {}
        }
    }

    if (showConfirmDialog) {
        ConfirmAlertDialog(
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                viewModel.register()
            },
            title = stringResource(R.string.register_confirm_title),
            text = stringResource(R.string.register_confirm_msg)
        )
    }

    // Permission Dialog
    onboardingState.showPermissionDialog?.let { permissionType ->
        if (permissionType == PermissionType.LOCATION) {
            OnboardingPermissionDialog(
                permissionType = permissionType,
                onChoiceMade = { always ->
                    onboardingViewModel.onPermissionChoice(permissionType, always)
                    triggerLocationRequest()
                },
                onDismiss = {
                    onboardingViewModel.dismissPermissionDialog()
                    viewModel.onLocationPermissionDenied()
                }
            )
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
            
            // GPS Location Section
            if (!locationPermissionDenied) {
                Text(
                    text = stringResource(R.string.createpostscreen_ubicaci_n_8),
                    fontSize = 14.sp,
                    color = GrayText.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp, top = 8.dp)
                )
                OutlinedButton(
                    onClick = {
                        val permissions = arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        val isGranted = permissions.all { 
                            androidx.core.content.ContextCompat.checkSelfPermission(context, it) == android.content.pm.PackageManager.PERMISSION_GRANTED 
                        }

                        if (isGranted) {
                            triggerLocationRequest()
                        } else {
                            onboardingViewModel.checkAndShowPermissionOnboarding(PermissionType.LOCATION) {
                                triggerLocationRequest()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, if (viewModel.city.value == gpsLabel) Turquoise else Color.LightGray.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (viewModel.city.value == gpsLabel) Turquoise.copy(alpha = 0.1f) else Color.Transparent
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn, 
                            contentDescription = null, 
                            tint = if (viewModel.city.value == gpsLabel) Turquoise else GrayText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.city.value == gpsLabel) stringResource(R.string.register_location_detected) else stringResource(R.string.register_use_gps), 
                            color = if (viewModel.city.value == gpsLabel) Turquoise else GrayText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.register_or_choose_manual),
                    fontSize = 12.sp,
                    color = GrayText.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            DropdownMenu(
                value = viewModel.city.value,
                onValueChange = { viewModel.city.onChange(it) },
                label = stringResource(R.string.editprofilescreen_ubicaci_n_8),
                icon = Icons.Default.Home,
                list = viewModel.cities,
                supportingText = viewModel.city.error
            )

            RegisterField(label = stringResource(R.string.register_email_label), placeholder = stringResource(R.string.register_email_placeholder), field = viewModel.email, keyboardType = KeyboardType.Email)
            RegisterField(label = stringResource(R.string.register_password_label), placeholder = stringResource(R.string.register_password_placeholder), field = viewModel.password, isPassword = true)
            RegisterField(label = stringResource(R.string.register_confirm_password_label), placeholder = stringResource(R.string.register_confirm_password_placeholder), field = viewModel.confirmPassword, isPassword = true)

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise),
                enabled = viewModel.isFormValid
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
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                cursorColor = Turquoise
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
