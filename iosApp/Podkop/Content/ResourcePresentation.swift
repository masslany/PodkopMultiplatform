import Foundation
import PodkopShared

enum ResourceKind: String, Hashable {
    case link, entry, entryComment, linkComment, unknown
}

enum Deletion: String, Hashable {
    case moderator, author, entryAuthor, unknown
}

struct Author: Hashable {
    let name: String
    let avatarURL: String?
    let color: String?
    let verified: Bool
    let online: Bool
    let rank: Int?
    var gender: String? = nil
}

struct Vote: Hashable {
    let up: Int
    let down: Int
    let state: String
    let canUp: Bool
    let canDown: Bool
    let canUndo: Bool
}

struct Photo: Hashable {
    let url: String
    let width: Int
    let height: Int
    let mimeType: String
    let key: String
    /// The image source Wykop shows under it ("źródło: …").
    var label: String = ""
    var isAnimated: Bool { mimeType.lowercased().contains("gif") }
}

struct Embed: Hashable {
    let key: String
    let url: String
    let thumbnailURL: String
    let type: String
}

struct Survey: Hashable {
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
struct Resource: Identifiable, Hashable {
    let sourceID: Int
    let kind: ResourceKind
    let title: String
    let body: String
    let description: String
    let author: Author?
    let deletion: Deletion?
    let adult: Bool
    let editable: Bool
    let deletable: Bool
    var favourite: Bool
    let parentID: Int?
    let createdAt: String?
    let commentCount: Int
    var vote: Vote
    let tags: [String]
    let photo: Photo?
    let embed: Embed?
    let survey: Survey?
    let sourceURL: String?
    let sourceLabel: String?
    let hot: Bool
    let recommended: Bool
    let slug: String
    let canReply: Bool
    let canFavourite: Bool
    /// Newest comments embedded by the API: under entries in lists, and replies under link comments.
    var inlineComments: [Resource]
    /// From a blacklisted author (or, for comments, blacklisted itself): the body stays hidden
    /// until the reader asks, like Android's `BlacklistedContentGate`.
    var blacklisted = false

    var id: String { "\(kind.rawValue):\(sourceID)" }

    init(_ value: IOSResource) {
        sourceID = Int(value.id)
        kind = ResourceKind(rawValue: value.kind) ?? .unknown
        title = value.title
        body = value.content
        description = value.description_
        author = value.author.map {
            Author(name: $0, avatarURL: value.authorAvatarUrl,
                         color: value.authorColor, verified: value.authorVerified,
                         online: value.authorOnline, rank: value.authorRank?.intValue,
                         gender: value.authorGender)
        }
        deletion = value.deleted
            ? value.deletionReason.flatMap(Deletion.init(rawValue:)) ?? .unknown
            : nil
        adult = value.adult
        editable = value.editable
        deletable = value.canDelete
        favourite = value.favourite
        parentID = value.parentId?.intValue
        createdAt = value.createdAt
        commentCount = Int(value.commentsCount)
        vote = Vote(up: Int(value.votesUp), down: Int(value.votesDown), state: value.voted,
                          canUp: value.canVoteUp, canDown: value.canVoteDown,
                          canUndo: value.canUndoVote)
        tags = value.tags
        photo = value.photo.map { Photo(url: $0.url, width: Int($0.width),
                                              height: Int($0.height), mimeType: $0.mimeType,
                                              key: $0.key, label: $0.label) }
        embed = value.embed.map { Embed(key: $0.key, url: $0.url,
                                             thumbnailURL: $0.thumbnailUrl, type: $0.type) }
        survey = value.survey.map { survey in
            Survey(question: survey.question,
                         answers: survey.answers.map {
                             Survey.Answer(id: Int($0.id), text: $0.text,
                                                 count: Int($0.count), selected: $0.selected)
                         }, count: Int(survey.count), canVote: survey.canVote,
                         selectedOption: survey.selectedOption?.intValue)
        }
        sourceURL = value.sourceUrl
        sourceLabel = value.sourceLabel
        hot = value.hot
        recommended = value.recommended
        slug = value.slug
        canReply = value.canReply
        canFavourite = value.canFavourite
        inlineComments = value.inlineComments.map(Resource.init)
        blacklisted = value.blacklisted
    }

    init(sourceID: Int, kind: ResourceKind, title: String = "", body: String,
         description: String = "", author: Author? = nil, deletion: Deletion? = nil,
         adult: Bool = false, editable: Bool = false, deletable: Bool = false,
         favourite: Bool = false, parentID: Int? = nil, createdAt: String? = nil,
         commentCount: Int = 0,
         vote: Vote = Vote(up: 0, down: 0, state: "none", canUp: false,
                                       canDown: false, canUndo: false),
         tags: [String] = [], photo: Photo? = nil, embed: Embed? = nil,
         survey: Survey? = nil, sourceURL: String? = nil, sourceLabel: String? = nil,
         hot: Bool = false, recommended: Bool = false, slug: String = "",
         canReply: Bool = false, canFavourite: Bool = false, inlineComments: [Resource] = []) {
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
        self.canReply = canReply
        self.canFavourite = canFavourite
        self.inlineComments = inlineComments
    }
}
