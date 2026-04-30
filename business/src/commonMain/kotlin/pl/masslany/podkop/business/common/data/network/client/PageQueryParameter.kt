package pl.masslany.podkop.business.common.data.network.client

import pl.masslany.podkop.common.pagination.PageRequest

/**
 * Converts a typed pagination request into the concrete query parameter expected by Wykop.
 *
 * The parameter name is part of the endpoint contract, not a property of the cursor value, so the
 * caller must choose the correct [PageRequest] variant before reaching the API client.
 */
internal fun paginationQueryParameter(pageRequest: PageRequest): Pair<String, String>? =
    when (pageRequest) {
        PageRequest.Initial -> null
        is PageRequest.Number -> "page" to pageRequest.value.toString()
        is PageRequest.PageCursor -> "page" to pageRequest.value
        is PageRequest.KeyCursor -> "key" to pageRequest.value
    }

internal fun MutableMap<String, String>.putPagination(pageRequest: PageRequest) {
    paginationQueryParameter(pageRequest)?.let { (name, value) ->
        put(name, value)
    }
}
