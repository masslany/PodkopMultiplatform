package pl.masslany.podkop.business.testsupport.fixtures

import kotlinx.datetime.LocalDateTime
import pl.masslany.podkop.business.common.data.network.models.comments.CommentItemDto
import pl.masslany.podkop.business.common.data.network.models.comments.LocationDto
import pl.masslany.podkop.business.common.data.network.models.comments.ParentDto
import pl.masslany.podkop.business.common.data.network.models.common.ActionsDto as CommonActionsDto
import pl.masslany.podkop.business.common.data.network.models.common.AuthorDto
import pl.masslany.podkop.business.common.data.network.models.common.CommentsDto
import pl.masslany.podkop.business.common.data.network.models.common.EmbedDto
import pl.masslany.podkop.business.common.data.network.models.common.MediaDto
import pl.masslany.podkop.business.common.data.network.models.common.PaginationDto
import pl.masslany.podkop.business.common.data.network.models.common.PhotoDto
import pl.masslany.podkop.business.common.data.network.models.common.RankDto as CommonRankDto
import pl.masslany.podkop.business.common.data.network.models.common.ResourceItemDto
import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SourceDto
import pl.masslany.podkop.business.common.data.network.models.common.UserDto
import pl.masslany.podkop.business.common.data.network.models.common.VotesDto
import pl.masslany.podkop.business.common.data.network.models.entries.AnswerDto
import pl.masslany.podkop.business.common.data.network.models.entries.SurveyDto
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.domain.models.common.Author
import pl.masslany.podkop.business.common.domain.models.common.Comment
import pl.masslany.podkop.business.common.domain.models.common.Comments
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Embed
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.Media
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Pagination
import pl.masslany.podkop.business.common.domain.models.common.Parent
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Resources
import pl.masslany.podkop.business.common.domain.models.common.Source
import pl.masslany.podkop.business.common.domain.models.common.Survey
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes
import pl.masslany.podkop.business.common.domain.models.links.Link
import pl.masslany.podkop.business.profile.data.network.models.ActionsDto as ProfileActionsDto
import pl.masslany.podkop.business.profile.data.network.models.BannedDto
import pl.masslany.podkop.business.profile.data.network.models.EntriesDetailsDto
import pl.masslany.podkop.business.profile.data.network.models.LinksDetailsDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedTagsResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ObservedUsersResponseDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDataDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDataDto
import pl.masslany.podkop.business.profile.data.network.models.ProfileShortDto
import pl.masslany.podkop.business.profile.data.network.models.RankDto as ProfileRankDto
import pl.masslany.podkop.business.profile.data.network.models.SocialMediaDto
import pl.masslany.podkop.business.profile.data.network.models.SummaryDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteDataDto
import pl.masslany.podkop.business.profile.data.network.models.UsersAutoCompleteResponseDto
import pl.masslany.podkop.business.profile.domain.models.ObservedTag
import pl.masslany.podkop.business.profile.domain.models.ObservedTags
import pl.masslany.podkop.business.profile.domain.models.ObservedUser
import pl.masslany.podkop.business.profile.domain.models.ObservedUsers
import pl.masslany.podkop.business.profile.domain.models.Profile
import pl.masslany.podkop.business.profile.domain.models.ProfileShort
import pl.masslany.podkop.business.profile.domain.models.Summary
import pl.masslany.podkop.business.profile.domain.models.UserAutoCompleteItem
import pl.masslany.podkop.business.profile.domain.models.UsersAutoComplete
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteDataDto
import pl.masslany.podkop.business.tags.data.network.models.TagsAutoCompleteResponseDto
import pl.masslany.podkop.business.tags.domain.models.TagsAutoComplete
import pl.masslany.podkop.business.tags.domain.models.TagsAutoCompleteItem

/**
 * Centralized test data builders for the business module.
 *
 * Conventions:
 * - Defaults are valid, deterministic, and non-recursive.
 * - Tests can override only the fields they care about using `.copy(...)`.
 * - DTO and domain builders share the same value vocabulary to keep mapper tests readable.
 */
object BusinessFixtures {
    val dateTime: LocalDateTime = LocalDateTime.parse("2024-01-02T03:04:05")

