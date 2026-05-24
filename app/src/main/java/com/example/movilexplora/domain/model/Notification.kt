package com.example.movilexplora.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.NEARBY_POINTS,
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val isNew: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

enum class NotificationType {
    NEW_PLACE,
    COMMENT,
    NEARBY_POINTS,
    ACHIEVEMENT,
    LIKE
}
