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
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.PostRepository
import javax.inject.Inject

data class ActivityItemModel(
    val title: String,
    val time: String
)

data class StatisticsState(
    val activePosts: Int = 0,
    val activePostsChange: String = "+0%",
    val isActivePostsPositive: Boolean = true,
    
    val finishedPosts: Int = 0,
    val finishedPostsChange: String = "+0%",
    val isFinishedPostsPositive: Boolean = true,
    
    val pendingPosts: Int = 0,
    val pendingPostsChange: String = "0%",
    val isPendingPostsPositive: Boolean = true,

    val rejectedPosts: Int = 0,
    val rejectedPostsChange: String = "0%",
    val isRejectedPostsPositive: Boolean = false,

    val totalMonthPosts: Int = 0,

    val recentActivities: List<ActivityItemModel> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val postRepository: PostRepository,
    private val resourceProvider: ResourceProvider
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
            var rejectedCount = 0

            val recentActivities = mutableListOf<ActivityItemModel>()

            userPosts.forEach { post ->
                val statusText: String
                when (post.status.name) {
                    "ACTIVO", "VERIFICADO" -> {
                        activeCount++
                        statusText = resourceProvider.getString(R.string.stat_recent_approved, post.title)
                    }
                    "FINALIZADO" -> {
                        finishedCount++
                        statusText = resourceProvider.getString(R.string.stat_recent_rejected, post.title) // or generic finished logic
                    }
                    "PENDIENTE" -> {
                        pendingCount++
                        statusText = resourceProvider.getString(R.string.stat_recent_created, post.title)
                    }
                    "RECHAZADO" -> {
                        rejectedCount++
                        statusText = resourceProvider.getString(R.string.stat_recent_rejected, post.title)
                    }
                    else -> {
                        statusText = resourceProvider.getString(R.string.stat_recent_created, post.title)
                    }
                }
                
                recentActivities.add(ActivityItemModel(statusText, resourceProvider.getString(R.string.stat_time_recent)))
            }

            val total = activeCount + finishedCount + pendingCount + rejectedCount

            _state.update {
                it.copy(
                    activePosts = activeCount,
                    finishedPosts = finishedCount,
                    pendingPosts = pendingCount,
                    rejectedPosts = rejectedCount,
                    totalMonthPosts = total,
                    recentActivities = recentActivities.takeLast(5).reversed(),
                    activePostsChange = "${if (activeCount > 0) "+" else ""}0%",
                    isActivePostsPositive = true,
                    finishedPostsChange = "${if (finishedCount > 0) "+" else ""}0%",
                    isFinishedPostsPositive = true,
                    pendingPostsChange = "${if (pendingCount > 0) "+" else ""}0%",
                    isPendingPostsPositive = true,
                    rejectedPostsChange = "${if (rejectedCount > 0) "+" else ""}0%",
                    isRejectedPostsPositive = false
                )
            }
        }
    }
}
