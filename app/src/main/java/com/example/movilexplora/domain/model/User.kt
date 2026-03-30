package com.example.movilexplora.domain.model

enum class UserRole {
    EXPLORER,
    MODERATOR,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String = "",
    val city: String = "",
    val address: String = "",
    val profilePictureUrl: String = "",
    val role: UserRole = UserRole.EXPLORER
)
