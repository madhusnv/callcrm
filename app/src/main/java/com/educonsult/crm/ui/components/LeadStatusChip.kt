package com.educonsult.crm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.educonsult.crm.domain.model.LeadStatus

@Composable
fun LeadStatusChip(
    status: LeadStatus,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(status.color) {
        parseColor(status.color)
    }
    val contentColor = remember(backgroundColor) {
        if (isColorDark(backgroundColor)) Color.White else Color.Black
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

private fun parseColor(colorString: String): Color {
    return try {
        val colorHex = colorString.removePrefix("#")
        val colorInt = colorHex.toLong(16)
        when (colorHex.length) {
            6 -> Color(0xFF000000 or colorInt)
            8 -> Color(colorInt)
            else -> Color.Gray
        }
    } catch (e: Exception) {
        Color.Gray
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
