# Contexto de Sesión de Arquitectura U-DICE

Este documento resume todo el trabajo realizado en esta sesión sobre el proyecto Android **U-DICE** (Kotlin + Jetpack Compose, paquete `com.example.udice`) para que otro desarrollador o IA pueda continuar exactamente desde este punto sin perder el hilo.

---

## 1. Estado Actual del Repositorio Git

- **Rama activa:** `3rEtapa` (creada y activa desde el inicio).
- **Estado del árbol de trabajo:** Limpio (`working tree clean`).
- **Compilación verificada:** Todos los commits fueron compilados exitosamente con `./gradlew assembleDebug` previo a cada commit.
- **Commits realizados en esta sesión (8 commits en orden):**

```text
c4eb8a3 feat(repository): implementa PreferencesRepository persistido localmente
4677f64 feat(repository): implementa PostRepository con sincronización y validación de permisos
ece360d feat(repository): implementa SessionRepository offline-first con Flow
6afce1a feat(remote): crea RemoteDataSource y FakeRemoteDataSource con simulación de red
d0c8d4a feat(local): implementa Room con entidades, DAOs, mappers y LocalDataSource
f9253c8 feat(model): agrega modelos compartidos User, Rol, Facultad, Categoria, Alcance y Post
8dc979e refactor(arch): organiza estructura de paquetes data, ui y di
4aa31b6 feat(di): configura Hilt, KSP, Application, EntryPoint y calificador @IoDispatcher
```

---

## 2. Pantallas Existentes en el Proyecto

No se modificó el diseño visual de las pantallas existentes. Se migraron a subpaquetes dedicados dentro de `ui/`:
1. **Preferencias** (`com.example.udice.ui.preferencias.PreferenciasScreen.kt`): Gestión de facultades y dependencias de interés con switches y control de cambios para habilitar guardado.
2. **Nueva Publicación** (`com.example.udice.ui.nuevapublicacion.NuevaPublicacionScreen.kt`): Formulario con título, descripción, selección visual de clasificación/categoría, adjuntar imagen y diálogo de éxito.
3. **Mi Perfil** (`com.example.udice.ui.perfil.PerfilScreen.kt`): Tarjeta de usuario con avatar, facultad, estadísticas y opciones (publicaciones, guardados, seguridad, cerrar sesión).
4. **Navegación y Cabeceras**:
   - `com.example.udice.UDiceApp.kt`: Contenedor principal con `NavHost`, `Scaffold`, selector dinámico de rol en el Header y BottomBar.
   - `com.example.udice.ui.navigation.NavRoutes.kt`: Constantes de rutas (`PREFERENCIAS`, `NUEVA_PUBLICACION`, `PERFIL`).
   - `com.example.udice.ui.components.UDiceHeader.kt` y `UDiceBottomBar.kt`.

---

## 3. Resumen Detallado de lo Realizado (Módulos 1 al 8)

### Módulo 1: Hilt, KSP y Dispatchers.IO (`commit 4aa31b6`)
- **Plugins y Dependencias:** Configurado Hilt (`2.55`), KSP (`2.2.10-2.0.2`), `hilt-navigation-compose` (`1.2.0`) y `lifecycle-runtime-compose`.
- **Application Class:** Creada `com.example.udice.UDiceApplication` con `@HiltAndroidApp` y registrada en `AndroidManifest.xml`.
- **MainActivity:** Anotada con `@AndroidEntryPoint`.
- **Concurrencia:** Creado `@IoDispatcher` (anotación Qualifier) y `CoroutinesModule` en `com.example.udice.di` proveyendo `Dispatchers.IO`.
- **Ajustes de compatibilidad AGP 9.3:** En `gradle.properties` se añadieron `android.newDsl=false` y `android.disallowKotlinSourceSets=false` para habilitar el funcionamiento de Hilt y KSP con la versión 9.3 del Android Gradle Plugin.

