package com.example.movilexplora.core.di

import com.example.movilexplora.data.repository.PostRepositoryImpl
import com.example.movilexplora.data.repository.UserRepositoryImpl
import com.example.movilexplora.data.repository.EventRepositoryImpl
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.EventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(
        postRepositoryImpl: PostRepositoryImpl
    ): PostRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        eventRepositoryImpl: EventRepositoryImpl
    ): EventRepository
}
