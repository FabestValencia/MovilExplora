package com.example.movilexplora.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.movilexplora.R

@Composable
fun getTranslatedCategoryName(categoryKey: String): String {
    return when (categoryKey.lowercase().replace("í", "i")) {
        "gastronomia" -> stringResource(R.string.create_post_cat_gastronomy)
        "cultura" -> stringResource(R.string.create_post_cat_culture)
        "naturaleza" -> stringResource(R.string.create_post_cat_nature)
        "entretenimiento" -> stringResource(R.string.create_post_cat_entertainment)
        "historia" -> stringResource(R.string.create_post_cat_history)
        else -> categoryKey
    }
}

@Composable
fun getTranslatedBadgeName(badgeKey: String): String {
    return when (badgeKey) {
        "Primera Publicación" -> stringResource(R.string.badge_first_post_title)
        "10 Publicaciones" -> stringResource(R.string.badge_10_posts_title)
        "Maestro del Mapa" -> stringResource(R.string.badge_map_master_title)
        "Explorador del Mes" -> stringResource(R.string.badge_explorer_month_title)
        "Guía Local" -> stringResource(R.string.badge_local_guide_title)
        else -> badgeKey
    }
}

@Composable
fun getTranslatedBadgeDescription(badgeDesc: String): String {
    return when (badgeDesc) {
        "¡Tu primera aventura compartida!" -> stringResource(R.string.badge_first_post_desc)
        "Comunidad confiable y activa" -> stringResource(R.string.badge_10_posts_desc)
        "Experto en navegación local" -> stringResource(R.string.badge_map_master_desc)
        "Sé el más activo este mes" -> stringResource(R.string.badge_explorer_month_desc)
        "Ayuda a otros viajeros" -> stringResource(R.string.badge_local_guide_desc)
        else -> badgeDesc
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun MovilExploraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