### Módulo 2: Estructura de Paquetes (`commit 8dc979e`)
- Creada estructura limpia:
  - `com.example.udice.data` (`model`, `local`, `remote`, `repository`)
  - `com.example.udice.ui` (un subpaquete por pantalla: `nuevapublicacion`, `perfil`, `preferencias`, además de `components`, `navigation`, `theme`)
  - `com.example.udice.di`

### Módulo 3: Modelos Compartidos de Dominio (`commit f9253c8`)
Ubicados en `com.example.udice.data.model`:
- `Rol.kt`: Enum `LECTOR`, `ASO`.
- `Facultad.kt`: Enum con las 8 dependencias/facultades del campus y métodos de resolución por nombre.
- `Categoria.kt`: Enum `EVENTO`, `CONVOCATORIA`, `CONFERENCIA`, `AVISO`, `OTRO`.
- `Alcance.kt`: Enum `GENERAL`, `CARRERA`.
- `User.kt`: `data class User(id, nombre, email, rol, facultad, carrera)`.
- `Post.kt`: `data class Post(id, autorId, autorNombre, titulo, descripcion, categoria, facultad, alcance, carrera?, fecha, imageUrl?)`.

### Módulo 4: Fuente Local Room y LocalDataSource (`commit d0c8d4a`)
Ubicados en `com.example.udice.data.local`:
- **Entidades:** `PostEntity`, `UserEntity`, `PreferenceEntity`, `SavedPostEntity`.
- **DAOs:** `PostDao`, `UserDao`, `PreferenceDao`, `SavedPostDao` con consultas optimizadas y `Flow`.
- **Base de Datos:** `UDiceDatabase : RoomDatabase()`. Se utilizó Room `2.8.4` para compatibilidad completa con el motor KSP2.
- **Mappers:** Funciones de extensión bidireccionales `toDomain()` y `toEntity()` para `Post` y `User`.
- **LocalDataSource:** Interfaz y su implementación `RoomLocalDataSource` inyectando `@IoDispatcher private val ioDispatcher: CoroutineDispatcher` y ejecutando cada operación con `withContext(ioDispatcher)` y `flowOn(ioDispatcher)`.

### Módulo 5: Fuente Remota y FakeRemoteDataSource (`commit 6afce1a`)
Ubicados en `com.example.udice.data.remote`:
- **Interfaz `RemoteDataSource`:** Métodos suspend para operaciones CRUD de `Post`, consulta de usuarios, gestión de preferencias y posts guardados.
- **Implementación `FakeRemoteDataSource`:**
  - Simula latencia de red real con `delay(500)`.
  - Operaciones seguras en memoria usando `Mutex` y `withContext(ioDispatcher)`.
  - Datos precargados: cuenta `ASO` ("user-aso-1"), cuenta `LECTOR` ("user-lector-1"), 5 publicaciones representativas con diversas categorías y preferencias predeterminadas.

### Módulo 6: SessionRepository Offline-First (`commit ece360d`)
Ubicado en `com.example.udice.data.repository`:
- **Interfaz `SessionRepository`:** `val currentUser: Flow<User?>`, `getCurrentUser()`, `switchUser(userId)`, `updateRole(newRole)`, `syncUsers()`, `logout()`.
- **Implementación `SessionRepositoryImpl`:** Recibe `LocalDataSource`, `RemoteDataSource` e `ioDispatcher` por constructor. Room es la fuente única de verdad para la sesión activa.

### Módulo 7: PostRepository con Validación y Sincronización (`commit 4677f64`)
Ubicado en `com.example.udice.data.repository`:
- **Interfaz `PostRepository`:** `posts: Flow<List<Post>>`, `observePost(id): Flow<Post?>`, `getPostById(id)`, `syncPosts()`, `createPost(post)`, `updatePost(post)`, `deletePost(id)`.
- **Implementación `PostRepositoryImpl`:**
  - Cero lógica en UI: las validaciones de rol y autoría residen dentro del repositorio usando `SessionRepository`.
  - `createPost`: Valida que el usuario tenga rol `Rol.ASO`. Inyecta `autorId` y `autorNombre`. Sincroniza en remoto y persiste en Room.
  - `updatePost` y `deletePost`: Validan rol `Rol.ASO` y autoría (`post.autorId == currentUser.id`).
  - `syncPosts`: Descarga de remoto y actualiza la fuente local Room.

