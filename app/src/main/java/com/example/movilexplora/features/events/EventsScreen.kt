package com.example.movilexplora.features.events

import android.R.attr.bottom
import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.component.BottomNavigationBar
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.VerifiedBlue
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getTranslatedCategoryName
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun EventsScreen(
    onNavigateToEventDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateEvent,
                containerColor = Turquoise,
                contentColor = Color.White,
                modifier = Modifier.padding(bottom = 36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.eventsscreen_crear_evento_4))
            }
        },
        bottomBar = {
            BottomNavigationBar(
                onCreateClick = onNavigateToCreatePost,
                onHomeClick = onNavigateToHome,
                onEventsClick = { /* Already here */ },
                onAlertsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile,
                selectedItem = "Eventos"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            HeaderSection(
                searchQuery = state.searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onMapClick = onNavigateToMap
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.eventsscreen_pr_ximos_eventos_0),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(state.events) { event ->
                    EventCard(
                        event = event,
                        currentUserId = currentUserId,
                        onFavoriteClick = { viewModel.toggleFavorite(event.id) },
                        onDetailClick = { onNavigateToEventDetail(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(searchQuery: String, onSearchQueryChange: (String) -> Unit, onMapClick: () -> Unit) {
    var isSearchActive by remember { mutableStateOf(false) }

    if (isSearchActive) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.eventsscreen_search_6), color = GrayText) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Turquoise,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Turquoise)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        isSearchActive = false
                        onSearchQueryChange("")
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.eventsscreen_explora_1), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            Row {
                IconButton(onClick = onMapClick) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = stringResource(R.string.eventsscreen_map_5), tint = MaterialTheme.colorScheme.onBackground)
                }
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.eventsscreen_search_6), tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    currentUserId: String,
    onFavoriteClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
                // TODO: Eliminar este uso fijo de imageUrl una vez que se retome la integración
                // real con imágenes en la nube o persistencia real de archivos en el dispositivo.
                val mockImageUrl = if (event.id.hashCode() % 2 == 0) {
                    "android.resource://com.example.movilexplora/drawable/circasia"
                } else {
                    "android.resource://com.example.movilexplora/drawable/salento"
                }

                AsyncImage(
                    model = if (event.imageUrl.startsWith("http") || event.imageUrl.startsWith("content")) {
                        event.imageUrl
                    } else {
                        mockImageUrl
                    },
                    contentDescription = stringResource(R.string.common_event_image),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                val categoryColor = getCategoryColor(event.category)
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = categoryColor
                ) {
                    Text(
                        text = getTranslatedCategoryName(event.category),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                if (event.isVerified) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(VerifiedBlue, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = stringResource(R.string.eventsscreen_verificado_2), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = event.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Turquoise, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(text = "${stringResource(R.string.profile_event_start)} ${event.date}", fontSize = 12.sp, color = GrayText)
                                if (event.endDate.isNotEmpty()) {
                                    Text(text = "${stringResource(R.string.profile_event_end)} ${event.endDate}", fontSize = 12.sp, color = GrayText)
                                }
                            }
                        }
                    }
                    val isFavorite = event.likedBy.contains(currentUserId)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.LightGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (event.likedBy.isNotEmpty()) {
                            Text(
                                text = "${event.likedBy.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isFavorite) Color.Red else GrayText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location, fontSize = 12.sp, color = GrayText)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDetailClick) {
                        Text(text = stringResource(R.string.eventsscreen_detalle_3), color = Turquoise, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
