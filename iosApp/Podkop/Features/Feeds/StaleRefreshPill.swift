import SwiftUI

/// Android's `StaleRefreshPill`: a floating "Odśwież" capsule over a feed that has not been
/// opened for a while.
struct StaleRefreshPill: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Label(.commonRefreshButton, systemImage: "arrow.clockwise")
                .font(.subheadline.weight(.semibold))
                .padding(.horizontal, 18)
                .padding(.vertical, 10)
        }
        .buttonStyle(.plain)
        .foregroundStyle(PodkopTheme.background)
        .background(Color.primary, in: Capsule())
        .shadow(color: .black.opacity(0.2), radius: 8, y: 2)
        .accessibilityIdentifier("staleRefresh")
    }
}
