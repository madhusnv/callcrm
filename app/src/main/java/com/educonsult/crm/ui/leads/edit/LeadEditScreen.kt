package com.educonsult.crm.ui.leads.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.ui.components.LoadingButton
import com.educonsult.crm.ui.components.LoadingIndicator
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeadEditScreen(
    onNavigateBack: () -> Unit,
    onLeadSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeadEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LeadEditEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is LeadEditEvent.LeadSaved -> {
                    onLeadSaved(event.leadId)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(if (viewModel.isEditMode) "Edit Lead" else "Add Lead")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormSection(title = "Contact Information") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.form.firstName,
                                onValueChange = viewModel::updateFirstName,
                                label = "First Name *",
                                error = uiState.errors["firstName"],
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )
                            FormTextField(
                                value = uiState.form.lastName,
                                onValueChange = viewModel::updateLastName,
                                label = "Last Name",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )
                        }

                        FormTextField(
                            value = uiState.form.phone,
                            onValueChange = viewModel::updatePhone,
                            label = "Phone *",
                            error = uiState.errors["phone"],
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )

                        FormTextField(
                            value = uiState.form.secondaryPhone,
                            onValueChange = viewModel::updateSecondaryPhone,
                            label = "Secondary Phone",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                        )

                        FormTextField(
                            value = uiState.form.email,
                            onValueChange = viewModel::updateEmail,
                            label = "Email",
                            error = uiState.errors["email"],
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    FormSection(title = "Student Information") {
                        FormTextField(
                            value = uiState.form.studentName,
                            onValueChange = viewModel::updateStudentName,
                            label = "Student Name",
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.form.parentName,
                                onValueChange = viewModel::updateParentName,
                                label = "Parent/Guardian Name",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )
                            FormTextField(
                                value = uiState.form.relationship,
                                onValueChange = viewModel::updateRelationship,
                                label = "Relationship",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )
                        }

                        DatePickerField(
                            value = uiState.form.dateOfBirth,
                            onValueChange = viewModel::updateDateOfBirth,
                            label = "Date of Birth"
                        )
                    }

                    FormSection(title = "Education") {
                        FormTextField(
                            value = uiState.form.currentEducation,
                            onValueChange = viewModel::updateCurrentEducation,
                            label = "Current Education",
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        FormTextField(
                            value = uiState.form.currentInstitution,
                            onValueChange = viewModel::updateCurrentInstitution,
                            label = "Current Institution",
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.form.stream,
                                onValueChange = viewModel::updateStream,
                                label = "Stream",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                )
                            )
                            FormTextField(
                                value = uiState.form.percentage?.toString() ?: "",
                                onValueChange = viewModel::updatePercentage,
                                label = "Percentage",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }

                        FormTextField(
                            value = uiState.form.graduationYear?.toString() ?: "",
                            onValueChange = viewModel::updateGraduationYear,
                            label = "Graduation Year",
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    FormSection(title = "Interests") {
                        ChipInputField(
                            values = uiState.form.interestedCourses,
                            onValuesChange = viewModel::updateInterestedCourses,
                            label = "Interested Courses",
                            placeholder = "Add course"
                        )

                        ChipInputField(
                            values = uiState.form.preferredCountries,
                            onValuesChange = viewModel::updatePreferredCountries,
                            label = "Preferred Countries",
                            placeholder = "Add country"
                        )

                        ChipInputField(
                            values = uiState.form.preferredInstitutions,
                            onValuesChange = viewModel::updatePreferredInstitutions,
                            label = "Preferred Institutions",
                            placeholder = "Add institution"
                        )

                        FormTextField(
                            value = uiState.form.intakePreference,
                            onValueChange = viewModel::updateIntakePreference,
                            label = "Intake Preference",
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )
                    }

                    FormSection(title = "Budget") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FormTextField(
                                value = uiState.form.budgetMin?.toString() ?: "",
                                onValueChange = viewModel::updateBudgetMin,
                                label = "Min Budget (₹)",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )
                            FormTextField(
                                value = uiState.form.budgetMax?.toString() ?: "",
                                onValueChange = viewModel::updateBudgetMax,
                                label = "Max Budget (₹)",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                )
                            )
                        }
                    }

                    FormSection(title = "Status & Priority") {
                        if (uiState.statuses.isNotEmpty()) {
                            StatusDropdown(
                                selectedStatusId = uiState.form.statusId,
                                statuses = uiState.statuses,
                                onStatusSelected = viewModel::updateStatusId
                            )
                        }

                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LeadPriority.entries.forEach { priority ->
                                FilterChip(
                                    selected = uiState.form.priority == priority,
                                    onClick = { viewModel.updatePriority(priority) },
                                    label = {
                                        Text(priority.name.lowercase().replaceFirstChar { it.uppercase() })
                                    }
                                )
                            }
                        }
                    }

                    FormSection(title = "Additional Information") {
                        FormTextField(
                            value = uiState.form.source,
                            onValueChange = viewModel::updateSource,
                            label = "Lead Source",
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            )
                        )

                        FormTextField(
                            value = uiState.form.reminderNote,
                            onValueChange = viewModel::updateReminderNote,
                            label = "Reminder Note",
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onNavigateBack) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    LoadingButton(
                        text = if (viewModel.isEditMode) "Update" else "Save",
                        onClick = viewModel::save,
                        isLoading = uiState.isSaving
                    )
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            singleLine = maxLines == 1
        )
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    value: LocalDate?,
    onValueChange: (LocalDate?) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onValueChange(date)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    OutlinedTextField(
        value = value?.toString() ?: "",
        onValueChange = { },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusDropdown(
    selectedStatusId: String?,
    statuses: List<com.educonsult.crm.domain.model.LeadStatus>,
    onStatusSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedStatus = statuses.find { it.id == selectedStatusId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedStatus?.name ?: "Select Status",
            onValueChange = { },
            label = { Text("Status") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            statuses.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.name) },
                    onClick = {
                        onStatusSelected(status.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipInputField(
    values: List<String>,
    onValuesChange: (List<String>) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var inputValue by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (values.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                values.forEach { value ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(value) },
                        trailingIcon = {
                            IconButton(
                                onClick = { onValuesChange(values - value) },
                                modifier = Modifier.then(Modifier)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.padding(0.dp)
                                )
                            }
                        },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = { inputValue = it },
                placeholder = { Text(placeholder) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputValue.isNotBlank()) {
                        onValuesChange(values + inputValue.trim())
                        inputValue = ""
                    }
                },
                enabled = inputValue.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
    }
}
