package com.example.movilexplora.features.createpost

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.ai.CategoryRecommender
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.ImageRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.net.Uri

data class CreatePostState(
    val selectedCategory: String? = null,
    val aiRecommendationReason: String? = null,
    val selectedPriceRange: Int = 2,
    val selectedTime: String? = null,
    val address: String = "",
    val isRecommendingCategory: Boolean = false,
    val imageUri: Uri? = null,
    val pendingRecommendation: com.example.movilexplora.domain.ai.Recommendation? = null,
    val showAiRecommendationDialog: Boolean = false,
    val selectedLatitude: Double? = null,
    val selectedLongitude: Double? = null
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val imageRepository: ImageRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val resources: ResourceProvider,
    private val categoryRecommender: CategoryRecommender,
    savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {
    val title = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_post_title_empty) else null
    }

    val description = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_post_description_empty) else null
    }

    private val postId: String? = savedStateHandle.get<String>("postId")

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    init {
        if (postId != null) {
            loadExistingPost(postId)
        }
    }

    private fun loadExistingPost(postId: String) {
        viewModelScope.launch {
            postRepository.getPost(postId)
                .filterNotNull()
                .first()
                .let { post ->
                    title.setValueDirectly(post.title)
                    description.setValueDirectly(post.description)
                    _state.update {
                        it.copy(
                            selectedCategory = post.category,
                            selectedPriceRange = post.price.length.coerceIn(1, 4),
                            selectedLatitude = post.latitude,
                            selectedLongitude = post.longitude,
                            address = post.location,
                            imageUri = if (post.imageUrl.isNotEmpty()) Uri.parse(post.imageUrl) else null
                        )
                    }
                }
        }
    }

    private val _publishResult = MutableStateFlow<RequestResult?>(null)
    val publishResult: StateFlow<RequestResult?> = _publishResult.asStateFlow()

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category, aiRecommendationReason = null) }
    }

    fun recommendCategory() {
        val desc = description.value
        if (desc.isBlank()) return

        viewModelScope.launch {
            try {
                _state.update { it.copy(isRecommendingCategory = true) }
                val recommendation = categoryRecommender.recommendCategory(desc)
                
                if (recommendation != null) {
                    _state.update { 
                        it.copy(
                            pendingRecommendation = recommendation,
                            showAiRecommendationDialog = true
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        pendingRecommendation = com.example.movilexplora.domain.ai.Recommendation("Gastronomia", "Error inesperado: ${e.message?.take(15)}"),
                        showAiRecommendationDialog = true
                    )
                }
                e.printStackTrace()
            } finally {
                _state.update { it.copy(isRecommendingCategory = false) }
            }
        }
    }

    fun acceptRecommendation() {
        _state.value.pendingRecommendation?.let { recommendation ->
            _state.update {
                it.copy(
                    selectedCategory = recommendation.category,
                    aiRecommendationReason = recommendation.reason,
                    pendingRecommendation = null,
                    showAiRecommendationDialog = false
                )
            }
        }
    }

    fun dismissRecommendation() {
        _state.update {
            it.copy(
                pendingRecommendation = null,
                showAiRecommendationDialog = false
            )
        }
    }

    fun selectPriceRange(range: Int) {
        _state.update { it.copy(selectedPriceRange = range) }
    }

    fun selectTime(time: String) {
        _state.update { it.copy(selectedTime = time) }
    }

    fun onImageSelected(uri: Uri?) {
        _state.update { it.copy(imageUri = uri) }
    }

    fun updateLocation(latitude: Double, longitude: Double, address: String) {
        _state.update {
            it.copy(
                selectedLatitude = latitude,
                selectedLongitude = longitude,
                address = address
            )
        }
    }

    fun publish() {
        if (title.isValid && description.isValid && (_state.value.selectedCategory != null)) {
            viewModelScope.launch {
                _publishResult.value = RequestResult.Loading

                try {
                    val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "1"
                    var imageUrl = ""
                    val currentUri = _state.value.imageUri

                    if (currentUri != null) {
                        // 1. OBTENER URL (Subir si es necesario)
                        if (currentUri.toString().startsWith("http://") || currentUri.toString().startsWith("https://")) {
                            imageUrl = currentUri.toString()
                        } else {
                            // Aquí el código SE ESPERA hasta que Cloudinary responda
                            imageUrl = imageRepository.uploadImage(currentUri) ?: ""
                        }

                        // 2. VALIDACIÓN CRÍTICA: Si no hay URL, no seguimos
                        if (imageUrl.isEmpty()) {
                            _publishResult.value = RequestResult.Failure("Error al subir la imagen. Por favor, intenta de nuevo.")
                            return@launch // Detenemos la ejecución aquí
                        }

                        // 3. CREAR POST (Solo si llegamos aquí es porque tenemos URL)
                        val newPost = Post(
                            id = postId ?: System.currentTimeMillis().toString(),
                            title = title.value,
                            location = _state.value.address.ifEmpty { resources.getString(R.string.location_not_specified) },
                            rating = 0.0,
                            category = _state.value.selectedCategory!!,
                            price = "$".repeat(_state.value.selectedPriceRange),
                            status = PostStatus.PENDIENTE,
                            imageUrl = imageUrl,
                            description = description.value,
                            latitude = _state.value.selectedLatitude ?: 0.0,
                            longitude = _state.value.selectedLongitude ?: 0.0,
                            likedBy = emptyList(),
                            distance = 5f,
                            creatorId = userId
                        )

                        // 4. SUBIR A FIRESTORE
                        postRepository.addPost(newPost)

                        if (postId == null) {
                            userRepository.addPoints(userId, 50)
                            _publishResult.value = RequestResult.Success(resources.getString(R.string.post_created_success_points))
                        } else {
                            _publishResult.value = RequestResult.Success("")
                        }

                    } else {
                        _publishResult.value = RequestResult.Failure("Es necesario asignar una imagen a la publicación")
                    }

                } catch (e: Exception) {
                    _publishResult.value = RequestResult.Failure(e.message ?: "Error al publicar")
                    e.printStackTrace()
                }
            }
        }
    }
    fun resetResult() {
        _publishResult.value = null
    }
}
