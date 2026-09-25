import SwiftUI

struct ContentFixtureGallery: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                LazyVStack(spacing: 12) {
                    ForEach(ContentFixtures.all) { resource in
                        NativeResourceCard(resource: resource)
                    }
                }
                .padding()
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .navigationTitle("Content")
        }
    }
}
