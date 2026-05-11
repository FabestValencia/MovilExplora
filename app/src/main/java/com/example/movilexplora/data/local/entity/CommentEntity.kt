package com.example.movilexplora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movilexplora.domain.model.Comment

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userName: String,
    val userAvatar: String,
    val date: String,
    val content: String
)

fun CommentEntity.toDomainModel() = Comment(
    id = id,
    postId = postId,
    userName = userName,
    userAvatar = userAvatar,
    date = date,
    content = content
)

fun Comment.toEntity() = CommentEntity(
    id = id,
    postId = postId,
    userName = userName,
    userAvatar = userAvatar,
    date = date,
    content = content
)

