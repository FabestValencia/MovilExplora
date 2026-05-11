package com.example.movilexplora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val endDate: String,
    val endTime: String,
    val location: String,
    val imageUrl: String,
    val attendeesCount: Int,
    val category: String,
    val creatorId: String,
    val status: String,
    val rejectionReason: String?
)

fun EventEntity.toDomainModel(): Event {
    return Event(
        id = id,
        title = title,
        description = description,
        date = date,
        time = time,
        endDate = endDate,
        endTime = endTime,
        location = location,
        imageUrl = imageUrl,
        attendeesCount = attendeesCount,
        category = category,
        creatorId = creatorId,
        status = PostStatus.valueOf(status),
        rejectionReason = rejectionReason
    )
}

fun Event.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        title = title,
        description = description,
        date = date,
        time = time,
        endDate = endDate,
        endTime = endTime,
        location = location,
        imageUrl = imageUrl,
        attendeesCount = attendeesCount,
        category = category,
        creatorId = creatorId,
        status = status.name,
        rejectionReason = rejectionReason
    )
}

