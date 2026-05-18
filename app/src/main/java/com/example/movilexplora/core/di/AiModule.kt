package com.example.movilexplora.core.di

import com.example.movilexplora.data.ai.GeminiCategoryRecommender
import com.example.movilexplora.domain.ai.CategoryRecommender
import com.google.ai.client.generativeai.GenerativeModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides
    @Singleton
    fun provideGenerativeModel(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-flash-latest",
            apiKey = com.example.movilexplora.BuildConfig.GEMINI_API_KEY
        )
    }

    @Provides
    @Singleton
    fun provideCategoryRecommender(recommender: GeminiCategoryRecommender): CategoryRecommender {
        return recommender
    }
}
