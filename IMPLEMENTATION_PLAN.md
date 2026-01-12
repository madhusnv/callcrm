# Educational Consultancy CRM - Implementation Plan

## Project Overview

| Attribute | Details |
|-----------|---------|
| Project Name | EduConsult CRM |
| Platform | Android (Kotlin) |
| Architecture | MVVM + Clean Architecture |
| Duration | 16 Weeks (4 Months) |
| Team Size | 3-5 Developers |

---

# PHASE 1: FOUNDATION
**Duration: Weeks 1-4**  
**Goal: Project setup, authentication, and core infrastructure**

---

## Week 1: Project Setup & Architecture

### Day 1-2: Project Initialization

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 1.1 | Create Android project with Kotlin DSL | Dev 1 | 2 |
| 1.2 | Setup Git repository with branching strategy | Dev 1 | 1 |
| 1.3 | Configure build variants (debug, staging, release) | Dev 1 | 2 |
| 1.4 | Setup CI/CD pipeline (GitHub Actions/Bitrise) | DevOps | 4 |
| 1.5 | Create project documentation structure | Lead | 2 |

#### Deliverables
- [ ] Android project with proper package structure
- [ ] Git repository with main, develop, feature branches
- [ ] Build variants configured
- [ ] CI/CD pipeline for automated builds

#### Project Structure
```
com.educonsult.crm/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/educonsult/crm/
│   │   │   │   ├── EduConsultApp.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── di/
│   │   │   │   ├── data/
│   │   │   │   ├── domain/
│   │   │   │   ├── ui/
│   │   │   │   ├── services/
│   │   │   │   ├── receivers/
│   │   │   │   └── workers/
│   │   │   └── res/
│   │   ├── debug/
│   │   ├── staging/
│   │   └── release/
│   └── build.gradle.kts
├── buildSrc/
│   └── Dependencies.kt
├── gradle/
└── build.gradle.kts
```

### Day 3-4: Dependencies & DI Setup

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 1.6 | Add all project dependencies | Dev 1 | 3 |
| 1.7 | Setup Hilt dependency injection | Dev 1 | 4 |
| 1.8 | Create DI modules (App, Network, Database, Repository) | Dev 1 | 4 |
| 1.9 | Configure ProGuard rules | Dev 1 | 2 |

#### Dependencies List
```kotlin
// Core
androidx.core:core-ktx:1.12.0
androidx.lifecycle:lifecycle-runtime-ktx:2.7.0
androidx.activity:activity-compose:1.8.2

// Compose
androidx.compose:compose-bom:2024.01.00
androidx.compose.material3:material3
androidx.navigation:navigation-compose:2.7.6

// DI
com.google.dagger:hilt-android:2.50
androidx.hilt:hilt-navigation-compose:1.1.0

// Network
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Database
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// Firebase
com.google.firebase:firebase-bom:32.7.0
firebase-analytics, firebase-messaging, firebase-crashlytics

// Others
androidx.work:work-runtime-ktx:2.9.0
androidx.datastore:datastore-preferences:1.0.0
io.coil-kt:coil-compose:2.5.0
```

### Day 5: Base Classes & Utilities

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 1.10 | Create base ViewModel class | Dev 2 | 2 |
| 1.11 | Create base Repository class | Dev 2 | 2 |
| 1.12 | Create Result/Resource wrapper | Dev 2 | 2 |
| 1.13 | Create extension functions | Dev 2 | 2 |
| 1.14 | Setup logging utility (Timber) | Dev 2 | 1 |

#### Deliverables
- [ ] Base classes created
- [ ] Utility functions ready
- [ ] Logging configured

---

## Week 2: Authentication Module

### Day 1-2: Auth API & Data Layer

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 2.1 | Create AuthApi interface | Dev 1 | 3 |
| 2.2 | Create auth request/response DTOs | Dev 1 | 2 |
| 2.3 | Create UserEntity for Room | Dev 2 | 2 |
| 2.4 | Create UserDao | Dev 2 | 2 |
| 2.5 | Implement TokenManager with EncryptedPrefs | Dev 1 | 4 |
| 2.6 | Create AuthInterceptor for OkHttp | Dev 1 | 3 |

#### API Endpoints
```
POST /oauth/token          - Login
POST /auth/refresh         - Refresh token
POST /auth/register/sendOTP    - Send OTP
POST /auth/register/verifyOTP  - Verify OTP
POST /auth/register        - Register user
POST /auth/logout          - Logout
```

### Day 3-4: Auth Repository & Use Cases

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 2.7 | Create AuthRepository interface | Dev 1 | 1 |
| 2.8 | Implement AuthRepositoryImpl | Dev 1 | 4 |
| 2.9 | Create LoginUseCase | Dev 2 | 2 |
| 2.10 | Create RegisterUseCase | Dev 2 | 2 |
| 2.11 | Create LogoutUseCase | Dev 2 | 1 |
| 2.12 | Create RefreshTokenUseCase | Dev 2 | 2 |

### Day 5: Auth UI

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 2.13 | Create LoginScreen (Compose) | Dev 3 | 4 |
| 2.14 | Create RegisterScreen (Compose) | Dev 3 | 4 |
| 2.15 | Create OTPVerificationScreen | Dev 3 | 3 |
| 2.16 | Create AuthViewModel | Dev 2 | 3 |
| 2.17 | Setup auth navigation graph | Dev 3 | 2 |

#### Deliverables
- [ ] Complete authentication flow
- [ ] Token management with auto-refresh
- [ ] Secure storage for credentials
- [ ] Login/Register UI screens

---

## Week 3: Database Setup

### Day 1-2: Core Entities

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 3.1 | Create LeadEntity | Dev 1 | 3 |
| 3.2 | Create LeadStatusEntity | Dev 1 | 1 |
| 3.3 | Create LeadTagEntity | Dev 1 | 1 |
| 3.4 | Create ContactEntity | Dev 1 | 2 |
| 3.5 | Create CallLogEntity | Dev 2 | 2 |
| 3.6 | Create CallNoteEntity | Dev 2 | 2 |
| 3.7 | Create ReminderEntity | Dev 2 | 2 |

#### Lead Entity Schema
```kotlin
@Entity(tableName = "leads")
data class LeadEntity(
    @PrimaryKey val id: String,
    
    // Personal Info
    val firstName: String,
    val lastName: String?,
    val phone: String,
    val secondaryPhone: String?,
    val countryCode: Int,
    val email: String?,
    
    // Student Info
    val studentName: String?,
    val parentName: String?,
    val relationship: String?,
    val dateOfBirth: Long?,
    
    // Education
    val currentEducation: String?,
    val currentInstitution: String?,
    val percentage: Float?,
    val stream: String?,
    val graduationYear: Int?,
    
    // Inquiry
    val interestedCourses: String?,      // JSON
    val preferredCountries: String?,      // JSON
    val preferredInstitutions: String?,   // JSON
    val budgetMin: Long?,
    val budgetMax: Long?,
    val intakePreference: String?,
    
    // Status
    val statusId: String,
    val pipelineStage: String,
    val priority: String,
    val source: String,
    
    // Assignment
    val assignedTo: String?,
    val branchId: String?,
    
    // Follow-up
    val lastContactDate: Long?,
    val nextFollowUpDate: Long?,
    val reminderNote: String?,
    
    // Stats
    val totalCalls: Int = 0,
    val totalAttempts: Int = 0,
    
    // Custom
    val customFields: String?,
    
    // Meta
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val isDeleted: Boolean = false,
    val syncStatus: Int = 0
)
```

