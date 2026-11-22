# Contexto de Sesión de Arquitectura U-DICE

Este documento resume todo el trabajo realizado en el proyecto Android **U-DICE** (Kotlin + Jetpack Compose, paquete `com.example.udice`) para que cualquier desarrollador del equipo o IA pueda continuar exactamente desde este punto sin perder el hilo de la arquitectura ni de las decisiones técnicas.

---

## 1. Estado Actual del Repositorio Git

- **Rama base:** `3rEtapa` (contiene la arquitectura base y módulos iniciales desarrollados por Fabián).
- **Rama activa actual:** `feature/freddy-preferencias-feed` (contiene la integración de Supabase y los requisitos funcionales de Freddy).
- **Estado del árbol de trabajo:** Limpio (`working tree clean`).
- **Compilación verificada:** Todos los commits fueron compilados exitosamente con `./gradlew assembleDebug` previo a cada commit (`BUILD SUCCESSFUL`).

---

## 2. Pantallas y Rutas Existentes en la Aplicación

Todas las pantallas están ubicadas en subpaquetes dedicados dentro de `ui/`, siguiendo el patrón de separación estricta **Route / StateHolder / Composable Visual**:

1. **Novedades / Feed Principal** (`com.example.udice.ui.feed.InicioFeedScreen.kt`):
   - Ruta: `NavRoutes.INICIO_FEED` (destino inicial de la app).
   - Feed reactivo con orden cronológico descendente (`fecha DESC`).
   - Filtro horizontal por chips de categoría (Todos, Eventos, Convocatorias, Conferencias, Avisos, Otros).
   - Filtro dinámico según las facultades activas en las preferencias del usuario.
   - Componente `PostCard` con autor, avatar, badges, conjunto de reacciones interactivas y botón de marcador.
2. **Preferencias** (`com.example.udice.ui.preferencias.PreferenciasScreen.kt`):
   - Ruta: `NavRoutes.PREFERENCIAS`.
   - Gestión de facultades y dependencias campus con switches reactivos.
   - Control de cambios (`hasChanges`) para habilitar guardado, persistencia en Room/Supabase y diálogo de confirmación.
3. **Nueva Publicación** (`com.example.udice.ui.nuevapublicacion.NuevaPublicacionScreen.kt`):
   - Ruta: `NavRoutes.NUEVA_PUBLICACION` (visible solo para rol `ASO`).
   - Formulario de creación con validación, selección de categoría y confirmación.
4. **Mi Perfil** (`com.example.udice.ui.perfil.PerfilScreen.kt`):
   - Ruta: `NavRoutes.PERFIL`.
   - Tarjeta de usuario con estadísticas y opciones de cierre de sesión.
5. **Componentes y Navegación Compartida**:
   - `com.example.udice.UDiceApp.kt`: `Scaffold` con `NavHost`, `UDiceHeader` y `UDiceBottomBar`.
   - `com.example.udice.ui.navigation.NavRoutes.kt`: Constantes `INICIO_FEED`, `PREFERENCIAS`, `NUEVA_PUBLICACION`, `PERFIL`.
   - `com.example.udice.ui.components.UDiceHeader.kt` y `UDiceBottomBar.kt`.

---

## 3. Resumen de lo Realizado por Fabián (Módulos 1 al 14 en `3rEtapa`)

### Módulo 1: Hilt, KSP y Dispatchers.IO (`commit 4aa31b6`)
- Hilt (`2.55`), KSP (`2.2.10-2.0.2`), `hilt-navigation-compose` (`1.2.0`).
- `UDiceApplication` con `@HiltAndroidApp`, `MainActivity` con `@AndroidEntryPoint`.
- Calificador `@IoDispatcher` y `CoroutinesModule` proveyendo `Dispatchers.IO`.

### Módulo 2: Estructura de Paquetes (`commit 8dc979e`)
- Separación limpia: `data` (`local`, `model`, `remote`, `repository`), `ui` (subpaquetes por pantalla) y `di`.

### Módulo 3: Modelos Compartidos de Dominio (`commit f9253c8`)
- `Rol.kt` (`LECTOR`, `ASO`), `Facultad.kt` (8 dependencias del campus), `Categoria.kt`, `Alcance.kt`, `User.kt` y `Post.kt`.

### Módulo 4: Fuente Local Room y LocalDataSource (`commit d0c8d4a`)
- Entidades `PostEntity`, `UserEntity`, `PreferenceEntity`, `SavedPostEntity`.
- DAOs correspondientes y base de datos `UDiceDatabase`.
- `RoomLocalDataSource` ejecutando operaciones con `withContext(ioDispatcher)`.

### Módulo 5: Fuente Remota y FakeRemoteDataSource (`commit 6afce1a`)
- Interfaz `RemoteDataSource` y `FakeRemoteDataSource` con retardo simulado (`delay(500)`).

### Módulos 6 al 10: Repositorios y DI Singleton (`commits ece360d` a `a57408b`)
- `SessionRepository`, `PostRepository`, `PreferencesRepository`, `SavedPostsRepository`.
- Módulos Hilt: `DatabaseModule`, `DataSourceModule`, `RepositoryModule`.

### Módulos 11 al 14: Semilla, State Holders y TODOs (`commits 2050887` a `54fb84c`)
- `SeedDataCallback` para datos en frío offline.
- State holders esqueleto y patrón de referencia en `NuevaPublicacion`.
- Inserción de comentarios estructurados `// TODO(RF n)` para desarrollo paralelo.

---

## 4. Sesión de Trabajo de Freddy Marcelo Bravo Ponce (en `feature/freddy-preferencias-feed`)

