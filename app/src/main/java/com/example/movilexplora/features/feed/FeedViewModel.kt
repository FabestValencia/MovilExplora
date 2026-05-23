package com.example.movilexplora.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilexplora.R
import com.example.movilexplora.core.utils.ResourceProvider
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
import kotlinx.coroutines.flow.flatMapLatest
import androidx.paging.cachedIn
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Category(val name: String)

data class FeedState(
    val userName: String = "",
    val userProfilePictureUrl: String? = null,
    val filterState: FilterState = FilterState(), // Añadimos state del filtro
    val searchQuery: String = "",
    val categories: List<Category> = emptyList()
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionDataStore: SessionDataStore,
    private val userRepository: UserRepository,
    private val resources: ResourceProvider
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
        // Mock locations removed
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371.0 // Radio de la tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return (r * c).toFloat()
    }

    // Lista real del repo
    private val _allPosts: StateFlow<List<Post>> = postRepository.getPosts()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _userLocation = MutableStateFlow<Pair<Double, Double>?>(null)

    // Variable para controlar la carga paginada real
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val pagedPosts: Flow<PagingData<Post>> = combine(_state, _userLocation) { currentState, userLoc ->
        Pair(currentState, userLoc)
    }.flatMapLatest { (currentState, userLoc) ->
        val filters = currentState.filterState
        
        // Calcular Bounding Box si hay ubicación y filtro de distancia
        var minLat: Double? = null
        var maxLat: Double? = null
        var minLon: Double? = null
        var maxLon: Double? = null
        
        if (userLoc != null && filters.distance < 60f) {
            val latRange = filters.distance / 111.0 // 1 degree lat is ~111km
            val lonRange = filters.distance / (111.0 * cos(Math.toRadians(userLoc.first)))
            
            minLat = userLoc.first - latRange
            maxLat = userLoc.first + latRange
            minLon = userLoc.second - lonRange
            maxLon = userLoc.second + lonRange
        }

        postRepository.getPagedPosts(
            category = filters.selectedCategory,
            priceLimit = filters.selectedPriceRange,
            searchQuery = currentState.searchQuery,
            minLat = minLat,
            maxLat = maxLat,
            minLon = minLon,
            maxLon = maxLon
        ).cachedIn(viewModelScope)
    }

    // Combine logs
    val posts: StateFlow<List<Post>> = combine(
        _allPosts,
        _state,
        currentUserId,
        _userLocation,
        _loadedCount
    ) { allPosts, currentState, _, userLoc, loadedCount ->
        val filters = currentState.filterState
        val query = currentState.searchQuery

        val filteredList = allPosts.filter { post ->
            val matchesCategory = filters.selectedCategory == null || post.category == filters.selectedCategory
            val matchesPrice = filters.selectedPriceRange == 4 || (post.price.count { it == '$' } <= filters.selectedPriceRange)
            
            // Si el usuario no dio permiso de ubicación (userLoc == null), ignoramos el filtro de distancia
            val matchesDistance = if (userLoc != null) {
                val postLocation = Pair(post.latitude, post.longitude)
                val calcDistance = calculateDistanceKm(userLoc.first, userLoc.second, postLocation.first, postLocation.second)
                calcDistance <= filters.distance
            } else {
                true // No hay ubicación -> no filtramos por distancia, mostramos todo lo verificado
            }
            
            val matchesSearch = query.isBlank() || post.title.contains(query, ignoreCase = true)

            // REGLA DE VISIBILIDAD: Solo los verificados aparecen en el Feed
            val matchesVisibility = post.status == PostStatus.VERIFICADO

            matchesCategory && matchesPrice && matchesDistance && matchesSearch && matchesVisibility
        }

        // Ordenar por popularidad (likes) o por ID (recientes) si no hay ubicación
        if (userLoc != null) {
            filteredList.sortedByDescending { it.likedBy.size }.take(loadedCount)
        } else {
            filteredList.sortedByDescending { it.id }.take(loadedCount)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun updateUserLocation(lat: Double, lon: Double) {
        _userLocation.value = Pair(lat, lon)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeCurrentUser() {
        viewModelScope.launch {
            sessionDataStore.sessionFlow.flatMapLatest { session ->
                val userId = session?.userId
                if (userId != null && userId != "guest") {
                    userRepository.observeUser(userId)
                } else {
                    kotlinx.coroutines.flow.flowOf(null)
                }
            }.collect { user ->
                if (user != null) {
                    val firstName = user.name.split(" ").firstOrNull() ?: ""
                    _state.update { it.copy(
                        userName = firstName,
                        userProfilePictureUrl = user.profilePictureUrl
                    ) }
                }
            }
        }
    }

    init {
        _state.update { 
            it.copy(
                categories = listOf(
                    Category(resources.getString(R.string.create_post_cat_gastronomy)),
                    Category(resources.getString(R.string.create_post_cat_culture)),
                    Category(resources.getString(R.string.create_post_cat_nature)),
                    Category(resources.getString(R.string.create_post_cat_entertainment)),
                    Category(resources.getString(R.string.create_post_cat_history))
                )
            )
        }

        observeCurrentUser()
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

}
