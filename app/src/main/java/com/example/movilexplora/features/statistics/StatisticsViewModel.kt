package com.example.movilexplora.features.statistics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.PostRepository
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
class StatisticsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val postRepository: PostRepository
) : ViewModel() {
    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val userPosts = postRepository.getPosts().firstOrNull()?.filter { it.creatorId == userId } ?: emptyList()

            var activeCount = 0
            var finishedCount = 0
            var pendingCount = 0

            userPosts.forEach { post ->
                when (post.status.name) {
                    "ACTIVO", "VERIFICADO" -> activeCount++
                    "FINALIZADO" -> finishedCount++
                    "PENDIENTE" -> pendingCount++
                }
            }

            val total = activeCount + finishedCount + pendingCount

            _state.update {
                it.copy(
                    activePosts = activeCount,
                    finishedPosts = finishedCount,
                    pendingPosts = pendingCount,
                    totalMonthPosts = total,
                    // Keep mocked changes for UI visualization
                    activePostsChange = "+2%",
                    isActivePostsPositive = true,
                    finishedPostsChange = "+5%",
                    isFinishedPostsPositive = true,
                    pendingPostsChange = "0%",
                    isPendingPostsPositive = true
                )
            }
        }
    }
}
