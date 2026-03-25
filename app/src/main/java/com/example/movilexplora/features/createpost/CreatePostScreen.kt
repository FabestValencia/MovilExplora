package com.example.movilexplora.features.createpost

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: CreatePostViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val publishResult by viewModel.publishResult.collectAsState()

    LaunchedEffect(publishResult) {
        if (publishResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            onPublishSuccess()
            viewModel.resetResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nueva publicación",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkBlue,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBlue)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Image Picker Placeholder
            Text(text = "Imagen del lugar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF7F8F9))
                    .clickable { /* Pick Image */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Turquoise, modifier = Modifier.size(40.dp))
                    Text(text = "Añadir foto", color = GrayText.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Field
            Text(text = "Título", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.title.value,
                onValueChange = { viewModel.title.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. Joyita por descubrir", color = GrayText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F8F9),
                    unfocusedContainerColor = Color(0xFFF7F8F9),
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Section
            Text(text = "Categoría", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Text(text = "Select the category that best fits the place.", fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            val categories = listOf(
                Triple("Gastronomía", "Restaurantes, cafés, comida callejera...", Icons.Default.Restaurant),
                Triple("Cultura", "Museos, galerías, monumentos...", Icons.Default.TheaterComedy),
                Triple("Naturaleza", "Parques, miradores, senderos...", Icons.Default.Landscape),
                Triple("Entretenimiento", "Bares, discotecas, teatros...", Icons.Default.MusicNote),
                Triple("Historia", "Sitios históricos, ruinas...", Icons.Default.History)
            )

            categories.forEach { (name, desc, icon) ->
                CategorySelectableItem(
                    name = name,
                    description = desc,
                    icon = icon,
                    isSelected = state.selectedCategory == name,
                    onSelect = { viewModel.selectCategory(name) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Field
            Text(text = "Descripción", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.description.onChange(it) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Comparte lo que hace que este lugar sea especial...", color = GrayText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF7F8F9),
                    unfocusedContainerColor = Color(0xFFF7F8F9),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range
            Text(text = "Price Range", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 1..4) {
                    PriceOption(
                        level = i,
                        isSelected = state.selectedPriceRange == i,
                        modifier = Modifier.weight(1f),
                        onSelect = { viewModel.selectPriceRange(i) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Ubicación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(text = "PIN INTERACTIVO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                Text("Mapa de México City", modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Horario Sugerido
            Text(text = "Horario Sugerido", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
            Spacer(modifier = Modifier.height(12.dp))
            
            val times = listOf(
                Pair("Mañana", Icons.Default.LightMode),
                Pair("Tarde", Icons.Default.WbSunny),
                Pair("Noche", Icons.Default.NightsStay),
                Pair("Todo el día", Icons.Default.Schedule)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeOption(times[0].first, times[0].second, state.selectedTime == times[0].first, Modifier.weight(1f)) { viewModel.selectTime(times[0].first) }
                    TimeOption(times[1].first, times[1].second, state.selectedTime == times[1].first, Modifier.weight(1f)) { viewModel.selectTime(times[1].first) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeOption(times[2].first, times[2].second, state.selectedTime == times[2].first, Modifier.weight(1f)) { viewModel.selectTime(times[2].first) }
                    TimeOption(times[3].first, times[3].second, state.selectedTime == times[3].first, Modifier.weight(1f)) { viewModel.selectTime(times[3].first) }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Publish Button
            Button(
                onClick = { viewModel.publish() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Publicar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CategorySelectableItem(
    name: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        color = if (isSelected) Turquoise.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(Color(0xFFE0E0E0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkBlue)
                Text(text = description, fontSize = 11.sp, color = GrayText.copy(alpha = 0.6f))
            }
            RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = Turquoise))
        }
    }
}

@Composable
fun PriceOption(level: Int, isSelected: Boolean, modifier: Modifier, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Turquoise else Color.LightGray.copy(alpha = 0.3f)),
        color = if (isSelected) Turquoise.copy(alpha = 0.15f) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "$".repeat(level),
                color = if (isSelected) Turquoise else GrayText.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeOption(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, modifier: Modifier, onSelect: () -> Unit) {
    Surface(
        onClick = onSelect,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Turquoise else Color.LightGray.copy(alpha = 0.3f)),
        color = if (isSelected) Turquoise.copy(alpha = 0.15f) else Color(0xFFF7F8F9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Turquoise else Turquoise.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 13.sp, color = if (isSelected) Turquoise else DarkBlue)
        }
    }
}
