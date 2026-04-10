package com.example.movilexplora.data.repository

import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.model.Comment
import com.example.movilexplora.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor() : PostRepository {
    private val _posts = MutableStateFlow(
        listOf(
            Post(
                "1", "Belcanto Experience", "Chiado, Lisbon", 4.9, 
                "Gastronomía", "$$$ • Caro", PostStatus.VERIFICADO, "https://example.com/food.jpg"
            ),
            Post(
                "2", "Historic Old Town", "Lisbon, Portugal", 4.8, 
                "Historia", "$$ • Moderado", PostStatus.VERIFICADO, "https://example.com/town.jpg"
            ),
            Post(
                "3", "Serra da Estrela", "Guarda, Portugal", 4.7, 
                "Naturaleza", "Gratis", PostStatus.VERIFICADO, "https://example.com/mountain.jpg"
            ),
            Post(
                "4", "Cascada Oculta", "Antioquia, Colombia", 4.5,
                "Naturaleza", "Gratis", PostStatus.PENDIENTE, ""
            )
        )
    )

    private val _comments = MutableStateFlow<List<Comment>>(
        listOf(
            Comment("1", "1", "Allison", "", "2 días", "El mejor momento para visitarlo es alrededor del mediodía, cuando el sol está justo encima. ¡El agua brilla intensamente!"),
            Comment("2", "1", "Boyaco", "", "1 Semana", "Prepárate para esperar si vas en agosto. Hay mucha gente, pero vale la pena.")
        )
    )

    override fun getPosts(): Flow<List<Post>> = _posts.asStateFlow()

    override fun getPost(id: String): Flow<Post?> = _posts.map { posts ->
        posts.find { it.id == id }
    }

    override fun getComments(postId: String): Flow<List<Comment>> = _comments.map { comments ->
        comments.filter { it.postId == postId }
    }

    override suspend fun addComment(comment: Comment) {
        _comments.update { it + comment }
    }

    override suspend fun toggleFavorite(postId: String) {
        _posts.update { posts ->
            posts.map {
                if (it.id == postId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }
    }
}
