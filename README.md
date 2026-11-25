# U-DICE

Aplicación Android en Kotlin y Jetpack Compose para consultar y publicar avisos universitarios. Los lectores pueden filtrar el feed, buscar avisos por texto o fecha, reaccionar y guardar avisos. Las cuentas ASO pueden crear y editar sus avisos, adjuntar hasta cuatro imágenes o un video y elegir qué reacciones ofrecen. Los administradores gestionan las cuentas ASO y el catálogo de reacciones.

## Compilación

Abre el proyecto en Android Studio con el SDK configurado. El modo Supabase lee la URL del proyecto y su clave **Publishable** desde `local.properties`, en la raíz del repositorio:

```properties
UDICE_SUPABASE_URL=https://tu-proyecto.supabase.co
UDICE_SUPABASE_PUBLISHABLE_KEY=sb_publishable_TU_CLAVE
```

Ejecuta `./gradlew assembleDebug` (en Windows, `.\gradlew assembleDebug`). La variante local se puede compilar sin esa configuración con `-PudiceSupabase=false`.


## Arquitectura y datos

```mermaid
flowchart TD
    subgraph UI["Interfaz · Jetpack Compose y Material 3"]
        Main["MainActivity<br/>Hilt y enlace de confirmación"] --> App["UDiceApp<br/>AuthStateHolder y SessionState"]
        App -->|"Sin sesión"| Auth["AuthScreen<br/>Registro e inicio de sesión"]
        App -->|"Con sesión"| Nav["SignedInApp<br/>Scaffold, barras y NavHost"]

        Nav -->|"ADMIN"| Admin["AdminRoute<br/>AdminStateHolder → AdminScreen"]
        Nav --> Feed["InicioFeedRoute<br/>InicioFeedStateHolder → InicioFeedScreen"]
        Nav --> Search["BusquedaRoute<br/>Texto, categoría, facultad y fechas"]
        Nav --> Pref["PreferenciasRoute<br/>PreferenciasStateHolder → PreferenciasScreen"]
        Nav -->|"ASO"| New["NuevaPublicacionRoute<br/>NuevaPublicacionStateHolder → NuevaPublicacionScreen"]
        Nav --> Profile["PerfilScreen<br/>Cuenta activa, guardados y avisos propios"]
        Profile --> Own["MisPublicacionesRoute<br/>MisPublicacionesStateHolder → MisPublicacionesScreen"]
        Profile --> Saved["GuardadosRoute<br/>GuardadosStateHolder → GuardadosScreen"]
        Feed --> Card["PostCard<br/>Aviso, reacciones y guardado"]
        Card --> Detail["DetallePublicacionRoute<br/>DetallePublicacionStateHolder → DetallePublicacionScreen"]
        Saved --> Detail
        Own --> Detail

        New --> Picker["Selector de Android<br/>Hasta cuatro fotos o un MP4"]
        New --> Gallery["Galería cuadrada y visor deslizable<br/>Coil y Compose HorizontalPager"]
        Card --> Gallery
        Detail --> Gallery
        Detail --> Player["Media3 ExoPlayer<br/>Reproducción bajo demanda"]
    end

    subgraph DATA["Repositorios, persistencia y sincronización"]
        Session["SessionRepository"]
        CloudSession["CloudSessionRepository<br/>Sesión y renovación en memoria"]
        LocalSession["SessionRepositoryImpl<br/>Sesión de desarrollo"]
        Posts["PostRepository<br/>PostRepositoryImpl"]
        PrefRepo["PreferencesRepository<br/>PreferencesRepositoryImpl"]
        SavedRepo["SavedPostsRepository<br/>SavedPostsRepositoryImpl"]
        Votes["ReactionsRepository<br/>ReactionsRepositoryImpl"]

        Session -->|"Modo Supabase"| CloudSession
        Session -->|"Modo local"| LocalSession
        Posts -->|"Flow de avisos y caché"| Local["LocalDataSource<br/>RoomLocalDataSource"]
        Posts -->|"CRUD y sincronización paginada"| Remote["RemoteDataSource<br/>SupabaseRemoteDataSource"]
        Posts --> Media["PublicationImageStorage<br/>Carga de fotos, MP4 y miniaturas"]
        LocalSession --> Local
        PrefRepo -->|"Preferencias por cuenta"| Local
        SavedRepo -->|"Guardados por cuenta"| Local
        Local --> Room["Room · UDiceDatabase v7<br/>PostDao, UserDao, PreferenceDao,<br/>SavedPostDao y ReaccionDao"]
        Votes -->|"ReaccionDao: votos y catálogo"| Room
        Seed["SeedDataCallback<br/>Datos iniciales de desarrollo"] -. "Solo modo local" .-> Room
        CloudSession --> HTTP["CloudHttpClient<br/>Peticiones HTTPS con token"]
        Remote --> HTTP
        Votes --> HTTP
        Media --> HTTP
    end

    App --> Session
    Admin --> Session
    Admin --> Votes
    Search --> Posts
    Feed --> Posts
    Feed --> PrefRepo
    Feed --> SavedRepo
    Feed --> Votes
    Pref --> PrefRepo
    New --> Posts
    Own --> Posts
    Own -->|"Catálogo de emojis"| Votes
    Saved --> SavedRepo
    Detail --> Posts
    Detail --> SavedRepo

    subgraph CLOUD["Supabase"]
        AuthAPI["Auth<br/>Registro, confirmación y tokens"]
        REST["PostgREST<br/>Publicaciones, perfiles y catálogo"]
        RPC["RPC de votos y búsqueda<br/>Emojis por aviso y hasta 20 resultados por página"]
        Edge["Edge Function udice-faculty-admin<br/>Gestión de cuentas ASO"]
        Storage["Storage<br/>publicaciones-imagenes y publicaciones-videos"]
        Policies["RLS y permisos de base de datos<br/>Lectura autenticada y escrituras por autor"]
        REST --> Policies
        Storage --> StoragePolicy["Políticas de Storage<br/>Carga ASO en carpeta propia y lectura por URL"]
    end

    HTTP --> AuthAPI
    HTTP --> REST
    HTTP --> RPC
    HTTP --> Edge
    HTTP --> Storage
    Gallery -. "Carga por URL" .-> Storage
    Player -. "Lee MP4 por HTTPS" .-> Storage
    AuthAPI -. "Confirmación de correo" .-> Main

    subgraph DI["Inyección de dependencias y concurrencia"]
        Hilt["Hilt y KSP"] --> Modules["SessionModule, RepositoryModule,<br/>DataSourceModule, DatabaseModule y CoroutinesModule"]
        Modules --> IO["@IoDispatcher · Dispatchers.IO"]
        Config["local.properties → BuildConfig<br/>URL y clave publishable"]
    end

    Modules -. "Inyección" .-> Session
    Modules -. "Inyección" .-> Posts
    IO -. "withContext y flowOn" .-> Local
    IO -. "Peticiones HTTPS" .-> HTTP
    Config -. "Configuración de Supabase" .-> HTTP
```

