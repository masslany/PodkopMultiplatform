package pl.masslany.podkop.ios

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import pl.masslany.podkop.business.common.domain.models.common.Actions
import pl.masslany.podkop.business.common.domain.models.common.Answer
import pl.masslany.podkop.business.common.domain.models.common.Author
import pl.masslany.podkop.business.common.domain.models.common.Comments
import pl.masslany.podkop.business.common.domain.models.common.Deleted
import pl.masslany.podkop.business.common.domain.models.common.Embed
import pl.masslany.podkop.business.common.domain.models.common.Gender
import pl.masslany.podkop.business.common.domain.models.common.Media
import pl.masslany.podkop.business.common.domain.models.common.NameColor
import pl.masslany.podkop.business.common.domain.models.common.Photo
import pl.masslany.podkop.business.common.domain.models.common.Rank
import pl.masslany.podkop.business.common.domain.models.common.Resource
import pl.masslany.podkop.business.common.domain.models.common.ResourceItem
import pl.masslany.podkop.business.common.domain.models.common.Survey
import pl.masslany.podkop.business.common.domain.models.common.Voted
import pl.masslany.podkop.business.common.domain.models.common.Votes

class ResourceMappingTest {
    @Test
    fun mapsUnknownDeletedCapabilitiesAndMediaWithoutDroppingFields() {
        val actions = Actions(
            create = false, createFavourite = false, delete = true,
            deleteFavourite = false, finishAma = false, report = true,
            startAma = false, undoVote = true, update = false,
            voteDown = true, voteUp = false, vote = true,
        )
        val item = ResourceItem(
            actions = actions, adult = true, archive = false,
            author = Author(
                avatar = "https://example.com/avatar.png", blacklist = false,
                color = NameColor.Green, company = false, follow = false,
                gender = Gender.Unspecified, note = false, online = true,
                rank = Rank(position = 4, trend = 1), status = "active",
                username = "Ewa", verified = true,
            ),
            comments = Comments(count = 7, hot = false, items = emptyList()),
            content = "tekst", createdAt = null, deleted = Deleted.Moderator,
            deletable = true, description = "opis", editable = false, hot = true,
            id = 77,
            media = Media(
                embed = Embed(key = "t", thumbnail = "https://example.com/thumb.png",
                              type = "twitter", url = "https://x.com/example/status/1"),
                photo = Photo(height = 480, key = "p", label = "foto", mimeType = "image/png",
                              size = 10, url = "https://example.com/p.png", width = 640),
                survey = Survey(
                    actions = actions,
                    answers = listOf(Answer(count = 3, id = 2, text = "Tak", voted = 1)),
                    count = 3, deletable = false, editable = false,
                    key = "s", question = "Pytanie?", voted = 1,
                ),
            ),
            name = "", parent = null, parentId = 42, publishedAt = null,
            recommended = true, resource = Resource.Unknown, slug = "", source = null,
            tags = listOf("nauka"), title = "tytuł", voted = Voted.Negative,
            votes = Votes(count = 6, down = 1, up = 5), favourite = true,
        )

        val mapped = item.toIOSResource()
        assertEquals("unknown", mapped.kind)
        assertEquals("moderator", mapped.deletionReason)
        assertEquals("green", mapped.authorColor)
        assertEquals(4, mapped.authorRank)
        assertEquals("unspecified", mapped.authorGender)
        assertEquals(42, mapped.parentId)
        assertEquals(7, mapped.commentsCount)
        assertEquals("negative", mapped.voted)
        assertFalse(mapped.canVoteUp)
        assertTrue(mapped.canVoteDown)
        assertTrue(mapped.canUndoVote)
        assertTrue(mapped.canDelete)
        assertEquals(640, mapped.photo?.width)
        assertEquals("twitter", mapped.embed?.type)
        assertEquals(1, mapped.survey?.selectedOption)
        assertFalse(mapped.survey?.canVote ?: true)
    }
}
