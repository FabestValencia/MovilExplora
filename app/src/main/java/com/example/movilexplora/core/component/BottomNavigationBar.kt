package com.example.movilexplora.core.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.movilexplora.R
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Turquoise,
            selectedTextColor = Turquoise,
            indicatorColor = Color.Transparent,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.bottom_nav_home)) },
            label = { Text(stringResource(R.string.bottom_nav_home)) },
            selected = selectedItem == "Inicio",
            onClick = onHomeClick,
            colors = navColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Event, contentDescription = stringResource(R.string.bottom_nav_events)) },
            label = { Text(stringResource(R.string.bottom_nav_events)) },
            selected = selectedItem == "Eventos",
            onClick = onEventsClick,
            colors = navColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.AddCircle, contentDescription = stringResource(R.string.bottom_nav_create)) },
            label = { Text(stringResource(R.string.bottom_nav_create)) },
            selected = false,
            onClick = onCreateClick,
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.bottom_nav_alerts)) },
            label = { Text(stringResource(R.string.bottom_nav_alerts)) },
            selected = selectedItem == "Alertas",
            onClick = onAlertsClick,
            colors = navColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.bottom_nav_profile)) },
            label = { Text(stringResource(R.string.bottom_nav_profile)) },
            selected = selectedItem == "Perfil",
            onClick = onProfileClick,
            colors = navColors
        )
    }
}
