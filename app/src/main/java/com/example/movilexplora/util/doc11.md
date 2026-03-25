# Persistencia de Datos en Android

> **Universidad del Quindío**  
> **Programa de Ingeniería de Sistemas y Computación**  
> **Título:** Persistencia de Datos en Android  
> **Docente:** Carlos Andrés Florez V.

---

## Introducción

En clases anteriores, estudiamos DataStore, una solución moderna para la persistencia de datos en Android. Pero ¿qué pasa si necesitamos almacenar datos más complejos, como objetos o listas de objetos? Aquí es donde entra en juego **Room**, una biblioteca de persistencia que proporciona una capa de abstracción sobre SQLite para permitir un acceso más robusto a la base de datos.

---

## SQLite

SQLite es una base de datos relacional ligera que viene integrada con Android. Permite almacenar datos estructurados en tablas y realizar consultas SQL para manipular esos datos. Sin embargo, trabajar directamente con SQLite puede ser tedioso y propenso a errores, especialmente cuando se trata de manejar esquemas de bases de datos y migraciones.

Internamente, SQLite utiliza un archivo de base de datos para almacenar los datos de la aplicación. Este archivo se encuentra en el sistema de archivos del dispositivo y es gestionado por el sistema operativo Android. Las aplicaciones pueden acceder a este archivo utilizando la API de SQLite proporcionada por Android.

Cabe resaltar que una aplicación móvil no tiene la responsabilidad de administrar datos complejos ya que esta responsabilidad recae en el backend. Sin embargo, existen casos en los que es necesario almacenar datos localmente, como cuando se desea ofrecer funcionalidad offline o mejorar el rendimiento al reducir las llamadas a la red.

> 📚 **Nota:** Para más información sobre SQLite en Android, puede consultar la documentación oficial: [SQLite Documentation](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase).

---

## Room

Room es una biblioteca de persistencia que simplifica el acceso a la base de datos SQLite en Android. Proporciona una capa de abstracción que permite definir entidades, DAOs (Data Access Objects) y bases de datos de manera más sencilla y segura. Gracias a Room, se simplifica la gestión de esquemas de tablas, consultas SQL y migraciones de bases de datos, ya que Room se encarga de generar el código necesario para interactuar con SQLite.

### Componentes Principales de Room

La arquitectura de Room se basa en tres componentes principales:

| Componente | Descripción |
|------------|-------------|
| **Entidades** | Son clases de datos que representan tablas en la base de datos. Cada entidad se anota con `@Entity` y define las columnas de la tabla mediante propiedades de la clase. |
| **DAOs (Data Access Objects)** | Son interfaces que definen los métodos para acceder a la base de datos. Los DAOs se anotan con `@Dao` y contienen métodos para insertar, actualizar, eliminar y consultar datos. |
| **Base de Datos** | Es una clase abstracta que extiende `RoomDatabase` y define la base de datos. Esta clase se anota con `@Database` y especifica las entidades y la versión de la base de datos. |

Se recomienda utilizar Room para la persistencia de datos en Android debido a su facilidad de uso, seguridad y eficiencia. Room proporciona una forma más estructurada y segura de interactuar con SQLite, lo que reduce la probabilidad de errores y mejora la calidad del código.

> 📚 **Nota:** La documentación oficial de Room se puede encontrar en el siguiente enlace: [Room Persistence Library](https://developer.android.com/training/data-storage/room).

---

## Ejemplo Práctico

A continuación, se presenta un ejemplo práctico de cómo utilizar Room para almacenar y recuperar datos en una aplicación Android.

### 1. Agregar dependencias

Primero, debemos agregar las dependencias de Room. En el archivo `libs.versions.toml`, agregamos las siguientes líneas:

```toml
[versions]
room="2.8.4"

[libraries]
room-runtime={ module="androidx.room:room-runtime", version.ref="room"}
room-compiler={ module="androidx.room:room-compiler", version.ref="room"}
room-ktx={ module="androidx.room:room-ktx", version.ref="room"}
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación, agregamos las dependencias:

```kotlin
dependencies {
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
```

### 2. Definir la Entidad

Creamos una clase de datos que represente una tabla en la base de datos. Por ejemplo, una entidad `User` con campos `id`, `name` y `age`. Esta clase se anota con `@Entity` y define las columnas de la tabla mediante propiedades de la clase.

Dado que las entidades suelen estar en un paquete separado, creamos un nuevo paquete llamado `data.local.entity` y dentro de este paquete creamos el archivo `UserEntity.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val age: Int
    // Otros campos que se deseen agregar...
)
```

### 3. Crear el DAO (Data Access Object)

Definimos una interfaz DAO que contenga las funciones para acceder a la base de datos. Estas funciones son `suspend` para permitir su uso con corrutinas ya que las operaciones de base de datos pueden ser lentas y no deben ejecutarse en el hilo principal.

Los DAOs suelen estar en un paquete separado, por lo que creamos un nuevo paquete llamado `data.local.dao` y dentro de este paquete creamos el archivo `UserDao.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.demoapp.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)
    
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>
}
```

### 4. Definir la Base de Datos

Creamos una clase abstracta que extienda `RoomDatabase` y defina la base de datos. A medida que agreguemos más entidades, las incluiremos en el array `entities`, así como los DAOs correspondientes.

La base de datos suele estar en un paquete separado, por lo que creamos un nuevo paquete llamado `data.local` y dentro de este paquete creamos el archivo `AppDatabase.kt` con el siguiente contenido:

```kotlin
package com.example.demoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.demoapp.data.local.dao.UserDao
import com.example.demoapp.data.local.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

### 5. Usar Room en la Aplicación

