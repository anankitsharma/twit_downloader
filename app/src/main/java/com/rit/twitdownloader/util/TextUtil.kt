package com.rit.twitdownloader.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.text.isDigitsOnly
import com.rit.twitdownloader.App
import com.rit.twitdownloader.App.Companion.applicationScope
import com.rit.twitdownloader.App.Companion.context
import com.rit.twitdownloader.R
import java.util.regex.Pattern
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Deprecated("Use extension functions of Context to show a toast")
object ToastUtil {
    fun makeToast(text: String) {
        Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    fun makeToastSuspend(text: String) {
        applicationScope.launch(Dispatchers.Main) { makeToast(text) }
    }

    fun makeToast(stringId: Int) {
        Toast.makeText(context.applicationContext, context.getString(stringId), Toast.LENGTH_SHORT)
            .show()
    }
}

@MainThread
fun Context.makeToast(stringId: Int) {
    Toast.makeText(applicationContext, getString(stringId), Toast.LENGTH_SHORT).show()
}

@MainThread
fun Context.makeToast(message: String) {
    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}

private const val GIGA_BYTES = 1024f * 1024f * 1024f
private const val MEGA_BYTES = 1024f * 1024f

@Composable
fun Number?.toFileSizeText(): String {
    if (this == null) return stringResource(id = R.string.unknown)

    return this.toFloat().run {
        // Use Locale.US to ensure consistent decimal formatting (1.0 instead of 1,0)
        val formatter = java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols(java.util.Locale.US))
        if (this > GIGA_BYTES) {
            val size = formatter.format(this / GIGA_BYTES)
            stringResource(R.string.filesize_gb).replace("%.2f", size)
        } else {
            val size = formatter.format(this / MEGA_BYTES)
            stringResource(R.string.filesize_mb).replace("%.2f", size)
        }
    }
}

/** Convert time in **seconds** to `hh:mm:ss` or `mm:ss` */
fun Int.toDurationText(): String =
    this.run {
        if (this > 3600) "%d:%02d:%02d".format(this / 3600, (this % 3600) / 60, this % 60)
        else "%02d:%02d".format(this / 60, this % 60)
    }

fun String.isNumberInRange(start: Int, end: Int): Boolean {
    return this.isNotEmpty() &&
        this.isDigitsOnly() &&
        this.length < 10 &&
        this.toInt() >= start &&
        this.toInt() <= end
}

private const val URL_REGEX_PATTERN =
    "(http|https)://[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?"

// Enhanced URL validation patterns
private const val VALID_URL_PATTERN = 
    "^(https?://)?([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$"

private const val SIMPLE_URL_PATTERN = 
    "^(https?://)?[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?$"

fun String.isNumberInRange(range: IntRange): Boolean = this.isNumberInRange(range.first, range.last)

fun ClosedFloatingPointRange<Float>.toIntRange() =
    IntRange(start.roundToInt(), endInclusive.roundToInt())

fun String?.toHttpsUrl(): String =
    this?.run { if (matches(Regex("^(http:).*"))) replaceFirst("http", "https") else this } ?: ""

fun matchUrlFromClipboard(string: String, isMatchingMultiLink: Boolean = false): String {
    // Handle empty/null input gracefully - don't show error for empty clipboard
    if (string.isBlank()) return ""
    
    findURLsFromString(string, !isMatchingMultiLink).joinToString(separator = "\n").run {
        if (isEmpty()) ToastUtil.makeToast(R.string.paste_fail_msg)
        else ToastUtil.makeToast(R.string.paste_msg)
        return this
    }
}

fun matchUrlFromSharedText(s: String): String {
    // Handle empty/null input gracefully - don't show error for empty clipboard
    if (s.isBlank()) return ""
    
    findURLsFromString(s, true).joinToString(separator = "\n").run {
        if (isEmpty()) ToastUtil.makeToast(R.string.share_fail_msg)
        //            else makeToast(R.string.share_success_msg)
        return this
    }
}

fun Number?.toBitrateText(): String {
    val br = this?.toFloat() ?: return ""
    // Use Locale.US to ensure consistent decimal formatting
    val formatter = java.text.DecimalFormat("#.##", java.text.DecimalFormatSymbols(java.util.Locale.US))
    return when {
        br <= 0f -> "" // i don't care
        br < 1024f -> "${formatter.format(br)} Kbps"
        else -> "${formatter.format(br / 1024f)} Mbps"
    }
}

fun getErrorReport(th: Throwable, url: String): String =
    App.getVersionReport() + "\nURL: ${url}\n${th.message}"

@Deprecated(
    "Use findURLsFromString instead",
    ReplaceWith("findURLsFromString(s, !isMatchingMultiLink).joinToString(separator = \"\\n\")"),
)
fun matchUrlFromString(s: String, isMatchingMultiLink: Boolean = false): String =
    findURLsFromString(s, !isMatchingMultiLink).joinToString(separator = "\n")

/**
 * Validates if a string is a proper URL format
 * @param input The string to validate
 * @return true if the string is a valid URL format, false otherwise
 */
fun isValidUrlFormat(input: String): Boolean {
    if (input.isBlank()) return false
    
    // Check if it matches basic URL pattern
    val pattern = Pattern.compile(SIMPLE_URL_PATTERN, Pattern.CASE_INSENSITIVE)
    return pattern.matcher(input.trim()).matches()
}

/**
 * Validates if a string looks like a URL (more lenient than isValidUrlFormat)
 * @param input The string to validate
 * @return true if the string looks like a URL, false otherwise
 */
fun looksLikeUrl(input: String): Boolean {
    if (input.isBlank()) return false
    
    val trimmed = input.trim()
    
    // Check for common URL indicators
    return trimmed.contains(".") && 
           (trimmed.startsWith("http://") || 
            trimmed.startsWith("https://") || 
            trimmed.contains("://") ||
            trimmed.matches(Regex(".*\\.[a-zA-Z]{2,}.*")))
}

/**
 * Gets a user-friendly error message for invalid URL input
 * @param input The invalid input
 * @return A descriptive error message
 */
fun getUrlValidationErrorMessage(input: String): String {
    return when {
        input.isBlank() -> "Please enter a URL"
        input.length < 5 -> "URL is too short"
        !input.contains(".") -> "Please enter a valid URL (must contain a domain)"
        !looksLikeUrl(input) -> "Please enter a valid URL format (e.g., https://example.com)"
        else -> "Please enter a valid URL"
    }
}

fun findURLsFromString(input: String, firstMatchOnly: Boolean = false): List<String> {
    val result = mutableListOf<String>()
    val pattern = Pattern.compile(URL_REGEX_PATTERN)

    with(pattern.matcher(input)) {
        if (!firstMatchOnly) {
            while (find()) {
                result += group()
            }
        } else {
            if (find()) result += (group())
        }
    }
    return result
}

fun connectWithDelimiter(vararg strings: String?, delimiter: String): String =
    strings
        .toList()
        .filter { !it.isNullOrBlank() }
        .joinToString(separator = delimiter) { it.toString() }

fun connectWithBlank(s1: String, s2: String): String {
    val blank = if (s1.isEmpty() || s2.isEmpty()) "" else " "
    return s1 + blank + s2
}

