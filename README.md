# 🎬🍿 CineApp Android

**Una aplicación móvil para la gestión de un cine, desarrollada en Kotlin con Jetpack Compose y Firebase. Permite a los usuarios autenticarse, explorar la cartelera, comprar/cancelar boletos y dejar opiniones con contenido multimedia.**

---

## ✨ Funcionalidades Clave

-   **Autenticación Segura:** Registro e inicio de sesión de usuarios mediante Firebase Authentication (Email/Contraseña).
-   **Cartelera Dinámica:** Visualización de películas disponibles con sus pósters, géneros, duraciones, horarios y salas, obtenidas en tiempo real desde Cloud Firestore.
-   **Gestión de Boletos:**
    -   Compra de boletos con actualización atómica de asientos disponibles en Firestore.
    -   Cancelación de boletos desde el historial de usuario, con devolución de asientos.
-   **Historial de Transacciones:** Seguimiento detallado de todas las compras y cancelaciones de boletos por usuario, almacenado y visualizado desde Cloud Firestore.
-   **Opiniones con Multimedia:**
    -   Pantalla dedicada para dejar calificaciones y comentarios sobre películas ya finalizadas.
    -   Capacidad de adjuntar y subir **fotos, videos y audios** a Firebase Storage junto con la opinión.
-   **Registro de Interacciones:** Control de inicios y cierres de sesión de usuarios registrados en la base de datos de Firebase.

---

## 🛠️ Construido Con

-   📱 **Kotlin:** Lenguaje de programación principal.
-   🎨 **Jetpack Compose:** Toolkit moderno y declarativo para la construcción de la interfaz de usuario.
-   🔥 **Firebase:**
    -   **Authentication:** Gestión de usuarios y sesiones.
    -   **Cloud Firestore:** Base de datos NoSQL para datos de la aplicación (películas, horarios, transacciones, historial, opiniones).
    -   **Cloud Storage:** Almacenamiento de archivos multimedia (fotos, videos, audios) para las opiniones.
-   🗺️ **Jetpack Navigation Compose:** Para la gestión de la navegación entre las diferentes pantallas de la aplicación.
-   🖼️ **Coil:** Librería eficiente para la carga asíncrona de imágenes (incluyendo pósters de películas y multimedia de opiniones).
-   🔒 **Accompanist Permissions:** Para la gestión y solicitud de permisos de Android en tiempo de ejecución (cámara, micrófono, almacenamiento).
-   🔄 **Gson:** Librería para la serialización y deserialización de objetos JSON.

---

## 🏛️ Arquitectura del Proyecto

La aplicación sigue una arquitectura **MVVM (Model-View-ViewModel)**, promoviendo una clara separación de responsabilidades y facilitando la escalabilidad, la testabilidad y el mantenimiento del código.

-   **Capa de UI (`ui/screens`):** Contiene los Composables (Vistas) que definen la interfaz de usuario y los ViewModels que gestionan el estado y la lógica de presentación de cada pantalla.
-   **Capa de Datos (`data`):** Incluye los Modelos de datos (clases `data class`), los Repositorios (que abstraen el acceso a las fuentes de datos) y la lógica para interactuar con Firebase Firestore y Storage.
-   **Capa de Utilidades (`utils`):** Clases y extensiones auxiliares.

---

## 🚀 Cómo Ejecutar la Aplicación

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/danielpg10/cine-app-android.git](https://github.com/danielpg10/cine-app-android.git)
    ```
2.  **Abrir en Android Studio:**
    -   Abrir la carpeta `cine-app-android` como un proyecto Android existente.
3.  **Sincronizar Gradle:**
    -   Permitir que Gradle sincronice automáticamente las dependencias.
4.  **Configuración de Firebase:**
    -   Asegurarse de que el proyecto de Firebase "CineAppMKT" esté configurado y que el archivo `google-services.json` esté en la carpeta `app/`.
    -   **Reglas de Firestore:** Verificar que las reglas en `Firestore Database > Rules` permitan:
        ```firebase
        rules_version = '2';
        service cloud.firestore {
          match /databases/{database}/documents {
            match /{document=**} {
              allow read, write: if request.auth != null;
            }
            match /reviews/{reviewId} {
              allow read: if true;
              allow create: if request.auth != null;
            }
          }
        }
        ```
    -   **Reglas de Storage:** Verificar que las reglas en `Storage > Rules` permitan:
        ```firebase
        rules_version = '2';
        service firebase.storage {
          match /b/{bucket}/o {
            match /{allPaths=**} {
              allow read, write: if request.auth != null;
            }
          }
        }
        ```
    -   **Datos de Prueba:** Asegurarse de que las colecciones `movies`, `theaters`, `showtimes` en Firestore tengan datos con `startTime` en el futuro y `available: true`.
    -   **Permisos en el Emulador/Dispositivo:** Otorgar manualmente los permisos de `Cámara` y `Micrófono` a la aplicación desde la configuración del dispositivo (`Ajustes > Aplicaciones > CineApp > Permisos`).
5.  **Ejecutar la Aplicación:**
    -   Seleccionar un emulador/dispositivo y hacer clic en el botón `Run 'app'` en Android Studio.

---

## 👨‍💻 Desarrollo

Marlon Daniel Portuguez Gomez