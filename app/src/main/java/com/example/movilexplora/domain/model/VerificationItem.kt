package com.example.movilexplora.domain.model

data class VerificationItem(
    val id: String,
    val title: String,
    val author: String,
    val authorAvatarUrl: String? = null,
    val timeAgo: String,
    val description: String,
    val imageUrl: String,
    val type: VerificationType,
    val badgeText: String,
    val category: String = "",
    val location: String = "",
    val price: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class VerificationType {
    LOCATION, PHOTO, REVIEW, EVENT
}
