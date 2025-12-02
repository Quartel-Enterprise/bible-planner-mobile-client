# Bible Planner

A Kotlin Multiplatform application for planning and tracking your Bible reading progress. Bible Planner helps you stay organized with structured reading plans, progress tracking, and a beautiful, customizable interface.

## 📱 Features

- **Reading Plans**: Choose from multiple reading plan types including:
  - Books Order: Read through the Bible in book order
  - Chronological Order: Read through the Bible in chronological order
- **Progress Tracking**: Track your reading progress at the book, chapter, and verse level
- **Weekly Organization**: 52-week reading plans with weekly breakdowns
- **Theme Customization**: 
  - Light and dark themes
  - Material You dynamic colors support
  - Custom theme selection
- **Multi-platform Support**: Runs on Android, iOS, and Desktop (JVM)
- **Local Storage**: All data is stored locally on your device for privacy
- **Offline First**: No internet connection required

## 🏗️ Architecture

This project follows a modular architecture with clear separation of concerns:

- **Core Modules**: Shared business logic and data models
  - `core/model`: Domain models
  - `core/books`: Bible books data and management
  - `core/plan`: Reading plan data and logic
  - `core/navigation`: Navigation setup
  - `core/provider`: Dependency injection (Koin), database (Room), and data storage (DataStore)
  - `core/utils`: Utility functions

- **Feature Modules**: Feature-specific implementations
  - `feature/reading_plan`: Reading plan selection and display
  - `feature/day`: Daily reading tracking
  - `feature/theme_selection`: Theme customization
  - `feature/material_you`: Material You integration
  - `feature/delete_progress`: Progress deletion functionality

- **UI Modules**: Shared UI components and theming
  - `ui/theme`: Theme configuration
  - `ui/component`: Reusable UI components
  - `ui/utils`: UI utility functions

## 🛠️ Tech Stack

- **Kotlin Multiplatform**: Shared codebase across platforms
- **Compose Multiplatform**: Modern declarative UI framework
- **Room**: Local database for reading progress
- **DataStore**: Preferences storage
- **Koin**: Dependency injection
- **Navigation Compose**: Navigation between screens
- **Coroutines**: Asynchronous programming
- **Kotlin Serialization**: JSON parsing for reading plans

## 📁 Project Structure

This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./composeApp/src/commonMain/kotlin) is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
    folder is the appropriate location.

* [/iosApp](./iosApp/iosApp) contains iOS applications. Even if you're sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

## 🚀 Getting Started

### Prerequisites

- JDK 21 or higher
- Android Studio or IntelliJ IDEA
- For iOS development: Xcode (macOS only)
- For Android development: Android SDK

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE's toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

## 📝 Code Quality

This project uses ktlint for code formatting and style checking. The project is configured to automatically check code style during builds.

## 📄 License

This project is licensed under the MIT License. See [LICENSE.md](./LICENSE.md) for details.

## 📚 Learn More

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
