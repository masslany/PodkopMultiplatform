import SwiftUI

/// Blurs adult content behind Wykop's "+18" notice until the reader taps it (Android's adult
/// image overlay). The hidden content is removed from accessibility until revealed.
struct AdultContentGate<Content: View>: View {
    let hidden: Bool
    let reveal: () -> Void
    @ViewBuilder var content: () -> Content

    var body: some View {
        if hidden {
            // A ZStack rather than an overlay: short content grows to fit the notice instead of
            // clipping it, even at large text sizes.
            ZStack {
                content()
                    .blur(radius: 22, opaque: true)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .allowsHitTesting(false)
                    .accessibilityHidden(true)
                    Button(action: reveal) {
                        VStack(spacing: 8) {
                            Text(verbatim: "18+")
                                .font(.headline.weight(.heavy))
                                .foregroundStyle(.white)
                                .padding(.horizontal, 10).padding(.vertical, 4)
                                .background(WykopTheme.adultRed, in: Capsule())
                            Text(.contentContentAdultsOnlyTap)
                                .font(.subheadline.weight(.semibold))
                                .multilineTextAlignment(.center)
                                .foregroundStyle(.primary)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, minHeight: 120, maxHeight: .infinity)
                        .background(.ultraThinMaterial,
                                    in: RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(.commonShowAdultContent)
                    .accessibilityHint(.contentRevealsSensitiveContent)
            }
            .frame(minHeight: 120)
            .clipShape(RoundedRectangle(cornerRadius: WykopTheme.smallRadius, style: .continuous))
        } else {
            content()
        }
    }
}
