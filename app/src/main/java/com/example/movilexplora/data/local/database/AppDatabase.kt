package com.example.movilexplora.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
}
