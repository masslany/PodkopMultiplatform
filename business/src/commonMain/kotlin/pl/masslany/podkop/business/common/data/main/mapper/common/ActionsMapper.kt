package pl.masslany.podkop.business.common.data.main.mapper.common

import pl.masslany.podkop.business.common.data.network.models.common.ActionsDto
import pl.masslany.podkop.business.common.domain.models.common.Actions


fun ActionsDto.toActions(): Actions {
    return Actions(
        create = this.create ?: false,
        createFavourite = this.createFavourite ?: false,
        delete = this.delete ?: false,
        deleteFavourite = this.deleteFavourite ?: false,
        finishAma = this.finishAma ?: false,
        report = this.report ?: false,
        startAma = this.startAma ?: false,
        undoVote = this.undoVote ?: false,
        update = this.update ?: false,
        voteDown = this.voteDown ?: false,
        voteUp = this.voteUp ?: false,
    )
}
