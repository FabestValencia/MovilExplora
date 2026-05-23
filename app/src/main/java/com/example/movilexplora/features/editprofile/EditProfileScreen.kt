package com.example.movilexplora.features.editprofile

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.component.ProfileImage
import com.example.movilexplora.features.profile.DeleteAccountDialog
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import java.io.File

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "profile_photo_",
        ".jpg",
        context.cacheDir
    ).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    onUpdateSuccess: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val updateResult by viewModel.updateResult.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val onboardingViewModel: com.example.movilexplora.features.onboarding.OnboardingViewModel = hiltViewModel()
    val onboardingState by onboardingViewModel.state.collectAsState()

    val (showDeleteDialog, setShowDeleteDialog) = remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onPhotoSelected(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempCameraUri?.let { viewModel.onPhotoSelected(it) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            tempCameraUri = createTempImageUri(context)
            tempCameraUri?.let { cameraLauncher.launch(it) }
        }
    }

    val triggerCamera = {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            tempCameraUri = createTempImageUri(context)
            tempCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val triggerGallery = {
        galleryLauncher.launch("image/*")
    }

    // Permission Dialog
    onboardingState.showPermissionDialog?.let { permissionType ->
        com.example.movilexplora.core.component.OnboardingPermissionDialog(
            permissionType = permissionType,
            onChoiceMade = { always ->
                onboardingViewModel.onPermissionChoice(permissionType, always)
                if (permissionType == com.example.movilexplora.features.onboarding.PermissionType.CAMERA) triggerCamera()
                else if (permissionType == com.example.movilexplora.features.onboarding.PermissionType.GALLERY) triggerGallery()
            },
            onDismiss = {
                onboardingViewModel.dismissPermissionDialog()
            }
        )
    }

    LaunchedEffect(updateResult) {
        if (updateResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            onUpdateSuccess()
            viewModel.resetResult()
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { setShowDeleteDialog(false) },
            onConfirm = {
                // Logic to delete account
                setShowDeleteDialog(false)
                onNavigateBack() // Redirect or exit app
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.editprofilescreen_back_9), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    val isSaving = updateResult is com.example.movilexplora.core.utils.RequestResult.Loading
                    TextButton(
                        onClick = { viewModel.updateProfile() },
                        enabled = viewModel.isFormValid && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Turquoise)
                        } else {
                            Text(stringResource(R.string.edit_profile_save), color = Turquoise, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Profile Image Edit
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    val currentImageUrl = photoUri?.toString() ?: photoUrl
                    
                    ProfileImage(
                        imageUrl = currentImageUrl,
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape),
                        backgroundColor = Color(0xFFFFCCBC),
                        iconColor = Color.Gray,
                        placeholderModifier = Modifier.padding(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF2196F3), CircleShape) // Color azul para el lapiz
                            .border(1.dp, Color.Black, CircleShape)
                            .clickable { showBottomSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.edit_profile_change_photo), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                // Name Field
                EditFieldCustom(
                    label = stringResource(R.string.editprofilescreen_nombre_5),
                    value = viewModel.name.value,
                    onValueChange = { viewModel.name.onChange(it) },
                    error = viewModel.name.error,
                    icon = Icons.Default.PersonOutline
                )
    
                Spacer(modifier = Modifier.height(24.dp))
                
                // Description Field
                EditFieldCustom(
                    label = stringResource(R.string.editprofilescreen_descripci_n_6),
                    value = viewModel.description.value,
                    onValueChange = { viewModel.description.onChange(it) },
                    error = viewModel.description.error,
                    icon = null,
                    isTextArea = true
                )
                
                Spacer(modifier = Modifier.height(24.dp))
    
                // Email Field
                EditFieldCustom(
                    label = stringResource(R.string.editprofilescreen_email_7),
                    value = viewModel.email.value,
                    onValueChange = { viewModel.email.onChange(it) },
                    error = viewModel.email.error,
                    icon = Icons.Default.MailOutline,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Ubicacion Field
                EditFieldCustom(
                    label = stringResource(R.string.editprofilescreen_ubicaci_n_8),
                    value = viewModel.location.value,
                    onValueChange = { viewModel.location.onChange(it) },
                    error = viewModel.location.error,
                    icon = Icons.Default.LocationOn // Adjust with LocationOnOutlined if available
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.editprofilescreen_preferencias_0),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Dark Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.editprofilescreen_modo_oscuro_1), fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Turquoise, checkedTrackColor = Turquoise.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications Switch
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.editprofilescreen_notificaciones_2), fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Turquoise, checkedTrackColor = Turquoise.copy(alpha = 0.5f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Language Selection
                var showLanguageMenu by remember { mutableStateOf(false) }
                
                val currentLocale = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    context.getSystemService(android.app.LocaleManager::class.java).applicationLocales.toLanguageTags().split(",").firstOrNull() ?: "es"
                } else {
                    context.resources.configuration.locales[0].language
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { showLanguageMenu = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.LightGray.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.edit_profile_language), fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    }
                    
                    Box {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentLocale.startsWith("en")) stringResource(R.string.language_english) else stringResource(R.string.language_spanish),
                                color = GrayText,
                                fontSize = 14.sp
                            )
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = GrayText)
                        }
                        
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_english)) },
                                onClick = {
                                    val lang = "en"
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        context.getSystemService(android.app.LocaleManager::class.java).applicationLocales = android.os.LocaleList.forLanguageTags(lang)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val mLocale = java.util.Locale(lang)
                                        java.util.Locale.setDefault(mLocale)
                                        val config = context.resources.configuration
                                        @Suppress("DEPRECATION")
                                        config.setLocale(mLocale)
                                        @Suppress("DEPRECATION")
                                        context.resources.updateConfiguration(config, context.resources.displayMetrics)
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                    showLanguageMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.language_spanish)) },
                                onClick = {
                                    val lang = "es"
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                        context.getSystemService(android.app.LocaleManager::class.java).applicationLocales = android.os.LocaleList.forLanguageTags(lang)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        val mLocale = java.util.Locale(lang)
                                        java.util.Locale.setDefault(mLocale)
                                        val config = context.resources.configuration
                                        @Suppress("DEPRECATION")
                                        config.setLocale(mLocale)
                                        @Suppress("DEPRECATION")
                                        context.resources.updateConfiguration(config, context.resources.displayMetrics)
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.editprofilescreen_sobre_el_perfil_3),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { setShowDeleteDialog(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFDECEA)), // Soft red bg
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE57373).copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFE57373), modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.editprofilescreen_eliminar_cuenta_4), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE57373))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = stringResource(R.string.common_select_image),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                onboardingViewModel.checkAndShowPermissionOnboarding(com.example.movilexplora.features.onboarding.PermissionType.CAMERA) {
                                    triggerCamera()
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Turquoise)
                        Text(text = stringResource(R.string.common_take_photo), style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                onboardingViewModel.checkAndShowPermissionOnboarding(com.example.movilexplora.features.onboarding.PermissionType.GALLERY) {
                                    triggerGallery()
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Turquoise)
                        Text(text = stringResource(R.string.common_choose_gallery), style = MaterialTheme.typography.bodyLarge)
                    }

                    TextButton(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(stringResource(R.string.common_cancel), color = Turquoise)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldCustom(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    isTextArea: Boolean = false,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isTextArea) 120.dp else 56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            isError = error != null,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
            singleLine = !isTextArea,
            trailingIcon = if (icon != null) {
                { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground) }
            } else null
        )
        // Sobrescribir el borde superior para poner el texto flotando fuera de OutlinedTextField por defecto.
        Box(
            modifier = Modifier
                .offset(x = 12.dp, y = (-8).dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp)
        ) {
            Text(text = label, fontSize = 12.sp, color = Turquoise)
        }
        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(start = 12.dp, top = 4.dp).align(Alignment.BottomStart))
        }
    }
}
