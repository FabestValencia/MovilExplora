package com.example.movilexplora.features.forgotpassword

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
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
            viewModelScope.launch {
                _requestResult.value = RequestResult.Loading
                try {
                    userRepository.sendPasswordResetEmail(email.value)
                    _requestResult.value = RequestResult.Success(resources.getString(R.string.forgot_password_link_sent))
                } catch (e: Exception) {
                    _requestResult.value = RequestResult.Failure(e.message ?: "Error al enviar correo")
                }
            }
        }
    }

    fun resetResult() {
        _requestResult.value = null
    }
}
