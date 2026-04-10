package com.example.movilexplora.features.postdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Comment
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class PostDetailState(
    val post: Post? = null,
    val description: String = "",
    val comments: List<Comment> = emptyList()
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    private var currentUserId: String = "guest"

    init {
        viewModelScope.launch {
            currentUserId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
        }
    }

    fun loadPostDetail(postId: String) {
        val mockDescription = "Experimente las fascinantes aguas azules de esta cueva marina natural. La luz del sol, al atravesar una cavidad submarina, crea un reflejo azul que ilumina la caverna. Ideal para nadar y realizar excursiones guiadas en barco."

        viewModelScope.launch {
            postRepository.getPost(postId).collect { post ->
                _state.value = _state.value.copy(
                    post = post,
                    description = mockDescription
                )
            }
        }

        viewModelScope.launch {
            postRepository.getComments(postId).collect { comments ->
                _state.value = _state.value.copy(
                    comments = comments
                )
            }
        }
    }

    fun addComment(postId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val user = userRepository.findById(currentUserId)
            val currentUserName = user?.name ?: "Guest"
            val currentUserAvatar = user?.profilePictureUrl ?: ""

            val newComment = Comment(
                id = System.currentTimeMillis().toString(),
                postId = postId,
                userName = currentUserName,
                userAvatar = currentUserAvatar,
                date = "Ahora",
                content = content
            )
            postRepository.addComment(newComment)
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            postRepository.toggleFavorite(postId, currentUserId)
        }
    }

    fun isFavorite(post: Post?): Boolean {
        return post?.likedBy?.contains(currentUserId) == true
    }
}
