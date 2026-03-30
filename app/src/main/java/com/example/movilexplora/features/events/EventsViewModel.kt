package com.example.movilexplora.features.events

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class EventsState(
    val events: List<Event> = emptyList(),
    val selectedFilter: String = "Todo"
)

@HiltViewModel
class EventsViewModel @Inject constructor() : ViewModel() {
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
                        endDate = "15 de Diciembre",
                        endTime = "06:00 PM",
                        location = "Plaza Central",
                        imageUrl = "",
                        attendeesCount = 120,
                        category = "Gastronomía",
                        status = PostStatus.VERIFICADO
                    ),
                    Event(
                        id = "2",
                        title = "Caminata al Mirador",
                        description = "Una ruta guiada para ver el atardecer.",
                        date = "18 de Diciembre",
                        time = "04:30 PM",
                        endDate = "18 de Diciembre",
                        endTime = "07:30 PM",
                        location = "Parque del Este",
                        imageUrl = "",
                        attendeesCount = 45,
                        category = "Naturaleza",
                        status = PostStatus.PENDIENTE
                    ),
                    Event(
                        id = "3",
                        title = "Tour Histórico",
                        description = "Recorre los monumentos más emblemáticos.",
                        date = "20 de Diciembre",
                        time = "09:00 AM",
                        endDate = "20 de Diciembre",
                        endTime = "01:00 PM",
                        location = "Casco Antiguo",
                        imageUrl = "",
                        attendeesCount = 30,
                        category = "Cultura",
                        status = PostStatus.VERIFICADO
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
