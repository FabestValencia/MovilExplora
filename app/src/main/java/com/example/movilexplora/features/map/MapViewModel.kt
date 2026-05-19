package com.example.movilexplora.features.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.Event
import com.example.movilexplora.domain.repository.EventRepository
import com.example.movilexplora.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class MapFeature {
    abstract val id: String
    abstract val title: String
    abstract val latitude: Double
    abstract val longitude: Double
    abstract val category: String

    data class PostFeature(val post: Post) : MapFeature() {
        override val id = post.id
        override val title = post.title
        override val latitude = post.latitude
        override val longitude = post.longitude
        override val category = post.category
    }

    data class EventFeature(val event: Event) : MapFeature() {
        override val id = event.id
        override val title = event.title
        override val latitude = event.latitude
        override val longitude = event.longitude
        override val category = event.category
    }
}

data class MapState(
    val features: List<MapFeature> = emptyList(),
    val filteredFeatures: List<MapFeature> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "",
    val selectedFeature: MapFeature? = null
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val eventRepository: EventRepository,
    private val resources: ResourceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        val nearbyFilter = resources.getString(R.string.filter_nearby)
        _state.update { it.copy(selectedFilter = nearbyFilter) }

        viewModelScope.launch {
            combine(
                postRepository.getPosts(),
                eventRepository.getEvents()
            ) { posts, events ->
                val features = posts.map { MapFeature.PostFeature(it) } +
                              events.map { MapFeature.EventFeature(it) }
                features
            }.collect { features ->
                _state.update { 
                    it.copy(
                        features = features,
                        filteredFeatures = filterFeatures(features, it.selectedFilter, it.searchQuery)
                    )
                }
            }
        }
    }

    private fun filterFeatures(features: List<MapFeature>, filter: String, query: String): List<MapFeature> {
        return features.filter { feature ->
            val matchesFilter = if (filter == resources.getString(R.string.filter_nearby) || filter.isEmpty()) {
                true
            } else {
                feature.category.equals(filter, ignoreCase = true)
            }
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                feature.title.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { 
            it.copy(
                searchQuery = query,
                filteredFeatures = filterFeatures(it.features, it.selectedFilter, query)
            )
        }
    }

    fun onFilterSelected(filter: String) {
        _state.update { 
            it.copy(
                selectedFilter = filter,
                filteredFeatures = filterFeatures(it.features, filter, it.searchQuery)
            )
        }
    }

    fun onFeatureClick(feature: MapFeature) {
        _state.update { it.copy(selectedFeature = feature) }
    }

    fun onDismissDetail() {
        _state.update { it.copy(selectedFeature = null) }
    }
}
