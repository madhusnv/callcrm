package com.educonsult.crm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.educonsult.crm.domain.model.LeadPriority
import com.educonsult.crm.ui.theme.Error
import com.educonsult.crm.ui.theme.Success
import com.educonsult.crm.ui.theme.Tertiary
import com.educonsult.crm.ui.theme.Warning

@Composable
fun PriorityBadge(
    priority: LeadPriority,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, icon) = when (priority) {
        LeadPriority.LOW -> Pair(Success, Icons.Default.KeyboardArrowDown)
        LeadPriority.MEDIUM -> Pair(Tertiary, Icons.Default.Remove)
        LeadPriority.HIGH -> Pair(Warning, Icons.Default.KeyboardArrowUp)
        LeadPriority.URGENT -> Pair(Error, Icons.Default.KeyboardDoubleArrowUp)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .background(
                color = backgroundColor.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = priority.name,
            tint = backgroundColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun PriorityBadgeWithLabel(
    priority: LeadPriority,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, label) = when (priority) {
        LeadPriority.LOW -> Pair(Success, "Low")
        LeadPriority.MEDIUM -> Pair(Tertiary, "Medium")
        LeadPriority.HIGH -> Pair(Warning, "High")
        LeadPriority.URGENT -> Pair(Error, "Urgent")
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = backgroundColor,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