En nuestra aplicación, inicializamos la base de datos utilizando Room:

```kotlin
val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java,
    "app-database"
).build()
```

### 6. Usar el DAO

Finalmente, podemos usar el DAO para insertar y recuperar datos:

```kotlin
val userDao = db.userDao()
val newUser = UserEntity(id = 1, name = "John Doe", age = 30)
userDao.insert(newUser)
val users = userDao.getAll()
```

> ⚠ **Importante:** El código anterior muestra cómo insertar un nuevo usuario y recuperar todos los usuarios almacenados en la base de datos. Tenga en cuenta que estas operaciones deben realizarse en un hilo de fondo, por lo que es recomendable utilizar corrutinas o algún otro mecanismo de concurrencia.

---

## Integrar Room con una arquitectura más completa

Para una mejor arquitectura, es recomendable integrar Room con un patrón de repositorio y utilizar Hilt para la inyección de dependencias. Se recomienda seguir estos pasos adicionales:

### 1. Crear el Repositorio

Definimos una clase de repositorio que utilice el DAO para acceder a los datos:

```kotlin
package com.example.demoapp.data.repository

import com.example.demoapp.data.local.dao.UserDao
import com.example.demoapp.data.local.entity.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao // Se inyecta el DAO aquí
) {
    // Función para insertar un usuario, es una operación suspend
    suspend fun insert(user: UserEntity) {
        userDao.insert(user)
    }
    
    // Función para obtener todos los usuarios, es una operación suspend
    suspend fun getAll(): List<UserEntity> {
        return userDao.getAll()
    }
}
```

> ⚠ **Importante:** Tenga en cuenta que si el repositorio realiza operaciones suspendidas, las funciones que lo llamen también deben ser suspendidas o ejecutarse en un contexto adecuado (como una corrutina).

Si ya contamos con una capa de modelos de dominio, es recomendable mapear entre las entidades de Room y los modelos de dominio dentro del repositorio para mantener una separación clara entre las capas de datos y dominio.

Además, este repositorio puede ser la implementación de una interfaz que defina las operaciones disponibles, de esta manera, podemos cambiar la fuente de datos en el futuro sin afectar las capas superiores.

### 2. Configurar Hilt para la Inyección de Dependencias

Creamos un módulo de Hilt para proporcionar la instancia de la base de datos y el DAO. Esto asegura que las dependencias se gestionen correctamente, por lo tanto, no se debe llamar a `Room.databaseBuilder` directamente en la aplicación.

```kotlin
package com.example.demoapp.di

import android.content.Context
import androidx.room.Room
import com.example.demoapp.data.local.AppDatabase
import com.example.demoapp.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // Proporciona la instancia de la base de datos
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app-database"
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        // Se inicializa el DAO a partir de la base de datos
        return database.userDao()
    }
}
```

Cuando se ejecute la aplicación, Hilt se encargará de crear y proporcionar las instancias necesarias de `AppDatabase` y `UserDao` donde se requieran.

### 3. Usar el Repositorio en ViewModel

Finalmente, inyectamos el repositorio en nuestro ViewModel para acceder a los datos. Supongamos que tenemos un ViewModel llamado `UserViewModel`:

```kotlin
package com.example.demoapp.features.user.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.local.entity.UserEntity
import com.example.demoapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableLiveData
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository // Se inyecta el repositorio aquí
) : ViewModel() {
    
    fun insert(user: UserEntity) {
        // Se utiliza una corrutina para insertar el usuario ya que es una operación suspend
        viewModelScope.launch {
            userRepository.insert(user)
        }
    }
    
    fun getAll(): MutableLiveData<List<UserEntity>> {
        // Se crea un LiveData para observar los usuarios
        val usersLiveData = MutableLiveData<List<UserEntity>>()
        
        // Se utiliza una corrutina para obtener los usuarios ya que es una operación suspend
        viewModelScope.launch {
            val users = userRepository.getAll()
            usersLiveData.postValue(users)
        }
        
        return usersLiveData
    }
}
```

> ⚠ **Importante:** En el ejemplo anterior, ambas funciones usan `UserEntity` directamente. Sin embargo, en una arquitectura más limpia, es recomendable utilizar modelos de dominio separados para evitar acoplar la capa de presentación con la capa de datos.

Esta arquitectura busca que el DAO esté encapsulado dentro del repositorio, y que el ViewModel interactúe únicamente con el repositorio para obtener los datos. Esto mejora la separación de responsabilidades y facilita las pruebas unitarias.

---

## Conclusión

Room es una herramienta poderosa para la persistencia de datos en Android, que facilita el manejo de bases de datos SQLite. Al utilizar Room, podemos definir entidades, DAOs y bases de datos de manera más sencilla y segura, lo que mejora la calidad del código y reduce la probabilidad de errores. Aunque la persistencia local no siempre es necesaria en aplicaciones móviles, existen casos donde es fundamental para ofrecer una mejor experiencia al usuario.

---

## Actividad práctica

1.  **Implementar Room en el proyecto:**
    Integre Room en su proyecto actual siguiendo los pasos descritos en esta guía. Cree al menos una entidad, un DAO y la clase de base de datos.

2.  **Crear repositorio con Room:**
    Implemente un repositorio que utilice Room para almacenar y recuperar datos. Asegúrese de integrar Hilt para la inyección de dependencias.

3.  **Migración de datos:**
    Investigue cómo implementar migraciones de base de datos en Room cuando necesite cambiar el esquema de la base de datos en futuras versiones de la aplicación.

---

> 📚 **Recursos adicionales:**
> - [Room Persistence Library](https://developer.android.com/training/data-storage/room)
> - [SQLite Documentation](https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase)
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)