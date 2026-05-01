package com.example.movilexplora.features.forgotpassword

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val resources: ResourceProvider
) : ViewModel() {
    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_email_invalid)
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
            _requestResult.value = RequestResult.Success(resources.getString(R.string.forgot_password_link_sent))
        }
    }

    fun resetResult() {
        _requestResult.value = null
    }
}
