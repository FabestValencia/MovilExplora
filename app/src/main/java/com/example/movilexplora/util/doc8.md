# Manejo de cadenas de texto en Android

## Introducción

El manejo de los strings o cadenas de texto es un aspecto neurálgico en el desarrollo de aplicaciones Android. Los strings se utilizan para mostrar información al usuario, como mensajes, etiquetas de botones, títulos y descripciones. Además, una correcta gestión de los strings es esencial para la internacionalización y localización de la aplicación, permitiendo que esta pueda adaptarse a diferentes idiomas y regiones.

Por tal motivo, Android proporciona un sistema robusto para manejar los strings a través de archivos de recursos, facilitando la organización y mantenimiento del texto en la aplicación. En esta guía, exploraremos cómo manejar los strings en Android, desde la definición y uso de recursos de strings hasta la implementación de la internacionalización y localización.

---

## Definición de Strings en Archivos de Recursos

En Android, los strings se definen en archivos XML ubicados en el directorio `res/values/`. El archivo más comúnmente utilizado para definir strings es `strings.xml`. Aquí es donde se almacenan todas las cadenas de texto que la aplicación utilizará.

Se recomienda definir todos los strings en este archivo para facilitar su gestión y permitir la traducción a otros idiomas. Por lo tanto, es una buena práctica evitar el uso de cadenas de texto "hardcoded" directamente en el código fuente.

---

## Uso de Strings en los componentes de la UI (Composables)

Para utilizar los strings definidos en `strings.xml` dentro de los componentes de la UI en Jetpack Compose, se utiliza la función `stringResource()`. Esta función permite acceder a los recursos de strings de manera sencilla y eficiente.

Aquí hay un ejemplo de cómo utilizar `stringResource()` en el composable `LoginScreen`:

```kotlin
package com.example.demoapp.features.login

// Los demás imports permanecen igual...
import androidx.compose.ui.res.stringResource
import com.example.demoapp.R

@Composable
fun LoginScreen(
    onNavigateToUsers: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    // Obtener el estado del resultado del login desde el ViewModel
    val loginResult by viewModel.loginResult.collectAsState()

    // Navegar a la pantalla de usuarios si el login fue exitoso
    LaunchedEffect(loginResult) {
        if (loginResult is RequestResult.Success) {
            viewModel.resetForm()
            onNavigateToUsers()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = CenterVertically)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.email.value,
            onValueChange = { viewModel.email.onChange(it) },
            label = {
                Text(text = stringResource(R.string.login_email_label)) // Uso de stringResource
            },
            isError = viewModel.email.error != null,
            supportingText = viewModel.email.error?.let { error ->
                { Text(text = error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewModel.password.value,
            onValueChange = { viewModel.password.onChange(it) },
            visualTransformation = PasswordVisualTransformation(),
            label = {
                Text(text = stringResource(R.string.login_password_label)) // Uso de stringResource
            },
            isError = viewModel.password.error != null,
            supportingText = viewModel.password.error?.let { error ->
                { Text(text = error) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Button(
            onClick = { viewModel.login() },
            enabled = viewModel.isFormValid,
            content = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.login_icon_description) // Uso de stringResource
                )
                Spacer(modifier = Modifier.width(width = 5.dp))
                Text(text = stringResource(R.string.login_button)) // Uso de stringResource
            }
        )

        loginResult?.let { result ->
            when (result) {
                is RequestResult.Success -> {
                    Text(text = result.message, color = Color.Green)
                }
                is RequestResult.Failure -> {
                    Text(text = result.errorMessage, color = Color.Red)
                }
            }
        }
    }
}
```

Ahora, dichos strings deben ser definidos en el archivo `strings.xml` de la siguiente manera:

```xml
<resources>
    <string name="app_name">DemoApp</string>
    
    <!-- Login Screen -->
    <string name="login_email_label">Email</string>
    <string name="login_password_label">Password</string>
    <string name="login_button">Iniciar sesión</string>
    <string name="login_icon_description">Icono de Login</string>
</resources>
```

Ejecute la aplicación y navegue a la pantalla de inicio de sesión para ver los strings en acción, todo debe seguir funcionando correctamente.

---

## Manejo de Strings en ViewModels

Usar `stringResource()` solo es posible dentro de composables, ya que depende del contexto de la composición para acceder a los recursos. Por lo tanto, en los ViewModels, no se puede utilizar `stringResource()` directamente debido a que los ViewModels no tienen acceso al contexto de la UI.

