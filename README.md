# Bible Planner

A Kotlin Multiplatform application for planning and tracking your Bible reading progress. Bible Planner helps you stay organized with structured reading plans, progress tracking, and a beautiful, customizable interface.

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
  ./gradlew :androidApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :androidApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :desktopApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :desktopApp:run
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE's toolbar or open the [/iosApp](./iosApp) directory in Xcode and run it from there.

## Payment Integration

- [RevenueCat Setup](docs/setup_revenuecat.md)

## Authentication

- [Apple Sign-In Setup](docs/setup_apple_signin.md)

## 📝 Code Quality

This project uses ktlint for code formatting and style checking. The project is configured to automatically check code style during builds.

## 🛠️ Contributing Notes

- [Commit timestamps and the "next day" GitHub grouping](docs/commit-timezones.md)

## 📄 License

This project is licensed under the MIT License. See [LICENSE.md](./LICENSE.md) for details.

## 📚 Learn More

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