### Day 3-4: Course & Institution Entities

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 3.8 | Create CourseEntity | Dev 1 | 2 |
| 3.9 | Create InstitutionEntity | Dev 1 | 2 |
| 3.10 | Create CountryEntity | Dev 1 | 1 |
| 3.11 | Create NoteTemplateEntity | Dev 2 | 2 |
| 3.12 | Create MessageTemplateEntity | Dev 2 | 2 |
| 3.13 | Create ActivityLogEntity | Dev 2 | 2 |

### Day 5: DAOs & Database

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 3.14 | Create LeadDao with queries | Dev 1 | 4 |
| 3.15 | Create CallLogDao | Dev 1 | 2 |
| 3.16 | Create CourseDao | Dev 2 | 2 |
| 3.17 | Create TemplateDao | Dev 2 | 2 |
| 3.18 | Create AppDatabase class | Dev 1 | 2 |
| 3.19 | Create TypeConverters | Dev 1 | 2 |
| 3.20 | Create database migrations | Dev 1 | 2 |

#### Key Queries
```kotlin
// LeadDao
@Query("SELECT * FROM leads WHERE isDeleted = 0 ORDER BY createdAt DESC")
fun getAllLeads(): Flow<List<LeadEntity>>

@Query("SELECT * FROM leads WHERE statusId = :status AND isDeleted = 0")
fun getLeadsByStatus(status: String): Flow<List<LeadEntity>>

@Query("SELECT * FROM leads WHERE phone = :phone OR secondaryPhone = :phone LIMIT 1")
suspend fun getLeadByPhone(phone: String): LeadEntity?

@Query("SELECT * FROM leads WHERE nextFollowUpDate BETWEEN :start AND :end")
fun getFollowUpsForDateRange(start: Long, end: Long): Flow<List<LeadEntity>>

@Query("SELECT * FROM leads WHERE syncStatus = 0")
suspend fun getPendingSync(): List<LeadEntity>
```

#### Deliverables
- [ ] All entities created with proper indices
- [ ] DAOs with CRUD operations
- [ ] Database with migrations
- [ ] Type converters for complex types

---

## Week 4: Network Layer

### Day 1-2: API Interfaces

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 4.1 | Create LeadApi interface | Dev 1 | 4 |
| 4.2 | Create CallLogApi interface | Dev 1 | 2 |
| 4.3 | Create CourseApi interface | Dev 1 | 2 |
| 4.4 | Create EmployeeApi interface | Dev 2 | 2 |
| 4.5 | Create TemplateApi interface | Dev 2 | 2 |
| 4.6 | Create SettingsApi interface | Dev 2 | 1 |

#### Lead API Endpoints
```kotlin
interface LeadApi {
    @POST("lead/getData")
    suspend fun getLeads(@Body request: LeadListRequest): Response<BaseResponse<LeadListResponse>>
    
    @POST("lead/save")
    suspend fun saveLead(@Body request: SaveLeadRequest): Response<BaseResponse<String>>
    
    @POST("lead/saveNote")
    suspend fun saveNote(@Body request: LeadNoteRequest): Response<BaseResponse<LeadNoteResponse>>
    
    @POST("lead/status")
    suspend fun getStatuses(): Response<BaseResponse<List<LeadStatusResponse>>>
    
    @POST("lead/getByNumber")
    suspend fun getByNumber(@FieldMap params: Map<String, String>): Response<BaseResponse<LeadResponse>>
    
    @POST("lead/callLogs/getActive")
    suspend fun getCallHistory(
        @Query("leadId") leadId: String,
        @Query("pageNo") page: Int,
        @Query("pageSize") size: Int
    ): Response<BaseResponse<List<CallHistoryResponse>>>
    
    @POST("lead/form/getDynamicComponentDetails")
    suspend fun getDynamicFields(): Response<BaseResponse<DynamicFormResponse>>
    
    @POST("lead/allTags")
    suspend fun getAllTags(): Response<BaseResponse<List<String>>>
    
    @POST("lead/notContacted")
    suspend fun getNotContacted(@Body request: LeadListRequest): Response<BaseResponse<LeadListResponse>>
    
    @POST("lead/callBack/totalDue")
    suspend fun getTotalDueCallbacks(@Body request: CallbackRequest): Response<BaseResponse<Int>>
}
```

### Day 3-4: DTOs & Mappers

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 4.7 | Create request DTOs | Dev 1 | 3 |
| 4.8 | Create response DTOs | Dev 1 | 3 |
| 4.9 | Create DTO to Entity mappers | Dev 2 | 3 |
| 4.10 | Create Entity to Domain mappers | Dev 2 | 3 |
| 4.11 | Create Domain to DTO mappers | Dev 2 | 2 |

### Day 5: Network Module & Error Handling

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 4.12 | Create NetworkModule (Hilt) | Dev 1 | 3 |
| 4.13 | Create ApiErrorHandler | Dev 1 | 2 |
| 4.14 | Create NetworkMonitor | Dev 2 | 2 |
| 4.15 | Implement retry logic | Dev 2 | 2 |
| 4.16 | Add request/response logging | Dev 1 | 1 |
| 4.17 | Test all API endpoints | All | 4 |

#### Deliverables
- [ ] All API interfaces created
- [ ] DTOs and mappers complete
- [ ] Network module configured
- [ ] Error handling implemented
- [ ] API endpoints tested

---

# PHASE 2: CORE CRM FEATURES
**Duration: Weeks 5-8**  
**Goal: Lead management, call tracking, course management**

---

## Week 5: Lead Management - Data Layer

### Day 1-2: Lead Repository

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 5.1 | Create LeadRepository interface | Dev 1 | 2 |
| 5.2 | Implement LeadRepositoryImpl | Dev 1 | 6 |
| 5.3 | Implement offline-first logic | Dev 1 | 4 |
| 5.4 | Create lead sync logic | Dev 2 | 4 |

#### Repository Methods
```kotlin
interface LeadRepository {
    // Read
    fun getAllLeads(): Flow<List<Lead>>
    fun getLeadsByStatus(status: String): Flow<List<Lead>>
    fun getLeadsByPriority(priority: String): Flow<List<Lead>>
    suspend fun getLeadById(id: String): Lead?
    suspend fun getLeadByPhone(phone: String): Lead?
    fun searchLeads(query: String): Flow<List<Lead>>
    fun getFollowUpsForToday(): Flow<List<Lead>>
    fun getOverdueFollowUps(): Flow<List<Lead>>
    
    // Write
    suspend fun createLead(lead: Lead): Result<String>
    suspend fun updateLead(lead: Lead): Result<Unit>
    suspend fun deleteLead(id: String): Result<Unit>
    suspend fun updateStatus(id: String, status: String): Result<Unit>
    suspend fun updateFollowUp(id: String, date: Long, note: String?): Result<Unit>
    
    // Sync
    suspend fun syncLeads(): Result<Int>
    suspend fun getPendingSync(): List<Lead>
    suspend fun refreshFromServer(): Result<Unit>
}
```

