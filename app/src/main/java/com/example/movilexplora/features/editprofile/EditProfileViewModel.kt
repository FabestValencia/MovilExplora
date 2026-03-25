package com.example.movilexplora.features.editprofile

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditProfileViewModel : ViewModel() {
    val name = ValidatedField("Jean Botsito") { value ->
        if (value.isEmpty()) "El nombre es obligatorio" else null
    }

    val email = ValidatedField("bot.vaquero@example.com") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    private val _updateResult = MutableStateFlow<RequestResult?>(null)
    val updateResult: StateFlow<RequestResult?> = _updateResult.asStateFlow()

    val isFormValid: Boolean
        get() = name.isValid && email.isValid

    fun updateProfile() {
        if (isFormValid) {
            // Simulación de actualización
            _updateResult.value = RequestResult.Success("Perfil actualizado correctamente")
        }
    }

    fun resetResult() {
        _updateResult.value = null
    }
}
