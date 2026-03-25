package com.example.movilexplora.features.resetpassword

import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResetPasswordViewModel : ViewModel() {
    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "La contraseña es obligatoria"
            value.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
            else -> null
        }
    }

    val confirmPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "Debes confirmar la contraseña"
            value != password.value -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    private val _resetResult = MutableStateFlow<RequestResult?>(null)
    val resetResult: StateFlow<RequestResult?> = _resetResult.asStateFlow()

    val isFormValid: Boolean
        get() = password.isValid && confirmPassword.isValid

    fun resetPassword() {
        if (isFormValid) {
            // Simulación de cambio de contraseña exitoso
            _resetResult.value = RequestResult.Success("Contraseña restablecida con éxito")
        }
    }

    fun resetResultState() {
        _resetResult.value = null
    }
}
