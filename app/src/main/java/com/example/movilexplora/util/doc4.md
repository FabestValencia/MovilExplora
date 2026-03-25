# Navegación en Jetpack Compose (Parte 1)

## Introducción

Generalmente, las aplicaciones móviles constan de múltiples pantallas que los usuarios navegan para interactuar con diferentes funcionalidades. En Jetpack Compose, la navegación entre estas pantallas se maneja mediante la biblioteca de navegación de Jetpack Compose, que facilita la gestión de rutas y el paso de datos entre composables.

En esta guía, aprenderemos a implementar la navegación básica en una aplicación de Jetpack Compose y exploraremos algunos de los composables que nos ayudarán a crear una experiencia de usuario fluida.

---

## Navigation Compose

Navigation Compose es una biblioteca que proporciona una forma sencilla de manejar la navegación en aplicaciones construidas con Jetpack Compose. Permite definir rutas, gestionar el back stack y pasar datos entre pantallas de manera eficiente.

### ¿Qué es un back stack?

El **back stack** es una estructura de datos que mantiene un registro de las pantallas visitadas por el usuario en una aplicación.

*   Cada vez que el usuario navega a una nueva pantalla, esta se agrega al tope del back stack.
*   Cuando el usuario presiona el botón de retroceso, la pantalla actual se elimina del tope del back stack y se muestra la pantalla anterior.

Esto permite a los usuarios regresar a pantallas previas de manera intuitiva. Cabe aclarar que el back stack es gestionado automáticamente por el sistema de navegación, lo que significa que no es necesario implementar manualmente esta funcionalidad. Sin embargo, es importante entender cómo funciona para diseñar una experiencia de usuario coherente y predecible.

### NavHost

La navegación en Jetpack Compose se gestiona mediante el componente **NavHost**, que actúa como un contenedor para las diferentes pantallas (composables) de la aplicación. El NavHost define las rutas y los composables asociados a cada ruta. Es el punto central donde se configura la navegación.

### Nested NavHost

En algunos casos, es posible que deseemos tener una navegación más compleja dentro de una sección específica de la aplicación. Para esto, podemos usar un **Nested NavHost**, que es un NavHost anidado dentro de otro NavHost. Esto permite organizar la navegación de manera jerárquica y manejar diferentes flujos de navegación dentro de la misma aplicación.

---

## Configuración inicial

Para comenzar a usar Navigation Compose en nuestro proyecto, debemos agregar la dependencia necesaria.

### 1. Modificar `libs.versions.toml`

Primero, modifique el archivo `libs.versions.toml` para incluir la versión de Navigation Compose:

```toml
[versions]
kotlin = "2.2.21" # Debe usarse kotlin 2.2.x
navigationCompose = "2.9.6"
kotlinxSerialization = "1.9.0"

[libraries]
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinxSerialization" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlinxSerialization" }
```

### 2. Modificar `build.gradle.kts`

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación, modifique lo siguiente:

```kotlin
plugins {
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
```

Finalmente, sincronice el proyecto para descargar las nuevas dependencias.

