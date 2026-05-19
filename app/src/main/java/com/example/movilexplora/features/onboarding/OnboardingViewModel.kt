package com.example.movilexplora.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine

enum class PermissionType {
    LOCATION, CAMERA, GALLERY
}

data class OnboardingState(
    val isFirstLaunch: Boolean = true,
    val locationAlways: Boolean = false,
    val cameraAlways: Boolean = false,
    val galleryAlways: Boolean = false,
    val showPermissionDialog: PermissionType? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _showPermissionDialog = MutableStateFlow<PermissionType?>(null)

    val state: StateFlow<OnboardingState> = combine(
        settingsDataStore.isFirstLaunchFlow,
        settingsDataStore.locationAlwaysFlow,
        settingsDataStore.cameraAlwaysFlow,
        settingsDataStore.galleryAlwaysFlow,
        _showPermissionDialog
    ) { isFirst, loc, cam, gal, dialogType ->
        OnboardingState(isFirst, loc, cam, gal, dialogType)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OnboardingState())

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setFirstLaunchCompleted()
        }
    }

    /**
     * Checks if onboarding for the given permission should be shown.
     * If 'always' was previously selected, returns false.
     */
    fun checkAndShowPermissionOnboarding(permissionType: PermissionType, onAlreadyAlways: () -> Unit) {
        val isAlways = when (permissionType) {
            PermissionType.LOCATION -> state.value.locationAlways
            PermissionType.CAMERA -> state.value.cameraAlways
            PermissionType.GALLERY -> state.value.galleryAlways
        }

        if (isAlways) {
            onAlreadyAlways()
        } else {
            _showPermissionDialog.value = permissionType
        }
    }

    fun dismissPermissionDialog() {
        _showPermissionDialog.value = null
    }

    /**
     * Handles the user's choice for a specific permission.
     * If 'always' is true, we save it to DataStore.
     */
    fun onPermissionChoice(permissionType: PermissionType, always: Boolean) {
        viewModelScope.launch {
            if (always) {
                settingsDataStore.setPermissionAlways(permissionType.name, true)
            }
            dismissPermissionDialog()
        }
    }
}
