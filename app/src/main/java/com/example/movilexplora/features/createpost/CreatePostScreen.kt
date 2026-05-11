package com.example.movilexplora.features.createpost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.movilexplora.R
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPublishSuccess: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val publishResult by viewModel.publishResult.collectAsState()
    
    // Estado para la imagen seleccionada localmente
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Estado para el Pop-up de éxito
    var showSuccessDialog by remember { mutableStateOf(false) }
    var publishedTitle by remember { mutableStateOf("") }

    // Lanzador para la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        // Opcional: pasar el URI al viewModel si este lo requiere para subirlo
    }

    LaunchedEffect(publishResult) {
        if (publishResult is com.example.movilexplora.core.utils.RequestResult.Success) {
            publishedTitle = viewModel.title.value
            showSuccessDialog = true
            viewModel.resetResult()
        }
    }

    // Pop-up de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar fuera */ },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPublishSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Text("Entendido")
                }
            },
            title = {
                Text(text = "¡Publicación Creada!", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Tu publicación \"$publishedTitle\" ha sido creada exitosamente y pronto estará disponible para la comunidad.")
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.createpostscreen_nueva_publicaci_n_0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.createpostscreen_back_12), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Image Picker
            Text(text = stringResource(R.string.createpostscreen_imagen_del_lugar_1), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Icono para indicar que se puede cambiar
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp).size(16.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Turquoise, modifier = Modifier.size(40.dp))
                        Text(text = stringResource(R.string.createpostscreen_a_adir_foto_2), color = GrayText.copy(alpha = 0.5f), fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title Field
            Text(text = stringResource(R.string.createpostscreen_t_tulo_3), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.title.value,
                onValueChange = { viewModel.title.onChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.create_post_placeholder_title), color = GrayText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Section
            Text(text = stringResource(R.string.createpostscreen_categor_a_4), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = stringResource(R.string.createpostscreen_select_the_category_that_best_5), fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            data class CategoryData(val key: String, val name: String, val desc: String)
            
            val categories = listOf(
                CategoryData("Gastronomia", stringResource(R.string.create_post_cat_gastronomy), stringResource(R.string.create_post_cat_gastronomy_desc)),
                CategoryData("Cultura", stringResource(R.string.create_post_cat_culture), stringResource(R.string.create_post_cat_culture_desc)),
                CategoryData("Naturaleza", stringResource(R.string.create_post_cat_nature), stringResource(R.string.create_post_cat_nature_desc)),
                CategoryData("Entretenimiento", stringResource(R.string.create_post_cat_entertainment), stringResource(R.string.create_post_cat_entertainment_desc)),
                CategoryData("Historia", stringResource(R.string.create_post_cat_history), stringResource(R.string.create_post_cat_history_desc))
            )

            categories.forEach { category ->
                CategorySelectableItem(
                    name = category.name,
                    description = category.desc,
                    icon = getCategoryIcon(category.key),
                    iconColor = getCategoryColor(category.key),
                    isSelected = state.selectedCategory == category.key,
                    onSelect = { viewModel.selectCategory(category.key) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Description Field
            Text(text = stringResource(R.string.createpostscreen_descripci_n_6), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.description.value,
                onValueChange = { viewModel.description.onChange(it) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text(stringResource(R.string.create_post_placeholder_desc), color = GrayText.copy(alpha = 0.4f)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Price Range
            Text(text = stringResource(R.string.createpostscreen_price_range_7), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
                Text(text = stringResource(R.string.createpostscreen_ubicaci_n_8), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = stringResource(R.string.createpostscreen_pin_interactivo_9), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(stringResource(R.string.create_post_map_placeholder), modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Horario Sugerido
            Text(text = stringResource(R.string.createpostscreen_horario_sugerido_10), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(12.dp))
            
            data class TimeOptionData(val key: String, val text: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)
            val times = listOf(
                TimeOptionData("Mañana", stringResource(R.string.create_post_time_morning), Icons.Default.LightMode),
                TimeOptionData("Tarde", stringResource(R.string.create_post_time_afternoon), Icons.Default.WbSunny),
                TimeOptionData("Noche", stringResource(R.string.create_post_time_night), Icons.Default.NightsStay),
                TimeOptionData("Todo el día", stringResource(R.string.create_post_time_allday), Icons.Default.Schedule)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeOption(times[0].text, times[0].icon, state.selectedTime == times[0].key, Modifier.weight(1f)) { viewModel.selectTime(times[0].key) }
                    TimeOption(times[1].text, times[1].icon, state.selectedTime == times[1].key, Modifier.weight(1f)) { viewModel.selectTime(times[1].key) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeOption(times[2].text, times[2].icon, state.selectedTime == times[2].key, Modifier.weight(1f)) { viewModel.selectTime(times[2].key) }
                    TimeOption(times[3].text, times[3].icon, state.selectedTime == times[3].key, Modifier.weight(1f)) { viewModel.selectTime(times[3].key) }
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
                Text(text = stringResource(R.string.createpostscreen_publicar_11), fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
    iconColor: Color,
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
                modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
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
        color = if (isSelected) Turquoise.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) Turquoise else Turquoise.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 13.sp, color = if (isSelected) Turquoise else MaterialTheme.colorScheme.primary)
        }
    }
}
