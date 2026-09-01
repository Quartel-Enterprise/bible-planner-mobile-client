# Bible Planner

A Compose Multiplatform application for planning and tracking your Bible reading progress. [Bible Planner](https://www.bibleplanner.app/) helps you stay organized with structured reading plans, progress tracking, and a beautiful, customizable interface.

Android | iOS | Web | Desktop |
| -- | -- | -- | -- |
| [Google Play Store](https://play.google.com/store/apps/details?id=com.quare.bibleplanner&hl=en) | [App Store](https://apps.apple.com/us/app/bible-planner-reading-plans/id6756151777) | Not available yet | Available just for development at the moment |

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

This project uses ktlint for code formatting and style checking, extended with the custom rules in
`tools/ktlint-custom-rules` (documented in [Code Style](docs/architecture/code-style.md)).

CI runs the ktlint CLI directly, and `scripts/ktlint.sh` runs exactly the same check locally:

```bash
./scripts/ktlint.sh            # check (what CI runs)
./scripts/ktlint.sh --format   # autocorrect what can be autocorrected
```

The script downloads the ktlint CLI once (cached in `~/.cache/ktlint`) and rebuilds the custom
ruleset jar only when its sources change. `./gradlew ktlintCheck` still works but is much slower,
because it drags KSP and Kotlin/Native compilation into the task graph.

## 🖼️ Store Listing Screenshots

The Play and App Store screenshots are generated from the real screens, never captured by hand.
`./gradlew collectStoreScreenshots` rebuilds all of them into `store_listings/`; see
[Store listing screenshots](docs/store-listing-screenshots.md) for the layout, what each image
shows and how they reach the stores.

## 📄 License

This project is licensed under the MIT License. See [LICENSE.md](./LICENSE.md) for details.

## 📚 Learn More

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
