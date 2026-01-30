# Podkop

Podkop is a Kotlin Multiplatform (KMP) client for Wykop.pl, built with modern Android development practices and Compose Multiplatform.

## 🏗 Project Structure

The project is divided into several modules to ensure a clean separation of concerns and maximize code sharing:

- **`:business`**: The core logic of the application. It contains the domain models, repositories, and data sources (networking with Ktor, serialization). This is a pure Kotlin Multiplatform module.
  - `di`: Koin modules for dependency injection.
  - `auth`: Authentication logic.
  - `links` & `hits`: Core functionality for browsing content.
- **`:composeApp`**: Shared UI module using Compose Multiplatform. It contains the ViewModels, screens, and navigation logic that are shared between Android and iOS.
- **`:common`**: Shared utilities, design system components, and base classes used by other modules.
- **`:androidApp`**: The Android-specific entry point and configuration.
- **`:iosApp`**: The iOS-specific entry point (SwiftUI wrapper).

## 🚀 Tech Stack

- **UI**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- **Dependency Injection**: [Koin](https://insert-koin.io/)
- **Networking**: [Ktor](https://ktor.io/)
- **Navigation**: [Jetpack Navigation 3](https://developer.android.com/jetpack/compose/navigation)
- **Concurrency**: Kotlin Coroutines & Flow
- **Data Collections**: Kotlinx Immutable Collections
- **Architecture**: MVVM with a focus on Unidirectional Data Flow (UDF)

## 🔑 Configuration & API Keys

The app interacts with the Wykop API and requires API keys to function.

### Android
1. Create a file named `apikeys.properties` in the root directory of the project.
2. Add your Wykop API credentials:
   ```properties
   WYKOP_KEY=your_api_key_here
   WYKOP_SECRET=your_api_secret_here
   ```

### iOS
1. Create a file named `ApiKeys.xcconfig` in the `iosApp/Configuration` directory.
2. Add your Wykop API credentials:
   ```properties
   WYKOP_KEY=your_api_key_here
   WYKOP_SECRET=your_api_secret_here
   ```

## 🛠 Building the Project

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

### iOS
1. Open the `iosApp/iosApp.xcworkspace` in Xcode.
2. Select your target device/simulator and click Run.
Note: You can also run the iOS app directly from Android Studio if you have the [Kotlin Multiplatform plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) installed.