### Módulo 8: PreferencesRepository (`commit c4eb8a3`)
Ubicado en `com.example.udice.data.repository`:
- **Interfaz `PreferencesRepository`:** `observePreferences(userId): Flow<Map<String, Boolean>>`, `getPreferences(userId)`, `updatePreferences(userId, preferences)`, `syncPreferences(userId)`.
- **Implementación `PreferencesRepositoryImpl`:** Persistencia offline-first en Room (`PreferenceEntity`) y sincronización bidireccional con el data source remoto.

---

## 4. Tareas Pendientes para la Próxima IA (Módulos 9 al 14)

La siguiente IA debe continuar con los módulos restantes, manteniendo la regla de **un commit por módulo**, **compilación previa verificada (`./gradlew assembleDebug`)** y **mensajes descriptivos en español**:

### Módulo 9: `SavedPostsRepository`
- **Archivos a crear en `com.example.udice.data.repository`:**
  - `SavedPostsRepository.kt` (interfaz):
    - `fun observeSavedPostIds(userId: String): Flow<Set<String>>`
    - `fun observeSavedPosts(userId: String): Flow<List<Post>>`
    - `fun isPostSaved(userId: String, postId: String): Flow<Boolean>`
    - `suspend fun toggleSavePost(userId: String, postId: String): Result<Boolean>`
    - `suspend fun syncSavedPosts(userId: String): Result<Unit>`
  - `SavedPostsRepositoryImpl.kt` (implementación):
    - Inyectar `LocalDataSource`, `RemoteDataSource`, `@IoDispatcher ioDispatcher`.
    - Conectar con `savedPostDao` a través de `LocalDataSource`.
- **Commit:** `feat(repository): implementa SavedPostsRepository con Flow para publicaciones guardadas`

### Módulo 10: Módulos Hilt en `di`
- **Archivos a crear en `com.example.udice.di`:**
  - `DatabaseModule.kt`:
    - `@Provides @Singleton fun provideDatabase(@ApplicationContext context: Context): UDiceDatabase`
    - `@Provides fun providePostDao(db: UDiceDatabase): PostDao`
    - `@Provides fun provideUserDao(db: UDiceDatabase): UserDao`
    - `@Provides fun providePreferenceDao(db: UDiceDatabase): PreferenceDao`
    - `@Provides fun provideSavedPostDao(db: UDiceDatabase): SavedPostDao`
  - `DataModule.kt` (o `DataSourceModule.kt`):
    - `@Binds @Singleton abstract fun bindLocalDataSource(impl: RoomLocalDataSource): LocalDataSource`
    - `@Binds @Singleton abstract fun bindRemoteDataSource(impl: FakeRemoteDataSource): RemoteDataSource`
  - `RepositoryModule.kt`:
    - `@Binds @Singleton abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository`
    - `@Binds @Singleton abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository`
    - `@Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository`
    - `@Binds @Singleton abstract fun bindSavedPostsRepository(impl: SavedPostsRepositoryImpl): SavedPostsRepository`
- **Commit:** `feat(di): agrega modulos Hilt para Room, DataSources y Repositorios con scope Singleton`

### Módulo 11: Datos Semilla (Local y Remoto)
- Asegurar que al arrancar en frío sin internet, Room tenga datos iniciales:
  - Crear un inicializador o callback Room (`RoomDatabase.Callback`) que inserte la cuenta ASO, la cuenta LECTOR y avisos iniciales en `UserDao` y `PostDao`.
