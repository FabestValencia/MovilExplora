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

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {
    override fun getEvents(): Flow<List<Event>> = eventDao.getAllEvents().map { entities ->
        entities.map { it.toDomainModel() }
    }

    override suspend fun addEvent(event: Event) {
        eventDao.insertEvent(event.toEntity())
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.updateEvent(event.toEntity())
    }

    override suspend fun updateEventStatus(eventId: String, status: PostStatus, rejectionReason: String?) {
        if (rejectionReason != null) {
            eventDao.updateEventStatusWithReason(eventId, status.name, rejectionReason)
        } else {
            eventDao.updateEventStatus(eventId, status.name)
        }
    }
}