    fun commonActionsDto(
        create: Boolean? = true,
        createFavourite: Boolean? = false,
        delete: Boolean? = true,
        deleteFavourite: Boolean? = false,
        finishAma: Boolean? = true,
        report: Boolean? = false,
        startAma: Boolean? = true,
        undoVote: Boolean? = false,
        update: Boolean? = true,
        voteDown: Boolean? = false,
        voteUp: Boolean? = true,
    ): CommonActionsDto = CommonActionsDto(
        create = create,
        createFavourite = createFavourite,
        delete = delete,
        deleteFavourite = deleteFavourite,
        finishAma = finishAma,
        report = report,
        startAma = startAma,
        undoVote = undoVote,
        update = update,
        voteDown = voteDown,
        voteUp = voteUp,
    )

    fun commonRankDto(
        position: Int? = 10,
        trend: Int = 2,
    ): CommonRankDto = CommonRankDto(
        position = position,
        trend = trend,
    )

    fun authorDto(
        avatar: String = "https://example.com/avatar.png",
        blacklist: Boolean = false,
        color: String = "orange",
        company: Boolean = false,
        follow: Boolean = true,
        gender: String? = "m",
        note: Boolean = false,
        online: Boolean = true,
        rank: CommonRankDto = commonRankDto(),
        status: String = "active",
        username: String = "tester",
        verified: Boolean = true,
    ): AuthorDto = AuthorDto(
        avatar = avatar,
        blacklist = blacklist,
        color = color,
        company = company,
        follow = follow,
        gender = gender,
        note = note,
        online = online,
        rank = rank,
        status = status,
        username = username,
        verified = verified,
    )

    fun userDto(
        avatar: String = "https://example.com/user.png",
        blacklist: Boolean = false,
        color: String = "green",
        company: Boolean = true,
        follow: Boolean = false,
        gender: String? = "f",
        note: Boolean = true,
        online: Boolean = false,
        rank: CommonRankDto = commonRankDto(position = 22, trend = -1),
        status: String = "online",
        username: String = "observed-user",
        verified: Boolean = false,
    ): UserDto = UserDto(
        avatar = avatar,
        blacklist = blacklist,
        color = color,
        company = company,
        follow = follow,
        gender = gender,
        note = note,
        online = online,
        rank = rank,
        status = status,
        username = username,
        verified = verified,
    )

    fun votesDto(
        count: Int? = 5,
        down: Int = 1,
        up: Int = 6,
        users: List<UserDto>? = null,
    ): VotesDto = VotesDto(
        count = count,
        down = down,
        up = up,
        users = users,
    )

    fun answerDto(
        count: Int = 3,
        id: Int = 101,
        text: String = "Answer text",
        voted: Int = 1,
    ): AnswerDto = AnswerDto(
        count = count,
        id = id,
        text = text,
        voted = voted,
    )

    fun surveyDto(
        actions: CommonActionsDto = commonActionsDto(),
        answers: List<AnswerDto> = listOf(answerDto()),
        count: Int = 10,
        deletable: Boolean = false,
        editable: Boolean = true,
        key: String = "survey-key",
        question: String = "Question?",
        voted: Int = 1,
    ): SurveyDto = SurveyDto(
        actions = actions,
        answers = answers,
        count = count,
        deletable = deletable,
        editable = editable,
        key = key,
        question = question,
        voted = voted,
    )

    fun embedDto(
        key: String = "embed-key",
        thumbnail: String? = "https://example.com/thumb.jpg",
        type: String = "youtube",
        url: String = "https://example.com/embed",
    ): EmbedDto = EmbedDto(
        key = key,
        thumbnail = thumbnail,
        type = type,
        url = url,
    )

    fun photoDto(
        height: Int = 480,
        key: String = "photo-key",
        label: String = "photo-label",
        mimeType: String = "image/png",
        size: Int = 2048,
        url: String = "https://example.com/photo.png",
        width: Int = 640,
    ): PhotoDto = PhotoDto(
        height = height,
        key = key,
        label = label,
        mimeType = mimeType,
        size = size,
        url = url,
        width = width,
    )

    fun mediaDto(
        embed: EmbedDto? = null,
        photo: PhotoDto? = null,
        survey: SurveyDto? = null,
    ): MediaDto = MediaDto(
        embed = embed,
        photo = photo,
        survey = survey,
    )

    fun sourceDto(
        label: String = "Source label",
        type: String? = "app",
        typeId: Int? = 7,
        url: String = "https://example.com/source",
    ): SourceDto = SourceDto(
        label = label,
        type = type,
        typeId = typeId,
        url = url,
    )

