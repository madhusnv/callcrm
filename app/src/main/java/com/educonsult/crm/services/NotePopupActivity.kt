package com.educonsult.crm.services

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.educonsult.crm.data.remote.dto.template.NoteTemplateDto
import com.educonsult.crm.ui.theme.EduConsultCRMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotePopupActivity : ComponentActivity() {

    private val viewModel: NotePopupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )

        val callLogId = intent.getStringExtra("call_log_id") ?: ""
        val leadId = intent.getStringExtra("lead_id")
        val leadName = intent.getStringExtra("lead_name") ?: "Unknown"
        val phoneNumber = intent.getStringExtra("phone_number") ?: ""
        val duration = intent.getIntExtra("duration", 0)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            
            EduConsultCRMTheme {
                NotePopupScreen(
                    uiState = uiState,
                    leadName = leadName,
                    phoneNumber = phoneNumber,
                    duration = duration,
                    hasLead = !leadId.isNullOrBlank(),
                    onNoteChange = viewModel::updateNoteText,
                    onTemplateSelected = viewModel::selectTemplate,
                    onStatusSelected = viewModel::selectStatus,
                    onToggleFollowUp = viewModel::toggleFollowUp,
                    onFollowUpDaysSelected = viewModel::selectFollowUpDays,
                    onFollowUpNoteChange = viewModel::updateFollowUpNote,
                    onSave = { 
                        viewModel.saveNote(callLogId, leadId) { finish() }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotePopupScreen(
    uiState: NotePopupUiState,
    leadName: String,
    phoneNumber: String,
    duration: Int,
    hasLead: Boolean,
    onNoteChange: (String) -> Unit,
    onTemplateSelected: (NoteTemplateDto) -> Unit,
    onStatusSelected: (LeadStatusOption) -> Unit,
    onToggleFollowUp: () -> Unit,
    onFollowUpDaysSelected: (Int) -> Unit,
    onFollowUpNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Call Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Lead info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = leadName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Call duration
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDuration(duration),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Template chips
                if (uiState.isLoadingTemplates) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (uiState.templates.isNotEmpty()) {
                    uiState.templatesByCategory.forEach { (category, templates) ->
                        Text(
                            text = category.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            templates.forEach { template ->
                                FilterChip(
                                    selected = uiState.selectedTemplate?.id == template.id,
                                    onClick = { onTemplateSelected(template) },
                                    label = { Text(template.name, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text = "Quick Notes:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.defaultQuickNotes.forEach { note ->
                            FilterChip(
                                selected = uiState.noteText == note,
                                onClick = { onNoteChange(note) },
                                label = { Text(note, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Note input
                OutlinedTextField(
                    value = uiState.noteText,
                    onValueChange = onNoteChange,
                    label = { Text("Add custom note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    enabled = !uiState.isSaving
                )

                // Status Update Section (only if lead exists)
                if (hasLead) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Update Status:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeadStatusOption.entries.forEach { status ->
                            FilterChip(
                                selected = uiState.selectedStatus == status,
                                onClick = { onStatusSelected(status) },
                                label = { Text(status.displayName, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }

                    // Follow-up Section
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Schedule Follow-up",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Switch(
                            checked = uiState.showFollowUp,
                            onCheckedChange = { onToggleFollowUp() }
                        )
                    }

                    AnimatedVisibility(visible = uiState.showFollowUp) {
                        Column {
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.followUpOptions.forEach { (days, label) ->
                                    FilterChip(
                                        selected = uiState.selectedFollowUpDays == days,
                                        onClick = { onFollowUpDaysSelected(days) },
                                        label = { Text(label, style = MaterialTheme.typography.bodySmall) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.followUpNote,
                                onValueChange = onFollowUpNoteChange,
                                label = { Text("Follow-up reminder") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                enabled = !uiState.isSaving
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !uiState.isSaving
                    ) {
                        Text("Skip")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onSave,
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) {
        "${minutes}m ${secs}s"
    } else {
        "${secs}s"
    }
}
