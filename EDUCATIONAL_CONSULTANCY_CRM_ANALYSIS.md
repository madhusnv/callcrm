# Educational Consultancy CRM - Analysis & Implementation Plan

## Executive Summary

This document provides a comprehensive analysis of the Callyzer Biz application (v2.9.8) and outlines a detailed implementation plan for building an Educational Consultancy CRM system based on its architecture and features.

---

# PART 1: CALLYZER BIZ APP ANALYSIS

## 1. Application Overview

| Attribute | Value |
|-----------|-------|
| Package Name | `com.websoptimization.callyzerbiz` |
| Version | 2.9.8 (Build 188) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 35 (Android 15) |
| Architecture | MVVM with Clean Architecture |
| Language | Kotlin |
| Backend | REST API with OAuth 2.0 |

## 2. Technology Stack

### 2.1 Frontend (Android)
- **UI Framework**: Jetpack Compose (likely) + XML Views
- **Navigation**: Jetpack Navigation Component
- **Dependency Injection**: Hilt/Dagger (inferred from structure)
- **Async Operations**: Kotlin Coroutines + Flow
- **Image Loading**: Coil
- **Local Database**: Room (SQLite)

### 2.2 Backend Integration
- **Network Layer**: Retrofit2 + OkHttp3
- **Serialization**: Gson/Moshi (JSON)
- **Authentication**: OAuth 2.0 with Bearer tokens
- **Cloud Services**: 
  - AWS Amplify (Cognito for Auth)
  - Firebase (Analytics, Crashlytics, Cloud Messaging, Remote Config)

### 2.3 Third-Party Integrations
- WhatsApp integration
- Truecaller integration
- Barcode/QR scanning (ML Kit)
- Speech recognition
- Camera (CameraX)

## 3. App Permissions & Features

### 3.1 Core Permissions
```
READ_CONTACTS          - Access contact list
READ_CALL_LOG          - Track call history
READ_PHONE_STATE       - Monitor call states
CALL_PHONE             - Initiate calls
RECORD_AUDIO           - Call recording
INTERNET               - API communication
POST_NOTIFICATIONS     - Push notifications
CAMERA                 - Document scanning
SYSTEM_ALERT_WINDOW    - Floating note popup
FOREGROUND_SERVICE     - Background call monitoring
```

### 3.2 Key Features Identified
1. **Call Tracking & Monitoring**
2. **Lead Management (CRM)**
3. **Call Recording & Cloud Sync**
4. **Note-taking during/after calls**
5. **Employee Management**
6. **Message Templates**
7. **Quick Call Extensions**
8. **Push Notifications (FCM)**
9. **Subscription Management**

## 4. Database Schema (Room)

### 4.1 Core Tables
| Table Name | Purpose |
|------------|---------|
| `UserSimDetails` | SIM card and device info |
| `CallLog` | Call history records |
| `CorruptCallLog` | Failed/corrupted call logs |
| `FakeCallLog` | Test/demo call logs |
| `LeadStatus` | Lead pipeline stages |
| `Contact` | Contact information |
| `CallLogVerification` | Call verification status |
| `CallNoteTemplate` | Predefined note templates |
| `message_template` | Message templates |
| `message_sub_template` | Sub-templates |
| `Message_template_documents` | Template attachments |
| `Message_template_dynamic_fields` | Dynamic field definitions |
| `message_template_tag` | Template categorization |
| `Message_templateId_tag_Id` | Many-to-many mapping |
| `quick_call_extensions` | Quick dial extensions |

### 4.2 Database Views
- `viewcalllogcontactusersim` - Joins CallLog + Contact + UserSim
- `viewcontactcallhistusersim` - Contact call history view
- `viewcalllogusersim` - CallLog + UserSim view

## 5. API Endpoints Analysis

### 5.1 Authentication
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `oauth/token` | POST | Get access token (Basic Auth header) |

### 5.2 Lead Management
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `lead/getData` | POST | Fetch leads with filters |
| `lead/save` | POST | Create/update lead |
| `lead/saveNote` | POST | Save note for lead |
| `lead/status` | POST | Get all lead statuses |
| `lead/notContacted` | POST | Get uncontacted leads |
| `lead/getByNumber` | POST | Find lead by phone |
| `lead/isDeleted` | POST | Check soft delete status |
| `lead/restore` | POST | Restore deleted lead |
| `lead/allTags` | POST | Get all lead tags |
| `lead/note` | POST | Get lead notes |
| `lead/recent/notes` | POST | Recent notes |
| `lead/callLogs/getActive` | POST | Lead call history |
| `lead/callLogs/getSummary` | POST | Call summary stats |
| `lead/callBack/totalDue` | POST | Pending callbacks |
| `lead/form/getDynamicComponentDetails` | POST | Dynamic form fields |
| `lead/isAssignedToMe` | POST | Assignment check |

### 5.3 Call Management
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `callLog/sync` | POST | Sync call logs to server |
| `callLog/getDetails` | POST | Get call details |
| `callLog/sync/note` | POST | Sync call notes |
| `callLog/getByNotesUpdatedAtWeb` | POST | Fetch web-updated notes |
| `callRecording/sync` | POST | Upload recordings |
| `callRecording/suggestPaths` | POST | Storage path suggestions |
| `callRecording/availableSpace` | POST | Check cloud storage |

### 5.4 Employee Management
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `employee/register` | POST | Register new employee |
| `employee/register/sendOTP` | POST | Send OTP |
| `employee/register/verifyOTP` | POST | Verify OTP |
| `employee/settings/save` | POST | Save settings |
| `employee/updateFCM` | POST | Update FCM token |
| `employee/get/customer/employees` | POST | List all employees |
| `employee/get/callLogs` | POST | Employee call logs |
| `employee/validateNumber` | POST | Validate phone |
| `employee/subscriptionDetail` | POST | Subscription info |
| `employee/customer/excludeList` | POST | Excluded numbers |

