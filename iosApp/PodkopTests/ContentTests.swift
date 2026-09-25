import XCTest
import UIKit
import ImageIO
import UniformTypeIdentifiers
import PodkopShared
@testable import Podkop

@MainActor
final class ContentTests: XCTestCase {
    func testBridgePayloadPreservesUnknownDeletionMediaAndCapabilities() {
        let photo = IOSPhoto(url: "https://example.com/p.png", width: 640, height: 480,
                             mimeType: "image/png", key: "photo-key", label: "foto")
        let survey = IOSSurvey(question: "Pytanie?",
                               answers: [IOSSurveyAnswer(id: 2, text: "Tak", count: 3, selected: true)],
                               count: 3, canVote: false, selectedOption: nil)
        let payload = IOSResource(
            id: 77, kind: "futureKind", title: "Title", content: "Body", author: "Ewa",
            adult: true, deleted: true, editable: false, favourite: true,
            description: "Description", authorAvatarUrl: nil, authorColor: "green",
            authorVerified: true, authorOnline: true, authorRank: nil, authorGender: "female",
            deletionReason: "moderator", parentId: nil, createdAt: nil,
            commentsCount: 9, votesUp: 5, votesDown: 1, voted: "negative",
            canVoteUp: true, canVoteDown: true, canUndoVote: false, canDelete: false,
            tags: ["nauka"], photo: photo, embed: nil, survey: survey,
            sourceUrl: nil, sourceLabel: nil, hot: false, recommended: true, slug: "",
            canReply: true, canFavourite: true,
            inlineComments: [IOSResource(
                id: 5, kind: "entryComment", title: "", content: "reply", author: "Ola",
                adult: false, deleted: false, editable: false, favourite: false,
                description: "", authorAvatarUrl: nil, authorColor: nil,
                authorVerified: false, authorOnline: false, authorRank: nil, authorGender: nil,
                deletionReason: nil, parentId: 77, createdAt: nil,
                commentsCount: 0, votesUp: 0, votesDown: 0, voted: "none",
                canVoteUp: false, canVoteDown: false, canUndoVote: false, canDelete: false,
                tags: [], photo: nil, embed: nil, survey: nil,
                sourceUrl: nil, sourceLabel: nil, hot: false, recommended: false, slug: "",
                canReply: false, canFavourite: false, inlineComments: []
            )]
        )
        let resource = Resource(payload)
        XCTAssertEqual(resource.id, "unknown:77")
        XCTAssertEqual(resource.description, "Description", "Kotlin exports description as description_")
        XCTAssertEqual(resource.deletion, .moderator)
        XCTAssertEqual(resource.author?.name, "Ewa")
        XCTAssertEqual(resource.author?.color, "green")
        XCTAssertTrue(resource.adult)
        XCTAssertTrue(resource.favourite)
        XCTAssertEqual(resource.vote.down, 1)
        XCTAssertTrue(resource.vote.canDown)
        XCTAssertEqual(resource.photo?.width, 640)
        XCTAssertEqual(resource.photo?.key, "photo-key")
        XCTAssertEqual(resource.survey?.answers.first?.selected, true)
        XCTAssertEqual(resource.tags, ["nauka"])
        XCTAssertEqual(resource.photo?.label, "foto")
        XCTAssertTrue(resource.canReply)
        XCTAssertEqual(resource.inlineComments.map(\.id), ["entryComment:5"])
        let unknownDeletion = Resource(IOSResource(
            id: 78, kind: "entry", title: "", content: "must stay hidden", author: nil,
            adult: false, deleted: true, editable: false, favourite: false,
            description: "", authorAvatarUrl: nil, authorColor: nil,
            authorVerified: false, authorOnline: false, authorRank: nil, authorGender: nil,
            deletionReason: "futureReason", parentId: nil, createdAt: nil,
            commentsCount: 0, votesUp: 0, votesDown: 0, voted: "none",
            canVoteUp: false, canVoteDown: false, canUndoVote: false, canDelete: false,
            tags: [], photo: nil, embed: nil, survey: nil,
            sourceUrl: nil, sourceLabel: nil, hot: false, recommended: false, slug: "",
            canReply: false, canFavourite: false, inlineComments: []
        ))
        XCTAssertEqual(unknownDeletion.deletion, .unknown)
    }

    func testRichCorpusKeepsSpoilerListsCodeUnicodeAndLiteralDashes() {
        let blocks = RichContentParser.parse(ContentFixtures.entry.body + "\n-------------")
        XCTAssertTrue(blocks.contains(.spoiler("Ukryty tekst ze spoilerem.")))
        XCTAssertTrue(blocks.contains(.bullet(0, "Pierwszy punkt")))
        XCTAssertTrue(RichContentParser.parse("- A\n  - B").contains(.bullet(1, "B")))
        XCTAssertTrue(blocks.contains(.quote("Cytat")))
        XCTAssertTrue(blocks.contains(.code("let a = 1")))
        XCTAssertTrue(blocks.contains(.paragraph("-------------")))
        XCTAssertTrue(ContentFixtures.entry.body.contains("👩🏽‍💻"))
        let attributed = RichContentParser.attributed("@ewa-test #zażółć [site](https://example.com)")
        let links = Array(attributed.runs).compactMap { $0.link }
        XCTAssertEqual(links.count, 3)
        XCTAssertEqual(links[0].host, "profile")
        XCTAssertEqual(links[1].host, "tag")
        let protected = RichContentParser.linkMentionsAndTags("`@code` [@existing](https://example.com?q=@x) @new")
        XCTAssertTrue(protected.contains("`@code`"))
        XCTAssertTrue(protected.contains("[@existing](https://example.com?q=@x)"))
        XCTAssertTrue(protected.contains("podkop://profile/new"))
    }