- **Commit:** `feat(data): agrega datos semilla en base de datos local y sincronizacion inicial`

### Módulo 12: State Holders Esqueleto
- Crear `<Pantalla>StateHolder.kt` con `@HiltViewModel`, `UiState` sellado, `StateFlow` y eventos para cada pantalla existente:
  - `PreferenciasStateHolder.kt` en `ui.preferencias`
  - `PerfilStateHolder.kt` en `ui.perfil`
  - `NuevaPublicacionStateHolder.kt` en `ui.nuevapublicacion`
- Cada uno con lógica mínima y TODOs claros.
- **Commit:** `feat(ui): crea state holders esqueleto con UiState y eventos para pantallas existentes`

### Módulo 13: Patrón de Ejemplo Completo en `NuevaPublicacion`
- En `NuevaPublicacion`:
  - `NuevaPublicacionStateHolder.kt`: Manejo completo de validación (título/descripción no vacíos), verificación de rol `ASO`, llamada a `postRepository.createPost()`, estados `Loading`, `Success`, `Error`.
  - `NuevaPublicacionRoute` composable: obtiene `hiltViewModel<NuevaPublicacionStateHolder>()`, consume estado con `collectAsStateWithLifecycle()` y pasa al composable visual SOLO estado estable y lambdas `() -> Unit`.
  - Composable hijo `NuevaPublicacionScreen`: CERO referencias a ViewModel, solo pinta UI y despacha lambdas.
- **Commit:** `feat(ui): implementa patron de arquitectura completo en NuevaPublicacion`

### Módulo 14: TODOs Claros (`// TODO(RF n)`) para Trabajo Paralelo de 4 Personas
- Añadir comentarios estructurados `// TODO(RF n): ...`:
  - RF 1 / RF 2: Login y Registro
  - RF 3: Feed y filtrado por preferencias
  - RF 4: Reacciones / Me gusta
  - RF 5: Guardados / Bookmarks
  - RF 6: Detalle de publicación
  - RF 7: Preferencias
  - RF 8: Navegación y control de roles
- **Commit:** `docs(arch): agrega TODOs arquitecturales para modulos paralelos de desarrollo`

---

## 5. Reglas de Arquitectura Mandatorias a Recordar

1. **Inyección de dependencias SOLO mediante Hilt:** Prohibido `AppContainer` o fábricas manuales.
2. **Ningún composable hijo referencia un ViewModel:** Solo la función `@Composable` de ruta de cada pantalla obtiene `hiltViewModel()`. Desde ahí desciende únicamente estado inmutable y callbacks `() -> Unit`.
3. **Un `<Pantalla>StateHolder.kt` por pantalla:** Contiene el `@HiltViewModel`, `UiState` (sellado) y eventos.
4. **Cero lógica en la UI:** Las vistas solo pintan datos y reenvían eventos.
5. **Dos fuentes de datos:** Room como fuente única de verdad local y `RemoteDataSource` con `FakeRemoteDataSource`.
6. **Concurrencia:** Lectura y escritura en background usando `@IoDispatcher` inyectado.
7. **Inyección de interfaces:** Repositorios y ViewModels siempre reciben interfaces por constructor `@Inject constructor`.
8. **Git:** Cada módulo debe compilar limpiamente con `./gradlew assembleDebug` antes de hacer commit. No hacer push ni merge.

---

## Estado actualizado al 22-11-2026

La lista de pendientes de los módulos 9–14 incluida arriba era el plan al iniciar esta continuación. Ya se ejecutaron esos módulos en la rama `3rEtapa`; el historial final está al final de este documento. No se hizo push ni merge.

### Pantallas identificadas

Las rutas actuales siguen siendo **Preferencias**, **Nueva Publicación** y **Mi Perfil**. `UDiceHeader` y `UDiceBottomBar` son componentes compartidos, no pantallas. No se rediseñó el aspecto de ninguna pantalla.

### Commits de esta continuación y archivos

