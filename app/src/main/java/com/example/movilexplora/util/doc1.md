# Composables en Jetpack Compose


---

## Introducción

Un **"composable"** es una función en Jetpack Compose que define una parte de la interfaz de usuario (UI) de una aplicación Android. Estas funciones son anotadas con `@Composable` y permiten construir interfaces de usuario de manera **declarativa**, lo que significa que se describe cómo debería verse la UI en función del estado actual de los datos.

La idea es construir la UI utilizando pequeñas piezas reutilizables (composables) que pueden combinarse para formar interfaces más complejas. Esto facilita la creación, el mantenimiento y la actualización de la UI, ya que cada composable puede ser desarrollado y probado de manera independiente.

Dado que un composable es simplemente una función, puede aceptar parámetros y utilizar otras funciones composables dentro de su definición. Esto permite una gran flexibilidad y modularidad en el diseño de la UI.

---

## Composables básicos

Jetpack Compose proporciona varios composables básicos que podemos utilizar para construir nuestra UI. Algunos de los más comunes incluyen:

| Composable | Descripción |
|------------|-------------|
| `Text` | Muestra texto en la pantalla |
| `Button` | Crea un botón interactivo |
| `Column` | Organiza los elementos hijos en una columna vertical |
| `Row` | Organiza los elementos hijos en una fila horizontal |
| `Scaffold` | Proporciona una estructura básica para la pantalla, incluyendo barras de herramientas, menús y contenido principal |
| `Surface` | Proporciona un contenedor para otros composables con propiedades de estilo como color de fondo y elevación |
| `Box` | Un contenedor que permite apilar elementos unos sobre otros |
| `AlertDialog` | Muestra un cuadro de diálogo modal |
| `Image` | Muestra una imagen en la pantalla |
| `Icon` | Muestra un icono vectorial |
| `TextField` | Permite la entrada de texto por parte del usuario |

Estos composables pueden ser combinados y anidados para crear interfaces de usuario complejas y dinámicas. Además, cada composable recibe parámetros que permiten personalizar su apariencia y comportamiento.

---

## Text

El composable `Text` se utiliza para mostrar texto en la pantalla. Aquí hay un ejemplo simple de cómo usarlo:

```kotlin
Text(text = "Hello, Jetpack Compose!")
```

Este composable también puede aceptar parámetros adicionales para personalizar la apariencia del texto, como el tamaño de fuente, el color y el estilo. Por ejemplo:

```kotlin
Text(
    text = "Hello, Jetpack Compose!",
    fontSize = 24.sp,
    color = Color.Blue,
    fontWeight = FontWeight.Bold
)
```

> ⚠️ **Importante:** El tamaño de fuente se especifica en `sp` (scale-independent pixels), que es una unidad recomendada para el texto en Android.

El color y el peso de la fuente también se pueden personalizar utilizando las clases proporcionadas por Jetpack Compose, aunque es necesario importar los paquetes correspondientes para que el código funcione correctamente.

### Estilos tipográficos de Material Design

En lugar de definir tamaños y pesos manualmente, se recomienda usar el parámetro `style` con la escala tipográfica que proporciona Material Design 3. Esto garantiza consistencia visual con el sistema de diseño de la aplicación:

```kotlin
Text(
    text = "Título principal",
    style = MaterialTheme.typography.headlineLarge
)

Text(
    text = "Subtítulo",
    style = MaterialTheme.typography.titleMedium
)

Text(
    text = "Cuerpo de texto",
    style = MaterialTheme.typography.bodyLarge
)

Text(
    text = "Etiqueta pequeña",
    style = MaterialTheme.typography.labelSmall
)
```

**Material Design 3 organiza la tipografía en cinco grupos, cada uno con tres tamaños (Large, Medium, Small):**

| Grupo | Uso típico |
|-------|-----------|
| `display` | Textos muy grandes y decorativos |
| `headline` | Títulos de pantalla o sección |
| `title` | Encabezados de tarjetas o listas |
| `body` | Texto de contenido principal |
| `label` | Etiquetas, botones y textos pequeños |

> ⚠️ **Importante:** Al usar `MaterialTheme.typography`, los estilos se adaptan automáticamente al tema de la aplicación (claro u oscuro), lo que facilita mantener una apariencia coherente en toda la UI.

---

## Button

El composable `Button` se utiliza para crear un botón interactivo. Aquí hay un ejemplo de cómo usarlo:

```kotlin
Button(
    onClick = { /* Acción al hacer clic */ },
    content = {
        Text(text = "Hacer algo")
    }
)
```

El botón puede contener otros composables, como `Text`, para definir su contenido. El parámetro `onClick` define la acción que se ejecutará cuando el usuario haga clic en el botón.

