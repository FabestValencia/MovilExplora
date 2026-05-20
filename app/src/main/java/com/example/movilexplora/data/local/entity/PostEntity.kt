package com.example.movilexplora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val location: String,
    val rating: Double,
    val category: String,
    val price: String,
    val status: String,
    val imageUrl: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Float,
    val creatorId: String,
    val rejectionReason: String? = null,
    val isDeleted: Boolean = false
)

fun PostEntity.toDomainModel(): Post {
    val finalImageUrl = if (imageUrl.isNotEmpty()) {
        imageUrl
    } else {
        if (id.hashCode() % 2 == 0) {
            "android.resource://com.example.movilexplora/drawable/circasia"
        } else {
            "android.resource://com.example.movilexplora/drawable/salento"
        }
    }

    return Post(
        id = id,
        title = title,
        location = location,
        rating = rating,
        category = category,
        price = price,
        status = PostStatus.valueOf(status),
        imageUrl = finalImageUrl,
        description = description,
        latitude = latitude,
        longitude = longitude,
        distance = distance,
        creatorId = creatorId,
        rejectionReason = rejectionReason,
        isDeleted = isDeleted
    )
}

fun Post.toEntity(): PostEntity {
    return PostEntity(
        id = id,
        title = title,
        location = location,
        rating = rating,
        category = category,
        price = price,
        status = status.name,
        imageUrl = imageUrl,
        description = description,
        latitude = latitude,
        longitude = longitude,
        distance = distance,
        creatorId = creatorId,
        rejectionReason = rejectionReason,
        isDeleted = isDeleted
    )
}
