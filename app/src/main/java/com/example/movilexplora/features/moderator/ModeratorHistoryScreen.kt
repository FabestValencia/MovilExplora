package com.example.movilexplora.features.moderator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.R
import com.example.movilexplora.domain.model.VerificationType
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ModeratorHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historial",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Movimientos recientes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedFilter == "Todo",
                        onClick = { viewModel.onFilterSelected("Todo") },
                        label = { Text("Todo (${state.counts["Todo"]})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Turquoise,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = if (state.selectedFilter == "Todo") null else FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = false)
                    )
                    FilterChip(
                        selected = state.selectedFilter == "Aceptados",
                        onClick = { viewModel.onFilterSelected("Aceptados") },
                        label = { Text("Aceptados (${state.counts["Aceptados"]})") },
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = state.selectedFilter == "Aceptados")
                    )
                    FilterChip(
                        selected = state.selectedFilter == "Rechazados",
                        onClick = { viewModel.onFilterSelected("Rechazados") },
                        label = { Text("Rechazados (${state.counts["Rechazados"]})") },
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = state.selectedFilter == "Rechazados")
                    )
                }
            }

            items(
                items = state.items,
                key = { it.id }
            ) { item ->
                ModeratorHistoryItemCard(item = item)
            }
        }
    }
}

@Composable
fun ModeratorHistoryItemCard(item: HistoryItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(180.dp).fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(stringResource(R.string.image_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                }

                // Badge
                Surface(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (item.type) {
                            VerificationType.LOCATION -> Icons.Default.Storefront
                            VerificationType.PHOTO -> Icons.Default.CameraAlt
                            VerificationType.REVIEW -> Icons.Default.RateReview
                            VerificationType.EVENT -> Icons.Default.Event
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.badgeText, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.submitted_by, item.author),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hace ${item.timeAgo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (item.status == "Aceptado") Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, if (item.status == "Aceptado") Color(0xFF4CAF50) else Color(0xFFF44336))
                    ) {
                        Text(
                            text = item.status,
                            color = if (item.status == "Aceptado") Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
