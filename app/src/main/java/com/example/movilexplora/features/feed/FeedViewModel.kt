package com.example.movilexplora.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Category(val name: String)

data class FeedState(
    val userName: String = "Jean Botsito",
    val categories: List<Category> = listOf(
        Category("Gastronomía"),
        Category("Cultura"),
        Category("Naturaleza"),
        Category("Entretenimiento"),
        Category("Historia")
    )
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    val posts: StateFlow<List<Post>> = postRepository.getPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            postRepository.toggleFavorite(postId)
        }
    }
}
