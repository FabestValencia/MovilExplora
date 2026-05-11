# Data Store en Jetpack Compose



## Introducción

En el desarrollo de aplicaciones móviles con Jetpack Compose, es fundamental contar con mecanismos eficientes para almacenar y gestionar datos de manera persistente. **Data Store** es una solución moderna proporcionada por Android que permite almacenar datos de forma segura y eficiente, reemplazando a `SharedPreferences`. En esta guía, exploraremos cómo utilizar Data Store en una aplicación Jetpack Compose para almacenar y recuperar datos de usuario.

Data Store ofrece dos tipos principales de almacenamiento:

1.  **Preferences Data Store**: Ideal para almacenar pares clave-valor simples.
2.  **Proto Data Store**: Utiliza Protocol Buffers para almacenar datos estructurados.

En nuestro caso, nos centraremos en **Preferences Data Store** debido a su simplicidad y facilidad de uso.

La implementación de Data Store en Jetpack Compose nos permitirá mantener la persistencia de datos asegurando que la información relevante se conserve incluso después de cerrar la aplicación. Esto es especialmente útil para almacenar configuraciones de usuario, datos de sesión y preferencias.

---

## SharedPreferences vs Data Store

Originalmente, Android proporcionaba `SharedPreferences` como la principal solución para el almacenamiento de datos clave-valor. Sin embargo, `SharedPreferences` tiene varios problemas, por lo que Google introdujo **Data Store** como una alternativa más moderna y eficiente.

Antes de sumergirnos en la implementación de Data Store, es importante entender las diferencias clave entre `SharedPreferences` y `Data Store`.

| Característica | SharedPreferences | Data Store |
| :--- | :--- | :--- |
| **Modelo de almacenamiento** | Basado en XML | Basado en archivos binarios |
| **Tipo de datos** | Clave-valor simples | Clave-valor (Preferences) o datos estructurados (Proto) |
| **Seguridad** | No cifrado por defecto | Soporta cifrado y es más seguro |
| **Rendimiento** | Menor rendimiento en operaciones concurrentes | Mejor rendimiento en operaciones concurrentes |
| **API** | Síncrona y propensa a errores | Asíncrona y basada en corrutinas |
| **Manejo de errores** | Propenso a errores de concurrencia | Manejo de errores más robusto |

Claramente, Data Store ofrece varias ventajas sobre SharedPreferences, especialmente en términos de seguridad, rendimiento y manejo de errores. Por estas razones, se recomienda utilizar Data Store para nuevas aplicaciones y migrar las existentes cuando sea posible.

> 📚 **Nota:** La documentación oficial de Data Store se puede encontrar en el siguiente enlace: [Data Store](https://developer.android.com/topic/libraries/architecture/datastore).

---

## Implementación de Data Store en el proyecto

Abra su proyecto de Android Studio y siga los siguientes pasos para integrar Data Store.

### 1. Agregar dependencias

Para utilizar Data Store en nuestro proyecto, primero debe agregar las dependencias necesarias en el archivo `libs.versions.toml`. Añada la siguiente línea para incluir la biblioteca de Data Store:

```toml
[versions]
dataStore="1.2.0"

[libraries]
data-store={ module="androidx.datastore:datastore-preferences", version.ref="dataStore"}
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación, agregue la dependencia:

```kotlin
dependencies {
    implementation(libs.data.store)
}
```

Sincronice el proyecto para descargar las nuevas dependencias.

### 2. Configurar Data Store

El siguiente paso es crear un Singleton para manejar la instancia de Data Store. Cree un archivo llamado `SessionDataStore.kt` para gestionar el almacenamiento de datos de sesión del usuario. Este archivo debe crearlo en el paquete `data/datastore`.

```kotlin
package com.example.demoapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.demoapp.data.model.UserSession
import com.example.demoapp.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extensión para crear la instancia de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name="session")

@Singleton
class SessionDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Claves para las preferencias
    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val ROLE = stringPreferencesKey("role")
    }

    // Flujo para observar los datos de la sesión
    val sessionFlow: Flow<UserSession?> = context.dataStore.data.map { prefs ->
        // Intenta obtener los datos de la sesión desde Data Store
        val userId = prefs[Keys.USER_ID]
        val roleStr = prefs[Keys.ROLE]

        // Si alguno de los datos es nulo, retorna null
        if (userId.isNullOrBlank() || roleStr.isNullOrBlank()) {
            null
        } else {
            // Retorna un objeto UserSession con los datos obtenidos si existen
            UserSession(
                userId = userId,
                role = UserRole.valueOf(roleStr)
            )
        }
    }

    suspend fun saveSession(userId: String, role: UserRole) {
        // Guarda los datos de la sesión en Data Store (clave-valor)
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_ID] = userId
            prefs[Keys.ROLE] = role.name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
