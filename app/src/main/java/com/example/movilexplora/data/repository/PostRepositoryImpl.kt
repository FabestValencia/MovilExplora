package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Comment
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.data.local.dao.CommentDao
import com.example.movilexplora.data.local.dao.LikeDao
import com.example.movilexplora.data.local.entity.CommentEntity
import com.example.movilexplora.data.local.entity.LikeEntity
import com.example.movilexplora.data.local.entity.toDomainModel
import com.example.movilexplora.data.local.entity.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val commentDao: CommentDao,
    private val likeDao: LikeDao
) : PostRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val _posts = MutableStateFlow(
        listOf(
            Post(
                "1", "Belcanto Experience", "Chiado, Lisbon", 4.9, 
                "Gastronomía", "$$$ € Caro", PostStatus.VERIFICADO, "https://example.com/food.jpg", emptySet(), 12f
            ),
            Post(
                "2", "Historic Old Town", "Lisbon, Portugal", 4.8, 
                "Historia", "$$ € Moderado", PostStatus.VERIFICADO, "https://example.com/town.jpg", emptySet(), 5f
            ),
            Post(
                "3", "Serra da Estrela", "Guarda, Portugal", 4.7, 
                "Naturaleza", "Gratis", PostStatus.VERIFICADO, "https://example.com/mountain.jpg", emptySet(), 30f
            ),
            Post(
                "4", "Cascada Oculta", "Antioquia, Colombia", 4.5,
                "Naturaleza", "Gratis", PostStatus.PENDIENTE, "", emptySet(), 45f
            )
        )
    )

    init {
        scope.launch {
            val initialComments = listOf(
                CommentEntity("1", "1", "Allison", "", "2 días", "El mejor momento para visitarlo es alrededor del mediodía, cuando el sol está justo encima. ¡El agua brilla intensamente!"),
                CommentEntity("2", "1", "Boyaco", "", "1 Semana", "Prepárate para esperar si vas en agosto. Hay mucha gente, pero vale la pena.")
            )
            commentDao.insertComments(initialComments)
        }
    }

    override fun getPosts(): Flow<List<Post>> = _posts.combine(likeDao.getAllPostLikes()) { posts, likes ->
        posts.map { post ->
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
        _posts.update { it + post }
    }
}
