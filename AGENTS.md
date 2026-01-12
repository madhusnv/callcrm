# AGENTS.md - Android Project Guidelines

## Project Overview

| Attribute | Value |
|-----------|-------|
| Project | Educational Consultancy CRM |
| Platform | Android |
| Language | Kotlin 1.9+ |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Architecture | MVVM + Clean Architecture |
| UI | Jetpack Compose (Material 3) |

---

## ⚠️ CRITICAL RULES

### 1. NO DEPRECATED LIBRARIES
```
❌ NEVER USE:
- AsyncTask                    → Use Coroutines
- Loader                       → Use ViewModel + Flow
- LocalBroadcastManager        → Use SharedFlow/EventBus
- startActivityForResult()     → Use Activity Result API
- onActivityResult()           → Use registerForActivityResult()
- requestPermissions()         → Use Activity Result API
- Kotlin synthetics            → Use ViewBinding or Compose
- kotlin-android-extensions    → REMOVED in Kotlin 1.8
- kapt                         → Use KSP where possible
- LiveData (in new code)       → Use StateFlow/SharedFlow
- RxJava (in new code)         → Use Coroutines + Flow
- Gson                         → Use Kotlinx Serialization or Moshi
- Volley                       → Use Retrofit + OkHttp
- AsyncHttpClient              → Use Retrofit + OkHttp
- Universal Image Loader       → Use Coil
- Glide (in Compose)           → Use Coil
- ButterKnife                  → Use ViewBinding or Compose
- Dagger 2 (standalone)        → Use Hilt
- SharedPreferences            → Use DataStore
- Room with kapt               → Use Room with KSP
```

### 2. USE LATEST STABLE VERSIONS
```kotlin
// Check before using any library:
// 1. Is it actively maintained?
// 2. Is there a newer/better alternative?
// 3. Does it support Kotlin 1.9+?
// 4. Does it support Compose?
```

### 3. NO JAVA CODE
```
❌ Do not write Java files
✅ Kotlin only for all new code
```

---

## Tech Stack (Use These Versions)

### Core
```kotlin
kotlin = "1.9.22"
compose-bom = "2024.02.00"
compose-compiler = "1.5.10"
agp = "8.2.2"
```

### Dependencies
```kotlin
// DI
hilt = "2.50"
hilt-navigation-compose = "1.2.0"

// Network
retrofit = "2.9.0"
okhttp = "4.12.0"
kotlinx-serialization = "1.6.3"

// Database
room = "2.6.1"
datastore = "1.0.0"

// Async
coroutines = "1.8.0"
lifecycle = "2.7.0"

// Navigation
navigation-compose = "2.7.7"

// Image Loading
coil = "2.5.0"

// Firebase
firebase-bom = "32.7.2"

// Work
work-runtime = "2.9.0"

// Security
security-crypto = "1.1.0-alpha06"
```

---

## Project Structure

```
app/src/main/java/com/educonsult/crm/
├── EduConsultApp.kt                 # Application class
├── MainActivity.kt                  # Single Activity
│
├── di/                              # Hilt modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── LeadDao.kt
│   │   │   │   └── CallLogDao.kt
│   │   │   └── entity/
│   │   │       ├── LeadEntity.kt
│   │   │       └── CallLogEntity.kt
│   │   └── datastore/
│   │       └── UserPreferences.kt
│   │
│   ├── remote/
│   │   ├── api/
│   │   │   ├── AuthApi.kt
│   │   │   ├── LeadApi.kt
│   │   │   └── CallApi.kt
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   └── response/
│   │   └── interceptor/
│   │       ├── AuthInterceptor.kt
│   │       └── ApiKeyInterceptor.kt
│   │
│   └── repository/
│       ├── AuthRepositoryImpl.kt
│       ├── LeadRepositoryImpl.kt
│       └── CallRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── Lead.kt
│   │   ├── CallLog.kt
│   │   └── User.kt
│   ├── repository/
│   │   ├── AuthRepository.kt
│   │   ├── LeadRepository.kt
│   │   └── CallRepository.kt
│   └── usecase/
│       ├── auth/
│       ├── lead/
│       └── call/
│
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── components/                  # Reusable composables
│   │   ├── LoadingIndicator.kt
│   │   ├── ErrorMessage.kt
│   │   └── LeadCard.kt
│   ├── auth/
│   │   ├── LoginScreen.kt
│   │   └── LoginViewModel.kt
│   ├── leads/
│   │   ├── list/
│   │   │   ├── LeadListScreen.kt
│   │   │   └── LeadListViewModel.kt
│   │   └── detail/
│   │       ├── LeadDetailScreen.kt
│   │       └── LeadDetailViewModel.kt
│   └── dashboard/
│       ├── DashboardScreen.kt
│       └── DashboardViewModel.kt
│
├── services/
│   ├── CallMonitorService.kt
│   └── SyncService.kt
│
├── receivers/
│   ├── PhoneStateReceiver.kt
│   └── BootReceiver.kt
│
├── workers/
│   ├── SyncWorker.kt
│   └── ReminderWorker.kt
│
└── util/
    ├── Extensions.kt
    ├── Constants.kt
    └── Resource.kt
```

