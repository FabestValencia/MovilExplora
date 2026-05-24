package com.example.movilexplora.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.model.UserSession
import com.example.movilexplora.data.datastore.SessionDataStore
import com.example.movilexplora.domain.model.enum.UserRole
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define los posibles estados de la sesión
sealed interface SessionState {
    data object Loading : SessionState
    data object NotAuthenticated : SessionState
    data class Authenticated(val session: UserSession) : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Flujo que representa el estado de la sesión
    val sessionState: StateFlow<SessionState> = sessionDataStore.sessionFlow
        .map { session ->
            val firebaseUser = auth.currentUser
            
            // Verificación rigurosa síncrona
            val isStaff = session?.role == UserRole.ADMIN
            
            val isValid = session != null && 
                         firebaseUser != null && 
                         firebaseUser.uid == session.userId && 
                         (firebaseUser.isEmailVerified || isStaff)

            if (isValid) {
                SessionState.Authenticated(session)
            } else {
                // Si la sesión local existe pero no es válida según Firebase,
                // no lanzamos corrutinas aquí. El MainActivity se encargará de la limpieza
                // cuando detecte el estado NotAuthenticated.
                SessionState.NotAuthenticated
            }
        }
        .stateIn(
            // Convierte el flujo en un StateFlow para que pueda ser observado
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState.Loading
        )

    fun login(userId: String, role: UserRole) {
        // Guarda la sesión del usuario en Data Store. Se utiliza viewModelScope para lanzar la corrutina
        viewModelScope.launch {
            sessionDataStore.saveSession(userId, role)
        }
    }

    fun logout() {
        // Limpia la sesión del usuario en Data Store y la caché local de posts/eventos
        viewModelScope.launch {
            try {
                sessionDataStore.clearSession()
                postRepository.clearCache()
                eventRepository.clearCache()
                auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateFcmToken(userId: String, token: String) {
        viewModelScope.launch {
            userRepository.updateFcmToken(userId, token)
        }
    }
}

