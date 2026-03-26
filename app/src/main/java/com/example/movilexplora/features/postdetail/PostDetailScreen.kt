package com.example.movilexplora.features.postdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    isModerator: Boolean = false,
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
                        text = "Punto de interes",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBlue)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the back button
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (isModerator) {
                ModeratorActionButtons()
            } else {
                BottomActionButtons()
            }
        }
    ) { paddingValues ->
        state.post?.let { post ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .background(Color.White)
            ) {
                // Header Image & Title
                Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                    // Placeholder for main image
                    Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                        Text("Imagen de la Cueva", modifier = Modifier.align(Alignment.Center))
                    }
                    
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
                        DetailBadge(
                            text = "VERIFICADO",
                            icon = Icons.Default.CheckCircle,
                            containerColor = Turquoise.copy(alpha = 0.1f),
                            contentColor = Turquoise
                        )
                        DetailBadge(
                            text = post.price,
                            icon = Icons.Default.AttachMoney,
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                        DetailBadge(
                            text = post.category,
                            containerColor = Turquoise.copy(alpha = 0.15f),
                            contentColor = Turquoise
                        )
                        DetailBadge(
                            text = "${post.rating} (1.2k)",
                            icon = Icons.Default.Star,
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Description
                    Text(
                        text = state.description,
                        fontSize = 16.sp,
                        color = GrayText.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Location Section
                    Text(text = "Ubicación", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.LightGray)
                    ) {
                        Text("Vista del Mapa", modifier = Modifier.align(Alignment.Center))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "40.5594° N, 14.2045° E",
                        fontSize = 12.sp,
                        color = GrayText.copy(alpha = 0.5f)
                    )

                    if (!isModerator) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(24.dp))

                        // Comments Section
                        Text(text = "Comentarios", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        state.comments.forEach { comment ->
                            CommentItem(comment)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Add Comment Field
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Añadir comentario", fontSize = 14.sp) },
                            trailingIcon = {
                                IconButton(onClick = { /* Send */ }) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = GrayText)
                                }
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF7F8F9),
                                unfocusedContainerColor = Color(0xFFF7F8F9),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(if (isModerator) 200.dp else 100.dp)) // Space for bottom buttons
                }
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
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = comment.userName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(text = comment.date, fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            }
            Text(
                text = comment.content,
                fontSize = 14.sp,
                color = GrayText.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun BottomActionButtons() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Button(
            onClick = { /* Estoy interesado */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Estoy interesando", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { /* Marcar como visitado */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Icon(imageVector = Icons.Outlined.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Marcar como visitado", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ModeratorActionButtons() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White)
    ) {
        Button(
            onClick = { /* Rechazar */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Rechazar", color = Color(0xFFD32F2F), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { /* Verificar */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF42A5F5))
        ) {
            Icon(imageVector = Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Verificar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { /* Resolver */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Resolver", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
