package com.example.movilexplora.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.movilexplora.ui.theme.Turquoise

@Composable
fun BottomNavigationBar(
    onCreateClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    selectedItem: String = "Inicio"
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            selected = selectedItem == "Inicio",
            onClick = onHomeClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Event, contentDescription = "Eventos") },
            label = { Text("Eventos") },
            selected = selectedItem == "Eventos",
            onClick = onEventsClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Crear") },
            label = { Text("Crear") },
            selected = false,
            onClick = onCreateClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Alertas") },
            label = { Text("Alertas") },
            selected = selectedItem == "Alertas",
            onClick = onAlertsClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise, indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            selected = selectedItem == "Perfil",
            onClick = onProfileClick,
            colors = NavigationBarItemDefaults.colors(selectedIconColor = Turquoise, selectedTextColor = Turquoise, indicatorColor = Color.Transparent)
        )
    }
}
