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
    private val firestore: FirebaseFirestore
) : PostRepository {
    private val collection = firestore.collection("posts")
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        // Sincronizar posts desde Firestore en tiempo real
        collection.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                scope.launch {
                    val posts = it.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)?.apply { id = doc.id }
                    }
                    // Actualizar caché local: limpiar y reinsertar para mantener sincronización exacta
                    postDao.clearAll()
                    postDao.insertPosts(posts.map { it.toEntity() })
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

    override fun getPagedPosts(category: String?, priceLimit: Int): Flow<androidx.paging.PagingData<Post>> {
        return androidx.paging.Pager(
            config = androidx.paging.PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { postDao.getFilteredPostsPagingSource(category, priceLimit, PostStatus.VERIFICADO.name) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override fun getComments(postId: String): Flow<List<Comment>> =
        commentDao.getCommentsByPostId(postId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun addComment(comment: Comment) {
        commentDao.insertComment(comment.toEntity())
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
}
