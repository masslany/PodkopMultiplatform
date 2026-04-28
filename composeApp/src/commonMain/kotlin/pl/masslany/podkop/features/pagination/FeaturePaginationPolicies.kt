package pl.masslany.podkop.features.pagination

import pl.masslany.podkop.business.notifications.domain.models.NotificationGroup
import pl.masslany.podkop.common.pagination.PaginationMode

/**
 * Centralizes the endpoint-specific pagination modes used by screens.
 *
 * The API mixes numbered pages and cursor pagination depending on endpoint and authentication
 * state. Keeping that matrix here makes view models ask only "which feature am I loading?" while
 * the shared paginator handles the actual [PageRequest][pl.masslany.podkop.common.pagination.PageRequest]
 * generation.
 */
internal object FeaturePaginationPolicies {
    /**
     * Logged-in entries use opaque cursors under `page`; anonymous entries still use page numbers.
     */
    fun entries(isLoggedIn: Boolean): PaginationMode =
        if (isLoggedIn) PaginationMode.CursorInPage else PaginationMode.Numbered

    /**
     * Homepage links switch to `page` cursors for logged-in users, but upcoming links remain
     * numbered even when authenticated.
     */
    fun links(
        isLoggedIn: Boolean,
        isUpcoming: Boolean,
    ): PaginationMode =
        if (isLoggedIn && !isUpcoming) PaginationMode.CursorInPage else PaginationMode.Numbered

    /**
     * Logged-in tag streams return an opaque `pagination.next`, but the next request must send it as
     * `page`. Sending it as `key` was observed to repeat the first page with the same cursor.
     */
    fun tagStream(isLoggedIn: Boolean): PaginationMode =
        if (isLoggedIn) PaginationMode.CursorInPage else PaginationMode.Numbered

    /**
     * Logged-in favourites use the same unpaged-first-request and `page` cursor flow as logged-in
     * feeds; anonymous favourites remain numbered.
     */
    fun favourites(isLoggedIn: Boolean): PaginationMode =
        if (isLoggedIn) PaginationMode.CursorInPage else PaginationMode.Numbered

    /**
     * Notification groups are split by endpoint behavior: private messages are numbered, while
     * entries, tag notifications, and observed discussions use opaque cursors under `key`.
     */
    fun notifications(group: NotificationGroup): PaginationMode =
        when (group) {
            NotificationGroup.PrivateMessages -> PaginationMode.Numbered

            NotificationGroup.Entries,
            NotificationGroup.Tags,
            NotificationGroup.ObservedDiscussions,
            -> PaginationMode.CursorInKey
        }
}
