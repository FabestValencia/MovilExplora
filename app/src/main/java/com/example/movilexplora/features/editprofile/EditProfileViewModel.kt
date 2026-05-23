package com.example.movilexplora.features.editprofile

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import android.util.Patterns
import androidx.lifecycle.ViewModel
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
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
import com.example.movilexplora.domain.repository.ImageRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import android.net.Uri

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val imageRepository: ImageRepository,
    private val resources: ResourceProvider
) : ViewModel() {
    val name = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_name_empty) else null
    }

    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_empty)
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_email_invalid)
            else -> null
        }
    }
    
    val description = ValidatedField("") { _ -> null } // Opcional
    
    val location = ValidatedField("") { value ->
        if (value.isEmpty()) resources.getString(R.string.error_location_empty) else null
    }

    private val _photoUrl = MutableStateFlow<String>("")
    val photoUrl: StateFlow<String> = _photoUrl.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    init {
        viewModelScope.launch {
            @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
            sessionDataStore.sessionFlow.flatMapLatest { session ->
                val userId = session?.userId
                if (userId != null && userId != "guest") {
                    userRepository.observeUser(userId)
                } else {
                    kotlinx.coroutines.flow.flowOf(null)
                }
            }.collect { user ->
                if (user != null) {
                    if (name.value.isEmpty()) name.onChange(user.name)
                    if (email.value.isEmpty()) email.onChange(user.email)
                    if (location.value.isEmpty()) location.onChange(user.city)
                    if (description.value.isEmpty()) description.onChange(user.address)
                    _photoUrl.value = user.profilePictureUrl
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
                        _updateResult.value = RequestResult.Loading
                        
                        val newPhotoUrl = _photoUri.value?.let { uri ->
                            imageRepository.uploadImage(uri)
                        } ?: _photoUrl.value

                        val updatedUser = user.copy(
                            name = name.value,
                            email = email.value,
                            city = location.value,
                            address = description.value,
                            profilePictureUrl = newPhotoUrl
                        )
                        userRepository.save(updatedUser)
                        _photoUrl.value = newPhotoUrl
                        _photoUri.value = null
                        _updateResult.value = RequestResult.Success(resources.getString(R.string.profile_updated_success))
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
