import SwiftUI

struct CollectionScroll<Controls: View, Rows: View>: View {
    let refresh: () async -> Void
    @ViewBuilder let controls: Controls
    @ViewBuilder let rows: Rows

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                controls.padding(.top, 8)
                rows
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 20)
            .frame(maxWidth: 900)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await refresh() }
    }
}