- **Avisos.** Supabase recibe los cambios y Room expone el estado reactivo a la interfaz. El feed empieza con hasta 50 avisos y carga más al llegar al final. Cada página actualiza Room. Ante un fallo de red se conserva la última copia.
- **Guardados y preferencias.** Se almacenan por cuenta en Room y permanecen en el dispositivo.
- **Registro.** Supabase solicita verificar el correo. El enlace abre U-DICE y el usuario puede iniciar sesión con su contraseña.
- **Feed.** Toca Inicio para volver arriba. Tira de la lista desde el borde superior para actualizarla. También se sincroniza al volver tras al menos 60 segundos en segundo plano.
- **Búsqueda.** Supabase devuelve resúmenes de 20 en 20. Los filtros combinan categoría, facultad y fechas. Si el aviso no está en Room, se descarga al abrir su detalle.
- **Edición.** Los autores pueden editar texto, emojis y archivos desde un mismo diálogo. En sus tarjetas del feed, el menú permite editar o eliminar.
- **Imágenes.** Se seleccionan hasta cuatro por aviso y se agrupan en un bloque cuadrado en el formulario, el feed y el detalle. El bucket admite JPG, PNG y WebP de hasta 5 MiB. La lectura por URL es pública y las operaciones de escritura están protegidas por RLS.
- **Videos.** MP4 opcional de hasta 20 MiB por aviso, sin límite de duración. La app genera una miniatura y reproduce el video bajo demanda en el detalle con Media3 ExoPlayer.
- **Reacciones.** Los autores eligen de un catálogo compartido desde Mis publicaciones. Supabase valida la selección al guardar cada voto.

Consulta [el contexto de arquitectura](CONTEXTO_SESION_ARQUITECTURA.md#historial-del-esquema-desplegado) para ver las migraciones, funciones y políticas de Supabase.
