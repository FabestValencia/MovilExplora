package com.example.movilexplora.features.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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

import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@Composable
fun EventsScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToCreateEvent: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateEvent,
                containerColor = Turquoise,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Evento")
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
                .background(Color.White)
        ) {
            HeaderSection(onMapClick = onNavigateToMap)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Próximos Eventos",
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
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
                        onJoinClick = { viewModel.toggleJoinEvent(event.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(onMapClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Explora", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Turquoise)
        }
        Row {
            IconButton(onClick = onMapClick) {
                Icon(imageVector = Icons.Default.Map, contentDescription = "Map", tint = DarkBlue)
            }
            IconButton(onClick = { /* Search */ }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = DarkBlue)
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onJoinClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(160.dp).fillMaxWidth().background(Color.LightGray)) {
                // Image placeholder
                Text("Imagen del Evento", modifier = Modifier.align(Alignment.Center))
                
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopStart),
                    shape = RoundedCornerShape(8.dp),
                    color = Turquoise
                ) {
                    Text(
                        text = event.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Turquoise, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "${event.date} • ${event.time}", fontSize = 12.sp, color = GrayText)
                        }
                    }
                    
                    Button(
                        onClick = onJoinClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.isJoined) Color.LightGray else Turquoise
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (event.isJoined) "Asistiré" else "Asistir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = event.location, fontSize = 12.sp, color = Color.LightGray)
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Turquoise, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.attendeesCount} personas asistirán",
                        fontSize = 12.sp,
                        color = GrayText,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
