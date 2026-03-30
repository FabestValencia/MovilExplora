package com.example.movilexplora.features.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Achievement
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.ReputationLevel
import com.example.movilexplora.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    init {
        // Mock data based on the image
        _userProfile.value = UserProfile(
            name = "Jean Botsito",
            email = "bot.vaquero@example.com",
            role = "EMBAJADOR LOCAL",
            activePosts = 12,
            finishedPosts = 48,
            pendingPosts = 3,
            currentXp = 1250,
            maxXp = 2000,
            reputationLevel = ReputationLevel.EMBAJADOR,
            achievements = listOf(
                Achievement("Primera Publicación", "¡Tu primera aventura compartida!", "celebration", true),
                Achievement("10 Publicaciones", "Comunidad confiable y activa", "verified", true),
                Achievement("Maestro del Mapa", "Experto en navegación local", "map", true),
                Achievement("Explorador del Mes", "Sé el más activo este mes", "stars", false)
            )
        )
        
        _userEvents.value = listOf(
            Event(
                id = "evt_1",
                title = "Limpieza de Parque del Este",
                description = "Voluntariado para limpiar el parque central del barrio.",
                date = "22 de Diciembre",
                time = "08:00 AM",
                location = "Parque del Este",
                imageUrl = "",
                attendeesCount = 14,
                category = "Voluntariado",
                creatorId = "user_current"
            )
        )
    }

    fun deleteAccount() {
        // Logic to delete account
    }

    fun deleteEvent(eventId: String) {
        _userEvents.update { events ->
            events.filterNot { it.id == eventId }
        }
    }
}
