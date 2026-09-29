import SwiftUI

/// iOS only: the community rules shown once before any content, as App Store guideline 1.2
/// requires apps with user-generated content to have users agree to zero-tolerance terms.
struct ContentTermsView: View {
    /// Raise when the rules change so everyone agrees to the new version.
    static let version = 1
    static let storageKey = "contentTermsAcceptedVersion"

    let accept: () -> Void

    var body: some View {
        NavigationStack {
            PodkopList {
                Section { Text(.termsIntro) }
                Section(.termsZeroToleranceTitle) {
                    Text(.termsZeroToleranceBody)
                    Link(.termsWykopTerms, destination: URL(string: "https://wykop.pl/regulamin")!)
                }
                Section(.termsReportTitle) { Text(.termsReportBody) }
                Section(.termsAdultTitle) { Text(.termsAdultBody) }
            }
            .navigationTitle(.termsTitle)
            .safeAreaInset(edge: .bottom) {
                Button(action: accept) {
                    Text(.termsAccept).frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .buttonBorderShape(.capsule)
                .tint(.primary)
                .foregroundStyle(PodkopTheme.background)
                .controlSize(.large)
                .padding()
                .background(PodkopTheme.background)
                .accessibilityIdentifier("acceptContentTerms")
            }
        }
        .accessibilityIdentifier("contentTerms")
    }
}
