# Formularios en Jetpack Compose

Introducción

En la clase anterior, aprendimos a crear formularios básicos utilizando Jetpack Compose y a usar `ViewModel` para gestionar el estado y la lógica de validación. En esta clase, vamos a profundizar en la creación de formularios más avanzados, con otros composables como `DropdownMenu`, `DatePicker` y `AlertDialog`.

---

## DropdownMenu

Un `DropdownMenu` es un componente que permite a los usuarios seleccionar una opción de una lista desplegable. Es útil cuando hay múltiples opciones y se desea ahorrar espacio en la interfaz de usuario. Jetpack Compose proporciona un composable llamado `ExposedDropdownMenuBox` que facilita la creación de menús desplegables.

Por ejemplo, para crear un menú desplegable de selección de ciudad en un formulario de registro, podemos usar el siguiente código:

```kotlin
package com.example.demoapp.core.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class) // Algunos composables aún están en fase experimental
@Composable
fun DropdownMenu(
    value: String,
    label: String,
    supportingText: String? = null,
    list: List<String>,
    icon: ImageVector? = null,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true
) {
    // Estado interno para controlar si el menú está expandido o no
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            enabled = enabled,
            readOnly = true,
            value = value, // Muestra el valor seleccionado
            onValueChange = {},
            label = { Text(text = label) }, // Muestra la etiqueta del campo
            supportingText = supportingText?.let {
                { Text(text = supportingText) } // Muestra el texto de soporte si se proporciona
            },
            leadingIcon = icon?.let {
                { Icon(imageVector = icon, contentDescription = null) } // Muestra el icono si se proporciona
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            // Genera un elemento del menú para cada opción en la lista
            list.forEach {
                DropdownMenuItem(
                    text = { Text(text = it) },
                    onClick = {
                        onValueChange(it) // Actualiza el valor seleccionado al hacer clic
                        expanded = false // Cierra el menú desplegable
                    }
                )
            }
        }
    }
}
```

La idea es utilizar este `DropdownMenu` dentro de un formulario, gestionando su estado desde un `ViewModel`. Por lo tanto, cree este composable en el paquete `com.example.demoapp.core.components` de su proyecto.

Para hacer uso de este `DropdownMenu`, puede integrarlo en su formulario de registro de la siguiente manera:

```kotlin
@Composable
fun RegisterScreen(viewModel: RegisterViewModel = viewModel()) {
    // Resto del formulario...
    DropdownMenu(
        value = viewModel.city.value,
        onValueChange = { viewModel.city.onChange(it) },
        label = "Ciudad",
        icon = Icons.Default.Home,
        list = viewModel.cities,
    )
    // Resto del formulario...
}
```

Además, se debe ajustar el `ViewModel` para manejar el estado del campo de ciudad:

```kotlin
class RegisterViewModel : ViewModel() {
    // Resto del ViewModel...
    val cities = listOf("Ciudad 1", "Ciudad 2", "Ciudad 3")
    val city = ValidatedField("") { value ->
        if (value.isEmpty()) "Selecciona una ciudad" else null
    }
    // Resto del ViewModel...
}
```

Haga las modificaciones necesarias en su proyecto para integrar este `DropdownMenu` en el formulario de registro. Luego, ejecute la aplicación y verifique que el menú desplegable funcione correctamente.

---

## Alerta de Diálogo (AlertDialog)

Un `AlertDialog` es un cuadro de diálogo modal que presenta información importante o solicita una acción del usuario. Se utiliza comúnmente para confirmar acciones o mostrar mensajes críticos. Por ejemplo, en el formulario de registro, podemos usar un `AlertDialog` para controlar el envío del formulario.

Agregue la siguiente función composable para mostrar un `AlertDialog` de confirmación:

```kotlin
@Composable
fun ConfirmAlertDialog(
    onShowExitDialogChange: (Boolean) -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        title = { Text(text = "¿Está seguro de enviar los datos?") },
        text = { Text(text = "Está a punto de enviar los datos al servidor.") },
        onDismissRequest = { onShowExitDialogChange(false) },
        confirmButton = {
            TextButton(
                onClick = {
                    onShowExitDialogChange(false)
                    onConfirm()
                }
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onShowExitDialogChange(false) }
            ) {
                Text("Cerrar")
            }
        }
    )
}
```

Ahora, modifique el formulario para mostrar este diálogo cuando el usuario intente enviar los datos.

1.  **Agregar estado para controlar la visibilidad:**

    ```kotlin
    var showConfirmDialog by remember { mutableStateOf(false) }
    ```

2.  **Actualizar el botón de envío:**

    ```kotlin
    Button(
        onClick = {
            showConfirmDialog = true
        },
        enabled = viewModel.isFormValid,
        content = {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Icono de persona"
            )
            Spacer(modifier = Modifier.width(width = 5.dp))
            Text(text = "Register")
        }
    )
    ```

3.  **Agregar el `ConfirmAlertDialog` al final del formulario:**

    ```kotlin
    if (showConfirmDialog) {
        ConfirmAlertDialog(
            onShowExitDialogChange = { showConfirmDialog = it },
            onConfirm = {
                // Lógica para enviar los datos del formulario, por ahora solo un log
                Log.d("Register", "Email:${viewModel.email.value}, Password:${viewModel.password.value}")
            }
        )
    }
    ```

Ejecute la aplicación y pruebe el formulario. Al hacer clic en el botón de envío, debería aparecer el cuadro de diálogo de confirmación.

---

## DatePicker

