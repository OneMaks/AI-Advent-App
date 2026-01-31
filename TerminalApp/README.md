# LM Studio Terminal Chat

A command-line chat application in Kotlin that connects to a local LM Studio instance via its OpenAI-compatible REST API.

## Features

- **Interactive Chat Loop**: Real-time conversation with local LLMs
- **Streaming Responses**: See tokens as they arrive (typewriter effect)
- **Conversation Management**: Save and load chat history to/from JSON files
- **Multi-Model Support**: Switch between available models on the fly
- **Configurable Parameters**: Adjust temperature, max_tokens, top_p, and more
- **System Prompts**: Customize AI behavior with system prompts
- **Colored Terminal Output**: Easy-to-read color-coded messages
- **Token Usage Tracking**: Monitor your token consumption

## Requirements

- **Java 17** or higher
- **LM Studio** running locally with the server enabled
- **Gradle** (wrapper included)

## Quick Start

### 1. Start LM Studio

1. Open LM Studio
2. Load a model
3. Go to "Local Server" tab
4. Click "Start Server" (default port: 1234)

### 2. Build and Run

```bash
# Clone or navigate to the project directory
cd TerminalApp

# Run with Gradle
./gradlew run

# Or build a JAR and run it
./gradlew fatJar
java -jar build/libs/kotlin-lmstudio-chat-all.jar
```

## Usage

Once started, you can chat directly or use commands:

```
╔══════════════════════════════════════════════════════════════╗
║               LM Studio Terminal Chat                        ║
╚══════════════════════════════════════════════════════════════╝

Connected to LM Studio at http://localhost:1234/v1
Current model: llama-3.2-1b
Type /help for commands or start chatting!

You: Hello! Who are you?
AI: Hi! I'm an AI assistant running locally on your computer via LM Studio...

You: /system You are a helpful coding assistant specialized in Kotlin
System prompt updated.

You: How do I create a coroutine in Kotlin?
AI: To create a coroutine in Kotlin, you can use the `launch` or `async` builders...
```

## Commands

| Command | Description |
|---------|-------------|
| `/help` | Show available commands |
| `/clear` | Clear conversation history |
| `/save <filename>` | Save conversation to file |
| `/load <filename>` | Load conversation from file |
| `/model <name>` | Switch to a different model |
| `/models` | List available models |
| `/config` | Show current configuration |
| `/set <param> <value>` | Set a parameter (temperature, max_tokens, top_p) |
| `/system <prompt>` | Set the system prompt |
| `/history` | Show conversation history |
| `/tokens` | Show token usage statistics |
| `/multiline` | Enter multi-line input mode |
| `/exit`, `/quit` | Exit the application |

## Configuration

### Configuration File

Create a `config.properties` file in the application directory:

```properties
# Server Settings
server.url=http://localhost:1234/v1
model=llama-3.2-1b

# Generation Parameters
temperature=0.7
max_tokens=2000
top_p=1.0
frequency_penalty=0.0
presence_penalty=0.0

# Application Settings
streaming=true
colors=true
context_window=4096
conversations_dir=conversations

# Default system prompt (optional)
system_prompt=You are a helpful AI assistant.
```

### Environment Variables

Environment variables override file configuration:

| Variable | Description |
|----------|-------------|
| `LMSTUDIO_URL` | LM Studio server URL |
| `LMSTUDIO_MODEL` | Default model to use |
| `LMSTUDIO_TEMPERATURE` | Temperature setting (0.0-2.0) |
| `LMSTUDIO_MAX_TOKENS` | Maximum tokens for response |
| `LMSTUDIO_TOP_P` | Top-p sampling parameter |
| `LMSTUDIO_STREAMING` | Enable/disable streaming |
| `LMSTUDIO_COLORS` | Enable/disable colors |
| `LMSTUDIO_SYSTEM_PROMPT` | Default system prompt |

## Project Structure

```
TerminalApp/
├── build.gradle.kts           # Gradle build configuration
├── settings.gradle.kts        # Gradle settings
├── config.properties          # Application configuration
├── README.md                  # This file
├── conversations/             # Saved conversations directory
└── src/
    └── main/
        ├── kotlin/
        │   └── com/lmstudio/chat/
        │       ├── Main.kt                    # Entry point
        │       ├── ChatApplication.kt         # Main application logic
        │       ├── api/
        │       │   ├── LMStudioClient.kt      # API client
        │       │   └── models/                # Data models
        │       │       ├── Message.kt
        │       │       ├── ChatRequest.kt
        │       │       ├── ChatResponse.kt
        │       │       ├── StreamChunk.kt
        │       │       └── ModelsResponse.kt
        │       ├── terminal/
        │       │   ├── TerminalUI.kt          # Terminal rendering
        │       │   ├── CommandProcessor.kt    # Command parsing
        │       │   └── Colors.kt              # ANSI color codes
        │       ├── storage/
        │       │   ├── ConversationManager.kt # Conversation persistence
        │       │   └── ConfigurationManager.kt # Config management
        │       └── utils/
        │           └── Extensions.kt          # Utility functions
        └── resources/
            └── config.properties              # Default configuration
```

## API Integration

This application uses LM Studio's OpenAI-compatible API:

### Endpoints

- `GET /v1/models` - List available models
- `POST /v1/chat/completions` - Chat completion (supports streaming)

### Example Request

```json
{
  "model": "llama-3.2-1b",
  "messages": [
    {"role": "system", "content": "You are a helpful assistant."},
    {"role": "user", "content": "Hello!"}
  ],
  "temperature": 0.7,
  "max_tokens": 2000,
  "stream": true
}
```

## Troubleshooting

### Cannot connect to LM Studio

1. Ensure LM Studio is running
2. Check that the server is started in LM Studio's "Local Server" tab
3. Verify the server URL and port in configuration
4. Check firewall settings

### Model not found

1. Use `/models` to list available models
2. Ensure a model is loaded in LM Studio
3. Use the exact model name from the list

### Slow responses

1. Reduce `max_tokens` value
2. Use a smaller model
3. Enable streaming for better perceived performance

### No colors in terminal

1. Ensure your terminal supports ANSI colors
2. Set `colors=true` in configuration
3. Some terminals may require specific settings

## Dependencies

- **OkHttp 4.12.0** - HTTP client
- **kotlinx.serialization 1.6.2** - JSON processing
- **kotlinx.coroutines 1.7.3** - Async programming

## Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create fat JAR with all dependencies
./gradlew fatJar

# Run the application
./gradlew run
```

## License

MIT License - Feel free to use and modify as needed.
