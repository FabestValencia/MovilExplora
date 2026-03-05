package com.example.movilexplora.features.passwordReset

import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordResetViewModel : ViewModel() {
    val codeFields = List(5) {
        ValidatedField("") { value ->
            when {
                value.isEmpty() -> "Obligatorio"
                value.length > 1 || !value.all { it.isDigit() } -> "Un dígito"
                else -> null
            }
        }
    }

    val newPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "La contraseña es obligatoria"
            value.length < 6 -> "Debe tener al menos 6 caracteres"
            else -> null
        }
    }

    private val _resetResult = MutableStateFlow<RequestResult?>(null)
    val resetResult: StateFlow<RequestResult?> = _resetResult.asStateFlow()

    val isFormValid: Boolean
        get() = codeFields.all { it.isValid } && newPassword.isValid

    fun resetPassword() {
        if (isFormValid) {
            // Simulación de éxito
            _resetResult.value = RequestResult.Success("Contraseña restablecida con éxito")
        }
    }

    fun resetStatus() {
        _resetResult.value = null
    }
}