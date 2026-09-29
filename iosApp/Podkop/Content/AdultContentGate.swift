import SwiftUI

/// Blurs adult content behind Wykop's "+18" notice until the reader taps it (Android's adult
/// image overlay). The hidden content is removed from accessibility until revealed.
///
/// iOS only: App Store guideline 1.2 allows adult content only after the user turned it on via
/// the website, so without Wykop's "+18" account setting the content is never rendered and the
/// notice explains where to change it instead of offering a reveal.
struct AdultContentGate<Content: View>: View {
    let hidden: Bool
    let reveal: () -> Void
    @ViewBuilder var content: () -> Content
    @Environment(\.adultContentAllowed) private var adultContentAllowed

    var body: some View {
        if hidden && !adultContentAllowed {
            VStack(spacing: 8) {
                Text(verbatim: "18+")
                    .font(.headline.weight(.heavy))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 10).padding(.vertical, 4)
                    .background(PodkopTheme.adultRed, in: Capsule())
                Text(.contentAdultContentTurnedOff)
                    .font(.subheadline)
                    .multilineTextAlignment(.center)
                    .foregroundStyle(.secondary)
            }
            .padding()
            .frame(maxWidth: .infinity, minHeight: 120)
            .background(PodkopTheme.cardInset,
                        in: RoundedRectangle(cornerRadius: PodkopTheme.smallRadius, style: .continuous))
            .accessibilityElement(children: .combine)
            .accessibilityIdentifier("adultContentTurnedOff")
        } else if hidden {
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
                                .background(PodkopTheme.adultRed, in: Capsule())
                            Text(.contentContentAdultsOnlyTap)
                                .font(.subheadline.weight(.semibold))
                                .multilineTextAlignment(.center)
                                .foregroundStyle(.primary)
                        }
                        .padding()
                        .frame(maxWidth: .infinity, minHeight: 120, maxHeight: .infinity)
                        .background(.ultraThinMaterial,
                                    in: RoundedRectangle(cornerRadius: PodkopTheme.smallRadius, style: .continuous))
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(.commonShowAdultContent)
                    .accessibilityHint(.contentRevealsSensitiveContent)
            }
            .frame(minHeight: 120)
            .clipShape(RoundedRectangle(cornerRadius: PodkopTheme.smallRadius, style: .continuous))
        } else {
            content()
        }
    }
}

private struct AdultContentAllowedKey: EnvironmentKey {
    static let defaultValue = false
}

extension EnvironmentValues {
    /// Whether the signed-in Wykop account has "+18" content turned on (see `SessionModel`).
    var adultContentAllowed: Bool {
        get { self[AdultContentAllowedKey.self] }
        set { self[AdultContentAllowedKey.self] = newValue }
    }
}
