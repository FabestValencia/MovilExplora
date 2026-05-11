# Firebase Firestore

## Introducción

Firebase es una plataforma de desarrollo de aplicaciones móviles y web proporcionada por Google. Ofrece una variedad de servicios, como autenticación, almacenamiento en la nube, análisis y bases de datos en tiempo real. Uno de los servicios más populares de Firebase es **Firestore**, una base de datos NoSQL flexible y escalable que permite almacenar y sincronizar datos para aplicaciones móviles y web.

---

## ¿Qué es Firestore?

Firestore es una base de datos NoSQL basada en documentos que permite a los desarrolladores almacenar, sincronizar y consultar datos para sus aplicaciones. A diferencia de las bases de datos relacionales tradicionales, Firestore utiliza una estructura de documentos y colecciones, lo que facilita la organización y el acceso a los datos.

Firestore ofrece varias características clave, como:

*   **Escalabilidad:** Firestore está diseñado para manejar grandes volúmenes de datos y tráfico, lo que lo hace adecuado para aplicaciones de cualquier tamaño.
*   **Sincronización en tiempo real:** Firestore permite la sincronización automática de datos entre clientes y la base de datos, lo que facilita la creación de aplicaciones colaborativas y en tiempo real.
*   **Consultas flexibles:** Firestore admite consultas avanzadas, como filtrado, ordenamiento y paginación, lo que permite a los desarrolladores recuperar datos de manera eficiente.
*   **Seguridad:** Firestore ofrece reglas de seguridad basadas en roles que permiten a los desarrolladores controlar el acceso a los datos de manera granular.
*   **Integración con Firebase:** Firestore se integra fácilmente con otros servicios de Firebase, como la autenticación y el almacenamiento en la nube, lo que facilita la creación de aplicaciones completas.
*   **Soporte multiplataforma:** Firestore es compatible con aplicaciones móviles (iOS, Android) y web, lo que permite a los desarrolladores crear aplicaciones multiplataforma con facilidad.

Gracias a estas características, Firestore se ha convertido en una opción popular para desarrolladores que buscan una solución de base de datos flexible y escalable para sus aplicaciones, además que permite un *"backendless development"*, es decir, desarrollar aplicaciones sin necesidad de gestionar un servidor backend propio.

