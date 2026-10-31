import Shared

class SwiftUIReleaseNotesScreenFactory: ReleaseNotesNativeScreenFactory {
    func create(onEvent: @escaping (ReleaseNotesUiEvent) -> Void) -> any ReleaseNotesNativeScreen {
        SwiftUIReleaseNotesScreen(onEvent: onEvent)
    }
}
