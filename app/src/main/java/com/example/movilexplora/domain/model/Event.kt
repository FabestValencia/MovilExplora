package com.example.movilexplora.domain.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
    val imageUrl: String,
    val attendeesCount: Int,
    val isJoined: Boolean = false,
    val category: String,
    val creatorId: String = "admin" // For identifying who created it
)
