package com.example.movilexplora.features.feed

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.component.BottomNavigationBar
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.features.filters.FilterBottomSheet
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.VerifiedBlue
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToMap: () -> Unit,
    onNavigateToEvents: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val posts by viewModel.posts.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApplyFilters = {
                // Aquí se aplicarían los filtros en el ViewModel del Feed
                showFilterSheet = false
            }
        )
    }

    Scaffold(
        bottomBar = { 
            BottomNavigationBar(
                onCreateClick = onNavigateToCreatePost,
                onEventsClick = onNavigateToEvents,
                onAlertsClick = onNavigateToNotifications,
                onProfileClick = onNavigateToProfile,
                onHomeClick = { /* Already here */ },
                selectedItem = "Inicio"
            ) 
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            HeaderSection(userName = state.userName, onMapClick = onNavigateToMap)
            
            FilterToggleSection(onFilterClick = { showFilterSheet = true })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            CategoriesSection(categories = state.categories.map { it.name })
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(posts) { post ->
                    PostCard(
                        post = post, 
                        onFavoriteClick = { viewModel.toggleFavorite(post.id) },
                        onDetailClick = { onNavigateToDetail(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onMapClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                // Profile image placeholder
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = stringResource(R.string.feedscreen_hola_0), fontSize = 14.sp, color = GrayText)
                Text(text = userName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        Row {
            IconButton(onClick = onMapClick) {
                Icon(imageVector = Icons.Default.Map, contentDescription = stringResource(R.string.feedscreen_map_5), tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = { /* Search */ }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.feedscreen_search_6), tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun FilterToggleSection(onFilterClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = { /* Feed */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.5f)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.feedscreen_feed_1), color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
            }
        }
        
        TextButton(onClick = onFilterClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = null, tint = Turquoise, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.feedscreen_filtrar_2), color = Turquoise, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoriesSection(categories: List<String>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { categoryName ->
            val icon = getCategoryIcon(categoryName)
            val color = getCategoryColor(categoryName)
            
            Surface(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
                color = color.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = categoryName, color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post, 
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
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                // Placeholder for Image
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                    Text("Imagen", modifier = Modifier.align(Alignment.Center))
                }
                
                // Verified Badge
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(VerifiedBlue, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(R.string.feedscreen_verificado_3), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                
                // Rating Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = post.rating.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            Text(text = post.location, fontSize = 12.sp, color = Color.LightGray)
                        }
                    }
                    IconButton(onClick = onFavoriteClick) {
                        Icon(
                            imageVector = if (post.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.feedscreen_favorite_7),
                            tint = if (post.isFavorite) Color.Red else Color.LightGray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val categoryColor = getCategoryColor(post.category)
                        Box(
                            modifier = Modifier
                                .background(categoryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(text = post.category, color = categoryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = post.price, color = GrayText, fontSize = 12.sp)
                    }
                    TextButton(onClick = onDetailClick) {
                        Text(text = stringResource(R.string.feedscreen_detalle_4), color = Turquoise, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// End of FeedScreen
