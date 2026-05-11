package com.example.movilexplora.features.badges

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Achievement
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgesState(
    val unlockedCount: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

@HiltViewModel
class BadgesViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BadgesState())
    val state: StateFlow<BadgesState> = _state.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val userPosts = postRepository.getPosts().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()

            val postCount = userPosts.size
            val activePostsCount = userPosts.count { it.status.name == "ACTIVO" || it.status.name == "VERIFICADO" }

            val dynamicAchievements = listOf(
                Achievement(
                    name = "Primera Publicación",
                    description = "¡Tu primera aventura compartida!",
                    iconName = "celebration",
                    isUnlocked = postCount >= 1
                ),
                Achievement(
                    name = "10 Publicaciones",
                    description = "Comunidad confiable y activa",
                    iconName = "verified",
                    isUnlocked = postCount >= 10
                ),
                Achievement(
                    name = "Maestro del Mapa",
                    description = "Experto en navegación local",
                    iconName = "map",
                    isUnlocked = activePostsCount >= 5
                ),
                Achievement(
                    name = "Explorador del Mes",
                    description = "Sé el más activo este mes",
                    iconName = "stars",
                    isUnlocked = postCount >= 20 // Dynamic condition
                ),
                Achievement(
                    name = "Guía Local",
                    description = "Ayuda a otros viajeros",
                    iconName = "contact_page",
                    isUnlocked = activePostsCount >= 15 // Dynamic condition
                )
            )

            _state.update {
                it.copy(
                    unlockedCount = dynamicAchievements.count { a -> a.isUnlocked },
                    achievements = dynamicAchievements
                )
            }
        }
    }
}
