package com.example.movilexplora.data.repository
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class EventRepositoryImpl @Inject constructor() : EventRepository {
    private val _events = MutableStateFlow<List<Event>>(
        emptyList()
    )
    override fun getEvents(): Flow<List<Event>> = _events.asStateFlow()
    override suspend fun addEvent(event: Event) {
        _events.update { it + event }
    }

    override suspend fun updateEvent(event: Event) {
        _events.update { events ->
            events.map { if (it.id == event.id) event else it }
        }
    }

    override suspend fun updateEventStatus(eventId: String, status: PostStatus, rejectionReason: String?) {
        _events.update { events ->
            events.map {
                if (it.id == eventId) it.copy(status = status, rejectionReason = rejectionReason) else it
            }
        }
    }
}
