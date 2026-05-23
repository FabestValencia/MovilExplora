package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.repository.UserRepository
import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.entity.toDomainModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
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
        // Migración automática de datos locales a Firebase (solo perfiles)
        scope.launch {
            migrateLocalDataToFirebase()
        }
    }

    private suspend fun migrateLocalDataToFirebase() {
        try {
            val localUsers = userDao.getAllUsers().first()
            if (localUsers.isNotEmpty()) {
                localUsers.forEach { entity ->
                    val user = entity.toDomainModel()
                    // Subir a Firestore si no existe el ID
                    val doc = collection.document(user.id).get().await()
                    if (!doc.exists()) {
                        // Guardamos sin password por seguridad
                        collection.document(user.id).set(user.copy(password = null)).await()
                    }
                }
                // Una vez migrados, limpiamos la base de datos local para evitar duplicados en el futuro
                userDao.clearAll()
            }
        } catch (_: Exception) {
            // Error de migración silencioso
        }
    }

    override suspend fun save(user: User) {
        // Intentar registrar en Auth si tiene password
        val uid = if (user.password != null) {
            val result = auth.createUserWithEmailAndPassword(user.email, user.password!!).await()
            val firebaseUser = result.user ?: throw Exception("Error al crear usuario")
            
            // Enviar correo de verificación
            firebaseUser.sendEmailVerification().await()
            
            firebaseUser.uid
        } else {
            user.id.ifEmpty { collection.document().id }
        }

        val userCopy = user.copy(id = uid, password = null)
        collection.document(uid).set(userCopy).await()
    }

    override suspend fun findById(id: String): User? {
        return try {
            val snapshot = collection.document(id).get().await()
            if (snapshot.exists()) {
                val user = snapshot.toObject(User::class.java)?.apply { this.id = snapshot.id }
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
                // Forzar recarga para obtener el estado de verificación más reciente
                firebaseUser.reload().await()
                
                if (!firebaseUser.isEmailVerified) {
                    auth.signOut()
                    throw Exception("Tu cuenta no está verificada. Por favor, revisa tu correo electrónico para activarla.")
                }
                
                android.util.Log.d("FIREBASE_DEBUG", "Login Auth exitoso, UID: ${firebaseUser.uid}")
                findById(firebaseUser.uid)
            } else {
                android.util.Log.e("FIREBASE_DEBUG", "Usuario es nulo después de Auth")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_DEBUG", "Error en login Auth: ${e.message}")
            throw e // Re-lanzar para que el ViewModel lo capture
        }
    }

    override suspend fun loginWithGoogle(idToken: String): User? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: return null
        
        // Verificar si el perfil existe en Firestore, si no, crearlo
        val existingProfile = findById(firebaseUser.uid)
        if (existingProfile == null) {
            val newUser = User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Google User",
                email = firebaseUser.email ?: "",
                profilePictureUrl = firebaseUser.photoUrl?.toString() ?: ""
            )
            save(newUser)
            return newUser
        }
        return existingProfile
    }

    override suspend fun addPoints(userId: String, points: Int) {
        val user = findById(userId)
        user?.let {
            val updatedUser = it.copy(points = it.points + points)
            collection.document(userId).set(updatedUser).await()
        }
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    override suspend fun updateFcmToken(userId: String, token: String) {
        collection.document(userId).update("fcmToken", token).await()
    }

    override suspend fun softDeleteUser(userId: String) {
        collection.document(userId).update("isDeleted", true).await()
    }
}