    fun paginationDto(
        perPage: Int? = 20,
        total: Int? = 100,
        totalItems: Int? = 100,
        next: String? = "/page/2",
        prev: String? = "/page/0",
    ): PaginationDto = PaginationDto(
        perPage = perPage,
        total = total,
        totalItems = totalItems,
        next = next,
        prev = prev,
    )

    fun locationDto(
        filter: String = "all",
        page: Int = 1,
        parentPage: Int? = null,
    ): LocationDto = LocationDto(
        filter = filter,
        page = page,
        parentPage = parentPage,
    )

    fun parentDto(
        author: AuthorDto = authorDto(),
        id: Int = 200,
        link: ResourceItemDto? = null,
        location: List<LocationDto> = listOf(locationDto()),
        resource: String = "link",
        slug: String = "parent-slug",
    ): ParentDto = ParentDto(
        author = author,
        id = id,
        link = link,
        location = location,
        resource = resource,
        slug = slug,
    )

    fun commentItemDto(
        actions: CommonActionsDto = commonActionsDto(),
        adult: Boolean = false,
        archive: Boolean = false,
        author: AuthorDto = authorDto(),
        blacklist: Boolean = false,
        comments: CommentsDto? = null,
        content: String = "Comment content",
        createdAt: LocalDateTime? = dateTime,
        deletable: Boolean = true,
        deleted: String? = "moderator",
        device: String = "android",
        editable: Boolean = false,
        favourite: Boolean = true,
        id: Int = 300,
        media: MediaDto = mediaDto(),
        parent: ParentDto = parentDto(),
        parentId: Int? = 999,
        resource: String = "link_comment",
        slug: String = "comment-slug",
        tags: List<String> = listOf("#tag1", "#tag2"),
        voted: Int = -1,
        votes: VotesDto = votesDto(),
    ): CommentItemDto = CommentItemDto(
        actions = actions,
        adult = adult,
        archive = archive,
        author = author,
        blacklist = blacklist,
        comments = comments,
        content = content,
        createdAt = createdAt,
        deletable = deletable,
        deleted = deleted,
        device = device,
        editable = editable,
        favourite = favourite,
        id = id,
        media = media,
        parent = parent,
        parentId = parentId,
        resource = resource,
        slug = slug,
        tags = tags,
        voted = voted,
        votes = votes,
    )

    fun commentsDto(
        count: Int = 2,
        hot: Boolean? = true,
        items: List<CommentItemDto>? = emptyList(),
    ): CommentsDto = CommentsDto(
        count = count,
        hot = hot,
        items = items,
    )

    fun resourceItemDto(
        actions: CommonActionsDto? = commonActionsDto(),
        adult: Boolean? = true,
        archive: Boolean? = false,
        author: AuthorDto? = authorDto(),
        blacklist: Boolean? = false,
        comments: CommentsDto? = commentsDto(),
        content: String? = "Resource content",
        createdAt: LocalDateTime? = dateTime,
        deletable: Boolean? = true,
        deleted: String? = "author",
        device: String? = "ios",
        description: String? = "Resource description",
        editable: Boolean? = false,
        favourite: Boolean? = true,
        hot: Boolean? = true,
        id: Int? = 400,
        media: MediaDto? = mediaDto(),
        name: String? = "resource-name",
        parent: ParentDto? = parentDto(id = 201),
        parentId: Int? = 202,
        publishedAt: LocalDateTime? = dateTime,
        recommended: Boolean? = true,
        resource: String? = "link",
        slug: String? = "resource-slug",
        source: SourceDto? = sourceDto(),
        tags: List<String>? = listOf("#kotlin"),
        title: String? = "Resource title",
        voted: Int? = 1,
        votes: VotesDto? = votesDto(),
    ): ResourceItemDto = ResourceItemDto(
        actions = actions,
        adult = adult,
        archive = archive,
        author = author,
        blacklist = blacklist,
        comments = comments,
        content = content,
        createdAt = createdAt,
        deletable = deletable,
        deleted = deleted,
        device = device,
        description = description,
        editable = editable,
        favourite = favourite,
        hot = hot,
        id = id,
        media = media,
        name = name,
        parent = parent,
        parentId = parentId,
        publishedAt = publishedAt,
        recommended = recommended,
        resource = resource,
        slug = slug,
        source = source,
        tags = tags,
        title = title,
        voted = voted,
        votes = votes,
    )

