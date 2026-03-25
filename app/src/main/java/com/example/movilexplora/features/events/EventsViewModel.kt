package com.example.movilexplora.features.events

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EventsState(
    val events: List<Event> = emptyList(),
    val selectedFilter: String = "Todo"
)

class EventsViewModel : ViewModel() {
    private val _state = MutableStateFlow(EventsState())
    val state: StateFlow<EventsState> = _state.asStateFlow()

    init {
        // Datos de prueba para Eventos
        _state.update {
            it.copy(
                events = listOf(
                    Event(
                        id = "1",
                        title = "Festival Gastronómico",
                        description = "Ven a disfrutar de los mejores platos locales.",
                        date = "15 de Diciembre",
                        time = "10:00 AM",
                        location = "Plaza Central",
                        imageUrl = "",
                        attendeesCount = 120,
                        category = "Gastronomía"
                    ),
                    Event(
                        id = "2",
                        title = "Caminata al Mirador",
                        description = "Una ruta guiada para ver el atardecer.",
                        date = "18 de Diciembre",
                        time = "04:30 PM",
                        location = "Parque del Este",
                        imageUrl = "",
                        attendeesCount = 45,
                        category = "Naturaleza"
                    ),
                    Event(
                        id = "3",
                        title = "Tour Histórico",
                        description = "Recorre los monumentos más emblemáticos.",
                        date = "20 de Diciembre",
                        time = "09:00 AM",
                        location = "Casco Antiguo",
                        imageUrl = "",
                        attendeesCount = 30,
                        category = "Cultura"
                    )
                )
            )
        }
    }

    fun toggleJoinEvent(eventId: String) {
        _state.update { currentState ->
            currentState.copy(
                events = currentState.events.map { event ->
                    if (event.id == eventId) {
                        val newIsJoined = !event.isJoined
                        event.copy(
                            isJoined = newIsJoined,
                            attendeesCount = if (newIsJoined) event.attendeesCount + 1 else event.attendeesCount - 1
                        )
                    } else event
                }
            )
        }
    }

    fun onFilterSelected(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
    }
}
