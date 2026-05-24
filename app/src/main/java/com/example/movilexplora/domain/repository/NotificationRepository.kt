package com.example.movilexplora.domain.repository

import com.example.movilexplora.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>
    suspend fun addNotification(userId: String, notification: Notification)
    suspend fun markAsRead(userId: String, notificationId: String)
    suspend fun clearAll(userId: String)
}
