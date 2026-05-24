package com.example.movilexplora.data.repository

import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.domain.repository.NotificationRepository
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.Notification
import com.example.movilexplora.domain.model.NotificationType
import com.example.movilexplora.domain.model.ReputationLevel
import com.example.movilexplora.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val notificationRepository: NotificationRepository,
    private val resources: ResourceProvider
) : UserRepository {

    private val collection = firestore.collection("users")
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun observeUser(id: String): Flow<User?> = callbackFlow {
        val listener = collection.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(User::class.java)?.apply { this.id = snapshot.id }
            trySend(user)
        }
        awaitClose { listener.remove() }
    }

    init {
        // Limpiamos datos locales antiguos que puedan causar conflictos
        scope.launch {
            try {
                val localUsers = userDao.getAllUsers().first()
                if (localUsers.any { it.id == "1" }) {
                    userDao.clearAll()
                    android.util.Log.d("FIREBASE_DEBUG", "Base de datos local antigua limpiada")
                }
            } catch (_: Exception) {}
        }
    }

    override suspend fun save(user: User): String {
        // Intentar registrar en Auth si tiene password
        val uid = if (user.password != null) {
            val result = auth.createUserWithEmailAndPassword(user.email, user.password!!).await()
            val firebaseUser = result.user ?: throw Exception("Error al crear usuario")
            
            // Enviar correo de verificación nativo de Firebase
            firebaseUser.sendEmailVerification().await()
            android.util.Log.d("FIREBASE_DEBUG", "Correo de verificación enviado a ${user.email}")
            
            firebaseUser.uid
        } else {
            user.id.ifEmpty { collection.document().id }
        }

        // Guardar en Firestore (ya no necesitamos verificationCode de 6 dígitos)
        val userCopy = user.copy(
            id = uid, 
            password = null, 
            isVerified = false, 
            verificationCode = ""
        )
        
        collection.document(uid).set(userCopy).await()
        return uid
    }

    override suspend fun findById(id: String): User? {
        return try {
            val snapshot = collection.document(id).get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.apply { this.id = snapshot.id }
                
                // Sincronizar estado de verificación de Firebase Auth con Firestore si es necesario
                val firebaseUser = auth.currentUser
                if (firebaseUser != null && firebaseUser.uid == id && firebaseUser.isEmailVerified && user?.isVerified == false) {
                    collection.document(id).update("isVerified", true).await()
                    user.isVerified = true
                }
                
                android.util.Log.d("FIREBASE_DEBUG", "findById: Usuario encontrado: ${user?.name}, Rol: ${user?.role}")
                user
            } else {
                android.util.Log.e("FIREBASE_DEBUG", "findById: El documento con ID $id no existe en Firestore")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "findById: Error mapeando usuario: ${e.message}")
            null
        }
    }

    override suspend fun login(email: String, password: String): User? {
        return try {
            android.util.Log.d("FIREBASE_DEBUG", "Intentando login para: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user
            
            if (firebaseUser != null) {
                // Obtenemos el perfil de Firestore primero para chequear el ROL
                val user = findById(firebaseUser.uid)

                // Forzamos recarga del usuario para obtener el estado de verificación más reciente
                firebaseUser.reload().await()
                
                // REGLA: Si el usuario es ADMIN o MODERATOR, permitimos entrar sin verificar email
                // (útil para cuentas de staff pre-creadas o pruebas)
                val isStaff = user?.role == "ADMIN" || user?.role == "MODERATOR"

                // Verificar si el correo está validado nativamente
                if (!firebaseUser.isEmailVerified && !isStaff) {
                    // Si no es staff y no está verificado, reenviamos el correo y lanzamos error
                    firebaseUser.sendEmailVerification().await()
                    throw Exception("CUENTA_NO_VERIFICADA:${firebaseUser.uid}")
                }

                android.util.Log.d("FIREBASE_DEBUG", "Login Auth exitoso, UID: ${firebaseUser.uid}, Staff: $isStaff")
                user
            } else {
                android.util.Log.e("FIREBASE_DEBUG", "Usuario es nulo después de Auth")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "Error en login Auth: ${e.message}")
            throw e
        }
    }

    override suspend fun loginWithGoogle(idToken: String): User? {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return null
            
            android.util.Log.d("FIREBASE_DEBUG", "Google Login Auth exitoso: ${firebaseUser.email}, UID: ${firebaseUser.uid}")
            
            // Intentar obtener perfil existente
            var user = findById(firebaseUser.uid)
            
            if (user == null) {
                android.util.Log.d("FIREBASE_DEBUG", "Perfil no encontrado en Firestore")
                throw Exception("CUENTA_NO_EXISTE")
            }
            
            // NOTA: Para Google, Firebase Auth ya marca isEmailVerified como true por defecto.
            // Sincronizamos en Firestore si no estaba marcado.
            if (!user.isVerified && firebaseUser.isEmailVerified) {
                collection.document(firebaseUser.uid).update("isVerified", true).await()
                user = user.copy(isVerified = true)
            } else if (!user.isVerified) {
                 throw Exception("CUENTA_NO_VERIFICADA:${firebaseUser.uid}")
            }
            
            return user
        } catch (e: Exception) {
            if (e.message == "CUENTA_NO_EXISTE" || e.message?.startsWith("CUENTA_NO_VERIFICADA") == true) throw e
            android.util.Log.e("FIREBASE_DEBUG", "Error en loginWithGoogle: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    override suspend fun addPoints(userId: String, points: Int) {
        val user = findById(userId)
        user?.let {
            val oldPoints = it.points
            val newPoints = oldPoints + points
            
            val oldLevel = calculateLevel(oldPoints)
            val newLevel = calculateLevel(newPoints)
            
            val updatedUser = it.copy(points = newPoints)
            collection.document(userId).set(updatedUser).await()
            
            if (newLevel != oldLevel) {
                // Generar notificación de subida de nivel
                notificationRepository.addNotification(
                    userId = userId,
                    notification = Notification(
                        type = NotificationType.ACHIEVEMENT,
                        title = resources.getString(R.string.notification_level_up_title),
                        description = resources.getString(R.string.notification_level_up_desc, resources.getString(newLevel.displayNameRes)),
                        time = resources.getString(R.string.notification_time_recent),
                        isNew = true
                    )
                )
            }
        }
    }

    private fun calculateLevel(points: Long): ReputationLevel {
        return when {
            points < 100 -> ReputationLevel.TURISTA
            points < 500 -> ReputationLevel.EXPLORADOR
            points < 1000 -> ReputationLevel.AVENTURERO
            else -> ReputationLevel.EMBAJADOR
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun updateFcmToken(userId: String, token: String) {
        try {
            // Usamos update para que SOLO funcione si el usuario ya existe en Firestore
            if (userId.isNotEmpty() && userId != "1") {
                collection.document(userId).update("fcmToken", token).await()
                android.util.Log.d("FIREBASE_DEBUG", "FCM Token actualizado para $userId")
            }
        } catch (_: Exception) {
            // Si el documento no existe, no hacemos nada (evita crear usuarios basura)
            android.util.Log.e("FIREBASE_DEBUG", "No se pudo actualizar FCM: Usuario no encontrado o sesión inválida.")
        }
    }

    override suspend fun softDeleteUser(userId: String) {
        try {
            collection.document(userId).update("isDeleted", true).await()
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "Error en softDelete: ${e.message}")
        }
    }

    override suspend fun verifyCode(userId: String, code: String): User? {
        // Obsoleto para email/password nativo. 
        // Sincronizamos con Firebase Auth para confirmar verificación.
        val firebaseUser = auth.currentUser
        if (firebaseUser != null && firebaseUser.uid == userId) {
            firebaseUser.reload().await()
            if (firebaseUser.isEmailVerified) {
                return findById(userId)
            }
        }
        return null
    }

    override suspend fun resendVerificationCode(userId: String) {
        try {
            // Reenviar correo de verificación nativo de Firebase
            auth.currentUser?.sendEmailVerification()?.await()
            android.util.Log.d("FIREBASE_DEBUG", "Enlace de verificación reenviado vía Firebase Auth")
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "Error resending email: ${e.message}")
        }
    }
}
