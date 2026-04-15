package com.example.movilexplora.domain.model

import com.example.movilexplora.R

data class UserProfile(
    val name: String,
    val email: String,
    val role: String,
    val activePosts: Int,
    val finishedPosts: Int,
    val pendingPosts: Int,
    val currentXp: Int,
    val maxXp: Int,
    val reputationLevel: ReputationLevel,
    val achievements: List<Achievement>
)

enum class ReputationLevel(val displayNameRes: Int) {
    TURISTA(R.string.reputation_level_turista),
    EXPLORADOR(R.string.reputation_level_explorador),
    AVENTURERO(R.string.reputation_level_aventurero),
    EMBAJADOR(R.string.reputation_level_embajador)
}

data class Achievement(
    val name: String,
    val description: String = "",
    val iconName: String, // Resource name or key
    val isUnlocked: Boolean
)
