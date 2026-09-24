package pl.masslany.podkop.features.pagination

import kotlin.test.Test
import kotlin.test.assertEquals
import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.pagination.PaginationMode

class FeaturePaginationPoliciesTest {

    @Test
    fun `entries use page cursor only for logged in user`() {
        assertEquals(PaginationMode.CursorInPage, FeaturePaginationPolicies.entries(isLoggedIn = true))
        assertEquals(PaginationMode.Numbered, FeaturePaginationPolicies.entries(isLoggedIn = false))
    }

    @Test
    fun `links homepage uses page cursor for logged in user but upcoming stays numbered`() {
        assertEquals(
            PaginationMode.CursorInPage,
            FeaturePaginationPolicies.links(isLoggedIn = true, isUpcoming = false),
        )
        assertEquals(
            PaginationMode.Numbered,
            FeaturePaginationPolicies.links(isLoggedIn = true, isUpcoming = true),
        )
        assertEquals(
            PaginationMode.Numbered,
            FeaturePaginationPolicies.links(isLoggedIn = false, isUpcoming = false),
        )
    }

    @Test
    fun `tag stream uses page cursor only for logged in user`() {
        assertEquals(PaginationMode.CursorInPage, FeaturePaginationPolicies.tagStream(isLoggedIn = true))
        assertEquals(PaginationMode.Numbered, FeaturePaginationPolicies.tagStream(isLoggedIn = false))
    }

    @Test
    fun `favourites use page cursor only for logged in user`() {
        assertEquals(PaginationMode.CursorInPage, FeaturePaginationPolicies.favourites(isLoggedIn = true))
        assertEquals(PaginationMode.Numbered, FeaturePaginationPolicies.favourites(isLoggedIn = false))
    }

    @Test
    fun `notifications observed entries and tags use key cursor but private messages remain numbered`() {
        assertEquals(PaginationMode.CursorInKey, FeaturePaginationPolicies.notifications(NotificationGroup.Entries))
        assertEquals(PaginationMode.CursorInKey, FeaturePaginationPolicies.notifications(NotificationGroup.Tags))
        assertEquals(
            PaginationMode.CursorInKey,
            FeaturePaginationPolicies.notifications(NotificationGroup.ObservedDiscussions),
        )
        assertEquals(
            PaginationMode.Numbered,
            FeaturePaginationPolicies.notifications(NotificationGroup.PrivateMessages),
        )
    }
}
