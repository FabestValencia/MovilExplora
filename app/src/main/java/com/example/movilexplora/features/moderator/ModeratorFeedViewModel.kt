package com.example.movilexplora.features.moderator

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.domain.model.VerificationType
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

data class ModeratorFeedState(
    val items: List<VerificationItem> = emptyList(),
    val selectedFilter: String = "Todo",
    val counts: Map<String, Int> = mapOf("Todo" to 0, "Lugares" to 0, "Reseñas" to 0, "Eventos" to 0),
    val sortByRecent: Boolean = true
)

@HiltViewModel
class ModeratorFeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ModeratorFeedState())
    val state: StateFlow<ModeratorFeedState> = _state.asStateFlow()
    
    // Store all items internally
    private var allItems: List<VerificationItem> = emptyList()

    init {
        combine(
            postRepository.getPosts(),
            eventRepository.getEvents()
        ) { posts, events ->
            val pendingPosts = posts.filter { it.status == PostStatus.PENDIENTE }.map { post ->
                VerificationItem(
                    id = "POST_${post.id}", // Add prefix to identify type later
                    title = post.title,
                    author = post.creatorId,
                    timeAgo = "Reciente", 
                    description = "${post.location} - ${post.category}\n\nPrice: ${post.price}",
                    imageUrl = post.imageUrl,
                    type = VerificationType.LOCATION, // Or differentiate later
                    badgeText = "New Location"
                )
            }
            
            val pendingEvents = events.filter { it.status == PostStatus.PENDIENTE }.map { event ->
                VerificationItem(
                    id = "EVENT_${event.id}",
                    title = event.title,
                    author = "Organización",
                    timeAgo = "Reciente",
                    description = "${event.date} • ${event.time}\n${event.location}\n\n${event.description}",
                    imageUrl = event.imageUrl,
                    type = VerificationType.EVENT,
                    badgeText = "New Event"
                )
            }
            
            allItems = pendingPosts + pendingEvents
            refreshState()
        }.launchIn(viewModelScope)
    }
    
    private fun refreshState() {
        // Calculate counts
        val counts = mutableMapOf(
            "Todo" to allItems.size,
            "Lugares" to allItems.count { it.type == VerificationType.LOCATION },
            "Reseñas" to allItems.count { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }, // Assuming PHOTO counts as Review-like content or separate? Let's simplify
            "Eventos" to allItems.count { it.type == VerificationType.EVENT }
        )
        
        // Filter items based on selected filter
        val currentFilter = _state.value.selectedFilter
        var filteredItems = when (currentFilter) {
            "Lugares" -> allItems.filter { it.type == VerificationType.LOCATION }
            "Reseñas" -> allItems.filter { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }
            "Eventos" -> allItems.filter { it.type == VerificationType.EVENT }
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

    fun rejectItem(itemId: String) {
        updateStatus(itemId, PostStatus.RECHAZADO)
    }
    
    private fun updateStatus(itemId: String, status: PostStatus) {
        viewModelScope.launch {
            if (itemId.startsWith("POST_")) {
                val realId = itemId.removePrefix("POST_")
                postRepository.updatePostStatus(realId, status)
            } else if (itemId.startsWith("EVENT_")) {
                val realId = itemId.removePrefix("EVENT_")
                eventRepository.updateEventStatus(realId, status)
            }
        }
    }
}
