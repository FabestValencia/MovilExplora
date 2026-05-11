package com.example.movilexplora.features.resetpassword

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resources: ResourceProvider
) : ViewModel() {
    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_password_empty)
            value.length < 8 -> resources.getString(R.string.error_password_min_8)
            else -> null
        }
    }

    val confirmPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_confirm_password_empty)
            value != password.value -> resources.getString(R.string.error_password_mismatch)
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
            _resetResult.value = RequestResult.Success(resources.getString(R.string.reset_password_success))
        }
    }

    fun resetResultState() {
        _resetResult.value = null
    }
}
