package com.example.movilexplora.features.moderator

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.domain.model.UserRole
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
                val user = userRepository.login(email.value, password.value)

                if (user != null && (user.role == UserRole.ADMIN || user.role == UserRole.MODERATOR)) {
                    sessionDataStore.saveSession(user.id, user.role)
                    _accessResult.value = RequestResult.Success(resources.getString(R.string.moderator_access_granted))
                } else {
                    _accessResult.value = RequestResult.Failure(resources.getString(R.string.moderator_invalid_credentials))
                }
            }
        }
    }

    fun resetResult() {
        _accessResult.value = null
    }
}
