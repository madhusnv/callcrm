package com.educonsult.crm.ui.leads.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.domain.model.Lead
import com.educonsult.crm.domain.model.LeadNote
import com.educonsult.crm.domain.model.NoteType
import com.educonsult.crm.ui.components.EmptyState
import com.educonsult.crm.ui.components.ErrorMessage
import com.educonsult.crm.ui.components.LeadStatusChip
import com.educonsult.crm.ui.components.LoadingIndicator
import com.educonsult.crm.ui.components.PriorityBadge
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LeadDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LeadDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                LeadDetailEvent.NoteAdded -> {
                    snackbarHostState.showSnackbar("Note added")
                }
                LeadDetailEvent.LeadDeleted -> {
                    onNavigateBack()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Lead") },
            text = { Text("Are you sure you want to delete this lead? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteLead()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    uiState.lead?.let { lead ->
                        Column {
                            Text(
                                text = lead.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            lead.status?.let { status ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LeadStatusChip(status = status)
                                    PriorityBadge(priority = lead.priority)
                                }
                            }
                        }
                    } ?: Text("Lead Details")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    uiState.lead?.let { lead ->
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${lead.phone}")
                                }
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call"
                            )
                        }
                        IconButton(onClick = { onNavigateToEdit(lead.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(padding))
            }
            uiState.error != null -> {
                ErrorMessage(
                    message = uiState.error,
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.lead != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    TabRow(
                        selectedTabIndex = uiState.selectedTab.ordinal
                    ) {
                        LeadDetailTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.selectedTab == tab,
                                onClick = { viewModel.onTabSelected(tab) },
                                text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }

                    when (uiState.selectedTab) {
                        LeadDetailTab.OVERVIEW -> OverviewTab(lead = uiState.lead!!)
                        LeadDetailTab.NOTES -> NotesTab(
                            notes = uiState.notes,
                            newNoteContent = uiState.newNoteContent,
                            isSavingNote = uiState.isSavingNote,
                            onNoteContentChange = viewModel::updateNewNoteContent,
                            onAddNote = { viewModel.addNote(it) }
                        )
                        LeadDetailTab.CALLS -> CallsTab(
                            lead = uiState.lead!!,
                            calls = uiState.calls
                        )
                        LeadDetailTab.TIMELINE -> TimelineTab(
                            lead = uiState.lead!!,
                            notes = uiState.notes
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    lead: Lead,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoSection(title = "Contact Information") {
            InfoRow(icon = Icons.Default.Person, label = "Name", value = lead.displayName)
            lead.parentName?.let {
                InfoRow(icon = Icons.Default.Person, label = "Parent/Guardian", value = it)
            }
            InfoRow(icon = Icons.Default.Call, label = "Phone", value = lead.phone)
            lead.secondaryPhone?.let {
                InfoRow(icon = Icons.Default.Call, label = "Secondary Phone", value = it)
            }
            lead.email?.let {
                InfoRow(icon = Icons.Default.Email, label = "Email", value = it)
            }
            lead.dateOfBirth?.let {
                InfoRow(
                    icon = Icons.Default.Schedule,
                    label = "Date of Birth",
                    value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                )
            }
        }

        lead.currentEducation?.let { education ->
            InfoSection(title = "Education") {
                InfoRow(icon = Icons.Default.School, label = "Current Education", value = education)
                lead.currentInstitution?.let {
                    InfoRow(icon = Icons.Default.School, label = "Institution", value = it)
                }
                lead.stream?.let {
                    InfoRow(icon = Icons.Default.School, label = "Stream", value = it)
                }
                lead.percentage?.let {
                    InfoRow(icon = Icons.Default.School, label = "Percentage", value = "${it}%")
                }
                lead.graduationYear?.let {
                    InfoRow(icon = Icons.Default.Schedule, label = "Graduation Year", value = it.toString())
                }
            }
        }

        if (lead.interestedCourses.isNotEmpty() || lead.preferredCountries.isNotEmpty()) {
            InfoSection(title = "Interests") {
                if (lead.interestedCourses.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Default.School,
                        label = "Interested Courses",
                        value = lead.interestedCourses.joinToString(", ")
                    )
                }
                if (lead.preferredCountries.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Outlined.Public,
                        label = "Preferred Countries",
                        value = lead.preferredCountries.joinToString(", ")
                    )
                }
                if (lead.preferredInstitutions.isNotEmpty()) {
                    InfoRow(
                        icon = Icons.Default.School,
                        label = "Preferred Institutions",
                        value = lead.preferredInstitutions.joinToString(", ")
                    )
                }
                lead.intakePreference?.let {
                    InfoRow(icon = Icons.Default.Schedule, label = "Intake Preference", value = it)
                }
            }
        }

        if (lead.budgetMin != null || lead.budgetMax != null) {
            InfoSection(title = "Budget") {
                val budget = when {
                    lead.budgetMin != null && lead.budgetMax != null ->
                        "₹${formatBudget(lead.budgetMin)} - ₹${formatBudget(lead.budgetMax)}"
                    lead.budgetMin != null -> "₹${formatBudget(lead.budgetMin)}+"
                    lead.budgetMax != null -> "Up to ₹${formatBudget(lead.budgetMax)}"
                    else -> ""
                }
                InfoRow(icon = Icons.Outlined.AttachMoney, label = "Budget Range", value = budget)
            }
        }

        if (lead.nextFollowUpDate != null || lead.source != null) {
            InfoSection(title = "Follow-up") {
                lead.nextFollowUpDate?.let {
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        label = "Next Follow-up",
                        value = it.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
                    )
                }
                lead.reminderNote?.let {
                    InfoRow(icon = Icons.Outlined.Note, label = "Reminder", value = it)
                }
                lead.source?.let {
                    InfoRow(icon = Icons.Outlined.History, label = "Source", value = it)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun InfoSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NotesTab(
    notes: List<LeadNote>,
    newNoteContent: String,
    isSavingNote: Boolean,
    onNoteContentChange: (String) -> Unit,
    onAddNote: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (notes.isEmpty()) {
            Box(modifier = Modifier.weight(1f)) {
                EmptyState(
                    message = "No notes yet. Add your first note below."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note = note)
                }
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = newNoteContent,
                onValueChange = onNoteContentChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a note...") },
                maxLines = 3,
                enabled = !isSavingNote
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = { onAddNote(newNoteContent) },
                enabled = newNoteContent.isNotBlank() && !isSavingNote,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: LeadNote,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.noteType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = note.createdAt.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CallsTab(
    lead: Lead,
    calls: List<com.educonsult.crm.data.local.db.entity.CallLogEntity>,
    modifier: Modifier = Modifier
) {
    if (calls.isEmpty() && lead.totalCalls == 0) {
        EmptyState(
            message = "No call history yet",
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Stats header
            CallStatsHeader(
                totalCalls = calls.size,
                incomingCalls = calls.count { it.callType == com.educonsult.crm.data.local.db.entity.CallLogEntity.TYPE_INCOMING },
                outgoingCalls = calls.count { it.callType == com.educonsult.crm.data.local.db.entity.CallLogEntity.TYPE_OUTGOING },
                missedCalls = calls.count { it.callType == com.educonsult.crm.data.local.db.entity.CallLogEntity.TYPE_MISSED }
            )

            // Call list
            CallsTabContent(
                calls = calls,
                onPlayRecording = { callLogId ->
                    // TODO: Implement recording playback via ViewModel
                    null
                }
            )
        }
    }
}

@Composable
private fun TimelineTab(
    lead: Lead,
    notes: List<LeadNote>,
    modifier: Modifier = Modifier
) {
    val timelineItems = remember(lead, notes) {
        buildList {
            add(TimelineItem(
                title = "Lead Created",
                description = "Lead was added to the system",
                timestamp = lead.createdAt,
                type = TimelineItemType.CREATED
            ))
            
            notes.forEach { note ->
                add(TimelineItem(
                    title = when (note.noteType) {
                        NoteType.CALL -> "Call Note"
                        NoteType.MEETING -> "Meeting Note"
                        NoteType.EMAIL -> "Email Note"
                        NoteType.FOLLOW_UP -> "Follow-up"
                        NoteType.STATUS_CHANGE -> "Status Changed"
                        NoteType.GENERAL -> "Note Added"
                    },
                    description = note.content,
                    timestamp = note.createdAt,
                    type = when (note.noteType) {
                        NoteType.CALL -> TimelineItemType.CALL
                        NoteType.STATUS_CHANGE -> TimelineItemType.STATUS
                        else -> TimelineItemType.NOTE
                    }
                ))
            }
            
            if (lead.updatedAt != lead.createdAt) {
                add(TimelineItem(
                    title = "Lead Updated",
                    description = "Lead information was modified",
                    timestamp = lead.updatedAt,
                    type = TimelineItemType.UPDATED
                ))
            }
        }.sortedByDescending { it.timestamp }
    }

    if (timelineItems.isEmpty()) {
        EmptyState(
            message = "No activity yet",
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(timelineItems) { item ->
                TimelineItemCard(item = item)
            }
        }
    }
}

@Composable
private fun TimelineItemCard(
    item: TimelineItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp)
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = when (item.type) {
                            TimelineItemType.CREATED -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            TimelineItemType.UPDATED -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                            TimelineItemType.CALL -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                            TimelineItemType.NOTE -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
                            TimelineItemType.STATUS -> androidx.compose.ui.graphics.Color(0xFFE91E63)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = item.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class TimelineItem(
    val title: String,
    val description: String,
    val timestamp: java.time.LocalDateTime,
    val type: TimelineItemType
)

private enum class TimelineItemType {
    CREATED, UPDATED, CALL, NOTE, STATUS
}

private fun formatBudget(amount: Long): String {
    return when {
        amount >= 10_000_000 -> "${amount / 10_000_000.0} Cr"
        amount >= 100_000 -> "${amount / 100_000.0} L"
        amount >= 1000 -> "${amount / 1000.0} K"
        else -> amount.toString()
    }
}