Esto puede presentar un desafío ya que un ViewModel a menudo necesita acceder a strings para manejar mensajes de error, validaciones y otros textos dinámicos.

Para manejar strings en los ViewModels, vamos a crear una interfaz llamada `ResourceProvider` que nos permitirá obtener strings desde el ViewModel sin depender del contexto de la UI. Implementaremos esta interfaz utilizando la inyección de dependencias con Hilt para proporcionar el contexto de la aplicación.

Cree un nuevo archivo Kotlin llamado `ResourceProvider.kt` en el paquete `core/utils` y agregue el siguiente código:

```kotlin
package com.example.demoapp.core.utils

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

// Interfaz para proporcionar acceso a los recursos de strings
interface ResourceProvider {
    fun getString(id: Int): String
}

class ResourceProviderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(id: Int): String {
        // Se obtiene el string a partir del ID de recurso utilizando el contexto de la aplicación
        return context.getString(id)
    }
}
```

En este código, hemos definido la interfaz `ResourceProvider` con un método `getString(id: Int): String` que permite obtener un string a partir de su ID de recurso. La clase `ResourceProviderImpl` implementa esta interfaz y utiliza el contexto de la aplicación para acceder a los recursos.

Ahora, es necesario proporcionar esta implementación a través de Hilt. Cree el archivo `AppModule.kt` en el paquete `di` y agregue el siguiente código para proporcionar una instancia de `ResourceProvider`:

```kotlin
package com.example.demoapp.di

import android.content.Context
import com.example.demoapp.core.utils.ResourceProvider
import com.example.demoapp.core.utils.ResourceProviderImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Provides
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideResourceProvider(
        @ApplicationContext context: Context
    ): ResourceProvider = ResourceProviderImpl(context)
}
```

Con esto, hemos configurado Hilt para proporcionar una instancia de `ResourceProviderImpl` siempre que se inyecte `ResourceProvider`. Ahora, podemos inyectar `ResourceProvider` en nuestros ViewModels para acceder a los strings.

Aquí hay un ejemplo de cómo hacerlo en el `LoginViewModel`:

```kotlin
package com.example.demoapp.features.login

// Los demás imports permanecen igual...
import com.example.demoapp.R
import com.example.demoapp.core.utils.ResourceProvider

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: UserRepository,
    private val resources: ResourceProvider // Inyección de ResourceProvider para acceder a los strings
) : ViewModel() {
    
    private val _loginResult = MutableStateFlow<RequestResult?>(null)
    val loginResult: StateFlow<RequestResult?> = _loginResult.asStateFlow()
    
    val email = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_email_empty) // Uso de ResourceProvider
            !Patterns.EMAIL_ADDRESS.matcher(value).matches() -> resources.getString(R.string.error_email_invalid)
            else -> null
        }
    }
    
    val password = ValidatedField("") { value ->
        when {
            value.isEmpty() -> resources.getString(R.string.error_password_empty) // Uso de ResourceProvider
            value.length < 6 -> resources.getString(R.string.error_password_short) // Uso de ResourceProvider
            else -> null
        }
    }
    
    val isFormValid: Boolean
        get() = email.isValid && password.isValid
    
    fun resetForm() {
        email.reset()
        password.reset()
        _loginResult.value = null
    }
    
    fun login() {
        if (isFormValid) {
            val user = repository.login(email.value, password.value)
            _loginResult.value = if (user != null) {
                RequestResult.Success(resources.getString(R.string.login_success)) // Uso de ResourceProvider
            } else {
                RequestResult.Failure(resources.getString(R.string.login_failure)) // Uso de ResourceProvider
            }
        }
    }
}
```

Gracias a la inyección de `ResourceProvider`, el `LoginViewModel` puede acceder a los strings definidos en `strings.xml` sin necesidad de depender del contexto de la UI. Esto lo hace más modular y fácil de probar.

Dado que hemos agregado nuevos strings para los mensajes de validaciones y resultados de login, debemos definirlos en el archivo `strings.xml` de la siguiente manera:

