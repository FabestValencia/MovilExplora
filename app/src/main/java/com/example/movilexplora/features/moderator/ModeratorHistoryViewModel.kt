package com.example.movilexplora.features.moderator

import androidx.lifecycle.ViewModel
import com.example.movilexplora.domain.model.VerificationItem
import com.example.movilexplora.domain.model.VerificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ModeratorHistoryState(
    val items: List<HistoryItem> = emptyList(),
    val selectedFilter: String = "Todo",
    val counts: Map<String, Int> = mapOf("Todo" to 0, "Aceptados" to 0, "Rechazados" to 0)
)

data class HistoryItem(
    val id: String,
    val type: VerificationType,
    val badgeText: String,
    val title: String,
    val author: String,
    val timeAgo: String,
    val description: String,
    val status: String // "Aceptado" or "Rechazado"
)

@HiltViewModel
class ModeratorHistoryViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ModeratorHistoryState())
    val state: StateFlow<ModeratorHistoryState> = _state.asStateFlow()

    private var allItems = listOf<HistoryItem>()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        // Mock data for history to show traceability of admin actions
        allItems = listOf(
            HistoryItem(
                id = "1",
                type = VerificationType.EVENT,
                badgeText = "EVENTO",
                title = "Rally Deportivo de Ciudad 1",
                author = "DeportesCiudad1",
                timeAgo = "1 d",
                description = "Gran recorrido atlético por el parque central de la ciudad.",
                status = "Aceptado"
            ),
            HistoryItem(
                id = "2",
                type = VerificationType.LOCATION,
                badgeText = "NUEVO LUGAR",
                title = "Restaurante La Casona",
                author = "Juan Cocina",
                timeAgo = "3 d",
                description = "Falta claridad en la dirección. Imágenes un poco borrosas.",
                status = "Rechazado"
            ),
            HistoryItem(
                id = "3",
                type = VerificationType.PHOTO,
                badgeText = "FOTOGRAFÍA",
                title = "Atardecer en el Río",
                author = "FedeFotos",
                timeAgo = "1 w",
                description = "Hermosa postal de nuestro río durante el atardecer.",
                status = "Aceptado"
            )
        )

        updateState()
    }

    fun onFilterSelected(filter: String) {
        _state.value = _state.value.copy(selectedFilter = filter)
        updateState(filter)
    }

    private fun updateState(filter: String = _state.value.selectedFilter) {
        val filtered = if (filter == "Todo") {
            allItems
        } else {
            allItems.filter { it.status + "s" == filter || it.status == filter }
        }

        val counts = mapOf(
            "Todo" to allItems.size,
            "Aceptados" to allItems.count { it.status == "Aceptado" },
            "Rechazados" to allItems.count { it.status == "Rechazado" }
        )

        _state.value = _state.value.copy(
            items = filtered,
            counts = counts
        )
    }
}

