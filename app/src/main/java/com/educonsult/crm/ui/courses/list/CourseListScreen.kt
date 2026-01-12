package com.educonsult.crm.ui.courses.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.domain.model.Course
import com.educonsult.crm.ui.components.EmptyState
import com.educonsult.crm.ui.components.ErrorMessage
import com.educonsult.crm.ui.components.LoadingIndicator
import com.educonsult.crm.ui.components.OfflineBanner
import com.educonsult.crm.ui.common.NetworkStatusViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (String) -> Unit,
    viewModel: CourseListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val networkViewModel: NetworkStatusViewModel = hiltViewModel()
    val isConnected by networkViewModel.isConnected.collectAsStateWithLifecycle()

    val institutionMap = remember(uiState.institutions) {
        uiState.institutions.associateBy { it.id }
    }
    val countryMap = remember(uiState.countries) {
        uiState.countries.associateBy { it.id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Courses") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OfflineBanner(isOffline = !isConnected)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search courses") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterMenu(
                    label = "Country",
                    options = uiState.countries.map { it.id to it.name },
                    selectedId = uiState.selectedCountryId,
                    onSelect = viewModel::selectCountry
                )
                FilterMenu(
                    label = "Institution",
                    options = uiState.institutions.map { it.id to it.name },
                    selectedId = uiState.selectedInstitutionId,
                    onSelect = viewModel::selectInstitution
                )
                FilterMenu(
                    label = "Level",
                    options = listOf(
                        "bachelors" to "Bachelors",
                        "masters" to "Masters",
                        "diploma" to "Diploma"
                    ),
                    selectedId = uiState.selectedLevel,
                    onSelect = viewModel::selectLevel
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null -> {
                    ErrorMessage(message = uiState.error, onRetry = viewModel::refresh)
                }
                uiState.courses.isEmpty() -> {
                    EmptyState(message = "No courses found", actionLabel = "Refresh") {
                        viewModel.refresh()
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.courses) { course ->
                            CourseCard(
                                course = course,
                                institutionName = institutionMap[course.institutionId]?.name,
                                countryName = countryMap[course.countryId]?.name,
                                onClick = { onCourseClick(course.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            viewModel.applySearch()
        }
    }
}

@Composable
private fun FilterMenu(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelect: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second ?: "All"

    Column {
        OutlinedButton(onClick = { expanded = true }) {
            Text(text = "$label: $selectedLabel", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    expanded = false
                    onSelect(null)
                }
            )
            options.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    institutionName: String?,
    countryName: String?,
    onClick: () -> Unit
) {
    val feeText = remember(course.tuitionFee, course.currencyCode) {
        course.tuitionFee?.let {
            val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
            "${course.currencyCode ?: ""} ${formatter.format(it)}"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = listOfNotNull(institutionName, countryName).joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                if (!course.level.isNullOrBlank()) {
                    Text(
                        text = course.level.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (course.durationMonths != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${course.durationMonths} months",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (feeText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = feeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
