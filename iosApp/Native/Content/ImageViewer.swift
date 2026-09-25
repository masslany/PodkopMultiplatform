import SwiftUI
import UIKit

struct NativeImageViewer: View {
    let bytes: Data
    let key: String
    @State private var image: UIImage?

    var body: some View {
        Group {
            if let image {
                ZoomableImage(image: image)
                    .ignoresSafeArea(edges: .bottom)
            } else {
                ProgressView("Loading image…")
            }
        }
        .task(id: key) {
            let bytes = bytes
            let key = key
            let decoded = await Task.detached(priority: .utility) {
                NativeImageDecoder.shared.image(from: bytes, key: key, maxDimension: 2048)
            }.value
            if !Task.isCancelled { image = decoded }
        }
        .onDisappear { image = nil }
    }
}

/// Full-screen zoomable image with save, copy and share, like Android's image viewer.
struct NativeImageViewerScreen: View {
    let bytes: Data
    let key: String
    @Environment(\.dismiss) private var dismiss
    @State private var message: String?

    var body: some View {
        NavigationStack {
            NativeImageViewer(bytes: bytes, key: key)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.black)
                .safeAreaInset(edge: .bottom) {
                    VStack(spacing: 8) {
                        if let message {
                            Text(message).font(.footnote).foregroundStyle(.white)
                                .accessibilityIdentifier("imageMessage")
                        }
                        ImageExportControls(data: bytes) { message = $0 }
                            .tint(.white)
                    }
                    .padding()
                }
                .toolbar {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Close") { dismiss() }.tint(.white)
                    }
                }
                .toolbarBackground(.black, for: .navigationBar)
                .preferredColorScheme(.dark)
        }
    }
}

private struct ZoomableImage: UIViewRepresentable {
    let image: UIImage

    func makeCoordinator() -> Coordinator { Coordinator() }
    func makeUIView(context: Context) -> UIScrollView {
        let scroll = UIScrollView()
        scroll.minimumZoomScale = 1
        scroll.maximumZoomScale = 4
        scroll.delegate = context.coordinator
        let imageView = UIImageView(image: image)
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        scroll.addSubview(imageView)
        NSLayoutConstraint.activate([
            imageView.leadingAnchor.constraint(equalTo: scroll.contentLayoutGuide.leadingAnchor),
            imageView.trailingAnchor.constraint(equalTo: scroll.contentLayoutGuide.trailingAnchor),
            imageView.topAnchor.constraint(equalTo: scroll.contentLayoutGuide.topAnchor),
            imageView.bottomAnchor.constraint(equalTo: scroll.contentLayoutGuide.bottomAnchor),
            imageView.widthAnchor.constraint(equalTo: scroll.frameLayoutGuide.widthAnchor),
            imageView.heightAnchor.constraint(equalTo: scroll.frameLayoutGuide.heightAnchor)
        ])
        context.coordinator.imageView = imageView
        return scroll
    }
    func updateUIView(_ scroll: UIScrollView, context: Context) {
        context.coordinator.imageView?.image = image
    }
    final class Coordinator: NSObject, UIScrollViewDelegate {
        weak var imageView: UIImageView?
        func viewForZooming(in scrollView: UIScrollView) -> UIView? { imageView }
    }
}