### Day 3-4: Lead Use Cases

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 5.5 | Create GetLeadsUseCase | Dev 2 | 2 |
| 5.6 | Create GetLeadByIdUseCase | Dev 2 | 1 |
| 5.7 | Create CreateLeadUseCase | Dev 2 | 2 |
| 5.8 | Create UpdateLeadUseCase | Dev 2 | 2 |
| 5.9 | Create DeleteLeadUseCase | Dev 2 | 1 |
| 5.10 | Create SearchLeadsUseCase | Dev 2 | 2 |
| 5.11 | Create GetFollowUpsUseCase | Dev 2 | 2 |

### Day 5: Lead Status & Tags

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 5.12 | Create LeadStatusRepository | Dev 1 | 3 |
| 5.13 | Create LeadTagRepository | Dev 1 | 2 |
| 5.14 | Sync statuses from server | Dev 1 | 2 |
| 5.15 | Implement tag management | Dev 1 | 2 |

#### Lead Statuses (Educational Consultancy)
```kotlin
enum class LeadStatus(val id: String, val name: String, val color: Long, val order: Int) {
    NEW("new", "New Inquiry", 0xFF2196F3, 1),
    CONTACTED("contacted", "Contacted", 0xFF9C27B0, 2),
    COUNSELING_SCHEDULED("counseling_scheduled", "Counseling Scheduled", 0xFFFF9800, 3),
    COUNSELING_DONE("counseling_done", "Counseling Done", 0xFF4CAF50, 4),
    DOCS_PENDING("docs_pending", "Documents Pending", 0xFFE91E63, 5),
    APPLICATION_PROGRESS("app_progress", "Application In Progress", 0xFF00BCD4, 6),
    APPLICATION_SUBMITTED("app_submitted", "Application Submitted", 0xFF3F51B5, 7),
    OFFER_RECEIVED("offer", "Offer Received", 0xFF8BC34A, 8),
    VISA_PROCESSING("visa_processing", "Visa Processing", 0xFFFF5722, 9),
    VISA_APPROVED("visa_approved", "Visa Approved", 0xFF4CAF50, 10),
    ENROLLED("enrolled", "Enrolled", 0xFF009688, 11),
    DROPPED("dropped", "Dropped", 0xFF757575, 12),
    NOT_INTERESTED("not_interested", "Not Interested", 0xFF9E9E9E, 13)
}
```

---

## Week 6: Lead Management - UI Layer

### Day 1-2: Lead List Screen

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 6.1 | Create LeadListViewModel | Dev 2 | 4 |
| 6.2 | Create LeadListScreen (Compose) | Dev 3 | 6 |
| 6.3 | Create LeadCard component | Dev 3 | 3 |
| 6.4 | Implement status filter chips | Dev 3 | 2 |
| 6.5 | Implement search functionality | Dev 3 | 2 |
| 6.6 | Add pull-to-refresh | Dev 3 | 1 |

#### UI Components
```
LeadListScreen
├── TopAppBar (Search, Filter icons)
├── StatusFilterChips (horizontal scroll)
├── LeadList (LazyColumn)
│   └── LeadCard
│       ├── Avatar (with priority indicator)
│       ├── Name & Phone
│       ├── Status chip
│       ├── Interested courses
│       ├── Follow-up date
│       └── Call button
└── FAB (Add Lead)
```

### Day 3-4: Lead Detail Screen

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 6.7 | Create LeadDetailViewModel | Dev 2 | 4 |
| 6.8 | Create LeadDetailScreen | Dev 3 | 6 |
| 6.9 | Create BasicInfoSection | Dev 3 | 2 |
| 6.10 | Create StudentInfoSection | Dev 3 | 2 |
| 6.11 | Create InquiryDetailsSection | Dev 3 | 2 |
| 6.12 | Create CallHistorySection | Dev 3 | 2 |
| 6.13 | Create NotesSection | Dev 3 | 2 |
| 6.14 | Create ActivityTimeline | Dev 3 | 2 |

### Day 5: Add/Edit Lead Screen

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 6.15 | Create AddEditLeadViewModel | Dev 2 | 3 |
| 6.16 | Create AddEditLeadScreen | Dev 3 | 5 |
| 6.17 | Implement form validation | Dev 3 | 2 |
| 6.18 | Create dynamic form fields | Dev 3 | 3 |
| 6.19 | Implement country/course pickers | Dev 3 | 2 |

#### Dynamic Form Fields
```kotlin
sealed class FormField {
    data class Text(
        val key: String,
        val label: String,
        val required: Boolean,
        val maxLength: Int?,
        val inputType: InputType
    ) : FormField()
    
    data class Dropdown(
        val key: String,
        val label: String,
        val options: List<String>,
        val required: Boolean
    ) : FormField()
    
    data class MultiSelect(
        val key: String,
        val label: String,
        val options: List<String>,
        val maxSelections: Int?
    ) : FormField()
    
    data class Date(
        val key: String,
        val label: String,
        val minDate: Long?,
        val maxDate: Long?
    ) : FormField()
    
    data class Currency(
        val key: String,
        val label: String,
        val currencyCode: String
    ) : FormField()
}
```

---

## Week 7: Call Tracking Module

### Day 1-2: Call Log Repository

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 7.1 | Create CallLogRepository interface | Dev 1 | 2 |
| 7.2 | Implement CallLogRepositoryImpl | Dev 1 | 4 |
| 7.3 | Create device call log reader | Dev 1 | 4 |
| 7.4 | Implement call log sync | Dev 1 | 3 |
| 7.5 | Create CallNoteRepository | Dev 2 | 3 |

### Day 3-4: Call Monitor Service

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 7.6 | Create CallMonitorService | Dev 1 | 6 |
| 7.7 | Create PhoneStateReceiver | Dev 1 | 3 |
| 7.8 | Implement incoming call detection | Dev 1 | 2 |
| 7.9 | Create lead lookup on incoming call | Dev 1 | 2 |
| 7.10 | Handle call end event | Dev 1 | 2 |
| 7.11 | Create RebootReceiver (auto-start on boot) | Dev 1 | 1 |

