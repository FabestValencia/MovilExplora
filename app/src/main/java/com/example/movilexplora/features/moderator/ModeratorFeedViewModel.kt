package com.example.movilexplora.features.moderator

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.domain.model.VerificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ModeratorFeedState(
    val items: List<VerificationItem> = emptyList(),
    val selectedFilter: String = "Todo",
    val counts: Map<String, Int> = mapOf("Todo" to 0, "Lugares" to 0, "Reseñas" to 0, "Eventos" to 0)
)

class ModeratorFeedViewModel : ViewModel() {
    private val _state = MutableStateFlow(ModeratorFeedState())
    val state: StateFlow<ModeratorFeedState> = _state.asStateFlow()
    
    // Store all items internally
    private var allItems: List<VerificationItem> = emptyList()

    init {
        // Dummy data initialization
        allItems = listOf(
            VerificationItem(
                id = "1",
                title = "Hidden Gem: Old Bookstore",
                author = "local_explorer_99",
                timeAgo = "2 hours ago",
                description = "A cozy spot tucked away in the Latin Quarter. They have rare editions and serve great coffee...",
                imageUrl = "",
                type = VerificationType.LOCATION,
                badgeText = "New Location"
            ),
            VerificationItem(
                id = "2",
                title = "Luigi's Trattoria",
                author = "foodie_traveler",
                timeAgo = "5 hours ago",
                description = "The carbonara here is authentic Roman style! No cream, just eggs and pecorino. Highly...",
                imageUrl = "",
                type = VerificationType.PHOTO,
                badgeText = "Photo Submission"
            ),
            VerificationItem(
                id = "3",
                title = "Tech Meetup 2024",
                author = "dev_community",
                timeAgo = "10 hours ago",
                description = "Evento anual de desarrolladores locales. Charlas sobre AI, Mobile y Web...",
                imageUrl = "",
                type = VerificationType.EVENT,
                badgeText = "New Event"
            ),
            VerificationItem(
                id = "4",
                title = "Concierto al Aire Libre",
                author = "music_lovers",
                timeAgo = "1 day ago",
                description = "Concierto benéfico en el parque central. Bandas locales e internacionales...",
                imageUrl = "",
                type = VerificationType.EVENT,
                badgeText = "New Event"
            )
        )
        refreshState()
    }
    
    private fun refreshState() {
        // Calculate counts
        val counts = mutableMapOf(
            "Todo" to allItems.size,
            "Lugares" to allItems.count { it.type == VerificationType.LOCATION },
            "Reseñas" to allItems.count { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }, // Assuming PHOTO counts as Review-like content or separate? Let's simplify
            "Eventos" to allItems.count { it.type == VerificationType.EVENT }
        )
        
        // Filter items based on selected filter
        val currentFilter = _state.value.selectedFilter
        val filteredItems = when (currentFilter) {
            "Lugares" -> allItems.filter { it.type == VerificationType.LOCATION }
            "Reseñas" -> allItems.filter { it.type == VerificationType.REVIEW || it.type == VerificationType.PHOTO }
            "Eventos" -> allItems.filter { it.type == VerificationType.EVENT }
            else -> allItems
        }
        
        _state.update { 
            it.copy(
                items = filteredItems,
                counts = counts
            )
        }
    }

    fun onFilterSelected(filter: String) {
        _state.update { it.copy(selectedFilter = filter) }
        refreshState()
    }

    fun verifyItem(itemId: String) {
        allItems = allItems.filter { it.id != itemId }
        refreshState()
    }

    fun approveItem(itemId: String) {
        allItems = allItems.filter { it.id != itemId }
        refreshState()
    }

    fun rejectItem(itemId: String) {
        allItems = allItems.filter { it.id != itemId }
        refreshState()
    }
}
