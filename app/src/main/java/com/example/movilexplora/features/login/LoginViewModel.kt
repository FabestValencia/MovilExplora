package com.example.movilexplora.features.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionDataStore: SessionDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Clase interna para manejar el estado de cada campo (Email y Password)
    class FieldState(initialValue: String = "") {
        var value by mutableStateOf(initialValue)
        var error by mutableStateOf<String?>(null)
        fun onChange(newValue: String) {
            value = newValue
            error = null
        }
    }

    val email = FieldState()
    val password = FieldState()

    private val _loginResult = MutableStateFlow<RequestResult?>(null)
    val loginResult: StateFlow<RequestResult?> = _loginResult.asStateFlow()

    // Propiedad para habilitar/deshabilitar el botón de login
    val isFormValid: Boolean
        get() = email.value.isNotBlank() && password.value.isNotBlank() && email.error == null && password.error == null

    fun login() {
        if (!isFormValid) return

        viewModelScope.launch {
            _loginResult.value = RequestResult.Loading
            
            runCatching {
                userRepository.login(email.value, password.value)
            }.onSuccess { user ->
                if (user != null && user.id != "1") {
                    sessionDataStore.saveSession(userId = user.id, role = user.userRole)
                    _loginResult.value = RequestResult.Success(context.getString(R.string.login_success))
                } else {
                    _loginResult.value = RequestResult.Failure(context.getString(R.string.login_failure))
                }
            }.onFailure {
                val message = if (it.message?.contains("no user record") == true || it.message?.contains("INVALID_LOGIN_CREDENTIALS") == true) {
                    "La cuenta no existe o las credenciales son incorrectas."
                } else if (it.message?.startsWith("CUENTA_NO_VERIFICADA") == true) {
                    it.message!!
                } else {
                    it.message ?: "Error al iniciar sesión"
                }
                android.util.Log.e("LOGIN_ERROR", "Fallo en login: ${it.message}")
                _loginResult.value = RequestResult.Failure(message)
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _loginResult.value = RequestResult.Loading
            runCatching {
                userRepository.loginWithGoogle(idToken)
            }.onSuccess { user ->
                if (user != null && user.id != "1") {
                    sessionDataStore.saveSession(userId = user.id, role = user.userRole)
                    _loginResult.value = RequestResult.Success(context.getString(R.string.login_success))
                } else {
                    _loginResult.value = RequestResult.Failure("Error al autenticar con Google")
                }
            }.onFailure {
                val message = when (it.message) {
                    "CUENTA_NO_EXISTE" -> "CUENTA_NO_EXISTE"
                    else -> it.message ?: "Error en autenticación Google"
                }
                _loginResult.value = RequestResult.Failure(message)
            }
        }
    }

    fun resetLoginResult() {
        _loginResult.value = null
    }
}
