import SwiftUI

enum ContentState<Value> {
    case loading, empty, content(Value), failure
}

struct StateView<Content: View>: View {
    let state: ContentState<Content>
    var retry: (() -> Void)?
    var body: some View {
        switch state {
        case .loading: ProgressView(.commonLoading)
        case .empty: ContentUnavailableView(.commonNothingHereYet, systemImage: "tray")
        case .content(let content): content
        case .failure:
            ContentUnavailableView {
                Label(.commonCouldNotLoadContent, systemImage: "wifi.exclamationmark")
            } actions: {
                if let retry { Button(.commonRetry, action: retry) }
            }
        }
    }
}
