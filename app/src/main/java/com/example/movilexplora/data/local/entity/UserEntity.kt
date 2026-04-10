package com.example.movilexplora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.model.UserRole

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val email: String,
    val password: String,
    val profilePictureUrl: String,
    val role: String
)

fun UserEntity.toDomainModel() = User(
    id = id,
    name = name,
    city = city,
    address = address,
    email = email,
    password = password,
    profilePictureUrl = profilePictureUrl,
    role = UserRole.valueOf(role)
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    city = city,
    address = address,
    email = email,
    password = password ?: "",
    profilePictureUrl = profilePictureUrl,
    role = role.name
)

