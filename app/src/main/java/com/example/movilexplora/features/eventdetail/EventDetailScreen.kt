package com.example.movilexplora.features.eventdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movilexplora.R
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.VerifiedBlue
import com.example.movilexplora.ui.theme.getCategoryColor
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.layers.generated.CircleLayer
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.generated.Expression

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.eventdetailscreen_detalle_del_evento_0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.eventdetailscreen_back_4), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    if (state.isOwner) {
                        IconButton(onClick = { onNavigateToEdit(eventId) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.common_edit), tint = Turquoise)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (!state.isOwner) {
                BottomActionButtons(
                    isFavorite = viewModel.isFavorite(state.event),
                    onToggleFavorite = { viewModel.toggleFavorite(eventId) },
                    onMarkVisited = { viewModel.markAsVisited() }
                )
            }
        }
    ) { paddingValues ->
        state.event?.let { event ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header Image & Title
                Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                    AsyncImage(
                        model = event.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 300f
                                )
                            )
                    )
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = event.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = event.location, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Rejection Alert if owner and rejected
                    if (state.isOwner && event.status == PostStatus.RECHAZADO) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFD32F2F))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFD32F2F))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stringResource(R.string.profile_post_rejected_title),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.profile_rejection_reason_format, event.rejectionReason ?: "N/A"),
                                        color = Color(0xFFD32F2F),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Badges Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (event.isVerified) {
                            DetailBadge(
                                text = stringResource(R.string.eventdetailscreen_verificado_1),
                                icon = Icons.Default.CheckCircle,
                                containerColor = VerifiedBlue,
                                contentColor = Color.White
                            )
                        }

                        if (state.isExpired) {
                            DetailBadge(
                                text = stringResource(R.string.event_expired_label),
                                icon = Icons.Default.TimerOff,
                                containerColor = Color.Gray,
                                contentColor = Color.White
                            )
                        }
                        
                        val categoryCol = getCategoryColor(event.category)
                        DetailBadge(
                            text = event.category,
                            containerColor = categoryCol.copy(alpha = 0.15f),
                            contentColor = categoryCol
                        )
                        
                        DetailBadge(
                            text = "${event.likedBy.size}",
                            icon = Icons.Default.Favorite,
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )

                        if (state.isOwner) {
                            DetailBadge(
                                text = event.status.name,
                                containerColor = when(event.status) {
                                    PostStatus.PENDIENTE -> Color(0xFFFFF3E0)
                                    PostStatus.VERIFICADO -> Color(0xFFE8F5E9)
                                    PostStatus.RECHAZADO -> Color(0xFFFFEBEE)
                                },
                                contentColor = when(event.status) {
                                    PostStatus.PENDIENTE -> Color(0xFFEF6C00)
                                    PostStatus.VERIFICADO -> Color(0xFF2E7D32)
                                    PostStatus.RECHAZADO -> Color(0xFFC62828)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Date and Time Section
                    Text(text = stringResource(R.string.event_detail_date_time), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Turquoise, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "${stringResource(R.string.profile_event_start)} ${event.date} • ${event.time}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                            if (event.endDate.isNotEmpty()) {
                                Text(text = "${stringResource(R.string.profile_event_end)} ${event.endDate} • ${event.endTime}", fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(text = stringResource(R.string.eventdetailscreen_acerca_de_este_evento_2), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Map Section
                    Text(text = stringResource(R.string.eventdetailscreen_ubicaci_n_3), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val mapViewportState = rememberMapViewportState {
                        setCameraOptions {
                            center(Point.fromLngLat(event.longitude, event.latitude))
                            zoom(14.0)
                        }
                    }

                    // Sincronizar cámara si los datos cambian
                    LaunchedEffect(event.latitude, event.longitude) {
                        mapViewportState.setCameraOptions {
                            center(Point.fromLngLat(event.longitude, event.latitude))
                            zoom(14.0)
                        }
                    }

                    val eventLocationGeoJson = remember(event.latitude, event.longitude) {
                        """
                        {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "type": "Feature",
                              "geometry": {
                                "type": "Point",
                                "coordinates": [${event.longitude}, ${event.latitude}]
                              },
                              "properties": {
                                "title": "${event.title}",
                                "category": "${event.category}"
                              }
                            }
                          ]
                        }
                        """.trimIndent()
                    }

                    val sourceState = rememberGeoJsonSourceState {
                        data = GeoJSONData(eventLocationGeoJson)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        MapboxMap(
                            modifier = Modifier.fillMaxSize(),
                            mapViewportState = mapViewportState
                        ) {
                            CircleLayer(
                                sourceState = sourceState,
                                layerId = "event-location-layer"
                            ) {
                                circleRadius = DoubleValue(10.0)
                                circleColor = ColorValue(Expression.color(Turquoise.toArgb()))
                                circleStrokeWidth = DoubleValue(2.0)
                                circleStrokeColor = ColorValue(Expression.color(Color.White.toArgb()))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp)) // Space for bottom buttons
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Turquoise)
            }
        }
    }
}

@Composable
fun DetailBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text, color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BottomActionButtons(
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onMarkVisited: () -> Unit = {}
) {
    var isVisited by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onToggleFavorite,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFavorite) Turquoise else Turquoise.copy(alpha = 0.7f),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.postdetailscreen_me_interesa),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Button(
            onClick = { 
                isVisited = !isVisited
                if (isVisited) onMarkVisited()
            },
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isVisited) Turquoise else MaterialTheme.colorScheme.surface,
                contentColor = if (isVisited) Color.White else Turquoise
            ),
            border = if (!isVisited) BorderStroke(1.dp, Turquoise.copy(alpha = 0.5f)) else null,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = if (isVisited) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.postdetailscreen_visitado),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
