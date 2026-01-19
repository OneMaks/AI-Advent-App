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