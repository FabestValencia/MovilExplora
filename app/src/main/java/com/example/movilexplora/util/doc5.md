# Navegación en Jetpack Compose (Parte 2)

## Introducción

La navegación es un aspecto crucial en el desarrollo de aplicaciones móviles, ya que permite a los usuarios moverse entre diferentes pantallas y funcionalidades de la aplicación. En Jetpack Compose, la navegación se maneja a través de la biblioteca **Navigation Compose**, que facilita la creación y gestión de rutas dentro de una aplicación.

En la sesión anterior, se aprendió a navegar entre pantallas cuando el usuario interactúa con botones. En esta guía, nos enfocaremos en composables que permiten organizar mejor la navegación, como `NavigationBar`, `NavigationDrawer` y `Tabs`.

---

## Tabs (Pestañas)

Las pestañas (**Tabs**) son una forma común de organizar el contenido en una aplicación. Para crear pestañas, podemos usar el componente `TabRow` junto con `Tab`. A continuación, se muestra un ejemplo básico de cómo implementar pestañas en una aplicación:

```kotlin
@Composable
fun TabsExample() {
    // Definir las pestañas
    val tabs = listOf("Home", "Profile", "Settings")
    // Estado para la pestaña seleccionada
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        // Crear la fila de pestañas
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                // Crear cada pestaña con su título y acción de selección
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        // Cargar el contenido correspondiente a la pestaña seleccionada
        when (selectedTabIndex) {
            0 -> HomeScreen()
            1 -> ProfileScreen()
            2 -> SettingsScreen()
        }
    }
}
```

---

## Navigation Bar (Barra de Navegación)

La barra de navegación (**Navigation Bar**) es una forma de proporcionar acceso rápido a las principales secciones de una aplicación. Podemos usar el componente `NavigationBar` para crear una barra de navegación en la parte inferior de la pantalla. Un `NavigationBar` típicamente contiene varios `NavigationBarItem`, cada uno representando un elemento de navegación, similar a las pestañas.

### Consideraciones de Implementación

1.  **Scaffold:** Es importante tener en cuenta que `NavigationBar` debe estar contenido dentro de un `Scaffold`, que es un composable que proporciona una estructura básica para la pantalla, incluyendo áreas para la barra de navegación, la barra superior y el contenido principal.
2.  **NavHost:** Típicamente, se utiliza junto con `NavHost` para gestionar la navegación entre diferentes pantallas.
3.  **Complejidad:** Dado que requiere la creación de múltiples composables y la configuración de un sistema de navegación, su implementación completa puede ser más extensa.

### Aclaración sobre el Grafo de Navegación

Tenga en cuenta que el grafo de navegación para la barra de navegación inferior es diferente al grafo de navegación principal de la aplicación:

*   **Grafo Interno:** Se utiliza para gestionar la navegación dentro de una sección específica de la aplicación (por ejemplo, la sección de usuarios).
*   **Grafo Principal:** Gestiona la navegación entre las diferentes secciones de la aplicación (por ejemplo, entre login, registro y dashboard).

Gracias a esta estructura, podemos tener una navegación más organizada y modular, donde cada sección de la aplicación tiene su propio grafo de navegación interno que se encarga de gestionar las pantallas específicas de esa sección. Esto se conoce como **navegación anidada (nested navigation)**, donde un `NavHost` está contenido dentro de otro `NavHost`.

---

## Comparativa: Tabs vs Navigation Bar

Aunque tanto las pestañas (**Tabs**) como la barra de navegación (**Navigation Bar**) se utilizan para organizar el contenido y facilitar la navegación, tienen diferencias clave en su uso y propósito:

| Característica | Tabs | Navigation Bar |
| :--- | :--- | :--- |
| **Ubicación** | Generalmente en la parte superior de la pantalla | Generalmente en la parte inferior de la pantalla |
| **Límite de elementos** | Puede manejar un número mayor de secciones (hasta 5-6) | Ideal para un número limitado de secciones (3-5) |
| **Uso principal** | Organizar contenido relacionado en secciones dentro de una misma pantalla | Proporcionar acceso rápido a las principales secciones de la aplicación |
| **Navegación** | Cambia el contenido dentro de la misma pantalla | Navega a diferentes pantallas o secciones de la aplicación |
| **Ejemplo de uso** | Pestañas para diferentes categorías de productos en una aplicación de compras | Barra de navegación para acceder a Home, Search, Profile en una aplicación de redes sociales |

---

## Implementación de Navigation Bar

A continuación, se detalla el proceso para integrar un `NavigationBar` en el proyecto desarrollado en las clases anteriores.

### 1. Crear la estructura de paquetes

Supongamos que una vez que se inicie sesión, el usuario verá una pantalla principal con una barra de navegación en la parte inferior. Dicha barra permitirá al usuario navegar entre tres secciones principales: "Inicio", "Búsqueda" y "Perfil".

