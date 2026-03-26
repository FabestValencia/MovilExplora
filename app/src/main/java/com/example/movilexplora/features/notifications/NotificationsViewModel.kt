package com.example.movilexplora.features.notifications

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class NotificationsState(
    val recentNotifications: List<Notification> = emptyList(),
    val olderNotifications: List<Notification> = emptyList()
)

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        // Mock data based on the image
        _state.update {
            it.copy(
                recentNotifications = listOf(
                    Notification(
                        id = "1",
                        type = NotificationType.NEW_PLACE,
                        title = "¡Nuevo lugar cerca!",
                        description = "\"Cueva del Esplendor\" ha sido verificado.",
                        time = "Hace 5 min",
                        isNew = true
                    ),
                    Notification(
                        id = "2",
                        type = NotificationType.COMMENT,
                        title = "Comentarios recibidos",
                        description = "Alejandro G. comentó en tu publicación \"Museo del Oro\".",
                        time = "Hace 20 min",
                        isNew = true
                    )
                ),
                olderNotifications = listOf(
                    Notification(
                        id = "3",
                        type = NotificationType.NEARBY_POINTS,
                        title = "Nuevos puntos cercanos",
                        description = "Se han descubierto 3 nuevos senderos en tu área de exploración.",
                        time = "Hace 2 h",
                        isNew = false
                    ),
                    Notification(
                        id = "4",
                        type = NotificationType.ACHIEVEMENT,
                        title = "Logro desbloqueado",
                        description = "Has alcanzado el nivel \"Explorador de Plata\". ¡Sigue así!",
                        time = "Ayer",
                        isNew = false
                    ),
                    Notification(
                        id = "5",
                        type = NotificationType.COMMENT,
                        title = "Comentarios recibidos",
                        description = "Elena R. te preguntó sobre el clima en \"Páramo de Iguaque\".",
                        time = "Hace 2 días",
                        isNew = false
                    )
                )
            )
        }
    }

    fun markAllAsRead() {
        _state.update { currentState ->
            currentState.copy(
                recentNotifications = currentState.recentNotifications.map { it.copy(isNew = false) }
            )
        }
    }
}
