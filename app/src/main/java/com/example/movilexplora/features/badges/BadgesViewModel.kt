package com.example.movilexplora.features.badges

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Achievement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BadgesState(
    val unlockedCount: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

class BadgesViewModel : ViewModel() {
    private val _state = MutableStateFlow(BadgesState())
    val state: StateFlow<BadgesState> = _state.asStateFlow()

    init {
        val mockAchievements = listOf(
            Achievement("Primera Publicación", "¡Tu primera aventura compartida!", "celebration", true),
            Achievement("10 Publicaciones", "Comunidad confiable y activa", "verified", true),
            Achievement("Maestro del Mapa", "Experto en navegación local", "map", true),
            Achievement("Explorador del Mes", "Sé el más activo este mes", "stars", false),
            Achievement("Guía Local", "Ayuda a otros viajeros", "contact_page", false)
        )
        _state.update { 
            it.copy(
                unlockedCount = mockAchievements.count { a -> a.isUnlocked },
                achievements = mockAchievements
            )
        }
    }
}