#### Service Implementation
```kotlin
class CallMonitorService : Service() {
    
    companion object {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "call_monitor"
    }
    
    private var currentCallState = CallState.IDLE
    private var currentNumber: String? = null
    private var callStartTime: Long = 0
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        registerPhoneStateReceiver()
    }
    
    private fun handleIncomingCall(number: String) {
        currentNumber = number
        callStartTime = System.currentTimeMillis()
        currentCallState = CallState.RINGING
        
        // Check if lead exists
        CoroutineScope(Dispatchers.IO).launch {
            val lead = leadRepository.getLeadByPhone(number)
            if (lead != null) {
                showLeadPopup(lead)
            }
        }
    }
    
    private fun handleCallConnected() {
        currentCallState = CallState.CONNECTED
        callStartTime = System.currentTimeMillis()
    }
    
    private fun handleCallEnded() {
        val duration = (System.currentTimeMillis() - callStartTime) / 1000
        
        CoroutineScope(Dispatchers.IO).launch {
            // Log the call
            logCall(currentNumber, duration, currentCallState)
            
            // Show note popup if call was connected
            if (currentCallState == CallState.CONNECTED && duration > 10) {
                showNotePopup(currentNumber)
            }
            
            // Sync call logs
            syncCallLogs()
        }
        
        currentCallState = CallState.IDLE
        currentNumber = null
    }
}
```

#### RebootReceiver Implementation
> **Purpose:** Automatically restart `CallMonitorService` when device boots, ensuring call tracking resumes without user intervention.

```kotlin
class RebootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if user is logged in and service should run
            val goAsync = goAsync()
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val isLoggedIn = userPreferences.isLoggedIn()
                    val isServiceEnabled = userPreferences.isCallMonitorEnabled()
                    
                    if (isLoggedIn && isServiceEnabled) {
                        val serviceIntent = Intent(context, CallMonitorService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                        Timber.d("CallMonitorService started after boot")
                    }
                } finally {
                    goAsync.finish()
                }
            }
        }
    }
}
```

#### AndroidManifest Additions for RebootReceiver
```xml
<!-- Permission for boot completed -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

<!-- RebootReceiver registration -->
<receiver
    android:name=".receivers.RebootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED"/>
        <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
        <!-- HTC devices -->
        <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
    </intent-filter>
</receiver>
```


### Day 5: Note Popup

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 7.11 | Create NotePopUpActivity | Dev 3 | 3 |
| 7.12 | Create NotePopupScreen (Compose) | Dev 3 | 4 |
| 7.13 | Create NotePopupViewModel | Dev 2 | 2 |
| 7.14 | Implement quick note templates | Dev 3 | 2 |
| 7.15 | Add status update option | Dev 3 | 2 |
| 7.16 | Add follow-up scheduling | Dev 3 | 2 |

---

## Week 8: Course & Institution Module

### Day 1-2: Course Repository

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 8.1 | Create CourseRepository interface | Dev 1 | 2 |
| 8.2 | Implement CourseRepositoryImpl | Dev 1 | 4 |
| 8.3 | Create InstitutionRepository | Dev 1 | 3 |
| 8.4 | Create CountryRepository | Dev 1 | 2 |
| 8.5 | Implement course search | Dev 1 | 3 |

### Day 3-4: Course UI

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 8.6 | Create CourseListScreen | Dev 3 | 4 |
| 8.7 | Create CourseDetailScreen | Dev 3 | 4 |
| 8.8 | Create InstitutionListScreen | Dev 3 | 3 |
| 8.9 | Create InstitutionDetailScreen | Dev 3 | 3 |
| 8.10 | Create course filters UI | Dev 3 | 2 |

### Day 5: Course Recommendation

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 8.11 | Create CourseRecommendationUseCase | Dev 2 | 4 |
| 8.12 | Implement matching algorithm | Dev 2 | 4 |
| 8.13 | Create RecommendationCard UI | Dev 3 | 2 |
| 8.14 | Integrate with lead detail | Dev 3 | 2 |

#### Recommendation Algorithm
```kotlin
class CourseRecommendationEngine {
    
    fun calculateMatchScore(lead: Lead, course: Course): Float {
        var score = 0f
        var maxScore = 0f
        
        // Budget Match (30 points)
        maxScore += 30f
        if (lead.budgetMax != null && course.tuitionFee <= lead.budgetMax) {
            score += 30f
        } else if (lead.budgetMax != null) {
            val ratio = lead.budgetMax.toFloat() / course.tuitionFee
            score += (30f * ratio).coerceIn(0f, 30f)
        }
        
        // Country Preference (25 points)
        maxScore += 25f
        if (lead.preferredCountries?.contains(course.country) == true) {
            score += 25f
        }
        
        // Intake Match (20 points)
        maxScore += 20f
        if (course.intakes.any { it.equals(lead.intakePreference, ignoreCase = true) }) {
            score += 20f
        }
        
        // Stream Relevance (25 points)
        maxScore += 25f
        if (isStreamRelevant(lead.stream, course)) {
            score += 25f
        }
        
        return (score / maxScore) * 100
    }
}
```

---

# PHASE 3: ADVANCED FEATURES
**Duration: Weeks 9-12**  
**Goal: Call recording, templates, analytics, offline sync**

---

## Week 9: Call Recording Pipeline & Templates

> **Note:** Due to Android restrictions (especially Android 9+), apps cannot directly record calls.
> We adopt the **Find → Compress → Upload** approach: find recordings made by the phone's native
> recorder or third-party apps, compress them using FFmpeg, then upload to cloud storage.

### Day 1-2: Recording Find Worker

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 9.1 | Create RecordingFindWorker (WorkManager) | Dev 1 | 4 |
| 9.2 | Create RecordingPathManager (suggested paths) | Dev 1 | 3 |
| 9.3 | Implement device-specific path detection | Dev 1 | 3 |
| 9.4 | Create CallRecordingEntity for Room | Dev 2 | 2 |
| 9.5 | Create RecordingDao with queries | Dev 2 | 2 |
| 9.6 | Implement recording-to-call matching | Dev 1 | 4 |

#### Recording Entity Schema
```kotlin
@Entity(tableName = "call_recordings")
data class CallRecordingEntity(
    @PrimaryKey val id: String,
    val callLogId: String,               // Link to call log
    val phoneNumber: String,
    val originalFilePath: String,        // Original file found
    val compressedFilePath: String?,     // Compressed file path
    val originalSize: Long,
    val compressedSize: Long?,
    val duration: Int,                   // Seconds
    val callTimestamp: Long,             // When call occurred
    val recordingFoundAt: Long,          // When we found it
    val status: String,                  // found, compressing, compressed, uploading, uploaded, failed
    val uploadUrl: String?,              // Cloud URL after upload
    val uploadProgress: Int = 0,
    val retryCount: Int = 0,
    val lastError: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: Int = 0
)
```

