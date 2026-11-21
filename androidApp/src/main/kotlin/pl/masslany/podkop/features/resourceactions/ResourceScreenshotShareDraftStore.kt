package pl.masslany.podkop.features.resourceactions

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ResourceScreenshotShareDraftStore {
    private val drafts = mutableMapOf<String, ResourceScreenshotShareDraft>()

    @OptIn(ExperimentalUuidApi::class)
    fun put(draft: ResourceScreenshotShareDraft): String {
        val id = Uuid.random().toString()
        drafts[id] = draft
        return id
    }

    fun get(id: String): ResourceScreenshotShareDraft? = drafts[id]

    fun duplicate(id: String): String? {
        val draft = drafts[id] ?: return null
        return put(draft)
    }

    fun remove(id: String) {
        drafts.remove(id)
    }
}
