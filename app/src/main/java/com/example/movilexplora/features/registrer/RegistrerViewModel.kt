package com.example.movilexplora.features.registrer

import android.util.Patterns
import androidx.lifecycle.ViewModel
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

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val nombre = ValidatedField("") { if (it.isEmpty()) "El nombre es obligatorio" else null }

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }

    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "La contraseña es obligatoria"
            value.length < 8 -> "Debe tener al menos 8 caracteres"
            else -> null
        }
    }

    val confirmPassword = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "Debes confirmar la contraseña"
            value != password.value -> "Las contraseñas no coinciden"
            else -> null
        }
    }

    val isFormValid: Boolean
        get() = nombre.isValid && email.isValid && password.isValid && confirmPassword.isValid

    private val _registerResult = MutableStateFlow<RequestResult?>(null)
    val registerResult: StateFlow<RequestResult?> = _registerResult.asStateFlow()

    fun register() {
        if (isFormValid) {
            val newUser = User(
                id = UUID.randomUUID().toString(),
                name = nombre.value,
                email = email.value,
                password = password.value,
                city = "",
                address = "",
                profilePictureUrl = ""
            )
            userRepository.save(newUser)
            _registerResult.value = RequestResult.Success("Usuario registrado exitosamente")
        }
    }

    fun resetRegisterResult() {
        _registerResult.value = null
    }
}
