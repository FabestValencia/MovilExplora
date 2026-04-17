package com.example.movilexplora.domain.model

enum class PostStatus {
    PENDIENTE,
    VERIFICADO,
    RECHAZADO
}

data class Post(
    val id: String,
    val title: String,
    val location: String,
    val rating: Double,
    val category: String,
    val price: String,
    val status: PostStatus = PostStatus.PENDIENTE,
    val imageUrl: String,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val likedBy: Set<String> = emptySet(),
    val distance: Float = 5f,
    val creatorId: String = "",
    val rejectionReason: String? = null
) {
    val isVerified: Boolean get() = status == PostStatus.VERIFICADO

    // Deprecated old property for backward compatibility where needed briefly.
    val isFavorite: Boolean get() = likedBy.isNotEmpty()
}
