# Contexto de arquitectura de U-DICE

La rama `4taEtapa` reúne la implementación final en Kotlin, Jetpack Compose, Room, Hilt y Supabase. La referencia histórica de la tercera etapa está en Git.


## Estado de la rama

- `41d13cc`: sincronización de avisos entre Supabase y Room.
- `c21b494`: Main-Safety, autenticación por correo, RLS de tablas heredadas, imágenes, refresco del feed y emojis por aviso.
- `9503704`: soporte para videos MP4 y documentación de arquitectura.
- `755476e`: galería y configuración local de Supabase.
- `f34f2f2`: diagrama detallado del README.
- Cambios del 25 de noviembre: búsqueda remota y por fechas, edición multimedia, acciones del autor en el feed, paginación y gestos de navegación.

## Puesta en marcha

En la raíz, agrega `UDICE_SUPABASE_URL` y `UDICE_SUPABASE_PUBLISHABLE_KEY` al archivo local `local.properties`, junto a `sdk.dir`. El archivo está excluido de Git. Cada integrante necesita la URL del proyecto y su clave Publishable para compilar con Supabase; el README incluye un ejemplo sin valores reales. Gradle genera ambos campos en `BuildConfig` e informa qué configurar si faltan en el modo Supabase.

En Windows, ejecuta `.\gradlew assembleDebug`. La variante de desarrollo sin configuración Supabase se compila con `-PudiceSupabase=false`.

## Interfaz y estado

- `MainActivity` comparte `AuthStateHolder` con `UDiceApp`, que elige la interfaz pública o el grafo autenticado. El registro muestra un aviso de correo y Supabase redirige a `com.example.udice://confirm-email`. El enlace confirma el correo y la sesión se abre con la contraseña.
- Cada ruta obtiene su ViewModel con Hilt y consume el estado con `collectAsStateWithLifecycle`. El feed combina flujos de avisos, votos, preferencias y guardados. El gesto de tirar desde arriba actualiza el feed. Tocar Inicio cuando ya está abierto solo desplaza la lista al primer aviso. Volver tras al menos 60 segundos en segundo plano también actualiza el feed.
- Nueva publicación permite seleccionar y quitar hasta cuatro imágenes o un MP4. Muestra una vista previa cuadrada de las imágenes y el progreso de subida del video. Mis publicaciones permite buscar avisos propios y editar texto, emojis y archivos en las pestañas Publicación y Multimedia. El menú de los avisos propios en el feed abre ese editor o pide confirmación para eliminar. Feed y Detalle cargan las imágenes mediante Coil. El autor ve «Yo» en sus propios avisos. Cambiar de categoría devuelve la lista a su primer aviso.
- Mi perfil permite elegir o quitar la foto. El feed recibe la ruta del avatar junto con cada aviso y conserva las iniciales como respaldo. Desde el nombre o la foto se abre el perfil público, que lista los avisos en páginas de 20.
- El detalle muestra un resumen de emojis, y el panel de participantes solicita páginas de 30 y permite filtrar por tipo de reacción.

## Persistencia y concurrencia

- `PostRepositoryImpl` serializa cambios y refrescos con un `Mutex`. En modo Supabase escribe en PostgREST y actualiza Room después de la respuesta. El feed pide hasta 50 avisos por página mediante un cursor por fecha e ID. Cada página inserta o actualiza en Room, y el feed observa únicamente su ventana cargada. Buscar espera 400 ms, consulta la RPC por páginas de 20, reutiliza resultados durante un minuto y carga el detalle en Room si faltaba.
- Room v8 guarda los avisos, las URLs de video y avatar cuando aplican, sus emojis habilitados, votos y catálogo. Guardados y preferencias se guardan por cuenta y dispositivo. El modo local de desarrollo usa una base separada.
- `@IoDispatcher` provee `Dispatchers.IO` a los repositorios, clientes HTTP y acceso a Room. Los repositorios propagan la cancelación de corrutinas. Compose consume `Flow` y `StateFlow`.
- `CloudSessionRepository` maneja el inicio de sesión y la renovación de tokens. La función `udice-faculty-admin` administra las cuentas ASO tras verificar la identidad y el rol del solicitante.

