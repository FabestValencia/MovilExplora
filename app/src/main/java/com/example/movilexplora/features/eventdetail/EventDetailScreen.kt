package com.example.movilexplora.features.eventdetail

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.VerifiedBlue
import com.example.movilexplora.ui.theme.getCategoryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
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
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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
                // Header Image
                Box(modifier = Modifier.height(240.dp).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Text("Imagen del Evento", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))

                    val categoryColor = getCategoryColor(event.category)
                    Surface(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopStart),
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor
                    ) {
                        Text(
                            text = event.category,
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
                                .padding(16.dp)
                                .background(VerifiedBlue, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.eventdetailscreen_verificado_1), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = event.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Date and Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = Turquoise, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Inicio: ${event.date} • ${event.time}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            if (event.endDate.isNotEmpty() && event.endTime.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Fin: ${event.endDate} • ${event.endTime}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Turquoise, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = event.location, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(text = stringResource(R.string.eventdetailscreen_acerca_de_este_evento_2), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = event.description,
                        fontSize = 16.sp,
                        color = GrayText.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    // Map Section
                    Text(text = stringResource(R.string.eventdetailscreen_ubicaci_n_3), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(stringResource(R.string.common_map_view), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Turquoise)
            }
        }
    }
}