### 5.5 Templates
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `callnote/template/save` | POST | Save note templates |
| `callnote/template/fetchAll` | POST | Get all templates |
| `callnote/template/delete` | POST | Delete templates |
| `messagetemplate/fetchAll` | POST | Get message templates |
| `messagetemplate/fetch` | GET | Get single template |
| `messagetemplate/tag/fetchAll` | GET | Get template tags |

### 5.6 Quick Call
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `quickcall/connect` | POST | Connect extension |
| `quickcall/disconnect` | POST | Disconnect |
| `quickcall/fetch/active` | POST | Active connections |

### 5.7 System
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `app/getSettings` | GET | App settings |
| `feature/getStatus` | GET | Feature flags |
| `feature/subscription` | POST | Check subscription |

## 6. Data Models

### 6.1 Lead Model
```kotlin
Lead {
    id: String
    firstName: String
    lastName: String?
    number: String (primary phone)
    secondaryNumber: String?
    countryCode: Int
    secondaryCountryCode: Int?
    status: String (pipeline stage)
    assignToAll: Boolean
    leadTags: List<String>
    serialNumber: String
    simNumbers: List<String>
    assignedTo: List<Employee>
    properties: Map<String, DynamicField> // Custom fields
    noOfAttempts: Int
    createdDate: LocalDateTime
    modifiedDate: LocalDateTime?
    reminderDate: LocalDateTime? (follow-up)
    lastCallDetails: CallDetails?
}
```

### 6.2 Dynamic Form Components
```kotlin
DynamicComponents {
    inputBox: List<InputBox>      // Text fields
    checkBox: List<CheckBox>      // Multi-select
    radioBox: List<RadioBox>      // Single-select
    dropDownBox: List<DropDownBox> // Dropdown
}
```

### 6.3 Call Log Sync Model
```kotlin
SyncCallLog {
    callId: String
    number: String
    duration: Int
    callType: Int (incoming/outgoing/missed)
    timestamp: Long
    simSlot: Int
}

SyncCallLogNote {
    callLogId: String
    note: String
    createdAt: LocalDateTime
}

SyncCallLogCallRecording {
    callLogId: String
    recordingPath: String
    uploadStatus: Int
}
```

## 7. Services & Background Processing

### 7.1 Foreground Services
| Service | Purpose |
|---------|---------|
| `CallMonitorService` | Real-time call state monitoring |
| `NotePopupService` | Floating note popup during calls |

### 7.2 Background Services
| Service | Purpose |
|---------|---------|
| `FCMMessageService` | Push notification handling |
| `CallRecordingNotificationActionService` | Recording notifications |
| `ReSyncNotificationActionService` | Re-sync triggers |

### 7.3 Broadcast Receivers
| Receiver | Purpose |
|----------|---------|
| `PhoneStateReceiver` | Call state changes |
| `SimStatesChangedReceiver` | SIM swap detection |
| `RebootReceiver` | Auto-start after reboot |

### 7.4 WorkManager Tasks
- Call log sync
- Recording upload
- Note synchronization
- Data backup

---

# PART 2: EDUCATIONAL CONSULTANCY CRM IMPLEMENTATION PLAN

## Target Domain: Educational Consultancy

### Core Business Processes
1. **Student/Parent Inquiries** → Leads
2. **Course/Program Counseling** → Opportunities
3. **Application Processing** → Pipeline
4. **Enrollment & Payment** → Conversion
5. **Ongoing Support** → Retention

---

## PHASE 1: FOUNDATION (Weeks 1-4)

### 1.1 Project Setup (Week 1)

#### 1.1.1 Android Project Structure
```
app/
├── src/main/
│   ├── java/com/educonsult/crm/
│   │   ├── CallyzerBizApp.kt           # Application class
│   │   ├── MainActivity.kt
│   │   ├── di/                         # Dependency Injection
│   │   │   ├── AppModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   └── RepositoryModule.kt
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── db/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   └── entity/
│   │   │   │   └── preferences/
│   │   │   ├── remote/
│   │   │   │   ├── api/
│   │   │   │   │   ├── AuthApi.kt
│   │   │   │   │   ├── LeadApi.kt
│   │   │   │   │   ├── StudentApi.kt
│   │   │   │   │   └── CourseApi.kt
│   │   │   │   ├── dto/
│   │   │   │   └── interceptor/
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── usecase/
│   │   ├── ui/
│   │   │   ├── auth/
│   │   │   ├── dashboard/
│   │   │   ├── leads/
│   │   │   ├── students/
│   │   │   ├── courses/
│   │   │   ├── calls/
│   │   │   └── common/
│   │   ├── services/
│   │   ├── receivers/
│   │   └── workers/
│   └── res/
└── build.gradle.kts
```

#### 1.1.2 Dependencies (build.gradle.kts)
```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

#### 1.1.3 Permissions (AndroidManifest.xml)
```xml
<uses-permission android:name="android.permission.READ_CONTACTS"/>
<uses-permission android:name="android.permission.READ_CALL_LOG"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
```

### 1.2 Authentication Module (Week 1-2)

#### 1.2.1 Database Entities
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val role: String, // ADMIN, COUNSELOR, AGENT
    val branchId: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val tokenExpiry: Long?,
    val createdAt: Long,
    val isActive: Boolean
)

@Entity(tableName = "user_sim_details")
data class UserSimDetailsEntity(
    @PrimaryKey val simId: String,
    val userId: String,
    val simNumber: String,
    val carrier: String,
    val slotIndex: Int,
    val isDefault: Boolean
)
```

#### 1.2.2 API Interfaces
```kotlin
interface AuthApi {
    @FormUrlEncoded
    @POST("oauth/token")
    @Headers("Authorization: Basic <base64_encoded_credentials>")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<AuthTokenResponse>
    
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest
    ): Response<AuthTokenResponse>
    
    @POST("auth/register/sendOTP")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<BaseResponse<String>>
    
    @POST("auth/register/verifyOTP")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<BaseResponse<Boolean>>
    
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<BaseResponse<UserResponse>>
    
    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>
}
```

