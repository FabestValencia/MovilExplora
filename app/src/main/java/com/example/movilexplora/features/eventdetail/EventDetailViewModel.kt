package com.example.movilexplora.features.eventdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EventDetailState(
    val event: Event? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class EventDetailViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(EventDetailState())
    val state: StateFlow<EventDetailState> = _state.asStateFlow()

    fun loadEvent(eventId: String) {
        // Mock data
        _state.update { it.copy(isLoading = true) }
        
        val event = Event(
            id = eventId,
            title = "Festival de Gastronomía",
            description = "Un evento para disfrutar de la mejor comida típica, con degustaciones de chefs reconocidos y demostraciones en vivo. Ven preparado para un día lleno de sabor y música.",
            date = "15 Dic 2026",
            time = "10:00 AM",
            endDate = "15 Dic 2026",
            endTime = "06:00 PM",
            location = "Plaza Central, Ciudad de México",
            imageUrl = "url",
            attendeesCount = 120,
            category = "Gastronomía",
            status = PostStatus.VERIFICADO
        )
        
        _state.update { it.copy(event = event, isLoading = false) }
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
