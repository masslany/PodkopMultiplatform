import Foundation
import PodkopShared

enum NativeResourceKind: String, Hashable {
    case link, entry, entryComment, linkComment, unknown
}

enum NativeDeletion: String, Hashable {
    case moderator, author, entryAuthor, unknown
}

struct NativeAuthor: Hashable {
    let name: String
    let avatarURL: String?
    let color: String?
    let verified: Bool
    let online: Bool
    let rank: Int?
    var gender: String? = nil
}

struct NativeVote: Hashable {
    let up: Int
    let down: Int
    let state: String
    let canUp: Bool
    let canDown: Bool
    let canUndo: Bool
}

struct NativePhoto: Hashable {
    let url: String
    let width: Int
    let height: Int
    let mimeType: String
    let key: String
    var isAnimated: Bool { mimeType.lowercased().contains("gif") }
}

struct NativeEmbed: Hashable {
    let key: String
    let url: String
    let thumbnailURL: String
    let type: String
}

struct NativeSurvey: Hashable {
    struct Answer: Hashable {
        let id: Int
        let text: String
        let count: Int
        let selected: Bool
    }
    let question: String
    let answers: [Answer]
    let count: Int
    let canVote: Bool
    let selectedOption: Int?
}

/// A display value. It contains no bridge references, tokens, or network behavior.
struct NativeResource: Identifiable, Hashable {
    let sourceID: Int
    let kind: NativeResourceKind
    let title: String
    let body: String
    let description: String
    let author: NativeAuthor?
    let deletion: NativeDeletion?
    let adult: Bool
    let editable: Bool
    let deletable: Bool
    var favourite: Bool
    let parentID: Int?
    let createdAt: String?
    let commentCount: Int
    var vote: NativeVote
    let tags: [String]
    let photo: NativePhoto?
    let embed: NativeEmbed?
    let survey: NativeSurvey?
    let sourceURL: String?
    let sourceLabel: String?
    let hot: Bool
    let recommended: Bool
    let slug: String

    var id: String { "\(kind.rawValue):\(sourceID)" }

    init(_ value: IOSResource) {
        sourceID = Int(value.id)
        kind = NativeResourceKind(rawValue: value.kind) ?? .unknown
        title = value.title
        body = value.content
        description = value.description_
        author = value.author.map {
            NativeAuthor(name: $0, avatarURL: value.authorAvatarUrl,
                         color: value.authorColor, verified: value.authorVerified,
                         online: value.authorOnline, rank: value.authorRank?.intValue,
                         gender: value.authorGender)
        }
        deletion = value.deleted
            ? value.deletionReason.flatMap(NativeDeletion.init(rawValue:)) ?? .unknown
            : nil
        adult = value.adult
        editable = value.editable
        deletable = value.canDelete
        favourite = value.favourite
        parentID = value.parentId?.intValue
        createdAt = value.createdAt
        commentCount = Int(value.commentsCount)
        vote = NativeVote(up: Int(value.votesUp), down: Int(value.votesDown), state: value.voted,
                          canUp: value.canVoteUp, canDown: value.canVoteDown,
                          canUndo: value.canUndoVote)
        tags = value.tags
        photo = value.photo.map { NativePhoto(url: $0.url, width: Int($0.width),
                                              height: Int($0.height), mimeType: $0.mimeType,
                                              key: $0.key) }
        embed = value.embed.map { NativeEmbed(key: $0.key, url: $0.url,
                                             thumbnailURL: $0.thumbnailUrl, type: $0.type) }
        survey = value.survey.map { survey in
            NativeSurvey(question: survey.question,
                         answers: survey.answers.map {
                             NativeSurvey.Answer(id: Int($0.id), text: $0.text,
                                                 count: Int($0.count), selected: $0.selected)
                         }, count: Int(survey.count), canVote: survey.canVote,
                         selectedOption: survey.selectedOption?.intValue)
        }
        sourceURL = value.sourceUrl
        sourceLabel = value.sourceLabel
        hot = value.hot
        recommended = value.recommended
        slug = value.slug
    }

    init(sourceID: Int, kind: NativeResourceKind, title: String = "", body: String,
         description: String = "", author: NativeAuthor? = nil, deletion: NativeDeletion? = nil,
         adult: Bool = false, editable: Bool = false, deletable: Bool = false,
         favourite: Bool = false, parentID: Int? = nil, createdAt: String? = nil,
         commentCount: Int = 0,
         vote: NativeVote = NativeVote(up: 0, down: 0, state: "none", canUp: false,
                                       canDown: false, canUndo: false),
         tags: [String] = [], photo: NativePhoto? = nil, embed: NativeEmbed? = nil,
         survey: NativeSurvey? = nil, sourceURL: String? = nil, sourceLabel: String? = nil,
         hot: Bool = false, recommended: Bool = false, slug: String = "") {
        self.sourceID = sourceID
        self.kind = kind
        self.title = title
        self.body = body
        self.description = description
        self.author = author
        self.deletion = deletion
        self.adult = adult
        self.editable = editable
        self.deletable = deletable
        self.favourite = favourite
        self.parentID = parentID
        self.createdAt = createdAt
        self.commentCount = commentCount
        self.vote = vote
        self.tags = tags
        self.photo = photo
        self.embed = embed
        self.survey = survey
        self.sourceURL = sourceURL
        self.sourceLabel = sourceLabel
        self.hot = hot
        self.recommended = recommended
        self.slug = slug
    }
}
