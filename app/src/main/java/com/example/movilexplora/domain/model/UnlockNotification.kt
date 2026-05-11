package com.example.movilexplora.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class UnlockNotification(
    val title: String, // e.g., "LOGRO DESBLOQUEADO" or "NUEVO NIVEL"
    val name: String,  // e.g., "Explorador Nato"
    val icon: ImageVector,
    val date: String,
    val xpEarned: String,
    val footerText: String = "ESTE LOGRO ES PERMANENTE EN TU PERFIL"
)
