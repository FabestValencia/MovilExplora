package com.example.movilexplora.features.registrer

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel : ViewModel() {

    val nombre = ValidatedField("") { if (it.isEmpty()) "El nombre es obligatorio" else null }
    val ciudad = ValidatedField("") { if (it.isEmpty()) "La ciudad es obligatoria" else null }
    val direccion = ValidatedField("") { if (it.isEmpty()) "La dirección es obligatoria" else null }

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
            value.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
    }

    // Validación especial para confirmar contraseña
    val confirmPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "Debes confirmar la contraseña"
            value != password.value -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    val isFormValid: Boolean
        get() = nombre.isValid && ciudad.isValid && direccion.isValid &&
                email.isValid && password.isValid && confirmPassword.isValid

    private val _registerResult = MutableStateFlow<RequestResult?>(null)
    val registerResult: StateFlow<RequestResult?> = _registerResult.asStateFlow()

    fun register() {
        if (isFormValid) {
            // Simulación del proceso de registro [3]
            _registerResult.value = RequestResult.Success("Usuario registrado exitosamente")
        }
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}