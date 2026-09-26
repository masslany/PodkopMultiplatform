import SwiftUI

/// A `List` on the app's page background with card-colored rows, so list and form screens match
/// the feeds and More instead of the system grouped background (black in dark mode).
struct WykopList<Content: View>: View {
    @ViewBuilder var content: () -> Content

    var body: some View {
        List {
            Group { content() }
                .listRowBackground(WykopTheme.card)
        }
        .scrollContentBackground(.hidden)
        .background(WykopTheme.background.ignoresSafeArea())
    }
}