#### 1.2.3 Auth Repository
```kotlin
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun sendOtp(phone: String): Result<String>
    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean>
    suspend fun register(request: RegisterRequest): Result<User>
    suspend fun refreshToken(): Result<String>
    suspend fun logout(): Result<Unit>
    fun getCurrentUser(): Flow<User?>
    fun isLoggedIn(): Flow<Boolean>
}

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val tokenManager: TokenManager
) : AuthRepository {
    // Implementation
}
```

### 1.3 Database Setup (Week 2)

#### 1.3.1 Room Database
```kotlin
@Database(
    entities = [
        UserEntity::class,
        UserSimDetailsEntity::class,
        LeadEntity::class,
        LeadStatusEntity::class,
        LeadTagEntity::class,
        CallLogEntity::class,
        CallNoteEntity::class,
        ContactEntity::class,
        CourseEntity::class,
        InstitutionEntity::class,
        CountryEntity::class,
        NoteTemplateEntity::class,
        MessageTemplateEntity::class,
        ReminderEntity::class,
        ActivityLogEntity::class
    ],
    views = [
        LeadWithCallsView::class,
        LeadDashboardView::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun leadDao(): LeadDao
    abstract fun callLogDao(): CallLogDao
    abstract fun courseDao(): CourseDao
    abstract fun reminderDao(): ReminderDao
    abstract fun templateDao(): TemplateDao
}
```

#### 1.3.2 Lead Entity (Educational CRM Specific)
```kotlin
@Entity(
    tableName = "leads",
    indices = [
        Index("phone"),
        Index("email"),
        Index("statusId"),
        Index("assignedTo"),
        Index("createdAt")
    ]
)
data class LeadEntity(
    @PrimaryKey val id: String,
    
    // Basic Info
    val firstName: String,
    val lastName: String?,
    val phone: String,
    val secondaryPhone: String?,
    val countryCode: Int,
    val email: String?,
    
    // Student/Parent Info
    val studentName: String?,
    val parentName: String?,
    val relationship: String?, // SELF, PARENT, GUARDIAN
    val dateOfBirth: Long?,
    
    // Educational Background
    val currentEducation: String?, // 10th, 12th, Graduate, etc.
    val currentInstitution: String?,
    val percentage: Float?,
    val stream: String?, // Science, Commerce, Arts
    val graduationYear: Int?,
    
    // Inquiry Details
    val interestedCourses: String?, // JSON array
    val preferredCountries: String?, // JSON array
    val preferredInstitutions: String?, // JSON array
    val budgetMin: Long?,
    val budgetMax: Long?,
    val intakePreference: String?, // Fall 2025, Spring 2026
    
    // Lead Status
    val statusId: String,
    val pipelineStage: String, // INQUIRY, COUNSELING, APPLICATION, VISA, ENROLLED
    val priority: String, // HOT, WARM, COLD
    val source: String, // WALK_IN, CALL, WEBSITE, REFERRAL, SOCIAL_MEDIA
    
    // Assignment
    val assignedTo: String?, // Counselor ID
    val branchId: String?,
    
    // Follow-up
    val lastContactDate: Long?,
    val nextFollowUpDate: Long?,
    val reminderNote: String?,
    
    // Tracking
    val totalCalls: Int = 0,
    val totalAttempts: Int = 0,
    val lastCallDuration: Int = 0,
    
    // Custom Fields (JSON)
    val customFields: String?,
    
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val createdBy: String,
    val isDeleted: Boolean = false,
    val syncStatus: Int = 0 // 0=pending, 1=synced, 2=conflict
)
```

### 1.4 Network Layer (Week 2-3)

#### 1.4.1 API Service
```kotlin
interface LeadApi {
    @POST("lead/getData")
    suspend fun getLeads(
        @HeaderMap headers: Map<String, String>,
        @Body request: LeadListRequest
    ): Response<BaseResponse<LeadListResponse>>
    
    @POST("lead/save")
    suspend fun saveLead(
        @HeaderMap headers: Map<String, String>,
        @Body request: SaveLeadRequest
    ): Response<BaseResponse<String>>
    
    @POST("lead/saveNote")
    suspend fun saveLeadNote(
        @HeaderMap headers: Map<String, String>,
        @Body request: LeadNoteSaveRequest
    ): Response<BaseResponse<LeadNoteSaveResponse>>
    
    @POST("lead/status")
    suspend fun getLeadStatuses(
        @HeaderMap headers: Map<String, String>
    ): Response<BaseResponse<List<LeadStatusResponse>>>
    
    @POST("lead/getByNumber")
    suspend fun getLeadByNumber(
        @HeaderMap headers: Map<String, String>,
        @FieldMap params: Map<String, String>
    ): Response<BaseResponse<LeadByNumberResponse>>
    
    @POST("lead/callLogs/getActive")
    suspend fun getLeadCallHistory(
        @HeaderMap headers: Map<String, String>,
        @Query("leadId") leadId: String,
        @Query("pageNo") page: Int,
        @Query("pageSize") size: Int
    ): Response<BaseResponse<List<LeadCallHistory>>>
    
    @POST("lead/form/getDynamicComponentDetails")
    suspend fun getDynamicFormFields(
        @HeaderMap headers: Map<String, String>
    ): Response<BaseResponse<LeadFormResponse>>
}
```