Existen variantes del botón, como `OutlinedButton` y `TextButton`, que ofrecen diferentes estilos visuales.

### Personalización de botones

El parámetro `shape` permite cambiar la forma de los bordes del botón. Material Design 3 ofrece formas predefinidas que se pueden usar directamente:

```kotlin
Button(
    onClick = {},
    shape = RoundedCornerShape(8.dp) // Esquinas ligeramente redondeadas
) {
    Text("Hacer algo")
}
```

**Algunas formas comunes son:**

| Shape | Descripción |
|-------|-------------|
| `RoundedCornerShape(50%)` | Bordes completamente circulares (por defecto en M3) |
| `RoundedCornerShape(8.dp)` | Esquinas ligeramente redondeadas |
| `RectangleShape` | Sin redondeo, esquinas rectas |
| `CutCornerShape(8.dp)` | Esquinas cortadas en diagonal |

El color de fondo del botón se controla con el parámetro `colors`, usando `ButtonDefaults.buttonColors()`:

```kotlin
Button(
    onClick = {},
    colors = ButtonDefaults.buttonColors(
        containerColor = Color.DarkGray, // Color de fondo
        contentColor = Color.White // Color del contenido (texto e iconos)
    )
) {
    Text("Hacer algo")
}
```

> ⚠️ **Importante:** En Material Design 3 el fondo del botón se llama `containerColor`, no `backgroundColor`. Si no se especifica, toma automáticamente el color `primary` del tema de la aplicación.

Para más información sobre botones y sus variantes, se puede consultar la [documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose).

---

## Layouts básicos: Column, Row y Box

Los composables `Column`, `Row` y `Box` son fundamentales para organizar otros elementos en la pantalla. Cada uno tiene un propósito específico para la disposición de los elementos hijos.

### Box

El composable `Box` se utiliza para apilar elementos unos sobre otros. Es útil cuando se desea superponer elementos o crear diseños más complejos. Aquí hay un ejemplo de cómo usarlo:

```kotlin
Box {
    Text(text = "Texto de fondo")
    Text(
        text = "Texto superpuesto",
        modifier = Modifier.align(Alignment.Center) // Centra el texto superpuesto
    )
}
```

### Column y Row

Los composables `Column` y `Row` se utilizan para organizar otros composables en una disposición vertical u horizontal, respectivamente.

**Column** organiza los elementos en una columna vertical, es decir, uno debajo del otro:

```kotlin
Column {
    Text(text = "Elemento 1")
    Text(text = "Elemento 2")
    Text(text = "Elemento 3")
}
```

**Row** organiza los elementos en una fila horizontal, es decir, uno al lado del otro:

```kotlin
Row {
    Text(text = "Elemento A")
    Text(text = "Elemento B")
    Text(text = "Elemento C")
}
```

Adicionalmente, ambos composables pueden aceptar parámetros para personalizar la alineación, el espaciado y otros aspectos de la disposición de sus elementos hijos.

**Ejemplo: Agregar espaciado entre elementos en una Column**

```kotlin
Column(
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    Text(text = "Elemento 1")
    Text(text = "Elemento 2")
    Text(text = "Elemento 3")
}
```

> ⚠️ **Importante:** El espaciado se especifica en `dp` (density-independent pixels), que es una unidad recomendada para el diseño de interfaces en Android. No se recomienda usar píxeles absolutos ya que pueden variar entre diferentes dispositivos.

**Ejemplo: Alinear elementos en una Row al centro**

```kotlin
Row(
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(text = "Elemento A")
    Text(text = "Elemento B")
    Text(text = "Elemento C")
}
```

Tanto `Row` como `Column` son fundamentales para construir layouts en Jetpack Compose, ya que permiten organizar y estructurar la UI de manera flexible y sencilla.

> 💡 **Nota:** Existe una variación llamada `LazyColumn` y `LazyRow`, que son versiones optimizadas de `Column` y `Row` para listas largas de elementos. Estas versiones solo renderizan los elementos que están visibles en la pantalla, lo que mejora el rendimiento al manejar grandes conjuntos de datos.

---

## Image

El composable `Image` se utiliza para mostrar imágenes en la pantalla. Aquí hay un ejemplo de cómo usarlo:

```kotlin
Image(
    painter = painterResource(id = R.drawable.mi_imagen),
    contentDescription = "Descripción de la imagen"
)
```

- El parámetro `painter` se utiliza para cargar la imagen desde los recursos de la aplicación.
- `contentDescription` proporciona una descripción accesible de la imagen para usuarios con discapacidades visuales.
- `R.drawable.mi_imagen` es una referencia a una imagen almacenada en la carpeta `res/drawable` de su proyecto.

---

## Icon

El composable `Icon` se utiliza para mostrar iconos vectoriales en la pantalla:

