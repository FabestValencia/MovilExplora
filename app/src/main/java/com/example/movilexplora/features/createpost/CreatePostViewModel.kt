package com.example.movilexplora.features.createpost

import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CreatePostState(
    val selectedCategory: String? = null,
    val selectedPriceRange: Int = 2,
    val selectedTime: String? = null,
    val address: String = ""
)

class CreatePostViewModel : ViewModel() {
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
            // Simulación de publicación
            _publishResult.value = RequestResult.Success("Publicación creada correctamente")
        }
    }

    fun resetResult() {
        _publishResult.value = null
    }
}