- `03f5c3e feat(repository): agrega SavedPostsRepository offline-first`: `SavedPostsRepository.kt`, `SavedPostsRepositoryImpl.kt`.
- `a57408b feat(di): configura bindings singleton para datos y repositorios`: `DatabaseModule.kt`, `DataSourceModule.kt`, `RepositoryModule.kt`.
- `2050887 feat(data): agrega datos semilla locales para uso offline`: `SeedDataCallback.kt`, actualización de `DatabaseModule.kt`.
- `095b206 feat(ui): crea state holders para pantallas existentes`: `NuevaPublicacionStateHolder.kt`, `PerfilStateHolder.kt`, `PreferenciasStateHolder.kt`.
- `dab5c66 feat(ui): conecta Nueva Publicacion con state holder y repositorio`: actualización de `UDiceApp.kt`, `NuevaPublicacionScreen.kt` y `NuevaPublicacionStateHolder.kt`.
- `019f923 docs(arch): agrega TODOs funcionales para trabajo paralelo`: actualización de `UDiceApp.kt`, `NavRoutes.kt`, y state holders de Perfil y Nueva Publicación.
- `54fb84c fix(arch): inyecta contratos en state holders restantes`: actualización de state holders de Preferencias y Perfil; la cuenta ASO queda activa en la semilla.

Cada commit se creó después de una ejecución exitosa de `./gradlew assembleDebug`. El commit `54fb84c` es una corrección de cierre posterior a los seis módulos planeados.

### Estado de arquitectura para quien continúe

- Hilt/KSP, Room, data sources, repositorios y `@IoDispatcher` están configurados por constructor y módulos Hilt con scope `Singleton`.
- Room es la fuente local e incluye semilla offline con dos cuentas y publicaciones. La cuenta ASO es la sesión inicial.
- `NuevaPublicacionRoute` obtiene su state holder con `hiltViewModel()`, recoge `StateFlow` con `collectAsStateWithLifecycle()` y pasa estado/eventos a la UI. Validación, autorización y publicación están en el state holder/repositorio.
- Preferencias y Perfil tienen state holders con interfaces inyectadas, UiState sellado y eventos, pero siguen siendo esqueletos. Sus pantallas aún no están conectadas a esos holders; falta migrarlas al patrón de ruta y retirar estado/acciones locales antes de considerar centralizado todo el estado de pantalla.
- El selector de rol y decisiones de navegación existentes siguen en `UDiceApp.kt`; `TODO(RF 8)` marca su integración futura con `SessionRepository`.
- Los TODOs RF 1–8 orientan login, registro, feed, reacciones, guardados, detalle, preferencias y navegación/roles.

### Historial final

```text
54fb84c fix(arch): inyecta contratos en state holders restantes
019f923 docs(arch): agrega TODOs funcionales para trabajo paralelo
dab5c66 feat(ui): conecta Nueva Publicacion con state holder y repositorio
095b206 feat(ui): crea state holders para pantallas existentes
2050887 feat(data): agrega datos semilla locales para uso offline
a57408b feat(di): configura bindings singleton para datos y repositorios
03f5c3e feat(repository): agrega SavedPostsRepository offline-first
c4eb8a3 feat(repository): implementa PreferencesRepository persistido localmente
4677f64 feat(repository): implementa PostRepository con sincronización y validación de permisos
ece360d feat(repository): implementa SessionRepository offline-first con Flow
6afce1a feat(remote): crea RemoteDataSource y FakeRemoteDataSource con simulación de red
d0c8d4a feat(local): implementa Room con entidades, DAOs, mappers y LocalDataSource
f9253c8 feat(model): agrega modelos compartidos User, Rol, Facultad, Categoria, Alcance y Post
8dc979e refactor(arch): organiza estructura de paquetes data, ui y di
4aa31b6 feat(di): configura Hilt, KSP, Application, EntryPoint y calificador @IoDispatcher
```