#### 1.4.2 Network Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = 
        retrofit.create(AuthApi::class.java)
    
    @Provides
    @Singleton
    fun provideLeadApi(retrofit: Retrofit): LeadApi = 
        retrofit.create(LeadApi::class.java)
}
```

#### 1.4.3 Auth Interceptor
```kotlin
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login/register endpoints
        if (originalRequest.url.encodedPath.contains("oauth/token") ||
            originalRequest.url.encodedPath.contains("auth/register")) {
            return chain.proceed(originalRequest)
        }
        
        val token = runBlocking { tokenManager.getAccessToken() }
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json")
            .build()
        
        val response = chain.proceed(authenticatedRequest)
        
        // Handle 401 - Token refresh
        if (response.code == 401) {
            response.close()
            val newToken = runBlocking { tokenManager.refreshToken() }
            
            if (newToken != null) {
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                return chain.proceed(retryRequest)
            }
        }
        
        return response
    }
}
```

---

## PHASE 2: CORE CRM FEATURES (Weeks 5-8)

### 2.1 Lead Management Module (Week 5-6)

#### 2.1.1 Lead Statuses for Educational Consultancy
```kotlin
enum class LeadStatus(val displayName: String, val color: Long) {
    NEW("New Inquiry", 0xFF2196F3),
    CONTACTED("Contacted", 0xFF9C27B0),
    COUNSELING_SCHEDULED("Counseling Scheduled", 0xFFFF9800),
    COUNSELING_DONE("Counseling Done", 0xFF4CAF50),
    DOCUMENTS_PENDING("Documents Pending", 0xFFE91E63),
    APPLICATION_IN_PROGRESS("Application In Progress", 0xFF00BCD4),
    APPLICATION_SUBMITTED("Application Submitted", 0xFF3F51B5),
    OFFER_RECEIVED("Offer Received", 0xFF8BC34A),
    VISA_PROCESSING("Visa Processing", 0xFFFF5722),
    VISA_APPROVED("Visa Approved", 0xFF4CAF50),
    ENROLLED("Enrolled", 0xFF009688),
    DROPPED("Dropped", 0xFF757575),
    NOT_INTERESTED("Not Interested", 0xFF9E9E9E)
}
```

#### 2.1.2 Lead List Screen (Compose)
```kotlin
@Composable
fun LeadListScreen(
    viewModel: LeadListViewModel = hiltViewModel(),
    onLeadClick: (String) -> Unit,
    onAddLead: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            LeadListTopBar(
                onFilterClick = { viewModel.toggleFilters() },
                onSearchClick = { viewModel.toggleSearch() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddLead) {
                Icon(Icons.Default.Add, contentDescription = "Add Lead")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Status Filter Chips
            LeadStatusChips(
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = viewModel::filterByStatus
            )
            
            // Lead List
            when {
                uiState.isLoading -> LoadingIndicator()
                uiState.error != null -> ErrorMessage(uiState.error)
                else -> {
                    LazyColumn {
                        items(uiState.leads) { lead ->
                            LeadCard(
                                lead = lead,
                                onClick = { onLeadClick(lead.id) },
                                onCall = { viewModel.initiateCall(lead) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeadCard(
    lead: Lead,
    onClick: () -> Unit,
    onCall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lead Avatar
            LeadAvatar(lead.firstName, lead.priority)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Lead Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${lead.firstName} ${lead.lastName ?: ""}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = lead.phone,
                    style = MaterialTheme.typography.bodySmall
                )
                Row {
                    StatusChip(status = lead.status)
                    Spacer(modifier = Modifier.width(4.dp))
                    PriorityChip(priority = lead.priority)
                }
                lead.interestedCourses?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            // Action Buttons
            Column {
                IconButton(onClick = onCall) {
                    Icon(Icons.Default.Call, contentDescription = "Call")
                }
                lead.nextFollowUpDate?.let { date ->
                    Text(
                        text = formatDate(date),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
```

#### 2.1.3 Lead Detail/Edit Screen
```kotlin
@Composable
fun LeadDetailScreen(
    leadId: String,
    viewModel: LeadDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(leadId) {
        viewModel.loadLead(leadId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lead Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleEdit() }) {
                        Icon(Icons.Default.Edit, "Edit")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Basic Info Section
            item { BasicInfoSection(uiState.lead, uiState.isEditing) }
            
            // Student Details Section
            item { StudentDetailsSection(uiState.lead, uiState.isEditing) }
            
            // Inquiry Details Section
            item { InquiryDetailsSection(uiState.lead, uiState.isEditing) }
            
            // Call History Section
            item { 
                CallHistorySection(
                    calls = uiState.callHistory,
                    onViewAll = { /* Navigate to full history */ }
                )
            }
            
            // Notes Section
            item {
                NotesSection(
                    notes = uiState.notes,
                    onAddNote = { viewModel.addNote(it) }
                )
            }
            
            // Activity Timeline
            item { ActivityTimeline(uiState.activities) }
        }
    }
}
```

#### 2.1.4 Dynamic Form Fields
```kotlin
@Composable
fun DynamicFormField(
    field: DynamicField,
    value: String,
    onValueChange: (String) -> Unit
) {
    when (field.type) {
        FieldType.INPUT_BOX -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label) },
                modifier = Modifier.fillMaxWidth(),
                isError = field.isRequired && value.isEmpty()
            )
        }
        FieldType.DROPDOWN -> {
            ExposedDropdownMenuBox(
                // Dropdown implementation
            )
        }
        FieldType.CHECKBOX -> {
            field.options.forEach { option ->
                Row {
                    Checkbox(
                        checked = value.contains(option),
                        onCheckedChange = { /* Toggle selection */ }
                    )
                    Text(option)
                }
            }
        }
        FieldType.RADIO -> {
            field.options.forEach { option ->
                Row {
                    RadioButton(
                        selected = value == option,
                        onClick = { onValueChange(option) }
                    )
                    Text(option)
                }
            }
        }
        FieldType.DATE -> {
            DatePickerField(
                value = value,
                onValueChange = onValueChange,
                label = field.label
            )
        }
    }
}
```

### 2.2 Call Tracking Module (Week 6-7)

#### 2.2.1 Call Monitor Service
```kotlin
class CallMonitorService : Service() {
    
    @Inject lateinit var callLogRepository: CallLogRepository
    @Inject lateinit var leadRepository: LeadRepository
    
    private val phoneStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    val incomingNumber = intent.getStringExtra(
                        TelephonyManager.EXTRA_INCOMING_NUMBER
                    )
                    handleIncomingCall(incomingNumber)
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    handleCallConnected()
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    handleCallEnded()
                }
            }
        }
    }
    
    private fun handleIncomingCall(number: String?) {
        number?.let {
            // Check if number belongs to a lead
            CoroutineScope(Dispatchers.IO).launch {
                val lead = leadRepository.getLeadByNumber(it)
                if (lead != null) {
                    showLeadPopup(lead)
                }
            }
        }
    }
    
    private fun showLeadPopup(lead: Lead) {
        // Show floating window with lead info
        val intent = Intent(this, NotePopUpActivity::class.java).apply {
            putExtra("lead_id", lead.id)
            putExtra("lead_name", "${lead.firstName} ${lead.lastName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }
    
    private fun handleCallEnded() {
        // Log call and prompt for notes
        CoroutineScope(Dispatchers.IO).launch {
            syncCallLogs()
        }
    }
    
    private suspend fun syncCallLogs() {
        val cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DURATION,
                CallLog.Calls.DATE
            ),
            "${CallLog.Calls.DATE} > ?",
            arrayOf(getLastSyncTimestamp().toString()),
            "${CallLog.Calls.DATE} DESC"
        )
        
        cursor?.use {
            while (it.moveToNext()) {
                val callLog = CallLogEntity(
                    id = UUID.randomUUID().toString(),
                    phoneNumber = it.getString(1),
                    callType = it.getInt(2),
                    duration = it.getLong(3).toInt(),
                    timestamp = it.getLong(4),
                    syncStatus = 0
                )
                callLogRepository.insert(callLog)
            }
        }
        
        // Sync to server
        callLogRepository.syncPendingCallLogs()
    }
}
```

#### 2.2.2 Note Popup Activity
```kotlin
class NotePopUpActivity : ComponentActivity() {
    
    private val viewModel: NotePopupViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val leadId = intent.getStringExtra("lead_id")
        val leadName = intent.getStringExtra("lead_name")
        
        setContent {
            NotePopupScreen(
                leadId = leadId,
                leadName = leadName,
                viewModel = viewModel,
                onDismiss = { finish() }
            )
        }
    }
}

@Composable
fun NotePopupScreen(
    leadId: String?,
    leadName: String?,
    viewModel: NotePopupViewModel,
    onDismiss: () -> Unit
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var noteText by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var followUpDate by remember { mutableStateOf<Long?>(null) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = leadName ?: "Unknown Caller",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            // Quick Templates
            Text("Quick Notes", style = MaterialTheme.typography.labelMedium)
            FlowRow {
                templates.forEach { template ->
                    FilterChip(
                        selected = noteText == template.text,
                        onClick = { noteText = template.text },
                        label = { Text(template.name) }
                    )
                }
            }
            
            // Note Input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Add Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // Status Update
            Text("Update Status", style = MaterialTheme.typography.labelMedium)
            StatusDropdown(
                selected = selectedStatus,
                onSelect = { selectedStatus = it }
            )
            
            // Follow-up Date
            DatePickerButton(
                label = "Set Follow-up",
                selectedDate = followUpDate,
                onDateSelected = { followUpDate = it }
            )
            
            // Save Button
            Button(
                onClick = {
                    viewModel.saveNote(
                        leadId = leadId,
                        note = noteText,
                        newStatus = selectedStatus,
                        followUpDate = followUpDate
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
```

### 2.3 Course & Institution Module (Week 7-8)

#### 2.3.1 Database Entities
```kotlin
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val institutionId: String,
    val level: String, // BACHELORS, MASTERS, PHD, DIPLOMA
    val duration: String, // "2 Years", "4 Years"
    val tuitionFee: Long,
    val currency: String,
    val intakes: String, // JSON array: ["Fall", "Spring"]
    val requirements: String?, // JSON: eligibility criteria
    val description: String?,
    val isActive: Boolean = true
)

@Entity(tableName = "institutions")
data class InstitutionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val country: String,
    val city: String,
    val ranking: Int?,
    val type: String, // UNIVERSITY, COLLEGE, SCHOOL
    val website: String?,
    val logoUrl: String?,
    val description: String?,
    val isPartner: Boolean = false
)

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val code: String,
    val name: String,
    val flagEmoji: String,
    val currency: String,
    val isPopular: Boolean = false
)
```

#### 2.3.2 Course Recommendation
```kotlin
class CourseRecommendationUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val leadRepository: LeadRepository
) {
    suspend fun getRecommendations(leadId: String): List<CourseRecommendation> {
        val lead = leadRepository.getLeadById(leadId)
        
        return courseRepository.searchCourses(
            countries = lead.preferredCountries,
            level = mapEducationToLevel(lead.currentEducation),
            stream = lead.stream,
            budgetMax = lead.budgetMax,
            intake = lead.intakePreference
        ).map { course ->
            CourseRecommendation(
                course = course,
                matchScore = calculateMatchScore(lead, course),
                highlights = getMatchHighlights(lead, course)
            )
        }.sortedByDescending { it.matchScore }
    }
    
    private fun calculateMatchScore(lead: Lead, course: Course): Float {
        var score = 0f
        
        // Budget match
        if (course.tuitionFee <= (lead.budgetMax ?: Long.MAX_VALUE)) {
            score += 30f
        }
        
        // Country preference
        if (lead.preferredCountries?.contains(course.country) == true) {
            score += 25f
        }
        
        // Intake match
        if (course.intakes.contains(lead.intakePreference)) {
            score += 20f
        }
        
        // Stream relevance
        if (isStreamRelevant(lead.stream, course)) {
            score += 25f
        }
        
        return score
    }
}
```

---

## PHASE 3: ADVANCED FEATURES (Weeks 9-12)

### 3.1 Call Recording & Cloud Sync (Week 9)

#### 3.1.1 Recording Service
```kotlin
class CallRecordingService : Service() {
    
    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingPath: String? = null
    
    fun startRecording(callId: String) {
        val fileName = "call_${callId}_${System.currentTimeMillis()}.m4a"
        currentRecordingPath = "${getExternalFilesDir(null)}/recordings/$fileName"
        
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(currentRecordingPath)
            prepare()
            start()
        }
    }
    
    fun stopRecording(): String? {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        return currentRecordingPath
    }
}
```

#### 3.1.2 Recording Upload Worker
```kotlin
@HiltWorker
class RecordingUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val recordingRepository: RecordingRepository,
    private val uploadService: UploadService
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val recordingId = inputData.getString("recording_id") ?: return Result.failure()
        
        return try {
            val recording = recordingRepository.getById(recordingId)
            val file = File(recording.localPath)
            
            if (!file.exists()) {
                recordingRepository.updateStatus(recordingId, UploadStatus.FAILED)
                return Result.failure()
            }
            
            setForeground(createForegroundInfo())
            
            val uploadUrl = uploadService.uploadRecording(
                file = file,
                callLogId = recording.callLogId,
                onProgress = { progress ->
                    setProgress(workDataOf("progress" to progress))
                }
            )
            
            recordingRepository.updateStatus(
                id = recordingId,
                status = UploadStatus.COMPLETED,
                remoteUrl = uploadUrl
            )
            
            // Optionally delete local file after upload
            if (recording.deleteAfterUpload) {
                file.delete()
            }
            
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                recordingRepository.updateStatus(recordingId, UploadStatus.FAILED)
                Result.failure()
            }
        }
    }
}
```

### 3.2 Message Templates (Week 9-10)

#### 3.2.1 Template System
```kotlin
@Entity(tableName = "message_templates")
data class MessageTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // GREETING, FOLLOW_UP, OFFER, VISA_INFO
    val content: String,
    val dynamicFields: String?, // JSON: ["{{student_name}}", "{{course_name}}"]
    val attachments: String?, // JSON array of attachment paths
    val whatsappEnabled: Boolean = true,
    val smsEnabled: Boolean = true,
    val emailEnabled: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

