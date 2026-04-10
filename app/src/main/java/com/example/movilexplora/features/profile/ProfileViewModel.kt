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
import com.example.movilexplora.domain.repository.PostRepository
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
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
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

            val userPosts = postRepository.getPosts().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()

            var activeCount = 0
            var finishedCount = 0
            var pendingCount = 0
            var dynamicPoints = 0

            userPosts.forEach { post ->
                when (post.status.name) {
                    "ACTIVO", "VERIFICADO" -> {
                        activeCount++
                        dynamicPoints += 100
                    }
                    "FINALIZADO" -> finishedCount++
                    "PENDIENTE" -> {
                        pendingCount++
                        dynamicPoints += 50
                    }
                }
            }

            val postCount = userPosts.size

            _userProfile.value = UserProfile(
                name = user.name,
                email = user.email,
                role = roleMapping,
                activePosts = activeCount,
                finishedPosts = finishedCount,
                pendingPosts = pendingCount,
                currentXp = dynamicPoints.coerceAtLeast(10), // minimum 10 if no posts
                maxXp = 2000,
                reputationLevel = ReputationLevel.EMBAJADOR,
                achievements = listOf(
                    Achievement("Primera Publicación", "¡Tu primera aventura compartida!", "celebration", postCount >= 1),
                    Achievement("10 Publicaciones", "Comunidad confiable y activa", "verified", postCount >= 10),
                    Achievement("Maestro del Mapa", "Experto en navegación local", "map", activeCount >= 5),
                    Achievement("Explorador del Mes", "Sé el más activo este mes", "stars", postCount >= 20)
                )
            )
        }
    }

    fun deleteAccount() {
        // TODO Logic to delete account, la idea es que sea una eliminacion logica, no real, pues necesitamos
        //toda la informacion para
    }

    fun deleteEvent(eventId: String) {
        _userEvents.update { events ->
            events.filterNot { it.id == eventId }
        }
    }
}
