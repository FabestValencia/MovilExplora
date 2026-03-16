package com.example.movilexplora.core.domain.model

import com.example.movilexplora.core.domain.enums.UserRole

class User(id: String,
    name: String,
email: String,
val city: String,
val address: String,
val password: String,
val phoneNumber: String = "",
val profilePictureUrl: String = "",
val role: UserRole = UserRole.USER
) : Persona(id, name, email) {
}