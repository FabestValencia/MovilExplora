# Arquitectura MVVM y Repositorios en Android

> **Universidad del Quindío**  
> **Programa de Ingeniería de Sistemas y Computación**  
> **Título:** Arquitectura MVVM y Repositorios en Android  
> **Docente:** Carlos Andrés Florez V.

---

## Introducción

En las clases anteriores, hemos explorado los conceptos fundamentales de la arquitectura **MVVM (Model-View-ViewModel)** y cómo implementarla en aplicaciones Android utilizando Jetpack Compose. En esta guía, profundizaremos en el uso de repositorios para gestionar la lógica de datos y mejorar la separación de responsabilidades dentro de nuestra aplicación.

La idea es crear una aplicación más escalable y mantenible, donde los ViewModels se encarguen únicamente de preparar los datos para la UI, mientras que los repositorios manejen la lógica de acceso a datos, ya sea desde una base de datos local, una API remota o cualquier otra fuente de datos.

Por este motivo, utilizaremos una arquitectura basada en capas:

- **Capa de Presentación**: Compuesta por las pantallas y los ViewModels.
- **Capa de Dominio**: Contiene las interfaces de los repositorios y los modelos de datos.
- **Capa de Datos**: Implementa los repositorios y maneja la lógica de acceso a datos.
- **Capa de Inyección de Dependencias**: Configura Hilt para proporcionar las dependencias necesarias. Esta es una capa transversal que facilita la gestión de dependencias en toda la aplicación.

Una arquitectura bien definida no solo mejora la organización del código, sino que también facilita las pruebas unitarias y la evolución de la aplicación a lo largo del tiempo.

---

## Conceptos Clave

Antes de comenzar con la implementación, es importante entender algunos conceptos clave:

### ViewModel

Los ViewModels son componentes clave en la arquitectura MVVM. Actúan como intermediarios entre la vista (UI) y el modelo (datos). Su principal responsabilidad es preparar y gestionar los datos para la UI, asegurando que la lógica de negocio esté separada de la presentación. Ya hemos trabajado con ViewModels en las clases anteriores, pero ahora los integraremos con repositorios para una mejor gestión de datos.

### Repositorios

Los repositorios son una capa adicional que se sitúa entre el ViewModel y las fuentes de datos (como bases de datos locales, servicios web, etc.). Su función principal es abstraer la lógica de acceso a datos, proporcionando una interfaz limpia para que el ViewModel pueda interactuar con los datos sin preocuparse por los detalles de implementación.

Se dividen en dos partes:

1. **Interfaz del Repositorio**: Define las operaciones que el repositorio debe implementar, como obtener datos, guardar datos, etc. Esta interfaz se encuentra en la capa de dominio.

2. **Implementación del Repositorio**: Es la clase concreta que implementa la interfaz del repositorio y maneja la lógica de acceso a datos. Esta clase se encuentra en la capa de datos. Pueden haber múltiples implementaciones de un mismo repositorio, por ejemplo, una para datos locales y otra para datos remotos.

### Hilt en Jetpack Compose

Hilt es una biblioteca de inyección de dependencias para Android que facilita la gestión de dependencias en aplicaciones. En el contexto de Jetpack Compose, Hilt permite inyectar ViewModels y repositorios de manera sencilla, promoviendo un código más limpio y modular.

Básicamente, Hilt se encarga de crear y proporcionar las instancias necesarias de los ViewModels y repositorios cuando se requieren, eliminando la necesidad de instanciarlos manualmente.

> 📚 **Nota:** Para más información sobre Hilt, puede consultar la documentación oficial: [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android).

### KSP (Kotlin Symbol Processing)

KSP es una herramienta de procesamiento de símbolos para Kotlin que permite generar código a partir de anotaciones. A diferencia de Kapt, que se basa en la generación de código Java, KSP trabaja directamente con el código Kotlin, lo que puede resultar en un rendimiento mejorado y una integración más fluida con proyectos Kotlin.

KSP es compatible con bibliotecas como Hilt, permitiendo la inyección de dependencias en aplicaciones Android escritas en Kotlin. Al utilizar KSP, los desarrolladores pueden aprovechar las ventajas de la generación de código optimizada para Kotlin, facilitando la gestión de dependencias y facilitando la integración con otras bibliotecas y herramientas del ecosistema Kotlin.

> 📚 **Nota:** Para más información sobre KSP, puede consultar la documentación oficial: [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html).

---

## Diagrama de la arquitectura

