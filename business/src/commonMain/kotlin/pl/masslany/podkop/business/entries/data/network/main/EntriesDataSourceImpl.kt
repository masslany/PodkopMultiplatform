package pl.masslany.podkop.business.entries.data.network.main

import pl.masslany.podkop.business.common.data.network.models.common.ResourceResponseDto
import pl.masslany.podkop.business.common.data.network.models.common.SingleResourceResponseDto
import pl.masslany.podkop.business.entries.data.api.EntriesDataSource
import pl.masslany.podkop.business.entries.data.network.api.EntriesApi


class EntriesDataSourceImpl(
    private val entriesApi: EntriesApi,
) : EntriesDataSource {
    override suspend fun getEntries(
        page: Any?,
        limit: Int?,
        sort: String,
        hotSort: Int,
        category: String?,
        bucket: String?,
    ): Result<ResourceResponseDto> {
        return entriesApi.getEntries(page, limit, sort, hotSort, category, bucket)
    }

    override suspend fun getEntry(entryId: Int): Result<SingleResourceResponseDto> {
        return entriesApi.getEntry(entryId)
    }

    override suspend fun getEntryComments(
        entryId: Int,
        page: Any?,
    ): Result<ResourceResponseDto> {
        return entriesApi.getEntryComments(entryId, page)
    }

    override suspend fun voteUp(entryId: Int): Result<Unit> {
        return entriesApi.voteUp(entryId)
    }

    override suspend fun removeVoteUp(entryId: Int): Result<Unit> {
        return entriesApi.removeVoteUp(entryId)
    }
}
