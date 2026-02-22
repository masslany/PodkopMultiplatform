package pl.masslany.podkop.business.testsupport.fakes

internal fun <T> unstubbedResult(method: String): Result<T> {
    return Result.failure(IllegalStateException("No stub configured for $method"))
}