```kotlin
Icon(
    imageVector = Icons.Default.Favorite,
    contentDescription = "Icono de favorito"
)
```

Android proporciona una colección de iconos prediseñados en la biblioteca `Icons.Default`, pero también es posible utilizar iconos personalizados cargándolos desde recursos vectoriales.

---

## TextField

El composable `TextField` se utiliza para permitir la entrada de texto por parte del usuario:

```kotlin
TextField(
    value = text,
    onValueChange = { /* Actualizar el valor del texto */ },
    label = { Text("Ingrese su nombre") }
)
```

- `value`: representa el texto actual en el campo de texto.
- `onValueChange`: es una función que se llama cada vez que el usuario modifica el texto; debe recomponer el composable con el nuevo valor.
- `label`: proporciona una etiqueta descriptiva para el campo de texto.

Existen variantes de `TextField`, como `OutlinedTextField`, que muestra un borde alrededor del campo de texto.

---

## Creación de un Composable

Un composable es una función anotada con `@Composable` que puede recibir parámetros y definir la UI utilizando otros composables. Un composable puede ser tan simple o complejo como sea necesario, y puede reutilizarse en diferentes partes de la aplicación si se define de manera adecuada.

Dado que un composable puede contener otros composables, es posible crear una jerarquía de composables para construir interfaces de usuario más complejas.

### Ejemplo: Pantalla inicial (Home Screen)

Vamos a crear un composable simple que represente una pantalla inicial con una imagen, un texto de bienvenida y un botón para comenzar:

```kotlin
@Composable
fun HomeScreen() {
    // Estructura de la pantalla inicial, sus hijos se organizan uno debajo del otro
    Column {
        // Carga una imagen desde los recursos de la aplicación (res/drawable)
        Image(
            painter = painterResource(R.drawable.welcome),
            contentDescription = "Welcome Image"
        )
        
        // Muestra un texto de bienvenida
        Text(text = "Pantalla de bienvenida")
        
        // Organiza los botones en una fila horizontal
        Row {
            Button(onClick = {
                // Acción al hacer clic en el botón de inicio de sesión
            }) {
                Text(text = "Iniciar sesión")
            }
            Button(onClick = {
                // Acción al hacer clic en el botón de registro
            }) {
                Text(text = "Crear una cuenta")
            }
        }
    }
}
```

**Importaciones necesarias:**

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.example.demoapp.R
```

**Configuración en MainActivity.kt:**

```kotlin
setContent {
    DemoAppTheme {
        HomeScreen()
    }
}
```

> ⚠️ **Importante:** Asegúrese de tener una imagen llamada `welcome.png` o `welcome.jpg` en la carpeta `res/drawable` de su proyecto para que el composable `Image` funcione correctamente.

### Mejora de apariencia con Modifiers

Para mejorar la apariencia de la pantalla inicial, podemos modificar el `Column` para centrar los elementos y agregar espacio entre ellos:

```kotlin
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(), // Ocupa todo el espacio disponible
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally // Centrado horizontal
    ) {
        Image(
            painter = painterResource(R.drawable.welcome),
            contentDescription = "Welcome Image"
        )
        Text(text = "Home Screen")
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically // Centrado vertical
        ) {
            Button(onClick = {
                // Acción al hacer clic en el botón de inicio de sesión
            }) {
                Text(text = "Iniciar sesión")
            }
            Button(onClick = {
                // Acción al hacer clic en el botón de registro
            }) {
                Text(text = "Crear una cuenta")
            }
        }
    }
}
```

**Importaciones adicionales necesarias:**

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
```

---

### Ejemplo: Formulario de inicio de sesión

Vamos a crear un composable que represente un formulario de inicio de sesión con campos para el nombre de usuario y la contraseña:

```kotlin
@Composable
fun LoginScreen() {
    Column {
        Text(text = "Email")
        TextField(
            value = "",
            onValueChange = {}
        )
        Text(text = "Password")
        TextField(
            value = "",
            onValueChange = {},
            visualTransformation = PasswordVisualTransformation()
        )
        Button(
            onClick = { /* Acción de inicio de sesión */ },
            content = {
                Text(text = "Iniciar sesión")
            }
        )
    }
}
```

**Importaciones necesarias:**

```kotlin
import androidx.compose.material3.TextField
import androidx.compose.ui.text.input.PasswordVisualTransformation
```

**Configuración en MainActivity.kt:**

```kotlin
setContent {
    DemoAppTheme {
        LoginScreen()
    }
}
```

> ⚠️ **Importante:** Aunque este formulario no tiene funcionalidad real (los campos no almacenan datos y el botón no realiza ninguna acción), este ejemplo ilustra cómo crear y utilizar composables personalizados en Jetpack Compose para construir interfaces de usuario.