class MessageTemplateProcessor @Inject constructor() {
    
    fun processTemplate(
        template: MessageTemplate,
        lead: Lead,
        course: Course? = null,
        counselor: User? = null
    ): String {
        var content = template.content
        
        val replacements = mapOf(
            "{{student_name}}" to (lead.studentName ?: lead.firstName),
            "{{parent_name}}" to (lead.parentName ?: lead.firstName),
            "{{first_name}}" to lead.firstName,
            "{{course_name}}" to (course?.name ?: ""),
            "{{institution_name}}" to (course?.institutionName ?: ""),
            "{{country}}" to (course?.country ?: ""),
            "{{counselor_name}}" to (counselor?.name ?: ""),
            "{{counselor_phone}}" to (counselor?.phone ?: ""),
            "{{follow_up_date}}" to formatDate(lead.nextFollowUpDate),
            "{{intake}}" to (lead.intakePreference ?: ""),
            "{{fee}}" to formatCurrency(course?.tuitionFee, course?.currency)
        )
        
        replacements.forEach { (key, value) ->
            content = content.replace(key, value)
        }
        
        return content
    }
}
```

#### 3.2.2 WhatsApp Integration
```kotlin
class WhatsAppService @Inject constructor(
    private val context: Context,
    private val templateProcessor: MessageTemplateProcessor
) {
    
    fun sendMessage(
        phoneNumber: String,
        template: MessageTemplate,
        lead: Lead,
        course: Course? = null
    ): Boolean {
        val message = templateProcessor.processTemplate(template, lead, course)
        val formattedNumber = formatPhoneNumber(phoneNumber)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://api.whatsapp.com/send?phone=$formattedNumber&text=${
                    Uri.encode(message)
                }"
            )
            setPackage("com.whatsapp")
        }
        
        return try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            true
        } catch (e: ActivityNotFoundException) {
            // WhatsApp not installed
            false
        }
    }
    
    private fun formatPhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
    }
}
```

### 3.3 Reporting & Analytics (Week 10-11)

#### 3.3.1 Dashboard Data
```kotlin
data class DashboardData(
    val totalLeads: Int,
    val newLeadsToday: Int,
    val newLeadsThisWeek: Int,
    val pendingFollowUps: Int,
    val overdueFollowUps: Int,
    val totalCallsToday: Int,
    val totalCallDurationToday: Long,
    val leadsByStatus: Map<String, Int>,
    val leadsBySource: Map<String, Int>,
    val leadsByCountry: Map<String, Int>,
    val conversionRate: Float,
    val topPerformingCounselors: List<CounselorStats>,
    val recentActivities: List<ActivityLog>
)

