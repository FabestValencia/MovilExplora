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
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.UserRepository
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
            eventRepository.getEvents(),
            userRepository.users
        ) { posts, events, users ->
            val pendingPosts = posts.filter { it.status == PostStatus.PENDIENTE }.map { post ->
                val authorUser = users.find { it.id == post.creatorId }
                VerificationItem(
                    id = "POST_${post.id}", // Add prefix to identify type later
                    title = post.title,
                    author = authorUser?.name ?: post.creatorId,
                    authorAvatarUrl = authorUser?.profilePictureUrl,
                    timeAgo = resources.getString(R.string.notification_time_recent), 
                    description = "${post.location} - ${post.category}\n${resources.getString(R.string.price_label)} ${post.price}\n\n${resources.getString(R.string.description_label)}\n${post.description.ifEmpty { resources.getString(R.string.no_description) }}",
                    imageUrl = post.imageUrl,
                    type = VerificationType.LOCATION, // Or differentiate later
                    badgeText = resources.getString(R.string.new_location)
                )
            }
            
            val pendingEvents = events.filter { it.status == PostStatus.PENDIENTE }.map { event ->
                val authorUser = users.find { it.id == event.creatorId }
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
                    author = authorUser?.name ?: resources.getString(R.string.organization_default_name),
                    authorAvatarUrl = authorUser?.profilePictureUrl,
                    timeAgo = resources.getString(R.string.notification_time_recent),
                    description = "${event.date} • ${event.endDate.ifBlank { resources.getString(R.string.tbd) }}\n${resources.getString(R.string.published_label)} $publishDate\n${event.location}\n\n${event.description}",
                    imageUrl = event.imageUrl,
                    type = VerificationType.EVENT,
                    badgeText = resources.getString(R.string.new_event)
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
            resources.getString(R.string.filter_reviews) to allItems.count { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }, 
            resources.getString(R.string.filter_events) to allItems.count { it.type == VerificationType.EVENT }
        )
        
        // Filter items based on selected filter
        val currentFilter = _state.value.selectedFilter
        var filteredItems = when (currentFilter) {
            resources.getString(R.string.filter_locations) -> allItems.filter { it.type == VerificationType.LOCATION }
            resources.getString(R.string.filter_reviews) -> allItems.filter { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }
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
                    val title = if (status == PostStatus.VERIFICADO) "¡Lugar Aprobado!" else "Lugar Rechazado"
                    val body = if (status == PostStatus.VERIFICADO) 
                        "Tu publicación \"${postToUpdate.title}\" ha sido verificada." 
                        else "Tu publicación \"${postToUpdate.title}\" no pudo ser aprobada."
                    
                    notificationHelper.showStatusNotification(title, body)

                    if (status == PostStatus.VERIFICADO) {
                        userRepository.addPoints(postToUpdate.creatorId, 50)
                    }
                }
            } else if (itemId.startsWith("EVENT_")) {
                val realId = itemId.removePrefix("EVENT_")
                val eventToUpdate = eventRepository.getEvents().firstOrNull()?.find { it.id == realId }
                eventRepository.updateEventStatus(realId, status, reason)

                if (eventToUpdate != null) {
                    val title = if (status == PostStatus.VERIFICADO) "¡Evento Aprobado!" else "Evento Rechazado"
                    val body = if (status == PostStatus.VERIFICADO)
                        "Tu evento \"${eventToUpdate.title}\" ha sido verificado."
                        else "Tu evento \"${eventToUpdate.title}\" no pudo ser aprobado."
                    
                    notificationHelper.showStatusNotification(title, body)
                }
            }
        }
    }
}
