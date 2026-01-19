package ru.makscorp.project

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel
import ru.makscorp.project.presentation.chat.ChatScreen
import ru.makscorp.project.presentation.chat.ChatViewModel

@Composable
fun App() {
    MaterialTheme {
        val chatViewModel: ChatViewModel = koinViewModel()
        ChatScreen(viewModel = chatViewModel)
    }
}