#### Recording Path Detection
```kotlin
@Singleton
class RecordingPathManager @Inject constructor(
    private val context: Context,
    private val api: RecordingApi,
    private val dataStore: UserPreferences
) {
    // Default paths by manufacturer
    private val defaultPaths = mapOf(
        "samsung" to listOf(
            "/storage/emulated/0/Recordings/Call",
            "/storage/emulated/0/Call",
            "/storage/emulated/0/Record/Call"
        ),
        "xiaomi" to listOf(
            "/storage/emulated/0/MIUI/sound_recorder/call_rec",
            "/storage/emulated/0/Recordings/Call"
        ),
        "oneplus" to listOf(
            "/storage/emulated/0/Record/PhoneRecord",
            "/storage/emulated/0/Recordings/Call"
        ),
        "oppo" to listOf(
            "/storage/emulated/0/Recordings",
            "/storage/emulated/0/Record/Call"
        ),
        "vivo" to listOf(
            "/storage/emulated/0/Record/Call",
            "/storage/emulated/0/Recordings"
        ),
        "realme" to listOf(
            "/storage/emulated/0/Recordings",
            "/storage/emulated/0/Music/Recordings"
        ),
        "default" to listOf(
            "/storage/emulated/0/Recordings/Call",
            "/storage/emulated/0/Call",
            "/storage/emulated/0/Record",
            "/storage/emulated/0/Recordings"
        )
    )
    
    suspend fun getSuggestedPaths(): List<String> {
        // Try server-provided paths first
        return try {
            val response = api.getSuggestedPaths(getDeviceInfo())
            if (response.isSuccessful && response.body()?.data != null) {
                response.body()!!.data!!
            } else {
                getLocalSuggestedPaths()
            }
        } catch (e: Exception) {
            getLocalSuggestedPaths()
        }
    }
    
    private fun getLocalSuggestedPaths(): List<String> {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return defaultPaths[manufacturer] ?: defaultPaths["default"]!!
    }
    
    private fun getDeviceInfo(): DeviceInfo = DeviceInfo(
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        brand = Build.BRAND,
        sdkVersion = Build.VERSION.SDK_INT
    )
}
```

#### RecordingFindWorker Implementation
```kotlin
@HiltWorker
class RecordingFindWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recordingPathManager: RecordingPathManager,
    private val callLogRepository: CallLogRepository,
    private val recordingDao: RecordingDao,
    private val notificationManager: RecordingNotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            setForeground(getForegroundInfo())
            
            val suggestedPaths = recordingPathManager.getSuggestedPaths()
            val recentCalls = callLogRepository.getCallsWithoutRecording(
                since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
            )
            
            var foundCount = 0
            for (call in recentCalls) {
                val recording = findRecordingForCall(call, suggestedPaths)
                if (recording != null) {
                    recordingDao.insert(recording)
                    foundCount++
                }
            }
            
            if (foundCount > 0) {
                // Trigger compress worker
                enqueueCompressWork()
            }
            
            Result.success(workDataOf("found_count" to foundCount))
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
    
    private suspend fun findRecordingForCall(
        call: CallLogEntity, 
        paths: List<String>
    ): CallRecordingEntity? {
        val callTime = call.timestamp
        val phoneNumber = call.phoneNumber.takeLast(10)
        
        for (path in paths) {
            val dir = File(path)
            if (!dir.exists()) continue
            
            val candidates = dir.listFiles { file ->
                file.isFile &&
                isAudioFile(file) &&
                isWithinTimeWindow(file.lastModified(), callTime, 60_000) &&
                (file.name.contains(phoneNumber) || matchesByTimestamp(file, callTime))
            }
            
            candidates?.maxByOrNull { it.lastModified() }?.let { file ->
                return createRecordingEntity(call, file)
            }
        }
        return null
    }
    
    private fun isAudioFile(file: File): Boolean {
        val ext = file.extension.lowercase()
        return ext in listOf("mp3", "m4a", "aac", "wav", "amr", "3gp", "ogg")
    }
    
    private fun isWithinTimeWindow(fileTime: Long, callTime: Long, windowMs: Long): Boolean {
        return abs(fileTime - callTime) <= windowMs
    }
}
```

### Day 3-4: Recording Compress Worker

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 9.7 | Add FFmpegKit dependency | Dev 1 | 1 |
| 9.8 | Create RecordingCompressWorker | Dev 1 | 6 |
| 9.9 | Implement FFmpeg compression pipeline | Dev 1 | 4 |
| 9.10 | Handle compression progress/notification | Dev 2 | 3 |
| 9.11 | Create compression retry logic | Dev 2 | 2 |

#### FFmpegKit Dependency
```kotlin
// Add to build.gradle.kts
implementation("com.arthenica:ffmpeg-kit-audio:6.0-2")
```

#### RecordingCompressWorker Implementation
```kotlin
@HiltWorker
class RecordingCompressWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recordingDao: RecordingDao,
    private val fileManager: RecordingFileManager,
    private val notificationManager: RecordingNotificationManager
) : CoroutineWorker(context, params) {

    companion object {
        const val TARGET_BITRATE = "32k"          // 32kbps for small file size
        const val SAMPLE_RATE = 22050             // 22.05kHz is sufficient for voice
        const val MIN_FILE_SIZE_BYTES = 10_000L   // 10KB minimum
        const val MAX_FILE_SIZE_BYTES = 50_000_000L // 50MB max
    }

    override suspend fun doWork(): Result {
        return try {
            setForeground(getForegroundInfo())
            
            val pendingRecordings = recordingDao.getByStatus("found")
            var successCount = 0
            
            pendingRecordings.forEachIndexed { index, recording ->
                updateProgress(index, pendingRecordings.size)
                
                when (compressRecording(recording)) {
                    CompressResult.Success -> successCount++
                    CompressResult.FileTooLarge -> {
                        recordingDao.updateStatus(recording.id, "failed", "File too large")
                    }
                    CompressResult.Failed -> {
                        if (recording.retryCount < 3) {
                            recordingDao.incrementRetry(recording.id)
                        } else {
                            recordingDao.updateStatus(recording.id, "failed", "Max retries exceeded")
                        }
                    }
                }
            }
            
            if (successCount > 0) {
                enqueueUploadWork()
            }
            
            Result.success(workDataOf("compressed_count" to successCount))
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
    
    private suspend fun compressRecording(recording: CallRecordingEntity): CompressResult {
        val sourceFile = File(recording.originalFilePath)
        if (!sourceFile.exists()) return CompressResult.Failed
        
        // Skip if already small enough
        if (sourceFile.length() < MIN_FILE_SIZE_BYTES) {
            recordingDao.updateCompressed(
                id = recording.id,
                compressedPath = recording.originalFilePath,
                compressedSize = sourceFile.length()
            )
            return CompressResult.Success
        }
        
        val outputFile = fileManager.createCompressedFile(recording.id)
        val inputUri = fileManager.getContentUri(sourceFile)
        
        // FFmpeg command: convert to MP3 with low bitrate
        val command = buildString {
            append("-i ${FFmpegKitConfig.getSafParameterForRead(applicationContext, inputUri)} ")
            append("-vn ")                          // No video
            append("-ar $SAMPLE_RATE ")             // Sample rate
            append("-acodec libmp3lame ")           // MP3 codec
            append("-b:a $TARGET_BITRATE ")         // Bitrate
            append(outputFile.absolutePath)
        }
        
        return suspendCancellableCoroutine { cont ->
            FFmpegKit.executeAsync(command) { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    val compressedSize = outputFile.length()
                    
                    if (compressedSize > MAX_FILE_SIZE_BYTES) {
                        outputFile.delete()
                        cont.resume(CompressResult.FileTooLarge)
                    } else {
                        runBlocking {
                            recordingDao.updateCompressed(
                                id = recording.id,
                                compressedPath = outputFile.absolutePath,
                                compressedSize = compressedSize
                            )
                        }
                        cont.resume(CompressResult.Success)
                    }
                } else {
                    outputFile.delete()
                    cont.resume(CompressResult.Failed)
                }
            }
        }
    }
    
    sealed class CompressResult {
        object Success : CompressResult()
        object FileTooLarge : CompressResult()
        object Failed : CompressResult()
    }
}
```

