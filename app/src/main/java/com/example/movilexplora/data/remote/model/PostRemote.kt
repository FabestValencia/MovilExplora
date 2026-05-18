package com.example.movilexplora.data.remote.model

import com.google.gson.annotations.SerializedName

data class PostRemote(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("location") val location: String,
    @SerializedName("rating") val rating: Double,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: String,
    @SerializedName("status") val status: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("creatorId") val creatorId: String
)
