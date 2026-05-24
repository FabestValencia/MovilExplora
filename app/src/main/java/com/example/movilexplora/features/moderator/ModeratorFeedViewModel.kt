package com.example.movilexplora.features.moderator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.NotificationHelper
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.*
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    
    private var allItems: List<VerificationItem> = emptyList()
    private val userNames = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        _state.update { it.copy(selectedFilter = resources.getString(R.string.filter_all)) }
        observePendingItems()
    }

    private fun observePendingItems() {
        combine(
            postRepository.getPosts(),
            eventRepository.getEvents(),
            userNames
        ) { posts, events, names ->
            val pendingPosts = posts.filter { it.status == PostStatus.PENDIENTE && !it.isDeleted }.map { post ->
                val userName = names[post.creatorId]
                val authorDisplay = if (userName != null) "${post.creatorId} ($userName)" else post.creatorId
                
                VerificationItem(
                    id = "POST_${post.id}",
                    title = post.title,
                    author = authorDisplay,
                    authorAvatarUrl = null, // Placeholder or fetch if needed
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
            
            val pendingEvents = events.filter { it.status == PostStatus.PENDIENTE && !it.isDeleted }.map { event ->
                val userName = names[event.creatorId]
                val authorDisplay = if (userName != null) "${event.creatorId} ($userName)" else event.creatorId

                VerificationItem(
                    id = "EVENT_${event.id}",
                    title = event.title,
                    author = authorDisplay,
                    authorAvatarUrl = null,
                    timeAgo = resources.getString(R.string.notification_time_recent),
                    description = event.description,
                    imageUrl = event.imageUrl,
                    type = VerificationType.EVENT,
                    badgeText = resources.getString(R.string.new_event),
                    category = event.category,
                    location = event.location,
                    price = resources.getString(R.string.price_free),
                    latitude = event.latitude,
                    longitude = event.longitude
                )
            }
            
            // Trigger background fetch for unknown user IDs
            val allCreatorIds = (posts.map { it.creatorId } + events.map { it.creatorId }).distinct()
            fetchMissingUserNames(allCreatorIds)

            pendingPosts + pendingEvents
        }.onEach { items ->
            allItems = items
            refreshState()
        }.launchIn(viewModelScope)
    }

    private fun fetchMissingUserNames(ids: List<String>) {
        val currentNames = userNames.value
        val missingIds = ids.filter { it !in currentNames }
        
        if (missingIds.isNotEmpty()) {
            viewModelScope.launch {
                val newNames = mutableMapOf<String, String>()
                missingIds.forEach { id ->
                    val user = userRepository.findById(id)
                    user?.let { newNames[id] = it.name }
                }
                if (newNames.isNotEmpty()) {
                    userNames.update { it + newNames }
                }
            }
        }
    }
    
    private fun refreshState() {
        val filterAll = resources.getString(R.string.filter_all)
        val filterLocations = resources.getString(R.string.filter_locations)
        val filterEvents = resources.getString(R.string.filter_events)

        val counts = mapOf(
            filterAll to allItems.size,
            filterLocations to allItems.count { it.type == VerificationType.LOCATION },
            filterEvents to allItems.count { it.type == VerificationType.EVENT }
        )
        
        val currentFilter = _state.value.selectedFilter
        var filteredItems = when (currentFilter) {
            filterLocations -> allItems.filter { it.type == VerificationType.LOCATION }
            filterEvents -> allItems.filter { it.type == VerificationType.EVENT }
            else -> allItems
        }
        
        if (!_state.value.sortByRecent) {
            filteredItems = filteredItems.reversed()
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

    fun rejectItem(itemId: String, reason: String) {
        updateStatus(itemId, PostStatus.RECHAZADO, reason)
    }
    
    private fun updateStatus(itemId: String, status: PostStatus, reason: String? = null) {
        viewModelScope.launch {
            if (itemId.startsWith("POST_")) {
                val realId = itemId.removePrefix("POST_")
                val postToUpdate = postRepository.getPosts().first().find { it.id == realId }
                postRepository.updatePostStatus(realId, status, reason)
                
                if (postToUpdate != null) {
                    notifyUser(postToUpdate.creatorId, postToUpdate.title, status)
                    if (status == PostStatus.VERIFICADO) userRepository.addPoints(postToUpdate.creatorId, 50)
                }
            } else if (itemId.startsWith("EVENT_")) {
                val realId = itemId.removePrefix("EVENT_")
                val eventToUpdate = eventRepository.getEvents().first().find { it.id == realId }
                eventRepository.updateEventStatus(realId, status, reason)

                if (eventToUpdate != null) {
                    notifyUser(eventToUpdate.creatorId, eventToUpdate.title, status)
                    if (status == PostStatus.VERIFICADO) userRepository.addPoints(eventToUpdate.creatorId, 50)
                }
            }
        }
    }

    private suspend fun notifyUser(userId: String, title: String, status: PostStatus) {
        val notifyTitle = if (status == PostStatus.VERIFICADO) resources.getString(R.string.notification_approved_title) else resources.getString(R.string.notification_rejected_title)
        val body = if (status == PostStatus.VERIFICADO) 
            resources.getString(R.string.notification_approved_desc, title)
            else resources.getString(R.string.notification_rejected_desc, title)
        
        notificationHelper.showStatusNotification(notifyTitle, body)
        notificationRepository.addNotification(
            userId = userId,
            notification = Notification(
                type = NotificationType.NEW_PLACE,
                title = notifyTitle,
                description = body,
                time = resources.getString(R.string.notification_time_recent),
                isNew = true
            )
        )
    }
}