@Dao
interface DashboardDao {
    
    @Query("""
        SELECT 
            COUNT(*) as totalLeads,
            SUM(CASE WHEN DATE(createdAt/1000, 'unixepoch') = DATE('now') THEN 1 ELSE 0 END) as newLeadsToday,
            SUM(CASE WHEN DATE(createdAt/1000, 'unixepoch') >= DATE('now', '-7 days') THEN 1 ELSE 0 END) as newLeadsThisWeek,
            SUM(CASE WHEN nextFollowUpDate IS NOT NULL AND DATE(nextFollowUpDate/1000, 'unixepoch') = DATE('now') THEN 1 ELSE 0 END) as pendingFollowUps,
            SUM(CASE WHEN nextFollowUpDate IS NOT NULL AND DATE(nextFollowUpDate/1000, 'unixepoch') < DATE('now') THEN 1 ELSE 0 END) as overdueFollowUps
        FROM leads
        WHERE isDeleted = 0
    """)
    suspend fun getLeadStats(): LeadStats
    
    @Query("""
        SELECT statusId, COUNT(*) as count
        FROM leads
        WHERE isDeleted = 0
        GROUP BY statusId
    """)
    suspend fun getLeadsByStatus(): List<StatusCount>
    
    @Query("""
        SELECT source, COUNT(*) as count
        FROM leads
        WHERE isDeleted = 0
        GROUP BY source
        ORDER BY count DESC
    """)
    suspend fun getLeadsBySource(): List<SourceCount>
    
