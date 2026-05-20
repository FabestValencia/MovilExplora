package com.example.movilexplora.features.moderator

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.model.enum.UserRole
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.movilexplora.data.datastore.SessionDataStore
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class ModeratorViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionDataStore: SessionDataStore,
    private val resources: ResourceProvider
) : ViewModel() {
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
            else -> null
        }
    }

    private val _accessResult = MutableStateFlow<RequestResult?>(null)
    val accessResult: StateFlow<RequestResult?> = _accessResult.asStateFlow()

    val isFormValid: Boolean
        get() = email.isValid && password.isValid

    fun loginAdmin() {
        if (isFormValid) {
            viewModelScope.launch {
                _accessResult.value = RequestResult.Loading
                
                try {
                    val user = userRepository.login(email.value, password.value)
                    
                    if (user != null) {
                        android.util.Log.d("DEBUG_ROLE", "Usuario: ${user.name}, Rol String: ${user.role}, Rol Enum: ${user.userRole}")
                        if (user.userRole == UserRole.ADMIN) {
                            sessionDataStore.saveSession(user.id, UserRole.ADMIN)
                            _accessResult.value = RequestResult.Success(resources.getString(R.string.moderator_access_granted))
                        } else {
                            android.util.Log.e("DEBUG_ROLE", "Acceso denegado: El usuario no es ADMIN. Rol detectado: ${user.userRole}")
                            _accessResult.value = RequestResult.Failure(resources.getString(R.string.moderator_invalid_credentials))
                        }
                    } else {
                        // El login devolvió null (ej. credenciales incorrectas capturadas por el repo)
                        _accessResult.value = RequestResult.Failure(resources.getString(R.string.login_failure))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MODERATOR_LOGIN", "Error inesperado", e)
                    _accessResult.value = RequestResult.Failure(e.message ?: "Error desconocido")
                }
            }
        }
    }

    fun resetResult() {
        _accessResult.value = null
    }
}
