# Firebase Authentication

## Introducción

Como ya hemos visto, Firebase es una plataforma que ofrece una variedad de servicios para el desarrollo de aplicaciones móviles y web. Uno de los servicios más importantes es **Firebase Authentication**, que permite a los desarrolladores gestionar la autenticación de usuarios de manera sencilla y segura.

---

## ¿Qué es Firebase Authentication?

Firebase Authentication es un servicio que facilita la autenticación de usuarios en aplicaciones móviles y web. Proporciona una variedad de métodos de autenticación, incluyendo correo electrónico y contraseña, proveedores de identidad federada (como Google, Facebook, Twitter), y autenticación anónima.

La integración de Firebase Authentication en una aplicación permite a los desarrolladores centrarse en la funcionalidad de la aplicación sin preocuparse por la seguridad y la gestión de usuarios.

---

## Características principales

Algunas de las características principales de Firebase Authentication incluyen:

*   **Variedad de métodos de autenticación:** Soporta múltiples métodos de autenticación, incluyendo correo electrónico y contraseña, proveedores de identidad federada, y autenticación anónima.
*   **Seguridad:** Firebase Authentication utiliza prácticas de seguridad robustas para proteger la información de los usuarios.
*   **Integración con otros servicios de Firebase:** Se integra fácilmente con otros servicios de Firebase, como Firestore y Realtime Database.
*   **Gestión de usuarios:** Proporciona herramientas para gestionar usuarios, incluyendo la creación, eliminación y actualización de perfiles de usuario.
*   **Autenticación multiplataforma:** Soporta autenticación en aplicaciones móviles (iOS, Android) y web.
*   **Facilidad de uso:** La integración es sencilla y rápida, con SDKs disponibles para varias plataformas.
*   **Soporte para autenticación personalizada:** Permite la creación de sistemas de autenticación personalizados utilizando tokens JWT.

> 📌 La documentación oficial de Firebase Authentication se puede encontrar en: [Firebase Authentication Documentation](https://firebase.google.com/docs/auth).

---

## Integración de Firebase Authentication en una Aplicación Android

### 1. Configuración del Proyecto

Gracias a que Android Studio tiene integración directa con Firebase, podemos agregar Firebase Authentication a nuestro proyecto de manera sencilla.

1.  Abra su proyecto en Android Studio.
2.  Vaya a **Tools > Firebase** para abrir el asistente de Firebase.
3.  En el asistente de Firebase, seleccione **Authentication** y luego haga clic en **Email and password authentication**.
4.  Luego, dé clic en **"Add the Firebase Authentication SDK to your app"**. Esto agregará las dependencias necesarias a su archivo `build.gradle.kts`.

### 2. Configuración de Firebase Console

Vaya a **Firebase Console**, seleccione su proyecto y navegue a la sección de **Authentication**. En la pestaña de **Sign-in method**, habilite el método de autenticación por correo electrónico y contraseña.

### 3. Inyectar FirebaseAuth en el Proyecto

Modifique el archivo `FirebaseModule.kt` para incluir la instancia de `FirebaseAuth`. Agregue el siguiente código:

```kotlin
package com.example.demoapp.di

import com.google.firebase.auth.FirebaseAuth 
// otras importaciones

object FirebaseModule {
    // otras provisiones
    
    @Provides
    @Singleton
    fun provideAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
}
```

### 4. Ajustar el Repositorio de Autenticación

Modifique el archivo `UserRepositoryImpl.kt` que creamos en la guía anterior para utilizar Firebase Authentication tanto en el registro como en el inicio de sesión.

La función `save` debe crear un usuario en Firebase Authentication y luego guardar los datos del usuario en Firestore.

```kotlin
override suspend fun save(user: User) {
    // Crear usuario en Firebase Authentication
    val newUser = auth.createUserWithEmailAndPassword(user.email, user.password).await()
    val uid = newUser.user?.uid ?: throw Exception("Error al obtener el UID del usuario creado")
    
    // Se hace una copia del usuario con el UID generado por Firebase Authentication
    val userCopy = user.copy(
        id = uid,
        password = "" // No guardar la contraseña en Firestore
    )
    
    // Guardar los datos del usuario en Firestore
    collection
        .document(uid)
        .set(userCopy)
        .await()
}
```

Por otro lado, la función `login` debe autenticar al usuario utilizando Firebase Authentication y luego recuperar los datos del usuario desde Firestore.

```kotlin
override suspend fun login(email: String, password: String): User? {
    // Autenticar al usuario con Firebase Authentication
    val responseUser = auth.signInWithEmailAndPassword(email, password).await()
    
    // Obtener el UID del usuario autenticado
    val uid = responseUser.user?.uid ?: throw Exception("Usuario no encontrado")
    
    // Recuperar los datos del usuario desde Firestore
    return findById(uid)
}
```

### 5. Probar la Aplicación

Ejecute la aplicación y pruebe las funcionalidades de registro e inicio de sesión para asegurarse de que todo funcione correctamente con Firebase Authentication.

---

## Actividad práctica

1.  **Implementar Cierre de Sesión:**  
    Investigue y agregue la funcionalidad de cierre de sesión en la aplicación utilizando Firebase Authentication. Asegúrese de que al cerrar sesión, el usuario sea redirigido a la pantalla de inicio de sesión.

2.  **Verificación de Correo Electrónico:**  
    Investigue cómo implementar la verificación de correo electrónico en Firebase Authentication. Modifique el flujo de registro para enviar un correo de verificación al usuario después de registrarse y asegúrese de que el usuario no pueda iniciar sesión hasta que haya verificado su correo electrónico.

3.  **Recuperación de Contraseña:**  
    Investigue cómo implementar la funcionalidad de recuperación de contraseña utilizando Firebase Authentication. Agregue una opción en la pantalla de inicio de sesión para que los usuarios puedan solicitar un correo de restablecimiento de contraseña.

---

> 📄 *Documento de apoyo para el espacio académico: Construcción de Aplicaciones Móviles*  
> **Fuente:** Universidad del Quindío - Programa de Ingeniería de Sistemas y Computación  
> **Enlace de referencia:** https://caflorezvi.github.io/guias-apps-moviles/20.firebase-auth.html