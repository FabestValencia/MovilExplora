package com.example.movilexplora.features.registrer

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
) : ViewModel() {

    val nombre = ValidatedField("") { if (it.isEmpty()) resources.getString(R.string.error_name_empty) else null }

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_email_invalid)
            else -> null
        }
    }

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

    val isFormValid: Boolean
        get() = nombre.isValid && email.isValid && password.isValid && confirmPassword.isValid

    private val _registerResult = MutableStateFlow<RequestResult?>(null)
    val registerResult: StateFlow<RequestResult?> = _registerResult.asStateFlow()

    fun register() {
        nombre.markAsDirty()
        email.markAsDirty()
        password.markAsDirty()
        confirmPassword.markAsDirty()

        if (isFormValid) {
            // TODO: Eliminar direcciones (lat/lon quemadas mediante String) cuando se agregue mapa al registro de usuario.
            val randomLat = (Math.random() * 0.8) - 0.4
            val randomLon = (Math.random() * 0.8) - 0.4

            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = nombre.value,
                email = email.value,
                password = password.value,
                city = resources.getString(R.string.mock_city),
                address = "Lat: $randomLat, Lon: $randomLon",
                profilePictureUrl = ""
            )
            viewModelScope.launch {
                userRepository.save(newUser)
                _registerResult.value = RequestResult.Success(resources.getString(R.string.register_success_spanish))
            }
        }
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}