> 📌 La documentación oficial de Firestore se puede encontrar en el siguiente enlace: [Firestore](https://firebase.google.com/docs/firestore).

---

## Integración de Firestore en una Aplicación Android

A continuación, se presenta un ejemplo práctico de cómo integrar Firestore en una aplicación Android.

### 1. Configuración del Proyecto

Android Studio tiene una integración directa con Firebase, lo que facilita la configuración del proyecto. Sigue estos pasos:

1.  Abra Android Studio y cargue el proyecto en el que estamos trabajando.
2.  Seleccione **"Tools"** en la barra de menú superior.
3.  Haga clic en **"Firebase"** para abrir el asistente de Firebase.
4.  Seleccione **"Cloud Firestore"** y luego haga clic en **"Get started with Cloud Firestore"**.
5.  En el siguiente paso, haga clic en **"Add the Cloud Firestore SDK to your app"**. Esto agregará automáticamente las dependencias necesarias a su archivo `build.gradle.kts`. Puede que tarde unos momentos en sincronizar el proyecto.

### 2. Verificar Dependencias

Asegúrese de que las siguientes dependencias estén presentes en su archivo `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.firebase.firestore)
}
```

Y el plugin de Google Services en el archivo `build.gradle.kts` a nivel de proyecto y del módulo:

```kotlin
plugins {
    alias(libs.plugins.google.gms.google.services)
}
```

> ℹ️ Tenga en cuenta que Android Studio agrega automáticamente las dependencias necesarias al usar el asistente de Firebase, adicionalmente, crea el archivo `google-services.json` en la carpeta `app/` que contiene la configuración del proyecto de Firebase.

### 3. Habilitar Firestore en Firebase Console

Vaya a **Firebase Console**, seleccione su proyecto y habilite Firestore en la sección **"Compilación"**. Al dar clic en **"Crear base de datos"**, elija el modo estándar, la ubicación que prefiera y seleccione comenzar en **modo de prueba** para facilitar el desarrollo inicial.

### 4. Crear un nuevo repositorio que maneje Firestore

Cree una nueva clase llamada `UserRepositoryImpl.kt` en el paquete `data.repository.remote` o modifique una clase existente para manejar las operaciones de Firestore:

```kotlin
package com.example.demoapp.data.repository.remote

import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore // Se inyecta una instancia de FirebaseFirestore
): UserRepository {

    // Definimos la colección de usuarios donde se almacenarán los datos
    private val collection = firestore.collection("users")

    // StateFlow para observar los cambios en la colección de usuarios
    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()

    init {
        // Escuchar cambios en tiempo real en la colección de usuarios
        collection.addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                // Se actualiza el StateFlow con la lista de usuarios mapeados desde los documentos
                _users.value = it.documents.mapNotNull { snap ->
                    snap.toObject(User::class.java)?.apply { id = snap.id }
                }
            }
        }
    }

    override suspend fun save(user: User) {
        // Agregar un nuevo documento a la colección de usuarios
        collection.add(user).await()
    }

    override suspend fun findById(id: String): User? {
        // Obtener el documento por ID
        val snapshot = collection.document(id).get().await()
        // Se retorna el objeto User si existe, se mapea el documento a un objeto User y se asigna el ID
        return snapshot.toObject(User::class.java)?.apply { this.id = snapshot.id }
    }

    override suspend fun login(email: String, password: String): User? {
        // Consultar la colección para encontrar un usuario con el email y password proporcionados
        val snapshot = collection
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .await()

        // Si no se encuentra ningún documento, retornar null
        if (snapshot.documents.isEmpty()) {
            return null
        }

        // Retornar el primer usuario encontrado, se mapea el documento a un objeto User y se asigna el ID
        return snapshot.documents.first().toObject(User::class.java)?.apply {
            id = snapshot.documents.first().id
        }
    }

    override suspend fun update(user: User) {
        // Se actualiza el documento existente dado su ID con los nuevos datos del usuario
        collection.document(user.id).set(user).await()
    }

    override suspend fun getAll(): List<User> {
        // Obtener todos los documentos de la colección de usuarios
        val snapshot = collection.get().await()
        // Mapear los documentos a objetos User, asignando el ID del documento de Firestore
        return snapshot.documents.mapNotNull { it.toObject(User::class.java)?.apply { id = it.id } }
    }
}
```

> ⚠️ Firebase Firestore maneja las operaciones de forma asíncrona, por lo que utilizamos la función `await()` para esperar a que las tareas se completen dentro de las funciones `suspend`.
>
> Además, Firestore obliga a que los modelos de datos tengan un constructor vacío, por lo que asegúrese de que la clase `User` tenga uno. Por ejemplo:

```kotlin
package com.example.demoapp.domain.model

data class User(
    var id: String = "",
    val name: String = "",
    val city: String = "",
    val address: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val profilePictureUrl: String = "",
    val role: UserRole = UserRole.USER
)
```

*Asignar valores predeterminados a las propiedades garantiza que el constructor vacío esté presente.*

### 5. Modificar el UserRepository

Dado que nos vamos a conectar a Firestore, debemos asegurarnos de que el repositorio tenga los métodos necesarios y que estos sean `suspend` para poder usarlos con corutinas:

```kotlin
package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val users: StateFlow<List<User>>
    suspend fun save(user: User)
    suspend fun findById(id: String): User?
    suspend fun login(email: String, password: String): User?
    suspend fun update(user: User)
    suspend fun getAll(): List<User>
}
```

### 6. Inyectar FirebaseFirestore en el Módulo de Dagger-Hilt

Cree un proveedor para `FirebaseFirestore` en el paquete `di`. Para ello, cree un archivo llamado `FirebaseModule.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.di

import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        // Proporciona una instancia singleton de FirebaseFirestore en toda la aplicación
        return FirebaseFirestore.getInstance()
    }
}
```

### 7. Configurar Repositorio en el Módulo de Dagger-Hilt

Modifique el módulo de Dagger-Hilt que ya creamos en clases anteriores para enlazar la implementación del repositorio con su interfaz. Abra el archivo `RepositoryModule.kt` y revise que tenga el siguiente contenido:

```kotlin
@Binds
@Singleton
abstract fun bindReportRepository(
    reportRepositoryImpl: ReportRepositoryImpl
): ReportRepository
```

*Asegúrese de importar la clase `UserRepositoryImpl` correcta que creamos anteriormente.*

### 8. Nuevo estado en RequestResult

Agregue un nuevo estado `Loading` en la clase sellada `RequestResult` para representar el estado de carga durante las operaciones asíncronas:

```kotlin
package com.example.demoapp.core.util

sealed class RequestResult {
    data class Success(val message: String): RequestResult()
    data class Failure(val errorMessage: String): RequestResult()
    object Loading : RequestResult() // Nuevo estado para representar la carga
}
```

### 9. Uso del Repositorio en los ViewModels

Dado que el repositorio tiene funciones `suspend`, asegúrese de llamarlas dentro de una corutina en los ViewModels. Aquí hay un ejemplo de cómo usar el repositorio en algunos ViewModels:

#### Login

Modifique la función `login` en el `LoginViewModel` para manejar la autenticación utilizando Firestore:

```kotlin
fun login() {
    if (isFormValid) {
        viewModelScope.launch {
            // Indicar que la operación está en curso
            _loginResult.value = RequestResult.Loading
            // Llamar a la función de login del repositorio
            val user = repository.login(email.value, password.value)
            if (user != null) {
                // Guardar sesión
                sessionDataStore.saveSession(user.id, user.role)
                _loginResult.value = RequestResult.Success(resources.getString(R.string.login_success))
            } else {
                _loginResult.value = RequestResult.Failure(resources.getString(R.string.login_failure))
            }
        }
    }
}
```

Ahora, en `LoginScreen`, ajuste el `LaunchedEffect` para manejar el nuevo estado `Loading`:

```kotlin
// Efecto para mostrar el snackbar cuando hay resultado
LaunchedEffect(loginResult) {
    loginResult?.let { result ->
        val message = when (result) {
            is RequestResult.Success -> result.message
            is RequestResult.Failure -> result.errorMessage
            is RequestResult.Loading -> "Cargando..."
        }
        snackbarHostState.showSnackbar(message)
        viewModel.resetLoginResult()
    }
}
```

#### Register

Modifique la función `register` en el `RegisterViewModel` para manejar el registro utilizando Firestore:

```kotlin
fun register() {
    if (isFormValid) {
        viewModelScope.launch {
            // Indicar que la operación está en curso
            _registerResult.value = RequestResult.Loading
            // Llamar a la función de apoyo para crear el usuario
            _registerResult.value = runCatching { create() }
                .fold(
                    onSuccess = { RequestResult.Success("Usuario registrado exitosamente") },
                    onFailure = {
                        RequestResult.Failure(
                            it.message ?: "Error registrando al usuario"
                        )
                    }
                )
        }
    }
}

// Función de apoyo para crear el usuario
suspend fun create() {
    repository.save(
        // Ya no es necesario generar un ID manualmente, Firestore lo hace automáticamente
        User(
            name = name.value,
            city = city.value,
            address = address.value,
            email = email.value,
            profilePictureUrl = profilePictureUrl.value,
            password = password.value
        )
    )
}
```

*En esta implementación se utiliza `runCatching` para manejar cualquier excepción que pueda ocurrir durante la creación del usuario y se actualiza el estado `_registerResult` en consecuencia.*

Ajuste `RegisterScreen` para manejar el nuevo estado `Loading`, similar a como se hizo en `LoginScreen`.

#### UserDetail

Ajuste el ViewModel `UserDetailViewModel` para cargar los detalles del usuario desde Firestore:

```kotlin
package com.example.demoapp.features.user.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.core.util.RequestResult
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val repository: UserRepository
): ViewModel() {

    // StateFlow para manejar el resultado de la solicitud
    private val _detailResult = MutableStateFlow<RequestResult?>(null)
    val detailResult: StateFlow<RequestResult?> = _detailResult.asStateFlow()

    // StateFlow para manejar el usuario actual
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Función para encontrar un usuario por su ID
    fun findById(userId: String) {
        viewModelScope.launch {
            _currentUser.value = null
            _detailResult.value = RequestResult.Loading
            _detailResult.value = runCatching {
                repository.findById(userId)?.also { _currentUser.value = it }
            }.fold(
                onSuccess = { RequestResult.Success("Usuario encontrado") },
                onFailure = { RequestResult.Failure(it.message ?: "Error obteniendo el usuario") }
            )
        }
    }
}
```

Modifique `UserDetailScreen` para manejar el nuevo estado `Loading`, similar a como se hizo en `LoginScreen` y `RegisterScreen`.

```kotlin
package com.example.demoapp.features.user.detail

@Composable
fun UserDetailScreen(
    padding: PaddingValues,
    userId: String,
    userViewModel: UserDetailViewModel = hiltViewModel(),
) {
    // Se observa el estado del usuario actual desde el ViewModel
    val user by userViewModel.currentUser.collectAsState()

    // Efecto para cargar los detalles del usuario cuando el userId cambia (o al iniciar la pantalla)
    LaunchedEffect(userId) {
        userViewModel.findById(userId)
    }
    // Resto del código...
}
```

### 10. Ajuste en los demás ViewModels y Screens

Asegúrese de revisar y ajustar cualquier otro ViewModel o Screen que interactúe con el `UserRepository` para asegurarse de que todas las llamadas a funciones `suspend` se realicen dentro de corutinas y manejen adecuadamente los estados de carga, éxito y error utilizando la clase `RequestResult`.

Cree un nuevo repositorio para manejar los reportes de usuarios si es necesario, siguiendo un patrón similar al del `UserRepositoryImpl`.

### 11. Ejecución de la Aplicación

Ejecute la aplicación en un emulador o dispositivo físico para probar la integración de Firestore. Asegúrese de que las operaciones de registro, inicio de sesión y gestión de usuarios funcionen correctamente con la base de datos Firestore.

Vaya a la consola de Firebase para verificar que los datos se estén almacenando correctamente en Firestore. En la sección de **"Firestore Database"**, debería poder ver los documentos y colecciones creados por la aplicación.

---

## Actividad Práctica

1.  **Investigar otras funciones de Firestore:** Investigue cómo implementar funciones adicionales de Firestore, como eliminar documentos, realizar consultas avanzadas y manejar errores de red.
2.  **Cache local con Firestore:** Investigue cómo habilitar y utilizar la caché local de Firestore para mejorar el rendimiento de la aplicación y permitir el acceso a los datos sin conexión.

---

> 📄 *Documento de apoyo para el espacio académico: Construcción de Aplicaciones Móviles*  
> **Fuente:** Universidad del Quindío - Programa de Ingeniería de Sistemas y Computación  
> **Enlace de referencia:** https://caflorezvi.github.io/guias-apps-moviles/19.firebase-firestore.html