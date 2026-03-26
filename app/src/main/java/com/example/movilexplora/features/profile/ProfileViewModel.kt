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
                Achievement(name = "Caminante", iconName = "hiking", isUnlocked = true),
                Achievement(name = "Fotógrafo", iconName = "camera", isUnlocked = true),
                Achievement(name = "Foodie", iconName = "restaurant", isUnlocked = true),
                Achievement(name = "Mochilero", iconName = "flight", isUnlocked = false)
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
