package com.example.movilexplora.features.moderator

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ModeratorViewModel : ViewModel() {
    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "La contraseña es obligatoria"
            else -> null
        }
    }

    private val _accessResult = MutableStateFlow<RequestResult?>(null)
    val accessResult: StateFlow<RequestResult?> = _accessResult.asStateFlow()

    val isFormValid: Boolean
        get() = email.isValid && password.isValid

    fun loginAdmin() {
        if (isFormValid) {
            // Simulación de validación de moderador
            if (email.value == "admin@explora.com" && password.value == "admin123") {
                _accessResult.value = RequestResult.Success("Acceso concedido")
            } else {
                _accessResult.value = RequestResult.Failure("Credenciales de moderador incorrectas")
            }
        }
    }

    fun resetResult() {
        _accessResult.value = null
    }
}
