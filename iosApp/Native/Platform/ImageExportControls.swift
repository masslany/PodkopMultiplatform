import SwiftUI
import UIKit

/// Save, copy and share controls for image bytes; feedback goes to a caller-provided message.
struct ImageExportControls: View {
    let data: Data
    let onMessage: (String) -> Void
    @State private var sharing = false

    var body: some View {
        HStack(spacing: 12) {
            Button {
                Task {
                    switch await PlatformExport.saveToPhotos(data) {
                    case .saved: onMessage(String(localized: "Saved to Photos"))
                    case .denied: onMessage(String(localized: "Allow adding photos in Settings to save images."))
                    case .failed: onMessage(String(localized: "Could not complete this action. Try again."))
                    }
                }
            } label: {
                Label("Save", systemImage: "square.and.arrow.down")
            }
            .accessibilityIdentifier("imageSave")
            Button {
                onMessage(PlatformExport.copyImage(data)
                          ? String(localized: "Image copied")
                          : String(localized: "Could not complete this action. Try again."))
            } label: {
                Label("Copy", systemImage: "doc.on.doc")
            }
            .accessibilityIdentifier("imageCopy")
            Button {
                sharing = true
            } label: {
                Label("Share", systemImage: "square.and.arrow.up")
            }
            .accessibilityIdentifier("imageShare")
            .disabled(PlatformExport.fileExtension(for: data) == nil)
        }
        .buttonStyle(.bordered)
        .sheet(isPresented: $sharing) { ImageShareSheet(data: data, onMessage: onMessage) }
    }
}

/// The activity controller receives a real file with the source format. Cleanup happens when
/// the activity completes or is cancelled, including when the sheet is dismissed on iPad.
private struct ImageShareSheet: UIViewControllerRepresentable {
    let data: Data
    let onMessage: (String) -> Void

    func makeUIViewController(context: Context) -> UIActivityViewController {
        guard let ext = PlatformExport.fileExtension(for: data) else {
            return UIActivityViewController(activityItems: [], applicationActivities: nil)
        }
        let directory = FileManager.default.temporaryDirectory.appendingPathComponent(UUID().uuidString,
                                                                                        isDirectory: true)
        do {
            try FileManager.default.createDirectory(at: directory, withIntermediateDirectories: true)
            let file = directory.appendingPathComponent("podkop.\(ext)")
            try data.write(to: file, options: .atomic)
            let controller = UIActivityViewController(activityItems: [file], applicationActivities: nil)
            controller.completionWithItemsHandler = { _, _, _, error in
                try? FileManager.default.removeItem(at: directory)
                if error != nil {
                    Task { @MainActor in
                        onMessage(String(localized: "Could not complete this action. Try again."))
                    }
                }
            }
            return controller
        } catch {
            try? FileManager.default.removeItem(at: directory)
            onMessage(String(localized: "Could not complete this action. Try again."))
            return UIActivityViewController(activityItems: [], applicationActivities: nil)
        }
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
