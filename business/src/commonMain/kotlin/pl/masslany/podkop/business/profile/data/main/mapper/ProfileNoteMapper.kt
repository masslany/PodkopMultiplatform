package pl.masslany.podkop.business.profile.data.main.mapper

import pl.masslany.podkop.business.profile.data.network.models.ProfileNoteResponseDto
import pl.masslany.podkop.business.profile.domain.models.ProfileNote

fun ProfileNoteResponseDto.toProfileNote(): ProfileNote = ProfileNote(
    content = data.content,
)
