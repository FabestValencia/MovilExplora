package com.example.movilexplora.data.repository

import com.example.movilexplora.data.local.dao.UserDao
import com.example.movilexplora.data.local.entity.toDomainModel
import com.example.movilexplora.data.local.entity.toEntity
import com.example.movilexplora.domain.model.User
import com.example.movilexplora.domain.model.UserRole
import com.example.movilexplora.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override val users: StateFlow<List<User>> = userDao.getAllUsers()
        .map { entities -> entities.map { it.toDomainModel() } }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Populate initial mock users if DB is empty
        coroutineScope.launch {
            if (userDao.getUserById("1") == null) {
                fetchUsers().forEach { userDao.insertUser(it.toEntity()) }
            }
        }
    }

    override suspend fun save(user: User) {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun findById(id: String): User? {
        return userDao.getUserById(id)?.toDomainModel()
    }

    override suspend fun login(email: String, password: String): User? {
        val userEntity = userDao.login(email, password)
        return userEntity?.toDomainModel()
    }

    override suspend fun addPoints(userId: String, points: Int) {
        userDao.addPoints(userId, points)
    }

    private fun fetchUsers(): List<User> {
        return listOf(
            User(
                id = "1",
                name = "Juan",
                city = "Ciudad 1",
                address = "Calle 123",
                email = "juan@email.com",
                password = "111111",
                profilePictureUrl = "https://m.media-amazon.com/images/I/41g6jROgo0L.png"
            ),
            User(
                id = "2",
                name = "Maria",
                city = "Pereira",
                address = "Calle 456",
                email = "maria@email.com",
                password = "222222",
                profilePictureUrl = "https://picsum.photos/200?random=2"
            ),
            User(
                id = "3",
                name = "Carlos",
                city = "Armenia",
                address = "Calle 789",
                email = "carlos@email.com",
                password = "123456", // Change to match original login! 
                profilePictureUrl = "https://picsum.photos/200?random=3",
                role = UserRole.ADMIN
            )
        )
    }
}
