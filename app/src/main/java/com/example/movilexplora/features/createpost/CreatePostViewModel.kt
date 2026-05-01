package com.example.movilexplora.features.createpost

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class CreatePostState(
    val selectedCategory: String? = null,
    val selectedPriceRange: Int = 2,
    val selectedTime: String? = null,
    val address: String = ""
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
) : ViewModel() {
    val title = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_post_title_empty) else null
    }

    val description = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_post_description_empty) else null
    }

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    private val _publishResult = MutableStateFlow<RequestResult?>(null)
    val publishResult: StateFlow<RequestResult?> = _publishResult.asStateFlow()

    fun selectCategory(category: String) {
        _state.update { it.copy(selectedCategory = category) }
    }

    fun selectPriceRange(range: Int) {
        _state.update { it.copy(selectedPriceRange = range) }
    }

    fun selectTime(time: String) {
        _state.update { it.copy(selectedTime = time) }
    }

    fun publish() {
        if (title.isValid && description.isValid && _state.value.selectedCategory != null) {
            viewModelScope.launch {
                val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "1" // Defaulting if null

                // TODO: Eliminar generación aleatoria de latitud y longitud una vez que se integre el mapa interactivo.
                val randomLat = (Math.random() * 0.8) - 0.4
                val randomLon = (Math.random() * 0.8) - 0.4

                val newPost = Post(
                    id = System.currentTimeMillis().toString(),
                    title = title.value,
                    location = _state.value.address.ifEmpty { resources.getString(R.string.location_not_specified) },
                    rating = 0.0,
                    category = _state.value.selectedCategory!!,
                    price = "$".repeat(_state.value.selectedPriceRange),
                    status = PostStatus.PENDIENTE,
                    imageUrl = "",
                    description = description.value,
                    latitude = randomLat,
                    longitude = randomLon,
                    likedBy = emptySet(),
                    distance = 5f,
                    creatorId = userId
                )
                postRepository.addPost(newPost)
                userRepository.addPoints(userId, 50) // Granting initial 50 points
                _publishResult.value = RequestResult.Success(resources.getString(R.string.post_created_success_points))
            }
        }
    }

    fun resetResult() {
        _publishResult.value = null
    }
}
