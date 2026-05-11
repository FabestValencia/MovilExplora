# Mapas y Geolocalización en Android

> **Universidad del Quindío**  
> **Programa de Ingeniería de Sistemas y Computación**  
> **Título:** Mapas y Geolocalización en Android  
> **Docente:** Carlos Andrés Florez V.

---

## Introducción

El acceso al servicio de mapas y la geolocalización es una característica común en muchas aplicaciones móviles modernas. Todos los dispositivos cuentan con un sistema de posicionamiento global (GPS) que permite determinar la ubicación geográfica del dispositivo, además, los servicios de mapas proporcionan una representación visual de la ubicación y permiten la interacción con mapas, como la visualización de rutas, puntos de interés y más.

---

## Librerías de mapas

Algunas de las librerías de mapas más populares para Android incluyen:

*   **Google Maps SDK para Android**: La librería oficial de Google para integrar mapas en aplicaciones Android. Proporciona una amplia gama de funcionalidades, como la visualización de mapas, marcadores, rutas y capas personalizadas.
*   **Mapbox**: Una plataforma de mapas que ofrece una SDK para Android con características avanzadas, como mapas personalizables, navegación y análisis de ubicación.
*   **OpenStreetMap (OSM)**: Una alternativa de código abierto a Google Maps. Hay varias librerías disponibles para integrar OSM en aplicaciones Android, como osmdroid y Mapsforge.

Aunque todas estas librerías son útiles, en esta guía nos enfocaremos en el uso de **Mapbox** debido a su flexibilidad, personalización y uso gratuito más amplio que Google Maps.

> 📚 **Nota:** La documentación oficial de Mapbox para Android se puede encontrar en el siguiente enlace: [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps/guides/).

---

## Configuración de Mapbox en Android

Para comenzar a usar Mapbox en nuestro proyecto de Android, tenemos que seguir estos pasos:

### 1. Crear una cuenta en Mapbox

Crear una cuenta en Mapbox y obtener una clave de API (Access Token) que se utilizará para autenticar las solicitudes a los servicios de Mapbox. Una ventaja de Mapbox es que ofrece un plan gratuito con un límite generoso de uso mensual, lo que lo hace accesible para desarrolladores y proyectos pequeños, además no requiere una tarjeta de crédito para registrarse.

> ⚠ **Importante:** Cuando cree la cuenta asegúrese de elegir el plan **Individual**, no el plan Business. Además de usar su correo institucional para registrarse.

### 2. Agregar dependencias de Mapbox

Agregar las librerías necesarias de Mapbox en el archivo `libs.versions.toml`:

```toml
[versions]
mapsCompose="11.11.0"

[libraries]
maps-android={ module="com.mapbox.maps:android", version.ref="mapsCompose"}
maps-compose={ module="com.mapbox.extension:maps-compose", version.ref="mapsCompose"}
```

Luego, en el archivo `settings.gradle.kts`, asegurarse de incluir el repositorio de Mapbox:

```kotlin
dependencyResolutionManagement{
    ...
    repositories{
        ...
        maven{
            url= uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}
```

Y en el archivo `build.gradle.kts` del módulo de la aplicación, agregar las dependencias:

```kotlin
dependencies{
    implementation(libs.maps.android)
    implementation(libs.maps.compose)
}
```

Recuerde sincronizar el proyecto después de agregar las dependencias.

### 3. Configurar el Access Token

Cree un nuevo archivo llamado `mapbox_access_token.xml` en el directorio `res/values` y agregue el siguiente contenido, reemplazando `YOUR_MAPBOX_ACCESS_TOKEN` con su clave de API obtenida en el paso 1:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources xmlns:tools="http://schemas.android.com/tools">
    <string name="mapbox_access_token" translatable="false" tools:ignore="UnusedResources">YOUR_MAPBOX_ACCESS_TOKEN</string>
