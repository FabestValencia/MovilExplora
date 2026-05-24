package com.example.movilexplora.features.events

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
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

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class EventsState(
    val events: List<Event> = emptyList(),
    val selectedFilter: String = "",
    val searchQuery: String = ""
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val eventRepository: EventRepository,
    private val likeDao: LikeDao,
    private val resources: ResourceProvider
) : ViewModel() {
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state.asStateFlow()

    private val _currentUserId = MutableStateFlow("guest")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val dateFormatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

    init {
        _state.update { it.copy(selectedFilter = resources.getString(R.string.filter_all)) }

        viewModelScope.launch {
            _currentUserId.value = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
        }
        
        viewModelScope.launch {
            combine(eventRepository.getEvents(), likeDao.getAllEventLikes(), _state) { events, likes, currentState ->
                val query = currentState.searchQuery
                val currentTime = atStartOfDay(Calendar.getInstance().time)

                events.map { event ->
                    val eventLikes = likes.filter { it.itemId == event.id }.map { it.userId }
                    event.copy(likedBy = eventLikes)
                }.filter { event ->
                    // Regla de Negocio: Rango de fechas
                    val isWithinDateRange = try {
                        val start = dateFormatter.parse(event.date)
                        val end = dateFormatter.parse(event.endDate)
                        
                        start != null && end != null && 
                        !currentTime.before(start) && !currentTime.after(end)
                    } catch (e: Exception) {
                        true 
                    }

                    if (event.isDeleted) return@filter false

                    // Regla de Negocio: SOLO VERIFICADOS y EN RANGO DE FECHAS
                    val isVisible = event.status == PostStatus.VERIFICADO && isWithinDateRange
                    val matchesSearch = query.isBlank() || event.title.contains(query, ignoreCase = true)
                    
                    isVisible && matchesSearch
                }.sortedByDescending { it.likedBy.size }
            }.collect { combinedEvents ->
                _state.value = _state.value.copy(events = combinedEvents)
            }
        }
    }

    private fun atStartOfDay(date: java.util.Date): java.util.Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
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

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }
}