---

## Common Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build App Bundle
./gradlew bundleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check for lint errors
./gradlew lint

# Format code with ktlint
./gradlew ktlintFormat

# Check dependencies for updates
./gradlew dependencyUpdates

# Clean build
./gradlew clean
```

---

## Code Conventions

### Naming
```kotlin
// Classes: PascalCase
class LeadRepository
class LeadListViewModel
class LeadEntity

// Functions: camelCase
fun getLeadById(id: String): Lead
fun saveLeadToDatabase(lead: Lead)

// Variables: camelCase
val leadList: List<Lead>
var isLoading: Boolean

// Constants: SCREAMING_SNAKE_CASE
const val MAX_RETRY_COUNT = 3
const val API_TIMEOUT_SECONDS = 30L

// Compose: PascalCase for composables
@Composable
fun LeadCard(lead: Lead, onClick: () -> Unit)

// Files: PascalCase matching class name
LeadRepository.kt
LeadListScreen.kt
LeadEntity.kt
```

### Compose Guidelines
```kotlin
// ✅ DO: Use remember for expensive calculations
val sortedLeads = remember(leads) { leads.sortedBy { it.name } }

// ✅ DO: Use derivedStateOf for derived state
val hasLeads by remember { derivedStateOf { leads.isNotEmpty() } }

// ✅ DO: Use LaunchedEffect for side effects
LaunchedEffect(leadId) {
    viewModel.loadLead(leadId)
}

// ✅ DO: Use rememberSaveable for surviving config changes
var searchQuery by rememberSaveable { mutableStateOf("") }

// ❌ DON'T: Create objects in composable without remember
// This recreates on every recomposition
val dateFormatter = SimpleDateFormat("dd/MM/yyyy") // BAD

// ✅ DO: Remember the formatter
val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy") }

// ✅ DO: Hoist state when needed
@Composable
fun SearchBar(
    query: String,              // State hoisted up
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
)

// ✅ DO: Use Modifier parameter as first optional parameter
@Composable
fun LeadCard(
    lead: Lead,                 // Required params first
    onClick: () -> Unit,
    modifier: Modifier = Modifier,  // Modifier with default
    showCallButton: Boolean = true  // Other optional params
)
```

### ViewModel Guidelines
```kotlin
// ✅ DO: Use StateFlow for UI state
class LeadListViewModel @Inject constructor(
    private val getLeadsUseCase: GetLeadsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeadListUiState())
    val uiState: StateFlow<LeadListUiState> = _uiState.asStateFlow()

    // ✅ DO: Use sealed class for one-time events
    private val _events = Channel<LeadListEvent>()
    val events = _events.receiveAsFlow()

    fun loadLeads() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getLeadsUseCase()
                .onSuccess { leads ->
                    _uiState.update { it.copy(leads = leads, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                    _events.send(LeadListEvent.ShowError(error.message))
                }
        }
    }
}

// ✅ DO: Define UI state as data class
data class LeadListUiState(
    val leads: List<Lead> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedFilter: LeadFilter = LeadFilter.All
)

// ✅ DO: Define events as sealed class
sealed class LeadListEvent {
    data class ShowError(val message: String?) : LeadListEvent()
    data class NavigateToDetail(val leadId: String) : LeadListEvent()
}
```

### Repository Guidelines
```kotlin
// ✅ DO: Return Result or custom sealed class
interface LeadRepository {
    suspend fun getLeads(): Result<List<Lead>>
    suspend fun getLeadById(id: String): Result<Lead>
    suspend fun saveLead(lead: Lead): Result<String>
    fun observeLeads(): Flow<List<Lead>>
}

// ✅ DO: Implement offline-first pattern
class LeadRepositoryImpl @Inject constructor(
    private val leadDao: LeadDao,
    private val leadApi: LeadApi,
    private val networkMonitor: NetworkMonitor
) : LeadRepository {

    override fun observeLeads(): Flow<List<Lead>> {
        return leadDao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getLeads(): Result<List<Lead>> {
        return try {
            // Try network first
            if (networkMonitor.isConnected()) {
                val response = leadApi.getLeads()
                if (response.isSuccessful) {
                    val leads = response.body()?.data ?: emptyList()
                    // Cache to local DB
                    leadDao.insertAll(leads.map { it.toEntity() })
                    Result.success(leads.map { it.toDomain() })
                } else {
                    // Fall back to cache
                    getFromCache()
                }
            } else {
                // Offline - use cache
                getFromCache()
            }
        } catch (e: Exception) {
            getFromCache()
        }
    }

    private suspend fun getFromCache(): Result<List<Lead>> {
        val cached = leadDao.getAll()
        return if (cached.isNotEmpty()) {
            Result.success(cached.map { it.toDomain() })
        } else {
            Result.failure(Exception("No data available"))
        }
    }
}
```

---

## Permissions Handling

```kotlin
// ✅ DO: Use Activity Result API
@Composable
fun CallPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    val permissions = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE
    )

    LaunchedEffect(Unit) {
        if (permissions.all { 
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED 
        }) {
            onPermissionGranted()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}

// ❌ DON'T: Use deprecated requestPermissions
// override fun onRequestPermissionsResult(...) // DEPRECATED
```

---

## Network Error Handling

```kotlin
// ✅ DO: Create sealed class for API results
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String?) : ApiResult<Nothing>()
    data class NetworkError(val throwable: Throwable) : ApiResult<Nothing>()
    object Loading : ApiResult<Nothing>()
}

