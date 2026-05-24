package com.example.movilexplora.data.repository

import com.example.movilexplora.data.remote.ApiService
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Comment
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.local.dao.CommentDao
import com.example.movilexplora.data.local.dao.LikeDao
import com.example.movilexplora.data.local.dao.PostDao
import com.example.movilexplora.data.local.entity.CommentEntity
import com.example.movilexplora.data.local.entity.LikeEntity
import com.example.movilexplora.data.local.entity.toDomainModel
import com.example.movilexplora.data.local.entity.toEntity
import com.example.movilexplora.data.remote.model.PostRemote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import androidx.paging.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val likeDao: LikeDao,
    private val postDao: PostDao,
    private val firestore: FirebaseFirestore,
    private val sessionDataStore: com.example.movilexplora.data.datastore.SessionDataStore
) : PostRepository {
    private val collection = firestore.collection("posts")
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: kotlinx.coroutines.Job? = null
    private val activeListeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()

    init {
        // Regla de Negocio: Limpiar caché local al iniciar para evitar datos "quemados"
        // y asegurar una sincronización fresca desde Firestore.
        scope.launch {
            try {
                postDao.clearAll()
                android.util.Log.d("CACHE_CLEANUP", "Caché de publicaciones limpiada al iniciar")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Observar la sesión para ajustar los listeners en tiempo real
        sessionDataStore.sessionFlow.onEach { session ->
            setupRealtimeSync(session)
        }.launchIn(scope)
    }

    private fun setupRealtimeSync(session: com.example.movilexplora.data.model.UserSession?) {
        // Limpiar listeners previos
        activeListeners.forEach { it.remove() }
        activeListeners.clear()

        if (session == null) return

        val queries = mutableListOf<com.google.firebase.firestore.Query>()

        if (session.role == com.example.movilexplora.domain.model.enum.UserRole.ADMIN) {
            // Admin escucha to-do para moderación
            queries.add(collection)
        } else {
            // Usuario normal escucha:
            // 1. To-do lo verificado (de cualquier autor)
            queries.add(collection.whereEqualTo("status", PostStatus.VERIFICADO.name))
            // 2. Sus propias publicaciones (cualquier estado, para ver rechazadas/pendientes)
            queries.add(collection.whereEqualTo("creatorId", session.userId))
        }

        queries.forEach { query ->
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("SYNC_ERROR", "Error sincronizando posts: ${error.message}")
                    return@addSnapshotListener
                }
                
                snapshot?.documentChanges?.forEach { change ->
                    val post = change.document.toObject(Post::class.java).apply { id = change.document.id }
                    scope.launch {
                        when (change.type) {
                            com.google.firebase.firestore.DocumentChange.Type.ADDED,
                            com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                                postDao.insertPost(post.toEntity())
                                syncComments(post.id)
                            }
                            com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                                // Evitar borrar publicaciones propias si solo desaparecieron del filtro de "Verificados"
                                if (session.role == com.example.movilexplora.domain.model.enum.UserRole.ADMIN || post.creatorId != session.userId) {
                                    postDao.deletePost(post.id)
                                } else {
                                    // Si es nuestra, verificamos si realmente fue borrada de Firestore
                                    // o si simplemente cambió de estado (ej. rechazada)
                                    val exists = collection.document(post.id).get().await().exists()
                                    if (!exists) {
                                        postDao.deletePost(post.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            activeListeners.add(listener)
        }
    }

    private fun syncComments(postId: String) {
        collection.document(postId).collection("comments").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.documentChanges?.forEach { change ->
                val comment = change.document.toObject(Comment::class.java).apply { id = change.document.id }
                scope.launch {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED || 
                        change.type == com.google.firebase.firestore.DocumentChange.Type.MODIFIED) {
                        commentDao.insertComment(comment.toEntity())
                    }
                }
            }
        }
    }

    private fun PostRemote.toLocalEntity() = com.example.movilexplora.data.local.entity.PostEntity(
        id = id,
        title = title,
        location = location,
        rating = rating,
        category = category,
        price = price,
        status = status,
        imageUrl = imageUrl,
        description = description,
        latitude = latitude,
        longitude = longitude,
        distance = 0f,
        creatorId = creatorId
    )

    override fun getPosts(): Flow<List<Post>> = postDao.getAllPosts().combine(likeDao.getAllPostLikes()) { postEntities, likes ->
        postEntities.map { entity ->
            val post = entity.toDomainModel()
            val postLikes = likes.filter { it.itemId == post.id }.map { it.userId }
            post.copy(likedBy = postLikes)
        }.filter { !it.isDeleted }
    }

    override fun getPost(id: String): Flow<Post?> = getPosts().map { posts ->
        posts.find { it.id == id }
    }

    override fun getPagedPosts(
        category: String?, 
        priceLimit: Int, 
        searchQuery: String?,
        minLat: Double?,
        maxLat: Double?,
        minLon: Double?,
        maxLon: Double?
    ): Flow<androidx.paging.PagingData<Post>> {
        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { 
                postDao.getFilteredPostsPagingSource(
                    category, priceLimit, PostStatus.VERIFICADO.name, searchQuery,
                    minLat, maxLat, minLon, maxLon
                ) 
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override fun getComments(postId: String): Flow<List<Comment>> =
        commentDao.getCommentsByPostId(postId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun addComment(comment: Comment) {
        val commentRef = collection.document(comment.postId).collection("comments").document()
        val commentToSave = comment.copy(id = commentRef.id)
        commentRef.set(commentToSave).await()
    }

    override suspend fun toggleFavorite(postId: String, userId: String) {
        val isLiked = likeDao.isLiked(postId, userId, "POST") > 0
        if (isLiked) {
            likeDao.deleteLike(postId, userId, "POST")
        } else {
            likeDao.insertLike(LikeEntity(postId, userId, "POST"))
        }
    }

    override suspend fun addPost(post: Post) {
        val docRef = if (post.id.isEmpty()) collection.document() else collection.document(post.id)
        val postToSave = post.copy(id = docRef.id)
        docRef.set(postToSave).await()
    }

    override suspend fun updatePostStatus(postId: String, status: PostStatus, rejectionReason: String?) {
        val updates = mutableMapOf<String, Any>("status" to status.name)
        rejectionReason?.let { updates["rejectionReason"] = it }
        collection.document(postId).update(updates).await()
    }

    override suspend fun softDeletePost(postId: String) {
        collection.document(postId).update("isDeleted", true).await()
    }

    override suspend fun clearCache() {
        postDao.clearAll()
    }
}