Un `DatePicker` es un componente que permite a los usuarios seleccionar una fecha de un calendario. Es útil para formularios que requieren la entrada de fechas, como fechas de nacimiento o citas. Jetpack Compose tiene dos composables populares para seleccionar fechas: `DatePicker` y `DatePickerDialog`.

> 📚 **Nota:** Para más información sobre cómo integrar un `DatePicker`, puede consultar la documentación oficial de Material y la guía de Jetpack Compose.

---

## Iconos

Aunque Jetpack Compose incluye algunos iconos básicos en la biblioteca `androidx.compose.material:material-icons-core`, en algunos casos puede que necesitemos una gama más amplia de iconos para mejorar la experiencia del usuario en nuestros formularios.

Google ofrece una biblioteca llamada `material-icons-extended` que proporciona una amplia variedad de iconos adicionales basados en las directrices de Material Design.

### 1. Agregar la dependencia de iconos

Agregue lo siguiente en el archivo `libs.versions.toml`:

```toml
[versions]
material-icons-extended = "1.7.8"

[libraries]
material-icons-extended = { module = "androidx.compose.material:material-icons-extended", version.ref = "material-icons-extended" }
```

Luego, agregue la dependencia en el archivo `build.gradle.kts` de su módulo:

```kotlin
dependencies {
    implementation(libs.material.icons.extended)
}
```

Sincronice su proyecto para descargar la dependencia.

### 2. Usar iconos en los formularios

Para usar un icono en un campo de entrada, importe el icono deseado y utilícelo en el parámetro `leadingIcon` o `trailingIcon` del `OutlinedTextField`.

**Ejemplo: Icono en campo de texto**

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email

OutlinedTextField(
    value = viewModel.email.value,
    onValueChange = { viewModel.email.onChange(it) },
    label = { Text("Email") },
    leadingIcon = {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = "Icono de correo electrónico"
        )
    },
    // Otros parámetros...
)
```

**Ejemplo: Icono en botones**

```kotlin
Button(
    onClick = { viewModel.login() },
    enabled = viewModel.isFormValid,
    content = {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Icono de login"
        )
        Spacer(modifier = Modifier.width(width = 5.dp))
        Text(text = "Iniciar Sesión")
    }
)
```

Asegúrese de importar los iconos que desea utilizar desde `androidx.compose.material.icons.filled` o `androidx.compose.material.icons.outlined`, dependiendo del estilo que prefiera.

### 3. Lista de iconos disponibles

Puede explorar la lista completa de iconos disponibles en la documentación oficial de Material Icons. Allí encontrará iconos para diversas categorías y propósitos. Tenga en cuenta que los iconos vienen en diferentes estilos, como **Filled**, **Outlined**, **Rounded**, **TwoTone** y **Sharp**.

---

## Imágenes y recursos adicionales (Coil)

Supongamos que, al momento de registrar un usuario, queremos permitirle seleccionar una imagen de perfil. Jetpack Compose facilita la carga y visualización de imágenes utilizando la biblioteca **Coil**.

### 1. Agregar la dependencia de Coil

Agregue lo siguiente en su archivo `libs.versions.toml`:

```toml
[versions]
coil = "3.3.0"

[libraries]
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-okhttp = { module = "io.coil-kt.coil3:coil-network-okhttp", version.ref = "coil" }
```

Luego, agregue la dependencia en el archivo `build.gradle.kts` de su módulo:

```kotlin
dependencies {
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
```

### 2. Agregar permisos de Internet

Si va a cargar imágenes desde una URL, asegúrese de agregar el permiso de Internet en su archivo `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

Esto es necesario para que la aplicación pueda acceder a recursos en línea.

### 3. Cargar y mostrar una imagen

Para cargar y mostrar una imagen desde una URL, puede utilizar el composable `AsyncImage` proporcionado por Coil:

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun ProfileImage(url: String) {
    AsyncImage(
        contentScale = ContentScale.Crop,
        model = url,
        contentDescription = "Imagen de perfil",
        modifier = Modifier.size(100.dp) // Ajusta el tamaño según sea necesario
    )
}
```

`AsyncImage` maneja automáticamente la carga de la imagen, el almacenamiento en caché y la visualización. Sus principales parámetros son:

| Parámetro | Descripción |
|-----------|-------------|
| `model` | Aquí se especifica la URL de la imagen que se desea cargar. |
| `contentScale` | Define cómo se escala la imagen dentro del contenedor (ej. `ContentScale.Crop`, `ContentScale.Fit`). |
| `contentDescription` | Una descripción de la imagen para accesibilidad. |
| `modifier` | Permite ajustar el tamaño y otros aspectos visuales de la imagen. |

### 4. Personalización adicional

Puede personalizar aún más la carga de imágenes con Coil, como agregar un `placeholder` mientras se carga la imagen o manejar errores si la imagen no se puede cargar:

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(url) // URL de la imagen
        .crossfade(true) // Efecto de desvanecimiento al cargar
        .placeholder(R.drawable.placeholder) // Imagen de marcador de posición
        .error(R.drawable.error) // Imagen de error
        .build(),
    contentDescription = "Imagen de perfil",
    modifier = Modifier.size(100.dp)
)
```

Incluso, si se desea configurar la forma de la imagen (por ejemplo, hacerla circular), se puede utilizar el modificador `clip` junto con `RoundedCornerShape`:

```kotlin
modifier = Modifier
    .clip(RoundedCornerShape(16.dp))
    .width(95.dp)
    .height(95.dp)
```

---

> 📚 **Recursos adicionales:**
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Material Design Components](https://m3.material.io/)
> - [Coil Documentation](https://coil-kt.github.io/coil/)