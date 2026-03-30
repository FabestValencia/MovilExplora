package com.example.movilexplora.features.statistics

import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.movilexplora.core.component.BottomNavigationBar
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToEvents: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Estadísticas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.statisticsscreen_back_3), tint = MaterialTheme.colorScheme.onBackground)
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
                onProfileClick = onNavigateToProfile,
                selectedItem = "Perfil" // Manteniendo como parte del flujo del perfil
            )
        },
        containerColor = Color(0xFFF7F8FA) // Fondo gris claro
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Resumen General
            Text(
                text = stringResource(R.string.statisticsscreen_resumen_general_0),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayText,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cards
            StatDetailCard(
                title = "Publicaciones Activas",
                value = state.activePosts.toString(),
                change = state.activePostsChange,
                isPositive = state.isActivePostsPositive,
                icon = Icons.Default.CheckCircleOutline
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatDetailCard(
                title = "Publicaciones Finalizadas",
                value = state.finishedPosts.toString(),
                change = state.finishedPostsChange,
                isPositive = state.isFinishedPostsPositive,
                icon = Icons.Default.Archive
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatDetailCard(
                title = "Pendientes de Verificación",
                value = state.pendingPosts.toString(),
                change = state.pendingPostsChange,
                isPositive = state.isPendingPostsPositive,
                icon = Icons.Default.PendingActions
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Gráfico (Simulado)
            ChartCard(
                total = state.totalMonthPosts,
                active = state.activePosts,
                finished = state.finishedPosts,
                pending = state.pendingPosts
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Actividad Reciente
            Text(
                text = stringResource(R.string.statisticsscreen_actividad_reciente_1),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GrayText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            RecentActivityList(state.recentActivities)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatDetailCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 12.sp, color = GrayText)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = change,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) Color(0xFF4CAF50) else Color(0xFFE53935),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Turquoise.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun ChartCard(total: Int, active: Int, finished: Int, pending: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(text = stringResource(R.string.statisticsscreen_distribuci_n_de_publicaciones_2), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = "Total: $total este mes", fontSize = 12.sp, color = GrayText)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Calculamos la altura máxima y proporcional para las barras
            val maxVal = maxOf(active, finished, pending, 1)
            val maxHeight = 120f
            
            val activeHeight = (active.toFloat() / maxVal) * maxHeight
            val finishedHeight = (finished.toFloat() / maxVal) * maxHeight
            val pendingHeight = (pending.toFloat() / maxVal) * maxHeight

            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Bar 1 - Activas
                    Box(modifier = Modifier.width(24.dp).height(activeHeight.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(MaterialTheme.colorScheme.primary))
                    // Bar 2 - Finalizadas
                    Box(modifier = Modifier.width(24.dp).height(finishedHeight.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Turquoise))
                    // Bar 3 - Pendientes
                    Box(modifier = Modifier.width(24.dp).height(pendingHeight.dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(Color.LightGray))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ACTIVAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                Text("FINALIZADAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
                Text("PENDIENTES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrayText)
            }
        }
    }
}

@Composable
fun RecentActivityList(activities: List<ActivityItemModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            activities.forEachIndexed { index, activity ->
                ActivityItem(title = activity.title, time = activity.time)
                if (index < activities.size - 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ActivityItem(title: String, time: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color(0xFFF0F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = GrayText)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = time, fontSize = 12.sp, color = GrayText)
        }
    }
}
