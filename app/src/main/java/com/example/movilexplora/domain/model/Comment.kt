package com.example.movilexplora.domain.model

data class Comment(
    var id: String = "",
    val postId: String = "",
    val userName: String = "",
    val userAvatar: String = "",
    val date: String = "",
    val content: String = ""
)
