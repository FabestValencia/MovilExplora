# Manejo de Permisos en Android

## Introducción

Una aplicación móvil a menudo necesita acceder a recursos sensibles del dispositivo, como la cámara, el micrófono o la ubicación del usuario. Para proteger la privacidad del usuario, Android implementa un sistema de permisos que requiere que las aplicaciones soliciten permiso explícito para acceder a estos recursos.

---

## Funciones Principales

Para manejar los permisos en Android, se utilizan varias funciones y clases proporcionadas por el framework de Android. A continuación, se describen algunas de las más importantes:

| Función/Clase | Descripción |
|---------------|-------------|
| `ActivityResultContracts.RequestPermission` | Facilita la solicitud de permisos en tiempo de ejecución. Esta clase forma parte del sistema de resultados de actividades y permite solicitar permisos de manera sencilla y manejar la respuesta del usuario. |
| `ActivityResultContracts.GetContent` | Permite a las aplicaciones solicitar contenido del dispositivo. Esto es útil para simplificar el acceso a archivos y otros recursos una vez que los permisos han sido concedidos. |
| `ContextCompat.checkSelfPermission()` | Se utiliza para verificar si la aplicación ya tiene un permiso específico concedido. Esta función devuelve un valor que indica si el permiso está concedido o denegado. |

---

## Flujo de Trabajo para Solicitar Permisos

El flujo de trabajo típico para solicitar permisos en una aplicación Android es el siguiente:

1.  **Verificar el Permiso:** Antes de acceder a un recurso sensible, la aplicación debe verificar si ya tiene el permiso necesario utilizando `ContextCompat.checkSelfPermission()`.
2.  **Solicitar el Permiso:** Si el permiso no ha sido concedido, la aplicación debe solicitarlo utilizando `ActivityResultContracts.RequestPermission`.
3.  **Manejar la Respuesta:** La aplicación debe manejar la respuesta del usuario, ya sea que haya concedido o denegado el permiso.
4.  **Acceder al Recurso:** Para obtener contenido del dispositivo, la aplicación puede utilizar `ActivityResultContracts.GetContent()` para solicitar archivos sin preocuparse por los permisos subyacentes ya que se asume que el usuario ha concedido el permiso necesario.
5.  **Manejo de Denegaciones:** Si el permiso es denegado, la aplicación debe manejar esta situación adecuadamente, informando al usuario y posiblemente deshabilitando ciertas funcionalidades.

---

## Diagrama de Solicitud de Permisos

El siguiente diagrama ilustra el flujo de trabajo para solicitar permisos en una aplicación Android:

```
┌─────────────────────────────────────────────────────────────────┐
│              App intenta acceder al recurso                     │
│         Cámara, ubicación, micrófono...                         │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│        ¿Tiene el permiso concedido?                             │
│     ContextCompat.checkSelfPermission()                         │
└───────────────────────────┬─────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
        ┌───────┐                       ┌───────────┐
        │  Sí   │                       │    No     │
        └───┬───┘                       └─────┬─────┘
            │                                 │
            ▼                                 ▼
┌──────────────────────┐        ┌──────────────────────────────┐
│   Acceder al         │        │  Solicitar permiso al        │
│   recurso            │        │  sistema                     │
│                      │        │  ActivityResultContracts.    │
│                      │        │  RequestPermission           │
└──────────────────────┘        └─────────────┬────────────────┘
                                              │
                                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              Diálogo del sistema Android                        │
│                  El usuario decide                              │
└───────────────────────────┬─────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
        ┌───────────┐                   ┌───────────┐
        │ Concedido │                   │  Denegado │
        └─────┬─────┘                   └─────┬─────┘
              │                               │
              ▼                               ▼
┌──────────────────────┐        ┌──────────────────────────────┐
│  Ejecutar acción     │        │  Informar al usuario         │
│  con el recurso      │        │                              │
│  Cámara o galería    │        │  Funcionalidades             │
│  disponibles         │        │  deshabilitadas              │
└──────────────────────┘        └──────────────────────────────┘

Desde Android 6.0 (API 23): permisos normales se conceden automáticamente;
los peligrosos (cámara, ubicación...) requieren aprobación explícita en runtime.
```

---

## Permisos Comunes

Algunos de los permisos más comunes que las aplicaciones pueden solicitar incluyen:

