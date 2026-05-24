package com.example.movilexplora.features.notifications

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import com.example.movilexplora.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsState(
    val recentNotifications: List<Notification> = emptyList(),
    val olderNotifications: List<Notification> = emptyList(),
    val isPushEnabled: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsState())
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init {
        observeNotifications()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            sessionDataStore.pushNotificationsEnabled.collect { enabled ->
                _state.update { it.copy(isPushEnabled = enabled) }
            }
        }
    }

    fun togglePushNotifications(enabled: Boolean) {
        viewModelScope.launch {
            sessionDataStore.setPushNotificationsEnabled(enabled)
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            _state.update { it.copy(isLoading = true) }
            
            notificationRepository.getNotifications(userId).collect { notifications ->
                val recent = notifications.filter { it.isNew }
                val older = notifications.filter { !it.isNew }
                
                _state.update { 
                    it.copy(
                        recentNotifications = recent,
                        olderNotifications = older,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val userId = sessionDataStore.sessionFlow.firstOrNull()?.userId ?: return@launch
            _state.value.recentNotifications.forEach { 
                notificationRepository.markAsRead(userId, it.id)
            }
        }
    }
}
