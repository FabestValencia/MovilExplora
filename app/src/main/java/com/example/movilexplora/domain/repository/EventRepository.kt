package com.example.movilexplora.domain.repository
import com.example.movilexplora.domain.model.Event
import kotlinx.coroutines.flow.Flow
interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    suspend fun addEvent(event: Event)
    suspend fun updateEvent(event: Event)
    suspend fun updateEventStatus(eventId: String, status: com.example.movilexplora.domain.model.PostStatus, rejectionReason: String? = null)
}