Con el fin de visualizar mejor la arquitectura que vamos a implementar, aquí se muestra un diagrama que representa las diferentes capas y cómo interactúan entre sí:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Capa de presentación                          │
├──────────────┬──────────────┬──────────────┬──────────────────┤
│  Screen A    │  Screen B    │  Screen C    │  Screen ...      │
│ Composable·UI│ Composable·UI│ Composable·UI│ Composable·UI    │
│      ↓       │      ↓       │      ↓       │        ↓         │
│  ViewModel A │  ViewModel B │  ViewModel C │  ViewModel ...   │
│@HiltViewModel│@HiltViewModel│@HiltViewModel│ @HiltViewModel   │
└──────┬───────┴─────────────┴──────┬───────┴────────┬─────────┘
       │              │              │                │
       └──────────────┴──────────────┴────────────────┘
                          │
                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Capa de dominio                             │
├──────────────────┬──────────────────┬──────────────────────────┤
│ «interface» Repo A│ «interface» Repo B│   Modelos de datos      │
│StateFlow<List<   │StateFlow<List<   │  EntityA, EntityB ...    │
│  Entity>>        │  Entity>>        │  data classes Kotlin     │
│save(), findById()│save(), findById()│                          │
│operaciones del   │operaciones del   │                          │
│  dominio         │  dominio         │                          │
└────────┬─────────┴────────┬───────────────────────────────────┘
         │                  │
         ↓                  ↓
┌─────────────────────────────────────────────────────────────────┐
│                       Capa de datos                              │
├──────────────────┬──────────────────┬──────────────────────────┤
│   RepoImpl A     │   RepoImpl B     │    Fuente de datos       │
│ @Singleton       │ @Singleton       │  Memoria · Room · API    │
│ @Inject          │ @Inject          │                          │
└─────────────────┴─────────────────┴──────────────────────────┘
         │                  │
         └─────────────────┘
                  ↓
┌─────────────────────────────────────────────────────────────────┐
│        Inyección de dependencias · Hilt (capa transversal)      │
├──────────────────┬──────────────────┬──────────────────────────┤
│ @HiltAndroidApp  │ @Module · @Binds │         KSP              │
│                  │                  │  @AndroidEntryPoint      │
└──────────────────┴──────────────────┴──────────────────────────┘

Leyenda:
→ Uso / llamada
---> Implementa / inyecta
```

---

## Integración en el proyecto

Abra su proyecto de Android Studio donde ha estado trabajando en las clases anteriores y siga los siguientes pasos para integrar Hilt y configurar los repositorios.

### 1. Agregar dependencias

Agregue lo siguiente en el archivo `libs.versions.toml` para incluir Hilt y KSP en su proyecto:

```toml
[versions]
hiltAndroid="2.57.2"
hiltNavigationCompose="1.3.0"
ksp="2.1.21-2.0.1" # Revisar la compatibilidad con la versión de Kotlin utilizada

[libraries]
androidx-hilt-navigation-compose={ module="androidx.hilt:hilt-navigation-compose", version.ref="hiltNavigationCompose" }
hilt-android={ module="com.google.dagger:hilt-android", version.ref="hiltAndroid" }
hilt-compiler={ module="com.google.dagger:hilt-compiler", version.ref="hiltAndroid" }

[plugins]
hilt-android={ id="com.google.dagger.hilt.android", version.ref="hiltAndroid" }
devtools-ksp={ id="com.google.devtools.ksp", version.ref="ksp" }
```

Ahora, en el archivo `build.gradle` del proyecto, aplique el plugin de Hilt:

```kotlin
plugins {
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
}
```

Por último, en el archivo `build.gradle` del módulo de la aplicación, agregue las siguientes dependencias:

```kotlin
plugins {
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
}

dependencies {
    //--- Hilt Core ---
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    
    //--- Hilt + Compose Navigation ---
    implementation(libs.androidx.hilt.navigation.compose)
}
```

> ⚠️ **Importante:** Recuerde no borrar las dependencias que ya tiene en su proyecto. Solo agregue las nuevas. Asegúrese de sincronizar el proyecto después de agregar las dependencias para que se descarguen correctamente.

### 2. Configurar Hilt en la aplicación

Cree una clase que extienda de `Application` y anótela con `@HiltAndroidApp`. Esto inicializa Hilt en su aplicación. Por ejemplo, cree un archivo llamado `MyApp.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application()
```

### 3. Configurar el AndroidManifest.xml

En el archivo `AndroidManifest.xml`, asegúrese de declarar la clase de aplicación que acaba de crear:

```xml
<application
    android:name=".MyApp"
    ...>
    ...
