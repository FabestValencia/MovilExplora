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
import kotlinx.coroutines.launch
import android.net.Uri

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    val name = ValidatedField("Jean Botsito") { value ->
        if (value.isEmpty()) "El nombre es obligatorio" else null
    }

    val email = ValidatedField("bot.vaquero@example.com") { value ->
        when {
            value.isEmpty() -> "El email es obligatorio"
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Ingresa un email válido"
            else -> null
        }
    }
    
    val description = ValidatedField("") { _ -> null } // Opcional
    
    val location = ValidatedField("Bogotá, Colombia") { value ->
        if (value.isEmpty()) "La ubicación es obligatoria" else null
    }

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    fun onPhotoSelected(uri: Uri?) {
        _photoUri.value = uri
    }

    private val _updateResult = MutableStateFlow<RequestResult?>(null)
    val updateResult: StateFlow<RequestResult?> = _updateResult.asStateFlow()

    val isFormValid: Boolean
        get() = name.isValid && email.isValid && location.isValid

    fun updateProfile() {
        if (isFormValid) {
            // Simulación de actualización usando todos los campos
            _updateResult.value = RequestResult.Success("Perfil actualizado correctamente")
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
