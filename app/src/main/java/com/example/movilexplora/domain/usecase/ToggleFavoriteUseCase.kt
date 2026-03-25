package com.example.movilexplora.domain.usecase

import com.example.movilexplora.domain.repository.PostRepository

class ToggleFavoriteUseCase(private val repository: PostRepository) {
    suspend operator fun invoke(postId: String) = repository.toggleFavorite(postId)
}
