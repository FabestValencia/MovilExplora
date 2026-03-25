# Entidades y datos en Jetpack Compose

## Introducción

Toda aplicación que gestione datos debe tener definidas sus entidades del dominio. Una entidad del dominio representa un objeto o concepto central en el sistema. Por ejemplo, en una aplicación de gestión de tareas, una entidad del dominio podría ser "Tarea", que tendría atributos como título, descripción, fecha de vencimiento y estado. En una aplicación de comercio electrónico, una entidad del dominio podría ser "Producto", con atributos como nombre, precio, descripción y categoría.

Definir claramente las entidades del dominio es crucial para el diseño y desarrollo de la aplicación, ya que estas entidades forman la base sobre la cual se construyen las funcionalidades y la lógica del negocio.

---

## Definición de Entidades del Dominio

De ahora en adelante, a manera de ejemplo, trabajaremos con una aplicación donde los ciudadanos (`users`) reportan problemas urbanos usando un mapa, y los administradores (`admin`) gestionan, validan y dan seguimiento a esos reportes.

Cada reporte (`Report`) tendrá los siguientes atributos:

*   `id`: Identificador único del reporte.
*   `title`: Título del reporte.
*   `description`: Descripción detallada del problema reportado.
*   `location`: Ubicación geográfica del problema.
*   `status`: Estado del reporte (pendiente, en progreso, resuelto).
*   `type`: Tipo de problema (hueco en la vía, alumbrado dañado, basura, accidente, etc.).
*   `photoUrl`: URL de la foto del problema reportado.
*   `ownerId`: Identificador del usuario que creó el reporte.

Adicionalmente, la ubicación (`Location`) tendrá:

*   `latitude`: Latitud de la ubicación.
*   `longitude`: Longitud de la ubicación.

Por otro lado, cada usuario (`User`) tendrá los siguientes atributos:

*   `id`: Identificador único del usuario.
*   `name`: Nombre completo del usuario.
*   `email`: Correo electrónico del usuario.
*   `phoneNumber`: Número de teléfono del usuario.
*   `city`: Ciudad de residencia del usuario.
*   `address`: Dirección del usuario (opcional).
*   `role`: Rol del usuario (user o admin).
*   `profilePictureUrl`: URL de la foto de perfil del usuario.
*   `password`: Contraseña del usuario (almacenada de forma segura).

Por lo tanto, debemos crear tres entidades de dominio: `Report`, `User` y `Location`. Y dos enumeraciones: `ReportStatus` y `UserRole`.

---

## Creación de las Entidades del Dominio

En el paquete `domain.model`, cree los siguientes archivos de Kotlin para definir las entidades y enumeraciones mencionadas:

### Entidad Report

Para la entidad `Report`, cree un archivo llamado `Report.kt` y defina la clase de la siguiente manera:

```kotlin
data class Report(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val status: ReportStatus,
    val type: String,
    val photoUrl: String,
    val ownerId: String
)
```

### Entidad Location

Para la entidad `Location`, cree un archivo llamado `Location.kt` y defina la clase de la siguiente manera:

```kotlin
data class Location(
    val latitude: Double,
    val longitude: Double
)
```

### Entidad User

Para la entidad `User`, cree un archivo llamado `User.kt` y defina la clase de la siguiente manera:

```kotlin
data class User(
    val id: String,
    val name: String,
    val city: String,
    val address: String,
    val email: String,
    val password: String,
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: UserRole = UserRole.USER
)
```

> ⚠️ **Nota:** Los atributos `phoneNumber` y `profilePictureUrl` son opcionales y tienen valores predeterminados vacíos. El atributo `role` también tiene un valor predeterminado de `UserRole.USER`.

### Enumeración ReportStatus

Para la enumeración `ReportStatus`, cree un archivo llamado `ReportStatus.kt` y defina la enumeración de la siguiente manera:

```kotlin
enum class ReportStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED
}
```

### Enumeración UserRole

Finalmente, para la enumeración `UserRole`, cree un archivo llamado `UserRole.kt` y defina la enumeración de la siguiente manera:

```kotlin
enum class UserRole {
    USER,
    ADMIN
}
```

Con base en estas definiciones, iremos integrando nuevas pantallas y composables para gestionar los reportes y usuarios en la aplicación.

> ⚠️ **Importante:** La diferencia entre una clase y una `data class` en Kotlin es que una `data class` está diseñada para almacenar datos y proporciona automáticamente métodos útiles como `equals()`, `hashCode()`, `toString()`, y `copy()`. Esto facilita la manipulación y comparación de objetos de datos. En contraste, una clase normal no tiene estas funcionalidades automáticas y se utiliza para definir comportamientos más complejos.

---

## Múltiples Elementos en Compose

