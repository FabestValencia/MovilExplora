package com.example.movilexplora.domain.model

data class VerificationItem(
    val id: String,
    val title: String,
    val author: String,
    val timeAgo: String,
    val description: String,
    val imageUrl: String,
    val type: VerificationType,
    val badgeText: String
)

enum class VerificationType {
    LOCATION, PHOTO, REVIEW, EVENT
}
