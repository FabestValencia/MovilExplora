package com.example.movilexplora.features.postdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movilexplora.R
import com.example.movilexplora.core.component.ProfileImage
import com.example.movilexplora.domain.model.Comment
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
fun PostDetailScreen(
    postId: String,
    isAdmin: Boolean = false,
    onNavigateBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) {
        viewModel.loadPostDetail(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.postdetailscreen_punto_de_interes_0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.postdetailscreen_back_10), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            if (isAdmin) {
                AdminActionButtons(
                    onVerify = { viewModel.updatePostStatus(postId, PostStatus.VERIFICADO) },
                    onReject = { reason -> viewModel.updatePostStatus(postId, PostStatus.RECHAZADO, reason) },
                    onResolve = { viewModel.updatePostStatus(postId, PostStatus.VERIFICADO) }
                )
            } else {
                BottomActionButtons(
                    isFavorite = viewModel.isFavorite(state.post),
                    onToggleFavorite = { viewModel.toggleFavorite(postId) },
                    onMarkVisited = { viewModel.markAsVisited() }
                )
            }
        }
    ) { paddingValues ->
        state.post?.let { post ->
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
                        model = post.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay to make text readable
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
                            text = post.title,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = post.location, fontSize = 14.sp, color = Color.White)
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Badges Row
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (post.isVerified) {
                            DetailBadge(
                                text = stringResource(R.string.postdetailscreen_verificado_1),
                                icon = Icons.Default.CheckCircle,
                                containerColor = VerifiedBlue,
                                contentColor = Color.White
                            )
                        }
                        DetailBadge(
                            text = post.price,
                            icon = Icons.Default.AttachMoney,
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                        val categoryCol = getCategoryColor(post.category)
                        DetailBadge(
                            text = post.category,
                            containerColor = categoryCol.copy(alpha = 0.15f),
                            contentColor = categoryCol
                        )
                        DetailBadge(
                            text = "${post.likedBy.size}",
                            icon = Icons.Default.Favorite,
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(
                        text = state.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Map Section
                    Text(text = stringResource(R.string.postdetailscreen_ubicaci_n_2), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Turquoise, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${post.latitude}° N, ${post.longitude}° E",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isAdmin) {
                        val mapViewportState = rememberMapViewportState {
                            setCameraOptions {
                                center(Point.fromLngLat(post.longitude, post.latitude))
                                zoom(14.0)
                            }
                        }

                        LaunchedEffect(post.latitude, post.longitude) {
                            mapViewportState.setCameraOptions {
                                center(Point.fromLngLat(post.longitude, post.latitude))
                                zoom(14.0)
                            }
                        }

                        val postLocationGeoJson = remember(post.latitude, post.longitude) {
                            """
                            {
                              "type": "FeatureCollection",
                              "features": [
                                {
                                  "type": "Feature",
                                  "geometry": {
                                    "type": "Point",
                                    "coordinates": [${post.longitude}, ${post.latitude}]
                                  },
                                  "properties": {
                                    "title": "${post.title}",
                                    "category": "${post.category}"
                                  }
                                }
                              ]
                            }
                            """.trimIndent()
                        }

                        val sourceState = rememberGeoJsonSourceState {
                            data = GeoJSONData(postLocationGeoJson)
                        }

                        LaunchedEffect(postLocationGeoJson) {
                            sourceState.data = GeoJSONData(postLocationGeoJson)
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
                                    layerId = "post-location-layer"
                                ) {
                                    circleRadius = DoubleValue(10.0)
                                    circleColor = ColorValue(Expression.color(Turquoise.toArgb()))
                                    circleStrokeWidth = DoubleValue(2.0)
                                    circleStrokeColor = ColorValue(Expression.color(Color.White.toArgb()))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Comments Section
                    Text(text = stringResource(R.string.postdetailscreen_comentarios_4), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(12.dp))

                    state.comments.forEach { comment ->
                        CommentItem(comment)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    var commentText by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.post_detail_add_comment), fontSize = 14.sp) },
                        trailingIcon = {
                            IconButton(onClick = {
                                if (commentText.isNotBlank()) {
                                    viewModel.addComment(postId, commentText)
                                    commentText = ""
                                }
                            }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(R.string.common_send), tint = Turquoise)
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(if (isAdmin) 200.dp else 100.dp)) // Space for bottom buttons
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
fun CommentItem(comment: Comment) {
    Row(modifier = Modifier.fillMaxWidth()) {
        ProfileImage(
            imageUrl = comment.userAvatar,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = comment.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = comment.date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BottomActionButtons(
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    onMarkVisited: () -> Unit = {}
) {
    // Add internal state for visual toggle of "Visited" until functionality is hooked up
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

@Composable
fun AdminActionButtons(
    onVerify: () -> Unit,
    onReject: (String) -> Unit,
    onResolve: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(text = stringResource(R.string.reject_reason_title), fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = rejectReason,
                    onValueChange = { rejectReason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.reject_reason_hint)) },
                    minLines = 3
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(rejectReason)
                    }
                ) {
                    Text(text = stringResource(R.string.common_ok), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.postdetailscreen_rechazar_7), color = Color(0xFFD32F2F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
        ) {
            Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.postdetailscreen_verificar_8), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onResolve,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.postdetailscreen_resolver_9), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
