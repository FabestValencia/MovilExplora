package com.example.movilexplora.features.events

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.ai.CategoryRecommender
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.ImageRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.NotificationRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
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

    private val _publishResult = MutableStateFlow<RequestResult?>(null)
    val publishResult: StateFlow<RequestResult?> = _publishResult.asStateFlow()

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
            // Emitimos la categoría recomendada para que la UI se encargue de capturarla
            _selectedCategoryFromAi.value = recommendation.category
            _showAiRecommendationDialog.value = false
        }
    }

    private val _selectedCategoryFromAi = MutableStateFlow<String?>(null)
    val selectedCategoryFromAi: StateFlow<String?> = _selectedCategoryFromAi.asStateFlow()

    fun clearCategoryFromAi() {
        _selectedCategoryFromAi.value = null
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
        imageUri: Uri?,
        latitude: Double,
        longitude: Double
    ) {
        // Validación de campos obligatorios, incluyendo imagen
        if (title.isBlank() || startDate.isBlank() || endDate.isBlank()) {
            _publishResult.value = RequestResult.Failure(resources.getString(R.string.fill_all_fields))
            return
        }

        if (imageUri == null) {
            _publishResult.value = RequestResult.Failure(resources.getString(R.string.error_image_required))
            return
        }

        viewModelScope.launch {
            _publishResult.value = RequestResult.Loading
            try {
                val currentUserId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "1"
                val existingEvent = _eventToEdit.value
                
                var imageUrl = ""
                // 1. OBTENER URL (Subir si es necesario)
                if (imageUri.toString().startsWith("http://") || imageUri.toString().startsWith("https://")) {
                    imageUrl = imageUri.toString()
                } else {
                    imageUrl = imageRepository.uploadImage(imageUri) ?: ""
                }

                // 2. VALIDACIÓN CRÍTICA
                if (imageUrl.isEmpty()) {
                    _publishResult.value = RequestResult.Failure(resources.getString(R.string.error_image_upload))
                    return@launch
                }

                if (existingEvent != null) {
                    val updatedEvent = existingEvent.copy(
                        title = title,
                        description = description,
                        date = startDate,
                        endDate = endDate,
                        location = location.ifEmpty { existingEvent.location },
                        imageUrl = imageUrl,
                        category = category.ifEmpty { existingEvent.category },
                        latitude = latitude,
                        longitude = longitude
                    )
                    eventRepository.updateEvent(updatedEvent)
                    _publishResult.value = RequestResult.Success("")
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
                        status = PostStatus.PENDIENTE,
                        latitude = latitude,
                        longitude = longitude
                    )
                    eventRepository.addEvent(newEvent)
                    userRepository.addPoints(currentUserId, 50)
                    
                    // Achievement notification
                    notificationRepository.addNotification(
                        userId = currentUserId,
                        notification = Notification(
                            type = NotificationType.ACHIEVEMENT,
                            title = resources.getString(R.string.notification_first_event_title),
                            description = resources.getString(R.string.notification_first_event_desc),
                            time = resources.getString(R.string.notification_time_recent),
                            isNew = true
                        )
                    )

                    _publishResult.value = RequestResult.Success(resources.getString(R.string.post_created_success_points))
                }
            } catch (e: Exception) {
                _publishResult.value = RequestResult.Failure(e.message ?: resources.getString(R.string.error_publish_failed))
                e.printStackTrace()
            }
        }
    }

    fun resetResult() {
        _publishResult.value = null
    }
}
