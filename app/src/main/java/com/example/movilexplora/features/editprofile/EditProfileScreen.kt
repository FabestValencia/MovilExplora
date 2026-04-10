package com.example.movilexplora.features.editprofile

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.features.profile.DeleteAccountDialog
import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

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
    val scope = rememberCoroutineScope()

    val updateResult by viewModel.updateResult.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
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

    LaunchedEffect(updateResult) {
        if (updateResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            onUpdateSuccess()
            viewModel.resetResult()
        }
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                // Logic to delete account
                showDeleteDialog = false
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
                    TextButton(onClick = { viewModel.updateProfile() }, enabled = viewModel.isFormValid) {
                        Text(stringResource(R.string.edit_profile_save), color = Turquoise, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                    if (photoUri != null) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(100.dp)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .border(2.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFFFFCCBC)) // Fondo naranja claro como en la imagen
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(12.dp),
                                tint = Color.Gray
                            )
                        }
                    }
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
                    onClick = { showDeleteDialog = true },
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
                        text = "Seleccionar Imagen",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Turquoise)
                        Text(text = "Tomar Foto", style = MaterialTheme.typography.bodyLarge)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showBottomSheet = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Turquoise)
                        Text(text = "Elegir de Galería", style = MaterialTheme.typography.bodyLarge)
                    }

                    TextButton(
                        onClick = { showBottomSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Cancelar", color = Turquoise)
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
