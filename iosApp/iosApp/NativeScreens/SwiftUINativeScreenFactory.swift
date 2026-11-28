import Shared

/// Builds the screens Kotlin draws in SwiftUI; each one owns its ViewModel through a `ViewModelHolder`.
final class SwiftUINativeScreenFactory: NativeScreenFactory {
    func createReadingPlanScreen() -> any NativeScreen {
        SwiftUIReadingPlanScreen()
    }
}
