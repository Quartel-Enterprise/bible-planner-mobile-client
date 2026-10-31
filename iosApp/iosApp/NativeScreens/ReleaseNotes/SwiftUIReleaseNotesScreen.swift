import Observation
import Shared
import SwiftUI
import UIKit

@Observable
final class ReleaseNotesScreenModel {
    var state: ReleaseNotesNativeState?
}

/// Hosts the SwiftUI screen; Compose pushes every new state through `render`.
final class SwiftUIReleaseNotesScreen: ReleaseNotesNativeScreen {
    let viewController: UIViewController
    private let navigationController: UINavigationController
    private let model = ReleaseNotesScreenModel()

    init(onEvent: @escaping (ReleaseNotesUiEvent) -> Void) {
        let hostingController = UIHostingController(rootView: ReleaseNotesView(model: model, onEvent: onEvent))
        hostingController.view.backgroundColor = .clear
        // A SwiftUI NavigationStack inside the Compose interop view gets a zero leading layout margin,
        // which pins the large title to the screen edge; a UIKit navigation controller does not.
        navigationController = UINavigationController(rootViewController: hostingController)
        navigationController.navigationBar.prefersLargeTitles = true
        viewController = navigationController
    }

    func render(state: ReleaseNotesNativeState) {
        guard model.state != state else { return }
        model.state = state
        let titleColor = UIColor(Color(kotlinArgb: state.colors.onBackgroundArgb))
        let titleAttributes = [NSAttributedString.Key.foregroundColor: titleColor]
        navigationController.navigationBar.titleTextAttributes = titleAttributes
        navigationController.navigationBar.largeTitleTextAttributes = titleAttributes
    }
}
