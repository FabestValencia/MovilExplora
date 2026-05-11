package com.example.movilexplora.data.model

import com.example.movilexplora.domain.model.UserRole

data class UserSession(
    val userId: String,
    val role: UserRole
)
