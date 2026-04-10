package com.example.movilexplora.features.notifications

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsState(
    val recentNotifications: List<Notification> = emptyList(),
    val olderNotifications: List<Notification> = emptyList()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val userPosts = postRepository.getPosts().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()

            val dynamicRecent = mutableListOf<Notification>()
            val dynamicOlder = mutableListOf<Notification>()

            var notificationId = 1
            userPosts.forEach { post ->
                if (post.status.name == "VERIFICADO" || post.status.name == "ACTIVO") {
                    dynamicRecent.add(
                        Notification(
                            id = notificationId++.toString(),
                            type = NotificationType.NEW_PLACE,
                            title = "Publicación aprobada",
                            description = "Tu publicación \"${post.title}\" ha sido verificada.",
                            time = "Reciente",
                            isNew = true
                        )
                    )
                } else if (post.status.name == "PENDIENTE") {
                    dynamicOlder.add(
                        Notification(
                            id = notificationId++.toString(),
                            type = NotificationType.NEARBY_POINTS,
                            title = "Publicación en revisión",
                            description = "Tu publicación \"${post.title}\" está siendo revisada.",
                            time = "Pendiente",
                            isNew = false
                        )
                    )
                }
            }

            // Generate an achievement notification if they have any posts
            if (userPosts.isNotEmpty()) {
                dynamicOlder.add(
                    Notification(
                        id = notificationId++.toString(),
                        type = NotificationType.ACHIEVEMENT,
                        title = "Nuevos logros",
                        description = "Sigue publicando para aumentar tu reputación exploradora.",
                        time = "Ayer",
                        isNew = false
                    )
                )
            }

            _state.update {
                it.copy(
                    recentNotifications = dynamicRecent.reversed(),
                    olderNotifications = dynamicOlder.reversed()
                )
            }
        }
    }

    fun markAllAsRead() {
        _state.update { currentState ->
            currentState.copy(
                recentNotifications = currentState.recentNotifications.map { it.copy(isNew = false) }
            )
        }
    }
}
