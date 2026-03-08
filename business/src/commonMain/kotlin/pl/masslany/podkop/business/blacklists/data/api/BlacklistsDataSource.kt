package pl.masslany.podkop.business.blacklists.data.api

interface BlacklistsDataSource {
    suspend fun addBlacklistedUser(username: String): Result<Unit>

    suspend fun removeBlacklistedUser(username: String): Result<Unit>

    suspend fun addBlacklistedTag(tag: String): Result<Unit>

    suspend fun removeBlacklistedTag(tag: String): Result<Unit>
}
