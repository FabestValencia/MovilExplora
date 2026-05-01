package com.example.movilexplora.features.passwordReset

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordResetViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    val codeFields = List(5) {
        ValidatedField("") { value ->
            when {
                value.isEmpty() -> resources.getString(R.string.error_required)
                value.length > 1 || !value.all { it.isDigit() } -> resources.getString(R.string.error_single_digit)
                else -> null
            }
        }
    }

    val newPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_password_required)
            value.length < 6 -> resources.getString(R.string.error_password_min_6)
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
            _resetResult.value = RequestResult.Success(resources.getString(R.string.reset_password_success))
        }
    }

    fun resetStatus() {
        _resetResult.value = null
    }
}
