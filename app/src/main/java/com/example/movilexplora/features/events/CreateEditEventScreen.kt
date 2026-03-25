package com.example.movilexplora.features.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.movilexplora.ui.theme.DarkBlue
import com.example.movilexplora.ui.theme.Turquoise
import com.example.movilexplora.ui.theme.GrayText

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
                        color = DarkBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkBlue)
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
                        contentDescription = "Añadir imagen",
                        tint = Turquoise,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Añadir imagen del evento", color = DarkBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(text = "Formato JPG o PNG, máx. 5MB", color = GrayText, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Form Fields
            EventInputField(
                label = "Título del evento",
                value = title,
                onValueChange = { title = it },
                placeholder = "Ej: Festival de Gastronomía"
            )

            EventInputField(
                label = "Descripción",
                value = description,
                onValueChange = { description = it },
                placeholder = "Describe los detalles del evento, actividades y más...",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            EventInputField(
                label = "Punto turístico",
                value = location,
                onValueChange = { location = it },
                placeholder = "Seleccionar lugar",
                trailingIcon = {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GrayText)
                },
                readOnly = true
            )

            EventInputField(
                label = "Categoría",
                value = category,
                onValueChange = { category = it },
                placeholder = "Seleccionar categoría",
                trailingIcon = {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, tint = GrayText)
                },
                readOnly = true
            )

            EventInputField(
                label = "Fecha de inicio",
                value = startDate,
                onValueChange = { startDate = it },
                placeholder = "mm/dd/yyyy, --:-- --",
                trailingIcon = {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GrayText, modifier = Modifier.size(20.dp))
                },
                readOnly = true
            )

            EventInputField(
                label = "Fecha de fin",
                value = endDate,
                onValueChange = { endDate = it },
                placeholder = "mm/dd/yyyy, --:-- --",
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
            color = DarkBlue,
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
