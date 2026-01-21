# AI Chat App Development Plan

## Project Overview
Single-screen Android chat application using Kotlin Multiplatform with AI agent integration.

**Current State:** Clean KMP template with Compose Multiplatform, Material3, Android-only target.

---

## Architecture Decision

### Recommended: MVVM + Clean Architecture (Simplified)

```
composeApp/src/
├── commonMain/kotlin/ru/makscorp/project/
│   ├── data/
│   │   ├── api/          # API client, DTOs
│   │   └── repository/   # Repository implementations
│   ├── domain/
│   │   ├── model/        # Domain models (Message, ChatState)
│   │   └── usecase/      # Use cases (SendMessage, GetChatHistory)
│   └── presentation/
│       ├── chat/         # Chat screen UI + ViewModel
│       └── theme/        # App theming
└── androidMain/          # Android-specific implementations
```

---

## Development Phases

### Phase 1: Project Structure & Dependencies

**1.1 Add Required Dependencies**

Add to `libs.versions.toml`:
```toml
# Networking
ktor = "3.1.3"

# Serialization
kotlinx-serialization = "1.8.1"

# Coroutines
kotlinx-coroutines = "1.10.2"

# DI (optional, lightweight)
koin = "4.0.3"

# DateTime
kotlinx-datetime = "0.6.2"
```

**1.2 Setup commonMain Source Set**
- Create `commonMain` directory structure
- Move shared code from `androidMain` to `commonMain`
- Keep Android-specific code in `androidMain`

---

### Phase 2: Domain Layer

**2.1 Domain Models**

```kotlin
// Message.kt
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Instant,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageRole { USER, ASSISTANT }
enum class MessageStatus { SENDING, SENT, ERROR }
```

```kotlin
// ChatState.kt
data class ChatState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)
```

**2.2 Use Cases**

```kotlin
// SendMessageUseCase.kt
class SendMessageUseCase(private val repository: ChatRepository) {
    suspend operator fun invoke(message: String): Result<Message>
}
```

---

### Phase 3: Data Layer

**3.1 AI API Integration**

Choose one AI provider:

| Provider | Pros | Cons |
|----------|------|------|
| OpenAI API | Most popular, well-documented | Paid, requires API key |
| Anthropic Claude API | High quality responses | Paid, requires API key |
| Google Gemini | Free tier available | Limited free quota |
| Local LLM (Ollama) | Free, private | Requires local setup |

**3.2 API Client Setup (Example: OpenAI)**

```kotlin
// ChatApiClient.kt
class ChatApiClient(private val httpClient: HttpClient) {
    suspend fun sendMessage(
        messages: List<ChatMessageDto>
    ): ChatCompletionResponse
}
```

**3.3 Repository**

```kotlin
// ChatRepository.kt
interface ChatRepository {
    suspend fun sendMessage(userMessage: String): Result<Message>
    fun getMessages(): Flow<List<Message>>
}

// ChatRepositoryImpl.kt
class ChatRepositoryImpl(
    private val apiClient: ChatApiClient
) : ChatRepository
```

---

### Phase 4: Presentation Layer (UI)

**4.1 ChatViewModel**

```kotlin
class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun onMessageSend(text: String)
    fun onInputChange(text: String)
    fun onRetry(messageId: String)
}
```

**4.2 Chat Screen Components**

| Component | Description |
|-----------|-------------|
| `ChatScreen` | Main composable, orchestrates layout |
| `MessageList` | LazyColumn displaying messages |
| `MessageBubble` | Individual message UI (user/assistant styling) |
| `ChatInputBar` | TextField + Send button |
| `LoadingIndicator` | Typing indicator for AI responses |
| `ErrorBanner` | Error display with retry option |

**4.3 UI Layout**

```
┌─────────────────────────┐
│      App Bar            │
├─────────────────────────┤
│                         │
│    Message List         │
│    (LazyColumn)         │
│                         │
│  ┌───────────────────┐  │
│  │ User Message      │  │
│  └───────────────────┘  │
│                         │
│  ┌───────────────────┐  │
│  │ AI Response       │  │
│  └───────────────────┘  │
│                         │
│  [Typing Indicator...]  │
│                         │
├─────────────────────────┤
│ [TextField    ] [Send]  │
└─────────────────────────┘
```

---

### Phase 5: Features Implementation

