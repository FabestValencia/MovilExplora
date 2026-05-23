package com.example.movilexplora.features.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSelectorScreen(
    onNavigateBack: (String?) -> Unit
) {
    var selectedLocation by remember { mutableStateOf<Point?>(null) }
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(2.1734, 41.3851))
            zoom(12.0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_selector_title)) },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack(null) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.regresar))
                    }
                },
                actions = {
                    TextButton(onClick = { 
                        selectedLocation?.let { 
                            onNavigateBack("${it.latitude()},${it.longitude()}") 
                        } ?: onNavigateBack(null)
                    }) {
                        Text(stringResource(R.string.confirm_button))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MapboxMap(
                modifier = Modifier.fillMaxSize(),
                mapViewportState = mapViewportState,
                onMapClickListener = { point ->
                    selectedLocation = point
                    true
                }
            )
            selectedLocation?.let {
                Text(
                    text = stringResource(R.string.map_selector_selected_format, it.latitude(), it.longitude()),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
