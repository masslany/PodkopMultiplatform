package pl.masslany.podkop.business.testsupport.fakes

import pl.masslany.podkop.business.observed.data.api.ObservedDataSource
import pl.masslany.podkop.business.observed.data.network.models.ObservedResponseDto
import pl.masslany.podkop.business.observed.domain.models.request.ObservedType

class FakeObservedDataSource : ObservedDataSource {
    data class GetObservedCall(
        val page: Any?,
        val type: ObservedType,
    )

    var getObservedResult: Result<ObservedResponseDto> = unstubbedResult("ObservedDataSource.getObserved")

    val getObservedCalls = mutableListOf<GetObservedCall>()

    override suspend fun getObserved(
        page: Any?,
        type: ObservedType,
    ): Result<ObservedResponseDto> {
        getObservedCalls += GetObservedCall(page = page, type = type)
        return getObservedResult
    }
}
