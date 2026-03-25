package com.example.movilexplora.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.repository.PostRepositoryImpl
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.usecase.GetPostsUseCase
import com.example.movilexplora.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {
    // Nota: En una app real usaríamos Inyección de Dependencias (Hilt/Koin)
    private val repository = PostRepositoryImpl()
    private val getPostsUseCase = GetPostsUseCase(repository)
    private val toggleFavoriteUseCase = ToggleFavoriteUseCase(repository)

    val posts: StateFlow<List<Post>> = getPostsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(postId)
        }
    }
}
