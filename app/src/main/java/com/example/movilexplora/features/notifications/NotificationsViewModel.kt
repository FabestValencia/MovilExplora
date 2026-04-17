package com.example.movilexplora.features.notifications

import android.content.Context
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

import com.example.movilexplora.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.model.PostStatus
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
    private val postRepository: PostRepository,
    @ApplicationContext private val context: Context
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
                when (post.status) {
                    PostStatus.VERIFICADO -> {
                        dynamicRecent.add(
                            Notification(
                                id = (notificationId++).toString(),
                                type = NotificationType.NEW_PLACE,
                                title = context.getString(R.string.notification_approved_title),
                                description = context.getString(R.string.notification_approved_desc, post.title),
                                time = context.getString(R.string.notification_time_recent),
                                isNew = true
                            )
                        )
                    }
                    PostStatus.PENDIENTE -> {
                        dynamicOlder.add(
                            Notification(
                                id = (notificationId++).toString(),
                                type = NotificationType.NEARBY_POINTS,
                                title = context.getString(R.string.notification_pending_title),
                                description = context.getString(R.string.notification_pending_desc, post.title),
                                time = context.getString(R.string.notification_time_pending),
                                isNew = false
                            )
                        )
                    }
                    PostStatus.RECHAZADO -> {
                        dynamicOlder.add(
                            Notification(
                                id = (notificationId++).toString(),
                                type = NotificationType.NEARBY_POINTS,
                                title = context.getString(R.string.notification_rejected_title),
                                description = context.getString(R.string.notification_rejected_desc, post.title),
                                time = context.getString(R.string.notification_time_recent),
                                isNew = false
                            )
                        )
                    }
                }
            }

            // Generate an achievement notification if they have any posts
            if (userPosts.isNotEmpty()) {
                dynamicOlder.add(
                    Notification(
                        id = (notificationId++).toString(),
                        type = NotificationType.ACHIEVEMENT,
                        title = context.getString(R.string.notification_achievement_title),
                        description = context.getString(R.string.notification_achievement_desc),
                        time = context.getString(R.string.notification_time_yesterday),
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