| Recurso | Permiso |
|---------|---------|
| Cámara | `android.permission.CAMERA` |
| Ubicación | `android.permission.ACCESS_FINE_LOCATION` y `android.permission.ACCESS_COARSE_LOCATION` |
| Almacenamiento | `android.permission.READ_EXTERNAL_STORAGE` y `android.permission.WRITE_EXTERNAL_STORAGE` |
| Micrófono | `android.permission.RECORD_AUDIO` |
| Contactos | `android.permission.READ_CONTACTS` y `android.permission.WRITE_CONTACTS` |
| SMS | `android.permission.SEND_SMS` y `android.permission.RECEIVE_SMS` |
| Calendario | `android.permission.READ_CALENDAR` y `android.permission.WRITE_CALENDAR` |
| Internet | `android.permission.INTERNET` |
| Redes | `android.permission.ACCESS_NETWORK_STATE` |

> ⚠️ **Importante:** Desde Android 6.0 (API nivel 23), los permisos se dividen en dos categorías: **permisos normales** y **permisos peligrosos**. Los permisos normales se conceden automáticamente, mientras que los permisos peligrosos requieren la aprobación explícita del usuario en tiempo de ejecución.

---

## Captura de Foto con Permisos y Selección de Imagen

Con base en lo explicado anteriormente, a continuación se presenta un ejemplo completo de una pantalla de perfil que maneja permisos para acceder a la cámara y la galería de imágenes utilizando Jetpack Compose.

### 1. Crear la Pantalla de Perfil

Modifique o cree el archivo `ProfileScreen.kt` en el paquete `com.example.demoapp.features.user.profile` con el siguiente código:

```kotlin
package com.example.demoapp.features.user.profile

// Importaciones necesarias...

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    padding: PaddingValues, // Padding del Scaffold
    userId: String = "1", // ID de usuario de ejemplo
    snackbarHostState: SnackbarHostState, // Para mostrar mensajes, si pertenece a un Scaffold
    viewModel: ProfileViewModel = hiltViewModel() // ViewModel inyectado con Hilt
) {
    // El contexto se refiere a la actividad o aplicación actual
    val context = LocalContext.current
    // Coroutine scope para lanzar tareas asíncronas, como mostrar Snackbars
    val scope = rememberCoroutineScope()
    
    // Estados del ViewModel
    val user by viewModel.user.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val photoUri by viewModel.photoUri.collectAsState()
    val updateResult by viewModel.updateResult.collectAsState()
    
    // Estado del Bottom Sheet
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    // URI temporal para la foto de cámara
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher para seleccionar de galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Usar el URI seleccionado de la galería
        uri?.let { viewModel.onPhotoSelected(it) }
    }
    
    // Launcher para tomar foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // Usar el URI temporal para la foto tomada
            tempCameraUri?.let { viewModel.onPhotoSelected(it) }
        }
    }
    
    // Launcher para pedir permiso de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Crear URI temporal para la foto
            tempCameraUri = createTempImageUri(context)
            // Lanzar cámara con el URI temporal, finalmente la foto se maneja en cameraLauncher
            tempCameraUri?.let { cameraLauncher.launch(it) }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Se necesita permiso de cámara para tomar foto")
            }
        }
    }
    
    // Cargar usuario al iniciar
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    // Mostrar resultado de actualización
    LaunchedEffect(updateResult) {
        when (updateResult) {
            is RequestResult.Success -> {
                snackbarHostState.showSnackbar(
                    (updateResult as RequestResult.Success).message
                )
                viewModel.clearResult()
            }
            is RequestResult.Failure -> {
                snackbarHostState.showSnackbar(
                    (updateResult as RequestResult.Failure).errorMessage
                )
                viewModel.clearResult()
            }
            null -> {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Imagen de perfil circular
            ProfileImage(
                photoUri = photoUri,
                isEditMode = isEditMode,
                onEditClick = { showBottomSheet = true }
            )
            
            // Email (solo lectura)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = user?.email ?: "",
                onValueChange = {},
                label = { Text(text = stringResource(R.string.login_email_label)) },
                readOnly = true,
                enabled = false
            )
            
            // Nombre
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.name.value,
                onValueChange = { viewModel.name.onChange(it) },
                label = { Text(text = stringResource(R.string.txt_name)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null
                    )
                },
                isError = viewModel.name.error != null,
                supportingText = viewModel.name.error?.let { error ->
                    { Text(text = error) }
                },
                enabled = isEditMode,
                singleLine = true
            )
            
            // Ciudad (Dropdown)
            DropdownMenu(
                label = stringResource(R.string.txt_city),
                list = viewModel.cities,
                icon = Icons.Outlined.Place,
                onValueChange = { viewModel.city.onChange(it) },
                enabled = isEditMode,
                value = viewModel.city.value
            )
            
            // Dirección
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.address.value,
                onValueChange = { viewModel.address.onChange(it) },
                label = { Text(text = stringResource(R.string.txt_address)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Place,
                        contentDescription = null
                    )
                },
                isError = viewModel.address.error != null,
                supportingText = viewModel.address.error?.let { error ->
                    { Text(text = error) }
                },
                enabled = isEditMode,
                singleLine = true
            )
            
            // Teléfono
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = viewModel.phone.value,
                onValueChange = { viewModel.phone.onChange(it) },
                label = { Text(text = stringResource(R.string.txt_phone)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null
                    )
                },
                isError = viewModel.phone.error != null,
                supportingText = viewModel.phone.error?.let { error ->
                    { Text(text = error) }
                },
                enabled = isEditMode,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            
            // Botones de acción
            if (isEditMode) {
                // Guardar cambios
                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.isFormValid
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.txt_save))
                }
                
                // Cancelar edición
                OutlinedButton(
                    onClick = { viewModel.cancelEdit() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.txt_cancel))
                }
            } else {
                Button(
                    onClick = { viewModel.toggleEditMode() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = stringResource(R.string.txt_edit))
                }
            }
        }
    }
    
    // Bottom Sheet para elegir cámara o galería
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            ImagePickerBottomSheet(
                onCameraClick = {
                    showBottomSheet = false
                    // Solicitar permiso de cámara y luego lanzar cámara
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                onGalleryClick = {
                    showBottomSheet = false
                    // Lanzar selector de galería
                    galleryLauncher.launch("image/*")
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}
```

