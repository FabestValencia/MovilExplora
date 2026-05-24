package com.example.movilexplora.features.events

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import com.google.android.gms.location.LocationServices
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movilexplora.R
import com.example.movilexplora.core.component.ImagePickerBottomSheet
import com.example.movilexplora.core.component.OnboardingPermissionDialog
import com.example.movilexplora.features.createpost.CategorySelectableItem
import com.example.movilexplora.features.onboarding.OnboardingViewModel
import com.example.movilexplora.features.onboarding.PermissionType
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon
import java.io.File
import java.util.Calendar

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "event_photo_",
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
fun CreateEditEventScreen(
    eventId: String? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: CreateEventViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var category by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val onboardingState by onboardingViewModel.state.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(2.1734, 41.3851))
            zoom(12.0)
        }
    }

    val selectedPinTitle = stringResource(R.string.create_post_selected_location)
    val selectedPinGeoJson = remember(lat, lon, selectedPinTitle) {
        if (lat != null && lon != null) {
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [$lon, $lat]
                  },
                  "properties": {
                    "title": "$selectedPinTitle",
                    "category": "Selected"
                  }
                }
              ]
            }
            """.trimIndent()
        } else {
            """
            {
              "type": "FeatureCollection",
              "features": []
            }
            """.trimIndent()
        }
    }

    val selectedPinSourceState = rememberGeoJsonSourceState {
        data = GeoJSONData(selectedPinGeoJson)
    }

    LaunchedEffect(selectedPinGeoJson) {
        selectedPinSourceState.data = GeoJSONData(selectedPinGeoJson)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    mapViewportState.setCameraOptions {
                        center(Point.fromLngLat(location.longitude, location.latitude))
                        zoom(14.0)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude
                    mapViewportState.setCameraOptions {
                        center(Point.fromLngLat(location.longitude, location.latitude))
                        zoom(14.0)
                    }
                }
            }
        } else {
            onboardingViewModel.checkAndShowPermissionOnboarding(PermissionType.LOCATION) {
                requestPermissionLauncher.launch(permissions)
            }
        }
    }
    
    var showBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState()
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val calendar = Calendar.getInstance()

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> startDate = "$dayOfMonth/${month + 1}/$year" },
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> endDate = "$dayOfMonth/${month + 1}/$year" },
        calendar[Calendar.YEAR],
        calendar[Calendar.MONTH],
        calendar[Calendar.DAY_OF_MONTH]
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            imageUri = tempCameraUri
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
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
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
        OnboardingPermissionDialog(
            permissionType = permissionType,
            onChoiceMade = { always ->
                onboardingViewModel.onPermissionChoice(permissionType, always)
                if (permissionType == PermissionType.CAMERA) triggerCamera()
                else if (permissionType == PermissionType.GALLERY) triggerGallery()
            },
            onDismiss = {
                onboardingViewModel.dismissPermissionDialog()
            }
        )
    }

    val isEditing = (eventId != null) && (eventId != "{eventId}")

    val eventToEdit by viewModel.eventToEdit.collectAsState()
    val showAiRecommendationDialog by viewModel.showAiRecommendationDialog.collectAsState()
    val pendingRecommendation by viewModel.pendingRecommendation.collectAsState()
    val aiRecommendationReason by viewModel.aiRecommendationReason.collectAsState()
    val publishResult by viewModel.publishResult.collectAsState()
    val selectedCategoryFromAi by viewModel.selectedCategoryFromAi.collectAsState()

    LaunchedEffect(selectedCategoryFromAi) {
        selectedCategoryFromAi?.let {
            category = it
            viewModel.clearCategoryFromAi()
        }
    }

    LaunchedEffect(publishResult) {
        when (publishResult) {
            is com.example.movilexplora.core.utils.RequestResult.Success -> {
                onSaveSuccess()
                viewModel.resetResult()
            }
            is com.example.movilexplora.core.utils.RequestResult.Failure -> {
                // Error toast can be added here if needed
            }
            else -> {}
        }
    }

    LaunchedEffect(isEditing, eventId) {
        if (isEditing) {
            viewModel.loadEvent(eventId)
        }
    }

    LaunchedEffect(eventToEdit) {
        eventToEdit?.let { event ->
            title = event.title
            description = event.description
            location = event.location
            category = event.category
            startDate = event.date
            endDate = event.endDate
            lat = event.latitude
            lon = event.longitude
            
            // Mover la cámara a la ubicación del evento cargado
            mapViewportState.setCameraOptions {
                center(Point.fromLngLat(event.longitude, event.latitude))
                zoom(14.0)
            }

            if (event.imageUrl.isNotEmpty()) {
                try {
                    imageUri = event.imageUrl.toUri()
                } catch (_: Exception) {
                    // Ignore parsing error
                }
            }
        }
    }

    // Diálogo de Recomendación de IA
    if (showAiRecommendationDialog) {
        pendingRecommendation?.let { recommendation ->
            val categoryColor = getCategoryColor(recommendation.category)
            val categoryIcon = getCategoryIcon(recommendation.category)
            
            AlertDialog(
                onDismissRequest = { viewModel.dismissRecommendation() },
                confirmButton = {
                    Button(
                        onClick = { 
                            category = recommendation.category
                            viewModel.acceptRecommendation() 
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = categoryColor)
                    ) {
                        Text(stringResource(R.string.common_ok), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.dismissRecommendation() }
                    ) {
                        Text(stringResource(R.string.common_dismiss), color = GrayText, fontWeight = FontWeight.Medium)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = categoryColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.create_post_ai_recommendation_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Category Badge
                        Surface(
                            shape = CircleShape,
                            color = categoryColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp),
                            border = BorderStroke(2.dp, categoryColor)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = recommendation.category,
                                    tint = categoryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = recommendation.category.replaceFirstChar { it.uppercase() },
                            color = categoryColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        // Reason Container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.create_post_ai_why),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = categoryColor,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = recommendation.reason,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) stringResource(R.string.events_edit_event) else stringResource(R.string.eventsscreen_crear_evento_4),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.createediteventscreen_back_14), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Image Placeholder with Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .border(2.dp, Turquoise.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showBottomSheet = true },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Event Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = stringResource(R.string.createediteventscreen_a_adir_imagen_15),
                            tint = Turquoise,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = stringResource(R.string.createediteventscreen_a_adir_imagen_del_evento_0), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(text = stringResource(R.string.createediteventscreen_formato_jpg_o_png__m_x__5mb_1), color = GrayText, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Form Fields
            EventInputField(
                label = stringResource(R.string.createediteventscreen_t_tulo_del_evento_6),
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.createediteventscreen_ej__festival_de_gastronom_a_10)
            )

            EventInputField(
                label = stringResource(R.string.createediteventscreen_descripci_n_7),
                value = description,
                onValueChange = { description = it },
                placeholder = stringResource(R.string.createediteventscreen_describe_los_detalles_del_even_11),
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.createediteventscreen_ubicaci_n_2), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = stringResource(R.string.createediteventscreen_pin_interactivo_3), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    onMapClickListener = { point ->
                        lat = point.latitude()
                        lon = point.longitude()
                        location = "Lat: %.4f, Lon: %.4f".format(lat, lon)
                        true
                    }
                ) {
                    CircleLayer(
                        sourceState = selectedPinSourceState,
                        layerId = "selected-pin-layer"
                    ) {
                        circleRadius = DoubleValue(10.0)
                        circleColor = ColorValue(Expression.color(Color(0xFFFFAB00).toArgb()))
                        circleStrokeWidth = DoubleValue(3.0)
                        circleStrokeColor = ColorValue(Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.createediteventscreen_categor_a_4), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    if (aiRecommendationReason != null) {
                        Text(
                            text = "IA: $aiRecommendationReason",
                            fontSize = 11.sp,
                            color = Turquoise,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                val isRecommending by viewModel.isRecommendingCategory.collectAsState()

                TextButton(
                    onClick = { viewModel.recommendCategory(description) },
                    enabled = !isRecommending && description.isNotBlank()
                ) {
                    if (isRecommending) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Turquoise)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Turquoise)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.create_post_suggest_ai), fontSize = 12.sp, color = Turquoise, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(text = stringResource(R.string.createediteventscreen_elige_la_categor_a_que_mejor_d_5), fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            data class CategoryData(val key: String, val name: String, val desc: String)
            
            val categories = listOf(
                CategoryData("Gastronomia", "Gastronomía", stringResource(R.string.cat_gastronomy_desc_event)),
                CategoryData("Cultura", "Cultura", stringResource(R.string.cat_culture_desc_event)),
                CategoryData("Naturaleza", "Naturaleza", stringResource(R.string.cat_nature_desc_event)),
                CategoryData("Entretenimiento", "Entretenimiento", stringResource(R.string.cat_entertainment_desc_event)),
                CategoryData("Historia", "Historia", stringResource(R.string.cat_history_desc_event))
            )

            categories.forEach { cat ->
                CategorySelectableItem(
                    name = cat.name,
                    description = cat.desc,
                    icon = getCategoryIcon(cat.key),
                    iconColor = getCategoryColor(cat.key),
                    isSelected = category == cat.key,
                    onSelect = { category = cat.key }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.clickable { startDatePickerDialog.show() }) {
                EventInputField(
                    label = stringResource(R.string.createediteventscreen_fecha_de_inicio_8),
                    value = startDate,
                    onValueChange = {},
                    placeholder = stringResource(R.string.createediteventscreen_mm_dd_yyyy_12),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                    },
                    readOnly = true,
                    enabled = false // Disable to let the Box intercept clicks
                )
            }

            Box(modifier = Modifier.clickable { endDatePickerDialog.show() }) {
                EventInputField(
                    label = stringResource(R.string.createediteventscreen_fecha_de_fin_9),
                    value = endDate,
                    onValueChange = {},
                    placeholder = stringResource(R.string.createediteventscreen_mm_dd_yyyy_12),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                    },
                    readOnly = true,
                    enabled = false // Disable to let the Box intercept clicks
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.publishEvent(
                        title = title,
                        description = description,
                        location = location,
                        category = category,
                        startDate = startDate,
                        endDate = endDate,
                        imageUri = imageUri,
                        latitude = lat ?: 0.0,
                        longitude = lon ?: 0.0
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = title.isNotBlank() && startDate.isNotBlank() && endDate.isNotBlank() && imageUri != null && publishResult !is com.example.movilexplora.core.utils.RequestResult.Loading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Turquoise,
                    disabledContainerColor = Turquoise.copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.5f)
                )
            ) {
                if (publishResult is com.example.movilexplora.core.utils.RequestResult.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEditing) stringResource(R.string.common_save_changes) else stringResource(R.string.events_publish_event),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState
            ) {
                ImagePickerBottomSheet(
                    onCameraClick = {
                        showBottomSheet = false
                        onboardingViewModel.checkAndShowPermissionOnboarding(PermissionType.CAMERA) {
                            triggerCamera()
                        }
                    },
                    onGalleryClick = {
                        showBottomSheet = false
                        onboardingViewModel.checkAndShowPermissionOnboarding(PermissionType.GALLERY) {
                            triggerGallery()
                        }
                    },
                    onDismiss = { showBottomSheet = false }
                )
            }
        }
    }
}

@Composable
fun EventInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color(0xFFA0AAB4)) },
            singleLine = singleLine,
            readOnly = readOnly,
            enabled = enabled,
            trailingIcon = trailingIcon,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                cursorColor = Turquoise,
                disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                disabledTextColor = MaterialTheme.colorScheme.onBackground,
                disabledPlaceholderColor = Color(0xFFA0AAB4)
            )
        )
    }
}
