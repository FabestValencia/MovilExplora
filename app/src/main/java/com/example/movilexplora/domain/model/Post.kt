package com.example.movilexplora.domain.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

enum class PostStatus {
    PENDIENTE,
    VERIFICADO,
    RECHAZADO
}

data class Post(
    var id: String = "",
    val title: String = "",
    val location: String = "",
    val rating: Double = 0.0,
    val category: String = "",
    val price: String = "",
    val status: PostStatus = PostStatus.PENDIENTE,
    val imageUrl: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val likedBy: List<String> = emptyList(),
    val distance: Float = 5f,
    val creatorId: String = "",
    val rejectionReason: String? = null,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted") var isDeleted: Boolean = false
) {
    @PropertyName("deleted")
    fun setDeletedFromFirestore(value: Boolean) { this.isDeleted = value }

    @get:Exclude
    @set:Exclude
    var isVerified: Boolean
        get() = status == PostStatus.VERIFICADO
        set(_) {}

    @PropertyName("verified")
    fun setVerifiedFromFirestore(value: Boolean) { /* Calculado por status */ }

    // Deprecated old property for backward compatibility where needed briefly.
    @get:Exclude
    @set:Exclude
    var isFavorite: Boolean
        get() = likedBy.isNotEmpty()
        set(_) {}

    @PropertyName("favorite")
    fun setFavoriteFromFirestore(value: Boolean) { /* Calculado por likedBy */ }
}
