package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private fun getCollection(userId: String) = 
        firestore.collection("users").document(userId).collection("notifications")

    override fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val listener = getCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val notifications = snapshot?.toObjects(Notification::class.java) ?: emptyList()
                trySend(notifications)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun addNotification(userId: String, notification: Notification) {
        val ref = getCollection(userId).document()
        val notificationToSave = notification.copy(id = ref.id)
        ref.set(notificationToSave).await()
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        getCollection(userId).document(notificationId).update("isNew", false).await()
    }

    override suspend fun clearAll(userId: String) {
        val snapshot = getCollection(userId).get().await()
        snapshot.documents.forEach { it.reference.delete() }
    }
}
