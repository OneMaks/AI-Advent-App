package ru.makscorp.project.util

/**
 * Estimates the number of tokens in a text string.
 * Uses heuristics based on character types:
 * - English/Latin: ~4 characters per token
 * - Cyrillic (Russian): ~2 characters per token (due to UTF-8 encoding)
 * - CJK characters: ~1-2 characters per token
 * - Punctuation and special chars: ~1 character per token
 */
object TokenEstimator {

    fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0

        var tokenCount = 0.0

        for (char in text) {
            tokenCount += when {
                char.isWhitespace() -> 0.25
                char in '\u0400'..'\u04FF' -> 0.5  // Cyrillic
                char in '\u4E00'..'\u9FFF' -> 1.0  // CJK
                char in '\u3040'..'\u30FF' -> 0.75 // Japanese Hiragana/Katakana
                char.isLetterOrDigit() -> 0.25    // Latin letters and digits
                else -> 0.5                        // Punctuation and other
            }
        }

        // Add tokens for word boundaries (approximately)
        val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }
        tokenCount += words.size * 0.1

        return maxOf(1, tokenCount.toInt())
    }

    fun estimateTokensForMessages(messages: List<String>): Int {
        return messages.sumOf { estimateTokens(it) }
    }
}
