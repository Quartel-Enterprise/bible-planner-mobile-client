import Observation
import Shared
import SwiftUI
import UIKit

/// Keeps the reading plan's ViewModel for the screen's lifetime and follows its state and actions.
@Observable
final class ReadingPlanScreenModel {
    private(set) var state: (any ReadingPlanUiState)? = nil
    var isShowingNoProgressAlert = false

    @ObservationIgnored private var holder: ViewModelHolder<ReadingPlanViewModel>?
    @ObservationIgnored private var watches: [any Watch] = []

    init() {
        // Koin only fails here on a wiring bug; the screen then stays on its loading state instead of crashing.
        guard let holder = try? NativeViewModels.shared.createReadingPlan() else { return }
        self.holder = holder
        let viewModel = holder.viewModel
        watches = [
            viewModel.stateWatcher.observe { [weak self] state in
                self?.state = state
            },
            viewModel.actionWatcher.observe { [weak self] action in
                self?.handle(action)
            },
        ]
    }

    func send(_ event: ReadingPlanUiEvent) {
        holder?.viewModel.onEvent(event: event)
    }

    /// Ends the screen: stops watching and clears the ViewModel, which cancels its work.
    func clear() {
        watches.forEach { $0.cancel() }
        watches = []
        holder?.clear()
        holder = nil
    }

    private func handle(_ action: ReadingPlanUiAction) {
        if action is ReadingPlanUiActionShowNoProgressToDelete {
            isShowingNoProgressAlert = true
        } else if let openLink = action as? ReadingPlanUiActionOpenLink, let url = URL(string: openLink.url) {
            UIApplication.shared.open(url)
        }
    }
}

/// Hosts the SwiftUI reading plan inside the shared Plans tab; Compose disposes it when the tab goes away for good.
final class SwiftUIReadingPlanScreen: NativeScreen {
    let viewController: UIViewController
    private let model: ReadingPlanScreenModel

    init() {
        let model = ReadingPlanScreenModel()
        self.model = model
        let hostingController = UIHostingController(rootView: ReadingPlanView(model: model))
        // A SwiftUI NavigationStack inside the Compose interop view gets a zero leading layout margin,
        // which pins the large title to the screen edge; a UIKit navigation controller does not.
        let navigationController = UINavigationController(rootViewController: hostingController)
        navigationController.navigationBar.prefersLargeTitles = true
        viewController = navigationController
    }

    func dispose() {
        model.clear()
    }
}
