package com.example.movilexplora.features.badges

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.domain.model.Achievement
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.core.component.UnlockNotificationDialog
import com.example.movilexplora.domain.model.UnlockNotification
import com.example.movilexplora.ui.theme.getTranslatedBadgeDescription
import com.example.movilexplora.ui.theme.getTranslatedBadgeName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(
    onNavigateBack: () -> Unit,
    viewModel: BadgesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedBadge by remember { mutableStateOf<UnlockNotification?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.badges_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.badgesscreen_back_3), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Unlocked count badge
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Turquoise,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.badges_unlocked_count, state.unlockedCount),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.badgesscreen_sigue_explorando_para_complet_0),
                        fontSize = 14.sp,
                        color = GrayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = stringResource(R.string.badgesscreen_logros_recientes_1),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            items(state.achievements) { achievement ->
                BadgeCard(
                    achievement = achievement,
                    onClick = {
                        if (achievement.isUnlocked) {
                            selectedBadge = UnlockNotification(
                                title = "¡INSIGNIA DESBLOQUEADA!",
                                name = achievement.name,
                                icon = when (achievement.iconName) {
                                    "celebration" -> Icons.Default.Celebration
                                    "verified" -> Icons.Default.Verified
                                    "map" -> Icons.Default.Map
                                    "stars" -> Icons.Default.Stars
                                    "contact_page" -> Icons.Default.ContactPage
                                    else -> Icons.Default.EmojiEvents
                                },
                                date = "Reciente",
                                xpEarned = "+50 XP",
                                footerText = achievement.description
                            )
                        }
                    }
                )
            }
            item {
                NextChallengeCard()
            }
        }

        selectedBadge?.let { notification ->
            UnlockNotificationDialog(
                notification = notification,
                onDismiss = { selectedBadge = null }
            )
        }
    }
}

@Composable
fun BadgeCard(achievement: Achievement, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgeIcon(achievement)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = getTranslatedBadgeName(achievement.name),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.primary else GrayText,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = getTranslatedBadgeDescription(achievement.description),
                fontSize = 11.sp,
                color = GrayText.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun BadgeIcon(achievement: Achievement) {
    Box(contentAlignment = Alignment.BottomEnd) {
        val gradient = if (achievement.isUnlocked) {
            when (achievement.iconName) {
                "celebration" -> Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFF1DE9B6)))
                "verified" -> Brush.linearGradient(listOf(Color(0xFF00B8D4), Color(0xFF00E5FF)))
                "map" -> Brush.linearGradient(listOf(Color(0xFF40C4FF), Color(0xFF00B0FF)))
                else -> Brush.linearGradient(listOf(Turquoise, Turquoise))
            }
        } else {
            Brush.linearGradient(listOf(Color(0xFFE0E0E0), Color(0xFFE0E0E0)))
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(gradient, CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (achievement.iconName) {
                "celebration" -> Icons.Default.Celebration
                "verified" -> Icons.Default.Verified
                "map" -> Icons.Default.Map
                "stars" -> Icons.Default.Stars
                "contact_page" -> Icons.Default.ContactPage
                else -> Icons.Default.EmojiEvents
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (achievement.isUnlocked) Color.White else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!achievement.isUnlocked) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color.Gray, CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun NextChallengeCard() {
    val stroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.badgesscreen_pr_ximo_reto_2), color = GrayText.copy(alpha = 0.5f), fontSize = 14.sp)
        }
    }
}
