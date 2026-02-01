# AI Chat App - Architecture Overview

Kotlin Multiplatform приложение для чата с GigaChat API.

## Структура проекта

```
composeApp/src/
├── commonMain/kotlin/ru/makscorp/project/
│   ├── domain/           # Бизнес-логика
│   │   ├── model/        # Data classes
│   │   ├── repository/   # Интерфейсы репозиториев
│   │   └── service/      # Интерфейсы сервисов
│   ├── data/             # Реализации
│   │   ├── api/          # HTTP клиенты (Ktor)
│   │   ├── repository/   # Реализации репозиториев
│   │   ├── service/      # Реализации сервисов
│   │   └── storage/      # Expect classes для хранения
│   ├── presentation/     # UI (Jetpack Compose)
│   │   ├── chat/         # Экран чата
│   │   └── settings/     # Настройки
│   ├── di/               # Koin DI модули
│   └── util/             # Утилиты
└── androidMain/kotlin/ru/makscorp/project/
    ├── data/storage/     # Android-реализации (SharedPreferences)
    └── di/               # Android DI модуль
```

## Ключевые файлы

### Модели данных
- `domain/model/ChatSettings.kt` — настройки чата (модель, температура, сжатие контекста)
- `domain/model/Message.kt` — сообщение в чате
- `domain/model/ContextCompression.kt` — модели для сжатия контекста (ContextSummary, ConversationContext)

### Репозитории
- `domain/repository/ChatRepository.kt` — интерфейс работы с чатом
- `data/repository/ChatRepositoryImpl.kt` — реализация (отправка сообщений, сжатие контекста)
- `domain/repository/SettingsRepository.kt` — интерфейс настроек

### API
- `data/api/ChatApiClient.kt` — клиент GigaChat API (sendMessage, sendMessageForSummary)
- `data/api/AuthApiClient.kt` — OAuth 2.0 аутентификация

### Хранилище
- `data/storage/SettingsStorage.kt` — expect class для настроек
- `data/storage/ContextStorage.kt` — expect class для контекста сжатия
- `androidMain/.../SettingsStorage.android.kt` — реализация через SharedPreferences

### UI
- `presentation/chat/ChatScreen.kt` — главный экран
- `presentation/chat/ChatViewModel.kt` — ViewModel чата
- `presentation/chat/components/MessageList.kt` — список сообщений
- `presentation/chat/components/MessageBubble.kt` — пузырь сообщения
- `presentation/chat/components/SummaryBubble.kt` — блок резюме (сжатый контекст)
- `presentation/settings/SettingsBottomSheet.kt` — UI настроек

### DI
- `di/AppModule.kt` — общие зависимости (Koin)
- `androidMain/di/AndroidModule.kt` — Android-специфичные зависимости

## Архитектура

**Clean Architecture**: Presentation → Domain → Data

**Паттерны**:
- Repository Pattern для данных
- StateFlow для реактивности
- Koin для DI
- Kotlin Multiplatform (expect/actual)

## Сжатие контекста

Функциональность иерархического сжатия контекста:
1. `ChatSettings` содержит настройки: `contextCompressionEnabled`, `compressionThreshold`, `recentMessagesCount`
2. `ContextCompressionService` выполняет AI-суммаризацию через GigaChat API
3. `ChatRepositoryImpl` управляет `_summaries` (Flow) и применяет сжатие
4. `SendMessageResult` возвращает количество сжатых сообщений для обновления UI
5. `SummaryBubble` отображает резюме в чате

## Команды сборки

```bash
./gradlew assembleDebug    # Debug APK
./gradlew assembleRelease  # Release APK
```

## Конфигурация API

Файл `local.properties`:
```
gigachat.api.host=https://gigachat.devices.sberbank.ru/
gigachat.auth.host=https://ngw.devices.sberbank.ru/api/v2/oauth
gigachat.authorization.key=YOUR_KEY
```
