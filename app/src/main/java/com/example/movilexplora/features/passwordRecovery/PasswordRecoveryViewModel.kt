package com.example.movilexplora.features.passwordRecovery

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PasswordRecoveryViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_invalid_email)
            else -> null
        }
    }

    private val _recoveryResult = MutableStateFlow<RequestResult?>(null)
    val recoveryResult: StateFlow<RequestResult?> = _recoveryResult.asStateFlow()

    val isFormValid: Boolean get() = email.isValid

    fun recoverPassword() {
        if (isFormValid) {
            _recoveryResult.value = RequestResult.Success(
                resources.getString(R.string.recovery_email_sent, email.value)
            )
        }
    }

    fun resetRecoveryResult() {
        _recoveryResult.value = null
    }
}
