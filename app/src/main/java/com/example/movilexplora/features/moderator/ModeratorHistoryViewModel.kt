package com.example.movilexplora.features.moderator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.domain.model.VerificationType
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class ModeratorHistoryState(
    val items: List<HistoryItem> = emptyList(),
    val selectedFilter: String = "Todo",
    val counts: Map<String, Int> = mapOf("Todo" to 0, "Aceptados" to 0, "Rechazados" to 0)
)

data class HistoryItem(
    val id: String,
    val type: VerificationType,
    val badgeText: String,
    val title: String,
    val author: String,
    val timeAgo: String,
    val description: String,
    val status: String // "Aceptado" or "Rechazado"
)

@HiltViewModel
class ModeratorHistoryViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ModeratorHistoryState())
    val state: StateFlow<ModeratorHistoryState> = _state.asStateFlow()

    private var allItems = listOf<HistoryItem>()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        combine(
            postRepository.getPosts(),
            eventRepository.getEvents()
        ) { posts, events ->
            val processedPosts = posts.filter { it.status == PostStatus.VERIFICADO || it.status == PostStatus.RECHAZADO }
            val postHistoryItems = processedPosts.map { post ->
                HistoryItem(
                    id = "POST_${post.id}",
                    type = VerificationType.LOCATION,
                    badgeText = post.category.uppercase(),
                    title = post.title,
                    author = post.creatorId.ifEmpty { "Usuario" },
                    timeAgo = "Reciente",
                    description = post.description.ifEmpty { "Sin descripción" },
                    status = if (post.status == PostStatus.VERIFICADO) "Aceptado" else "Rechazado"
                )
            }

            val processedEvents = events.filter { it.status == PostStatus.VERIFICADO || it.status == PostStatus.RECHAZADO }
            val eventHistoryItems = processedEvents.map { event ->
                HistoryItem(
                    id = "EVENT_${event.id}",
                    type = VerificationType.EVENT,
                    badgeText = "EVENTO",
                    title = event.title,
                    author = "Organización",
                    timeAgo = "Reciente",
                    description = event.description.ifEmpty { "Sin descripción" },
                    status = if (event.status == PostStatus.VERIFICADO) "Aceptado" else "Rechazado"
                )
            }

            allItems = (postHistoryItems + eventHistoryItems).reversed() // Show newest first
            updateState()
        }.launchIn(viewModelScope)
    }

    fun onFilterSelected(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
        updateState(filter)
    }

    private fun updateState(filter: String = _state.value.selectedFilter) {
        val filtered = if (filter == "Todo") {
            allItems
        } else {
            allItems.filter { it.status + "s" == filter || it.status == filter }
        }

        val counts = mapOf(
            "Todo" to allItems.size,
            "Aceptados" to allItems.count { it.status == "Aceptado" },
            "Rechazados" to allItems.count { it.status == "Rechazado" }
        )

        _state.value = _state.value.copy(
            items = filtered,
            counts = counts
        )
    }
}
