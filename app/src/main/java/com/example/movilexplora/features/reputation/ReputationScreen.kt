package com.example.movilexplora.features.reputation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReputationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reputación", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Spacer(modifier = Modifier.height(16.dp))
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Turquoise, CircleShape)
                        .background(Color.LightGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        tint = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(Turquoise, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = state.userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Turquoise.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = state.currentLevel.displayName.uppercase(),
                    color = Turquoise,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "2,450", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Turquoise)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "pts totales", fontSize = 14.sp, color = GrayText, modifier = Modifier.padding(bottom = 6.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Next Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Turquoise.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Turquoise)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Siguiente: ${state.nextLevelName}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                            Text(text = "Top 15% de exploradores en tu ciudad", fontSize = 12.sp, color = GrayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Progreso", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        Text(text = "${state.currentPoints} / ${state.targetPoints} pts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { state.currentPoints.toFloat() / state.targetPoints.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Turquoise,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${state.targetPoints - state.currentPoints} pts para subir de nivel",
                        fontSize = 12.sp,
                        color = GrayText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Recent Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Puntos Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                TextButton(onClick = { /* Ver Historial */ }) {
                    Text(text = "Ver Historial", color = Turquoise, fontWeight = FontWeight.Bold)
                }
            }

            state.recentPoints.forEach { point ->
                RecentPointItem(point)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // How to earn points
            Text(
                text = "Cómo ganar puntos",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )
            Spacer(modifier = Modifier.height(16.dp))

            val earnOptions = listOf(
                EarnOption("Publicar Lugares", "Comparte nuevos descubrimientos con la comunidad.", "+10 pts", Icons.Default.EditNote),
                EarnOption("Comentar", "Interactúa con las publicaciones de otros exploradores.", "+2 pts", Icons.AutoMirrored.Filled.Chat),
                EarnOption("Recibir Votos", "Recibe 'me gusta' en tus aportaciones de contenido.", "+5 pts", Icons.Default.ThumbUp),
                EarnOption("Visitar Lugares", "Registra tu visita en puntos de interés verificados.", "+20 pts", Icons.Default.LocationOn)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EarnCard(earnOptions[0], Modifier.weight(1f))
                    EarnCard(earnOptions[1], Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EarnCard(earnOptions[2], Modifier.weight(1f))
                    EarnCard(earnOptions[3], Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun RecentPointItem(point: RecentPoint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Turquoise.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = when (point.type) {
                    PointType.POST -> Icons.Default.AddPhotoAlternate
                    PointType.COMMENT -> Icons.AutoMirrored.Filled.Chat
                    PointType.VISIT -> Icons.Default.LocationOn
                    PointType.VOTE -> Icons.Default.ThumbUp
                }
                Icon(imageVector = icon, contentDescription = null, tint = Turquoise, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = point.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(text = point.time, fontSize = 12.sp, color = GrayText)
            }
            Text(text = point.points, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Turquoise)
        }
    }
}

data class EarnOption(val title: String, val description: String, val points: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun EarnCard(option: EarnOption, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Turquoise.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Turquoise.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = option.icon, contentDescription = null, tint = Turquoise, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = option.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(text = option.description, fontSize = 11.sp, color = GrayText, lineHeight = 14.sp, modifier = Modifier.weight(1f))
            Text(text = option.points, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Turquoise)
        }
    }
}
