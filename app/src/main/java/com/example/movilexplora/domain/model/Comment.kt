package com.example.movilexplora.domain.model

data class Comment(
    val id: String,
    val postId: String,
    val userName: String,
    val userAvatar: String,
    val date: String,
    val content: String
)
