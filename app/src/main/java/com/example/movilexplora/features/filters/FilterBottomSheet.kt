package com.example.movilexplora.features.filters

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.getTranslatedCategoryName
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    initialState: FilterState,
    onDismiss: () -> Unit,
    onApplyFilters: (FilterState) -> Unit
) {
    var state by remember { mutableStateOf(initialState) }

    fun updateCount(currentState: FilterState): FilterState {
        var count = 0
        if (currentState.distance != 50f) count++
        if (currentState.selectedCategory != null) count++
        if (currentState.selectedPriceRange != 4) count++
        return currentState.copy(filterCount = count)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtros",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { state = FilterState() }) {
                    Text(text = "Limpiar", color = Turquoise, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Distance Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Distancia", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = "${state.distance.roundToInt()} km", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            
            Slider(
                value = state.distance,
                onValueChange = { state = updateCount(state.copy(distance = it)) },
                valueRange = 1f..50f,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Turquoise,
                    inactiveTrackColor = Color.Black
                ),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "1 km", fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
                Text(text = "50 km", fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Section
            Text(text = "Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(
                text = "Seleccione la categoría que mejor describa el lugar.",
                fontSize = 12.sp,
                color = GrayText.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            val categories = listOf("Gastronomía", "Cultura", "Naturaleza", "Entretenimiento", "Historia")
            categories.forEach { category ->
                CategoryItem(
                    name = getTranslatedCategoryName(category),
                    isSelected = state.selectedCategory == category,
                    onSelect = {
                        val newSelection = if (state.selectedCategory == category) null else category
                        state = updateCount(state.copy(selectedCategory = newSelection))
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range Section
            Text(text = "Rango de Precios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    PriceRangeItem(
                        level = i,
                        isSelected = state.selectedPriceRange == i,
                        modifier = Modifier.weight(1f),
                        onSelect = {
                            state = updateCount(state.copy(selectedPriceRange = i))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Apply Button
            Button(
                onClick = { onApplyFilters(state) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Aplicar Filtros",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (state.filterCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Color.White.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.filterCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by surface
                colors = RadioButtonDefaults.colors(selectedColor = Turquoise, unselectedColor = Color.LightGray)
            )
        }
    }
}

@Composable
fun PriceRangeItem(
    level: Int,
    isSelected: Boolean,
    modifier: Modifier,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Turquoise else Color.LightGray.copy(alpha = 0.5f)
        ),
        color = if (isSelected) Turquoise.copy(alpha = 0.1f) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$".repeat(level),
                color = if (isSelected) Turquoise else GrayText.copy(alpha = 0.6f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
