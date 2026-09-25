import SwiftUI
import UIKit

/// Save, copy and share controls for image bytes; feedback goes to a caller-provided message.
struct ImageExportControls: View {
    enum Style { case standard, overlay }
    let data: Data
    var style: Style = .standard
    let onMessage: (String) -> Void
    @State private var sharing = false

    var body: some View {
        switch style {
        case .standard:
            buttons.buttonStyle(.bordered)
        case .overlay:
            // Over photos: icon buttons with titles on a dark capsule, legible on any image.
            buttons
                .buttonStyle(OverlayButtonStyle())
                .padding(.horizontal, 8).padding(.vertical, 6)
                .background(.black.opacity(0.6), in: Capsule())
                .overlay(Capsule().strokeBorder(.white.opacity(0.2)))
        }
    }

    private var buttons: some View {
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
        .sheet(isPresented: $sharing) { ImageShareSheet(data: data, onMessage: onMessage) }
    }
}

private struct OverlayButtonStyle: ButtonStyle {
    @Environment(\.isEnabled) private var isEnabled

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .labelStyle(OverlayLabelStyle())
            .foregroundStyle(.white.opacity(isEnabled ? 1 : 0.4))
            .frame(minWidth: 64, minHeight: 48)
            .background(.white.opacity(configuration.isPressed ? 0.2 : 0), in: Capsule())
            .contentShape(Capsule())
    }
}

private struct OverlayLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        VStack(spacing: 3) {
            configuration.icon.font(.system(size: 20, weight: .medium))
            configuration.title.font(.caption2.weight(.semibold))
        }
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
