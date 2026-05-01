package com.example.movilexplora.features.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_invalid_email)
            else -> null
        }
    }

    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_password_required)
            value.length < 6 -> resources.getString(R.string.error_password_min_6_detailed)
            else -> null
        }
    }

    val isFormValid: Boolean
    get() = email.isValid && password.isValid

    private val _loginResult = MutableStateFlow<RequestResult?>(null)
    val loginResult: StateFlow<RequestResult?> = _loginResult.asStateFlow()

    fun login() {
        if (isFormValid) {
            // Simulación de validación con datos estáticos
            _loginResult.value = if (email.value == "carlos@email.com" && password.value == "123456") {
                RequestResult.Success(resources.getString(R.string.login_success))
            } else {
                RequestResult.Failure(resources.getString(R.string.login_invalid_credentials))
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }
}

