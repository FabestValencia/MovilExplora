package com.example.movilexplora.features.postdetail

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Comment(
    val id: String,
    val userName: String,
    val userAvatar: String,
    val date: String,
    val content: String
)

data class PostDetailState(
    val post: Post? = null,
    val description: String = "",
    val comments: List<Comment> = emptyList()
)

@HiltViewModel
class PostDetailViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(PostDetailState())
    val state: StateFlow<PostDetailState> = _state.asStateFlow()

    fun loadPostDetail(postId: String) {
        // En una app real, esto vendría de un repositorio
        val mockPost = Post(
            id = postId,
            title = "Blue Grotto Caves",
            location = "Capri, Italy",
            rating = 4.8,
            category = "Naturaleza",
            price = "Costo Moderado",
            status = PostStatus.VERIFICADO,
            imageUrl = ""
        )
        
        val mockDescription = "Experimente las fascinantes aguas azules de esta cueva marina natural. La luz del sol, al atravesar una cavidad submarina, crea un reflejo azul que ilumina la caverna. Ideal para nadar y realizar excursiones guiadas en barco."
        
        val mockComments = listOf(
            Comment("1", "Allison", "", "2 días", "El mejor momento para visitarlo es alrededor del mediodía, cuando el sol está justo encima. ¡El agua brilla intensamente!"),
            Comment("2", "Boyaco", "", "1 Semana", "Prepárate para esperar si vas en agosto. Hay mucha gente, pero vale la pena.")
        )
        
        _state.value = PostDetailState(mockPost, mockDescription, mockComments)
    }
}
