import SwiftUI

/// A `List` on the app's page background with card-colored rows, so list and form screens match
/// the feeds and More instead of the system grouped background (black in dark mode).
struct PodkopList<Content: View>: View {
    @ViewBuilder var content: () -> Content

    var body: some View {
        List {
            Group { content() }
                .listRowBackground(PodkopTheme.card)
        }
        .scrollContentBackground(.hidden)
        .background(PodkopTheme.background.ignoresSafeArea())
    }
}
