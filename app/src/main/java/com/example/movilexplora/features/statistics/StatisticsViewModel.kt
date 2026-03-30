package com.example.movilexplora.features.statistics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ActivityItemModel(
    val title: String,
    val time: String
)

data class StatisticsState(
    val activePosts: Int = 12,
    val activePostsChange: String = "+2%",
    val isActivePostsPositive: Boolean = true,
    
    val finishedPosts: Int = 48,
    val finishedPostsChange: String = "+5%",
    val isFinishedPostsPositive: Boolean = true,
    
    val pendingPosts: Int = 3,
    val pendingPostsChange: String = "-1%",
    val isPendingPostsPositive: Boolean = false,
    
    val totalMonthPosts: Int = 63,
    
    val recentActivities: List<ActivityItemModel> = listOf(
        ActivityItemModel("Nueva publicación aprobada", "Hace 2 horas"),
        ActivityItemModel("Actualización de perfil", "Ayer")
    )
)

@HiltViewModel
class StatisticsViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()
}
