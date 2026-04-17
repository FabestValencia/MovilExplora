package com.example.movilexplora.features.eventdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

data class EventDetailState(
    val event: Event? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {
    private val _state = MutableStateFlow(EventDetailState())
    val state: StateFlow<EventDetailState> = _state.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val events = eventRepository.getEvents().firstOrNull() ?: emptyList()
            val event = events.find { it.id == eventId }
            _state.update { it.copy(event = event, isLoading = false) }
        }
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