### Day 5: Recording Upload Worker

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 9.12 | Create RecordingUploadWorker | Dev 2 | 5 |
| 9.13 | Implement presigned URL upload | Dev 2 | 3 |
| 9.14 | Handle upload progress tracking | Dev 2 | 2 |
| 9.15 | Create upload retry with exponential backoff | Dev 2 | 2 |

#### RecordingUploadWorker Implementation
```kotlin
@HiltWorker
class RecordingUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recordingDao: RecordingDao,
    private val recordingApi: RecordingApi,
    private val networkMonitor: NetworkMonitor,
    private val notificationManager: RecordingNotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!networkMonitor.isConnected()) {
            return Result.retry()
        }
        
        return try {
            setForeground(getForegroundInfo())
            
            val pendingRecordings = recordingDao.getByStatus("compressed")
            var successCount = 0
            
            for (recording in pendingRecordings) {
                recordingDao.updateStatus(recording.id, "uploading")
                
                when (uploadRecording(recording)) {
                    UploadResult.Success -> successCount++
                    UploadResult.NetworkError -> {
                        recordingDao.updateStatus(recording.id, "compressed")
                        return Result.retry()
                    }
                    UploadResult.Failed -> {
                        if (recording.retryCount < 5) {
                            recordingDao.incrementRetry(recording.id)
                            recordingDao.updateStatus(recording.id, "compressed")
                        } else {
                            recordingDao.updateStatus(recording.id, "failed", "Upload failed")
                        }
                    }
                }
            }
            
            Result.success(workDataOf("uploaded_count" to successCount))
        } catch (e: Exception) {
            if (runAttemptCount < 5) Result.retry() else Result.failure()
        }
    }
    
    private suspend fun uploadRecording(recording: CallRecordingEntity): UploadResult {
        val file = File(recording.compressedFilePath ?: recording.originalFilePath)
        if (!file.exists()) return UploadResult.Failed
        
        return try {
            // 1. Get presigned upload URL from backend
            val presignedResponse = recordingApi.getUploadUrl(
                callLogId = recording.callLogId,
                fileName = file.name,
                fileSize = file.length(),
                contentType = "audio/mpeg"
            )
            
            if (!presignedResponse.isSuccessful) return UploadResult.Failed
            
            val presignedUrl = presignedResponse.body()?.data?.uploadUrl 
                ?: return UploadResult.Failed
            
            // 2. Upload to S3/R2 using presigned URL
            val requestBody = ProgressRequestBody(file, "audio/mpeg") { progress ->
                runBlocking {
                    recordingDao.updateProgress(recording.id, progress)
                }
            }
            
            val uploadResponse = recordingApi.uploadToPresignedUrl(
                presignedUrl, 
                requestBody
            )
            
            if (uploadResponse.isSuccessful) {
                // 3. Confirm upload to backend
                val confirmResponse = recordingApi.confirmUpload(
                    callLogId = recording.callLogId,
                    recordingId = recording.id
                )
                
                if (confirmResponse.isSuccessful) {
                    val cloudUrl = confirmResponse.body()?.data?.url
                    recordingDao.updateUploaded(recording.id, cloudUrl)
                    
                    // Optionally delete local compressed file
                    file.delete()
                    
                    UploadResult.Success
                } else {
                    UploadResult.Failed
                }
            } else {
                UploadResult.Failed
            }
        } catch (e: IOException) {
            UploadResult.NetworkError
        } catch (e: Exception) {
            UploadResult.Failed
        }
    }
    
    sealed class UploadResult {
        object Success : UploadResult()
        object NetworkError : UploadResult()
        object Failed : UploadResult()
    }
}
```

#### Progress Request Body
```kotlin
class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val onProgress: (Int) -> Unit
) : RequestBody() {
    
    override fun contentType() = contentType.toMediaType()
    override fun contentLength() = file.length()
    
    override fun writeTo(sink: BufferedSink) {
        val total = file.length()
        var uploaded = 0L
        
        file.inputStream().source().buffer().use { source ->
            var read: Long
            while (source.read(sink.buffer, 8192).also { read = it } != -1L) {
                uploaded += read
                sink.flush()
                val progress = ((uploaded * 100) / total).toInt()
                onProgress(progress)
            }
        }
    }
}
```

### Recording Worker Chain Setup
```kotlin
@Singleton
class RecordingWorkManager @Inject constructor(
    private val workManager: WorkManager
) {
    fun scheduleRecordingPipeline() {
        // Periodic find work (every 15 minutes when app is in use)
        val findRequest = PeriodicWorkRequestBuilder<RecordingFindWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "recording_find",
            ExistingPeriodicWorkPolicy.KEEP,
            findRequest
        )
    }
    
    fun enqueueCompressAndUpload() {
        val compressWork = OneTimeWorkRequestBuilder<RecordingCompressWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()
        
        val uploadWork = OneTimeWorkRequestBuilder<RecordingUploadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        workManager
            .beginWith(compressWork)
            .then(uploadWork)
            .enqueue()
    }
}

### Week 9 Day 5 (cont): Message Templates

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 9.16 | Create TemplateRepository | Dev 1 | 3 |
| 9.17 | Create TemplateProcessor | Dev 1 | 3 |
| 9.18 | Create TemplateListScreen | Dev 3 | 3 |
| 9.19 | Create TemplateEditorScreen | Dev 3 | 3 |
| 9.20 | Implement WhatsApp share | Dev 2 | 2 |

#### Template Variables
```
{{student_name}}      - Student's name
{{parent_name}}       - Parent's name
{{first_name}}        - Lead's first name
{{course_name}}       - Course name
{{institution_name}}  - Institution name
{{country}}           - Country name
{{counselor_name}}    - Assigned counselor
{{counselor_phone}}   - Counselor's phone
{{follow_up_date}}    - Next follow-up date
{{intake}}            - Intake period
{{fee}}               - Course fee
{{deadline}}          - Application deadline
```

---

## Week 10: Analytics & Dashboard

### Day 1-2: Dashboard Repository

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 10.1 | Create DashboardRepository | Dev 1 | 4 |
| 10.2 | Create complex SQL queries | Dev 1 | 4 |
| 10.3 | Create DashboardUseCase | Dev 2 | 3 |
| 10.4 | Implement caching for stats | Dev 2 | 2 |

#### Dashboard Queries
```kotlin
@Dao
interface DashboardDao {
    