## Supabase y seguridad

- RLS permite que usuarios autenticados lean avisos y que una cuenta ASO gestione sus propios avisos. Perfiles, votos y catálogo tienen sus propias políticas. Las tablas heredadas quedan fuera del acceso de clientes.
- `publicaciones.reacciones_permitidas` guarda la selección del autor. Un valor nulo usa el catálogo completo. La función `udice_guardar_reaccion` valida la opción elegida. Los votos de opciones deshabilitadas conservan su registro.
- `publicaciones-imagenes` admite JPG, PNG y WebP hasta 5 MiB. Las ASO cargan imágenes a su carpeta individual; el propietario gestiona los archivos. La lectura es pública con URL directa. La URL y la clave publicable se cargan desde local.properties mediante BuildConfig. La aplicación usa esa clave y la sesión del usuario. Las credenciales privilegiadas viven en el backend.
- `publicaciones-videos` admite MP4 hasta 20 MiB sin límite de duración, con el mismo modelo de acceso que las imágenes. Android valida el archivo y genera un JPEG para el feed. El feed y el detalle reproducen con Media3 ExoPlayer después de pulsar ▶. El feed conserva un solo reproductor activo y lo suelta cuando la tarjeta sale de vista. Ambos desconectan la superficie al salir de la pantalla. El formulario admite hasta cuatro imágenes o un video por aviso.
- Las migraciones SQL están en `supabase/migrations`. En el proyecto compartido se ejecutaron individualmente con Supabase CLI. Comprueba el esquema desplegado antes de intentar aplicar toda la serie a una base existente.

### Perfiles públicos y seguimiento

- Buscar tiene dos pestañas. Publicaciones conserva sus filtros. Personas busca publicadores por nombre y devuelve tarjetas con foto, facultad y total de seguidores.
- El perfil de un publicador se abre desde Personas o al tocar su foto o nombre en el feed. Sus avisos se cargan en páginas de 20. Al abrir un aviso se reutiliza el detalle de la app y, al volver, se conserva la posición de la lista.
- `PublisherRepository` consulta perfiles, avisos y conexiones mediante RPC autenticadas. Los perfiles públicos muestran el nombre, la foto, la facultad y el total de seguidores. Las listas de Mi perfil muestran los nombres y fotos de tus propias conexiones en páginas de 20.
- La relación `seguimientos` se guarda por cuenta en Supabase. Su clave primaria evita duplicados y la RPC solo permite seguir a cuentas ASO distintas de la cuenta activa. La vista Siguiendo consulta avisos de esas cuentas en páginas de 50, mantiene el filtro por categoría y es independiente de las preferencias de facultad de Novedades. El estado vacío de Siguiendo se habilita tras recibir la primera página de forma exitosa y se oculta durante la recarga.

### Historial del esquema desplegado

