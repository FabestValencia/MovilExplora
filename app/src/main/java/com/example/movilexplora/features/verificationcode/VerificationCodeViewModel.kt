package com.example.movilexplora.features.verificationcode

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class VerificationCodeViewModel @Inject constructor(
    private val resources: ResourceProvider
) : ViewModel() {
    private val _verificationResult = MutableStateFlow<RequestResult?>(null)
    val verificationResult: StateFlow<RequestResult?> = _verificationResult.asStateFlow()

    fun verifyCode(code: String) {
        if (code.length == 6) {
            // Simulación de verificación de código
            _verificationResult.value = if (code == "123456") {
                RequestResult.Success(resources.getString(R.string.verification_code_success))
            } else {
                RequestResult.Failure(resources.getString(R.string.verification_code_invalid))
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
