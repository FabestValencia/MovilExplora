package com.example.movilexplora.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkBlue) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More", tint = DarkBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            if (state.recentNotifications.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "RECIENTES",
                        actionText = "Marcar todo como leído",
                        onActionClick = { viewModel.markAllAsRead() }
                    )
                }
                items(state.recentNotifications) { notification ->
                    NotificationItem(notification)
                }
            }

            if (state.olderNotifications.isNotEmpty()) {
                item {
                    SectionHeader(title = "ANTERIORES")
                }
                items(state.olderNotifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = GrayText,
            letterSpacing = 1.sp
        )
        if (actionText != null) {
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Turquoise,
                modifier = Modifier.clickable { onActionClick() }
            )
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    val backgroundColor = if (notification.isNew) Turquoise.copy(alpha = 0.05f) else Color.Transparent
    val indicatorColor = if (notification.isNew) Turquoise else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // New status vertical indicator
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                .background(indicatorColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Turquoise.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (notification.type) {
                NotificationType.NEW_PLACE -> Icons.Default.LocationOn
                NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Chat
                NotificationType.NEARBY_POINTS -> Icons.Default.Explore
                NotificationType.ACHIEVEMENT -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Turquoise,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )
                if (notification.isNew) {
                    Text(
                        text = "NUEVO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Turquoise
                    )
                }
            }
            Text(
                text = notification.description,
                fontSize = 14.sp,
                color = GrayText,
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(
                text = notification.time,
                fontSize = 12.sp,
                color = GrayText.copy(alpha = 0.6f)
            )
        }
    }
}