Para organizar mejor el código, se sugiere la siguiente estructura de archivos y carpetas:

```text
features/
├── dashboard/
│   ├── admin/                  # Pantalla para administradores
│   │   └── AdminScreen.kt
│   ├── component/              # Componentes reutilizables
│   │   ├── BottomNavigationBar.kt
│   │   └── TopAppBar.kt
│   ├── navigation/             # Navegación interna
│   │   ├── DashboardRoutes.kt
│   │   └── UserNavigation.kt
│   └── user/                   # Pantalla para usuarios normales
│       ├── UserScreen.kt
│       ├── home/
│       ├── search/
│       └── profile/
├── login/
├── register/
└── report/
```

### 2. Crear el componente principal para los usuarios (UserScreen)

En el archivo `UserScreen.kt`, cree el componente principal que contendrá el `Scaffold` con la `NavigationBar`.

```kotlin
package com.example.demoapp.features.dashboard.user

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.features.dashboard.component.BottomNavigationBar
import com.example.demoapp.features.dashboard.component.TopAppBar
import com.example.demoapp.features.dashboard.navigation.UserNavigation

@Composable
fun UserScreen(
    onLogout: () -> Unit
) {
    // Estados para la navegación y el título de la barra superior
    val navController = rememberNavController()
    var title by remember { mutableStateOf("Inicio usuario") }

    // Estructura Scaffold (barra superior, barra inferior y contenido)
    Scaffold(
        topBar = {
            // Barra superior con título y botón de cierre de sesión
            TopAppBar(
                title = title,
                logout = onLogout // Función para cerrar sesión
            )
        },
        bottomBar = {
            // Barra de navegación inferior con iconos y títulos
            BottomNavigationBar(
                navController = navController,
                titleTopBar = {
                    title = it
                }
            )
        }
    ) { padding ->
        // Contenido principal gestionado por la navegación (NavHost)
        UserNavigation(
            navController = navController,
            padding = padding
        )
    }
}
```

> **Nota:** Observe que el contenido principal se gestiona a través de `UserNavigation`, que utilizará un `NavHost` para mostrar las diferentes pantallas según la navegación del usuario. Este `NavHost` es diferente al principal de la aplicación.

### 3. Crear la barra de navegación inferior (BottomNavigationBar)

En el archivo `BottomNavigationBar.kt`, cree el componente de la barra de navegación inferior.

```kotlin
package com.example.demoapp.features.dashboard.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.demoapp.features.dashboard.navigation.DashboardRoutes

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    titleTopBar: (String) -> Unit
) {
    // Obtener la entrada actual de la pila de navegación
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Actualizar el título de la barra superior según la pantalla actual
    LaunchedEffect(currentDestination) {
        val destination = Destination.entries.find {
            it.route::class.qualifiedName == currentDestination?.route
        }
        if (destination != null) {
            titleTopBar(destination.label)
        }
    }

    // Crear la barra de navegación inferior
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Iteramos cada item de navegación definido en Destination
        Destination.entries.forEachIndexed { index, destination ->
            // Verificar si el item está seleccionado
            val isSelected = currentDestination?.route == destination.route::class.qualifiedName

            NavigationBarItem(
                label = {
                    // Etiqueta del item de navegación
                    Text(text = destination.label)
                },
                onClick = {
                    // Navegar a la ruta correspondiente al item seleccionado
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    // Icono del item de navegación
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label
                    )
                },
                selected = isSelected
            )
        }
    }
}

// Definición de los items de navegación de la barra inferior
enum class Destination(
    val route: DashboardRoutes,
    val label: String,
    val icon: ImageVector,
) {
    HOME(DashboardRoutes.HomeUser, "Home", Icons.Default.Home),
    SEARCH(DashboardRoutes.Search, "Buscar", Icons.Default.Search),
    PROFILE(DashboardRoutes.Profile, "Perfil", Icons.Default.AccountCircle)
}
```

### 4. Crear el Top App Bar (TopAppBar)

En el archivo `TopAppBar.kt`, cree el componente de la barra superior.

```kotlin
package com.example.demoapp.features.dashboard.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBar(
    title: String,
    logout: () -> Unit
) {
    // Crear la barra superior centrada con título y botón de cierre de sesión
    CenterAlignedTopAppBar(
        modifier = Modifier.fillMaxWidth(),
        title = {
            Text(text = title)
        },
        actions = {
            // En esta sección agregamos el botón de cierre de sesión
            IconButton(onClick = { logout() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
            }
        }
    )
}
```

### 5. Configurar la navegación interna (UserNavigation)

En el archivo `UserNavigation.kt`, configure la navegación interna utilizando un `NavHost`.

