package com.example.movilexplora.domain.usecase

import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow

class GetPostsUseCase(private val repository: PostRepository) {
    operator fun invoke(): Flow<List<Post>> = repository.getPosts()
}
