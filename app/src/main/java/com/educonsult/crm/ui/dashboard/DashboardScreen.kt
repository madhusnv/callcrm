package com.educonsult.crm.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.ui.components.ErrorMessage
import com.educonsult.crm.ui.components.LoadingIndicator
import com.educonsult.crm.ui.components.OfflineBanner
import com.educonsult.crm.ui.common.NetworkStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToConflicts: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkViewModel: NetworkStatusViewModel = hiltViewModel()
    val isConnected by networkViewModel.isConnected.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToConflicts) {
                        Icon(imageVector = Icons.Default.ReportProblem, contentDescription = "Conflicts")
                    }
                    IconButton(onClick = onNavigateToCourses) {
                        Icon(imageVector = Icons.Default.School, contentDescription = "Courses")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.error != null -> {
                ErrorMessage(
                    message = uiState.error,
                    modifier = Modifier.padding(padding),
                    onRetry = viewModel::refresh
                )
            }
            uiState.stats != null -> {
                val stats = uiState.stats
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OfflineBanner(isOffline = !isConnected)
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Total Leads",
                                value = stats.totalLeads.toString(),
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.SupervisorAccount
                            )
                            StatCard(
                                title = "New Today",
                                value = stats.newLeadsToday.toString(),
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Today
                            )
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                title = "Pending Follow-ups",
                                value = stats.pendingFollowUps.toString(),
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Warning
                            )
                            StatCard(
                                title = "Overdue Follow-ups",
                                value = stats.overdueFollowUps.toString(),
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Warning
                            )
                        }
                    }
                    item {
                        StatCard(
                            title = "Calls Today",
                            value = "${stats.totalCallsToday} calls • ${stats.totalCallDurationToday} sec",
                            modifier = Modifier.fillMaxWidth(),
                            icon = Icons.Default.Today
                        )
                    }
                }
            }
            else -> {
                Text(
                    text = "No dashboard data",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
