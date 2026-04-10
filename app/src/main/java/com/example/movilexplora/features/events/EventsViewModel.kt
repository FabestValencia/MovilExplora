package com.example.movilexplora.features.events

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine
import com.example.movilexplora.data.local.dao.LikeDao
import com.example.movilexplora.data.local.entity.LikeEntity
import com.example.movilexplora.domain.repository.EventRepository

data class EventsState(
    val events: List<Event> = emptyList(),
    val selectedFilter: String = "Todo"
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val eventRepository: EventRepository,
    private val likeDao: LikeDao
) : ViewModel() {
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state.asStateFlow()

    private val _currentUserId = MutableStateFlow("guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            _currentUserId.value = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
        }
        
        viewModelScope.launch {
            combine(eventRepository.getEvents(), likeDao.getAllEventLikes()) { events, likes ->
                events.map { event ->
                    val eventLikes = likes.filter { it.itemId == event.id }.map { it.userId }.toSet()
                    event.copy(likedBy = eventLikes)
                }.sortedByDescending { it.likedBy.size }
            }.collect { combinedEvents ->
                _state.value = _state.value.copy(events = combinedEvents)
            }
        }
    }

    fun toggleFavorite(eventId: String) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            if (userId == "guest") return@launch

            val isLiked = likeDao.isLiked(eventId, userId, "EVENT") > 0
            if (isLiked) {
                likeDao.deleteLike(eventId, userId, "EVENT")
            } else {
                likeDao.insertLike(LikeEntity(eventId, userId, "EVENT"))
            }
        }
    }

    fun toggleJoinEvent(eventId: String) {
        // Now joining event should ideally be handled via Repository, but for this step we skip full integration of joined users or add it if needed.
    }

    fun onFilterSelected(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
    }
}
