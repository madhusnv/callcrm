package com.educonsult.crm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.educonsult.crm.data.local.ApiKeyManager
import com.educonsult.crm.data.local.datastore.UserPreferences
import com.educonsult.crm.ui.navigation.EduConsultNavGraph
import com.educonsult.crm.ui.navigation.Screen
import com.educonsult.crm.ui.theme.EduConsultCRMTheme
import com.educonsult.crm.services.FcmService
import com.educonsult.crm.workers.SyncScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var apiKeyManager: ApiKeyManager

    @Inject
    lateinit var syncScheduler: SyncScheduler

    private var isReady by mutableStateOf(false)
    private var startDestination by mutableStateOf(Screen.Login.route)
    private var pendingNavRoute by mutableStateOf<String?>(null)
    private var isLoggedInState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                determineStartDestination()
                observeAuthState()
            }
        }

        captureNavRoute(intent)

        enableEdgeToEdge()

        setContent {
            EduConsultCRMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isReady) {
                        val navController = rememberNavController()
                        androidx.compose.runtime.LaunchedEffect(isReady, isLoggedInState, pendingNavRoute) {
                            val route = pendingNavRoute
                            if (isLoggedInState && route != null && route != Screen.Dashboard.route) {
                                navController.navigate(route)
                                pendingNavRoute = null
                            }
                        }
                        EduConsultNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }

    private suspend fun determineStartDestination() {
        val isLoggedIn = userPreferences.isLoggedIn.first()
        val hasApiKey = apiKeyManager.hasApiKey()

        startDestination = when {
            !hasApiKey -> Screen.Onboarding.route
            isLoggedIn -> Screen.Dashboard.route
            else -> Screen.Login.route
        }
        isReady = true
    }

    private suspend fun observeAuthState() {
        userPreferences.isLoggedIn.collect { isLoggedIn ->
            isLoggedInState = isLoggedIn
            if (isLoggedIn) {
                Timber.d("User logged in - starting sync scheduler")
                syncScheduler.scheduleSyncWork(this@MainActivity)
                // Trigger immediate sync on login
                syncScheduler.syncNow(this@MainActivity)
            } else {
                Timber.d("User logged out - cancelling sync scheduler")
                syncScheduler.cancelSyncWork(this@MainActivity)
            }
        }
    }

    private fun captureNavRoute(intent: android.content.Intent?) {
        val route = intent?.getStringExtra(FcmService.EXTRA_NAV_ROUTE)
        if (!route.isNullOrBlank()) {
            pendingNavRoute = route
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        captureNavRoute(intent)
    }
}
