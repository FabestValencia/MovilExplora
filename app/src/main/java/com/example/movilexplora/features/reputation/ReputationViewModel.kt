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
    private val postRepository: PostRepository
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

            // Calculamos puntos dinamicamente basados en los posts totales publicados.
            // Asi evitamos "quemar" los 1250 estticos y lo hacemos por publicacin,
            // cada publicacin activa vale por ejemplo 50 puntos en nuestro modelo.
            val allPosts = postRepository.getPosts().firstOrNull() ?: emptyList()
            val posts = allPosts.filter { it.creatorId == userId }
            // Supondremos que cada publicacion verificada aporta 100 ptos, pendientes 50 pts, para aplicar lgica real
            var dynamicPoints = 0
            val recentPoints = mutableListOf<RecentPoint>()

            posts.forEach { post ->
                if (post.status == com.example.movilexplora.domain.model.PostStatus.VERIFICADO) {
                    dynamicPoints += 100
                    recentPoints.add(
                        RecentPoint(
                            id = post.id,
                            title = "Publicación verificada: ${post.title}",
                            time = "Reciente",
                            points = "+100 pts",
                            type = PointType.POST
                        )
                    )
                } else {
                    dynamicPoints += 50
                    recentPoints.add(
                        RecentPoint(
                            id = post.id,
                            title = "Publicación pendiente: ${post.title}",
                            time = "Reciente",
                            points = "+50 pts",
                            type = PointType.POST
                        )
                    )
                }
            }
            
            // Limitamos a los ms recientes o todos si lo deseamos. Aca cargamos el historial real de los pts
            val sortedRecentPoints = recentPoints.asReversed().take(10)

            _state.update {
                it.copy(
                    userName = user.name,
                    profilePictureUrl = user.profilePictureUrl,
                    currentPoints = dynamicPoints.coerceAtLeast(10), // minimum 10 if no posts
                    targetPoints = 2000,
                    currentLevel = ReputationLevel.EMBAJADOR,
                    nextLevelName = "Nivel Máximo",
                    recentPoints = sortedRecentPoints
                )
            }
        }
    }
}
