package com.example.movilexplora.features.postdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Comment
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.NotificationRepository
import com.example.movilexplora.core.utils.NotificationHelper
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
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationHelper: NotificationHelper,
    private val resources: ResourceProvider
) : ViewModel() {
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    private var currentUserId: String = ""

    init {
        viewModelScope.launch {
            currentUserId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: resources.getString(R.string.guest_user_name)
        }
    }

    fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            postRepository.getPost(postId).collect { post ->
                _state.value = _state.value.copy(
                    post = post,
                    description = post?.description ?: ""
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
            val currentUserName = user?.name ?: resources.getString(R.string.guest_user_name)
            val currentUserAvatar = user?.profilePictureUrl ?: ""

            val newComment = Comment(
                id = System.currentTimeMillis().toString(),
                postId = postId,
                userName = currentUserName,
                userAvatar = currentUserAvatar,
                date = resources.getString(R.string.time_now),
                content = content
            )
            postRepository.addComment(newComment)
            userRepository.addPoints(currentUserId, 10) // 10 points for commenting

            // Notify post creator
            _state.value.post?.let { post ->
                if (post.creatorId != currentUserId) {
                    val title = resources.getString(R.string.notification_comment_title)
                    val body = resources.getString(R.string.notification_comment_desc, currentUserName, post.title)
                    
                    // Push (as per requirement: push for comments and status)
                    notificationHelper.showStatusNotification(title, body)

                    // Persist
                    notificationRepository.addNotification(
                        userId = post.creatorId,
                        notification = Notification(
                            type = NotificationType.COMMENT,
                            title = title,
                            description = body,
                            time = resources.getString(R.string.notification_time_recent),
                            isNew = true
                        )
                    )
                }
            }
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val currentPost = _state.value.post
            if (currentPost != null) {
                val wasLiked = currentPost.likedBy.contains(currentUserId)
                postRepository.toggleFavorite(postId, currentUserId)
                
                if (!wasLiked && currentPost.creatorId != currentUserId) {
                    userRepository.addPoints(currentPost.creatorId, 5) // 5 points to creator
                    
                    // Notify (Only Screen notification for Likes as per requirement)
                    val user = userRepository.findById(currentUserId)
                    val userName = user?.name ?: resources.getString(R.string.guest_user_name)
                    
                    notificationRepository.addNotification(
                        userId = currentPost.creatorId,
                        notification = Notification(
                            type = NotificationType.LIKE,
                            title = resources.getString(R.string.notification_like_title),
                            description = resources.getString(R.string.notification_like_desc, userName, currentPost.title),
                            time = resources.getString(R.string.notification_time_recent),
                            isNew = true
                        )
                    )
                }
            }
        }
    }

    fun markAsVisited() {
        viewModelScope.launch {
            userRepository.addPoints(currentUserId, 20) // 20 points for visiting verified places
        }
    }

    fun isFavorite(post: Post?): Boolean {
        return post?.likedBy?.contains(currentUserId) == true
    }

    fun updatePostStatus(postId: String, status: PostStatus, reason: String? = null) {
        viewModelScope.launch {
            postRepository.updatePostStatus(postId, status, reason)
        }
    }
}
