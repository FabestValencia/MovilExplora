package com.example.movilexplora.features.moderator

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.NotificationHelper
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.domain.model.VerificationType
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.firstOrNull

data class ModeratorFeedState(
    val items: List<VerificationItem> = emptyList(),
    val selectedFilter: String = "",
    val counts: Map<String, Int> = emptyMap(),
    val sortByRecent: Boolean = true
)

@HiltViewModel
class ModeratorFeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val resources: ResourceProvider,
    private val notificationHelper: NotificationHelper
) : ViewModel() {
    private val _state = MutableStateFlow(ModeratorFeedState())
    val state: StateFlow<ModeratorFeedState> = _state.asStateFlow()
    
    // Store all items internally
    private var allItems: List<VerificationItem> = emptyList()

    init {
        _state.update { it.copy(selectedFilter = resources.getString(R.string.filter_all)) }

        combine(
            postRepository.getPosts(),
            eventRepository.getEvents()
        ) { posts, events ->
            val pendingPosts = posts.filter { it.status == PostStatus.PENDIENTE }.map { post ->
                VerificationItem(
                    id = "POST_${post.id}",
                    title = post.title,
                    author = post.creatorId, // We'll use ID or fetch name on demand
                    authorAvatarUrl = null,
                    timeAgo = resources.getString(R.string.notification_time_recent), 
                    description = post.description.ifEmpty { resources.getString(R.string.no_description) },
                    imageUrl = post.imageUrl,
                    type = VerificationType.LOCATION,
                    badgeText = resources.getString(R.string.new_location),
                    category = post.category,
                    location = post.location,
                    price = post.price,
                    latitude = post.latitude,
                    longitude = post.longitude
                )
            }
            
            val pendingEvents = events.filter { it.status == PostStatus.PENDIENTE }.map { event ->
                val publishDate = try {
                    val timeInMillis = event.id.toLong()
                    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                    formatter.format(java.util.Date(timeInMillis))
                } catch (e: Exception) {
                    resources.getString(R.string.unknown)
                }

                VerificationItem(
                    id = "EVENT_${event.id}",
                    title = event.title,
                    author = event.creatorId,
                    authorAvatarUrl = null,
                    timeAgo = resources.getString(R.string.notification_time_recent),
                    description = event.description,
                    imageUrl = event.imageUrl,
                    type = VerificationType.EVENT,
                    badgeText = resources.getString(R.string.new_event),
                    category = event.category,
                    location = event.location,
                    price = resources.getString(R.string.price_free), // Assuming events are free or price not stored yet
                    latitude = event.latitude,
                    longitude = event.longitude
                )
            }
            
            allItems = pendingPosts + pendingEvents
            refreshState()
        }.launchIn(viewModelScope)
    }
    
    private fun refreshState() {
        // Calculate counts
        val counts = mutableMapOf(
            resources.getString(R.string.filter_all) to allItems.size,
            resources.getString(R.string.filter_locations) to allItems.count { it.type == VerificationType.LOCATION },
            resources.getString(R.string.filter_events) to allItems.count { it.type == VerificationType.EVENT }
        )
        
        // Filter items based on selected filter
        val currentFilter = _state.value.selectedFilter
        var filteredItems = when (currentFilter) {
            resources.getString(R.string.filter_locations) -> allItems.filter { it.type == VerificationType.LOCATION }
            resources.getString(R.string.filter_events) -> allItems.filter { it.type == VerificationType.EVENT }
            else -> allItems
        }
        
        // Sort items
        filteredItems = if (_state.value.sortByRecent) {
            filteredItems // Assuming default list is mostly "newest first" logic from IDs or times.
        } else {
            filteredItems.reversed()
        }

        _state.update {
            it.copy(
                items = filteredItems,
                counts = counts
            )
        }
    }

    fun onFilterSelected(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
        refreshState()
    }

    fun onSortOrderChanged(sortByRecent: Boolean) {
        _state.update { it.copy(sortByRecent = sortByRecent) }
        refreshState()
    }

    fun verifyItem(itemId: String) {
        updateStatus(itemId, PostStatus.VERIFICADO)
    }

    fun approveItem(itemId: String) {
        updateStatus(itemId, PostStatus.VERIFICADO)
    }

    fun rejectItem(itemId: String, reason: String) {
        updateStatus(itemId, PostStatus.RECHAZADO, reason)
    }
    
    private fun updateStatus(itemId: String, status: PostStatus, reason: String? = null) {
        viewModelScope.launch {
            if (itemId.startsWith("POST_")) {
                val realId = itemId.removePrefix("POST_")
                val postToUpdate = postRepository.getPosts().firstOrNull()?.find { it.id == realId }
                postRepository.updatePostStatus(realId, status, reason)
                
                if (postToUpdate != null) {
                    val title = if (status == PostStatus.VERIFICADO) resources.getString(R.string.notification_approved_title) else resources.getString(R.string.notification_rejected_title)
                    val body = if (status == PostStatus.VERIFICADO) 
                        resources.getString(R.string.notification_approved_desc, postToUpdate.title)
                        else resources.getString(R.string.notification_rejected_desc, postToUpdate.title)
                    
                    // Push notification
                    notificationHelper.showStatusNotification(title, body)

                    // Persist notification for screen
                    notificationRepository.addNotification(
                        userId = postToUpdate.creatorId,
                        notification = Notification(
                            type = NotificationType.NEW_PLACE,
                            title = title,
                            description = body,
                            time = resources.getString(R.string.notification_time_recent),
                            isNew = true
                        )
                    )

                    if (status == PostStatus.VERIFICADO) {
                        userRepository.addPoints(postToUpdate.creatorId, 50)
                    }
                }
            } else if (itemId.startsWith("EVENT_")) {
                val realId = itemId.removePrefix("EVENT_")
                val eventToUpdate = eventRepository.getEvents().firstOrNull()?.find { it.id == realId }
                eventRepository.updateEventStatus(realId, status, reason)

                if (eventToUpdate != null) {
                    val title = if (status == PostStatus.VERIFICADO) resources.getString(R.string.notification_approved_title) else resources.getString(R.string.notification_rejected_title)
                    val body = if (status == PostStatus.VERIFICADO)
                        resources.getString(R.string.notification_approved_desc, eventToUpdate.title)
                        else resources.getString(R.string.notification_rejected_desc, eventToUpdate.title)
                    
                    // Push notification
                    notificationHelper.showStatusNotification(title, body)

                    // Persist notification
                    notificationRepository.addNotification(
                        userId = eventToUpdate.creatorId,
                        notification = Notification(
                            type = NotificationType.NEW_PLACE,
                            title = title,
                            description = body,
                            time = resources.getString(R.string.notification_time_recent),
                            isNew = true
                        )
                    )

                    if (status == PostStatus.VERIFICADO) {
                        userRepository.addPoints(eventToUpdate.creatorId, 50)
                    }
                }
            }
        }
    }
}
