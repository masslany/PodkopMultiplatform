package pl.masslany.podkop.common.pagination

/**
 * Describes the pagination shape expected by a repository or API client call.
 *
 * Wykop does not use one pagination contract everywhere. Older/public-style endpoints use numeric
 * pages, logged-in timeline endpoints return opaque cursors that still have to be sent as `page`,
 * and a few notification endpoints use the same opaque cursor value under `key`. Keeping those
 * cases explicit avoids guessing from the cursor value itself, because numeric-looking cursors can
 * still belong to cursor pagination.
 */
sealed interface PageRequest {
    /**
     * Initial cursor request.
     *
     * Cursor-based endpoints load their first page without a pagination query parameter. Numbered
     * endpoints should use [Number] with value `1` instead.
     */
    data object Initial : PageRequest

    /**
     * Numeric page request sent as `page=<value>`.
     */
    data class Number(val value: Int) : PageRequest

    /**
     * Opaque cursor request sent as `page=<value>`.
     *
     * This is used by logged-in feeds such as entries, links, favourites, observed content, and tag
     * streams. Even though the value comes from `pagination.next`, those endpoints expect the next
     * request to keep using the `page` query parameter.
     */
    data class PageCursor(val value: String) : PageRequest

    /**
     * Opaque cursor request sent as `key=<value>`.
     *
     * Only endpoints verified to use `key` should select this shape. Sending a page cursor as `key`
     * can make some Wykop endpoints return the first page again with the same cursor.
     */
    data class KeyCursor(val value: String) : PageRequest
}

/**
 * Feature-level pagination strategy used to turn a stored `pagination.next` value into the next
 * [PageRequest].
 */
enum class PaginationMode {
    /**
     * Pages are requested by increasing page numbers.
     */
    Numbered,

    /**
     * The first request is unpaged and subsequent cursors are sent as `page`.
     */
    CursorInPage,

    /**
     * The first request is unpaged and subsequent cursors are sent as `key`.
     */
    CursorInKey,
}

/**
 * Returns the first request shape for a pagination mode.
 */
fun PaginationMode.initialRequest(): PageRequest =
    when (this) {
        PaginationMode.Numbered -> PageRequest.Number(1)
        PaginationMode.CursorInPage,
        PaginationMode.CursorInKey,
        -> PageRequest.Initial
    }

fun PaginationMode.nextRequest(
    next: String?,
    nextNumber: Int,
): PageRequest? {
    val cursor = next?.takeIf(String::isNotBlank)
    return when (this) {
        PaginationMode.Numbered -> cursor
            ?.toIntOrNull()
            ?.let(PageRequest::Number)
            ?: PageRequest.Number(nextNumber)

        PaginationMode.CursorInPage -> cursor?.let(PageRequest::PageCursor)
        PaginationMode.CursorInKey -> cursor?.let(PageRequest::KeyCursor)
    }
}

/**
 * Returns the numeric page value only when this request is known to be numbered.
 *
 * Mismatched pagination modes should be handled by the caller as a recoverable no-op rather than
 * crashing the app.
 */
fun PageRequest.numberOrNull(): Int? =
    when (this) {
        is PageRequest.Number -> value
        PageRequest.Initial,
        is PageRequest.PageCursor,
        is PageRequest.KeyCursor,
        -> null
    }
