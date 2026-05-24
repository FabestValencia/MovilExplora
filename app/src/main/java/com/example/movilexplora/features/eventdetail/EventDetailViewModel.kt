package com.example.movilexplora.features.eventdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class EventDetailState(
    val event: Event? = null,
    val isLoading: Boolean = false,
    val isExpired: Boolean = false,
    val isOwner: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
) : ViewModel() {
    private val _state = MutableStateFlow(EventDetailState())
    val state: StateFlow<EventDetailState> = _state.asStateFlow()

    private var currentUserId: String = ""
    private val dateFormatter = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

    init {
        viewModelScope.launch {
            currentUserId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: ""
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            eventRepository.getEvents().collect { events ->
                val event = events.find { it.id == eventId }
                
                val expired = event?.let {
                    try {
                        val endDate = dateFormatter.parse(it.endDate)
                        endDate != null && endDate.before(atStartOfDay(Calendar.getInstance().time))
                    } catch (e: Exception) {
                        false
                    }
                } ?: false

                val isOwner = event?.creatorId == currentUserId

                _state.update { it.copy(event = event, isLoading = false, isExpired = expired, isOwner = isOwner) }
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
            val currentEvent = _state.value.event
            if (currentEvent != null && currentUserId.isNotEmpty()) {
                eventRepository.toggleFavorite(eventId, currentUserId)
            }
        }
    }

    fun markAsVisited() {
        viewModelScope.launch {
            if (currentUserId.isNotEmpty()) {
                userRepository.addPoints(currentUserId, 50) // 50 points for visiting events (higher than regular places)
            }
        }
    }

    fun isFavorite(event: Event?): Boolean {
        return event?.likedBy?.contains(currentUserId) == true
    }

    fun toggleJoin() {
        _state.update { currentState ->
            val event = currentState.event
            if (event != null) {
                currentState.copy(event = event.copy(isJoined = !event.isJoined))
            } else {
                currentState
            }
        }
    }
}
