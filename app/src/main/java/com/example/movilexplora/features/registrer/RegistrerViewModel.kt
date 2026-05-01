package com.example.movilexplora.features.registrer

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    private val resources = getApplication<Application>().resources

    val nombre = ValidatedField("") { if (it.isEmpty()) resources.getString(R.string.error_name_required) else null }
    val ciudad = ValidatedField("") { if (it.isEmpty()) resources.getString(R.string.error_city_required) else null }
    val direccion = ValidatedField("") { if (it.isEmpty()) resources.getString(R.string.error_address_required) else null }

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
            value.length < 6 -> resources.getString(R.string.error_password_min_6)
            else -> null
        }
    }

    // Validación especial para confirmar contraseña
    val confirmPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_confirm_password_required)
            value != password.value -> resources.getString(R.string.error_password_mismatch)
            else -> null
        }
    }

    val isFormValid: Boolean
        get() = nombre.isValid && ciudad.isValid && direccion.isValid &&
                email.isValid && password.isValid && confirmPassword.isValid

    private val _registerResult = MutableStateFlow<RequestResult?>(null)
    val registerResult: StateFlow<RequestResult?> = _registerResult.asStateFlow()

    fun register() {
        if (isFormValid) {
            // Simulación del proceso de registro [3]
            _registerResult.value = RequestResult.Success(resources.getString(R.string.register_success))
        }
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}