```

### 3. Crear el modelo de datos

Cree un archivo llamado `UserSession.kt` en el paquete `data/model` para definir el modelo de datos que se almacenará en Data Store.

```kotlin
package com.example.demoapp.data.model

import com.example.demoapp.domain.model.UserRole

data class UserSession(
    val userId: String,
    val role: UserRole
)
```

Esta clase representa la sesión del usuario, incluyendo su ID y rol. Es como un DTO (Data Transfer Object) que facilita el almacenamiento y recuperación de datos desde Data Store.

### 4. Crear ViewModel para gestionar la sesión

Cree un archivo llamado `SessionViewModel.kt` en el paquete `core/navigation` para gestionar la lógica de la sesión del usuario utilizando Data Store.

```kotlin
package com.example.demoapp.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.model.UserSession
import com.example.demoapp.data.datastore.SessionDataStore
import com.example.demoapp.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Define los posibles estados de la sesión
sealed interface SessionState {
    data object Loading : SessionState
    data object NotAuthenticated : SessionState
    data class Authenticated(val session: UserSession) : SessionState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    // Flujo que representa el estado de la sesión
    val sessionState: StateFlow<SessionState> = sessionDataStore.sessionFlow
        .map { session ->
            // Mapea la sesión a un estado de sesión
            if (session != null) {
                SessionState.Authenticated(session)
            } else {
                SessionState.NotAuthenticated
            }
        }
        .stateIn(
            // Convierte el flujo en un StateFlow para que pueda ser observado
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionState.Loading
        )

    fun login(userId: String, role: UserRole) {
        // Guarda la sesión del usuario en Data Store. Se utiliza viewModelScope para lanzar la corrutina
        viewModelScope.launch {
            sessionDataStore.saveSession(userId, role)
        }
    }

    fun logout() {
        // Limpia la sesión del usuario en Data Store. Se utiliza viewModelScope para lanzar la corrutina
        viewModelScope.launch {
            sessionDataStore.clearSession()
        }
    }
}
```

### 5. Integrar Data Store en la UI con Jetpack Compose

Ahora, vamos a modificar la navegación para que la pantalla de inicio dependa del estado de la sesión gestionada por Data Store.
La idea es hacer que `AppNavigation` sea reactivo al estado de autenticación.
# Grafo de Navegación — Según Estado de Sesión

```
┌─────────────────────────────────────────────────────────────────┐
│                    AppNavigation                                │
│                  when (sessionState)                            │
└───────────────────────────┬─────────────────────────────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
     NotAuthenticated                  Authenticated
            │                               │
            ▼                               ▼
┌──────────────────────┐        ┌──────────────────────────────┐
│   AuthNavigation     │        │    MainNavigation            │
│ startDestination:    │        │  startDestination por role   │
│       Home           │        │                              │
└──────────┬───────────┘        └─────────────┬────────────────┘
           │                                  │
     ┌─────┼─────┐                      ┌─────┴─────┐
     │     │     │                      │           │
     ▼     ▼     ▼                      ▼           ▼
┌────────┐ ┌────────┐ ┌──────────┐ ┌──────────┐ ┌────────────┐
│  Home  │ │ Login  │ │ Register │ │ HomeUser │ │ HomeAdmin  │
└────────┘ └────┬───┘ └──────────┘ └────┬─────┘ └─────┬──────┘
                │                       │             │
                │                    logout()         │
                │                       │             │
                ▼                       ▼             ▼
        ┌──────────────────┐    ┌─────────────────────────┐
        │   Authenticated  │    │    NotAuthenticated     │
        │AppNavigation     │    │   AppNavigation         │
        │    reactivo      │    │       reactivo          │
        └──────────────────┘    └─────────────────────────┘

AppNavigation reacciona automáticamente al cambio de sessionState
        no hay navegación manual entre grafos
