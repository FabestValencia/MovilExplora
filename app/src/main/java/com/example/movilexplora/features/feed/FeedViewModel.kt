package com.example.movilexplora.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.domain.model.Post
import com.example.movilexplora.domain.model.PostStatus
import com.example.movilexplora.domain.repository.PostRepository
import com.example.movilexplora.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.movilexplora.data.datastore.SessionDataStore
import kotlinx.coroutines.flow.firstOrNull
import com.example.movilexplora.features.filters.FilterState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map

data class Category(val name: String)

data class FeedState(
    val userName: String = "",
    val filterState: FilterState = FilterState(), // Añadimos state del filtro
    val searchQuery: String = "",
    val categories: List<Category> = listOf(
        Category("Gastronomia"),
        Category("Cultura"),
        Category("Naturaleza"),
        Category("Entretenimiento"),
        Category("Historia")
    )
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    val currentUserId: StateFlow<String> = sessionDataStore.sessionFlow
        .map { it?.userId ?: "guest" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "guest")

    // Variable para controlar la carga paginada (simulada o real según el repo)
    private val _pageSize = 10
    private val _loadedCount = MutableStateFlow(_pageSize)

    companion object {
        // TODO: Eliminar este mapa de ubicaciones quemadas y la lógica de cálculo de
        // distancia apenas se apliquen los mapas correctamente.
        // Se deben borrar los mapas `mockUserLocations` y `mockPostLocations`,
        // y las funciones `calculateDistanceKm`, `getMockLocationForUser` y `getMockLocationForPost`.
        // También se debe reemplazar el uso de `calcDistance` en `matchesDistance`
        // por la lógica final de ubicación del dispositivo real, sin dañar los filtros existentes.
        private val mockUserLocations = mutableMapOf<String, Pair<Double, Double>>()
        private val mockPostLocations = mutableMapOf<String, Pair<Double, Double>>()
    }

    private fun getMockLocationForUser(userId: String): Pair<Double, Double> {
        return mockUserLocations.getOrPut(userId) {
            // Genera lat/lon aleatorias (entre -0.1 y 0.1) para simular ubicaciones cerca de la distancia predeterminada.
            val lat = (Math.random() * 0.2) - 0.1
            val lon = (Math.random() * 0.2) - 0.1
            Pair(lat, lon)
        }
    }

    private fun getMockLocationForPost(postId: String, lat: Double, lon: Double): Pair<Double, Double> {
        if (lat != 0.0 || lon != 0.0) return Pair(lat, lon) // Si ya tiene una real, la usa
        return mockPostLocations.getOrPut(postId) {
            val randomLat = (Math.random() * 0.2) - 0.1
            val randomLon = (Math.random() * 0.2) - 0.1
            Pair(randomLat, randomLon)
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371.0 // Radio de la tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(java.lang.Math.sqrt(a), java.lang.Math.sqrt(1 - a))
        return (r * c).toFloat()
    }

    // Lista real del repo
    private val _allPosts: StateFlow<List<Post>> = postRepository.getPosts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Combine logs
    val posts: StateFlow<List<Post>> = combine(
        _allPosts,
        _state,
        currentUserId,
        _loadedCount
    ) { allPosts, currentState, userId, loadedCount ->
        val filters = currentState.filterState
        val query = currentState.searchQuery
        val userLocation = getMockLocationForUser(userId)

        val filteredList = allPosts.filter { post ->
            val matchesCategory = filters.selectedCategory == null || post.category == filters.selectedCategory
            val matchesPrice = filters.selectedPriceRange == 4 || (post.price.count { it == '$' } <= filters.selectedPriceRange)
            val postLocation = getMockLocationForPost(post.id, post.latitude, post.longitude)
            val calcDistance = calculateDistanceKm(userLocation.first, userLocation.second, postLocation.first, postLocation.second)
            val matchesDistance = calcDistance <= filters.distance
            val matchesSearch = query.isBlank() || post.title.contains(query, ignoreCase = true)

            // REGLA DE VISIBILIDAD: Verificados O creados por el usuario actual
            val matchesVisibility = post.status == PostStatus.VERIFICADO || post.creatorId == userId

            matchesCategory && matchesPrice && matchesDistance && matchesSearch && matchesVisibility
        }

        // Ordenar y limitar para el scroll
        filteredList.sortedWith(
            compareByDescending<Post> { it.creatorId == userId }
            .thenByDescending { it.status == PostStatus.VERIFICADO }
            .thenByDescending { it.likedBy.size }
        ).take(loadedCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            sessionDataStore.sessionFlow.collect { session ->
                val userId = session?.userId ?: "guest"
                if (userId.isNotBlank() && userId != "guest") {
                    val user = userRepository.findById(userId)
                    if (user != null) {
                        val firstName = user.name.split(" ").firstOrNull() ?: ""
                        _state.value = _state.value.copy(userName = firstName)
                    }
                }
            }
        }
    }

    fun toggleFavorite(postId: String) {
        viewModelScope.launch {
            val session = sessionDataStore.sessionFlow.firstOrNull()
            val currentUserId = session?.userId ?: "guest"
            postRepository.toggleFavorite(postId, currentUserId)
        }
    }

    // Filtros
    fun clearFilters() {
        _state.update { it.copy(filterState = FilterState()) }
    }

    fun applyFilters(newFilters: FilterState) {
        _state.update { it.copy(filterState = newFilters) }
    }

    fun toggleCategoryFilter(categoryName: String) {
        _state.update { currentState ->
            val currentFilters = currentState.filterState
            val newCategory = if (currentFilters.selectedCategory == categoryName) null else categoryName
            
            var count = 0
            if (currentFilters.distance != 50f) count++
            if (newCategory != null) count++
            if (currentFilters.selectedPriceRange != 4) count++
            
            currentState.copy(
                filterState = currentFilters.copy(
                    selectedCategory = newCategory,
                    filterCount = count
                )
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun loadMore() {
        _loadedCount.update { it + _pageSize }
    }

    suspend fun getCurrentUserId(): String {
        return sessionDataStore.sessionFlow.firstOrNull()?.userId ?: "guest"
    }
}
