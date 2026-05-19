package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.data.local.dao.EventDao
import com.example.movilexplora.data.local.entity.toEntity
import com.example.movilexplora.data.local.entity.toDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val firestore: FirebaseFirestore
) : EventRepository {
    private val collection = firestore.collection("events")
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Sincronizar eventos desde Firestore
        collection.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                scope.launch {
                    val events = it.documents.mapNotNull { doc ->
                        doc.toObject(Event::class.java)?.apply { id = doc.id }
                    }
                    // Actualizar caché local: limpiar y reinsertar
                    eventDao.clearAll()
                    eventDao.insertEvents(events.map { it.toEntity() })
                }
            }
        }
    }

    override fun getEvents(): Flow<List<Event>> = eventDao.getAllEvents().map { entities ->
        entities.map { it.toDomainModel() }
    }

    override suspend fun addEvent(event: Event) {
        val docRef = if (event.id.isEmpty()) collection.document() else collection.document(event.id)
        val eventToSave = event.copy(id = docRef.id)
        docRef.set(eventToSave).await()
    }

    override suspend fun updateEvent(event: Event) {
        collection.document(event.id).set(event).await()
    }

    override suspend fun updateEventStatus(eventId: String, status: PostStatus, rejectionReason: String?) {
        val updates = mutableMapOf<String, Any>("status" to status.name)
        rejectionReason?.let { updates["rejectionReason"] = it }
        collection.document(eventId).update(updates).await()
    }
}