    func testTweetPreviewMappingKeepsMediaMetadata() {
        let source = IOSTweetPreview(authorName: "Ewa", authorHandle: "@ewa",
                                     avatarUrl: "https://example.com/a.png", text: "tekst",
                                     replyCount: 1, retweetCount: 2, likeCount: 3,
                                     mediaThumbnailUrl: "https://example.com/t.png",
                                     mediaAspectRatio: nil)
        let preview = TweetPreview(source)
        XCTAssertEqual(preview.avatarURL, "https://example.com/a.png")
        XCTAssertEqual(preview.mediaThumbnailURL, "https://example.com/t.png")
        XCTAssertEqual(preview.likes, 3)
    }

    func testDecoderDownsamplesAndRejectsOversizedInput() {
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: 1024, height: 768))
        let bytes = renderer.pngData { context in
            UIColor.systemBlue.setFill()
            context.cgContext.fill(CGRect(x: 0, y: 0, width: 1024, height: 768))
        }
        let image = ImageDecoder.shared.image(from: bytes, key: "test-image", maxDimension: 256)
        XCTAssertNotNil(image)
        XCTAssertLessThanOrEqual(image?.cgImage?.width ?? 0, 256)
        XCTAssertNil(ImageDecoder.shared.image(
            from: Data(count: ImageDecoder.maximumInputBytes + 1),
            key: "oversized", maxDimension: 256
        ))
        ImageDecoder.shared.clear()
    }

    func testScreenshotSurfaceIncludesParent() {
        let image = ScreenshotSurface.render(resource: ContentFixtures.entryComment,
                                                   parent: ContentFixtures.entry, width: 390)
        XCTAssertNotNil(image)
        XCTAssertGreaterThan(image?.size.height ?? 0, 200)
    }

    func testExportKeepsSourceImageFormat() {
        XCTAssertEqual(PlatformExport.fileExtension(for: Data([0x47, 0x49, 0x46, 0x38])), "gif")
        XCTAssertEqual(PlatformExport.fileExtension(for: Data([0x89, 0x50, 0x4e, 0x47])), "png")
        XCTAssertEqual(PlatformExport.fileExtension(for: Data([0xff, 0xd8, 0xff])), "jpg")
        XCTAssertNil(PlatformExport.fileExtension(for: Data([0x00, 0x01])))
    }

    func testScreenshotRendersLoadedPhotoBytes() {
        let photo = Photo(url: "fixture://photo", width: 64, height: 40,
                                mimeType: "image/png", key: "photo")
        let resource = Resource(sourceID: 500, kind: .entry, body: "Photo",
                                      photo: photo)
        func imageData(_ color: UIColor) -> Data {
            UIGraphicsImageRenderer(size: CGSize(width: 64, height: 40)).pngData { context in
                color.setFill()
                context.fill(CGRect(x: 0, y: 0, width: 64, height: 40))
            }
        }
        let red = ScreenshotSurface.render(resource: resource,
            photoBytes: [photo.url: imageData(.red)])?.pngData()
        ImageDecoder.shared.clear()
        let blue = ScreenshotSurface.render(resource: resource,
            photoBytes: [photo.url: imageData(.blue)])?.pngData()
        ImageDecoder.shared.clear()
        XCTAssertNotNil(red)
        XCTAssertNotEqual(red, blue, "Screenshot rendering must use the loaded photo, not a loading placeholder")
    }

    func testAnimatedImageFrameCountIsBoundedAndDownsampled() {
        let frames = (0..<30).map { index in
            UIGraphicsImageRenderer(size: CGSize(width: 700, height: 700)).image { context in
                (index.isMultiple(of: 2) ? UIColor.red : UIColor.blue).setFill()
                context.cgContext.fill(CGRect(x: 0, y: 0, width: 700, height: 700))
            }.cgImage!
        }
        let data = NSMutableData()
        let destination = CGImageDestinationCreateWithData(data, UTType.gif.identifier as CFString,
                                                            frames.count, nil)!
        for frame in frames {
            CGImageDestinationAddImage(destination, frame, [
                kCGImagePropertyGIFDictionary: [kCGImagePropertyGIFDelayTime: 0.1]
            ] as CFDictionary)
        }
        XCTAssertTrue(CGImageDestinationFinalize(destination))
        let image = ImageDecoder.shared.image(from: data as Data, key: "test-gif", maxDimension: 700)
        XCTAssertEqual(image?.images?.count, 24)
        XCTAssertLessThanOrEqual(image?.images?.first?.cgImage?.width ?? 0, 512)
        ImageDecoder.shared.clear()
    }

    func testRichParserRepresentativeLongFeedTiming() {
        let mixed = ContentFixtures.entry.body + ContentFixtures.link.body
        let start = Date()
        for _ in 0..<1_000 { _ = RichContentParser.parse(mixed) }
        let duration = Date().timeIntervalSince(start)
        XCTAssertLessThan(duration, 10)
    }
}
