package com.example.movilexplora.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.data.repository.PostRepositoryImpl
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
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
    val posts: List<Post> = emptyList(),
    val markers: List<MapMarker> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "",
    val selectedPost: Post? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        _state.update { 
            it.copy(
                selectedFilter = resources.getString(R.string.filter_nearby),
                posts = listOf(
                    Post("1", "Belcanto Experience", "Chiado, Lisbon", 4.9, resources.getString(R.string.create_post_cat_gastronomy), "$$$ • " + resources.getString(R.string.price_expensive), PostStatus.VERIFICADO, ""),
                    Post("2", "Historic Old Town", "Lisbon, Portugal", 4.8, resources.getString(R.string.create_post_cat_history), "$$ • " + resources.getString(R.string.price_moderate), PostStatus.VERIFICADO, ""),
                    Post("3", "Serra da Estrela", "Guarda, Portugal", 4.7, resources.getString(R.string.create_post_cat_nature), resources.getString(R.string.price_free), PostStatus.VERIFICADO, ""),
                    Post("4", "Mirador del Valle", "Toledo, España", 4.8, resources.getString(R.string.create_post_cat_nature), resources.getString(R.string.price_free), PostStatus.VERIFICADO, "")
                )
            )
        }

        // Mocking coordinates for the posts
        val mockMarkers = listOf(
            MapMarker(
                Post("1", "Belcanto Experience", "Chiado, Lisbon", 4.9, resources.getString(R.string.create_post_cat_gastronomy), "$$$ • " + resources.getString(R.string.price_expensive), PostStatus.VERIFICADO, ""),
                LatLng(41.3851, 2.1734) // Barcelona center
            ),
            MapMarker(
                Post("2", "Historic Old Town", "Lisbon, Portugal", 4.8, resources.getString(R.string.create_post_cat_history), "$$ • " + resources.getString(R.string.price_moderate), PostStatus.VERIFICADO, ""),
                LatLng(41.3984, 2.1750) // Sagrada Familia area
            ),
            MapMarker(
                Post("3", "Serra da Estrela", "Guarda, Portugal", 4.7, resources.getString(R.string.create_post_cat_nature), resources.getString(R.string.price_free), PostStatus.VERIFICADO, ""),
                LatLng(41.3809, 2.1228) // Camp Nou area
            ),
            MapMarker(
                Post("4", "Mirador del Valle", "Toledo, España", 4.8, resources.getString(R.string.create_post_cat_nature), resources.getString(R.string.price_free), PostStatus.VERIFICADO, ""),
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
