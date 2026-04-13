package com.example.movilexplora.data.repository

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val likeDao: LikeDao,
    private val postDao: PostDao
) : PostRepository {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            // Verificar si la base de datos está vacía para insertar datos iniciales
            val existingPosts = postDao.getAllPosts().first()
            if (existingPosts.isEmpty()) {
                val initialPosts = listOf(
                    Post(
                        "1", "Belcanto Experience", "Chiado, Lisbon", 4.9,
                        "Gastronomia", "$$$ € Caro", PostStatus.VERIFICADO, "https://example.com/food.jpg",
                        "Uno de los restaurantes más emblemáticos de Lisboa, ofreciendo una alta cocina portuguesa contemporánea en un ambiente sofisticado.",
                        38.7104, -9.1419, emptySet(), 12f, "1"
                    ),
                    Post(
                        "2", "Historic Old Town", "Lisbon, Portugal", 4.8,
                        "Historia", "$$ € Moderado", PostStatus.VERIFICADO, "https://example.com/town.jpg",
                        "El centro histórico de Lisboa, lleno de calles estrechas, fado y una arquitectura impresionante que cuenta siglos de historia.",
                        38.710, -9.130, emptySet(), 5f, "1"
                    ),
                    Post(
                        "3", "Serra da Estrela", "Guarda, Portugal", 4.7,
                        "Naturaleza", "Gratis", PostStatus.VERIFICADO, "https://example.com/mountain.jpg",
                        "El punto más alto de Portugal continental, perfecto para disfrutar de paisajes nevados en invierno y senderismo en verano.",
                        40.322, -7.591, emptySet(), 30f, "2"
                    ),
                    Post(
                        "4", "Cascada Oculta", "Antioquia, Colombia", 4.5,
                        "Naturaleza", "Gratis", PostStatus.PENDIENTE, "",
                        "Una impresionante caída de agua escondida en la selva antioqueña, accesible tras una caminata de 2 horas por senderos naturales.",
                        6.244, -75.581, emptySet(), 45f, "1"
                    )
                )
                postDao.insertPosts(initialPosts.map { it.toEntity() })
            }

            val initialCommentsItems = listOf(
                CommentEntity("1", "1", "Allison", "", "2 días", "El mejor momento para visitarlo es alrededor del mediodía, cuando el sol está justo encima. ¡El agua brilla intensamente!"),
                CommentEntity("2", "1", "Boyaco", "", "1 Semana", "Prepárate para esperar si vas en agosto. Hay mucha gente, pero vale la pena.")
            )
            commentDao.insertComments(initialCommentsItems)
        }
    }

    override fun getPosts(): Flow<List<Post>> = postDao.getAllPosts().combine(likeDao.getAllPostLikes()) { postEntities, likes ->
        postEntities.map { entity ->
            val post = entity.toDomainModel()
            val postLikes = likes.filter { it.itemId == post.id }.map { it.userId }.toSet()
            post.copy(likedBy = postLikes)
        }
    }

    override fun getPost(id: String): Flow<Post?> = getPosts().map { posts ->
        posts.find { it.id == id }
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
        postDao.insertPost(post.toEntity())
    }

    override suspend fun updatePostStatus(postId: String, status: PostStatus, rejectionReason: String?) {
        if (rejectionReason != null) {
            postDao.updatePostStatusWithReason(postId, status.name, rejectionReason)
        } else {
            postDao.updatePostStatus(postId, status.name)
        }
    }
}
