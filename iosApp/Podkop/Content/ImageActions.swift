import SwiftUI

extension View {
    /// Long-pressing an image offers copying and saving it, like Android's `imageActions`
    /// sheet. The bytes load on demand through the media cache; results show in the app banner.
    func imageActions(url: String?) -> some View {
        modifier(ImageActionsModifier(url: url))
    }
}

private struct ImageActionsModifier: ViewModifier {
    let url: String?
    @Environment(\.mediaLoader) private var loader
    @Environment(\.imageActionMessage) private var message

    func body(content: Content) -> some View {
        if let url, !url.isEmpty, let loader, let message {
            content.contextMenu {
                Button {
                    Task {
                        guard let data = try? await loader.bytes(for: url) else {
                            return message(String(localized: .commonCouldNotCompleteAction))
                        }
                        message(PlatformExport.copyImage(data)
                                ? String(localized: .platformImageCopied)
                                : String(localized: .commonCouldNotCompleteAction))
                    }
                } label: {
                    Label(.platformScreenshotPreviewActionCopy, systemImage: "doc.on.doc")
                }
                Button {
                    Task {
                        guard let data = try? await loader.bytes(for: url) else {
                            return message(String(localized: .commonCouldNotCompleteAction))
                        }
                        switch await PlatformExport.saveToPhotos(data) {
                        case .saved: message(String(localized: .platformSavedToPhotos))
                        case .denied: message(String(localized: .platformAllowAddingPhotos))
                        case .failed: message(String(localized: .commonCouldNotCompleteAction))
                        }
                    }
                } label: {
                    Label(.commonSave, systemImage: "square.and.arrow.down")
                }
            }
        } else {
            content
        }
    }
}

private struct ImageActionMessageKey: EnvironmentKey {
    static let defaultValue: ((String) -> Void)? = nil
}

extension EnvironmentValues {
    /// Where image actions report their outcome; the app root shows it as a banner.
    var imageActionMessage: ((String) -> Void)? {
        get { self[ImageActionMessageKey.self] }
        set { self[ImageActionMessageKey.self] = newValue }
    }
}
