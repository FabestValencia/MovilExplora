package com.example.movilexplora.data.local.entity

import androidx.room.Entity

@Entity(tableName = "likes", primaryKeys = ["itemId", "userId"])
data class LikeEntity(
    val itemId: String,
    val userId: String,
    val itemType: String // "POST" or "EVENT"
)
