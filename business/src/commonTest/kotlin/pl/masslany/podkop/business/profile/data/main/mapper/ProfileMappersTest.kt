package pl.masslany.podkop.business.profile.data.main.mapper

import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class ProfileMappersTest {

    @Test
    fun `observed tag mapper maps fields`() {
        val dto = Fixtures.observedTagDto(name = "#compose", pinned = false)

        assertEquals(Fixtures.observedTag(name = "#compose", pinned = false), dto.toObservedTag())
    }

    @Test
    fun `observed user mapper maps user dto and enum conversions`() {
        val dto = Fixtures.userDto(
            username = "alice",
            avatar = "avatar",
            gender = null,
            color = "black",
            online = true,
            company = false,
            verified = true,
            status = "busy",
        )

        assertEquals(
            Fixtures.observedUser(
                username = "alice",
                avatar = "avatar",
                gender = Gender.Unspecified,
                color = NameColor.Black,
                online = true,
                company = false,
                verified = true,
                status = "busy",
            ),
            dto.toObservedUser(),
        )
    }

    @Test
    fun `observed users response mapper maps data and pagination`() {
        val dto = Fixtures.observedUsersResponseDto(
            data = listOf(Fixtures.userDto(username = "u1"), Fixtures.userDto(username = "u2")),
            pagination = Fixtures.paginationDto(perPage = 10, total = 15, next = "/n", prev = null),
        )

        assertEquals(
            Fixtures.observedUsers(
                data = listOf(
                    Fixtures.observedUser(username = "u1"),
                    Fixtures.observedUser(username = "u2"),
                ),
                page = Fixtures.pagination(perPage = 10, total = 15, next = "/n", prev = ""),
            ),
            dto.toObservedUsers(),
        )
    }

    @Test
    fun `observed tags response mapper maps data and nullable pagination`() {
        val dto = Fixtures.observedTagsResponseDto(
            data = listOf(
                Fixtures.observedTagDto(name = "#a", pinned = true),
                Fixtures.observedTagDto(name = "#b", pinned = false),
            ),
            pagination = null,
        )

        assertEquals(
            Fixtures.observedTags(
                data = listOf(
                    Fixtures.observedTag(name = "#a", pinned = true),
                    Fixtures.observedTag(name = "#b", pinned = false),
                ),
                page = null,
            ),
            dto.toObservedTags(),
        )
    }

    @Test
    fun `summary mapper maps exposed counters only`() {
        val dto = Fixtures.summaryDto(
            actions = 10,
            entries = 11,
            followers = 12,
            followingTags = 13,
            followingUsers = 14,
            links = 15,
            entriesDetails = Fixtures.entriesDetailsDto(added = 99, commented = 98, voted = 97),
            linksDetails = Fixtures.linksDetailsDto(added = 96, commented = 95, published = 94, related = 93, up = 92),
        )

        assertEquals(
            Fixtures.summary(
                actions = 10,
                entries = 11,
                followers = 12,
                followingTags = 13,
                followingUsers = 14,
                links = 15,
            ),
            dto.toSummary(),
        )
    }

    @Test
    fun `profile short mapper maps username avatar and enum conversions`() {
        val dto = Fixtures.profileShortDto(
            data = Fixtures.profileShortDataDto(
                username = "short-handle",
                avatar = "short-avatar",
                color = "green",
                gender = "f",
            ),
        )

        assertEquals(
            Fixtures.profileShort(
                name = "short-handle",
                avatarUrl = "short-avatar",
                gender = Gender.Female,
                color = NameColor.Green,
            ),
            dto.toProfileShort(),
        )
    }

    @Test
    fun `profile mapper maps username not display name and summary`() {
        val summaryDto = Fixtures.summaryDto(actions = 1, entries = 2, followers = 3, followingTags = 4, followingUsers = 5, links = 6)
        val data = Fixtures.profileDataDto(
            username = "handle",
            name = "Display Name",
            avatar = "avatar-url",
            background = "bg-url",
            blacklist = true,
            follow = false,
            actions = Fixtures.profileActionsDto(blacklist = false, follow = true),
            color = "burgundy",
            gender = null,
            memberSince = Fixtures.dateTime,
            summary = summaryDto,
        )
        val dto = Fixtures.profileDto(data = data)

        assertEquals(
            Fixtures.profile(
                name = "handle",
                avatarUrl = "avatar-url",
                gender = Gender.Unspecified,
                color = NameColor.Burgundy,
                backgroundUrl = "bg-url",
                profileSummary = Fixtures.summary(actions = 1, entries = 2, followers = 3, followingTags = 4, followingUsers = 5, links = 6),
                memberSince = Fixtures.dateTime,
                isObserved = false,
                isBlacklisted = true,
                canManageObservation = true,
            ),
            dto.toProfile(),
        )
    }

    @Test
    fun `profile badges mapper maps icon and metadata`() {
        val dto = Fixtures.profileBadgesResponseDto(
            data = listOf(
                Fixtures.profileBadgeDto(
                    label = "Odznaka",
                    slug = "odznaka-1",
                    description = "Opis odznaki",
                    media = Fixtures.profileBadgeMediaDto(
                        icon = Fixtures.profileBadgeIconDto(
                            url = "https://example.com/odznaka.gif",
                            mimeType = "image/gif",
                        ),
                    ),
                    color = Fixtures.profileBadgeColorDto(hex = "123456", hexDark = "654321"),
                    level = 3,
                    progress = 80,
                    achievedAt = LocalDateTime.parse("2024-02-03T10:11:12"),
                ),
            ),
        )

        assertEquals(
            listOf(
                Fixtures.profileBadge(
                    label = "Odznaka",
                    slug = "odznaka-1",
                    description = "Opis odznaki",
                    iconUrl = "https://example.com/odznaka.gif",
                    iconMimeType = "image/gif",
                    colorHex = "123456",
                    colorHexDark = "654321",
                    level = 3,
                    progress = 80,
                    achievedAt = LocalDateTime.parse("2024-02-03T10:11:12"),
                ),
            ),
            dto.toProfileBadges(),
        )
    }

    @Test
    fun `profile note mapper maps content only`() {
        val dto = Fixtures.profileNoteResponseDto(content = "Dobry człowiek")

        assertEquals(
            Fixtures.profileNote(content = "Dobry człowiek"),
            dto.toProfileNote(),
        )
    }

    @Test
    fun `users auto complete mapper maps items and fallback conversions`() {
        val dto = Fixtures.usersAutoCompleteResponseDto(
            data = listOf(
                Fixtures.usersAutoCompleteDataDto(username = "u1", color = "black", gender = "m"),
                Fixtures.usersAutoCompleteDataDto(username = "u2", color = "unknown", gender = null),
            ),
        )

        assertEquals(
            Fixtures.usersAutoComplete(
                users = listOf(
                    Fixtures.userAutoCompleteItem(username = "u1", color = NameColor.Black, gender = Gender.Male),
                    Fixtures.userAutoCompleteItem(username = "u2", color = NameColor.Orange, gender = Gender.Unspecified),
                ),
            ),
            dto.toUsersAutoComplete(),
        )
    }
}
