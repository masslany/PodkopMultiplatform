package pl.masslany.podkop.common.network.api

/** Implemented by failures that carry the HTTP status returned by the Wykop API. */
interface HttpStatusFailure {
    val statusCode: Int
}
