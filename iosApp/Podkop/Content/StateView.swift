import SwiftUI

enum ContentState<Value> {
    case loading, empty, content(Value), failure
}

struct StateView<Content: View>: View {
    let state: ContentState<Content>
    var retry: (() -> Void)?
    var body: some View {
        switch state {
        case .loading: ProgressView("Loading…")
        case .empty: ContentUnavailableView("Nothing here yet", systemImage: "tray")
        case .content(let content): content
        case .failure:
            ContentUnavailableView {
                Label("Could not load content", systemImage: "wifi.exclamationmark")
            } actions: {
                if let retry { Button("Retry", action: retry) }
            }
        }
    }
}
