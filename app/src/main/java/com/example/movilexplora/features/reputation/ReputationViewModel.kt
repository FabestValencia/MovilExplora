package com.example.movilexplora.features.reputation

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.ReputationLevel
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider

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
    val profilePictureUrl: String = "",
    val currentLevel: ReputationLevel = ReputationLevel.EMBAJADOR,
    val nextLevelName: String = "Nivel Máximo",
    val currentPoints: Int = 1250,
    val targetPoints: Int = 2000,
    val percentageMessage: String = "Top 15% de exploradores en tu ciudad",
    val recentPoints: List<RecentPoint> = emptyList()
)

@HiltViewModel
class ReputationViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private val _state = MutableStateFlow(ReputationState())
    val state: StateFlow<ReputationState> = _state.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            val user = userRepository.findById(userId) ?: return@launch

            // Cargamos dinámicamente basados en los posts creados por el usuario
            val allPosts = postRepository.getPosts().firstOrNull() ?: emptyList()
            val posts = allPosts.filter { it.creatorId == userId }

            val recentPoints = mutableListOf<RecentPoint>()

            posts.forEach { post ->
                val titleText = when (post.status) {
                    com.example.movilexplora.domain.model.PostStatus.VERIFICADO -> resourceProvider.getString(R.string.stat_recent_approved, post.title)
                    com.example.movilexplora.domain.model.PostStatus.RECHAZADO -> resourceProvider.getString(R.string.stat_recent_rejected, post.title)
                    else -> resourceProvider.getString(R.string.stat_recent_created, post.title)
                }

                val pointsText = when (post.status) {
                    com.example.movilexplora.domain.model.PostStatus.VERIFICADO -> "+100 pts"
                    com.example.movilexplora.domain.model.PostStatus.RECHAZADO -> "+0 pts"
                    else -> "+50 pts"
                }

                recentPoints.add(
                    RecentPoint(
                        id = post.id,
                        title = titleText,
                        time = resourceProvider.getString(R.string.stat_time_recent),
                        points = pointsText,
                        type = PointType.POST
                    )
                )
            }
            
            // Limitamos a los más recientes
            val sortedRecentPoints = recentPoints.asReversed().take(10)

            val actualPoints = user.points
            
            val (calculatedLevel, calcNextLevel, calcTarget) = when {
                actualPoints < 100 -> Triple(ReputationLevel.TURISTA, ReputationLevel.EXPLORADOR.displayName, 100)
                actualPoints < 500 -> Triple(ReputationLevel.EXPLORADOR, ReputationLevel.AVENTURERO.displayName, 500)
                actualPoints < 1000 -> Triple(ReputationLevel.AVENTURERO, ReputationLevel.EMBAJADOR.displayName, 1000)
                else -> Triple(ReputationLevel.EMBAJADOR, "Nivel Máximo", 2000)
            }

            _state.update {
                it.copy(
                    userName = user.name,
                    profilePictureUrl = user.profilePictureUrl,
                    currentPoints = actualPoints, // usamos los puntos REALES basados en la BD
                    targetPoints = calcTarget,
                    currentLevel = calculatedLevel,
                    nextLevelName = calcNextLevel,
                    recentPoints = sortedRecentPoints
                )
            }
        }
    }
}
