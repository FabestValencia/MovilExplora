package com.example.movilexplora.features.reputation

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getReputationColor
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReputationScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReputationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showFullHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reputation_title), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.reputationscreen_back_6), tint = MaterialTheme.colorScheme.onBackground)
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
                val boxModifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Turquoise, CircleShape)
                    .background(Color.LightGray)

                if (state.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = state.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = boxModifier,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = boxModifier) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            tint = Color.White
                        )
                    }
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
            Text(text = state.userName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            
            val levelColor = getReputationColor(state.currentLevel)
            
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = levelColor.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(state.currentLevel.displayNameRes).uppercase(),
                    color = levelColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = stringResource(R.string.reputationscreen_d_0).format(state.currentPoints), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Turquoise)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.reputationscreen_pts_totales_1), fontSize = 14.sp, color = GrayText, modifier = Modifier.padding(bottom = 6.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Next Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            val nextName = if (state.nextLevelName == "Nivel Máximo" || state.nextLevelName == "Max Level") stringResource(R.string.reputation_max_level) else state.nextLevelName
                            Text(text = stringResource(R.string.reputation_next, nextName), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text(text = stringResource(R.string.reputation_percentage_msg), fontSize = 12.sp, color = GrayText)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = stringResource(R.string.reputationscreen_progreso_2), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(text = "${state.currentPoints} / ${state.targetPoints} pts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
                        text = stringResource(R.string.reputation_pts_to_level_up, (state.targetPoints - state.currentPoints).coerceAtLeast(0)),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.reputationscreen_puntos_recientes_3), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                TextButton(onClick = { showFullHistory = !showFullHistory }) {
                    Text(
                        text = if (showFullHistory) "Ocultar" else stringResource(R.string.reputationscreen_ver_historial_4),
                        color = Turquoise,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val pointsToShow = if (showFullHistory) state.recentPoints else state.recentPoints.take(3)

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pointsToShow.forEach { point ->
                    RecentPointItem(point)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // How to earn points
            Text(
                text = stringResource(R.string.reputationscreen_c_mo_ganar_puntos_5),
                modifier = Modifier.fillMaxWidth(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            val earnOptions = listOf(
                EarnOption(stringResource(R.string.reputation_earn_post_title), stringResource(R.string.reputation_earn_post_desc), stringResource(R.string.reputation_earn_post_pts), Icons.Default.EditNote),
                EarnOption(stringResource(R.string.reputation_earn_verify_title), stringResource(R.string.reputation_earn_verify_desc), stringResource(R.string.reputation_earn_verify_pts), Icons.Default.Verified),
                EarnOption(stringResource(R.string.reputation_earn_comment_title), stringResource(R.string.reputation_earn_comment_desc), stringResource(R.string.reputation_earn_comment_pts), Icons.AutoMirrored.Filled.Chat),
                EarnOption(stringResource(R.string.reputation_earn_vote_title), stringResource(R.string.reputation_earn_vote_desc), stringResource(R.string.reputation_earn_vote_pts), Icons.Default.ThumbUp),
                EarnOption(stringResource(R.string.reputation_earn_visit_title), stringResource(R.string.reputation_earn_visit_desc), stringResource(R.string.reputation_earn_visit_pts), Icons.Default.LocationOn)
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
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EarnCard(earnOptions[4], Modifier.weight(1f))
                    Spacer(modifier = Modifier.weight(1f)) // Empty block to align grid
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun getTranslatedPointText(original: String): String {
    return original
}

@Composable
fun RecentPointItem(point: RecentPoint) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Text(text = getTranslatedPointText(point.title), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = getTranslatedPointText(point.time), fontSize = 12.sp, color = GrayText)
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
            Text(text = option.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = option.description, fontSize = 11.sp, color = GrayText, lineHeight = 14.sp, modifier = Modifier.weight(1f))
            Text(text = option.points, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Turquoise)
        }
    }
}
