package com.example.movilexplora.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.repository.PostRepositoryImpl
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.usecase.GetPostsUseCase
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class MapMarker(
    val post: Post,
    val position: LatLng
)

data class MapState(
    val markers: List<MapMarker> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "Cercanos",
    val selectedPost: Post? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    val posts: StateFlow<List<Post>> = getPostsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Mocking coordinates for the posts
        val mockMarkers = listOf(
            MapMarker(
                Post("1", "Belcanto Experience", "Chiado, Lisbon", 4.9, "Gastronomía", "$$$ • Caro", PostStatus.VERIFICADO, ""),
                LatLng(41.3851, 2.1734) // Barcelona center
            ),
            MapMarker(
                Post("2", "Historic Old Town", "Lisbon, Portugal", 4.8, "Historia", "$$ • Moderado", PostStatus.VERIFICADO, ""),
                LatLng(41.3984, 2.1750) // Sagrada Familia area
            ),
            MapMarker(
                Post("3", "Serra da Estrela", "Guarda, Portugal", 4.7, "Naturaleza", "Gratis", PostStatus.VERIFICADO, ""),
                LatLng(41.3809, 2.1228) // Camp Nou area
            ),
            MapMarker(
                Post("4", "Mirador del Valle", "Toledo, España", 4.8, "Naturaleza", "Gratis", PostStatus.VERIFICADO, ""),
                LatLng(41.3750, 2.1550)
            )
        )
        _state.update { it.copy(markers = mockMarkers) }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onFilterSelected(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
    }

    fun onMarkerClick(post: Post) {
        _state.update { it.copy(selectedPost = post) }
    }

    fun onDismissPostDetail() {
        _state.update { it.copy(selectedPost = null) }
    }
}