```xml
<resources>
    <!-- Otros strings permanecen igual... -->
    
    <!-- Login ViewModel -->
    <string name="error_email_empty">El email es obligatorio</string>
    <string name="error_email_invalid">Ingresa un email válido</string>
    <string name="error_password_empty">La contraseña es obligatoria</string>
    <string name="error_password_short">La contraseña debe tener al menos 6 caracteres</string>
    <string name="login_success">Login exitoso</string>
    <string name="login_failure">Credenciales inválidas</string>
</resources>
```

Ejecute la aplicación y pruebe el flujo de inicio de sesión para asegurarse de que los mensajes de error y éxito se muestren correctamente utilizando los strings definidos.

---

## Internacionalización y Localización

La internacionalización (i18n) y localización (l10n) son procesos esenciales para adaptar una aplicación a diferentes idiomas y regiones. Android facilita este proceso mediante el uso de archivos de recursos específicos para cada idioma.

Para agregar soporte para múltiples idiomas, se crean carpetas de recursos adicionales dentro del directorio `res/values/`, como `values-es/` para español, `values-fr/` para francés, etc.

El nombre de estas carpetas sigue el formato `values-<código de idioma>`, donde `<código de idioma>` es el código ISO 639-1 del idioma correspondiente. Además, se pueden incluir códigos de región para adaptarse a variaciones regionales, como `values-es-rMX/` para español de México o `values-en-rGB/` para inglés del Reino Unido.

El sistema operativo selecciona automáticamente el archivo de recursos adecuado según la configuración regional del dispositivo del usuario. Cada carpeta contendrá su propio archivo `strings.xml` con las traducciones correspondientes.

### Ejemplo: Soporte en Español

Por ejemplo, para agregar soporte en español, cree una carpeta llamada `values-es/` y dentro de ella un archivo `strings.xml` con las traducciones:

```xml
<resources>
    <string name="app_name">DemoApp</string>
    
    <!-- Login Screen -->
    <string name="login_email_label">Correo electrónico</string>
    <string name="login_password_label">Contraseña</string>
    <string name="login_button">Iniciar sesión</string>
    <string name="login_icon_description">Icono de inicio de sesión</string>
    
    <!-- Login ViewModel -->
    <string name="error_email_empty">El correo electrónico es obligatorio</string>
    <string name="error_email_invalid">Ingresa un correo electrónico válido</string>
    <string name="error_password_empty">La contraseña es obligatoria</string>
    <string name="error_password_short">La contraseña debe tener al menos 6 caracteres</string>
    <string name="login_success">Inicio de sesión exitoso</string>
    <string name="login_failure">Credenciales inválidas</string>
</resources>
```

### Idioma Predeterminado (Inglés)

Y deje el archivo `strings.xml` original en `values/` para el idioma predeterminado (por ejemplo, inglés):

```xml
<resources>
    <string name="app_name">DemoApp</string>
    
    <!-- Login Screen -->
    <string name="login_email_label">Email</string>
    <string name="login_password_label">Password</string>
    <string name="login_button">Login</string>
    <string name="login_icon_description">Login Icon</string>
    
    <!-- Login ViewModel -->
    <string name="error_email_empty">Email is required</string>
    <string name="error_email_invalid">Enter a valid email</string>
    <string name="error_password_empty">Password is required</string>
    <string name="error_password_short">Password must be at least 6 characters</string>
    <string name="login_success">Login successful</string>
    <string name="login_failure">Invalid credentials</string>
</resources>
```

Ejecute la aplicación y cambie el idioma del dispositivo a inglés para verificar que los strings se muestren correctamente en el nuevo idioma.

---

## Actividad práctica

1.  **Definir todos los strings:**
    Defina todos los strings utilizados en la aplicación en el archivo `strings.xml`, evitando el uso de cadenas de texto "hardcoded" en el código fuente tanto en los composables como en los ViewModels.

2.  **Agregar soporte para un segundo idioma:**
    De igual manera, agregue soporte para un segundo idioma (por ejemplo, inglés) creando las traducciones correspondientes en el archivo `strings.xml` dentro de una carpeta `values/`. Verifique que la aplicación muestre los strings correctos según la configuración regional del dispositivo.

---

> 📚 **Recursos adicionales:**
> - [Documentación oficial de Android - Strings](https://developer.android.com/guide/topics/resources/string-resource)
> - [Internacionalización en Android](https://developer.android.com/guide/topics/resources/internationalization)
> - [Jetpack Compose - Resources](https://developer.android.com/jetpack/compose/resources)