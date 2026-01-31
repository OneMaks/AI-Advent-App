package ru.makscorp.project.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.model.MessageStatus
import ru.makscorp.project.domain.model.TokenUsage

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )

                if (message.status == MessageStatus.ERROR) {
                    Text(
                        text = "Failed to send",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (message.status == MessageStatus.SENDING) {
                    Text(
                        text = "Sending...",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Token usage display for assistant messages
                if (!isUser && message.tokenUsage != null) {
                    TokenUsageInfo(
                        tokenUsage = message.tokenUsage,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenUsageInfo(
    tokenUsage: TokenUsage,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(4.dp))

        val textColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
        val labelStyle = MaterialTheme.typography.labelSmall

        // Estimated tokens
        Text(
            text = "Расчёт: ${tokenUsage.estimatedPromptTokens} → ${tokenUsage.estimatedCompletionTokens} (${tokenUsage.estimatedTotal})",
            style = labelStyle,
            color = textColor
        )

        // Actual tokens from API
        if (tokenUsage.hasActualData) {
            Text(
                text = "Факт: ${tokenUsage.actualPromptTokens} → ${tokenUsage.actualCompletionTokens} (${tokenUsage.actualTotalTokens})",
                style = labelStyle,
                color = textColor
            )
        }
    }
}