> ⚠️ **Importante:** Asegúrese de agregar los textos correspondientes en el archivo `strings.xml` para las cadenas utilizadas en la interfaz de usuario.

### 2. Funciones Auxiliares

Agregue las siguientes funciones auxiliares en el mismo archivo o en un archivo separado según su preferencia:

**ProfileImage:** Muestra la imagen de perfil del usuario.

```kotlin
@Composable
private fun ProfileImage(
    photoUri: Uri?,
    isEditMode: Boolean,
    onEditClick: () -> Unit
) {
    val imageSize = 140.dp
    Box(
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            // Mostrar la imagen seleccionada desde el URI
            AsyncImage(
                model = photoUri,
                contentDescription = stringResource(R.string.profile_image_description),
                modifier = Modifier
                    .size(imageSize)
                    .clip(CircleShape)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .then(
                        if (isEditMode) {
                            Modifier.clickable { onEditClick() }
                        } else {
                            Modifier
                        }
                    ),
                contentScale = ContentScale.Crop
            )
        } else {
            // Mostrar ícono predeterminado si no hay imagen
            Icon(
                imageVector = Icons.Rounded.AccountCircle,
                contentDescription = stringResource(R.string.profile_image_description),
                modifier = Modifier
                    .size(imageSize)
                    .then(
                        if (isEditMode) {
                            Modifier.clickable { onEditClick() }
                        } else {
                            Modifier
                        }
                    ),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Botón de cámara cuando está en modo edición
        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = stringResource(R.string.change_photo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
```

**ImagePickerBottomSheet:** Define el contenido del bottom sheet.

```kotlin
@Composable
private fun ImagePickerBottomSheet(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.select_image),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )
        
        ImagePickerOption(
            icon = Icons.Outlined.PhotoCamera,
            text = stringResource(R.string.take_photo),
            onClick = onCameraClick
        )
        
        ImagePickerOption(
            icon = Icons.Outlined.Image,
            text = stringResource(R.string.choose_from_gallery),
            onClick = onGalleryClick
        )
        
        TextButton(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}
```

**ImagePickerOption:** Define una opción individual en el bottom sheet.

```kotlin
@Composable
private fun ImagePickerOption(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
```

**createTempImageUri:** Crea un URI temporal para almacenar la foto tomada con la cámara.

```kotlin
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
```

### 3. Configurar FileProvider

Para que la función `createTempImageUri` funcione correctamente, es necesario configurar un FileProvider en el archivo `AndroidManifest.xml`. Agregue el siguiente proveedor dentro de la etiqueta `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths"/>
</provider>
```

### 4. Agregar Permisos en AndroidManifest.xml

Agregue el permiso de cámara en el archivo `AndroidManifest.xml`, esto es necesario para solicitar acceso a la cámara del dispositivo:

```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-feature android:name="android.hardware.camera" android:required="false"/>
```

### 5. Definir Rutas de FileProvider