// ✅ DO: Create extension for safe API calls
suspend fun <T> safeApiCall(
    apiCall: suspend () -> Response<BaseResponse<T>>
): ApiResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.status == true && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(response.code(), body?.message)
            }
        } else {
            ApiResult.Error(response.code(), response.message())
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e)
    } catch (e: Exception) {
        ApiResult.Error(-1, e.message)
    }
}
```

---

## Testing

### Unit Tests
```kotlin
// Test ViewModels with Turbine
@Test
fun `loadLeads success updates state`() = runTest {
    // Given
    val leads = listOf(Lead(id = "1", name = "John"))
    coEvery { getLeadsUseCase() } returns Result.success(leads)

    // When
    viewModel.loadLeads()

    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertFalse(state.isLoading)
        assertEquals(leads, state.leads)
    }
}
```

### Compose UI Tests
```kotlin
@Test
fun leadCard_displaysLeadInfo() {
    val lead = Lead(id = "1", firstName = "John", phone = "1234567890")
    
    composeTestRule.setContent {
        LeadCard(lead = lead, onClick = {})
    }

    composeTestRule.onNodeWithText("John").assertIsDisplayed()
    composeTestRule.onNodeWithText("1234567890").assertIsDisplayed()
}
```

---

## DO's and DON'Ts

### ✅ DO
- Use Kotlin Coroutines for async operations
- Use Flow for reactive streams
- Use StateFlow in ViewModels
- Use Compose for all new UI
- Use Hilt for dependency injection
- Use Room with KSP for database
- Use DataStore for preferences
- Use Coil for image loading
- Use Material 3 components
- Handle configuration changes properly
- Test ViewModels and Repositories
- Use ProGuard/R8 for release builds
- Handle back navigation properly in Compose
- Use `collectAsStateWithLifecycle()` for flows in Compose

### ❌ DON'T
- Don't use deprecated APIs (see list above)
- Don't block the main thread
- Don't hardcode strings (use resources)
- Don't ignore errors silently
- Don't store sensitive data in plain SharedPreferences
- Don't use `GlobalScope` for coroutines
- Don't create ViewModels manually (use Hilt)
- Don't use `mutableStateOf` at top level (use remember)
- Don't pass Context to ViewModels
- Don't use Android framework classes in domain layer
- Don't make network calls on main thread
- Don't ignore lifecycle (use lifecycle-aware components)
- Don't hardcode colors (use Theme)
- Don't suppress lint warnings without justification

---

## Security Requirements

```kotlin
// ✅ DO: Use EncryptedSharedPreferences for sensitive data
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val securePrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)

// ✅ DO: Use certificate pinning
val certificatePinner = CertificatePinner.Builder()
    .add("api.educonsult.com", "sha256/AAAA...")
    .build()

// ✅ DO: Clear sensitive data on logout
fun logout() {
    securePrefs.edit().clear().apply()
    database.clearAllTables()
}

// ❌ DON'T: Log sensitive information
Log.d("Auth", "Token: $token") // NEVER DO THIS
```

---

## ProGuard Rules

```proguard
# Keep Retrofit
-keepattributes Signature
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep data classes for serialization
-keep class com.educonsult.crm.data.remote.dto.** { *; }
-keep class com.educonsult.crm.domain.model.** { *; }

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
```

---

## Quick Reference

### Create new feature
1. Create domain model in `domain/model/`
2. Create entity in `data/local/db/entity/`
3. Create DAO in `data/local/db/dao/`
4. Create API interface in `data/remote/api/`
5. Create DTOs in `data/remote/dto/`
6. Create repository interface in `domain/repository/`
7. Create repository impl in `data/repository/`
8. Create use cases in `domain/usecase/`
9. Create ViewModel in `ui/feature/`
10. Create Screen composable in `ui/feature/`
11. Add navigation in `ui/navigation/NavGraph.kt`
12. Add Hilt bindings in `di/` modules

### Add new API endpoint
1. Add method to API interface
2. Create request/response DTOs
3. Add mapper to convert DTO → Domain model
4. Add method to repository
5. Create use case if needed
6. Call from ViewModel

### Run before committing
```bash
./gradlew ktlintFormat
./gradlew lint
./gradlew test
./gradlew assembleDebug
```
