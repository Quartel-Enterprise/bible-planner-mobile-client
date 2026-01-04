import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let remoteConfigService: RemoteConfigService

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(remoteConfigService: remoteConfigService)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let remoteConfigService: RemoteConfigService

    var body: some View {
        ComposeView(remoteConfigService: remoteConfigService)
            .ignoresSafeArea()
    }
}
