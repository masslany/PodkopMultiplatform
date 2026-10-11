package pl.masslany.podkop.test.entries

import pl.masslany.podkop.test.fixtures.ResourceFixtures
import pl.masslany.podkop.test.fixtures.withoutImages
import pl.masslany.podkop.test.support.MockApiServer

object EntryRoutes {
    const val ENTRIES_PATH = "/api/v3/entries"

    fun entryText(entryId: Int): String = "Entry $entryId"

    /**
     * An entry as a signed-in user opens it: who is viewing (`profile/short`), the entry, whose
     * content is [entryText], and its flat comments (the default) in numbered pages of 50.
     */
    fun MockApiServer.signedInEntryDetails(entryId: Int) {
        getJson(
            path = "/api/v3/profile/short",
            body = sample("profile-short-user").withoutImages().toString(),
        )
        getJson(
            path = "$ENTRIES_PATH/$entryId",
            body = ResourceFixtures.withId(sample("entry-details-user"), entryId, content = entryText(entryId)),
        )
        getJson(
            path = "$ENTRIES_PATH/$entryId/comments",
            query = mapOf("page" to "1"),
            body = ResourceFixtures.numberedPage(sample("entry-comments-user"), page = 1, firstId = 5001),
        )
    }
}
