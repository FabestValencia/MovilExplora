package com.example.movilexplora.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(id: String): Flow<com.example.movilexplora.domain.model.User?>
    suspend fun save(user: com.example.movilexplora.domain.model.User)
    suspend fun findById(id: String): com.example.movilexplora.domain.model.User?
    suspend fun login(email: String, password: String): com.example.movilexplora.domain.model.User?
    suspend fun loginWithGoogle(idToken: String): com.example.movilexplora.domain.model.User?
    suspend fun addPoints(userId: String, points: Int)
    suspend fun sendPasswordResetEmail(email: String)
    suspend fun updateFcmToken(userId: String, token: String)
}