Es común que las aplicaciones móviles requieran mostrar múltiples elementos en la pantalla, como listas de datos, galerías de imágenes o menús. En Jetpack Compose, existen varias formas de manejar y mostrar múltiples elementos de manera eficiente y flexible. Esto será importante para nuestra aplicación de reportes urbanos, donde necesitaremos mostrar listas de reportes y usuarios.

### Composables para Múltiples Elementos

#### LazyColumn y LazyRow

`LazyColumn` y `LazyRow` son componentes que permiten mostrar listas de elementos de manera eficiente, cargando solo los elementos visibles en pantalla. Esto es especialmente útil para listas largas, ya que mejora el rendimiento de la aplicación. Además, estos componentes incluyen scroll automático.

```kotlin
LazyColumn {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

Supongamos que `itemsList` es una lista de objetos que contienen un atributo `name`. El código anterior crea una columna perezosa que muestra el nombre de cada elemento en la lista, renderizando solo los elementos visibles.

Así mismo, podemos usar `LazyRow` para mostrar los elementos en una fila horizontal:

```kotlin
LazyRow {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

#### Grid

Para mostrar elementos en una cuadrícula, podemos usar `LazyVerticalGrid` o `LazyHorizontalGrid`. Estos componentes permiten organizar los elementos en filas y columnas formando una cuadrícula.

```kotlin
LazyVerticalGrid(columns = GridCells.Fixed(2)) {
    items(itemsList) { item ->
        Text(text = item.name)
    }
}
```

En este ejemplo, `LazyVerticalGrid` crea una cuadrícula con dos columnas, mostrando los nombres de los elementos en la lista.

> 📚 **Nota:** Para más información sobre cómo usar `LazyColumn`, `LazyRow` y `LazyVerticalGrid`, puede consultar la documentación oficial de Jetpack Compose: [Layouts en Jetpack Compose](https://developer.android.com/jetpack/compose/layouts).

Estos composables son fundamentales para mostrar listas de datos en las aplicaciones ya que permiten una representación eficiente y flexible de múltiples elementos si los comparamos con un `Column` o `Row` tradicional, que renderizaría todos los elementos de la lista, lo que podría afectar el rendimiento si la lista es larga.

---

## Ejemplo: Listado de Usuarios y su Detalle

Cree una pantalla llamada `UserListScreen` que muestre una lista de usuarios utilizando `LazyColumn`. Cada usuario debe mostrarse en un `ListItem`, y al hacer clic en un usuario, la idea es poder navegar a una pantalla de detalles del usuario (aunque la navegación no se implementará en este momento).

### 1. Crear la pantalla de la lista de usuarios

Cree un nuevo composable llamado `UserListScreen` que muestre una lista de usuarios. Este composable debe crearse en `features/users/list`:

```kotlin
package com.example.demoapp.features.users.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.demoapp.domain.model.User
import androidx.compose.ui.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.size

@Composable
fun UserListScreen(
    onNavigateToUserDetail: (String) -> Unit, // Función para navegar a la pantalla de detalle
    usersViewModel: UserListViewModel = viewModel()
) {
    // Obtener la lista de usuarios desde el ViewModel
    val users by usersViewModel.users.collectAsState(initial = emptyList())

    // Se usa LazyColumn para mostrar la lista de usuarios.
    // LazyColumn solo renderiza los elementos visibles en pantalla, mejorando el rendimiento
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Iterar sobre la lista de usuarios y crear un ItemUser para cada uno
        items(users) {
            ItemUser(
                onNavigateToUserDetail = onNavigateToUserDetail,
                user = it
            )
        }
    }
}

@Composable
fun ItemUser(
    onNavigateToUserDetail: (String) -> Unit, // Función para navegar a la pantalla de detalle
    user: User
) {
    // ListItem es un composable que muestra un elemento de una lista con un diseño predefinido
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable {
                // Sabemos que al hacer clic en el ListItem, se navega a la pantalla de detalle
                onNavigateToUserDetail(user.id)
            },
        headlineContent = { Text(text = user.name) },
        supportingContent = {
            // Mostrar el email del usuario como contenido secundario (puede ajustarse según necesite)
            Text(text = user.email)
        },
        leadingContent = {
            // Mostrar la imagen de perfil del usuario
            AsyncImage(
                contentScale = ContentScale.Crop,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(user.profilePictureUrl) // URL de la imagen
                    .crossfade(true) // Efecto de desvanecimiento al cargar
                    .build(),
                contentDescription = "Imagen de perfil",
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(80.dp)
            )
        }
    )
}
```

Observe que UserListScreen utiliza un ViewModel llamado UserViewModel para obtener la lista de usuarios. Cada usuario se muestra en un ListItem, y al hacer clic en un usuario, se llama a la función onNavigateToUserDetail, pasando el ID del usuario seleccionado.

Para obtener la lista de usuarios, se usa collectAsState para observar los cambios en el StateFlow del ViewModel. Esto asegura que la UI se actualice automáticamente cuando la lista de usuarios cambie.

### 2. Crear el UserListViewModel

Cree el `UserListViewModel` en el mismo paquete de `UserListScreen`, con el siguiente código:

```kotlin
package com.example.demoapp.features.users.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserListViewModel : ViewModel() {
    // Patrón de StateFlow para manejar el estado de la lista de usuarios
    private val _users = MutableStateFlow(emptyList<User>())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Inicializar con algunos datos de ejemplo
    init {
        fetchUsers()
    }

    // Función para obtener un usuario por su ID
    fun findById(id: String): User? {
        return _users.value.find { it.id == id }
    }

    // Función para simular algunos datos de usuarios
    private fun fetchUsers() {
        val users = listOf(
            User(
                id = "1",
                name = "Juan",
                city = "Ciudad 1",
                address = "Calle 123",
                email = "juan@email.com",
                password = "111111",
                profilePictureUrl = "https://m.media-amazon.com/images/I/41g6jROgo0L.png"
            ),
            User(
                id = "2",
                name = "Maria",
                city = "Pereira",
                address = "Calle 456",
                email = "maria@email.com",
                password = "222222",
                profilePictureUrl = "https://picsum.photos/200?random=2"
            ),
            User(
                id = "3",
                name = "Carlos",
                city = "Armenia",
                address = "Calle 789",
                email = "carlos@email.com",
                password = "333333",
                profilePictureUrl = "https://picsum.photos/200?random=3",
                role = UserRole.ADMIN
            )
        )
        _users.value = users
    }
}
```

El ViewModel inicializa una lista de usuarios de ejemplo y la expone como un `StateFlow` para que la UI pueda observarla.

### 3. Crear la pantalla de detalles del usuario

Cree un nuevo composable en `features/users/detail` llamado `UserDetailScreen` que reciba el ID del usuario como parámetro y muestre los detalles del usuario. Una implementación básica podría ser la siguiente:

```kotlin
package com.example.demoapp.features.users.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun UserDetailScreen(
    userId: String // Recibe el ID del usuario como parámetro
) {
    // Un Box es un contenedor simple que ubica a sus hijos uno encima del otro.
    Box(modifier = Modifier.fillMaxSize()) {
        Text(text = "User Detail Screen $userId")
    }
}
```

Esta pantalla es muy simple, pero nos servirá para demostrar que la navegación con parámetros funciona correctamente.

### 4. Ejecutar la aplicación

Por ahora, la navegación no está implementada, pero puede ejecutar la aplicación para asegurarse de que no haya errores de compilación y que la pantalla de lista de usuarios se muestre correctamente. Asegúrese de que la pantalla `UserListScreen` esté configurada como la pantalla inicial en `MainActivity.kt`, de la siguiente manera:

```kotlin
setContent {
    DemoAppTheme {
        // Asegúrese de que UserListScreen sea la pantalla inicial
        UserListScreen(
            onNavigateToUserDetail = { userId ->
                // Aquí se implementará la navegación más adelante
            }
        )
    }
}
```

Si todo está correcto, debería ver la lista de usuarios en la pantalla al ejecutar la aplicación. Agregue más usuarios en el `UserViewModel` si desea ver más elementos en la lista y pruebe el desplazamiento (*scrolling*) en la lista.

---

## Actividad práctica

1.  **Mejorar la pantalla de detalles del usuario:**
    Mejore la pantalla `UserDetailScreen` para que muestre los detalles completos del usuario, incluyendo su nombre, correo electrónico, ciudad, dirección y foto de perfil. Utilice el `UserViewModel` para obtener los datos del usuario basado en el ID recibido como parámetro. Por ahora, para probar puede "quemar" el ID del usuario directamente en la llamada a `UserDetailScreen`.

2.  **Implementar ReportListScreen:**
    Implemente la pantalla `ReportListScreen` utilizando `LazyColumn` para mostrar una lista de reportes. Cada elemento de la lista debe mostrar el título y el estado del reporte. Cree un `ReportViewModel` similar al `UserViewModel` para manejar la lista de reportes. Si desea, puede investigar `Card` en Jetpack Compose para mejorar la presentación de cada elemento de la lista.

3.  **Implementar ReportDetailScreen:**
    Igualmente, implemente la pantalla `ReportDetailScreen` que muestre los detalles completos de un reporte seleccionado. Utilice el `ReportViewModel` para obtener los datos del reporte basado en el ID recibido como parámetro.

---

> 📚 **Recursos adicionales:**
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Layouts en Jetpack Compose](https://developer.android.com/jetpack/compose/layouts)
> - [Guías de desarrollo Android](https://developer.android.com/guide)