Cree un archivo XML llamado `file_paths.xml` en la carpeta `res/xml/` con el siguiente contenido:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="."/>
</paths>
```

> ⚠️ **Importante:** Este archivo define las rutas accesibles a través del FileProvider, en este caso, la carpeta de caché de la aplicación.

### 6. ViewModel

Cree o modifique el `ProfileViewModel` para manejar la lógica de carga y actualización del usuario, así como el manejo de la foto seleccionada:

```kotlin
package com.example.demoapp.features.user.profile

// Importaciones necesarias...

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val resources: ResourceProvider // Para acceder a recursos de strings
) : ViewModel() {
    
    private val _updateResult = MutableStateFlow<RequestResult?>(null)
    val updateResult: StateFlow<RequestResult?> = _updateResult.asStateFlow()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()
    
    val cities = listOf("Ciudad 1", "Ciudad 2", "Ciudad 3")
    
    val name = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_name_required)
            value.length > 30 -> resources.getString(R.string.error_name_length)
            else -> null
        }
    }
    
    val city = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_city_required) else null
    }
    
    val address = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_address_required) else null
    }
    
    val phone = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_phone_required)
            !value.matches(Regex("^[0-9]{10}$")) -> resources.getString(R.string.error_phone_invalid)
            else -> null
        }
    }
    
    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()
    
    val isFormValid: Boolean
        get() = name.isValid && address.isValid && city.isValid && phone.isValid
    
    // Cargar usuario desde el repositorio
    fun loadUser(userId: String) {
        // Se recupera el usuario desde el repositorio
        val foundUser = repository.findById(userId)
        // Si se encuentra, se actualizan los campos
        foundUser?.let { user ->
            _user.value = user
            // Actualizar campos del formulario con los datos del usuario
            name.onChange(user.name)
            city.onChange(user.city)
            address.onChange(user.address)
            phone.onChange(user.phoneNumber ?: "")
            user.profilePictureUrl?.let { _photoUri.value = it.toUri() }
        }
    }
    
    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }
    
    fun onPhotoSelected(uri: Uri?) {
        _photoUri.value = uri
    }
    
    // Guardar cambios del perfil en el repositorio
    fun saveChanges() {
        if (isFormValid) {
            // Actualizar el usuario en el repositorio
            _user.value?.let { currentUser ->
                val updatedUser = currentUser.copy(
                    name = name.value,
                    city = city.value,
                    address = address.value,
                    phoneNumber = phone.value,
                    profilePictureUrl = _photoUri.value?.toString()
                )
                repository.update(updatedUser)
                _user.value = updatedUser
                _updateResult.value = RequestResult.Success(resources.getString(R.string.profile_update_success))
                _isEditMode.value = false
            }
        }
    }
    
    // Cancelar edición y restaurar valores originales
    fun cancelEdit() {
        _user.value?.let { user ->
            name.onChange(user.name)
            city.onChange(user.city)
            address.onChange(user.address)
            phone.onChange(user.phone ?: "")
            _photoUri.value = user.image?.toUri()
        }
        _isEditMode.value = false
    }
    
    fun clearResult() {
        _updateResult.value = null
    }
}
```

> ⚠️ **Importante:** Asegúrese de agregar los textos correspondientes en el archivo `strings.xml` para las cadenas utilizadas en el ViewModel. Además, adapte el repositorio y el modelo de usuario según la estructura de su proyecto.

### 7. Probar la Aplicación

Ejecute la aplicación en un dispositivo o emulador Android. Navegue a la pantalla de perfil y pruebe las funcionalidades de edición, incluyendo la captura de fotos con la cámara y la selección de imágenes desde la galería. Asegúrese de que los permisos se soliciten correctamente y que las imágenes se actualicen en el perfil del usuario.

---

## Actividad Práctica

1.  **Subida de imagen a servidor remoto:**
    Investigue cómo subir la imagen seleccionada a un servidor remoto o servicio de almacenamiento en la nube (como Firebase Storage, Cloudinary o AWS S3) al momento de guardar los cambios del perfil. Implemente esta funcionalidad en el `ProfileViewModel` y asegúrese de manejar los posibles errores durante la carga de la imagen.

2.  **Uso de Coroutines y Scope:**
    Profundizar sobre el concepto de scope en Kotlin Coroutines y cómo se utiliza en el contexto de Jetpack Compose para manejar tareas asíncronas, como la solicitud de permisos y la carga de imágenes.

---

> 📚 **Recursos adicionales:**
> - [Documentación oficial de Android - Permisos](https://developer.android.com/guide/topics/permissions/overview)
> - [Jetpack Compose - Permissions](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)