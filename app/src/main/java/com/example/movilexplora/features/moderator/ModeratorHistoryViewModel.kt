package com.example.movilexplora.features.moderator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
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
    private val eventRepository: EventRepository,
    private val resources: ResourceProvider
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
                    author = post.creatorId.ifEmpty { resources.getString(R.string.user_default_name) },
                    timeAgo = resources.getString(R.string.stat_time_recent),
                    description = post.description.ifEmpty { resources.getString(R.string.no_description) },
                    status = if (post.status == PostStatus.VERIFICADO) resources.getString(R.string.status_accepted_singular) else resources.getString(R.string.status_rejected_singular)
                )
            }

            val processedEvents = events.filter { it.status == PostStatus.VERIFICADO || it.status == PostStatus.RECHAZADO }
            val eventHistoryItems = processedEvents.map { event ->
                HistoryItem(
                    id = "EVENT_${event.id}",
                    type = VerificationType.EVENT,
                    badgeText = resources.getString(R.string.event_badge),
                    title = event.title,
                    author = resources.getString(R.string.organization_default_name),
                    timeAgo = resources.getString(R.string.stat_time_recent),
                    description = event.description.ifEmpty { resources.getString(R.string.no_description) },
                    status = if (event.status == PostStatus.VERIFICADO) resources.getString(R.string.status_accepted_singular) else resources.getString(R.string.status_rejected_singular)
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
        val filtered = if (filter == resources.getString(R.string.filter_all_es)) {
            allItems
        } else {
            allItems.filter { it.status + "s" == filter || it.status == filter }
        }

        val counts = mapOf(
            resources.getString(R.string.filter_all_es) to allItems.size,
            resources.getString(R.string.status_accepted_plural) to allItems.count { it.status == resources.getString(R.string.status_accepted_singular) },
            resources.getString(R.string.status_rejected_plural) to allItems.count { it.status == resources.getString(R.string.status_rejected_singular) }
        )

        _state.value = _state.value.copy(
            items = filtered,
            counts = counts
        )
    }
}
