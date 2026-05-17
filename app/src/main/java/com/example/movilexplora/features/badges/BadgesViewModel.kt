package com.example.movilexplora.features.badges

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
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
    private val postRepository: PostRepository,
    private val resources: ResourceProvider
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
                    name = resources.getString(R.string.badge_first_post_title),
                    description = resources.getString(R.string.badge_first_post_desc),
                    iconName = "celebration",
                    isUnlocked = postCount >= 1
                ),
                Achievement(
                    name = resources.getString(R.string.badge_10_posts_title),
                    description = resources.getString(R.string.badge_10_posts_desc),
                    iconName = "verified",
                    isUnlocked = postCount >= 10
                ),
                Achievement(
                    name = resources.getString(R.string.badge_map_master_title),
                    description = resources.getString(R.string.badge_map_master_desc),
                    iconName = "map",
                    isUnlocked = activePostsCount >= 5
                ),
                Achievement(
                    name = resources.getString(R.string.badge_explorer_month_title),
                    description = resources.getString(R.string.badge_explorer_month_desc),
                    iconName = "stars",
                    isUnlocked = postCount >= 20 // Dynamic condition
                ),
                Achievement(
                    name = resources.getString(R.string.badge_local_guide_title),
                    description = resources.getString(R.string.badge_local_guide_desc),
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
