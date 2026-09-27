import SwiftUI

/// Sign-in prompt that continues into the embedded Wykop Connect page.
struct LoginSheet: View {
    let session: SessionModel
    let dismiss: () -> Void
    @State private var loading = false
    @State private var failed = false

    var body: some View {
        NavigationStack {
            Group {
                if let url = session.loginURL {
                    LoginWebView(
                        url: url,
                        isCallback: session.isAppURL,
                        onCallback: { callback in Task { await session.completeLogin(callback) } },
                        onLoadingChange: { loading = $0 },
                        onFailure: { failed = true }
                    )
                    .overlay {
                        if failed {
                            ContentUnavailableView {
                                Label(.appCouldNotLoadSignInPage, systemImage: "wifi.exclamationmark")
                            } actions: {
                                Button(.commonRetry) {
                                    failed = false
                                    session.loginURL = nil
                                    Task { await session.beginLogin() }
                                }
                            }
                            .background(Color(uiColor: .systemBackground))
                        } else if loading {
                            ProgressView()
                        }
                    }
                    .ignoresSafeArea(edges: .bottom)
                } else {
                    VStack(spacing: 20) {
                        Text(.appSignInContinue).font(.title2)
                        Button {
                            Task { await session.beginLogin() }
                        } label: {
                            if session.loginPending { ProgressView() } else { Text(.commonSignIn) }
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(session.loginPending)
                        .accessibilityIdentifier("loginStart")
                    }
                    .padding()
                }
            }
            .navigationTitle(session.loginURL == nil ? "" : String(localized: .commonSignIn))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(.commonCancel, action: dismiss)
                }
            }
        }
        .presentationDetents(session.loginURL == nil ? [.medium] : [.large])
        .onDisappear { session.loginURL = nil }
    }
}
