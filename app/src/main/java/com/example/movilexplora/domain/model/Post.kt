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
    val isFavorite: Boolean = false
) {
    val isVerified: Boolean get() = status == PostStatus.VERIFICADO
}
