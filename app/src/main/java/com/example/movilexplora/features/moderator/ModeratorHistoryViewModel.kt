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
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ModeratorHistoryState(
    val items: List<HistoryItem> = emptyList(),
    val selectedFilter: String = "",
    val counts: Map<String, Int> = emptyMap()
)

data class HistoryItem(
    val id: String,
    val type: VerificationType,
    val badgeText: String,
    val title: String,
    val author: String,
    val authorAvatarUrl: String? = null,
    val timeAgo: String,
    val description: String,
    val status: String // "Aceptado" or "Rechazado"
)

@HiltViewModel
class ModeratorHistoryViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
) : ViewModel() {
    private val _state = MutableStateFlow(ModeratorHistoryState())
    val state: StateFlow<ModeratorHistoryState> = _state.asStateFlow()

    private var allItems = listOf<HistoryItem>()

    init {
        _state.update { it.copy(selectedFilter = resources.getString(R.string.filter_all_es)) }
        loadHistory()
    }

    private fun loadHistory() {
        combine(
            postRepository.getPosts(),
            eventRepository.getEvents(),
            userRepository.users
        ) { posts, events, users ->
            val processedPosts = posts.filter { it.status == PostStatus.VERIFICADO || it.status == PostStatus.RECHAZADO }
            val postHistoryItems = processedPosts.map { post ->
                val authorUser = users.find { it.id == post.creatorId }
                HistoryItem(
                    id = "POST_${post.id}",
                    type = VerificationType.LOCATION,
                    badgeText = post.category.uppercase(),
                    title = post.title,
                    author = authorUser?.name ?: post.creatorId.ifEmpty { resources.getString(R.string.user_default_name) },
                    authorAvatarUrl = authorUser?.profilePictureUrl,
                    timeAgo = resources.getString(R.string.stat_time_recent),
                    description = post.description.ifEmpty { resources.getString(R.string.no_description) },
                    status = if (post.status == PostStatus.VERIFICADO) resources.getString(R.string.status_accepted_singular) else resources.getString(R.string.status_rejected_singular)
                )
            }

            val processedEvents = events.filter { it.status == PostStatus.VERIFICADO || it.status == PostStatus.RECHAZADO }
            val eventHistoryItems = processedEvents.map { event ->
                val authorUser = users.find { it.id == event.creatorId }
                HistoryItem(
                    id = "EVENT_${event.id}",
                    type = VerificationType.EVENT,
                    badgeText = resources.getString(R.string.event_badge),
                    title = event.title,
                    author = authorUser?.name ?: resources.getString(R.string.organization_default_name),
                    authorAvatarUrl = authorUser?.profilePictureUrl,
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
        val filtered = when (filter) {
            resources.getString(R.string.filter_all_es) -> allItems
            resources.getString(R.string.status_accepted_plural) -> allItems.filter { it.status == resources.getString(R.string.status_accepted_singular) }
            resources.getString(R.string.status_rejected_plural) -> allItems.filter { it.status == resources.getString(R.string.status_rejected_singular) }
            else -> allItems
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
