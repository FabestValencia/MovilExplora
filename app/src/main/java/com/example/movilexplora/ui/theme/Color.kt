package com.example.movilexplora.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.movilexplora.domain.model.ReputationLevel

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val Turquoise = Color(0xFF00FFD4)
val VerifiedBlue = Color(0xFF2196F3)
val DarkBlue = Color(0xFF0B1425)
val GrayText = Color(0xFF667085)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Gastronomía" -> Color(0xFFE91E63) // Pink
        "Cultura" -> Color(0xFF9C27B0) // Purple
        "Naturaleza" -> Color(0xFF4CAF50) // Green
        "Entretenimiento" -> Color(0xFFFF9800) // Orange
        "Historia" -> Color(0xFF795548) // Brown
        else -> Turquoise
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Gastronomía" -> Icons.Default.Restaurant
        "Cultura" -> Icons.Default.Museum
        "Naturaleza" -> Icons.Default.Park
        "Entretenimiento" -> Icons.Default.LocalActivity
        "Historia" -> Icons.Default.AccountBalance
        else -> Icons.Default.Category
    }
}

fun getReputationColor(level: ReputationLevel): Color {
    return when (level) {
        ReputationLevel.TURISTA -> Turquoise
        ReputationLevel.EXPLORADOR -> VerifiedBlue
        ReputationLevel.AVENTURERO -> Color(0xFF9C27B0) // Purple
        ReputationLevel.EMBAJADOR -> Color(0xFFFF9800) // Orange
    }
}