### 1. Infraestructura Supabase (Optimización para Plan Gratuito)
- **Esquema SQL (`supabase_schema.sql`):**
  - Tabla `admin_users`: Usuario inicial `admin@uleam.edu.ec` para altas, bajas y reseteo de cuentas de facultades.
  - Tabla `facultades_cuentas`: Cuentas publicadoras de las facultades (1 por facultad, rol `ASO`). Se simplificó retirando `carrera` ya que no aplica a nivel de facultad.
  - Tabla `usuarios`: Lectores (estudiantes, profesores) con su respectiva facultad y carrera.
  - Tablas `publicaciones`, `reacciones` y `preferencias_usuarios`.
  - Índices en `fecha DESC`, `facultad` y `categoria` para optimizar consultas del feed sin agotar cuotas.
- **Cliente HTTP y Data Source:**
  - `SupabaseConfig`: Base URL y Publishable Key segura (la Secret Key se mantiene fuera del repositorio/cliente).
  - `SupabaseHttpClient`: Cliente asíncrono con `HttpURLConnection` y `ioDispatcher`.
  - `SupabaseRemoteDataSource`: Implementa `RemoteDataSource` con fallback automático a Room / `FakeRemoteDataSource` ante caídas de red (*offline-first*).

### 2. RF 3 y RF 5 · Preferencias y Alcance Local
- `PreferenciasStateHolder`: Inyecta `PreferencesRepository` y `SessionRepository`.
  - Expone `PreferenciasUiState` (sellado), detecta reactivamente `hasChanges` comparando con la base de datos, maneja `isSaving`, guardado y cancelación.
- `PreferenciasScreen`: Composable visual 100% desacoplado sin instancias de ViewModel.
- `PreferenciasRoute`: Conecta `hiltViewModel()` con `collectAsStateWithLifecycle()`.

### 3. RF 9 · Reacciones Predefinidas
- Modelo de dominio `TipoReaccion` (`ME_GUSTA`, `UTIL`, `IMPORTANTE`, `INTERESANTE`), `Reaccion` y `ReaccionSummary`.
- Persistencia local: `ReaccionEntity`, `ReaccionDao` y migración a versión 2 de `UDiceDatabase`.
- Repositorio: `ReactionsRepository` y `ReactionsRepositoryImpl` con sincronización bidireccional en Supabase.
- Tarjeta de publicación (`PostCard`): Permite alternar la reacción del usuario y refleja los conteos reactivos.

### 4. RF 6 y RF 13 · Feed Cronológico y Filtros por Preferencias y Categorías
- `InicioFeedStateHolder`:
  - Combina flujos de publicaciones, preferencias de facultades del usuario, reacciones y publicaciones guardadas.
  - Orden cronológico descendente estricto por timestamp de fecha (`it.fecha`).
  - Filtro automático según las facultades marcadas como activas en las preferencias del usuario.
  - Filtro por chips horizontales de categorías.
- `PostCard`: Componente reutilizable con iniciales del autor, badges temáticos, reacciones y guardado.
- `InicioFeedScreen` e `InicioFeedRoute`: Consumo seguro con Compose y estado vacío amigable.
- Integración en `UDiceApp.kt` y `UDiceBottomBar.kt` como pantalla de inicio.

---

## 5. Historial Completo de Commits en `feature/freddy-preferencias-feed`

```text
e57f7a9 refactor(supabase): ajusta esquema y data source retirando campo carrera de facultades y publicaciones
5e3cb4b docs(context): documenta sesion de trabajo de Freddy con RF 3, 5, 6, 9 y 13
72caa66 feat(feed): implementa InicioFeedScreen, InicioFeedStateHolder y PostCard con reacciones y filtros por preferencia (RF 6, RF 9 y RF 13)
b155615 feat(reactions): implementa modelo de reacciones predefinidas, Room DAO y ReactionsRepository (RF 9)
5914c7c feat(ui): implementa PreferenciasStateHolder y PreferenciasRoute con persistencia local y remota (RF 3 y RF 5)
4a3785e feat(remote): implementa SupabaseRemoteDataSource y SupabaseHttpClient con fallback offline
25d735b fix(supabase): deshabilita RLS en tablas publicas para acceso desde app movil
8edf275 feat(supabase): agrega esquema SQL con tablas de admin, facultades, publicaciones, reacciones y preferencias
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

---

## 6. Pautas para el Siguiente Desarrollador

### Integrante: Leandro Jesus Veliz Ponce (Sesión y Permisos)
- **RF 1 & RF 2 (Registro y Login):** Conectar `RegisterScreen` y `LoginScreen` con sus StateHolders y `SessionRepository`.
- **RF 4 (Cierre de Sesión):** Conectar opción en `PerfilScreen` invocando `sessionRepository.logout()` y limpiando la pila de navegación.
- **RF 8 (Navegación y Roles):** Eliminar el selector de rol manual en `UDiceHeader` y asociar la navegación reactivamente a `SessionRepository.currentUser`. Proteger `NavRoutes.NUEVA_PUBLICACION` para que solo cuentas con rol `ASO` puedan ingresar.

### Integrante 4: (Bóveda y Detalle de Publicación)
- **RF 10 & RF 11 (Guardados):** Conectar `GuardadosScreen` observando `SavedPostsRepository.observeSavedPosts(userId)` para listar publicaciones marcadas.
- **RF 12 (Detalle Completo):** Crear ruta `NavRoutes.DETALLE_PUBLICACION + "/{postId}"` para ver la publicación en vista completa.
- **Evidencias para el Informe Word:** Tomar capturas de pantalla de:
  1. Consumo de StateFlow con `collectAsStateWithLifecycle()` en composables de ruta.
  2. State Hoisting en `PostCard` o `PreferenciasScreen`.
  3. Repositorio delegado a `@IoDispatcher`.
  4. Módulos Hilt en `di/`.
