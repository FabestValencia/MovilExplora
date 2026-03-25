package com.example.movilexplora.features.forgotpassword

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordViewModel : ViewModel() {
    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    private val _requestResult = MutableStateFlow<RequestResult?>(null)
    val requestResult: StateFlow<RequestResult?> = _requestResult.asStateFlow()

    val isFormValid: Boolean
        get() = email.isValid

    fun sendResetLink() {
        if (isFormValid) {
            // Simulación de envío de correo
            _requestResult.value = RequestResult.Success("Enlace enviado correctamente")
        }
    }

    fun resetResult() {
        _requestResult.value = null
    }
}
