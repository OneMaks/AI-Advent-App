This is a Kotlin Multiplatform project targeting Android.

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

### Configuration

Before building the app, you need to configure the GigaChat API credentials in `local.properties`.

1. Open (or create) `local.properties` in the project root directory
2. Add the following configuration:

```properties
# GigaChat API Configuration
AI_API_HOST=https://gigachat.devices.sberbank.ru
AI_AUTH_HOST=https://ngw.devices.sberbank.ru:9443
AI_AUTH_KEY=your_authorization_key_here
AI_MODEL=GigaChat
AI_SCOPE=GIGACHAT_API_PERS
```

**Getting your Authorization Key:**
1. Register at [developers.sber.ru](https://developers.sber.ru/)
2. Create a new project and get access to GigaChat API
3. Copy the Authorization Key (Base64-encoded) from the developer portal
4. Paste it as `AI_AUTH_KEY` value

**Available scopes:**
- `GIGACHAT_API_PERS` - for individuals (free tier)
- `GIGACHAT_API_B2B` - for businesses with paid packages
- `GIGACHAT_API_CORP` - for businesses with pay-as-you-go pricing

> **Note:** `local.properties` is gitignored and should never be committed to version control.

---

### Token Usage Indicator

The app displays token usage information for each assistant response in the format:

```
Расчёт: 45 → 120 (165)
Факт: 52 → 134 (186)
```

**Format:** `prompt tokens → completion tokens (total)`

| Line | Description |
|------|-------------|
| **Расчёт** (Estimated) | Locally calculated token estimate using heuristics |
| **Факт** (Actual) | Real token counts from API response |

**Estimation Algorithm (`TokenEstimator`):**

The estimator uses character-based heuristics with different weights:

| Character Type | Weight | Approx. chars per token |
|----------------|--------|-------------------------|
| Latin letters/digits | 0.25 | ~4 |
| Cyrillic (Russian) | 0.5 | ~2 |
| CJK (Chinese) | 1.0 | ~1 |
| Whitespace | 0.25 | ~4 |
| Punctuation | 0.5 | ~2 |

**Example:** "Привет мир" (10 Cyrillic chars + 1 space):
- 10 × 0.5 = 5 (Cyrillic)
- 1 × 0.25 = 0.25 (space)
- 2 × 0.1 = 0.2 (word boundaries)
- **Total: ≈ 5 tokens**

Comparing estimated vs actual values helps understand real API consumption and evaluate estimation accuracy.

---

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…