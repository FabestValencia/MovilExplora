package com.example.movilexplora.features.profile

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Achievement
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.ReputationLevel
import com.example.movilexplora.domain.model.UserProfile
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.UserRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    init {
        loadUserProfile()

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

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val user = userRepository.findById(userId) ?: return@launch
            val roleMapping = when (user.role.name) {
                "ADMIN" -> "ADMINISTRADOR"
                "MODERATOR" -> "MODERADOR"
                else -> "EMBAJADOR LOCAL"
            }

            _userProfile.value = UserProfile(
                name = user.name,
                email = user.email,
                role = roleMapping,
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
        }
    }

    fun deleteAccount() {
        // TODO Logic to delete account
    }

    fun deleteEvent(eventId: String) {
        _userEvents.update { events ->
            events.filterNot { it.id == eventId }
        }
    }
}
