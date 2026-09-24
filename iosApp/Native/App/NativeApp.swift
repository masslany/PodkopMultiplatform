import SwiftUI
import PodkopShared

@main
struct PodkopNativeApp: App {
    private let client = PodkopClient.companion.create()
    private let adapter = BridgeAdapter()

    var body: some Scene {
        WindowGroup {
            NativeRoot(client: client, adapter: adapter)
        }
    }
}

private struct NativeRoot: View {
    let client: PodkopClient
    let adapter: BridgeAdapter
    @State private var phase = "initializing"
    @State private var message: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Podkop Native")
                .font(.title)
            Text(phase)
            if let message { Text(message) }
            Button("Retry") {
                Task {
                    do {
                        let _: IOSSuccess = try await adapter.call { client.startup.retry(completion: $0) }
                    } catch {
                        message = "Startup failed"
                    }
                }
            }
        }
        .task {
            let stream = adapter.stream { client.startup.observe(onChange: $0) }
            for await state in stream { phase = state.phase }
        }
        .task {
            guard let key = Bundle.main.object(forInfoDictionaryKey: "WYKOP_KEY") as? String,
                  let secret = Bundle.main.object(forInfoDictionaryKey: "WYKOP_SECRET") as? String,
                  !key.isEmpty, !secret.isEmpty,
                  !key.hasPrefix("$("), !secret.hasPrefix("$(") else {
                phase = "configuration error"
                return
            }
            do {
                let _: IOSSuccess = try await adapter.call { client.startup.start(key: key, secret: secret, completion: $0) }
            } catch {
                message = "Startup failed"
            }
        }
    }
}
