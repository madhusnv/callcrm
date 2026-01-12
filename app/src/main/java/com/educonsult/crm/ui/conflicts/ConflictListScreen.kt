package com.educonsult.crm.ui.conflicts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.ui.components.EmptyState
import com.educonsult.crm.ui.components.ErrorMessage
import com.educonsult.crm.ui.components.LoadingIndicator
import com.educonsult.crm.ui.components.OfflineBanner
import com.educonsult.crm.ui.common.NetworkStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictListScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConflictListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkViewModel: NetworkStatusViewModel = hiltViewModel()
    val isConnected by networkViewModel.isConnected.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Conflicts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
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
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.conflicts.isEmpty() -> {
                EmptyState(
                    message = "No conflicts to resolve",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
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
                    items(uiState.conflicts) { lead ->
                        ConflictCard(
                            lead = lead,
                            onKeepLocal = { viewModel.resolveKeepLocal(lead.id) },
                            onUseServer = { viewModel.resolveUseServer(lead.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConflictCard(
    lead: Lead,
    onKeepLocal: () -> Unit,
    onUseServer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${lead.firstName} ${lead.lastName.orEmpty()}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lead.phone,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onKeepLocal) {
                    Text("Keep Local")
                }
                OutlinedButton(onClick = onUseServer) {
                    Text("Use Server")
                }
            }
        }
    }
}
