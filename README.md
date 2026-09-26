# Podkop

Podkop is a Kotlin Multiplatform (KMP) client for Wykop.pl, built with modern Android development practices and Compose Multiplatform.

The iOS app is written in SwiftUI on top of the shared Kotlin modules; Android uses Compose.

## Project Structure

The project is divided into several modules to ensure a clean separation of concerns and maximize code sharing:

- **`:business`**: The core logic of the application. It contains the domain models, repositories, and data sources (networking with Ktor, serialization). This is a pure Kotlin Multiplatform module.
- **`:composeApp`**: The Android UI module using Compose. It contains the ViewModels, screens, and navigation logic.
- **`:common`**: Shared utilities, design system components, and base classes used by other modules.
- **`:androidApp`**: The Android-specific entry point and configuration.
- **`:iosShared`**: The Kotlin facade the iOS app calls; it exports the `PodkopShared` framework.
- **`iosApp/`**: The SwiftUI iOS app (`Podkop` target) with its unit (`PodkopTests`) and UI (`PodkopUITests`) tests. Swift files are grouped by feature under `iosApp/Podkop`; Xcode picks up new files in those folders automatically.

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/compose) on Android, SwiftUI on iOS
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Networking**: [Ktor](https://ktor.io/)
- **Navigation**: [Jetpack Navigation 3](https://developer.android.com/jetpack/compose/navigation)
- **Concurrency**: Kotlin Coroutines & Flow
- **Data Collections**: Kotlinx Immutable Collections
- **Architecture**: MVVM with a focus on Unidirectional Data Flow (UDF)

## Configuration & API Keys

The app interacts with the Wykop API and uses Firebase on Android. Some local configuration files are intentionally not stored in the repository and must be provided before the app can run.

### Android
1. Create a file named `apikeys.properties` in the root directory of the project.
2. Add your Wykop API credentials:
   ```properties
   WYKOP_KEY=your_api_key_here
   WYKOP_SECRET=your_api_secret_here
   ```
3. Add Firebase config files for the Android app:
   - `androidApp/src/debug/google-services.json`
   - `androidApp/src/release/google-services.json`
4. These files are gitignored, so you need to obtain them from the Firebase project and place them locally.

### iOS
1. Create a file named `ApiKeys.xcconfig` in the `iosApp/Configuration` directory.
2. Add your Wykop API credentials:
   ```properties
   WYKOP_KEY=your_api_key_here
   WYKOP_SECRET=your_api_secret_here
   ```
3. This file is gitignored and must be created locally.

### Summary of local-only files

- `apikeys.properties`
- `iosApp/Configuration/ApiKeys.xcconfig`
- `androidApp/src/debug/google-services.json`
- `androidApp/src/release/google-services.json`

## Building the Project

### Prerequisites
- Android Studio (latest stable or Bumblebee+) or IntelliJ IDEA.
- JDK 17 or higher.
- Xcode (for running the iOS application).

### Android
To build and run the Android app:
```bash
./gradlew :androidApp:assembleDebug
```
Or simply use the `androidApp` run configuration in Android Studio.

If Firebase config files are missing, the Android build will fail during Google Services processing.

### iOS
1. Open `iosApp/iosApp.xcodeproj` in Xcode.
2. Select the `Podkop` scheme and your device or simulator, then click Run. The build compiles the shared Kotlin framework first.


Unit tests that verify session persistence need a signed simulator host for Keychain access.
Run all unit tests (without UI tests) with a local signature and execution deadlines:

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme Podkop \
  -destination 'platform=iOS Simulator,name=iPhone 16e' \
  -only-testing:PodkopTests -parallel-testing-enabled NO \
  -test-timeouts-enabled YES -default-test-execution-time-allowance 30 \
  -maximum-test-execution-time-allowance 30 \
  CODE_SIGNING_ALLOWED=YES CODE_SIGN_IDENTITY=- test
```

`SessionPersistenceTests` also has a five-second callback deadline and owns its shared
client and adapter. It must not use `AppDependencies.shared`: other bridge tests close
the singleton client, leaving the host app's cached client reference closed.
