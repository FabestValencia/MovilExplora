package com.example.movilexplora.features.events

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movilexplora.features.createpost.CategorySelectableItem
import com.example.movilexplora.ui.theme.GrayText
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.getCategoryColor
import com.example.movilexplora.ui.theme.getCategoryIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEventScreen(
    eventId: String? = null,
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val isEditing = eventId != null && eventId != "{eventId}"

    LaunchedEffect(isEditing) {
        if (isEditing) {
            title = "Festival de Gastronomía"
            description = "Un evento para disfrutar de la mejor comida..."
            location = "Plaza Central"
            category = "Gastronomía"
            startDate = "12/15/2026, 10:00 AM"
            endDate = "12/15/2026, 06:00 PM"
        }
    }

    val backgroundColor = Color(0xFFF7F9FA) // Light greyish background

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar evento" else "Crear evento",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.createediteventscreen_back_14), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Image Placeholder with Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFEFFFFB), RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFF00FFD4).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .clickable { /* Select image */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = stringResource(R.string.createediteventscreen_a_adir_imagen_15),
                        tint = Turquoise,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(R.string.createediteventscreen_a_adir_imagen_del_evento_0), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(text = stringResource(R.string.createediteventscreen_formato_jpg_o_png__m_x__5mb_1), color = GrayText, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Form Fields
            EventInputField(
                label = stringResource(R.string.createediteventscreen_t_tulo_del_evento_6),
                value = title,
                onValueChange = { title = it },
                placeholder = stringResource(R.string.createediteventscreen_ej__festival_de_gastronom_a_10)
            )

            EventInputField(
                label = stringResource(R.string.createediteventscreen_descripci_n_7),
                value = description,
                onValueChange = { description = it },
                placeholder = stringResource(R.string.createediteventscreen_describe_los_detalles_del_even_11),
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            // Location Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.createediteventscreen_ubicaci_n_2), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(text = stringResource(R.string.createediteventscreen_pin_interactivo_3), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Turquoise)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                Text(stringResource(R.string.create_post_map_placeholder), modifier = Modifier.align(Alignment.Center))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category Selection
            Text(text = stringResource(R.string.createediteventscreen_categor_a_4), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = stringResource(R.string.createediteventscreen_elige_la_categor_a_que_mejor_d_5), fontSize = 12.sp, color = GrayText.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            data class CategoryData(val name: String, val desc: String)
            
            val categories = listOf(
                CategoryData("Gastronomía", "Festivales, catas, rutas..."),
                CategoryData("Cultura", "Exposiciones, ferias, talleres..."),
                CategoryData("Naturaleza", "Campamentos, caminatas, excursiones..."),
                CategoryData("Entretenimiento", "Conciertos, fiestas, shows..."),
                CategoryData("Historia", "Recorridos guiados, charlas...")
            )

            categories.forEach { cat ->
                CategorySelectableItem(
                    name = cat.name,
                    description = cat.desc,
                    icon = getCategoryIcon(cat.name),
                    iconColor = getCategoryColor(cat.name),
                    isSelected = category == cat.name,
                    onSelect = { category = cat.name }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            EventInputField(
                label = stringResource(R.string.createediteventscreen_fecha_de_inicio_8),
                value = startDate,
                onValueChange = { startDate = it },
                placeholder = stringResource(R.string.createediteventscreen_mm_dd_yyyy_12),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                },
                readOnly = true
            )

            EventInputField(
                label = stringResource(R.string.createediteventscreen_fecha_de_fin_9),
                value = endDate,
                onValueChange = { endDate = it },
                placeholder = stringResource(R.string.createediteventscreen_mm_dd_yyyy_12),
                trailingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                },
                readOnly = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSaveSuccess,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Outlined.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Guardar cambios" else "Publicar evento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EventInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = Color(0xFFA0AAB4)) },
            singleLine = singleLine,
            readOnly = readOnly,
            trailingIcon = trailingIcon,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = Turquoise,
                unfocusedBorderColor = Color(0xFFE2E8F0),
                disabledBorderColor = Color(0xFFE2E8F0),
            )
        )
    }
}
