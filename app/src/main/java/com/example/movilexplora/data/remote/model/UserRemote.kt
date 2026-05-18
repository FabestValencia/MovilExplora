package com.example.movilexplora.data.remote.model

import com.google.gson.annotations.SerializedName

data class UserRemote(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int
)
