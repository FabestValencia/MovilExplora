package com.example.movilexplora.domain.repository

import com.example.movilexplora.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    suspend fun toggleFavorite(postId: String)
}
