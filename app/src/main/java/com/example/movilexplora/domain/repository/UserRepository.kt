package com.example.movilexplora.domain.repository

import com.example.movilexplora.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val users: StateFlow<List<User>>
    suspend fun save(user: User)
    suspend fun findById(id: String): User?
    suspend fun login(email: String, password: String): User?
}
