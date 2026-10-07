# Inventario NFC

Sistema para controlar el inventario de equipos informáticos con etiquetas NFC. Cada equipo lleva pegada una etiqueta; al acercar el móvil se abre su ficha con los datos, el estado y el historial de incidencias. Todo se gestiona también desde un panel web.

Proyecto pensado como práctica de Desarrollo de Aplicaciones Multiplataforma: backend en Java, app móvil nativa en Kotlin y frontend web, conectados por una API REST.

## Qué hace

- Registrar un equipo desde la app y grabar su código en una etiqueta NFC (NTAG213/215/216).
- Leer una etiqueta y consultar al instante los datos del equipo.
- Reportar incidencias desde el móvil. El equipo pasa a "Averiado" y vuelve a "Operativo" cuando se resuelve la última incidencia pendiente.
- Panel web para buscar, crear, editar y eliminar equipos y revisar sus incidencias.

## Arquitectura

```
Etiqueta NFC --(código EQ-XXXXXXXX)--> App Android (Kotlin) --HTTP/JSON--> API REST (Spring Boot) --> Base de datos H2
                                                                                  ^
                                                      Panel web (HTML + JS) ------+
```

El panel web lo sirve el propio Spring Boot, así que se despliega todo junto.

## Tecnologías

| Parte | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 3 (Web, Data JPA, Validation), H2 |
| Web | HTML, CSS y JavaScript sin librerías |
| Android | Kotlin, API de NFC de Android (NDEF), Retrofit |
| Pruebas | JUnit 5 y MockMvc |

## Estructura

```
inventario-nfc/
├── backend/    API REST, lógica de negocio y panel web (src/main/resources/static)
└── android/    App Android (proyecto Gradle)
```

El backend sigue la estructura habitual por capas: `controlador` (endpoints), `servicio` (lógica), `repositorio` (acceso a datos) y `modelo` (entidades).

## Ejecutar el backend

Requisitos: JDK 17 o superior.

```
cd backend
./mvnw spring-boot:run        # en Windows: mvnw.cmd spring-boot:run
```

- Panel web: http://localhost:8080
- API: http://localhost:8080/api/equipos
- Consola de la base de datos: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:file:./datos/inventario`, usuario `sa`, sin contraseña)

Al arrancar por primera vez se crean tres equipos de ejemplo. Los datos se guardan en `backend/datos/`.

### Pruebas automáticas

```
cd backend
./mvnw test
```

## Compilar la app Android

Requisitos: JDK 17 y el SDK de Android (basta con las "command line tools", no hace falta Android Studio).

```
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

Define la variable de entorno `ANDROID_HOME` con la carpeta del SDK y compila:

```
cd android
./gradlew assembleDebug       # en Windows: gradlew.bat assembleDebug
```

El APK queda en `android/app/build/outputs/apk/debug/app-debug.apk`.

### Conectar la app con el servidor

La dirección del servidor se fija al compilar. Hay dos formas:

1. **Por USB (recomendada):** con el móvil conectado y la depuración USB activada, ejecuta `adb reverse tcp:8080 tcp:8080`. La app usa por defecto `http://localhost:8080/`.
2. **Por wifi:** compila indicando la IP del PC, por ejemplo `./gradlew assembleDebug -PurlBase=http://192.168.1.50:8080/`. El móvil y el PC deben estar en la misma red y el puerto 8080 abierto en el cortafuegos.

Instalar en el móvil: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/equipos?texto=` | Lista los equipos, con búsqueda opcional |
| GET | `/api/equipos/{codigo}` | Datos de un equipo (lo usa la app al leer una etiqueta) |
| POST | `/api/equipos` | Crea un equipo; el servidor genera el código |
| PUT | `/api/equipos/{codigo}` | Actualiza un equipo |
| DELETE | `/api/equipos/{codigo}` | Elimina un equipo y sus incidencias |
| GET | `/api/equipos/{codigo}/incidencias` | Historial de incidencias |
| POST | `/api/equipos/{codigo}/incidencias` | Reporta una incidencia |
| PUT | `/api/incidencias/{id}/resolver` | Marca una incidencia como resuelta |

## Decisiones de diseño

- **El código va dentro de la etiqueta** (registro NDEF de texto) en lugar de usar el UID del chip. El UID no se puede cambiar y varía según el modelo de etiqueta; así se puede sustituir una etiqueta estropeada grabando el mismo código en otra.
- **El servidor genera los códigos**, para evitar duplicados y no depender de lo que envíe el cliente.
- **H2 en fichero** para que el proyecto arranque sin instalar nada. Cambiar a MySQL o PostgreSQL solo requiere ajustar `application.properties` y añadir el driver.

## Posibles mejoras

- Autenticación y roles (técnico / administrador) con Spring Security y JWT.
- HTTPS y eliminación de `usesCleartextTraffic` en la app.
- Lectura de códigos QR como alternativa a las etiquetas NFC.
- Exportación del inventario a CSV o PDF.
