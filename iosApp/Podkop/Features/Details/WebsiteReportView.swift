import SwiftUI

/// A public website handoff; app tokens are never passed to the browser.
struct WebsiteReportView: View {
    let contentURL: URL
    let missingCommentID: Int?
    @Environment(\.dismiss) private var dismiss
    @State private var website: ReportWebsite?

    var body: some View {
        NavigationStack {
            WykopList {
                Section {
                    Text(.reportInstructions)
                    if let missingCommentID {
                        Text(.reportMissingComment)
                        Text(verbatim: String(missingCommentID)).textSelection(.enabled)
                    }
                    Button(.reportOpenContent) { website = ReportWebsite(url: contentURL) }
                }
                Section {
                    Text(.reportFallback)
                    Button(.detailsCopyLink) { UIPasteboard.general.url = contentURL }
                    Button(.reportContact) {
                        website = ReportWebsite(url: URL(string: "https://wykop.pl/kontakt")!)
                    }
                }
            }
            .navigationTitle(.reportTitle)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button(.detailsDone) { dismiss() }
                }
            }
            .sheet(item: $website) { SafariView(url: $0.url).ignoresSafeArea() }
        }
        .accessibilityIdentifier("websiteReport")
    }
}

private struct ReportWebsite: Identifiable {
    let url: URL
    var id: URL { url }
}
