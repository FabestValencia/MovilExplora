package com.example.movilexplora.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.dao.CommentDao
import com.example.movilexplora.data.local.dao.LikeDao
import com.example.movilexplora.data.local.dao.PostDao
import com.example.movilexplora.data.local.dao.EventDao
import com.example.movilexplora.data.local.entity.UserEntity
import com.example.movilexplora.data.local.entity.CommentEntity
import com.example.movilexplora.data.local.entity.LikeEntity
import com.example.movilexplora.data.local.entity.PostEntity
import com.example.movilexplora.data.local.entity.EventEntity

@Database(entities = [UserEntity::class, CommentEntity::class, LikeEntity::class, PostEntity::class, EventEntity::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
}
