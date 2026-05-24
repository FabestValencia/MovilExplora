package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.data.local.dao.EventDao
import com.example.movilexplora.data.local.entity.toEntity
import com.example.movilexplora.data.local.entity.toDomainModel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
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
    private val firestore: FirebaseFirestore,
    private val sessionDataStore: com.example.movilexplora.data.datastore.SessionDataStore
) : EventRepository {
    private val collection = firestore.collection("events")
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

    init {
        // Regla de Negocio: Limpiar caché local al iniciar para evitar datos "quemados"
        // y asegurar una sincronización fresca desde Firestore.
        scope.launch {
            try {
                eventDao.clearAll()
                android.util.Log.d("CACHE_CLEANUP", "Caché de eventos limpiada al iniciar")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Observar la sesión para ajustar los listeners en tiempo real
        sessionDataStore.sessionFlow.onEach { session ->
            setupRealtimeSync(session)
        }.launchIn(scope)
    }

    private fun setupRealtimeSync(session: com.example.movilexplora.data.model.UserSession?) {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()

        if (session == null) return

        val queries = mutableListOf<com.google.firebase.firestore.Query>()

        if (session.role == com.example.movilexplora.domain.model.enum.UserRole.ADMIN) {
            queries.add(collection)
        } else {
            // Usuario normal: eventos verificados + sus propios eventos
            queries.add(collection.whereEqualTo("status", PostStatus.VERIFICADO.name))
            queries.add(collection.whereEqualTo("creatorId", session.userId))
        }

        queries.forEach { query ->
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                snapshot?.documentChanges?.forEach { change ->
                    val event = change.document.toObject(Event::class.java).apply { id = change.document.id }
                    scope.launch {
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                eventDao.insertEvent(event.toEntity())
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                if (session.role == com.example.movilexplora.domain.model.enum.UserRole.ADMIN || event.creatorId != session.userId) {
                                    eventDao.deleteEvent(event.id)
                                } else {
                                    val exists = collection.document(event.id).get().await().exists()
                                    if (!exists) {
                                        eventDao.deleteEvent(event.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            activeListeners.add(listener)
        }
    }

    override fun getEvents(): Flow<List<Event>> = eventDao.getAllEvents().map { entities ->
        entities.map { it.toDomainModel() }.filter { !it.isDeleted }
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

    override suspend fun toggleFavorite(eventId: String, userId: String) {
        val doc = collection.document(eventId).get().await()
        val event = doc.toObject(Event::class.java)
        event?.let {
            val newList = it.likedBy.toMutableList()
            if (newList.contains(userId)) {
                newList.remove(userId)
            } else {
                newList.add(userId)
            }
            collection.document(eventId).update("likedBy", newList).await()
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        collection.document(eventId).update("isDeleted", true).await()
    }

    override suspend fun clearCache() {
        eventDao.clearAll()
    }
}
