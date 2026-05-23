package com.example.movilexplora.features.events
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.ai.CategoryRecommender
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val sessionDataStore: SessionDataStore,
    private val resources: ResourceProvider,
    private val categoryRecommender: CategoryRecommender
) : ViewModel() {

    private val _eventToEdit = MutableStateFlow<Event?>(null)
    val eventToEdit: StateFlow<Event?> = _eventToEdit.asStateFlow()

    private val _isRecommendingCategory = MutableStateFlow(false)
    val isRecommendingCategory: StateFlow<Boolean> = _isRecommendingCategory.asStateFlow()

    private val _pendingRecommendation = MutableStateFlow<com.example.movilexplora.domain.ai.Recommendation?>(null)
    val pendingRecommendation: StateFlow<com.example.movilexplora.domain.ai.Recommendation?> = _pendingRecommendation.asStateFlow()

    private val _showAiRecommendationDialog = MutableStateFlow(false)
    val showAiRecommendationDialog: StateFlow<Boolean> = _showAiRecommendationDialog.asStateFlow()

    private val _aiRecommendationReason = MutableStateFlow<String?>(null)
    val aiRecommendationReason: StateFlow<String?> = _aiRecommendationReason.asStateFlow()

    fun recommendCategory(description: String) {
        if (description.isBlank()) return

        viewModelScope.launch {
            try {
                _isRecommendingCategory.value = true
                val recommendation = categoryRecommender.recommendCategory(description)
                if (recommendation != null) {
                    _pendingRecommendation.value = recommendation
                    _showAiRecommendationDialog.value = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isRecommendingCategory.value = false
            }
        }
    }

    fun acceptRecommendation() {
        _pendingRecommendation.value?.let { recommendation ->
            _aiRecommendationReason.value = recommendation.reason
            // We'll handle setting the actual category in the UI for now as it's a local state there,
            // or we could emit a signal.
            _showAiRecommendationDialog.value = false
        }
    }

    fun dismissRecommendation() {
        _pendingRecommendation.value = null
        _showAiRecommendationDialog.value = false
    }

    fun clearRecommendation() {
        _pendingRecommendation.value = null
        _aiRecommendationReason.value = null
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            val events = eventRepository.getEvents().firstOrNull() ?: emptyList()
            _eventToEdit.value = events.find { it.id == eventId }
        }
    }

    fun publishEvent(
        title: String,
        description: String,
        location: String,
        category: String,
        startDate: String,
        endDate: String,
        imageUrl: String
    ) {
        // Validación de campos obligatorios
        if (title.isBlank() || startDate.isBlank() || endDate.isBlank()) return

        viewModelScope.launch {
            val currentUserId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "1"
            val existingEvent = _eventToEdit.value

            if (existingEvent != null) {
                val updatedEvent = existingEvent.copy(
                    title = title,
                    description = description,
                    date = startDate,
                    endDate = endDate,
                    location = location.ifEmpty { existingEvent.location },
                    imageUrl = imageUrl.ifEmpty { existingEvent.imageUrl },
                    category = category.ifEmpty { existingEvent.category }
                )
                eventRepository.updateEvent(updatedEvent)
            } else {
                val newEvent = Event(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    description = description,
                    date = startDate,
                    time = "",
                    endDate = endDate,
                    endTime = "",
                    location = location.ifEmpty { resources.getString(R.string.location_not_specified) },
                    imageUrl = imageUrl,
                    attendeesCount = 0,
                    isJoined = false,
                    category = category.ifEmpty { resources.getString(R.string.event_category_general) },
                    creatorId = currentUserId,
                    status = PostStatus.PENDIENTE
                )
                eventRepository.addEvent(newEvent)
            }
        }
    }
}
