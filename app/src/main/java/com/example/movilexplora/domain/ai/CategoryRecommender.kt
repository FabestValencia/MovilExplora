package com.example.movilexplora.domain.ai

data class Recommendation(
    val category: String,
    val reason: String
)

interface CategoryRecommender {
    suspend fun recommendCategory(description: String): Recommendation?
}
