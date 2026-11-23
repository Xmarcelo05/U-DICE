# Contexto de Sesión de Arquitectura U-DICE

Este documento resume todo el trabajo realizado en el proyecto Android **U-DICE** (Kotlin + Jetpack Compose, paquete `com.example.udice`) para que cualquier desarrollador del equipo o IA pueda continuar exactamente desde este punto sin perder el hilo de la arquitectura ni de las decisiones técnicas.

---

## 1. Estado Actual del Repositorio Git

- **Rama base:** `3rEtapa` (contiene la arquitectura base y módulos iniciales desarrollados por Fabián).
- **Rama activa actual:** `feature/freddy-preferencias-feed` (contiene la integración de Supabase y los requisitos funcionales de Freddy).
- **Cambios de la sesión actual:** RF 1, 2, 4 y 8 e insets de navegación del sistema.

---

## 2. Pantallas y Rutas Existentes en la Aplicación

Todas las pantallas están ubicadas en subpaquetes dedicados dentro de `ui/`, siguiendo el patrón de separación estricta **Route / StateHolder / Composable Visual**:

1. **Novedades / Feed Principal** (`com.example.udice.ui.feed.InicioFeedScreen.kt`):
   - Ruta: `NavRoutes.INICIO_FEED` (destino inicial después de autenticar).
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
   - Perfil de la cuenta activa con acceso a Mis publicaciones para ASO y opción de cerrar sesión.
5. **Componentes y Navegación Compartida**:
   - `com.example.udice.UDiceApp.kt`: `Scaffold` con `NavHost`, `UDiceHeader` y `UDiceBottomBar`.
   - `com.example.udice.ui.navigation.NavRoutes.kt`: Rutas del feed, preferencias, nueva publicación, perfil y mis publicaciones. La sesión determina si se muestra el formulario de acceso o el grafo privado.
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

### 2. RF 3 · Preferencias de facultad
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

## 6. Sesión, permisos y publicaciones propias

- **RF 1 · Registro.** `AuthScreen` solicita nombre, correo, contraseña, facultad y carrera. `AuthStateHolder` delega a `SessionRepository`, que valida y registra cuentas lectoras locales en Room. Las contraseñas se verifican con PBKDF2-HMAC-SHA256 y sal individual. La migración 2 → 3 agrega las columnas de credenciales y limpia las sesiones de la versión anterior.
- **RF 2 · Inicio de sesión.** `SessionRepository.currentUser` es la fuente de verdad de sesión y rol. `UDiceApp` monta el grafo privado cuando hay una sesión activa. El acceso local funciona sin conexión. La autenticación institucional y la provisión de cuentas ASO para producción quedan pendientes de integración con Supabase Auth y políticas de autorización del servidor.
- **RF 4 · Cierre de sesión.** La cabecera de todas las pantallas principales y Perfil exponen la acción. El repositorio limpia la sesión de Room y Compose retira el grafo privado. Preferencias, reacciones y marcadores del feed se consultan con el ID de la cuenta activa.
- **RF 8 · Mis publicaciones.** Desde Perfil, una cuenta ASO puede listar sus avisos, editar título, descripción y categoría, y confirmar la eliminación. `PostRepositoryImpl` comprueba el rol y la autoría antes de editar o eliminar. Room propaga los cambios al feed y a la lista propia. Los avisos de la cuenta ASO de demostración se gestionan localmente.
- **Barra inferior.** `UDiceBottomBar` aplica `navigationBarsPadding()` para reservar el espacio que Android reporta en modo gestual o de tres botones. `Scaffold` entrega el padding al contenido, que lo consume para evitar duplicarlo en destinos hijos.

### Diseño de acceso

`LoginScreen` y `RegisterScreen` recuperan las tarjetas, los iconos y el logotipo de `fixed-experimental`. El botón de acceso valida las credenciales y el rol proviene de la cuenta. Registro usa el selector de facultades y ambos formularios muestran los resultados de `AuthStateHolder`.

### Diseño de Mis publicaciones

`fixed-experimental` tenía una cuadrícula con avisos de ejemplo. Los controles de edición y eliminación se diseñaron sobre la pantalla actual. La lista usa tarjetas y colores del tema, acciones con icono, un formulario precargado y un diálogo de confirmación para eliminar. La lista sigue observando los avisos propios de Room.

### Comprobación manual en Android Studio

1. Ejecutar la variante `debug`. Registrar un lector, volver al login y probar una contraseña incorrecta y luego la correcta. Revisar que el lector ve Inicio, Preferencias y Perfil.
2. Cerrar sesión desde la cabecera y pulsar Atrás. Entrar con la cuenta de demostración `aso.sistemas@universidad.edu` y la contraseña `UDiceDemo2026!`. Esta cuenta de prueba está habilitada únicamente en `debug`.
3. Abrir Perfil → Mis publicaciones. Editar un aviso de demostración y comprobar el cambio en el feed. Eliminar un aviso y comprobar que desaparece de ambos sitios. Cerrar la app y volver a abrirla para comprobar la persistencia.
4. Revisar Inicio, Preferencias, Perfil y Nueva publicación con navegación por gestos y por tres botones. Comprobar que se pueden tocar todas las opciones en vertical y horizontal.

### Pendientes de la etapa

- **RF 5.** Guardar y aplicar el alcance general o específico de la carrera. La pantalla actual guarda facultades de interés.
- **RF 10 y RF 11.** Completar la pantalla de bóveda sobre `SavedPostsRepository.observeSavedPosts(userId)`. El marcador ya está conectado en el feed.
- **RF 12.** Añadir la ruta con el detalle completo del aviso.
- **Evidencias del Word.** Capturar los paquetes, la recolección de `StateFlow` con ciclo de vida, los repositorios con el dispatcher de IO y los módulos de Hilt.
