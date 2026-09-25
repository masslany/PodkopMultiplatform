import Foundation
import Observation
import PodkopShared

struct NativeLinkDraft: Identifiable, Hashable {
    let key: String
    let url: String
    let title: String
    let description: String
    let tags: [String]
    let adult: Bool
    let photoKey: String?
    let photoURL: String?
    let suggestedImages: [String]
    let selectedImageIndex: Int?
    var id: String { key }

    init(_ value: IOSLinkDraft) {
        key = value.key
        url = value.url
        title = value.title
        description = value.description_
        tags = value.tags
        adult = value.adult
        photoKey = value.photoKey
        photoURL = value.photoUrl
        suggestedImages = value.suggestedImages
        selectedImageIndex = value.selectedImageIndex?.intValue
    }
}

struct NativeLinkCheck {
    let key: String
    let duplicate: Bool
    let similar: [NativeResource]
}

struct LinkDraftValues {
    let title: String
    let description: String?
    let tags: [String]
    let photoKey: String?
    let adult: Bool
    let selectedImageIndex: Int?
}
