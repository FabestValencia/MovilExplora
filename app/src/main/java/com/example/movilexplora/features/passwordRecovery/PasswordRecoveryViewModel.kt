package com.example.movilexplora.features.passwordRecovery

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordRecoveryViewModel : ViewModel() {

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    private val _recoveryResult = MutableStateFlow<RequestResult?>(null)
    val recoveryResult: StateFlow<RequestResult?> = _recoveryResult.asStateFlow()

    val isFormValid: Boolean get() = email.isValid

    fun recoverPassword() {
        if (isFormValid) {
            _recoveryResult.value = RequestResult.Success("Se ha enviado un correo de recuperación a ${email.value}")
        }
    }

    fun resetRecoveryResult() {
        _recoveryResult.value = null
    }
}