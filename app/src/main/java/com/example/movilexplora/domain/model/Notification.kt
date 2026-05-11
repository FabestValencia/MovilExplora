package com.example.movilexplora.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val description: String,
    val time: String,
    val isNew: Boolean = false
)

enum class NotificationType {
    NEW_PLACE,
    COMMENT,
    NEARBY_POINTS,
    ACHIEVEMENT
}
