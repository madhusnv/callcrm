package com.educonsult.crm.util

import android.util.Patterns
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String?.isValidEmail(): Boolean {
    return !this.isNullOrBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String?.isValidPhone(): Boolean {
    if (this.isNullOrBlank()) return false
    val digitsOnly = this.filter { it.isDigit() }
    return digitsOnly.length in Constants.PHONE_NUMBER_MIN_LENGTH..Constants.PHONE_NUMBER_MAX_LENGTH
}

fun String?.orEmpty(default: String = ""): String = this ?: default

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercaseChar() }
    }
}

fun String.toInitials(maxLength: Int = 2): String {
    return split(" ")
        .filter { it.isNotBlank() }
        .take(maxLength)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
}

fun Long.toFormattedDate(pattern: String = Constants.DATE_FORMAT_DISPLAY): String {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        sdf.format(Date(this))
    } catch (e: Exception) {
        ""
    }
}

fun Long.toFormattedDateTime(): String {
    return toFormattedDate(Constants.DATETIME_FORMAT_DISPLAY)
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000} min ago"
        diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
        diff < 604_800_000 -> "${diff / 86_400_000} days ago"
        else -> toFormattedDate()
    }
}

fun String.toTimestamp(pattern: String = Constants.DATETIME_FORMAT_API): Long? {
    return try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(this)?.time
    } catch (e: Exception) {
        null
    }
}

fun <T> Flow<T>.collectWithLifecycle(
    lifecycleOwner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.repeatOnLifecycle(minActiveState) {
            collect(collector)
        }
    }
}

fun <T> Flow<T>.safeCatch(onError: (Throwable) -> Unit = {}): Flow<T> {
    return catch { e -> onError(e) }
}

fun Long.formatDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds % 60)
        else -> String.format(Locale.getDefault(), "%d:%02d", minutes, seconds % 60)
    }
}

fun String.maskPhone(visibleDigits: Int = 4): String {
    val digits = filter { it.isDigit() }
    if (digits.length <= visibleDigits) return this
    val masked = "*".repeat(digits.length - visibleDigits)
    return masked + digits.takeLast(visibleDigits)
}

fun String.maskEmail(): String {
    val atIndex = indexOf('@')
    if (atIndex <= 1) return this
    val visiblePart = take(1)
    val domain = substring(atIndex)
    val masked = "*".repeat(atIndex - 1)
    return "$visiblePart$masked$domain"
}
