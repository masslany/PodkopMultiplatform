import SwiftUI
import UIKit

/// Save, copy and share controls for image bytes; feedback goes to a caller-provided message.
struct ImageExportControls: View {
    enum Style { case standard, overlay, screenshot }
    let data: Data
    var style: Style = .standard
    let onMessage: (String) -> Void
    @State private var sharing = false

    var body: some View {
        Group {
            switch style {
            case .standard:
                buttons.buttonStyle(.bordered)
            case .overlay:
                buttons
                    .buttonStyle(OverlayButtonStyle())
                    .padding(.horizontal, 8).padding(.vertical, 6)
                    .background(.black.opacity(0.6), in: Capsule())
                    .overlay(Capsule().strokeBorder(.white.opacity(0.2)))
            case .screenshot:
                VStack(spacing: 12) {
                    shareButton.buttonStyle(ScreenshotExportButtonStyle(prominent: true))
                    HStack(spacing: 12) {
                        copyButton
                        saveButton
                    }
                    .buttonStyle(ScreenshotExportButtonStyle(prominent: false))
                }
            }
        }
        .sheet(isPresented: $sharing) { ImageShareSheet(data: data, onMessage: onMessage) }
    }

    private var buttons: some View {
        HStack(spacing: 12) { saveButton; copyButton; shareButton }
    }

    private var saveButton: some View {
        Button {
            Task {
                switch await PlatformExport.saveToPhotos(data) {
                case .saved: onMessage(String(localized: .platformSavedToPhotos))
                case .denied: onMessage(String(localized: .platformAllowAddingPhotos))
                case .failed: onMessage(String(localized: .commonCouldNotCompleteAction))
                }
            }
        } label: {
            Label(.commonSave, systemImage: "square.and.arrow.down")
        }
        .accessibilityIdentifier("imageSave")
    }

    private var copyButton: some View {
        Button {
            onMessage(PlatformExport.copyImage(data)
                      ? String(localized: .platformImageCopied)
                      : String(localized: .commonCouldNotCompleteAction))
        } label: {
            Label(.platformCopy, systemImage: "doc.on.doc")
        }
        .accessibilityIdentifier("imageCopy")
    }

    private var shareButton: some View {
        Button { sharing = true } label: {
            Label(.platformShare, systemImage: "square.and.arrow.up")
        }
        .accessibilityIdentifier("imageShare")
        .disabled(PlatformExport.fileExtension(for: data) == nil)
    }
}

private struct ScreenshotExportButtonStyle: ButtonStyle {
    let prominent: Bool

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.subheadline.weight(.semibold))
            .frame(maxWidth: .infinity, minHeight: 48)
            .foregroundStyle(prominent ? PodkopTheme.background : Color.primary)
            .background(prominent ? Color.primary : PodkopTheme.cardInset, in: Capsule())
            .overlay(Capsule().strokeBorder(prominent ? .clear : PodkopTheme.separator))
            .opacity(configuration.isPressed ? 0.75 : 1)
            .contentShape(Capsule())
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
                        onMessage(String(localized: .commonCouldNotCompleteAction))
                    }
                }
            }
            return controller
        } catch {
            try? FileManager.default.removeItem(at: directory)
            onMessage(String(localized: .commonCouldNotCompleteAction))
            return UIActivityViewController(activityItems: [], applicationActivities: nil)
        }
    }

    func updateUIViewController(_ controller: UIActivityViewController, context: Context) {}
}
