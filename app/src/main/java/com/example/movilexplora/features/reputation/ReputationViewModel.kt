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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider

data class RecentPoint(
    val id: String,
    val title: String,
    val time: String,
    val points: Long,
    val type: PointType
)

enum class PointType {
    POST, COMMENT, VISIT, VOTE
}

data class ReputationState(
    val userName: String = "",
    val profilePictureUrl: String = "",
    val currentLevel: ReputationLevel = ReputationLevel.TURISTA,
    val nextLevelName: String = "",
    val currentPoints: Long = 0,
    val targetPoints: Long = 100,
    val percentageMessage: String = "",
    val recentPoints: List<RecentPoint> = emptyList()
)

@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
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
        _state.update { 
            it.copy(
                userName = resourceProvider.getString(R.string.reputation_default_user_name),
                nextLevelName = resourceProvider.getString(R.string.reputation_max_level),
                percentageMessage = resourceProvider.getString(R.string.reputation_percentage_msg)
            )
        }
        
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            sessionDataStore.sessionFlow.flatMapLatest { session ->
                val userId = session?.userId
                if (userId != null && userId != "guest") {
                    kotlinx.coroutines.flow.combine(
                        userRepository.observeUser(userId),
                        postRepository.getPosts()
                    ) { user, allPosts ->
                        val userPosts = allPosts.filter { it.creatorId == userId }
                        DataState(user, userPosts)
                    }
                } else {
                    kotlinx.coroutines.flow.flowOf(DataState(null, emptyList()))
                }
            }.collect { (user, userPosts) ->
                if (user != null) {
                    updateReputationState(user, userPosts)
                }
            }
        }
    }

    private data class DataState(
        val user: com.example.movilexplora.domain.model.User?,
        val userPosts: List<com.example.movilexplora.domain.model.Post>
    )

    private fun updateReputationState(
        user: com.example.movilexplora.domain.model.User,
        posts: List<com.example.movilexplora.domain.model.Post>
    ) {
        val recentPoints = mutableListOf<RecentPoint>()

        posts.forEach { post ->
            val titleText = when (post.status) {
                com.example.movilexplora.domain.model.PostStatus.VERIFICADO -> resourceProvider.getString(R.string.stat_recent_approved, post.title)
                com.example.movilexplora.domain.model.PostStatus.RECHAZADO -> resourceProvider.getString(R.string.stat_recent_rejected, post.title)
                else -> resourceProvider.getString(R.string.stat_recent_created, post.title)
            }

            val pointsValue = when (post.status) {
                com.example.movilexplora.domain.model.PostStatus.VERIFICADO -> 100L
                com.example.movilexplora.domain.model.PostStatus.RECHAZADO -> 0L
                else -> 50L
            }

            recentPoints.add(
                RecentPoint(
                    id = post.id,
                    title = titleText,
                    time = resourceProvider.getString(R.string.stat_time_recent),
                    points = pointsValue,
                    type = PointType.POST
                )
            )
        }
        
        val sortedRecentPoints = recentPoints.asReversed().take(10)

        val actualPoints: Long = user.points
        val (calculatedLevel, calcNextLevel, calcTarget) = when {
            actualPoints < 100L -> Triple(ReputationLevel.TURISTA, resourceProvider.getString(ReputationLevel.EXPLORADOR.displayNameRes), 100L)
            actualPoints < 500L -> Triple(ReputationLevel.EXPLORADOR, resourceProvider.getString(ReputationLevel.AVENTURERO.displayNameRes), 500L)
            actualPoints < 1000L -> Triple(ReputationLevel.AVENTURERO, resourceProvider.getString(ReputationLevel.EMBAJADOR.displayNameRes), 1000L)
            else -> Triple(ReputationLevel.EMBAJADOR, resourceProvider.getString(R.string.reputation_max_level), 2000L)
        }

        _state.update {
            it.copy(
                userName = user.name,
                profilePictureUrl = user.profilePictureUrl,
                currentPoints = actualPoints,
                targetPoints = calcTarget,
                currentLevel = calculatedLevel,
                nextLevelName = calcNextLevel,
                recentPoints = sortedRecentPoints
            )
        }
    }
}
