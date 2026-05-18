package com.example.movilexplora.data.remote

import com.example.movilexplora.data.remote.model.PostRemote
import com.example.movilexplora.data.remote.model.UserRemote
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserRemote>

    @GET("posts")
    suspend fun getPosts(): List<PostRemote>
}
