package com.example.movilexplora.features.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.movilexplora.R
import com.example.movilexplora.core.component.ProfileImage
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.features.filters.FilterBottomSheet
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.VerifiedBlue
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon

@Composable
fun getTranslatedCategoryName(categoryKey: String): String {
    return when (categoryKey.lowercase().replace("í", "i")) {
        "gastronomia" -> stringResource(R.string.create_post_cat_gastronomy)
        "cultura" -> stringResource(R.string.create_post_cat_culture)
        "naturaleza" -> stringResource(R.string.create_post_cat_nature)
        "entretenimiento" -> stringResource(R.string.create_post_cat_entertainment)
        "historia" -> stringResource(R.string.create_post_cat_history)
        else -> categoryKey
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToMap: () -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagedPosts = viewModel.pagedPosts.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    if (showFilterSheet) {
        FilterBottomSheet(
            initialState = state.filterState,
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { filterState ->
                viewModel.applyFilters(filterState)
                showFilterSheet = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        HeaderSection(
            userName = state.userName, 
            profilePictureUrl = state.userProfilePictureUrl,
            searchQuery = state.searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            onMapClick = onNavigateToMap
        )
        
        FilterToggleSection(
            onFilterClick = { showFilterSheet = true },
            onFeedClick = { viewModel.clearFilters() }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        CategoriesSection(
            categories = state.categories.map { it.name },
            selectedCategory = state.filterState.selectedCategory,
            onCategorySelect = { viewModel.toggleCategoryFilter(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(
                count = pagedPosts.itemCount,
                key = { index -> pagedPosts[index]?.id ?: index }
            ) { index ->
                val post = pagedPosts[index]
                if (post != null) {
                    PostCard(
                        post = post,
                        currentUserId = currentUserId,
                        onFavoriteClick = { viewModel.toggleFavorite(post.id) },
                        onDetailClick = { onNavigateToDetail(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, profilePictureUrl: String?, searchQuery: String, onSearchQueryChange: (String) -> Unit, onMapClick: () -> Unit) {
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
                placeholder = { Text(stringResource(R.string.feedscreen_search_6), color = GrayText) },
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
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.common_close), tint = MaterialTheme.colorScheme.onBackground)
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
                ProfileImage(
                    imageUrl = profilePictureUrl,
                    modifier = Modifier.size(48.dp)
                )
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
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.feedscreen_search_6), tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
fun FilterToggleSection(onFilterClick: () -> Unit, onFeedClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .padding(4.dp)
        ) {
            Button(
                onClick = onFeedClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.feedscreen_feed_1), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 14.sp)
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
fun CategoriesSection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { categoryName ->
            val hasSelection = selectedCategory != null
            val isSelected = selectedCategory == categoryName

            val icon = getCategoryIcon(categoryName)
            val baseColor = getCategoryColor(categoryName)

            val displayColor = if (hasSelection && !isSelected) MaterialTheme.colorScheme.onSurfaceVariant else baseColor

            Surface(
                onClick = { onCategorySelect(categoryName) },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, displayColor.copy(alpha = if (isSelected) 1f else 0.5f)),
                color = displayColor.copy(alpha = if (isSelected) 0.5f else 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = displayColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = getTranslatedCategoryName(categoryName), color = displayColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun PostCard(
    post: Post, 
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
            Box(modifier = Modifier.height(200.dp).fillMaxWidth()) {
                // Header Image
                coil.compose.AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Verified Badge
                if (post.isVerified) {
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
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Text(text = post.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    val isFavorite = post.likedBy.contains(currentUserId)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = stringResource(R.string.feedscreen_favorite_7),
                                tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        if (post.likedBy.isNotEmpty()) {
                            Text(
                                text = "${post.likedBy.size}", 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Medium, 
                                color = if (isFavorite) Color.Red else GrayText
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                            Text(text = getTranslatedCategoryName(post.category), color = categoryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