    fun resourceResponseDto(
        data: List<ResourceItemDto> = listOf(resourceItemDto()),
        pagination: PaginationDto? = paginationDto(),
    ): ResourceResponseDto = ResourceResponseDto(
        data = data,
        pagination = pagination,
    )

    fun singleResourceResponseDto(
        data: ResourceItemDto = resourceItemDto(),
    ): SingleResourceResponseDto = SingleResourceResponseDto(
        data = data,
    )

    fun actions(
        create: Boolean = true,
        createFavourite: Boolean = false,
        delete: Boolean = true,
        deleteFavourite: Boolean = false,
        finishAma: Boolean = true,
        report: Boolean = false,
        startAma: Boolean = true,
        undoVote: Boolean = false,
        update: Boolean = true,
        voteDown: Boolean = false,
        voteUp: Boolean = true,
    ): Actions = Actions(
        create = create,
        createFavourite = createFavourite,
        delete = delete,
        deleteFavourite = deleteFavourite,
        finishAma = finishAma,
        report = report,
        startAma = startAma,
        undoVote = undoVote,
        update = update,
        voteDown = voteDown,
        voteUp = voteUp,
    )

    fun rank(
        position: Int = 10,
        trend: Int = 2,
    ): Rank = Rank(
        position = position,
        trend = trend,
    )

    fun author(
        avatar: String = "https://example.com/avatar.png",
        blacklist: Boolean = false,
        color: NameColor = NameColor.Orange,
        company: Boolean = false,
        follow: Boolean = true,
        gender: Gender = Gender.Male,
        note: Boolean = false,
        online: Boolean = true,
        rank: Rank = rank(),
        status: String = "active",
        username: String = "tester",
        verified: Boolean = true,
    ): Author = Author(
        avatar = avatar,
        blacklist = blacklist,
        color = color,
        company = company,
        follow = follow,
        gender = gender,
        note = note,
        online = online,
        rank = rank,
        status = status,
        username = username,
        verified = verified,
    )

    fun answer(
        count: Int = 3,
        id: Int = 101,
        text: String = "Answer text",
        voted: Int = 1,
    ): Answer = Answer(
        count = count,
        id = id,
        text = text,
        voted = voted,
    )

    fun embed(
        key: String = "embed-key",
        thumbnail: String = "https://example.com/thumb.jpg",
        type: String = "youtube",
        url: String = "https://example.com/embed",
    ): Embed = Embed(
        key = key,
        thumbnail = thumbnail,
        type = type,
        url = url,
    )

    fun photo(
        height: Int = 480,
        key: String = "photo-key",
        label: String = "photo-label",
        mimeType: String = "image/png",
        size: Int = 2048,
        url: String = "https://example.com/photo.png",
        width: Int = 640,
    ): Photo = Photo(
        height = height,
        key = key,
        label = label,
        mimeType = mimeType,
        size = size,
        url = url,
        width = width,
    )

    fun votes(
        count: Int = 5,
        down: Int = 1,
        up: Int = 6,
    ): Votes = Votes(
        count = count,
        down = down,
        up = up,
    )

    fun source(
        label: String = "Source label",
        type: String = "app",
        typeId: Int = 7,
        url: String = "https://example.com/source",
    ): Source = Source(
        label = label,
        type = type,
        typeId = typeId,
        url = url,
    )

    fun pagination(
        perPage: Int = 20,
        total: Int = 100,
        next: String = "/page/2",
        prev: String = "/page/0",
    ): Pagination = Pagination(
        perPage = perPage,
        total = total,
        next = next,
        prev = prev,
    )

    fun parent(
        id: Int = 200,
        linkId: Int? = null,
    ): Parent = Parent(
        id = id,
        linkId = linkId,
    )

    fun survey(
        actions: Actions = actions(),
        answers: List<Answer> = listOf(answer()),
        count: Int = 10,
        deletable: Boolean = false,
        editable: Boolean = true,
        key: String = "survey-key",
        question: String = "Question?",
        voted: Int = 1,
    ): Survey = Survey(
        actions = actions,
        answers = answers,
        count = count,
        deletable = deletable,
        editable = editable,
        key = key,
        question = question,
        voted = voted,
    )

    fun media(
        embed: Embed? = null,
        photo: Photo? = null,
        survey: Survey? = null,
    ): Media = Media(
        embed = embed,
        photo = photo,
        survey = survey,
    )