</resources>
```

### 4. Inicializar Mapbox en la aplicación

Para usar Mapbox, vamos a crear un composable que muestre un mapa básico. Cree un nuevo archivo Kotlin llamado `MapBox.kt` en el paquete `core/component` y agregue el siguiente código:

```kotlin
package com.example.demoapp.core.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@Composable
fun MapBox(
    modifier: Modifier = Modifier,
){
    // Configurar el estado inicial del mapa
    val mapViewportState = rememberMapViewportState{
        setCameraOptions{
            zoom(8.0) // Entre más alto el valor, más cerca estará la cámara del suelo
            center(Point.fromLngLat(-75.6491181, 4.4687891)) // Coordenadas fijas iniciales
        }
    }
    
    Box(modifier = modifier){
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            mapViewportState = mapViewportState
        )
    }
}
```

Con esto, simplemente puede llamar al composable `MapBox()` desde cualquier parte de su aplicación para mostrar un mapa básico centrado en una ubicación específica.

Tenga en cuenta que `setCameraOptions` recibe un objeto `CameraOptions` que incluye varias configuraciones para la cámara del mapa, como la posición inicial, el nivel de zoom, la inclinación y la orientación. En este ejemplo, hemos configurado el nivel de zoom y el centro del mapa utilizando coordenadas de longitud y latitud.

### 5. Probar la aplicación

Pruebe la aplicación en un dispositivo físico o emulador con soporte de ubicación para ver cómo funciona la geolocalización y el mapa. Para que el mapa ocupe toda la pantalla, simplemente llame al composable MapBox con el modificador `fillMaxSize()` en el archivo donde desee mostrar el mapa:

```kotlin
MapBox(
    modifier = Modifier.fillMaxSize()
)
```

### 6. Ubicación actual del usuario

Para mostrar la ubicación actual del usuario en el mapa, primero debemos solicitar los permisos necesarios para acceder a la ubicación del dispositivo. Asegúrese de agregar los siguientes permisos en el archivo `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

El primer permiso permite acceder a la ubicación aproximada del dispositivo, mientras que el segundo permiso permite acceder a la ubicación precisa utilizando GPS y otras fuentes. La ubicación aproximada se calcula utilizando torres de telefonía celular y puntos de acceso Wi-Fi cercanos, mientras que la ubicación precisa utiliza señales GPS para determinar la ubicación exacta del dispositivo.

Luego, modificamos el composable `MapBox` para incluir un botón que permita al usuario centrar el mapa en su ubicación actual:

```kotlin
package com.example.demoapp.core.component

// Importaciones necesarias...

@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    showMyLocationButton: Boolean = true // Mostrar botón de mi ubicación
){
    // Estado para manejar permisos de ubicación
    val permissionState = rememberLocationPermissionState()
    var shouldFollowUser by remember{ mutableStateOf(false)}
    
    // Configurar el estado inicial del mapa
    val mapViewportState = rememberMapViewportState{
        setCameraOptions{
            zoom(8.0)
            center(Point.fromLngLat(-75.6491181, 4.4687891))
        }
    }
    
    Box(modifier = modifier){
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState
        ){
            // Configurar ubicación del usuario si tiene permiso y quiere seguirla
            if(permissionState.hasPermission && shouldFollowUser){
                MapEffect(key1="follow_puck"){ mapView->
                    mapView.location.updateSettings{
                        locationPuck = createDefault2DPuck(withBearing = true)
                        enabled = true
                        puckBearing = PuckBearing.COURSE
                        puckBearingEnabled = true
                    }
                    mapViewportState.transitionToFollowPuckState(
                        defaultTransitionOptions = DefaultViewportTransitionOptions.Builder()
                            .maxDurationMs(1500)
                            .build()
                    )
                }
            }
            
            // Botón de mi ubicación
            if(showMyLocationButton){
                FloatingActionButton(
                    onClick={
                        if(permissionState.hasPermission){
                            shouldFollowUser = true
                        } else{
                            permissionState.requestPermission() // Solicitar permiso si no lo tiene
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ){
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Mi ubicación"
                    )
                }
            }
        }
    }
}
```

En el mismo archivo, agregue el siguiente código para manejar el estado del permiso de ubicación:

```kotlin
/**
 * Estado para manejar el permiso de ubicación de forma controlada
 */
class LocationPermissionState(
    hasPermission: Boolean = false,
    val requestPermission:()-> Unit={}
){
    var hasPermission by mutableStateOf(hasPermission)
        internal set
    var wasJustGranted by mutableStateOf(false)
        internal set
}
```

Y finalmente, cree una función composable para recordar el estado del permiso de ubicación:

```kotlin
@Composable
fun rememberLocationPermissionState(
    permission: String = android.Manifest.permission.ACCESS_FINE_LOCATION
): LocationPermissionState{
    val context = LocalContext.current
    
    // Verificar el estado inicial del permiso
    val initialPermission = remember{
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    // Estado para manejar el permiso
    val state = remember{ LocationPermissionState(hasPermission = initialPermission)}
    
    // Lanzador para solicitar el permiso
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ){ granted->
        state.wasJustGranted = granted && !state.hasPermission
        state.hasPermission = granted
    }
    
    // Recordar el estado del permiso
    return remember(state, launcher){
        LocationPermissionState(
            hasPermission = state.hasPermission,
            requestPermission = { launcher.launch(permission)}
        ).also{
            it.wasJustGranted = state.wasJustGranted
        }
    }
}
```