    @Query("""
        SELECT 
            COUNT(*) as totalCalls,
            SUM(duration) as totalDuration
        FROM call_logs
        WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now')
    """)
    suspend fun getTodayCallStats(): CallStats
}
```

#### 3.3.2 Dashboard Screen
```kotlin
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToLeads: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        // Stats Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Total Leads",
                    value = uiState.totalLeads.toString(),
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Today's Calls",
                    value = uiState.totalCallsToday.toString(),
                    icon = Icons.Default.Call,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Follow-up Alerts
        item {
            FollowUpAlertCard(
                pending = uiState.pendingFollowUps,
                overdue = uiState.overdueFollowUps,
                onClick = { onNavigateToLeads("follow_up") }
            )
        }
        
        // Lead Pipeline Chart
        item {
            PipelineChart(
                data = uiState.leadsByStatus,
                onStatusClick = { status ->
                    onNavigateToLeads(status)
                }
            )
        }
        
        // Lead Sources Chart
        item {
            SourcesChart(data = uiState.leadsBySource)
        }
        
        // Country Distribution
        item {
            CountryDistributionChart(data = uiState.leadsByCountry)
        }
        
        // Recent Activities
        item {
            RecentActivitiesSection(
                activities = uiState.recentActivities,
                onViewAll = { /* Navigate to activities */ }
            )
        }
    }
}
```

### 3.4 Push Notifications (Week 11)

#### 3.4.1 FCM Service
```kotlin
class FCMService : FirebaseMessagingService() {
    
    @Inject lateinit var notificationManager: AppNotificationManager
    @Inject lateinit var leadRepository: LeadRepository
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            // Update token on server
            updateFcmToken(token)
        }
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        val data = message.data
        val type = data["type"]
        
        when (type) {
            "LEAD_ASSIGNED" -> handleLeadAssigned(data)
            "FOLLOW_UP_REMINDER" -> handleFollowUpReminder(data)
            "NEW_INQUIRY" -> handleNewInquiry(data)
            "STATUS_UPDATE" -> handleStatusUpdate(data)
            "ANNOUNCEMENT" -> handleAnnouncement(data)
        }
    }
    
    private fun handleFollowUpReminder(data: Map<String, String>) {
        val leadId = data["lead_id"] ?: return
        val leadName = data["lead_name"] ?: "Unknown"
        val message = data["message"] ?: "Follow-up reminder"
        
        notificationManager.showNotification(
            channelId = NotificationChannels.REMINDERS,
            title = "Follow-up: $leadName",
            message = message,
            intent = createLeadDetailIntent(leadId),
            actions = listOf(
                NotificationAction("Call", createCallIntent(leadId)),
                NotificationAction("Snooze", createSnoozeIntent(leadId))
            )
        )
    }
}
```

#### 3.4.2 Scheduled Reminders
```kotlin
@HiltWorker
class FollowUpReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val leadRepository: LeadRepository,
    private val notificationManager: AppNotificationManager
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val todayFollowUps = leadRepository.getFollowUpsForToday()
        
        todayFollowUps.forEach { lead ->
            notificationManager.scheduleNotification(
                id = lead.id.hashCode(),
                time = lead.nextFollowUpDate!!,
                title = "Follow-up: ${lead.firstName}",
                message = lead.reminderNote ?: "Time for scheduled follow-up",
                data = mapOf("lead_id" to lead.id)
            )
        }
        
        return Result.success()
    }
}
```

### 3.5 Offline Support & Sync (Week 11-12)

#### 3.5.1 Sync Manager
```kotlin
class SyncManager @Inject constructor(
    private val leadRepository: LeadRepository,
    private val callLogRepository: CallLogRepository,
    private val noteRepository: NoteRepository,
    private val networkMonitor: NetworkMonitor,
    private val workManager: WorkManager
) {
    
    fun schedulePeriodSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    suspend fun syncNow(): SyncResult {
        return withContext(Dispatchers.IO) {
            try {
                // Sync in order of dependency
                val leadsResult = syncLeads()
                val callsResult = syncCallLogs()
                val notesResult = syncNotes()
                
                SyncResult.Success(
                    leadsSynced = leadsResult,
                    callsSynced = callsResult,
                    notesSynced = notesResult
                )
            } catch (e: Exception) {
                SyncResult.Error(e.message ?: "Sync failed")
            }
        }
    }
    
    private suspend fun syncLeads(): Int {
        val pendingLeads = leadRepository.getPendingSync()
        var synced = 0
        
        pendingLeads.forEach { lead ->
            try {
                leadRepository.syncToServer(lead)
                leadRepository.markAsSynced(lead.id)
                synced++
            } catch (e: Exception) {
                // Mark for retry
                leadRepository.markSyncFailed(lead.id)
            }
        }
        
        return synced
    }
}
```

#### 3.5.2 Conflict Resolution
```kotlin
sealed class ConflictResolution {
    object UseLocal : ConflictResolution()
    object UseRemote : ConflictResolution()
    data class Merge(val merged: Lead) : ConflictResolution()
}

class ConflictResolver @Inject constructor() {
    
    fun resolveLeadConflict(
        local: Lead,
        remote: Lead
    ): ConflictResolution {
        // If remote is newer by more than a threshold, use remote
        if (remote.updatedAt - local.updatedAt > 60_000) { // 1 minute
            return ConflictResolution.UseRemote
        }
        
        // If local is newer, use local
        if (local.updatedAt > remote.updatedAt) {
            return ConflictResolution.UseLocal
        }
        
        // Merge: Take most recent value for each field
        val merged = Lead(
            id = local.id,
            firstName = mostRecent(local.firstName, remote.firstName, local.updatedAt, remote.updatedAt),
            // ... merge other fields
            notes = (local.notes + remote.notes).distinctBy { it.id },
            updatedAt = maxOf(local.updatedAt, remote.updatedAt)
        )
        
        return ConflictResolution.Merge(merged)
    }
}
```

---

## PHASE 4: POLISH & DEPLOYMENT (Weeks 13-16)

### 4.1 Testing (Week 13)

#### 4.1.1 Unit Tests
```kotlin
@Test
fun `lead repository returns leads filtered by status`() = runTest {
    // Given
    val leads = listOf(
        createLead(status = "NEW"),
        createLead(status = "COUNSELING"),
        createLead(status = "NEW")
    )
    leadDao.insertAll(leads)
    
    // When
    val result = repository.getLeadsByStatus("NEW")
    
    // Then
    assertThat(result).hasSize(2)
    assertThat(result.all { it.status == "NEW" }).isTrue()
}