    fun comment(
        actions: Actions = actions(),
        adult: Boolean = false,
        archive: Boolean = false,
        author: Author = author(),
        blacklist: Boolean = false,
        comments: Comments? = null,
        content: String = "Comment content",
        createdAt: LocalDateTime? = dateTime,
        deletable: Boolean = true,
        deleted: Deleted = Deleted.Moderator,
        device: String = "android",
        editable: Boolean = false,
        favourite: Boolean = true,
        id: Int = 300,
        media: Media = media(),
        parentId: Int = 999,
        resource: Resource = Resource.LinkComment,
        slug: String = "comment-slug",
        tags: List<String> = listOf("#tag1", "#tag2"),
        voted: Voted = Voted.Negative,
        votes: Votes = votes(),
    ): Comment = Comment(
        actions = actions,
        adult = adult,
        archive = archive,
        author = author,
        blacklist = blacklist,
        comments = comments,
        content = content,
        createdAt = createdAt,
        deletable = deletable,
        deleted = deleted,
        device = device,
        editable = editable,
        favourite = favourite,
        id = id,
        media = media,
        parentId = parentId,
        resource = resource,
        slug = slug,
        tags = tags,
        voted = voted,
        votes = votes,
    )

    fun comments(
        count: Int = 2,
        hot: Boolean = true,
        items: List<Comment> = emptyList(),
    ): Comments = Comments(
        count = count,
        hot = hot,
        items = items,
    )

    fun resourceItem(
        actions: Actions? = actions(),
        adult: Boolean = true,
        archive: Boolean = false,
        author: Author? = author(),
        comments: Comments? = comments(),
        content: String = "Resource content",
        createdAt: LocalDateTime? = dateTime,
        deleted: Deleted = Deleted.Author,
        deletable: Boolean = true,
        description: String = "Resource description",
        editable: Boolean = false,
        hot: Boolean = true,
        id: Int = 400,
        media: Media? = media(),
        name: String = "resource-name",
        parent: Parent? = parent(id = 201),
        parentId: Int? = 202,
        publishedAt: LocalDateTime? = dateTime,
        recommended: Boolean = true,
        resource: Resource = Resource.Link,
        slug: String = "resource-slug",
        source: Source? = source(),
        tags: List<String> = listOf("#kotlin"),
        title: String = "Resource title",
        voted: Voted = Voted.Positive,
        votes: Votes? = votes(),
    ): ResourceItem = ResourceItem(
        actions = actions,
        adult = adult,
        archive = archive,
        author = author,
        comments = comments,
        content = content,
        createdAt = createdAt,
        deleted = deleted,
        deletable = deletable,
        description = description,
        editable = editable,
        hot = hot,
        id = id,
        media = media,
        name = name,
        parent = parent,
        parentId = parentId,
        publishedAt = publishedAt,
        recommended = recommended,
        resource = resource,
        slug = slug,
        source = source,
        tags = tags,
        title = title,
        voted = voted,
        votes = votes,
    )

    fun resources(
        data: List<ResourceItem> = listOf(resourceItem()),
        page: Pagination? = pagination(),
    ): Resources = Resources(
        data = data,
        pagination = page,
    )

    fun link(
        data: ResourceItem = resourceItem(),
    ): Link = Link(
        data = data,
    )

    fun profileActionsDto(
        blacklist: Boolean = false,
        follow: Boolean = true,
        update: Boolean = false,
        updateGender: Boolean = true,
        updateNote: Boolean = false,
    ): ProfileActionsDto = ProfileActionsDto(
        blacklist = blacklist,
        follow = follow,
        update = update,
        updateGender = updateGender,
        updateNote = updateNote,
    )

    fun profileRankDto(
        position: Int? = 30,
        trend: Int = 5,
    ): ProfileRankDto = ProfileRankDto(
        position = position,
        trend = trend,
    )

    fun entriesDetailsDto(
        added: Int = 10,
        commented: Int = 11,
        voted: Int = 12,
    ): EntriesDetailsDto = EntriesDetailsDto(
        added = added,
        commented = commented,
        voted = voted,
    )

    fun linksDetailsDto(
        added: Int = 20,
        commented: Int = 21,
        down: Int? = 22,
        published: Int = 23,
        related: Int = 24,
        up: Int = 25,
    ): LinksDetailsDto = LinksDetailsDto(
        added = added,
        commented = commented,
        down = down,
        published = published,
        related = related,
        up = up,
    )

