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

    private val _userPosts = MutableStateFlow<List<com.example.movilexplora.domain.model.Post>>(emptyList())
    val userPosts: StateFlow<List<com.example.movilexplora.domain.model.Post>> = _userPosts.asStateFlow()

    private var profileJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            sessionDataStore.sessionFlow.collect { session ->
                val userId = session?.userId
                if (userId != null && userId != "guest") {
                    profileJob?.cancel()
                    profileJob = loadDataForUser(userId)
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    private fun loadDataForUser(userId: String): kotlinx.coroutines.Job {
        return viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                userRepository.users,
                postRepository.getPosts(),
                eventRepository.getEvents()
            ) { users, posts, events ->
                val user = users.find { it.id == userId }
                Triple(user, posts, events)
            }.collect { (user, posts, events) ->
                if (user != null) {
                    updateProfileStateWithData(user, posts, events)
                } else {
                    // Intento de carga directa si no está en la lista general
                    userRepository.findById(userId)?.let { directUser ->
                        updateProfileStateWithData(directUser, posts, events)
                    }
                }
            }
        }
    }

    private fun updateProfileStateWithData(
        user: com.example.movilexplora.domain.model.User,
        allPosts: List<com.example.movilexplora.domain.model.Post>,
        allEvents: List<Event>
    ) {
        val roleMapping = when (user.role) {
            "ADMIN" -> resourceProvider.getString(R.string.role_admin)
            else -> resourceProvider.getString(R.string.role_local_ambassador)
        }

        val userPostsList = allPosts.filter { it.creatorId == user.id }
        _userPosts.value = userPostsList

        var activeCount = 0
        var finishedCount = 0
        var pendingCount = 0
        var rejectedCount = 0

        userPostsList.forEach { post ->
            when (post.status.name) {
                "VERIFICADO" -> activeCount++
                "PENDIENTE" -> pendingCount++
                "RECHAZADO" -> rejectedCount++
            }
        }

        val userEventsList = allEvents.filter { it.creatorId == user.id }
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
            profilePictureUrl = user.profilePictureUrl,
            activePosts = activeCount,
            finishedPosts = finishedCount,
            pendingPosts = pendingCount,
            rejectedPosts = rejectedCount,
            currentXp = actualPoints,
            maxXp = calcTarget,
            reputationLevel = calculatedLevel,
            achievements = listOf(
                Achievement(resourceProvider.getString(R.string.achievement_1_title), resourceProvider.getString(R.string.achievement_1_desc), "celebration", userPostsList.size >= 1),
                Achievement(resourceProvider.getString(R.string.achievement_2_title), resourceProvider.getString(R.string.achievement_2_desc), "verified", userPostsList.size >= 10),
                Achievement(resourceProvider.getString(R.string.achievement_3_title), resourceProvider.getString(R.string.achievement_3_desc), "map", activeCount >= 5),
                Achievement(resourceProvider.getString(R.string.achievement_4_title), resourceProvider.getString(R.string.achievement_4_desc), "stars", userPostsList.size >= 20)
            )
        )
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

    fun deletePost(postId: String) {
        _userPosts.update { posts ->
            posts.filterNot { it.id == postId }
        }
    }
}