```kotlin
package com.example.demoapp.features.dashboard.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.demoapp.features.user.detail.UserDetailScreen
import com.example.demoapp.features.user.list.UserListScreen
import com.example.demoapp.features.user.profile.ProfileScreen
import com.example.demoapp.features.user.search.SearchScreen

@Composable
fun UserNavigation(
    navController: NavHostController,
    padding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = DashboardRoutes.HomeUser
    ) {
        composable<DashboardRoutes.HomeUser> {
            // La pantalla principal de la sección de usuarios que muestra la lista de usuarios
            UserListScreen(
                paddingValues = padding, // Se pasa el padding del Scaffold para evitar solapamiento
                onNavigateToUserDetail = {
                    navController.navigate(DashboardRoutes.UserDetail(it))
                }
            )
        }
        composable<DashboardRoutes.Search> {
            SearchScreen() // Debe crear este composable en el paquete user/search
        }
        composable<DashboardRoutes.Profile> {
            ProfileScreen() // Debe crear este composable en el paquete user/profile
        }
        composable<DashboardRoutes.UserDetail> {
            val args = it.toRoute<DashboardRoutes.UserDetail>()
            UserDetailScreen(
                padding = padding, // Se pasa el padding del Scaffold para evitar solapamiento
                userId = args.userId
            )
        }
    }
}
```

### 6. Definir las rutas de navegación (DashboardRoutes)

En el archivo `DashboardRoutes.kt`, defina las rutas de navegación específicas para la sección de usuarios.

```kotlin
package com.example.demoapp.features.dashboard.navigation

import kotlinx.serialization.Serializable

sealed class DashboardRoutes {
    @Serializable
    data object HomeUser : DashboardRoutes()

    @Serializable
    data object Search : DashboardRoutes()

    @Serializable
    data object Profile : DashboardRoutes()

    @Serializable
    data class UserDetail(val userId: String) : DashboardRoutes()
}
```

### 7. Actualizar la navegación principal de la aplicación

Finalmente, actualice la navegación principal de la aplicación para que, una vez que el usuario inicie sesión, se dirija a la nueva pantalla `UserScreen`.

**En `AppNavigation.kt`:**

```kotlin
composable<MainRoutes.HomeUser> {
    UserScreen(
        onLogout = {
            // Lógica para cerrar sesión y regresar a la pantalla de login
            navController.navigate(MainRoutes.Login) {
                popUpTo(MainRoutes.Login) {
                    inclusive = true // Evitar regresar a la pantalla de usuario
                }
            }
        }
    )
}
```

**En `LoginScreen.kt`:**

Ajuste la navegación desde la pantalla de inicio de sesión para que navegue a `MainRoutes.HomeUser` después de un inicio de sesión exitoso:

```kotlin
composable<MainRoutes.Login> {
    LoginScreen(
        onNavigateToUsers = {
            navController.navigate(MainRoutes.HomeUser)
        }
    )
}
```

> ⚠️ **Importante:** Quite del `MainRoutes.kt` y de `AppNavigation.kt` cualquier referencia a `UserListScreen` y `UserDetailScreen`, ya que ahora estas pantallas se gestionan dentro de la navegación interna de la sección de usuarios.

### 8. Probar la aplicación

Ejecute la aplicación y verifique que, después de iniciar sesión, el usuario sea dirigido a la pantalla principal con la barra de navegación inferior. Asegúrese de que cada ícono en la barra de navegación funcione correctamente y que el título en la barra superior se actualice según la sección seleccionada.

### 9. Sección de administradores

Para la sección de administradores, puede seguir un enfoque similar al de los usuarios. Cree un `AdminScreen` que contenga su propia barra de navegación y navegación interna según las necesidades específicas de los administradores. Reutilice los componentes `BottomNavigationBar` y `TopAppBar` si es posible, para mantener la consistencia en el diseño de la aplicación.

Haga que `BottomNavigationBar` reciba una lista de destinos como parámetro para que pueda ser reutilizable tanto para usuarios como para administradores.

---

## Actividad práctica

1.  **Búsqueda de usuarios:**
    Investigue cómo implementar la funcionalidad de búsqueda en la pantalla de búsqueda (`SearchScreen`). Esto implica usar un `SearchBar` para que los usuarios puedan ingresar términos de búsqueda y mostrar los resultados correspondientes. Así mismo, debe crear un nuevo ViewModel para manejar la lógica de búsqueda con su respectiva función en el repositorio.

2.  **Perfil de usuario:**
    Implemente la pantalla de perfil (`UserProfileScreen`) donde los usuarios puedan ver y editar su información personal, como nombre, correo electrónico, etc.

3.  **Resto de pantallas:**
    Cree todas las pantallas que faltan para completar la navegación del proyecto, incluyendo sus respectivos ViewModels y funciones en el repositorio según sea necesario. Además, cree las demás entidades del dominio que se requieran para estas funcionalidades.

---

> 📚 **Recursos adicionales:**
> - [Material Design Components](https://m3.material.io/)
> - [Jetpack Compose Components](https://developer.android.com/jetpack/compose/components)
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)