---

## Arquitectura del proyecto

A medida que una aplicación crece en complejidad, es importante organizar el código de manera efectiva para facilitar su mantenimiento y escalabilidad. Una buena práctica es separar los composables en diferentes archivos y paquetes según su funcionalidad.

### Estructura recomendada de paquetes

```
com.example.demoapp/
├── core/
│   ├── component/          # Composables reutilizables en toda la app
│   ├── navigation/         # Navegación principal de la app
│   ├── theme/              # Temas y estilos de la app
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── utils/              # Utilidades y funciones comunes
├── data/
│   ├── model/              # Modelos de datos comunes (DTOs)
│   └── repository/         # Implementaciones de repositorios de datos
├── domain/
│   ├── model/              # Modelos de dominio (entidades)
│   └── repository/         # Interfaces de repositorios de dominio
├── features/
│   ├── home/
│   ├── login/
│   └── register/
└── MainActivity.kt         # Actividad principal de la app
```

### Separación de composables en archivos

1. Cree un archivo llamado `LoginScreen.kt` en el paquete `features/login` y mueva el composable `LoginScreen` a este archivo.

2. En `MainActivity.kt`, importe el composable:

```kotlin
import com.example.demoapp.features.login.LoginScreen
```

3. Haga lo mismo para `HomeScreen`, creando un archivo `HomeScreen.kt` en el paquete `features/home`.

---

## Modificadores

Todos los composables en Jetpack Compose pueden ser personalizados utilizando **modifiers**. Los modificadores son objetos que permiten cambiar la apariencia, el comportamiento y la disposición de los composables. Se aplican utilizando el parámetro `modifier`.

### Ejemplos de uso

**Agregar padding a un Text:**

```kotlin
Text(
    text = "Hello, Android Developer!",
    modifier = Modifier.padding(16.dp)
)
```

**Modificar tamaño y padding de un Button:**

```kotlin
Button(
    onClick = { /* Acción al hacer clic */ },
    modifier = Modifier
        .size(width = 200.dp, height = 60.dp)
        .padding(8.dp),
    content = {
        Text(text = "Hacer algo")
    }
)
```

### Modificadores comunes

| Modifier | Descripción |
|----------|-------------|
| `.background()` | Aplica un color de fondo |
| `.border()` | Agrega un borde al composable |
| `.clickable()` | Hace que el composable responda a clics |
| `.fillMaxWidth()` | Ocupa el ancho máximo disponible |
| `.fillMaxHeight()` | Ocupa el alto máximo disponible |
| `.fillMaxSize()` | Ocupa todo el espacio disponible |
| `.padding()` | Agrega espacio interior |
| `.size()` | Define dimensiones específicas |

> ⚠️ **Importante:** El orden de los modificadores puede afectar el resultado final. Por ejemplo, si primero aplica un `padding` y luego un `background`, el fondo se aplicará al área sin el padding; mientras que si aplica primero el `background` y luego el `padding`, el fondo cubrirá todo el área incluyendo el padding.

---

## Actividad práctica

### 1. Preview de Composables

Jetpack Compose proporciona una función de vista previa que permite ver cómo se verá un composable sin necesidad de ejecutar la aplicación en un emulador o dispositivo físico.

```kotlin
@Preview
@Composable
fun LoginScreenPreview() {
    DemoAppTheme {
        LoginScreen()
    }
}
```

Use `@Preview` en los composables `LoginScreen` y `HomeScreen` para ver cómo se ven en la ventana de vista previa de Android Studio.

### 2. Modifiers

Investigue y utilice al menos tres modificadores diferentes en el composable `LoginScreen` para mejorar su apariencia y disposición. Por ejemplo:
- Agregar un fondo con `.background()`
- Cambiar el tamaño de los campos de texto con `.fillMaxWidth()`
- Agregar bordes a los botones con `.border()`

### 3. Surface y Scaffold

Investigue acerca de los composables `Surface` y `Scaffold` en Jetpack Compose:
- ¿Para qué sirven?
- ¿Cómo se utilizan?
- ¿Cuáles son sus beneficios?

### 4. Revisar documentación oficial

Se recomienda revisar la documentación oficial de Jetpack Compose para conocer los diferentes composables disponibles:

- [Componentes en Jetpack Compose](https://developer.android.com/jetpack/compose/components)
- [Material Design Components en Jetpack Compose](https://developer.android.com/jetpack/compose/designsystems/material)

---

> 📚 **Recursos adicionales:**
> - [Documentación oficial de Jetpack Compose](https://developer.android.com/jetpack/compose)
> - [Guías de desarrollo Android](https://developer.android.com/guide)
> - [Material Design 3 Guidelines](https://m3.material.io/)