package com.example.movilexplora.core.di
import android.content.Context
import androidx.room.Room
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.ResourceProviderImpl
import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "movilexplora_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao
    }

    @Provides
    @Singleton
    fun provideCommentDao(database: AppDatabase) = database.commentDao

    @Provides
    @Singleton
    fun provideLikeDao(database: AppDatabase) = database.likeDao

    @Provides
    @Singleton
    fun provideResourceProvider(
        @ApplicationContext context: Context
    ): ResourceProvider = ResourceProviderImpl(context)
}
