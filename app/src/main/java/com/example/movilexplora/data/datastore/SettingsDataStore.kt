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

