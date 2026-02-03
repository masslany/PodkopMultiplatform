package pl.masslany.podkop.business.entries.data.api

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto


interface EntriesDataSource {
    @Suppress("LongParameterList")
    suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto>

    suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto>

    suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto>

    suspend fun voteUp(entryId: Int): Result<Unit>

    suspend fun removeVoteUp(entryId: Int): Result<Unit>
}
