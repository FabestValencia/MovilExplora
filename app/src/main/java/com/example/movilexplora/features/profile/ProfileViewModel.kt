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
import com.example.movilexplora.domain.repository.EventRepository
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.R
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
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _userEvents = MutableStateFlow<List<Event>>(emptyList())
    val userEvents: StateFlow<List<Event>> = _userEvents.asStateFlow()

    init {
        loadUserProfile()

        // Removiendo los datos quemados a solicitud del usuario. Los eventos ahora
        // vendran organicos si se añaden o puedes enlazarlos con EventRepository
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val user = userRepository.findById(userId) ?: return@launch
            val roleMapping = when (user.role.name) {
                "ADMIN" -> resourceProvider.getString(R.string.role_admin)
                "MODERATOR" -> resourceProvider.getString(R.string.role_moderator)
                else -> resourceProvider.getString(R.string.role_local_ambassador)
            }

            val userPosts = postRepository.getPosts().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()

            var activeCount = 0
            var finishedCount = 0
            var pendingCount = 0

            userPosts.forEach { post ->
                when (post.status.name) {
                    "ACTIVO", "VERIFICADO" -> {
                        activeCount++
                    }
                    "FINALIZADO" -> finishedCount++
                    "PENDIENTE" -> {
                        pendingCount++
                    }
                }
            }

            val postCount = userPosts.size

            val userEventsList = eventRepository.getEvents().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()
            _userEvents.value = userEventsList

            val actualPoints = user.points
            val (calculatedLevel, calcTarget) = when {
                actualPoints < 100 -> Pair(ReputationLevel.TURISTA, 100)
                actualPoints < 500 -> Pair(ReputationLevel.EXPLORADOR, 500)
                actualPoints < 1000 -> Pair(ReputationLevel.AVENTURERO, 1000)
                else -> Pair(ReputationLevel.EMBAJADOR, 2000)
            }

            _userProfile.value = UserProfile(
                name = user.name,
                email = user.email,
                role = roleMapping,
                activePosts = activeCount,
                finishedPosts = finishedCount,
                pendingPosts = pendingCount,
                currentXp = actualPoints,
                maxXp = calcTarget,
                reputationLevel = calculatedLevel,
                achievements = listOf(
                    Achievement(resourceProvider.getString(R.string.achievement_1_title), resourceProvider.getString(R.string.achievement_1_desc), "celebration", postCount >= 1),
                    Achievement(resourceProvider.getString(R.string.achievement_2_title), resourceProvider.getString(R.string.achievement_2_desc), "verified", postCount >= 10),
                    Achievement(resourceProvider.getString(R.string.achievement_3_title), resourceProvider.getString(R.string.achievement_3_desc), "map", activeCount >= 5),
                    Achievement(resourceProvider.getString(R.string.achievement_4_title), resourceProvider.getString(R.string.achievement_4_desc), "stars", postCount >= 20)
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