    fun summaryDto(
        actions: Int = 1,
        entries: Int = 2,
        entriesDetails: EntriesDetailsDto = entriesDetailsDto(),
        followers: Int = 3,
        followingTags: Int = 4,
        followingUsers: Int = 5,
        links: Int = 6,
        linksDetails: LinksDetailsDto = linksDetailsDto(),
    ): SummaryDto = SummaryDto(
        actions = actions,
        entries = entries,
        entriesDetails = entriesDetails,
        followers = followers,
        followingTags = followingTags,
        followingUsers = followingUsers,
        links = links,
        linksDetails = linksDetails,
    )

    fun socialMediaDto(
        facebook: String = "fb",
        instagram: String = "ig",
        twitter: String = "tw",
    ): SocialMediaDto = SocialMediaDto(
        facebook = facebook,
        instagram = instagram,
        twitter = twitter,
    )

    fun bannedDto(
        expired: String = "never",
        reason: String = "none",
    ): BannedDto = BannedDto(
        expired = expired,
        reason = reason,
    )

    fun profileDataDto(
        about: String = "about",
        actions: ProfileActionsDto = profileActionsDto(),
        avatar: String = "https://example.com/profile.png",
        background: String = "https://example.com/bg.png",
        banned: BannedDto? = null,
        canChangeGender: Boolean? = true,
        city: String = "Krakow",
        color: String = "green",
        company: Boolean = false,
        follow: Boolean = true,
        followers: Int = 10,
        gender: String? = "f",
        memberSince: LocalDateTime? = dateTime,
        name: String = "Display Name",
        note: Boolean = false,
        online: Boolean = true,
        publicEmail: String = "test@example.com",
        rank: ProfileRankDto = profileRankDto(),
        socialMedia: SocialMediaDto = socialMediaDto(),
        status: String = "active",
        summary: SummaryDto = summaryDto(),
        username: String = "profile-user",
        verified: Boolean = true,
        website: String = "https://example.com",
    ): ProfileDataDto = ProfileDataDto(
        about = about,
        actions = actions,
        avatar = avatar,
        background = background,
        banned = banned,
        canChangeGender = canChangeGender,
        city = city,
        color = color,
        company = company,
        follow = follow,
        followers = followers,
        gender = gender,
        memberSince = memberSince,
        name = name,
        note = note,
        online = online,
        publicEmail = publicEmail,
        rank = rank,
        socialMedia = socialMedia,
        status = status,
        summary = summary,
        username = username,
        verified = verified,
        website = website,
    )

    fun profileDto(
        data: ProfileDataDto = profileDataDto(),
    ): ProfileDto = ProfileDto(
        data = data,
    )

    fun profileShortDataDto(
        avatar: String = "https://example.com/profile-short.png",
        blacklist: Boolean = false,
        color: String = "burgundy",
        company: Boolean = true,
        follow: Boolean = false,
        gender: String = "m",
        note: Boolean = false,
        online: Boolean = true,
        rank: ProfileRankDto = profileRankDto(position = 7, trend = 1),
        status: String = "active",
        username: String = "short-user",
        verified: Boolean = true,
    ): ProfileShortDataDto = ProfileShortDataDto(
        avatar = avatar,
        blacklist = blacklist,
        color = color,
        company = company,
        follow = follow,
        gender = gender,
        note = note,
        online = online,
        rank = rank,
        status = status,
        username = username,
        verified = verified,
    )

    fun profileShortDto(
        data: ProfileShortDataDto = profileShortDataDto(),
    ): ProfileShortDto = ProfileShortDto(
        data = data,
    )

    fun observedTagDto(
        name: String = "#kotlin",
        pinned: Boolean = true,
    ): ObservedTagDto = ObservedTagDto(
        name = name,
        pinned = pinned,
    )

    fun observedUsersResponseDto(
        data: List<UserDto> = listOf(userDto()),
        pagination: PaginationDto? = paginationDto(),
    ): ObservedUsersResponseDto = ObservedUsersResponseDto(
        data = data,
        pagination = pagination,
    )

    fun observedTagsResponseDto(
        data: List<ObservedTagDto> = listOf(observedTagDto()),
        pagination: PaginationDto? = paginationDto(),
    ): ObservedTagsResponseDto = ObservedTagsResponseDto(
        data = data,
        pagination = pagination,
    )

