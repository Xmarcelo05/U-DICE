# Contexto de arquitectura de U-DICE

La rama `4taEtapa` reúne la implementación final en Kotlin, Jetpack Compose, Room, Hilt y Supabase. La referencia histórica de la tercera etapa está en Git.

## Interfaz y estado

- `MainActivity` comparte `AuthStateHolder` con `UDiceApp`, que elige la interfaz pública o el grafo autenticado. El registro muestra un aviso de correo y Supabase redirige a `com.example.udice://confirm-email`. El enlace confirma el correo y la sesión se abre con la contraseña.
- Cada ruta obtiene su ViewModel con Hilt y consume el estado con `collectAsStateWithLifecycle`. El feed combina flujos de avisos, votos, preferencias y guardados. La actualización manual o tras 60 segundos en segundo plano vuelve al inicio cuando la sincronización de avisos termina bien.
- Nueva publicación permite seleccionar, previsualizar y quitar una imagen. Mis publicaciones permite editar los campos del aviso, elegir los emojis para los lectores y eliminar el aviso. Feed y Detalle cargan las imágenes mediante Coil.

## Persistencia y concurrencia

- `PostRepositoryImpl` serializa cambios y refrescos con un `Mutex`. En modo Supabase escribe en PostgREST y actualiza Room después de la respuesta. Las descargas se paginan y reemplazan la caché en una transacción solo si terminan correctamente.
- Room v6 guarda los avisos, la URL de video cuando aplica, sus emojis habilitados, votos y catálogo. Guardados y preferencias se guardan por cuenta y dispositivo. El modo local de desarrollo usa una base separada.
- `@IoDispatcher` provee `Dispatchers.IO` a los repositorios, clientes HTTP y acceso a Room. Los repositorios propagan la cancelación de corrutinas. Compose consume `Flow` y `StateFlow`.
- `CloudSessionRepository` maneja el inicio de sesión y la renovación de tokens. La función `udice-faculty-admin` administra las cuentas ASO tras verificar la identidad y el rol del solicitante.

## Supabase y seguridad

- RLS permite que usuarios autenticados lean avisos y que una cuenta ASO gestione sus propios avisos. Perfiles, votos y catálogo tienen sus propias políticas. Las tablas heredadas quedan fuera del acceso de clientes.
- `publicaciones.reacciones_permitidas` guarda la selección del autor. Un valor nulo usa el catálogo completo. La función `udice_guardar_reaccion` valida la opción elegida. Los votos de opciones deshabilitadas conservan su registro.
- `publicaciones-imagenes` admite JPG, PNG y WebP hasta 5 MiB. Las ASO cargan imágenes a su carpeta individual; el propietario gestiona los archivos. La lectura es pública con URL directa. La aplicación usa su clave pública y la sesión del usuario, y las credenciales privilegiadas viven en el backend.
- `publicaciones-videos` admite MP4 hasta 20 MiB con el mismo modelo de acceso que las imágenes. Android valida el archivo y genera un JPEG para el feed. El detalle usa Media3 ExoPlayer y pausa al ir a segundo plano. El formulario admite una imagen o un video por aviso.
- Las migraciones SQL están en `supabase/migrations`. En el proyecto compartido se ejecutaron individualmente con Supabase CLI. Comprueba el esquema desplegado antes de intentar aplicar toda la serie a una base existente.

## Verificación

Se compiló el APK, se ejecutaron las pruebas unitarias e instrumentadas y se verificaron las políticas de Supabase. Las comprobaciones remotas incluyeron permisos de tablas heredadas, carga y borrado de una imagen temporal con una cuenta ASO, y selección de emojis y votación por rol. Las cuentas y avisos de prueba se aprovisionan en Supabase fuera del repositorio.
