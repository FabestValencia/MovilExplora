package com.example.movilexplora.features.editprofile

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.core.utils.RequestResult
import com.example.movilexplora.core.utils.ValidatedField
import com.example.movilexplora.data.datastore.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.net.Uri

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {
    val name = ValidatedField("") { value ->
        if (value.isEmpty()) "El nombre es obligatorio" else null
    }

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }
    
    val description = ValidatedField("") { _ -> null } // Opcional
    
    val location = ValidatedField("") { value ->
        if (value.isEmpty()) "La ubicación es obligatoria" else null
    }

    private val _photoUrl = MutableStateFlow<String>("")
    val photoUrl: StateFlow<String> = _photoUrl.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    init {
        viewModelScope.launch {
            sessionDataStore.sessionFlow.collect { session ->
                if (session != null) {
                    val user = userRepository.findById(session.userId)
                    if (user != null) {
                        // TODO: Eliminar mocking de location y address si están vacíos al integrar mapas
                        val mockCity = user.city.ifEmpty { "Ciudad Ficticia" }
                        val mockAddress = user.address.ifEmpty {
                            val randomLat = (Math.random() * 0.8) - 0.4
                            val randomLon = (Math.random() * 0.8) - 0.4
                            "Lat: $randomLat, Lon: $randomLon"
                        }

                        if (name.value.isEmpty()) name.onChange(user.name)
                        if (email.value.isEmpty()) email.onChange(user.email)
                        if (location.value.isEmpty()) location.onChange(mockCity)
                        if (description.value.isEmpty()) description.onChange(mockAddress)
                        _photoUrl.value = user.profilePictureUrl
                    }
                }
            }
        }
    }

    fun onPhotoSelected(uri: Uri?) {
        _photoUri.value = uri
    }

    private val _updateResult = MutableStateFlow<RequestResult?>(null)
    val updateResult: StateFlow<RequestResult?> = _updateResult.asStateFlow()

    val isFormValid: Boolean
        get() = name.isValid && email.isValid && location.isValid

    fun updateProfile() {
        if (isFormValid) {
            viewModelScope.launch {
                val session = sessionDataStore.sessionFlow.firstOrNull()
                if (session != null) {
                    val user = userRepository.findById(session.userId)
                    if (user != null) {
                        val updatedUser = user.copy(
                            name = name.value,
                            email = email.value,
                            city = location.value,
                            address = description.value,
                            profilePictureUrl = _photoUri.value?.toString() ?: _photoUrl.value
                        )
                        userRepository.save(updatedUser)
                        _updateResult.value = RequestResult.Success("Perfil actualizado correctamente")
                    }
                }
            }
        }
    }

    val isDarkMode: StateFlow<Boolean> = settingsDataStore.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val notificationsEnabled: StateFlow<Boolean> = settingsDataStore.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.toggleDarkMode(enabled)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsDataStore.toggleNotifications(enabled)
        }
    }

    fun resetResult() {
        _updateResult.value = null
    }
}
