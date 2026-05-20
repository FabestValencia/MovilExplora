package com.example.movilexplora.features.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.component.BottomNavigationBar
import com.example.movilexplora.core.component.OnboardingPermissionDialog
import com.example.movilexplora.core.utils.toGeoJson
import com.example.movilexplora.features.onboarding.OnboardingViewModel
import com.example.movilexplora.features.onboarding.PermissionType
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon
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


@Composable
fun MapScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToEventDetail: (String) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val onboardingState by onboardingViewModel.state.collectAsState()
    
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(2.1734, 41.3851)) // Default Barcelona
            zoom(12.0)
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateUserLocation(location.latitude, location.longitude)
                    mapViewportState.setCameraOptions {
                        center(Point.fromLngLat(location.longitude, location.latitude))
                        zoom(14.0)
                    }
                } else {
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        com.google.android.gms.tasks.CancellationTokenSource().token
                    ).addOnSuccessListener { currentLocation ->
                        currentLocation?.let {
                            viewModel.updateUserLocation(it.latitude, it.longitude)
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(it.longitude, it.latitude))
                                zoom(14.0)
                            }
                        }
                    }
                }
            }
        }
    }

    val triggerPermissionRequest = {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (permissions.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateUserLocation(location.latitude, location.longitude)
                    mapViewportState.setCameraOptions {
                        center(Point.fromLngLat(location.longitude, location.latitude))
                        zoom(14.0)
                    }
                } else {
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        com.google.android.gms.tasks.CancellationTokenSource().token
                    ).addOnSuccessListener { currentLocation ->
                        currentLocation?.let {
                            viewModel.updateUserLocation(it.latitude, it.longitude)
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(it.longitude, it.latitude))
                                zoom(14.0)
                            }
                        }
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(permissions)
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
                    viewModel.updateUserLocation(location.latitude, location.longitude)
                    mapViewportState.setCameraOptions {
                        center(Point.fromLngLat(location.longitude, location.latitude))
                        zoom(14.0)
                    }
                } else {
                    fusedLocationClient.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        com.google.android.gms.tasks.CancellationTokenSource().token
                    ).addOnSuccessListener { currentLocation ->
                        currentLocation?.let {
                            viewModel.updateUserLocation(it.latitude, it.longitude)
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(it.longitude, it.latitude))
                                zoom(14.0)
                            }
                        }
                    }
                }
            }
        } else {
            onboardingViewModel.checkAndShowPermissionOnboarding(PermissionType.LOCATION) {
                triggerPermissionRequest()
            }
        }
    }

    // Permission Dialog
    onboardingState.showPermissionDialog?.let { permissionType ->
        OnboardingPermissionDialog(
            permissionType = permissionType,
            onChoiceMade = { always ->
                onboardingViewModel.onPermissionChoice(permissionType, always)
                triggerPermissionRequest()
            },
            onDismiss = {
                onboardingViewModel.dismissPermissionDialog()
            }
        )
    }
    val geoJsonSourceState = rememberGeoJsonSourceState {
        data = GeoJSONData(state.filteredFeatures.toGeoJson())
    }

    LaunchedEffect(state.filteredFeatures) {
        geoJsonSourceState.data = GeoJSONData(state.filteredFeatures.toGeoJson())
    }

    val userLocation by viewModel.userLocation.collectAsState()

    val userLocationGeoJson = remember(userLocation) {
        val loc = userLocation
        if (loc != null) {
            """
            {
              "type": "FeatureCollection",
              "features": [
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [${loc.second}, ${loc.first}]
                  },
                  "properties": {
                    "title": "Mi Ubicación",
                    "category": "User"
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

    val userLocationSourceState = rememberGeoJsonSourceState {
        data = GeoJSONData(userLocationGeoJson)
    }

    LaunchedEffect(userLocationGeoJson) {
        userLocationSourceState.data = GeoJSONData(userLocationGeoJson)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState,
            onMapClickListener = { point ->
                viewModel.onDismissDetail()
                val threshold = 0.005 
                val nearest = state.filteredFeatures.minByOrNull {
                    val dx = it.longitude - point.longitude()
                    val dy = it.latitude - point.latitude()
                    dx * dx + dy * dy
                }?.takeIf {
                    val dx = it.longitude - point.longitude()
                    val dy = it.latitude - point.latitude()
                    (dx * dx + dy * dy) < threshold * threshold
                }
                
                if (nearest != null) {
                    viewModel.onFeatureClick(nearest)
                }
                true
            },
            onMapLongClickListener = { point ->
                val threshold = 0.005
                val nearest = state.filteredFeatures.minByOrNull {
                    val dx = it.longitude - point.longitude()
                    val dy = it.latitude - point.latitude()
                    dx * dx + dy * dy
                }?.takeIf {
                    val dx = it.longitude - point.longitude()
                    val dy = it.latitude - point.latitude()
                    (dx * dx + dy * dy) < threshold * threshold
                }
                
                if (nearest != null) {
                    if (nearest is MapFeature.PostFeature) {
                        onNavigateToDetail(nearest.id)
                    } else if (nearest is MapFeature.EventFeature) {
                        onNavigateToEventDetail(nearest.id)
                    }
                }
                true
            }
        ) {
            CircleLayer(
                sourceState = geoJsonSourceState,
                layerId = "features-layer"
            ) {
                circleRadius = DoubleValue(
                    Expression.interpolate(
                        Expression.linear(),
                        Expression.zoom(),
                        Expression.literal(10.0), Expression.literal(8.0),
                        Expression.literal(15.0), Expression.literal(12.0)
                    )
                )
                circleColor = ColorValue(
                    Expression.match(
                        Expression.get("category"),
                        Expression.literal("Gastronomía"), Expression.color(getCategoryColor("Gastronomía").toArgb()),
                        Expression.literal("Cultura"), Expression.color(getCategoryColor("Cultura").toArgb()),
                        Expression.literal("Naturaleza"), Expression.color(getCategoryColor("Naturaleza").toArgb()),
                        Expression.literal("Entretenimiento"), Expression.color(getCategoryColor("Entretenimiento").toArgb()),
                        Expression.literal("Historia"), Expression.color(getCategoryColor("Historia").toArgb()),
                        Expression.color(Turquoise.toArgb())
                    )
                )
                circleStrokeWidth = DoubleValue(2.0)
                circleStrokeColor = ColorValue(Color.White)
            }

            CircleLayer(
                sourceState = userLocationSourceState,
                layerId = "user-location-layer"
            ) {
                circleRadius = DoubleValue(12.0)
                circleColor = ColorValue(Expression.color(Color(0xFFFFAB00).toArgb()))
                circleStrokeWidth = DoubleValue(3.0)
                circleStrokeColor = ColorValue(Color.White)
            }
        }

        // Overlay Components
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White),
                placeholder = { Text(stringResource(R.string.map_search_placeholder), fontSize = 14.sp, color = GrayText.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = GrayText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Filters
            FilterChipsRow(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.onFilterSelected(it) }
            )
        }

        // Floating Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = if (state.selectedFeature == null) 20.dp else 340.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapControlAction(icon = Icons.Default.MyLocation) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            viewModel.updateUserLocation(location.latitude, location.longitude)
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(location.longitude, location.latitude))
                                zoom(14.0)
                            }
                        } else {
                            fusedLocationClient.getCurrentLocation(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                com.google.android.gms.tasks.CancellationTokenSource().token
                            ).addOnSuccessListener { currentLocation ->
                                currentLocation?.let {
                                    viewModel.updateUserLocation(it.latitude, it.longitude)
                                    mapViewportState.setCameraOptions {
                                        center(Point.fromLngLat(it.longitude, it.latitude))
                                        zoom(14.0)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }
            MapControlAction(icon = Icons.Default.Add) {
                mapViewportState.setCameraOptions {
                    zoom((mapViewportState.cameraState?.zoom ?: 12.0) + 1.0)
                }
            }
            MapControlAction(icon = Icons.Default.Remove) {
                mapViewportState.setCameraOptions {
                    zoom((mapViewportState.cameraState?.zoom ?: 12.0) - 1.0)
                }
            }
        }

        // Preview Card or Create Button
        if (state.selectedFeature != null) {
            FeaturePreviewCard(
                feature = state.selectedFeature!!,
                onDetailClick = {
                    val feature = state.selectedFeature!!
                    if (feature is MapFeature.PostFeature) {
                        onNavigateToDetail(feature.id)
                    } else if (feature is MapFeature.EventFeature) {
                        onNavigateToEventDetail(feature.id)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            )
        } else {
            // Create Post Button
            Button(
                onClick = onNavigateToCreatePost,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .height(56.dp)
                    .width(220.dp)
                    .shadow(8.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = stringResource(R.string.mapscreen_crear_0), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(text = stringResource(R.string.mapscreen_publicaci_n_1), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FeaturePreviewCard(
    feature: MapFeature,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 32.dp, bottomEnd = 32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            ) {
                Text(stringResource(R.string.common_location_image), modifier = Modifier.align(Alignment.Center))
                
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryColor = getCategoryColor(feature.category)
                    Surface(
                        color = categoryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = feature.category,
                            color = categoryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (feature is MapFeature.PostFeature) stringResource(R.string.map_feature_post) else stringResource(R.string.map_feature_event),
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = feature.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDetailClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.mapscreen_ver_m_s_5), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun getTranslatedCategoryName(categoryKey: String): String {
    return when (categoryKey.lowercase().replace("í", "i")) {
        "gastronomia" -> stringResource(R.string.create_post_cat_gastronomy)
        "cultura" -> stringResource(R.string.create_post_cat_culture)
        "naturaleza" -> stringResource(R.string.create_post_cat_nature)
        "entretenimiento" -> stringResource(R.string.create_post_cat_entertainment)
        "historia" -> stringResource(R.string.create_post_cat_history)
        "cercanos" -> stringResource(R.string.filter_nearby)
        else -> categoryKey
    }
}

@Composable
fun FilterChipsRow(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf(
        Pair("Cercanos", Icons.Default.NearMe),
        Pair("En la ciudad", Icons.Default.LocationCity),
        Pair("Gastronomía", getCategoryIcon("Gastronomía")),
        Pair("Cultura", getCategoryIcon("Cultura")),
        Pair("Naturaleza", getCategoryIcon("Naturaleza")),
        Pair("Entretenimiento", getCategoryIcon("Entretenimiento")),
        Pair("Historia", getCategoryIcon("Historia"))
    )

    val hasSelection = selectedFilter.isNotEmpty()

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (name, icon) ->
            val isSelected = selectedFilter == name || 
                (name == "Cercanos" && (selectedFilter == "Cercanos" || selectedFilter == "Nearby" || selectedFilter == stringResource(R.string.filter_nearby)))
            val baseColor = getCategoryColor(name)
            val displayColor = if (hasSelection && !isSelected) MaterialTheme.colorScheme.onSurfaceVariant else baseColor

            Surface(
                onClick = { onFilterSelected(name) },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, displayColor),
                color = if (isSelected) displayColor else Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.Black else displayColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getTranslatedCategoryName(name),
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MapControlAction(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = GrayText, modifier = Modifier.size(24.dp))
        }
    }
}
