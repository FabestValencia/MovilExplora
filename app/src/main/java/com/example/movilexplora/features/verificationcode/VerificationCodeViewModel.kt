package com.example.movilexplora.features.verificationcode

import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VerificationCodeViewModel : ViewModel() {
    private val _verificationResult = MutableStateFlow<RequestResult?>(null)
    val verificationResult: StateFlow<RequestResult?> = _verificationResult.asStateFlow()

    fun verifyCode(code: String) {
        if (code.length == 6) {
            // Simulación de verificación de código
            _verificationResult.value = if (code == "123456") {
                RequestResult.Success("Código verificado correctamente")
            } else {
                RequestResult.Failure("Código incorrecto")
            }
        }
    }

    fun resendCode() {
        // Simulación de reenvío de código
    }

    fun resetResult() {
        _verificationResult.value = null
    }
}
