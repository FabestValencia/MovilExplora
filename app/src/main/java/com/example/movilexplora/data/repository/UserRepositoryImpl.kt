package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    firestore: FirebaseFirestore,
) : UserRepository {

    private val collection = firestore.collection("users")

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

    override suspend fun addPoints(userId: String, points: Int) {
        val user = findById(userId)
        user?.let {
            val updatedUser = it.copy(points = it.points + points)
            collection.document(userId).set(updatedUser).await()
        }
    }
}