> 📚 **Nota:** Para más información sobre la navegación en Jetpack Compose, puede consultar la documentación oficial: [Navigation in Jetpack Compose](https://developer.android.com/jetpack/compose/navigation).

---

## Implementación de la navegación básica

A continuación, implementaremos una navegación básica entre una pantalla de inicio y las pantallas de Login y Registro que ya hemos creado en guías anteriores.

### 1. Configurar NavHost

Primero, vamos a crear un composable que contendrá el NavHost y definirá las rutas para nuestras pantallas.

Cree un nuevo paquete llamado `navigation` dentro del paquete `core` y, dentro de este paquete, cree un archivo llamado `AppNavigation.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    // Estado de la navegación, permite controlar la navegación entre pantallas
    val navController = rememberNavController()

    // Un Surface que ocupa toda la pantalla y se adapta al tema de la aplicación
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        NavHost(
            navController = navController, // Controlador de navegación
            startDestination = MainRoutes.Home // Pantalla de inicio
        ) {
            // Definición de las rutas y sus composables asociados
            composable<MainRoutes.Home> {
                HomeScreen()
            }
            composable<MainRoutes.Login> {
                LoginScreen()
            }
            composable<MainRoutes.Register> {
                RegisterScreen()
            }
        }
    }
}
```

Todas las pantallas que hemos creado anteriormente (`HomeScreen`, `LoginScreen` y `RegisterScreen`) se integran en el NavHost mediante la función `composable`, que asocia cada ruta con su respectivo composable.

> ⚠️ **Importante:** Observe que `NavHost` está dentro de un `Surface` que ocupa toda la pantalla, lo que garantiza que nuestras pantallas se muestren correctamente. Además, el `Surface` hereda el color de fondo del tema actual de la aplicación, por lo que funciona bien tanto en modo claro como oscuro.

### 2. Definir las rutas de navegación

Cree un archivo `MainRoutes.kt` en el mismo paquete `navigation` para definir las rutas de navegación de nuestra aplicación. Utilizaremos una `sealed class` para representar las diferentes rutas.

```kotlin
package com.example.demoapp.core.navigation

import kotlinx.serialization.Serializable

sealed class MainRoutes {
    @Serializable
    data object Home : MainRoutes()

    @Serializable
    data object Login : MainRoutes()

    @Serializable
    data object Register : MainRoutes()
}
```

Una `sealed class` nos permite definir un conjunto cerrado de subclases, lo que es útil para representar las diferentes rutas de navegación en nuestra aplicación.

### 3. Navegar entre pantallas

Ahora, ajustemos la pantalla de inicio para que los botones de Login y Registro naveguen a las pantallas correspondientes cuando se presionen. Modifique el composable `HomeScreen` para incluir las funciones de navegación:

```kotlin
package com.example.demoapp.features.home

// Importaciones necesarias...

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit, // Función para navegar a la pantalla de Login
    onNavigateToRegister: () -> Unit // Función para navegar a la pantalla de Registro
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.welcome),
            contentDescription = "Welcome Image"
        )
        Text(text = "Home Screen")
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onNavigateToLogin // Se indica qué hay que hacer al presionar el botón
            ) {
                Text(text = "Iniciar sesión")
            }
            Button(
                onClick = onNavigateToRegister // Se indica qué hay que hacer al presionar el botón
            ) {
                Text(text = "Crear una cuenta")
            }
        }
    }
}
```

### 4. Ajustes en AppNavigation.kt

Ahora, regresemos al archivo `AppNavigation.kt` y ajustemos la llamada a `HomeScreen` para pasar las funciones de navegación adecuadas.

```kotlin
composable<MainRoutes.Home> {
    HomeScreen(
        onNavigateToLogin = {
            navController.navigate(MainRoutes.Login)
        },
        onNavigateToRegister = {
            navController.navigate(MainRoutes.Register)
        }
    )
}
```

Observe que `AppNavigation.kt` es el punto central donde se maneja la navegación entre las pantallas. Es una buena práctica mantener toda la lógica de navegación en un solo lugar para facilitar el mantenimiento y la comprensión del flujo de la aplicación.

### 5. Actualizar el punto de entrada de la aplicación

Finalmente, asegúrese de que el punto de entrada de su aplicación utilice el nuevo sistema de navegación. En su archivo `MainActivity.kt`, reemplace la llamada a `LoginScreen()`, `RegisterScreen()` o `HomeScreen()` con `AppNavigation()`.

```kotlin
setContent {
    DemoAppTheme {
        // La navegación de la aplicación se maneja aquí
        AppNavigation()
    }
}
```

El composable `AppNavigation()` ahora manejará toda la navegación de la aplicación y se incluye en el tema de la aplicación para mantener la coherencia visual.

### 6. Probar la navegación

Ejecute la aplicación en un emulador o dispositivo físico. Debería ver la pantalla de inicio con los botones "Login" y "Register". Al hacer clic en cada botón, debería navegar a las pantallas correspondientes. Use el botón de retroceso del dispositivo para regresar a la pantalla de inicio desde las pantallas de Login y Register.

---

## Implementación de navegación con parámetros

En algunas situaciones, es posible que necesitemos pasar datos entre pantallas durante la navegación. Navigation Compose facilita esto mediante el uso de argumentos en las rutas. A continuación, veremos cómo pasar parámetros entre pantallas.

### 1. Definir una ruta con parámetros

En la clase anterior, creamos dos pantallas: `UserListScreen` y `UserDetailScreen`. Vamos a definir las rutas para estas pantallas en `MainRoutes.kt`, asegurándonos de que `UserDetailScreen` pueda recibir un parámetro, como el ID del usuario.

```kotlin
@Serializable
data object UserList : MainRoutes() // Ruta para la lista de usuarios

@Serializable
data class UserDetail(val userId: String) : MainRoutes() // Ruta para los detalles del usuario
```

> ⚠️ **Importante:** Asegúrese de que el parámetro `userId` sea de tipo `String` o un tipo primitivo compatible con la serialización. Se recomienda usar tipos simples para los parámetros de navegación, no objetos complejos, ya que esto puede tener un impacto en el rendimiento y la complejidad.

### 2. Configurar la navegación con parámetros

Agregue las nuevas rutas y composables en `AppNavigation.kt` para manejar la navegación con parámetros. Dentro del `NavHost`, agregue la siguiente configuración para la pantalla del detalle del usuario, la cual recibe el ID del usuario como parámetro:

```kotlin
composable<MainRoutes.UserDetail> {
    // Se obtienen los argumentos de la ruta
    val args = it.toRoute<MainRoutes.UserDetail>()
    // Se pasa el ID del usuario a la pantalla de detalles del usuario
    UserDetailScreen(
        userId = args.userId
    )
}
```

Adicionalmente, dentro del `NavHost`, agregue la configuración para la pantalla de la lista de usuarios, ya que desde allí se navegará a la pantalla de detalles del usuario:

```kotlin
composable<MainRoutes.UserList> {
    // Se pasa la función de navegación a la pantalla de la lista de usuarios
    UserListScreen(
        onNavigateToUserDetail = { userId ->
            navController.navigate(MainRoutes.UserDetail(userId))
        }
    )
}
```

Tenga en cuenta que la navegación a `UserDetailScreen` ahora incluye el ID del usuario como parámetro. Este ID es dado cuando se invoca la función `onNavigateToUserDetail` desde `UserListScreen` al presionar un usuario en la lista.

### 3. Probar la navegación con parámetros

Para probar la navegación con parámetros, haremos un pequeño ajuste en `LoginScreen` para que cuando el usuario inicie sesión, navegue a la pantalla de la lista de usuarios y desde allí pueda navegar a la pantalla de detalles del usuario.

**Ajuste en LoginScreen:**

A la función `LoginScreen`, agregue la función de navegación a la pantalla de la lista de usuarios:

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToUsers: () -> Unit // Función para navegar a la pantalla de la lista de usuarios
) {
    val loginResult by viewModel.loginResult.collectAsState()

    // Efecto secundario para manejar la navegación después de un inicio de sesión exitoso
    LaunchedEffect(loginResult) {
        // Solo navega si el inicio de sesión fue exitoso
        if (loginResult is RequestResult.Success) {
            viewModel.resetForm() // Borrar todos los campos del formulario
            onNavigateToUsers() // Navegar a la pantalla de la lista de usuarios
        }
    }
    // Resto del código de LoginScreen...
}
```

Un `LaunchedEffect` es un efecto secundario en Jetpack Compose que se ejecuta cuando una clave específica cambia. En este caso, se utiliza para observar los cambios en el estado `loginResult` del ViewModel.

**Ajuste en AppNavigation.kt:**

Modifique la llamada a `LoginScreen` para pasar la función de navegación a la pantalla de la lista de usuarios:

```kotlin
composable<MainRoutes.Login> {
    LoginScreen(
        onNavigateToUsers = {
            navController.navigate(MainRoutes.UserList)
        }
    )
}
```

Ejecute la aplicación y navegue a la pantalla de Login. Inicie sesión con las credenciales correctas y debería navegar a la pantalla de la lista de usuarios. Al seleccionar un usuario, debería navegar a la pantalla de detalles del usuario, mostrando el ID del usuario seleccionado.

---

## Navegación hacia atrás

En Jetpack Compose, la navegación hacia atrás se maneja automáticamente mediante el controlador de navegación (`NavController`). Cuando el usuario presiona el botón de retroceso del dispositivo, el `NavController` elimina la pantalla actual del back stack y muestra la pantalla anterior.

Sin embargo, en algunos casos, es posible que deseemos personalizar el comportamiento del botón de retroceso. Por ejemplo, podríamos querer mostrar un cuadro de diálogo de confirmación antes de salir de una pantalla o formulario, o simplemente que se navegue hacia atrás al dar clic en un botón específico.

Para manejar esto, podemos usar el componente **BackHandler** proporcionado por Jetpack Compose. El `BackHandler` es un componente que permite interceptar el evento de retroceso del dispositivo.

Por ejemplo, en el formulario de registro que creamos en las clases anteriores, podríamos querer confirmar con el usuario si realmente desea salir del formulario sin guardar los cambios, para evitar la pérdida de datos.

### Paso 1: Importar BackHandler

Agregue el siguiente código dentro de la función composable del formulario de registro (`RegisterScreen`), justo al inicio, para interceptar el evento de retroceso:

```kotlin
BackHandler(
    enabled = !showExitDialog
) {
    showExitDialog = true
}
```

Deberá agregar un nuevo estado para controlar la visibilidad del cuadro de diálogo de salida. Puede basarse en el que usamos para el cuadro de diálogo de confirmación. Adicionalmente, debe agregar otro bloque con un `if` para mostrar un cuadro de diálogo similar al de confirmación, pero para confirmar la salida del formulario.

> ⚠️ **Nota:** Ajuste el composable `ConfirmAlertDialog` para que sea reutilizable, permitiendo personalizar el título y el texto del diálogo y utilícelo tanto para la confirmación de envío como para la confirmación de salida.

### Paso 2: Evento de navegación hacia atrás personalizado

En el cuadro de diálogo de confirmación de salida, en el botón de confirmar, agregue la lógica para navegar hacia atrás utilizando el NavController. Para esto, puede pasar una función lambda `onNavigateBack` al composable del formulario de registro y llamarla cuando el usuario confirme la salida.

```kotlin
if (showExitDialog) {
    ConfirmAlertDialog(
        onShowExitDialogChange = { showExitDialog = it },
        onConfirm = {
            viewModel.resetForm() // Resetear el formulario antes de navegar hacia atrás
            onNavigateToBack() // Navegar hacia atrás
        },
        title = "¿Está seguro de salir?",
        text = "Está a punto de salir de la aplicación."
    )
}
```

### Paso 3: Pasar la función de navegación hacia atrás

Finalmente, en `AppNavigation.kt`, al llamar a `RegisterForm`, pase la función `onNavigateBack` para que navegue de regreso a la pantalla de inicio:

```kotlin
composable<MainRoutes.Register> {
    RegisterScreen(
        onNavigateToBack = {
            navController.popBackStack()
        }
    )
}
```

El `navController` se encargará de manejar la navegación hacia atrás, eliminando la pantalla actual del back stack y mostrando la pantalla anterior.

### Paso 4: Probar la navegación hacia atrás personalizada

Ejecute la aplicación y navegue a la pantalla de registro. Intente presionar el botón de retroceso del dispositivo o el botón de salir en el formulario. Debería aparecer un cuadro de diálogo de confirmación. Si confirma, debería navegar hacia atrás a la pantalla de inicio; si cancela, debería permanecer en la pantalla de registro.

---

## Actividad práctica

1.  **Nueva navegación desde Login:**
    Agregue un botón en la pantalla de Login que permita al usuario navegar directamente a la pantalla de Registro y otro botón para navegar a una pantalla cuando se haya olvidado la contraseña.

2.  **Ajuste a la pantalla del detalle del usuario:**
    Ajuste la pantalla `UserDetailScreen` para que muestre toda la información del usuario en lugar de solo el ID del usuario. Para esto, use el `UserViewModel`; puede crear una función en el ViewModel que reciba el ID del usuario y devuelva el objeto `User` correspondiente. Luego, en `UserDetailScreen`, obtenga el usuario usando esta función y muestre sus detalles en la UI incluyendo una imagen de perfil utilizando Coil.

3.  **Navegación con las pantallas de reportes:**
    Ajuste el `NavHost` para agregar dos nuevas pantallas: `ReportListScreen` y `ReportDetailScreen`. La pantalla de lista de reportes debe mostrar una lista de reportes y al seleccionar uno, navegar a la pantalla de detalles del reporte pasando el ID del reporte como parámetro.

---

> 📚 **Recursos adicionales:**
> - [Navigation in Jetpack Compose](https://developer.android.com/jetpack/compose/navigation)
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)