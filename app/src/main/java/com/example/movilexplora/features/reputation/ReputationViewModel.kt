package com.example.movilexplora.features.reputation

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.ReputationLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RecentPoint(
    val id: String,
    val title: String,
    val time: String,
    val points: String,
    val type: PointType
)

enum class PointType {
    POST, COMMENT, VISIT, VOTE
}

data class ReputationState(
    val userName: String = "Jean Botsito",
    val currentLevel: ReputationLevel = ReputationLevel.AVENTURERO,
    val nextLevelName: String = "Embajador Local",
    val currentPoints: Int = 2450,
    val targetPoints: Int = 3000,
    val recentPoints: List<RecentPoint> = emptyList()
)

@HiltViewModel
class ReputationViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ReputationState())
    val state: StateFlow<ReputationState> = _state.asStateFlow()

    init {
        _state.update {
            it.copy(
                recentPoints = listOf(
                    RecentPoint("1", "Nueva Publicación: Cascada Oculta", "Hace 2 horas", "+10 pts", PointType.POST),
                    RecentPoint("2", "Comentario recibido", "Hace 5 horas", "+5 pts", PointType.COMMENT),
                    RecentPoint("3", "Visitado: Plaza Central", "Ayer", "+25 pts", PointType.VISIT)
                )
            )
        }
    }
}