    @Query("""
        SELECT 
            COUNT(*) as totalLeads,
            SUM(CASE WHEN DATE(createdAt/1000, 'unixepoch') = DATE('now') THEN 1 ELSE 0 END) as newToday,
            SUM(CASE WHEN createdAt >= :weekStart THEN 1 ELSE 0 END) as newThisWeek,
            SUM(CASE WHEN nextFollowUpDate BETWEEN :todayStart AND :todayEnd THEN 1 ELSE 0 END) as pendingFollowUps,
            SUM(CASE WHEN nextFollowUpDate < :todayStart THEN 1 ELSE 0 END) as overdueFollowUps
        FROM leads WHERE isDeleted = 0
    """)
    suspend fun getLeadStats(weekStart: Long, todayStart: Long, todayEnd: Long): LeadStats
    
    @Query("""
        SELECT statusId, COUNT(*) as count
        FROM leads WHERE isDeleted = 0
        GROUP BY statusId
    """)
    suspend fun getLeadsByStatus(): List<StatusCount>
    
    @Query("""
        SELECT preferredCountries, COUNT(*) as count
        FROM leads WHERE isDeleted = 0 AND preferredCountries IS NOT NULL
        GROUP BY preferredCountries
        ORDER BY count DESC LIMIT 10
    """)
    suspend fun getTopCountries(): List<CountryCount>
    
    @Query("""
        SELECT source, COUNT(*) as count
        FROM leads WHERE isDeleted = 0
        GROUP BY source
        ORDER BY count DESC
    """)
    suspend fun getLeadsBySource(): List<SourceCount>
    
    @Query("""
        SELECT 
            COUNT(*) as totalCalls,
            SUM(duration) as totalDuration,
            AVG(duration) as avgDuration
        FROM call_logs
        WHERE timestamp BETWEEN :start AND :end
    """)
    suspend fun getCallStats(start: Long, end: Long): CallStats
}
```

### Day 3-4: Dashboard UI

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 10.5 | Create DashboardViewModel | Dev 2 | 3 |
| 10.6 | Create DashboardScreen | Dev 3 | 6 |
| 10.7 | Create StatCard component | Dev 3 | 2 |
| 10.8 | Create PipelineChart | Dev 3 | 4 |
| 10.9 | Create SourcesChart | Dev 3 | 3 |
| 10.10 | Create CountryDistributionChart | Dev 3 | 3 |

### Day 5: Reports

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 10.11 | Create ReportsScreen | Dev 3 | 4 |
| 10.12 | Implement date range picker | Dev 3 | 2 |
| 10.13 | Create export to CSV | Dev 2 | 3 |
| 10.14 | Create export to PDF | Dev 2 | 4 |

---

## Week 11: Push Notifications & Reminders

### Day 1-2: FCM Setup

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 11.1 | Create FCMService | Dev 1 | 4 |
| 11.2 | Handle different notification types | Dev 1 | 3 |
| 11.3 | Create NotificationManager | Dev 1 | 3 |
| 11.4 | Create notification channels | Dev 1 | 2 |
| 11.5 | Implement deep linking | Dev 2 | 3 |

#### Notification Types
```kotlin
enum class NotificationType {
    LEAD_ASSIGNED,       // New lead assigned to you
    FOLLOW_UP_REMINDER,  // Scheduled follow-up reminder
    NEW_INQUIRY,         // New inquiry received
    STATUS_UPDATE,       // Lead status changed
    APPLICATION_UPDATE,  // Application status changed
    TEAM_ANNOUNCEMENT,   // Team-wide announcement
    TASK_ASSIGNED,       // Task assigned to you
    DEADLINE_REMINDER    // Upcoming deadline
}
```

### Day 3-4: Scheduled Reminders

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 11.6 | Create FollowUpReminderWorker | Dev 2 | 4 |
| 11.7 | Create DeadlineReminderWorker | Dev 2 | 3 |
| 11.8 | Implement reminder scheduling | Dev 2 | 3 |
| 11.9 | Create ReminderSettingsScreen | Dev 3 | 3 |
| 11.10 | Add snooze functionality | Dev 2 | 2 |

### Day 5: Notification Actions

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 11.11 | Implement call action | Dev 1 | 2 |
| 11.12 | Implement snooze action | Dev 1 | 2 |
| 11.13 | Implement mark-done action | Dev 1 | 2 |
| 11.14 | Create notification history | Dev 3 | 3 |
| 11.15 | Test all notification flows | All | 4 |

---

## Week 12: Offline Support & Sync

### Day 1-2: Sync Manager

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 12.1 | Create SyncManager | Dev 1 | 4 |
| 12.2 | Create SyncWorker | Dev 1 | 3 |
| 12.3 | Implement periodic sync | Dev 1 | 2 |
| 12.4 | Create NetworkMonitor | Dev 2 | 2 |
| 12.5 | Handle sync queue | Dev 2 | 3 |

### Day 3-4: Conflict Resolution

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 12.6 | Create ConflictResolver | Dev 1 | 4 |
| 12.7 | Implement merge strategies | Dev 1 | 4 |
| 12.8 | Create ConflictResolutionUI | Dev 3 | 4 |
| 12.9 | Handle edge cases | Dev 2 | 3 |

#### Conflict Resolution Strategies
```kotlin
sealed class ConflictStrategy {
    object UseLocal : ConflictStrategy()
    object UseRemote : ConflictStrategy()
    object UseMostRecent : ConflictStrategy()
    data class Merge(val resolver: (local: Any, remote: Any) -> Any) : ConflictStrategy()
    object AskUser : ConflictStrategy()
}

class ConflictResolver {
    
    fun resolve(local: Lead, remote: Lead): Lead {
        return when {
            // Same version - no conflict
            local.updatedAt == remote.updatedAt -> local
            
            // Remote is newer by significant margin - use remote
            remote.updatedAt - local.updatedAt > 60_000 -> remote
            
            // Local is newer - use local
            local.updatedAt > remote.updatedAt -> local
            
            // Close timestamps - merge
            else -> merge(local, remote)
        }
    }
    
