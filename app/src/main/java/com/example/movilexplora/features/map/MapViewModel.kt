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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

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
            it.copy(selectedFilter = resources.getString(R.string.filter_nearby))
        }

        viewModelScope.launch {
            postRepository.getPosts().collect { postsList ->
                val markers = postsList.map { post ->
                    MapMarker(
                        post = post,
                        position = LatLng(post.latitude, post.longitude)
                    )
                }
                _state.update { it.copy(posts = postsList, markers = markers) }
            }
        }
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
