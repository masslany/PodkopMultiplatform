package pl.masslany.podkop.features.resourceactions

import kotlinx.serialization.Serializable
import pl.masslany.podkop.common.navigation.NavTarget

/** Explains how to report [contentUrl] on the website; the app never files a report itself. */
@Serializable
data class ResourceReportDialogScreen(val contentUrl: String) : NavTarget
