package com.lmstudio.chat.utils

/**
 * Utility extension functions.
 */

/**
 * Truncates a string to the specified length, adding ellipsis if truncated.
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (length > maxLength) {
        take(maxLength - ellipsis.length) + ellipsis
    } else {
        this
    }
}

/**
 * Wraps text to the specified line width.
 */
fun String.wrap(width: Int): String {
    if (width <= 0 || length <= width) return this

    val result = StringBuilder()
    var currentLineLength = 0

    split(" ").forEach { word ->
        when {
            currentLineLength == 0 -> {
                result.append(word)
                currentLineLength = word.length
            }
            currentLineLength + word.length + 1 <= width -> {
                result.append(" ").append(word)
                currentLineLength += word.length + 1
            }
            else -> {
                result.append("\n").append(word)
                currentLineLength = word.length
            }
        }
    }

    return result.toString()
}

/**
 * Estimates the number of tokens in a string.
 * Uses a rough estimate of 4 characters per token.
 */
fun String.estimateTokens(): Int = (length + 3) / 4

/**
 * Formats a number of bytes as a human-readable size.
 */
fun Long.formatBytes(): String {
    return when {
        this < 1024 -> "$this B"
        this < 1024 * 1024 -> String.format("%.1f KB", this / 1024.0)
        this < 1024 * 1024 * 1024 -> String.format("%.1f MB", this / (1024.0 * 1024))
        else -> String.format("%.1f GB", this / (1024.0 * 1024 * 1024))
    }
}

/**
 * Formats a duration in milliseconds as a human-readable string.
 */
fun Long.formatDuration(): String {
    return when {
        this < 1000 -> "${this}ms"
        this < 60000 -> String.format("%.1fs", this / 1000.0)
        else -> {
            val minutes = this / 60000
            val seconds = (this % 60000) / 1000
            "${minutes}m ${seconds}s"
        }
    }
}
