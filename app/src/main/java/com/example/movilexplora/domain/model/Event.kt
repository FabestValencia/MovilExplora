package com.example.movilexplora.domain.model

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
    val isJoined: Boolean = false,
    val likedBy: List<String> = emptyList(), // Added for likes
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val creatorId: String = "admin", // For identifying who created it
    val status: PostStatus = PostStatus.PENDIENTE,
    val rejectionReason: String? = null
) {
    val isVerified: Boolean get() = status == PostStatus.VERIFICADO
}
