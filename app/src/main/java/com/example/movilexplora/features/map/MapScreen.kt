package com.example.movilexplora.features.map

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.component.BottomNavigationBar
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToFeed: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val barcelona = LatLng(41.3851, 2.1734)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(barcelona, 12f)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                onCreateClick = onNavigateToCreatePost,
                onHomeClick = onNavigateToFeed,
                selectedItem = "Mapa"
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false),
                onMapClick = { viewModel.onDismissPostDetail() }
            ) {
                state.markers.forEach { marker ->
                    Marker(
                        state = MarkerState(position = marker.position),
                        title = marker.post.title,
                        snippet = marker.post.location,
                        onClick = {
                            viewModel.onMarkerClick(marker.post)
                            true
                        }
                    )
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
                    value = "",
                    onValueChange = {},
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
                    .padding(bottom = if (state.selectedPost == null) 100.dp else 420.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MapControlAction(icon = Icons.Default.MyLocation) { /* My Location */ }
                MapControlAction(icon = Icons.Default.Add) { /* Zoom In */ }
                MapControlAction(icon = Icons.Default.Remove) { /* Zoom Out */ }
            }

            // Preview Card or Create Button
            if (state.selectedPost != null) {
                PostPreviewCard(
                    post = state.selectedPost!!,
                    onDetailClick = { onNavigateToDetail(state.selectedPost!!.id) },
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
                        .padding(bottom = 32.dp)
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
}

@Composable
fun PostPreviewCard(
    post: Post,
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
            // Drag handle decorator
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Post Image with Favorite icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            ) {
                Text(stringResource(R.string.common_location_image), modifier = Modifier.align(Alignment.Center))
                
                // Back Button
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp).size(20.dp)
                    )
                }

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
                    val categoryColor = getCategoryColor(post.category)
                    Surface(
                        color = categoryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = post.category,
                            color = categoryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.mapscreen__2), color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = post.price, color = GrayText, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = post.rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.mapscreen_1_2k_rese_as_3), fontSize = 14.sp, color = GrayText.copy(alpha = 0.6f))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.mapscreen_disfruta_de_las_mejores_vistas_4),
                    fontSize = 14.sp,
                    color = GrayText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        placeholder = { Text("Buscar lugares, restaurantes ...", fontSize = 14.sp, color = GrayText.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = stringResource(R.string.mapscreen_filters_6),
                tint = Turquoise,
                modifier = Modifier.padding(end = 8.dp)
            )
        },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
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

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filters) { (name, icon) ->
            val isSelected = selectedFilter == name
            Surface(
                onClick = { onFilterSelected(name) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Turquoise else Color.White,
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
                        tint = if (isSelected) Color.White else Turquoise
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        color = if (isSelected) Color.White else GrayText,
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