    fun usersAutoCompleteDataDto(
        avatar: String = "https://example.com/auto.png",
        color: String = "black",
        gender: String? = "m",
        username: String = "auto-user",
    ): UsersAutoCompleteDataDto = UsersAutoCompleteDataDto(
        avatar = avatar,
        color = color,
        gender = gender,
        username = username,
    )

    fun usersAutoCompleteResponseDto(
        data: List<UsersAutoCompleteDataDto> = listOf(usersAutoCompleteDataDto()),
    ): UsersAutoCompleteResponseDto = UsersAutoCompleteResponseDto(
        data = data,
    )

    fun observedTag(
        name: String = "#kotlin",
        pinned: Boolean = true,
    ): ObservedTag = ObservedTag(
        name = name,
        pinned = pinned,
    )

    fun observedUser(
        username: String = "observed-user",
        avatar: String = "https://example.com/user.png",
        gender: Gender = Gender.Female,
        color: NameColor = NameColor.Green,
        online: Boolean = false,
        company: Boolean = true,
        verified: Boolean = false,
        status: String = "online",
    ): ObservedUser = ObservedUser(
        username = username,
        avatar = avatar,
        gender = gender,
        color = color,
        online = online,
        company = company,
        verified = verified,
        status = status,
    )

    fun observedUsers(
        data: List<ObservedUser> = listOf(observedUser()),
        page: Pagination? = pagination(),
    ): ObservedUsers = ObservedUsers(
        data = data,
        pagination = page,
    )

    fun observedTags(
        data: List<ObservedTag> = listOf(observedTag()),
        page: Pagination? = pagination(),
    ): ObservedTags = ObservedTags(
        data = data,
        pagination = page,
    )

    fun summary(
        actions: Int = 1,
        entries: Int = 2,
        links: Int = 6,
        followers: Int = 3,
        followingTags: Int = 4,
        followingUsers: Int = 5,
    ): Summary = Summary(
        actions = actions,
        entries = entries,
        links = links,
        followers = followers,
        followingTags = followingTags,
        followingUsers = followingUsers,
    )

    fun profile(
        name: String = "profile-user",
        avatarUrl: String = "https://example.com/profile.png",
        gender: Gender = Gender.Female,
        color: NameColor = NameColor.Green,
        backgroundUrl: String = "https://example.com/bg.png",
        profileSummary: Summary = summary(),
        memberSince: LocalDateTime? = dateTime,
    ): Profile = Profile(
        name = name,
        avatarUrl = avatarUrl,
        gender = gender,
        color = color,
        backgroundUrl = backgroundUrl,
        summary = profileSummary,
        memberSince = memberSince,
    )

    fun profileShort(
        name: String = "short-user",
        avatarUrl: String = "https://example.com/profile-short.png",
        gender: Gender = Gender.Male,
        color: NameColor = NameColor.Burgundy,
    ): ProfileShort = ProfileShort(
        name = name,
        avatarUrl = avatarUrl,
        gender = gender,
        color = color,
    )

    fun userAutoCompleteItem(
        username: String = "auto-user",
        avatarUrl: String = "https://example.com/auto.png",
        gender: Gender = Gender.Male,
        color: NameColor = NameColor.Black,
    ): UserAutoCompleteItem = UserAutoCompleteItem(
        username = username,
        avatarUrl = avatarUrl,
        gender = gender,
        color = color,
    )

    fun usersAutoComplete(
        users: List<UserAutoCompleteItem> = listOf(userAutoCompleteItem()),
    ): UsersAutoComplete = UsersAutoComplete(
        users = users,
    )

    fun tagsAutoCompleteDataDto(
        name: String = "#android",
        observedQuantity: Int = 12,
    ): TagsAutoCompleteDataDto = TagsAutoCompleteDataDto(
        name = name,
        observedQuantity = observedQuantity,
    )

    fun tagsAutoCompleteResponseDto(
        data: List<TagsAutoCompleteDataDto> = listOf(tagsAutoCompleteDataDto()),
    ): TagsAutoCompleteResponseDto = TagsAutoCompleteResponseDto(
        data = data,
    )

    fun tagsAutoCompleteItem(
        name: String = "#android",
        observedQuantity: Int = 12,
    ): TagsAutoCompleteItem = TagsAutoCompleteItem(
        name = name,
        observedQuantity = observedQuantity,
    )

    fun tagsAutoComplete(
        tags: List<TagsAutoCompleteItem> = listOf(tagsAutoCompleteItem()),
    ): TagsAutoComplete = TagsAutoComplete(
        tags = tags,
    )
}