    private fun merge(local: Lead, remote: Lead): Lead {
        return local.copy(
            // Take non-null values, prefer most recent
            firstName = local.firstName,
            lastName = remote.lastName ?: local.lastName,
            notes = mergeNotes(local.notes, remote.notes),
            updatedAt = maxOf(local.updatedAt, remote.updatedAt)
        )
    }
}
```

### Day 5: Sync Status UI

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 12.10 | Create SyncStatusIndicator | Dev 3 | 2 |
| 12.11 | Create SyncProgressBar | Dev 3 | 2 |
| 12.12 | Create OfflineBanner | Dev 3 | 2 |
| 12.13 | Create SyncSettingsScreen | Dev 3 | 3 |
| 12.14 | Test offline scenarios | All | 4 |

---

# PHASE 4: POLISH & DEPLOYMENT
**Duration: Weeks 13-16**  
**Goal: Testing, security, optimization, and release**

---

## Week 13: Testing

### Day 1-2: Unit Tests

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 13.1 | Write repository unit tests | Dev 1 | 6 |
| 13.2 | Write use case unit tests | Dev 2 | 6 |
| 13.3 | Write ViewModel unit tests | Dev 2 | 6 |
| 13.4 | Write utility function tests | Dev 1 | 2 |

#### Test Coverage Targets
| Module | Target |
|--------|--------|
| Repositories | 80% |
| Use Cases | 90% |
| ViewModels | 70% |
| Utilities | 95% |

### Day 3-4: Integration Tests

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 13.5 | Write database integration tests | Dev 1 | 4 |
| 13.6 | Write API integration tests | Dev 1 | 4 |
| 13.7 | Write sync integration tests | Dev 2 | 4 |
| 13.8 | Write auth flow tests | Dev 2 | 3 |

### Day 5: UI Tests

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 13.9 | Write Compose UI tests | Dev 3 | 4 |
| 13.10 | Write navigation tests | Dev 3 | 3 |
| 13.11 | Write end-to-end tests | All | 4 |
| 13.12 | Setup test reporting | DevOps | 2 |

---

## Week 14: Security & Performance

### Day 1-2: Security Implementation

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 14.1 | Implement EncryptedSharedPrefs | Dev 1 | 2 |
| 14.2 | Add certificate pinning | Dev 1 | 2 |
| 14.3 | Implement root detection | Dev 1 | 2 |
| 14.4 | Add biometric authentication | Dev 2 | 4 |
| 14.5 | Audit for security issues | Lead | 4 |
| 14.6 | Implement session timeout | Dev 2 | 2 |

#### Security Checklist
- [ ] Token stored in EncryptedSharedPreferences
- [ ] Certificate pinning enabled
- [ ] No sensitive data in logs
- [ ] SQL injection prevention
- [ ] Input validation on all forms
- [ ] Biometric/PIN lock option
- [ ] Session expiry handling
- [ ] Root/jailbreak detection

### Day 3-4: Performance Optimization

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 14.7 | Profile app with Android Profiler | Dev 1 | 3 |
| 14.8 | Optimize database queries | Dev 1 | 4 |
| 14.9 | Implement pagination | Dev 2 | 3 |
| 14.10 | Optimize image loading | Dev 3 | 2 |
| 14.11 | Reduce app startup time | Dev 1 | 3 |
| 14.12 | Implement lazy loading | Dev 3 | 2 |

### Day 5: Memory & Battery Optimization

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 14.13 | Fix memory leaks | Dev 1 | 4 |
| 14.14 | Optimize background services | Dev 2 | 3 |
| 14.15 | Implement Doze mode handling | Dev 2 | 2 |
| 14.16 | Test battery consumption | All | 2 |

---

## Week 15: UI Polish & Accessibility

### Day 1-2: UI Polish

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 15.1 | Implement loading states | Dev 3 | 3 |
| 15.2 | Add error state screens | Dev 3 | 3 |
| 15.3 | Add empty state screens | Dev 3 | 2 |
| 15.4 | Implement animations | Dev 3 | 4 |
| 15.5 | Polish transitions | Dev 3 | 2 |
| 15.6 | Review all screens | All | 2 |

### Day 3-4: Accessibility

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 15.7 | Add content descriptions | Dev 3 | 3 |
| 15.8 | Test with TalkBack | Dev 3 | 2 |
| 15.9 | Ensure touch target sizes | Dev 3 | 2 |
| 15.10 | Test color contrast | Dev 3 | 1 |
| 15.11 | Add dark theme support | Dev 3 | 4 |
| 15.12 | Test font scaling | Dev 3 | 1 |

### Day 5: Localization

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 15.13 | Extract all strings | Dev 3 | 2 |
| 15.14 | Setup string resources | Dev 3 | 2 |
| 15.15 | Add Hindi translations | Translator | 4 |
| 15.16 | Test RTL if needed | Dev 3 | 2 |

---

## Week 16: Deployment

### Day 1-2: Release Preparation

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 16.1 | Final QA testing | QA | 8 |
| 16.2 | Fix critical bugs | All | 8 |
| 16.3 | Update app version | Lead | 1 |
| 16.4 | Generate release APK/AAB | DevOps | 2 |
| 16.5 | Create release notes | Lead | 2 |

### Day 3-4: Store Submission

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 16.6 | Prepare store listing | Marketing | 4 |
| 16.7 | Create screenshots | Designer | 4 |
| 16.8 | Write app description | Marketing | 2 |
| 16.9 | Submit to Play Store | DevOps | 2 |
| 16.10 | Setup crash reporting | DevOps | 2 |
| 16.11 | Setup analytics | DevOps | 2 |

### Day 5: Post-Launch

#### Tasks
| # | Task | Owner | Hours |
|---|------|-------|-------|
| 16.12 | Monitor crash reports | All | Ongoing |
| 16.13 | Monitor user feedback | All | Ongoing |
| 16.14 | Create hotfix plan | Lead | 2 |
| 16.15 | Document known issues | Lead | 2 |
| 16.16 | Plan version 1.1 | Lead | 4 |

---

# RESOURCE ALLOCATION

## Team Structure

| Role | Count | Responsibilities |
|------|-------|------------------|
| Tech Lead | 1 | Architecture, code review, planning |
| Android Dev (Senior) | 1 | Core features, services, complex logic |
| Android Dev (Mid) | 1 | Use cases, ViewModels, testing |
| Android Dev (UI) | 1 | Compose UI, animations, UX |
| QA Engineer | 1 | Testing, bug tracking |
| DevOps | 0.5 | CI/CD, deployment (part-time) |

## Timeline Summary

```
Phase 1: Foundation          Weeks 1-4    (1 month)
Phase 2: Core Features       Weeks 5-8    (1 month)
Phase 3: Advanced Features   Weeks 9-12   (1 month)
Phase 4: Polish & Deploy     Weeks 13-16  (1 month)
                             ─────────────────────
                             Total: 16 weeks (4 months)
```

## Risk Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| API changes | Medium | High | Version API, graceful degradation |
| Scope creep | High | Medium | Strict change control, MVP focus |
| Performance issues | Medium | High | Early profiling, load testing |
| Security vulnerabilities | Low | Critical | Security audit, penetration testing |
| Integration issues | Medium | Medium | Early integration, mock APIs |
| Resource unavailability | Medium | High | Cross-training, documentation |

---

# APPENDIX

## A. Development Environment Setup

```bash
# Required Tools
- Android Studio Hedgehog or newer
- JDK 17
- Git
- Node.js (for Firebase CLI)

# Project Setup
git clone <repository-url>
cd educonsult-crm
./gradlew build

# Run Tests
./gradlew test
./gradlew connectedAndroidTest

# Build Release
./gradlew assembleRelease
./gradlew bundleRelease
```

## B. Code Review Checklist

- [ ] Follows architecture patterns (MVVM, Clean Architecture)
- [ ] No hardcoded strings (use resources)
- [ ] Proper error handling
- [ ] Unit tests for business logic
- [ ] No memory leaks
- [ ] Proper null safety
- [ ] Documentation for complex logic
- [ ] No sensitive data logged
- [ ] Follows Kotlin coding conventions

## C. Definition of Done

A task is considered "Done" when:
1. Code is written and compiles without errors
2. Unit tests are written and passing
3. Code review is completed
4. Documentation is updated
5. Feature works on debug and release builds
6. No critical bugs or regressions
7. Merged to develop branch

---

*Document Version: 1.0*  
*Last Updated: January 2026*  
*Project: Educational Consultancy CRM*
