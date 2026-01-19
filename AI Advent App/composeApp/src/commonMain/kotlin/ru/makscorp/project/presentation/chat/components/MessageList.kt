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
import ru.makscorp.project.domain.model.Message

@Composable
fun MessageList(
    messages: List<Message>,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            // Scroll to the last item (or typing indicator position)
            listState.animateScrollToItem(
                if (isLoading) messages.size else messages.size - 1
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
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