</application>
```

De esta manera, Android sabrá que debe usar esta clase como la aplicación principal y Hilt podrá inicializarse correctamente.

### 4. Configurar la actividad principal

En su actividad principal (por ejemplo, `MainActivity`), agregue la anotación `@AndroidEntryPoint` para habilitar la inyección de dependencias en esta clase.

### 5. Crear la interfaz del Repositorio de usuarios

En el paquete `domain/repository`, cree una nueva interfaz llamada `UserRepository` con el siguiente contenido:

```kotlin
package com.example.demoapp.domain.repository

import com.example.demoapp.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val users: StateFlow<List<User>>
    fun save(user: User)
    fun findById(id: String): User?
    fun login(email: String, password: String): User?
}
```

La idea es definir las operaciones que el repositorio debe implementar para gestionar los datos de los usuarios, pero sin preocuparse por los detalles de implementación.

### 6. Crear el Repositorio para gestionar los datos de usuarios

En el paquete `data/repository`, cree una clase llamada `UserRepositoryImpl` que implemente la interfaz `UserRepository`. Esta clase manejará la lógica de datos de los usuarios. Actualice el archivo `UserRepositoryImpl.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.data.repository

import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.model.UserRole
import com.example.demoapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Anotamos la clase como Singleton para que Hilt gestione una única instancia
class UserRepositoryImpl @Inject constructor() : UserRepository { // Implementamos la interfaz
    
    // Usamos StateFlow para manejar la lista de usuarios de manera reactiva
    private val _users = MutableStateFlow<List<User>>(emptyList())
    override val users: StateFlow<List<User>> = _users.asStateFlow()
    
    init {
        _users.value = fetchUsers()
    }
    
    override fun save(user: User) {
        _users.value += user
    }
    
    override fun findById(id: String): User? {
        return _users.value.firstOrNull { it.id == id }
    }
    
    override fun login(email: String, password: String): User? {
        return _users.value.firstOrNull { it.email == email && it.password == password }
    }
    
    private fun fetchUsers(): List<User> {
        return listOf(
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
    }
}
```

La idea es que el repositorio maneje toda la lógica relacionada con los datos de los usuarios, como guardar nuevos usuarios, buscar usuarios por ID y manejar el inicio de sesión. Además, esta clase implementa un patrón Singleton para asegurar que solo exista una instancia del repositorio en toda la aplicación.

### 7. Modificar los ViewModels para usar el Repositorio

Modifique el `UserListViewModel` para que utilice el `UserRepository` en lugar de manejar directamente la lista de usuarios. Actualice el archivo `UserListViewModel.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.features.user.list

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel // Anotamos el ViewModel con @HiltViewModel para que Hilt pueda inyectarlo
class UserListViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    // Exponemos la lista de usuarios desde el repositorio como un StateFlow para que la UI pueda observarla
    val users: StateFlow<List<User>> = repository.users
}
```

Ajuste o cree el `UserDetailViewModel` para que también utilice el `UserRepository` para obtener los detalles de un usuario específico. Actualice el archivo `UserDetailViewModel.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.features.user.detail

import androidx.lifecycle.ViewModel
import com.example.demoapp.domain.model.User
import com.example.demoapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {
    
    fun findById(userId: String): User? {
        return repository.findById(userId)
    }
}
```

Así mismo, modifique el `LoginViewModel` y el `RegisterViewModel` para que utilicen el `UserRepository` para manejar el inicio de sesión y el registro de usuarios, respectivamente.

Dado que todos los ViewModels ahora dependen del `UserRepository`, Hilt se encargará de inyectar automáticamente la instancia del repositorio cuando se creen los ViewModels, de esta manera, los datos estarán centralizados y gestionados de manera eficiente.

### 8. Configurar la inyección de dependencias para el Repositorio

Dado que `UserRepositoryImpl` es la implementación concreta de la interfaz `UserRepository`, necesitamos indicarle a Hilt cómo proporcionar esta implementación cuando se solicite una instancia de `UserRepository`.

Cree un módulo de Hilt para definir esta vinculación. Cree un nuevo paquete llamado `di` (inyección de dependencias) y dentro de este paquete, cree un archivo llamado `RepositoryModule.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.di

