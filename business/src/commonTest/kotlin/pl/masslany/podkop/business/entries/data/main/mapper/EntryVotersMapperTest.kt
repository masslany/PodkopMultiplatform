package pl.masslany.podkop.business.entries.data.main.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.entries.data.network.models.EntryVotersResponseDto
import pl.masslany.podkop.business.entries.domain.models.EntryVoter
import pl.masslany.podkop.business.entries.domain.models.EntryVoters
import pl.masslany.podkop.business.testsupport.fixtures.BusinessFixtures as Fixtures

class EntryVotersMapperTest {

    @Test
    fun `entry voters response mapper maps users and pagination`() {
        val dto = EntryVotersResponseDto(
            data = listOf(
                Fixtures.userDto(
                    username = "u1",
                    avatar = "avatar-1",
                    gender = "m",
                    color = "orange",
                    online = true,
                    company = false,
                    verified = true,
                    status = "active",
                ),
                Fixtures.userDto(
                    username = "u2",
                    avatar = "",
                    gender = null,
                    color = "unknown",
                    online = false,
                    company = true,
                    verified = false,
                    status = "removed",
                ),
            ),
            pagination = Fixtures.paginationDto(perPage = 50, total = 51, next = "2", prev = null),
        )

        assertEquals(
            EntryVoters(
                data = listOf(
                    EntryVoter(
                        username = "u1",
                        avatar = "avatar-1",
                        gender = Gender.Male,
                        color = NameColor.Orange,
                        online = true,
                        company = false,
                        verified = true,
                        status = "active",
                    ),
                    EntryVoter(
                        username = "u2",
                        avatar = "",
                        gender = Gender.Unspecified,
                        color = NameColor.Orange,
                        online = false,
                        company = true,
                        verified = false,
                        status = "removed",
                    ),
                ),
                pagination = Fixtures.pagination(perPage = 50, total = 51, next = "2", prev = ""),
            ),
            dto.toEntryVoters(),
        )
    }

    @Test
    fun `entry voters response mapper supports null pagination`() {
        val dto = EntryVotersResponseDto(
            data = listOf(Fixtures.userDto(username = "solo")),
            pagination = null,
        )

        assertEquals(
            EntryVoters(
                data = listOf(
                    EntryVoter(
                        username = "solo",
                        avatar = "https://example.com/user.png",
                        gender = Gender.Female,
                        color = NameColor.Green,
                        online = false,
                        company = true,
                        verified = false,
                        status = "online",
                    ),
                ),
                pagination = null,
            ),
            dto.toEntryVoters(),
        )
    }
}
