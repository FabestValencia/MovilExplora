package com.example.movilexplora.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val LOCATION_ALWAYS = booleanPreferencesKey("location_always")
        val CAMERA_ALWAYS = booleanPreferencesKey("camera_always")
        val GALLERY_ALWAYS = booleanPreferencesKey("gallery_gallery")
    }

    val isFirstLaunchFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.IS_FIRST_LAUNCH] ?: true
    }

    val locationAlwaysFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.LOCATION_ALWAYS] ?: false
    }

    val cameraAlwaysFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.CAMERA_ALWAYS] ?: false
    }

    val galleryAlwaysFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.GALLERY_ALWAYS] ?: false
    }

    suspend fun setFirstLaunchCompleted() {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setPermissionAlways(permission: String, always: Boolean) {
        context.settingsDataStore.edit { prefs ->
            when (permission) {
                "LOCATION" -> prefs[Keys.LOCATION_ALWAYS] = always
                "CAMERA" -> prefs[Keys.CAMERA_ALWAYS] = always
                "GALLERY" -> prefs[Keys.GALLERY_ALWAYS] = always
            }
        }
    }

    val isDarkModeFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_MODE] ?: false // Default false
    }

    val notificationsEnabledFlow: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATIONS_ENABLED] ?: true // Default true
    }

    suspend fun toggleDarkMode(isDark: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun toggleNotifications(enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}