Con estos cambios, el usuario podrá tocar el botón de "Mi ubicación" para centrar el mapa en su ubicación actual, siempre y cuando haya otorgado los permisos necesarios. Si el permiso no ha sido otorgado, se solicitará al usuario que lo conceda.

Pruebe la aplicación para asegurarse de que la funcionalidad de ubicación del usuario funciona correctamente.

### 7. Uso de marcadores en el mapa

Para agregar marcadores en el mapa, podemos usar `PointAnnotation` de Mapbox. En nuestro proyecto, los marcadores representan la ubicación exacta de un reporte hecho por un usuario. A continuación, se muestra cómo agregar marcadores al mapa.

Modifique el composable `MapBox` para incluir una lista de ubicaciones de marcadores:

```kotlin
package com.example.demoapp.core.component

// Importaciones necesarias...

@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    reports: List<Report> = emptyList(), // Lista de reportes con ubicaciones
    showMyLocationButton: Boolean = true
){
    // Todo el código previo...
    
    // Cargar el ícono del marcador
    val marker = rememberIconImage(
        key = R.drawable.red_marker,
        painter = painterResource(R.drawable.red_marker)
    )
    
    Box(modifier = modifier){
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState
        ){
            // Todo el código previo...
            
            // Mostrar marcadores de los lugares
            reports.forEach{ report->
                PointAnnotation(
                    point = Point.fromLngLat(report.location.longitude, report.location.latitude)
                ){
                    iconImage = marker
                }
            }
            
            // Todo el resto del código...
        }
    }
}
```

Se asume que este composable recibe una lista de objetos `Report`, cada uno con una propiedad `location` que contiene las coordenadas de longitud y latitud.

Pruebe la aplicación para ver los marcadores en el mapa según las ubicaciones proporcionadas. Debe tener un Repository con reportes "quemados" y un ViewModel que los exponga para que el composable que usa el mapa pueda recibirlos.

### 8. Obtener la ubicación a partir de un evento de click

Para obtener la ubicación en el mapa cuando el usuario hace clic en un punto específico, podemos agregar un detector de eventos de clic en el mapa. A continuación, se muestra cómo hacerlo.

Modifique el composable `MapBox` para incluir un manejador de clics en el mapa:

```kotlin
package com.example.demoapp.core.component

// Importaciones necesarias...

@Composable
fun MapBox(
    modifier: Modifier = Modifier,
    reports: List<Report> = emptyList(),
    showMyLocationButton: Boolean = true,
    activateClick: Boolean = false, // Activar detección de clics (no siempre se necesita)
    onMapClickListener:(Point)-> Unit = {} // Callback para manejar el clic en el mapa
){
    // Todo el código previo...
    
    // Estado para almacenar el punto clickeado
    var clickedPoint by remember{ mutableStateOf<Point?>(null)}
    
    Box(modifier = modifier){
        MapboxMap(
            modifier = Modifier.matchParentSize(),
            mapViewportState = mapViewportState,
            onMapClickListener = { point->
                // Manejar el clic en el mapa solo si está activado
                if(activateClick){
                    onMapClickListener(point) // Llamar al callback externo y se le pasa el punto
                    clickedPoint = point
                }
                true
            }
        ){
            // Todo el código previo...
            
            // Mostrar marcador del punto clickeado solo si no es nulo
            clickedPoint?.let{ point->
                PointAnnotation(point = point){
                    iconImage = marker // Usar el mismo ícono de marcador
                }
            }
            
            // Todo el resto del código...
        }
    }
}
```

Esta funcionalidad permite que el usuario haga clic en cualquier punto del mapa y se capture la ubicación de ese punto, esto es clave para crear nuevos reportes donde la exactitud de la ubicación es importante.

Pruebe la aplicación para asegurarse de que al hacer clic en el mapa, se capture la ubicación correcta y se pueda utilizar según sea necesario.

---

## Actividad práctica

1.  **Popup con información del marcador:**
    Implemente una funcionalidad que muestre un popup o ventana emergente con información adicional cuando el usuario haga clic en un marcador en el mapa. La información puede incluir detalles del reporte, como título, descripción y fecha.

2.  **Cambiar color del marcador:**
    Ajuste el color del marcador según el tipo de reporte. Por ejemplo, use un marcador rojo para reportes de emergencia y un marcador verde para reportes informativos.

3.  **Estilo personalizado del mapa:**
    Investigue cómo aplicar estilos personalizados al mapa utilizando otras opciones de Mapbox. Puede leer sobre estilos en la documentación oficial de Mapbox.

---

> 📚 **Recursos adicionales:**
> - [Mapbox Maps SDK for Android](https://docs.mapbox.com/android/maps/guides/)
> - [Documentación oficial de Android - Ubicación](https://developer.android.com/training/location)
> - [Guías de desarrollo Android](https://developer.android.com/guide)