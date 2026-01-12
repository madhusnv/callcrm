package com.educonsult.crm.ui.leads.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.educonsult.crm.data.local.db.entity.CallLogEntity
import com.educonsult.crm.ui.components.AudioPlayer
import com.educonsult.crm.ui.components.EmptyState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CallsTabContent(
    calls: List<CallLogEntity>,
    onPlayRecording: (String) -> String?,  // Returns stream URL
    modifier: Modifier = Modifier
) {
    if (calls.isEmpty()) {
        EmptyState(
            message = "No call history yet",
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(calls, key = { it.id }) { callLog ->
                CallLogCard(
                    callLog = callLog,
                    onPlayRecording = onPlayRecording
                )
            }
        }
    }
}

@Composable
private fun CallLogCard(
    callLog: CallLogEntity,
    onPlayRecording: (String) -> String?,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var recordingUrl by remember { mutableStateOf<String?>(null) }
    var hasRecording by remember { mutableStateOf(false) }  // TODO: Check from recording entity

    val callIcon: ImageVector
    val callColor: Color
    val callTypeText: String

    when (callLog.callType) {
        CallLogEntity.TYPE_INCOMING -> {
            callIcon = Icons.Default.CallReceived
            callColor = Color(0xFF4CAF50)  // Green
            callTypeText = "Incoming"
        }
        CallLogEntity.TYPE_OUTGOING -> {
            callIcon = Icons.Default.CallMade
            callColor = Color(0xFF2196F3)  // Blue
            callTypeText = "Outgoing"
        }
        CallLogEntity.TYPE_MISSED -> {
            callIcon = Icons.Default.CallMissed
            callColor = Color(0xFFF44336)  // Red
            callTypeText = "Missed"
        }
        else -> {
            callIcon = Icons.Default.CallReceived
            callColor = MaterialTheme.colorScheme.onSurfaceVariant
            callTypeText = "Unknown"
        }
    }

    val formattedTime = remember(callLog.callAt) {
        val instant = Instant.ofEpochMilli(callLog.callAt)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        localDateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
    }

    val formattedDuration = remember(callLog.duration) {
        val mins = callLog.duration / 60
        val secs = callLog.duration % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { isExpanded = !isExpanded },
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Call type icon with background
                    Icon(
                        imageVector = callIcon,
                        contentDescription = callTypeText,
                        tint = callColor,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(callColor.copy(alpha = 0.1f))
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = callLog.contactName ?: callLog.phoneNumber,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = callTypeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = callColor
                            )
                            if (callLog.duration > 0) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedDuration,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Recording indicator
                    if (hasRecording) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = "Recording",
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("REC")
                            }
                        }
                    }
                }
            }

            // Notes
            callLog.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(12.dp)
                    )
                }
            }

            // Audio player (expanded)
            if (isExpanded && hasRecording) {
                Spacer(modifier = Modifier.height(12.dp))
                AudioPlayer(
                    url = recordingUrl ?: onPlayRecording(callLog.id).also { recordingUrl = it }
                )
            }
        }
    }
}

@Composable
fun CallStatsHeader(
    totalCalls: Int,
    incomingCalls: Int,
    outgoingCalls: Int,
    missedCalls: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CallStatItem(
            count = totalCalls,
            label = "Total",
            color = MaterialTheme.colorScheme.primary
        )
        CallStatItem(
            count = incomingCalls,
            label = "Incoming",
            color = Color(0xFF4CAF50)
        )
        CallStatItem(
            count = outgoingCalls,
            label = "Outgoing",
            color = Color(0xFF2196F3)
        )
        CallStatItem(
            count = missedCalls,
            label = "Missed",
            color = Color(0xFFF44336)
        )
    }
}

@Composable
private fun CallStatItem(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
