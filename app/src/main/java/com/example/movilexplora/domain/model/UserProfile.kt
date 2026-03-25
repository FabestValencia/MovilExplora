package com.example.movilexplora.domain.model

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

enum class ReputationLevel(val displayName: String) {
    TURISTA("Turista"),
    EXPLORADOR("Explorador"),
    AVENTURERO("Aventurero"),
    EMBAJADOR("Embajador")
}

data class Achievement(
    val name: String,
    val description: String = "",
    val iconName: String, // Resource name or key
    val isUnlocked: Boolean
)
