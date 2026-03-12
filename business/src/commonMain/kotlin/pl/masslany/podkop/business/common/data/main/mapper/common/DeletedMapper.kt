package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.domain.models.common.Deleted


fun String?.toDeleted(): Deleted {
    return when (this) {
        "moderator" -> Deleted.Moderator
        "author" -> Deleted.Author
        "host" -> Deleted.Host
        else -> Deleted.None
    }
}
