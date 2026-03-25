# Retrofit y Comunicación con APIs REST

## Introducción

En el desarrollo de aplicaciones móviles, es común que necesitemos comunicarnos con servicios web para obtener o enviar datos. Una forma popular de hacerlo en Android es mediante el uso de **Retrofit**, una biblioteca que facilita la interacción con APIs RESTful.

---

## ¿Qué es Retrofit?

Retrofit es una biblioteca de cliente HTTP para Android y Java desarrollada por Square. Proporciona una forma sencilla y eficiente de consumir APIs RESTful al convertir las llamadas HTTP en interfaces de Java/Kotlin. Retrofit maneja automáticamente la serialización y deserialización de datos, lo que simplifica el proceso de comunicación con servicios web.

> 📌 La documentación oficial de Retrofit se puede encontrar en el siguiente enlace: [Retrofit](https://square.github.io/retrofit/).

---

## Comparación con otras bibliotecas

Aunque existen diversas bibliotecas para realizar solicitudes HTTP en Android, como Volley y OkHttp, Retrofit se distingue por su facilidad de uso y por su estrecha integración con bibliotecas de serialización como Gson y Moshi. Una de sus principales ventajas es la posibilidad de definir las solicitudes HTTP mediante anotaciones, lo que permite escribir un código más declarativo, legible y fácil de mantener. Estas características hacen de Retrofit una opción especialmente adecuada para el consumo de APIs REST en aplicaciones Android modernas.

### Tabla comparativa de características clave

| Característica | Retrofit | Volley | OkHttp |
|---------------|----------|--------|--------|
| **Nivel de abstracción** | Alto | Medio | Bajo |
| **Propósito principal** | Consumo de APIs REST | Gestión de solicitudes HTTP y cache | Cliente HTTP |
| **Uso de anotaciones** | Sí | No | No |
| **Soporte para APIs REST** | Sí (orientado a REST) | Sí | Sí (manual) |
| **Serialización de datos** | Mediante conversores (Gson, Moshi, etc.) | Manual | Manual |
| **Integración directa con Gson** | Sí (con converter) | No | No |
| **Manejo de concurrencia** | Automático (con coroutines, RxJava, etc.) | Automático | Manual |
| **Manejo de cache** | No (requiere configuración adicional) | Sí (integrado) | Sí (configurable) |
| **Facilidad de uso** | Alta | Media | Baja |
| **Uso típico** | Apps modernas basadas en APIs REST | Apps con muchas peticiones pequeñas | Base para otras bibliotecas |

> 💡 Podemos decir que Retrofit abstrae a OkHttp para facilitar el consumo de servicios REST.

---

## Ejemplo Práctico

A continuación, se presenta un ejemplo práctico de cómo utilizar Retrofit para consumir una API REST en una aplicación Android.

### 1. Agregar Dependencias

Primero, agregamos las siguientes líneas en el archivo `libs.versions.toml`:

```toml
[versions]
retrofit = "3.0.0"

[libraries]
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
converter-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
```

Luego, en el archivo `build.gradle.kts` del módulo de la aplicación, agregamos las dependencias:

```kotlin
dependencies {
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
}
```

> ℹ️ Aunque en este ejemplo utilizamos Gson como convertidor, Retrofit es compatible con otros convertidores como Moshi, Jackson o Protobuf, lo que permite a los desarrolladores elegir la opción que mejor se adapte a sus necesidades.

### 2. Definir la Interfaz de la API

Creamos una interfaz que defina los endpoints de la API utilizando anotaciones de Retrofit:

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<User>
}
```

En esta interfaz, definimos un método `getUsers` que realiza una solicitud GET al endpoint `users` y devuelve una lista de objetos `User`. El uso de la palabra clave `suspend` indica que esta función es una función de corrutina, lo que permite realizar operaciones asíncronas de manera más sencilla.

> 🔹 Así como usamos `@GET` para solicitudes GET, Retrofit también proporciona otras anotaciones como `@POST`, `@PUT`, `@DELETE`, entre otras, para manejar diferentes tipos de solicitudes HTTP.

### 3. Crear el Cliente Retrofit

Configuramos Retrofit para crear una instancia del cliente API:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/") // Aquí va la URL base de la API
    .addConverterFactory(GsonConverterFactory.create()) // Usamos Gson para la serialización
    .build()

val apiService = retrofit.create(ApiService::class.java)
```

### 4. Realizar Solicitudes a la API

Utilizamos la instancia de `apiService` para realizar solicitudes a la API:

```kotlin
// Se lanza una corrutina para realizar la solicitud de manera asíncrona
GlobalScope.launch {
    val users = apiService.getUsers()
    users.forEach { user ->
        Log.d("User", "Name: ${user.name}, Age: ${user.age}")
    }
}
```

> ⚠️ Este es un ejemplo básico. En un entorno de producción, es recomendable manejar los errores y las respuestas de manera más robusta, así como utilizar un enfoque más estructurado para la gestión de corrutinas, como `ViewModel` y `StateFlow`.

### 5. Definir el Modelo de Datos

Creamos una clase de datos que represente la estructura de los datos recibidos de la API:

```kotlin
data class User(
    val id: Int,
    val name: String,
    val age: Int
)
```

---

## Conclusión

Retrofit es una herramienta poderosa y fácil de usar para consumir APIs RESTful en aplicaciones Android. Su integración con bibliotecas de serialización como Gson facilita el manejo de datos, y su enfoque basado en anotaciones hace que el código sea limpio y mantenible. Al utilizar Retrofit, los desarrolladores pueden centrarse en la lógica de la aplicación sin preocuparse por los detalles complejos de las solicitudes HTTP.

---
