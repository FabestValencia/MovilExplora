package com.example.movilexplora.features.createpost

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePostState(
    val selectedCategory: String? = null,
    val selectedPriceRange: Int = 2,
    val selectedTime: String? = null,
    val address: String = ""
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    val title = ValidatedField("") { value ->
        if (value.isEmpty()) "El título es obligatorio" else null
    }

    val description = ValidatedField("") { value ->
        if (value.isEmpty()) "La descripción es obligatoria" else null
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
                val newPost = Post(
                    id = System.currentTimeMillis().toString(),
                    title = title.value,
                    location = _state.value.address.ifEmpty { "Ubicación no especificada" },
                    rating = 0.0,
                    category = _state.value.selectedCategory!!,
                    price = "$".repeat(_state.value.selectedPriceRange),
                    status = PostStatus.PENDIENTE,
                    imageUrl = "",
                    likedBy = emptySet(),
                    distance = 5f
                )
                postRepository.addPost(newPost)
                _publishResult.value = RequestResult.Success("Publicación creada correctamente")
            }
        }
    }

    fun resetResult() {
        _publishResult.value = null
    }
}
