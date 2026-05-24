package com.example.movilexplora.domain.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.Exclude

data class Event(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val endDate: String = "",
    val endTime: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val attendeesCount: Int = 0,
    @get:PropertyName("isJoined") @set:PropertyName("isJoined") var isJoined: Boolean = false,
    val likedBy: List<String> = emptyList(), // Added for likes
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val creatorId: String = "admin", // For identifying who created it
    val status: PostStatus = PostStatus.PENDIENTE,
    val rejectionReason: String? = null,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted") var isDeleted: Boolean = false
) {
    @PropertyName("deleted")
    fun setDeletedFromFirestore(value: Boolean) { this.isDeleted = value }

    @PropertyName("joined")
    fun setJoinedFromFirestore(value: Boolean) { this.isJoined = value }

    @get:Exclude
    @set:Exclude
    var isVerified: Boolean
        get() = status == PostStatus.VERIFICADO
        set(_) {}

    @PropertyName("verified")
    fun setVerifiedFromFirestore(value: Boolean) { /* Calculado por status */ }
}