```

Vaya a `AppNavigation.kt` y actualice el código de la siguiente manera:

```kotlin
@Composable
fun AppNavigation(
    sessionViewModel: SessionViewModel = hiltViewModel()
) {
    // Observa el estado de la sesión desde el ViewModel
    val sessionState by sessionViewModel.sessionState.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        when (val state = sessionState) {
            is SessionState.Loading -> {
                // Se muestra un indicador de carga mientras se determina el estado de la sesión
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SessionState.NotAuthenticated -> AuthNavigation() // Sin sesión, muestra la navegación de autenticación
            is SessionState.Authenticated -> MainNavigation(
                session = state.session,
                onLogout = sessionViewModel::logout
            )
        }
    }
}
```

Simplificamos la navegación para que dependa del estado de la sesión. Si el usuario no está autenticado, se muestra la navegación de autenticación; si está autenticado, se muestra la navegación principal con la sesión del usuario.

Debe agregar el siguiente composable para la **navegación de autenticación** en el mismo archivo `AppNavigation.kt`:

```kotlin
@Composable
private fun AuthNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MainRoutes.Home
    ) {
        composable<MainRoutes.Home> {
            // Tal como antes, se pasan los callbacks de navegación
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(MainRoutes.Login)
                },
                onNavigateToRegister = {
                    navController.navigate(MainRoutes.Register)
                }
            )
        }
        composable<MainRoutes.Login> {
            // Sin callback de navegación ya que el cambio de sesión hace que AppNavigation cambie
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(MainRoutes.Register)
                }
            )
        }
        composable<MainRoutes.Register> {
            RegisterScreen(
                onNavigateToBack = {
                    navController.popBackStack()
                }
            )
        }
        // Otras pantallas que no requieren autenticación pueden ir aquí
    }
}
```

> ⚠ **Importante:** En este ejemplo solo se usan algunas pantallas, en su proyecto puede tener más pantallas que debe agregar aquí.

Así mismo, debe agregar el siguiente composable para la **navegación una vez autenticado** en el mismo archivo `AppNavigation.kt`:

```kotlin
@Composable
private fun MainNavigation(
    session: UserSession,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // Determina la pantalla de inicio según el rol del usuario
    val startDestination: Any = when (session.role) {
        UserRole.ADMIN -> MainRoutes.HomeAdmin
        UserRole.USER -> MainRoutes.HomeUser
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<MainRoutes.HomeUser> {
            // Se pasa el callback de logout para cerrar sesión, úselo dentro de UserScreen
            UserScreen(
                onLogout = onLogout
            )
        }
        composable<MainRoutes.HomeAdmin> {
            // Si no tiene AdminScreen debe crearlo, se pasa el callback de logout para cerrar sesión
            AdminScreen(
                onLogout = onLogout
            )
        }
    }
}
```

De esta manera, la navegación de la aplicación se adapta dinámicamente según el estado de la sesión del usuario almacenada en Data Store. Tenga en cuenta que tanto `UserScreen` como `AdminScreen` tienen su propio `NavHost` para gestionar la navegación interna.

### 6. Probar la aplicación

Ejecute la aplicación en un emulador o dispositivo físico. Navegue a la pantalla de inicio de sesión, ingrese las credenciales y verifique que la sesión se almacene correctamente en Data Store. Luego, cierre y vuelva a abrir la aplicación para asegurarse de que la sesión persista y el usuario permanezca autenticado.

### 7. Acceder a los datos almacenados

Para acceder al usuario almacenado en Data Store desde cualquier parte de la aplicación, simplemente inyecte `SessionDataStore` utilizando Hilt directamente en el ViewModel donde lo necesite. Luego, puede observar el flujo `sessionFlow` para obtener los datos de la sesión del usuario.

Por ejemplo, en un ViewModel del perfil de usuario:

```kotlin
package com.example.demoapp.features.user.profile

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository,
    private val sessionDataStore: SessionDataStore // Inyección de SessionDataStore
) : ViewModel() {

    // Estado para el usuario
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        // Cargar el usuario actual al inicializar el ViewModel
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        // Usar viewModelScope para lanzar una corrutina
        viewModelScope.launch {
            // Obtener userId de la sesión
            val session = sessionDataStore.sessionFlow.filterNotNull().first()
            // Buscar el usuario en el repositorio usando el userId
            val foundUser = repository.findById(session.userId)
            // Actualizar el estado del usuario si se encuentra
            foundUser?.let { user ->
                _user.value = user
                // Aquí puede cargar los datos del usuario en la UI
            }
        }
    }
    // Resto del ViewModel...
}
```

### 8. Probar la aplicación nuevamente

Ejecute nuevamente la aplicación y navegue a la pantalla de perfil. Verifique que los datos del usuario se carguen correctamente utilizando el `userId` almacenado en Data Store.

---

## Actividad práctica

1.  **Pantallas del rol Admin:**
    Cree todas las pantallas necesarias para el rol Admin acorde a los requerimientos de su proyecto. Asegúrese de que estas pantallas solo sean accesibles para usuarios con el rol Admin. Si ya ha creado estas pantallas, revise que la navegación funcione correctamente según el rol del usuario almacenado en Data Store.

2.  **Pantallas del rol User:**
    Cree todas las pantallas necesarias para el rol User acorde a los requerimientos de su proyecto. Asegúrese de que estas pantallas solo sean accesibles para usuarios con el rol User. Si ya ha creado estas pantallas, revise que la navegación funcione correctamente según el rol del usuario almacenado en Data Store.

3.  **Datos adicionales en Data Store:**
    Piense en otros datos que podrían ser útiles almacenar en Data Store (por ejemplo, preferencias de usuario, configuraciones de la aplicación, etc.) e implemente su almacenamiento y recuperación utilizando Data Store.

---

> 📚 **Recursos adicionales:**
> - [Data Store Documentation](https://developer.android.com/topic/libraries/architecture/datastore)
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)