@Test
fun `template processor replaces all placeholders`() {
    // Given
    val template = MessageTemplate(
        content = "Hi {{student_name}}, your application for {{course_name}} is ready."
    )
    val lead = Lead(studentName = "John")
    val course = Course(name = "Computer Science")
    
    // When
    val result = processor.processTemplate(template, lead, course)
    
    // Then
    assertThat(result).isEqualTo(
        "Hi John, your application for Computer Science is ready."
    )
}
```

#### 4.1.2 Integration Tests
```kotlin
@HiltAndroidTest
class LeadSyncIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject lateinit var leadRepository: LeadRepository
    @Inject lateinit var syncManager: SyncManager
    
    @Test
    fun `offline created lead syncs when network available`() = runTest {
        // Given - Create lead while offline
        networkMonitor.setConnected(false)
        val lead = createLead()
        leadRepository.insert(lead)
        
        // When - Network becomes available
        networkMonitor.setConnected(true)
        syncManager.syncNow()
        
        // Then - Lead is synced
        val synced = leadRepository.getById(lead.id)
        assertThat(synced.syncStatus).isEqualTo(SyncStatus.SYNCED)
    }
}
```

### 4.2 Security (Week 14)

#### 4.2.1 Data Encryption
```kotlin
class EncryptedPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun saveToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }
    
    fun getToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }
}
```

#### 4.2.2 Certificate Pinning
```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    val certificatePinner = CertificatePinner.Builder()
        .add("api.educonsult.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build()
    
    return OkHttpClient.Builder()
        .certificatePinner(certificatePinner)
        .build()
}
```

### 4.3 Performance Optimization (Week 14-15)

#### 4.3.1 Database Optimization
```kotlin
// Add indices for frequently queried columns
@Entity(
    tableName = "leads",
    indices = [
        Index("phone"),
        Index("statusId"),
        Index("assignedTo"),
        Index("nextFollowUpDate"),
        Index("createdAt")
    ]
)

// Use pagination for large lists
@Query("""
    SELECT * FROM leads 
    WHERE isDeleted = 0 
    ORDER BY createdAt DESC 
    LIMIT :limit OFFSET :offset
""")
suspend fun getLeadsPaged(limit: Int, offset: Int): List<LeadEntity>
```

#### 4.3.2 Image Caching
```kotlin
@Composable
fun CachedImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
```

### 4.4 Deployment (Week 15-16)

#### 4.4.1 Build Variants
```kotlin
// build.gradle.kts
android {
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            buildConfigField("String", "BASE_URL", "\"https://dev-api.educonsult.com/\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "BASE_URL", "\"https://api.educonsult.com/\"")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            buildConfigField("String", "BASE_URL", "\"https://staging-api.educonsult.com/\"")
        }
        create("production") {
            dimension = "environment"
        }
    }
}
```

#### 4.4.2 ProGuard Rules
```proguard
# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Keep data classes
-keep class com.educonsult.crm.data.model.** { *; }
-keep class com.educonsult.crm.data.remote.dto.** { *; }
```

---

## APPENDIX A: API Response Models

```kotlin
// Base Response
data class BaseResponse<T>(
    val status: Boolean,
    val message: String?,
    val data: T?,
    val errorCode: String?
)

// Lead Response
data class LeadListResponse(
    val leads: List<LeadDto>,
    val totalCount: Int,
    val pageNo: Int,
    val pageSize: Int,
    val hasMore: Boolean
)

data class LeadDto(
    val id: String,
    val firstName: String,
    val lastName: String?,
    val number: String,
    val secondaryNumber: String?,
    val countryCode: Int,
    val email: String?,
    val status: String,
    val assignedTo: List<EmployeeDto>?,
    val leadTags: List<String>?,
    val properties: Map<String, DynamicFieldDto>?,
    val createdDate: String,
    val modifiedDate: String?,
    val reminderDate: String?,
    val lastCallDetails: CallDetailsDto?
)
```

## APPENDIX B: Error Handling

```kotlin
sealed class AppError {
    data class Network(val message: String) : AppError()
    data class Server(val code: Int, val message: String) : AppError()
    data class Auth(val message: String) : AppError()
    data class Database(val message: String) : AppError()
    data class Unknown(val throwable: Throwable) : AppError()
}

fun Throwable.toAppError(): AppError = when (this) {
    is IOException -> AppError.Network("Network error: ${message}")
    is HttpException -> AppError.Server(code(), message())
    is SQLiteException -> AppError.Database("Database error: ${message}")
    else -> AppError.Unknown(this)
}
```

## APPENDIX C: Feature Flags

```kotlin
object FeatureFlags {
    const val CALL_RECORDING = "call_recording"
    const val WHATSAPP_INTEGRATION = "whatsapp_integration"
    const val COURSE_RECOMMENDATIONS = "course_recommendations"
    const val ADVANCED_ANALYTICS = "advanced_analytics"
    const val MULTI_BRANCH = "multi_branch"
}

class FeatureFlagManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {
    fun isEnabled(feature: String): Boolean {
        return remoteConfig.getBoolean(feature)
    }
    
    suspend fun refresh() {
        remoteConfig.fetchAndActivate().await()
    }
}
```

---

## Summary

This implementation plan provides a comprehensive roadmap for building an Educational Consultancy CRM based on the Callyzer Biz architecture. The plan is divided into 4 phases:

1. **Foundation (Weeks 1-4)**: Project setup, authentication, database, and networking
2. **Core Features (Weeks 5-8)**: Lead management, call tracking, course/institution modules
3. **Advanced Features (Weeks 9-12)**: Call recording, templates, analytics, notifications, offline sync
4. **Polish & Deployment (Weeks 13-16)**: Testing, security, optimization, deployment

Total Estimated Timeline: **16 weeks** (4 months)

### Key Differentiators for Educational Consultancy

1. **Student-centric lead model** with educational background fields
2. **Course & Institution database** with recommendation engine
3. **Application pipeline tracking** (Inquiry → Counseling → Application → Visa → Enrollment)
4. **Country-specific workflow** support
5. **Document management** for applications
6. **Multi-intake support** (Fall/Spring/Rolling)
7. **Budget-based filtering** and recommendations

---

*Document Version: 1.0*  
*Created: Based on Callyzer Biz v2.9.8 analysis*