import com.example.demoapp.data.repository.UserRepositoryImpl
import com.example.demoapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds // Indica a Hilt que esta función vincula una implementación a una interfaz
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository // Vincula UserRepositoryImpl con UserRepository
}
```

Este módulo es clave, ya que si no se crea, Hilt no sabrá cómo proporcionar una instancia de `UserRepository` cuando se inyecte en los ViewModels.

### 9. Inyectar el ViewModel en las pantallas

Finalmente, modifique las pantallas para inyectar los ViewModels utilizando Hilt. Por ejemplo, en la pantalla de lista de usuarios (`UserListScreen`), inyecte el `UserViewModel` de la siguiente manera:

```kotlin
package com.example.demoapp.features.user.list

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UserListScreen(
    onNavigateToUserDetail: (String) -> Unit,
    userViewModel: UserListViewModel = hiltViewModel()
) {
    // Resto del código...
}
```

De esta manera, el `UserListViewModel` se inyectará automáticamente en la pantalla utilizando Hilt, y podrá acceder a los datos gestionados por el `UserRepository`.

Ajuste también la pantalla de detalles del usuario (`UserDetailScreen`) para inyectar el `UserDetailViewModel` de manera similar:

```kotlin
package com.example.demoapp.features.user.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun UserDetailScreen(
    padding: PaddingValues,
    userId: String,
    userViewModel: UserDetailViewModel = hiltViewModel(),
) {
    // Resto del código ...
}
```

Cambie las demás pantallas de manera similar para inyectar los ViewModels correspondientes usando `hiltViewModel()`.

### 10. Pruebe la aplicación

Ejecute la aplicación para asegurarse de que todo funcione correctamente con la nueva arquitectura MVVM y el uso de repositorios. Verifique que las funcionalidades de registro, inicio de sesión y visualización de detalles del usuario funcionen como se espera.

Por ahora, los datos se gestionan en memoria a través del `UserRepository`. En futuras guías, exploraremos cómo integrar fuentes de datos persistentes, como bases de datos locales o servicios web, para mejorar aún más la gestión de datos en la aplicación.

---

## Actividad práctica

### 1. Pantalla de login

Ajuste la pantalla de login para que utilice el `LoginViewModel` con el `UserRepository` para autenticar a los usuarios. Asegúrese de que al iniciar sesión, se verifique la existencia del usuario en el repositorio y se asigne el estado de login correctamente (éxito o fallo).

Para que esto funcione, modifique el `LoginViewModel` para que utilice el `UserRepository` y verifique las credenciales del usuario al iniciar sesión, de la siguiente manera:

```kotlin
fun login() {
    if (isFormValid) {
        // Se llama la función de login del repositorio
        val user = repository.login(email.value, password.value)
        
        // Actualizamos el estado del resultado del login
        _loginResult.value = if (user != null) {
            RequestResult.Success("Login exitoso")
        } else {
            RequestResult.Failure("Credenciales inválidas")
        }
    }
}
```

Dado que `LoginScreen` ya está configurada para observar el estado del login a través de un `LaunchedEffect`, al actualizar `loginResult` en el ViewModel, la pantalla reaccionará automáticamente a los cambios y navegará a la pantalla de la lista de usuarios si el login es exitoso.

Pruebe la pantalla de login para asegurarse de que el inicio de sesión funcione correctamente utilizando el repositorio con datos correctos e incorrectos.

### 2. Pantalla de registro

Ajuste la pantalla de registro para que utilice el `RegisterViewModel` con el `UserRepository` para registrar nuevos usuarios. Puede modificar el método `register` en el `RegisterViewModel` de la siguiente manera:

```kotlin
fun register() {
    if (isFormValid) {
        val newUser = User(
            id = UUID.randomUUID().toString(), // Genera un ID único para el nuevo usuario
            name = name.value,
            city = city.value,
            address = address.value,
            email = email.value,
            password = password.value,
            profilePictureUrl = profilePictureUrl.value
        )
        repository.save(newUser)
        _registerResult.value = RequestResult.Success("Registro exitoso")
    }
}
```

Asegúrese de que al registrar un nuevo usuario, se guarde en el repositorio y se navegue a la pantalla de login (solo si el registro es exitoso). Use el mismo enfoque que en la pantalla de login, utilizando un `LaunchedEffect` para observar el resultado del registro y navegar a la pantalla de login si el registro es exitoso.

### 3. Pantallas de los reportes

Ajuste las pantallas de los reportes para que utilicen los ViewModels correspondientes con el `ReportRepository` para obtener y mostrar los datos necesarios. Asegúrese de que las pantallas reaccionen a los cambios en los datos gestionados por el repositorio. Recuerde usar Hilt para inyectar los ViewModels en las pantallas.

---

> 📚 **Recursos adicionales:**
> - [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
> - [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)