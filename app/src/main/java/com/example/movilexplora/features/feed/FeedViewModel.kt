package com.example.movilexplora.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.movilexplora.data.datastore.SessionDataStore
import kotlinx.coroutines.flow.firstOrNull

data class Category(val name: String)

data class FeedState(
    val userName: String = "",
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
    private val postRepository: PostRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _currentUserId = MutableStateFlow("guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    val posts: StateFlow<List<Post>> = postRepository.getPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
            _currentUserId.value = userId
            
            if (userId != "guest") {
                val user = userRepository.findById(userId)
                if (user != null) {
                    val firstName = user.name.split(" ").firstOrNull() ?: ""
                    _state.value = _state.value.copy(userName = firstName)
                }
            }
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val session = sessionDataStore.sessionFlow.firstOrNull()
            val currentUserId = session?.userId ?: "guest"
            postRepository.toggleFavorite(postId, currentUserId)
        }
    }

    suspend fun getCurrentUserId(): String {
        return sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
    }
}
