# Contexto de arquitectura de U-DICE

La rama `4taEtapa` reúne la implementación final en Kotlin, Jetpack Compose, Room, Hilt y Supabase. La referencia histórica de la tercera etapa está en Git.


## Estado de la rama

- `41d13cc`: sincronización de avisos entre Supabase y Room.
- `c21b494`: Main-Safety, autenticación por correo, RLS de tablas heredadas, imágenes, refresco del feed y emojis por aviso.
- `9503704`: soporte para videos MP4 y documentación de arquitectura.
- Al 24 de noviembre de 2026, la última referencia remota disponible apunta a `9503704`. La configuración de Supabase mediante `local.properties` y esta actualización del contexto están pendientes de commit.

## Puesta en marcha

En la raíz, agrega `UDICE_SUPABASE_URL` y `UDICE_SUPABASE_PUBLISHABLE_KEY` al archivo local `local.properties`, junto a `sdk.dir`. El archivo está excluido de Git. Cada integrante necesita la URL del proyecto y su clave Publishable para compilar con Supabase; el README incluye un ejemplo sin valores reales. Gradle genera ambos campos en `BuildConfig` e informa qué configurar si faltan en el modo Supabase.

En Windows, ejecuta `.\gradlew assembleDebug`. La variante de desarrollo sin configuración Supabase se compila con `-PudiceSupabase=false`.

## Interfaz y estado

- `MainActivity` comparte `AuthStateHolder` con `UDiceApp`, que elige la interfaz pública o el grafo autenticado. El registro muestra un aviso de correo y Supabase redirige a `com.example.udice://confirm-email`. El enlace confirma el correo y la sesión se abre con la contraseña.
- Cada ruta obtiene su ViewModel con Hilt y consume el estado con `collectAsStateWithLifecycle`. El feed combina flujos de avisos, votos, preferencias y guardados. La actualización manual o tras 60 segundos en segundo plano vuelve al inicio cuando la sincronización de avisos termina bien.
- Nueva publicación permite seleccionar y quitar hasta cuatro imágenes o un MP4. Muestra una vista previa cuadrada de las imágenes y el progreso de subida del video. Mis publicaciones permite editar los campos del aviso, elegir los emojis para los lectores y eliminar el aviso. Feed y Detalle cargan las imágenes mediante Coil.

## Persistencia y concurrencia

- `PostRepositoryImpl` serializa cambios y refrescos con un `Mutex`. En modo Supabase escribe en PostgREST y actualiza Room después de la respuesta. Las descargas se paginan y reemplazan la caché en una transacción solo si terminan correctamente.
- Room v7 guarda los avisos, la URL de video cuando aplica, sus emojis habilitados, votos y catálogo. Guardados y preferencias se guardan por cuenta y dispositivo. El modo local de desarrollo usa una base separada.
- `@IoDispatcher` provee `Dispatchers.IO` a los repositorios, clientes HTTP y acceso a Room. Los repositorios propagan la cancelación de corrutinas. Compose consume `Flow` y `StateFlow`.
- `CloudSessionRepository` maneja el inicio de sesión y la renovación de tokens. La función `udice-faculty-admin` administra las cuentas ASO tras verificar la identidad y el rol del solicitante.

## Supabase y seguridad

- RLS permite que usuarios autenticados lean avisos y que una cuenta ASO gestione sus propios avisos. Perfiles, votos y catálogo tienen sus propias políticas. Las tablas heredadas quedan fuera del acceso de clientes.
- `publicaciones.reacciones_permitidas` guarda la selección del autor. Un valor nulo usa el catálogo completo. La función `udice_guardar_reaccion` valida la opción elegida. Los votos de opciones deshabilitadas conservan su registro.
- `publicaciones-imagenes` admite JPG, PNG y WebP hasta 5 MiB. Las ASO cargan imágenes a su carpeta individual; el propietario gestiona los archivos. La lectura es pública con URL directa. La URL y la clave publicable se cargan desde local.properties mediante BuildConfig. La aplicación usa esa clave y la sesión del usuario. Las credenciales privilegiadas viven en el backend.
- `publicaciones-videos` admite MP4 hasta 20 MiB sin límite de duración, con el mismo modelo de acceso que las imágenes. Android valida el archivo y genera un JPEG para el feed. El detalle usa Media3 ExoPlayer y pausa al ir a segundo plano. El formulario admite hasta cuatro imágenes o un video por aviso.
- Las migraciones SQL están en `supabase/migrations`. En el proyecto compartido se ejecutaron individualmente con Supabase CLI. Comprueba el esquema desplegado antes de intentar aplicar toda la serie a una base existente.

## Verificación y datos de prueba

- La compilación y las cuatro pruebas instrumentadas en Android pasaron tras integrar MP4. Tras mover la configuración a `local.properties` se repitieron la compilación y las pruebas unitarias. También se verificó la generación de `BuildConfig` y la exclusión de `local.properties` de Git.
- En Supabase se comprobó que los roles de cliente carecen de permisos sobre las tablas heredadas. El autor puede elegir emojis para su aviso, otra cuenta ASO no puede alterarlos y el lector solo puede votar con los tipos habilitados.
- Con la ASO de prueba se subió, descargó y eliminó una imagen temporal. También se subió un MP4 temporal, se verificó la lectura por rangos mediante HTTP 206, se guardó su URL en un aviso y se eliminaron ambos. Storage rechazó una carga con MIME `video/webm`.
- El proyecto compartido contiene cuatro avisos marcados `[Prueba]` con imágenes externas, una cuenta ADMIN y una cuenta ASO de prueba. Se verificaron ambas sesiones y el acceso administrativo al listado de ASO. Las credenciales se guardan en un TXT fuera del repositorio.

## Pendientes de entrega

- Completar una prueba manual del CRUD desde Android y la selección y reproducción de un MP4 en el dispositivo. Verificar también el flujo de correo con un registro nuevo y el refresco tras un minuto en segundo plano.
- La reproducción automática en el detalle quedó como ajuste opcional. En la versión actual el usuario pulsa **Reproducir video**.
- Preparar el Word individual de la etapa 4 con los integrantes, la URL del repositorio, la justificación de cambios respecto a la etapa 1, dos o tres capturas de código y dos o tres de la app. Comprobar la participación en Git y la fecha de entrega en Moodle.
