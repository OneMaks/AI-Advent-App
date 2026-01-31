package ru.makscorp.project.domain.model

/**
 * Token usage information for a message exchange
 *
 * @param estimatedPromptTokens Estimated tokens in the prompt (calculated locally)
 * @param estimatedCompletionTokens Estimated tokens in the completion (calculated locally)
 * @param actualPromptTokens Actual tokens in the prompt (from API response)
 * @param actualCompletionTokens Actual tokens in the completion (from API response)
 * @param actualTotalTokens Total actual tokens (from API response)
 */
data class TokenUsage(
    val estimatedPromptTokens: Int = 0,
    val estimatedCompletionTokens: Int = 0,
    val actualPromptTokens: Int? = null,
    val actualCompletionTokens: Int? = null,
    val actualTotalTokens: Int? = null
) {
    val estimatedTotal: Int
        get() = estimatedPromptTokens + estimatedCompletionTokens

    val hasActualData: Boolean
        get() = actualPromptTokens != null
}
