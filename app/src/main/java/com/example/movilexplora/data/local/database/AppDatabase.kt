package com.example.movilexplora.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.dao.CommentDao
import com.example.movilexplora.data.local.dao.LikeDao
import com.example.movilexplora.data.local.dao.PostDao
import com.example.movilexplora.data.local.entity.UserEntity
import com.example.movilexplora.data.local.entity.CommentEntity
import com.example.movilexplora.data.local.entity.LikeEntity
import com.example.movilexplora.data.local.entity.PostEntity

@Database(entities = [UserEntity::class, CommentEntity::class, LikeEntity::class, PostEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val commentDao: CommentDao
    abstract val likeDao: LikeDao
    abstract val postDao: PostDao
}