**5.1 Core Features (MVP)**
- [ ] Send text messages to AI
- [ ] Display AI responses
- [ ] Message history in current session
- [ ] Loading state while waiting for response
- [ ] Error handling with retry

**5.2 Enhanced Features**
- [ ] Auto-scroll to latest message
- [ ] Keyboard handling (IME)
- [ ] Copy message to clipboard
- [ ] Markdown rendering for AI responses
- [ ] Message timestamps

**5.3 Polish Features**
- [ ] Smooth animations (message appear)
- [ ] Haptic feedback on send
- [ ] Dark/Light theme support
- [ ] Empty state (first launch)

---

### Phase 6: Testing

**6.1 Unit Tests**
- ViewModel tests with fake repository
- Use case tests
- Repository tests with mock API

**6.2 UI Tests**
- Compose UI tests for ChatScreen
- Input validation tests

---

## Implementation Order (Step-by-Step)

```
Step 1:  Setup commonMain + dependencies
Step 2:  Create domain models (Message, ChatState)
Step 3:  Build UI skeleton (ChatScreen layout)
Step 4:  Implement ChatViewModel with mock data
Step 5:  Wire up UI to ViewModel
Step 6:  Add Ktor HTTP client
Step 7:  Implement API client for chosen AI provider
Step 8:  Create repository implementation
Step 9:  Connect real API to ViewModel
Step 10: Add error handling
Step 11: Polish UI (animations, theming)
Step 12: Add tests
```

---

## File Structure (Final)

```
composeApp/src/
├── commonMain/
│   └── kotlin/ru/makscorp/project/
│       ├── data/
│       │   ├── api/
│       │   │   ├── ChatApiClient.kt
│       │   │   ├── dto/
│       │   │   │   ├── ChatRequestDto.kt
│       │   │   │   └── ChatResponseDto.kt
│       │   │   └── HttpClientFactory.kt
│       │   └── repository/
│       │       └── ChatRepositoryImpl.kt
│       ├── domain/
│       │   ├── model/
│       │   │   ├── Message.kt
│       │   │   └── ChatState.kt
│       │   ├── repository/
│       │   │   └── ChatRepository.kt
│       │   └── usecase/
│       │       └── SendMessageUseCase.kt
│       ├── presentation/
│       │   ├── chat/
│       │   │   ├── ChatScreen.kt
│       │   │   ├── ChatViewModel.kt
│       │   │   └── components/
│       │   │       ├── MessageList.kt
│       │   │       ├── MessageBubble.kt
│       │   │       ├── ChatInputBar.kt
│       │   │       └── TypingIndicator.kt
│       │   └── theme/
│       │       └── Theme.kt
│       ├── di/
│       │   └── AppModule.kt
│       └── App.kt
└── androidMain/
    └── kotlin/ru/makscorp/project/
        ├── MainActivity.kt
        └── Platform.android.kt
```

---

## Dependencies to Add (Complete)

```toml
# libs.versions.toml additions

[versions]
ktor = "3.1.3"
kotlinx-serialization = "1.8.1"
kotlinx-coroutines = "1.10.2"
kotlinx-datetime = "0.6.2"
koin = "4.0.3"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }

kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

[plugins]
kotlinx-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

---

## API Key Management

Store API keys securely:
1. Use `local.properties` (not committed to git)
2. Read via BuildConfig field
3. Never hardcode in source

```kotlin
// build.gradle.kts
android {
    defaultConfig {
        val apiKey = project.findProperty("AI_API_KEY") ?: ""
        buildConfigField("String", "AI_API_KEY", "\"$apiKey\"")
    }
}
```

---

## Decisions Made

| Decision | Choice |
|----------|--------|
| AI Provider | Configurable endpoint (OpenAI-compatible API) |
| Persistence | In-memory only (no database) |
| Response Mode | Complete response (not streaming) |

---

## API Configuration

The app will use a configurable API endpoint compatible with OpenAI's chat completion format.
This allows integration with:
- OpenAI
- Azure OpenAI
- Local LLMs (Ollama, LM Studio)
- Any OpenAI-compatible API

**Configuration (local.properties):**
```properties
AI_API_HOST=https://api.openai.com
AI_API_KEY=your-api-key-here
AI_MODEL=gpt-4
```

**BuildConfig Fields:**
```kotlin
BuildConfig.AI_API_HOST  // Base URL
BuildConfig.AI_API_KEY   // API Key
BuildConfig.AI_MODEL     // Model name
```
