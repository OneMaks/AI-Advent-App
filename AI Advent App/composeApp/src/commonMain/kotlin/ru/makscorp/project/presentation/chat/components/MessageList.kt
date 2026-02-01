package ru.makscorp.project.presentation.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.model.Message

@Composable
fun MessageList(
    messages: List<Message>,
    summaries: List<ContextSummary>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    // Общее количество элементов для правильного скролла
    val totalItems = summaries.size + messages.size

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(totalItems, isLoading) {
        if (totalItems > 0) {
            // Scroll to the last item (or typing indicator position)
            listState.animateScrollToItem(
                if (isLoading) totalItems else totalItems - 1
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Сначала отображаем резюме (сжатый контекст)
        items(
            items = summaries,
            key = { "summary_${it.id}" }
        ) { summary ->
            SummaryBubble(summary = summary)
        }

        // Затем обычные сообщения
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            MessageBubble(message = message)
        }

        if (isLoading) {
            item {
                TypingIndicator()
            }
        }
    }
}
