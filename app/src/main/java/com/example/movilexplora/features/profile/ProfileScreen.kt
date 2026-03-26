package com.example.movilexplora.features.profile

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.domain.model.Achievement
import com.example.movilexplora.domain.model.ReputationLevel
import com.example.movilexplora.domain.model.UserProfile
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

import com.example.movilexplora.core.component.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToEvents: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onEditData: () -> Unit,
    onNavigateToEditEvent: (String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val userEvents by viewModel.userEvents.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var eventToDeleteId by remember { mutableStateOf<String?>(null) }
    
    // 0 -> Mis Lugares, 1 -> Mis Eventos
    var selectedTabIndex by remember { mutableStateOf(0) }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                viewModel.deleteAccount()
                showDeleteDialog = false
            }
        )
    }
    
    if (eventToDeleteId != null) {
        DeleteEventDialog(
            onDismiss = { eventToDeleteId = null },
            onConfirm = {
                viewModel.deleteEvent(eventToDeleteId!!)
                eventToDeleteId = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue) },
                actions = {
                    IconButton(onClick = onEditData) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Edit", tint = DarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onCreateClick = onNavigateToCreatePost,
                onHomeClick = onNavigateToHome,
                onEventsClick = onNavigateToEvents,
                onAlertsClick = onNavigateToNotifications,
                onProfileClick = { /* Already here */ },
                selectedItem = "Perfil"
            )
        }
    ) { paddingValues ->
        userProfile?.let { profile ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
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
                            .size(32.dp)
                            .background(Turquoise, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(text = profile.email, fontSize = 14.sp, color = GrayText)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Turquoise.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, Turquoise.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Turquoise, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = profile.role, color = Turquoise, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Turquoise,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Mis Lugares", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Mis Eventos", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                if (selectedTabIndex == 0) {
                    // Post Stats Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Activas", profile.activePosts.toString(), Turquoise, Modifier.weight(1f))
                        StatCard("Finalizadas", profile.finishedPosts.toString(), Turquoise, Modifier.weight(1f))
                        StatCard("Pendientes", profile.pendingPosts.toString(), Color(0xFFFFB74D), Modifier.weight(1f))
                    }
    
                    Spacer(modifier = Modifier.height(24.dp))
    
                    // Points Card
                    ParticipationPointsCard(profile)
    
                    Spacer(modifier = Modifier.height(32.dp))
    
                    // Reputation Levels
                    Text(
                        text = "Niveles de reputación",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ReputationTimeline(profile.reputationLevel)
    
                    Spacer(modifier = Modifier.height(32.dp))
    
                    // Achievements
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Logros", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                        TextButton(onClick = { /* Ver todos */ }) {
                            Text(text = "Ver todos", color = Turquoise, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    AchievementsRow(profile.achievements)
                } else {
                    if (userEvents.isEmpty()) {
                        Text("No tienes eventos creados.", color = GrayText, modifier = Modifier.padding(32.dp))
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            userEvents.forEach { event ->
                                MyEventCard(
                                    event = event,
                                    onEditClick = { onNavigateToEditEvent(event.id) },
                                    onDeleteClick = { eventToDeleteId = event.id }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Action Buttons
                Button(
                    onClick = onEditData,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Icon(imageVector = Icons.Default.EditNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Editar datos", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Eliminar cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¿Eliminar cuenta?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Esta acción es permanente y perderás todas tus insignias y publicaciones. No podrás recuperar tus datos una vez confirmada.",
                    fontSize = 14.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancelar", color = Turquoise, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = valueColor)
            Text(text = label, fontSize = 12.sp, color = GrayText)
        }
    }
}

@Composable
fun DeleteEventDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE57373),
                    modifier = Modifier.size(40.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "¿Eliminar evento?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Esta acción no se puede deshacer. El evento será eliminado permanentemente.",
                    fontSize = 14.sp,
                    color = GrayText,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancelar", color = Turquoise, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.2f).height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                    ) {
                        Text(text = "Eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MyEventCard(event: com.example.movilexplora.domain.model.Event, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = event.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = event.description, fontSize = 14.sp, color = GrayText)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Turquoise, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${event.date} • ${event.time}", fontSize = 12.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Turquoise)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
        }
    }
}

@Composable
fun ParticipationPointsCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "PUNTOS POR PARTICIPACIÓN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrayText)
                    Text(text = profile.role, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                }
                Box(
                    modifier = Modifier.size(40.dp).background(Turquoise.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Turquoise)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = profile.currentXp.toString(), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = DarkBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "/ ${profile.maxXp} XP",
                    fontSize = 14.sp,
                    color = GrayText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { profile.currentXp.toFloat() / profile.maxXp.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = Turquoise,
                trackColor = Color.LightGray.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${profile.maxXp - profile.currentXp} XP para el próximo nivel",
                fontSize = 12.sp,
                color = GrayText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun ReputationTimeline(currentLevel: ReputationLevel) {
    val levels = ReputationLevel.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        levels.forEachIndexed { index, level ->
            val isReached = level.ordinal <= currentLevel.ordinal
            val isCurrent = level == currentLevel

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 48.dp else 36.dp)
                        .background(if (isReached) Turquoise else Color.LightGray.copy(alpha = 0.3f), CircleShape)
                        .border(if (isCurrent) 2.dp else 0.dp, Turquoise, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (level) {
                        ReputationLevel.TURISTA -> Icons.Default.PhotoCamera
                        ReputationLevel.EXPLORADOR -> Icons.Default.Explore
                        ReputationLevel.AVENTURERO -> Icons.Default.Landscape
                        ReputationLevel.EMBAJADOR -> Icons.Default.Public
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isReached) Color.White else GrayText,
                        modifier = Modifier.size(if (isCurrent) 24.dp else 18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = level.displayName,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isReached) Turquoise else GrayText
                )
            }

            if (index < levels.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(if (levels[index + 1].ordinal <= currentLevel.ordinal) Turquoise else Color.LightGray.copy(alpha = 0.3f))
                        .padding(bottom = 12.dp) // Offset to align with icons
                )
            }
        }
    }
}

@Composable
fun AchievementsRow(achievements: List<Achievement>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        achievements.forEach { achievement ->
            AchievementItem(achievement, Modifier.weight(1f))
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val gradient = if (achievement.isUnlocked) {
            when (achievement.name) {
                "Caminante" -> Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFF57C00)))
                "Fotógrafo" -> Brush.verticalGradient(listOf(Color(0xFF64B5F6), Color(0xFF1976D2)))
                "Foodie" -> Brush.verticalGradient(listOf(Color(0xFF81C784), Color(0xFF388E3C)))
                else -> Brush.verticalGradient(listOf(Turquoise, Turquoise))
            }
        } else {
            Brush.verticalGradient(listOf(Color.LightGray.copy(alpha = 0.3f), Color.LightGray.copy(alpha = 0.3f)))
        }

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(gradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (achievement.iconName) {
                "hiking" -> Icons.Default.Hiking
                "camera" -> Icons.Default.PhotoCamera
                "restaurant" -> Icons.Default.Restaurant
                "flight" -> Icons.Default.Flight
                else -> Icons.Default.Star
            }
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = achievement.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (achievement.isUnlocked) DarkBlue else GrayText,
            textAlign = TextAlign.Center
        )
    }
}
