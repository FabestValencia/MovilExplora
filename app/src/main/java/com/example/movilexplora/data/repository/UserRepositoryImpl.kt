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

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : UserRepository {

    private val collection = firestore.collection("users")
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        // Escuchar cambios en tiempo real
        collection.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _users.value = it.documents.mapNotNull { snap ->
                    snap.toObject(User::class.java)?.apply { id = snap.id }
                }
            }
        }

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
            val result = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            result.user?.uid ?: throw Exception("Error al crear usuario")
        } else {
            user.id.ifEmpty { collection.document().id }
        }

        val userCopy = user.copy(id = uid, password = null)
        collection.document(uid).set(userCopy).await()
    }

    override suspend fun findById(id: String): User? {
        val snapshot = collection.document(id).get().await()
        return snapshot.toObject(User::class.java)?.apply { this.id = snapshot.id }
    }

    override suspend fun login(email: String, password: String): User? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid ?: return null
        return findById(uid)
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
}