| Migración | Cambios de esquema y permisos |
| --- | --- |
| [20261122000100](supabase/migrations/20261122000100_udice_auth_catalog.sql) | Perfiles enlazados a Supabase Auth, catálogo de reacciones, funciones de administración y aprovisionamiento de perfiles, y políticas RLS iniciales. |
| [20261122000200](supabase/migrations/20261122000200_nombres_tablas_en_espanol.sql) | Renombra las tablas a `perfiles_udice` y `tipos_reaccion_udice` y actualiza las funciones que las consultan. |
| [20261122000300](supabase/migrations/20261122000300_votos_y_roles_aso.sql) | Roles ASO, RLS de publicaciones y votos, y RPC `udice_guardar_reaccion`. Cada usuario conserva un voto por publicación. |
| [20261124000100](supabase/migrations/20261124000100_restringir_tablas_heredadas.sql) | Activa RLS y retira permisos de cliente sobre las tablas heredadas que existan. |
| [20261124000200](supabase/migrations/20261124000200_imagenes_publicaciones.sql) | Crea el bucket de imágenes y limita la subida a publicadores en su propia carpeta. |
| [20261124000300](supabase/migrations/20261124000300_emojis_por_publicacion.sql) | Añade `publicaciones.reacciones_permitidas` y valida la selección del autor y los votos en la base. |
| [20261124000400](supabase/migrations/20261124000400_videos_publicaciones.sql) | Añade `publicaciones.video_url`, crea el bucket MP4 y sus políticas. |
| [20261124000500](supabase/migrations/20261124000500_galerias_publicaciones.sql) | Añade `publicaciones.additional_image_urls`. La restricción admite hasta tres fotos extra con una imagen principal y excluye video en esa combinación. |
| [20261125000100](supabase/migrations/20261125000100_busqueda_publicaciones.sql) | Añade `udice_buscar_publicaciones`, una RPC autenticada que respeta RLS y devuelve hasta 20 resúmenes filtrados por texto, categoría, facultad y fechas. |
| [20261125000200](supabase/migrations/20261125000200_fotos_de_perfil.sql) | Añade `perfiles_udice.avatar_path`, el bucket `perfiles-imagenes` y políticas que limitan la edición del perfil y la escritura de archivos a su propietario. Las imágenes se leen mediante URL pública. |
| [20261125000300](supabase/migrations/20261125000300_avatar_autor_en_feed.sql) | Añade el campo calculado de PostgREST que devuelve la ruta del avatar de cada autor sin exponer su perfil. |
| [20261125000400](supabase/migrations/20261125000400_participantes_reacciones.sql) | Añade la RPC autenticada del detalle de reacciones. Devuelve totales por emoji y páginas de participantes con nombre y avatar. |
| [20261125000500](supabase/migrations/20261125000500_perfiles_y_seguimientos.sql) | Crea seguimientos privados por cuenta y añade RPC autenticadas de búsqueda de personas, perfil público, avisos del publicador y feed Siguiendo. |
| [20261125000600](supabase/migrations/20261125000600_contadores_y_seguidores.sql) | Añade contadores en las tarjetas públicas y RPC para consultar las listas propias de seguidores y cuentas seguidas. |

El feed usa PostgREST con un cursor por fecha e ID. La paginación del feed y la edición de multimedia usan las columnas y políticas ya desplegadas.

## Verificación y datos de prueba

- La compilación y las cuatro pruebas instrumentadas en Android pasaron tras integrar MP4. Tras mover la configuración a `local.properties` se repitieron la compilación y las pruebas unitarias. También se verificó la generación de `BuildConfig` y la exclusión de `local.properties` de Git.
- En Supabase se comprobó que los roles de cliente carecen de permisos sobre las tablas heredadas. El autor puede elegir emojis para su aviso, otra cuenta ASO no puede alterarlos y el lector solo puede votar con los tipos habilitados.
- Con la ASO de prueba se subió, descargó y eliminó una imagen temporal. También se subió un MP4 temporal, se verificó la lectura por rangos mediante HTTP 206, se guardó su URL en un aviso y se eliminaron ambos. Storage rechazó una carga con MIME `video/webm`.
- El proyecto compartido contiene cuatro avisos marcados `[Prueba]` con imágenes externas, cuentas de prueba para ADMIN, LECTOR y ASO. Se verificaron ambas sesiones y el acceso administrativo al listado de ASO. Las credenciales se guardan en un TXT fuera del repositorio.

- Se verificó la RPC con dos páginas sin duplicados, texto sin tilde, filtros combinados y bloqueo de acceso anónimo. Se comprobó en el teléfono la carga progresiva del feed.

## Pendientes de entrega

- Completar una prueba manual del CRUD desde Android y la selección y reproducción de un MP4 en el dispositivo. Verificar también el flujo de correo con un registro nuevo y el refresco tras un minuto en segundo plano.
- Verificar en el A15 el panel de reacciones, los avatares y la salida de una pantalla con video activo.
- Preparar el Word individual de la etapa 4 con los integrantes, la URL del repositorio, la justificación de cambios respecto a la etapa 1, dos o tres capturas de código y dos o tres de la app. Comprobar la participación en Git y la fecha de entrega en Moodle.
