package com.example.movilexplora.domain.repository

import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.Comment
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    fun getPost(id: String): Flow<Post?>
    fun getComments(postId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
    suspend fun toggleFavorite(postId: String, userId: String)
}
