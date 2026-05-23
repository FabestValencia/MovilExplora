package com.example.movilexplora.features.moderator

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movilexplora.R
import com.example.movilexplora.core.component.ProfileImage
import com.example.movilexplora.core.navigation.ThemeViewModel
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorFeedScreen(
    onLogout: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    viewModel: ModeratorFeedViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    var selectedItem by remember { mutableStateOf<VerificationItem?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var itemToReject by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = selectedItem != null) {
        selectedItem = null
    }

    if (showRejectDialog && itemToReject != null) {
        AlertDialog(
            onDismissRequest = {
            },
            title = { Text(text = stringResource(R.string.reject_reason_title), fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    label = { Text(stringResource(R.string.reject_reason_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReason.isNotBlank()) {
                            viewModel.rejectItem(itemToReject!!, rejectReason)
                            itemToReject = null
                            rejectReason = ""
                            selectedItem = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text(stringResource(R.string.reject_action))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    if (selectedItem != null) {
        ModeratorItemDetailScreen(
            item = selectedItem!!,
            onBack = { },
            onVerify = {
                viewModel.verifyItem(it)
            },
            onApprove = {
                viewModel.approveItem(it)
            },
            onReject = {
                itemToReject = it
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.moderator_title), 
                        style = MaterialTheme.typography.titleLarge, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout, 
                            contentDescription = stringResource(R.string.logout_desc), 
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { themeViewModel.toggleTheme() }) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) stringResource(R.string.modo_claro) else stringResource(R.string.modo_oscuro),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.ver_historial),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .padding(top = 64.dp) // Space for TopAppBar
                .background(MaterialTheme.colorScheme.background), // Use theme background
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.pending_verification_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Box {
                        Button(
                            onClick = { showSortMenu = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.filter_button), fontSize = 14.sp)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fechas_mas_recientes), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onSortOrderChanged(sortByRecent = true)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (state.sortByRecent) Icon(Icons.Default.Check, contentDescription = null, tint = Turquoise)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.fechas_mas_antiguos), color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    viewModel.onSortOrderChanged(sortByRecent = false)
                                    showSortMenu = false
                                },
                                trailingIcon = {
                                    if (!state.sortByRecent) Icon(Icons.Default.Check, contentDescription = null, tint = Turquoise)
                                }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterAll = stringResource(R.string.filter_all)
                    val filterLocations = stringResource(R.string.filter_locations)
                    val filterReviews = stringResource(R.string.filter_reviews)
                    val filterEvents = stringResource(R.string.filter_events)
                    
                    FilterChip(
                        selected = state.selectedFilter == filterAll,
                        onClick = { viewModel.onFilterSelected(filterAll) },
                        label = { Text("$filterAll (${state.counts[filterAll]})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Turquoise,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = if (state.selectedFilter == filterAll) null else FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = false)
                    )
                    FilterChip(
                        selected = state.selectedFilter == filterLocations,
                        onClick = { viewModel.onFilterSelected(filterLocations) },
                        label = { Text("$filterLocations (${state.counts[filterLocations]})") },
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = state.selectedFilter == filterLocations)
                    )
                    FilterChip(
                        selected = state.selectedFilter == filterReviews,
                        onClick = { viewModel.onFilterSelected(filterReviews) },
                        label = { Text("$filterReviews (${state.counts[filterReviews]})") },
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = state.selectedFilter == filterReviews)
                    )
                    FilterChip(
                        selected = state.selectedFilter == filterEvents,
                        onClick = { viewModel.onFilterSelected(filterEvents) },
                        label = { Text("$filterEvents (${state.counts[filterEvents] ?: 0})") },
                        shape = RoundedCornerShape(20.dp),
                        border = FilterChipDefaults.filterChipBorder(borderColor = Color.LightGray, enabled = true, selected = state.selectedFilter == filterEvents)
                    )
                }
            }

            items(
                items = state.items,
                key = { it.id }
            ) { item ->
                ModeratorItemCard(
                    item = item,
                    onClick = { },
                    onVerify = { viewModel.verifyItem(item.id) },
                    onApprove = { viewModel.approveItem(item.id) },
                    onReject = { 
                        itemToReject = item.id
                    }
                )
            }
        }
    }
}

@Composable
fun ModeratorItemDetailScreen(
    item: VerificationItem,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(stringResource(R.string.eventdetailscreen_detalle_del_evento_0), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.regresar), tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Optional gradient to make badge readable
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f)),
                                    startY = 300f
                                )
                            )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(stringResource(R.string.image_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    }
                }

                Surface(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (item.type) {
                            com.example.movilexplora.domain.model.VerificationType.LOCATION -> Icons.Default.Storefront
                            com.example.movilexplora.domain.model.VerificationType.PHOTO -> Icons.Default.CameraAlt
                            com.example.movilexplora.domain.model.VerificationType.REVIEW -> Icons.Default.RateReview
                            com.example.movilexplora.domain.model.VerificationType.EVENT -> Icons.Default.Event
                        }
                        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = item.badgeText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp).weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProfileImage(
                        imageUrl = item.authorAvatarUrl,
                        modifier = Modifier.size(16.dp),
                        placeholderModifier = Modifier.padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.submitted_by, item.author),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.timeAgo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }

            // Actions at bottom
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.type == com.example.movilexplora.domain.model.VerificationType.EVENT) {
                        Button(
                            onClick = { onApprove(item.id) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.approve_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }

                        Button(
                            onClick = { onVerify(item.id) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.verify_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    } else {
                        Button(
                            onClick = { onVerify(item.id) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.verify_action), fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = { onReject(item.id) },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Turquoise)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.reject_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeratorItemCard(
    item: VerificationItem,
    onClick: () -> Unit,
    onVerify: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), // Use surface variant
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                if (item.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Text(stringResource(R.string.image_placeholder), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center))
                    }
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
                            com.example.movilexplora.domain.model.VerificationType.LOCATION -> Icons.Default.Storefront
                            com.example.movilexplora.domain.model.VerificationType.PHOTO -> Icons.Default.CameraAlt
                            com.example.movilexplora.domain.model.VerificationType.REVIEW -> Icons.Default.RateReview
                            com.example.movilexplora.domain.model.VerificationType.EVENT -> Icons.Default.Event
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
                    ProfileImage(
                        imageUrl = item.authorAvatarUrl,
                        modifier = Modifier.size(14.dp),
                        placeholderModifier = Modifier.padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.submitted_by, item.author), 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.timeAgo, 
                        style = MaterialTheme.typography.bodySmall, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.type == com.example.movilexplora.domain.model.VerificationType.EVENT) {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            // Small text or no text if too cramped? Let's try text.
                            Text(stringResource(R.string.approve_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }

                        Button(
                            onClick = onVerify,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.verify_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        }
                    } else {
                        Button(
                            onClick = onVerify,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.verify_action), fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Turquoise)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp)) // Add spacer if not event (more space) or small spacer?
                        Text(stringResource(R.string.reject_action), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}
