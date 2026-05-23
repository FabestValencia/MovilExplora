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

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)
    val userLocation: StateFlow<Pair<Double, Double>?> = _userLocation.asStateFlow()

    init {
        val nearbyFilter = resources.getString(R.string.filter_nearby)
        _state.update { it.copy(selectedFilter = nearbyFilter) }

        viewModelScope.launch {
            combine(
                postRepository.getPosts(),
                eventRepository.getEvents()
            ) { posts, events ->
                val verifiedPosts = posts.filter { it.status == com.example.movilexplora.domain.model.PostStatus.VERIFICADO }
                val features = verifiedPosts.map { MapFeature.PostFeature(it) } +
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

    private fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }

    private fun filterFeatures(
        features: List<MapFeature>,
        filter: String,
        query: String,
        userLoc: Pair<Double, Double>? = _userLocation.value
    ): List<MapFeature> {
        val nearbyLabel = resources.getString(R.string.filter_nearby)
        val inCityLabel = resources.getString(R.string.map_filter_in_city)
        
        val filtered = features.filter { feature ->
            val matchesFilter = when {
                filter == nearbyLabel || filter.isEmpty() -> {
                    if (filter == nearbyLabel && userLoc != null) {
                        val distance = calculateDistanceInKm(
                            userLoc.first, userLoc.second,
                            feature.latitude, feature.longitude
                        )
                        distance <= 50.0 // 50 km radius limit
                    } else {
                        true
                    }
                }
                filter == inCityLabel -> {
                    true // Show all posts and events in the database on the map
                }
                else -> {
                    feature.category.equals(filter, ignoreCase = true)
                }
            }
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                feature.title.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }

        return if (filter == nearbyLabel && userLoc != null) {
            filtered.sortedBy { feature ->
                calculateDistanceInKm(
                    userLoc.first, userLoc.second,
                    feature.latitude, feature.longitude
                )
            }
        } else {
            filtered
        }
    }

    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
        _state.update { 
            it.copy(
                filteredFeatures = filterFeatures(it.features, it.selectedFilter, it.searchQuery, Pair(latitude, longitude))
            )
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
