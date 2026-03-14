package pl.masslany.podkop.features.resourceactions.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.resourceactions.ResourceActionUpdatesStore
import pl.masslany.podkop.features.resourceactions.ResourceActionsBottomSheetViewModel
import pl.masslany.podkop.features.resourceactions.ResourceActionsParams
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotPreviewDialogViewModel
import pl.masslany.podkop.features.resourceactions.ResourceScreenshotShareDraftStore
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogParams
import pl.masslany.podkop.features.resourceactions.ResourceTextSelectionDialogViewModel
import pl.masslany.podkop.features.resourceactions.ResourceVotesBottomSheetViewModel
import pl.masslany.podkop.features.resourceactions.ResourceVotesParams

val resourceActionsModule = module {
    single { ResourceScreenshotShareDraftStore() }
    single { ResourceActionUpdatesStore() }

    viewModel { params ->
        ResourceScreenshotPreviewDialogViewModel(
            draftId = params.get<String>(),
            draftStore = get(),
            appNavigator = get(),
            screenshotExporter = get(),
            snackbarManager = get(),
        )
    }

    viewModel { params ->
        val dialogParams = params.get<ResourceTextSelectionDialogParams>()
        ResourceTextSelectionDialogViewModel(
            content = dialogParams.content,
            previewDraftId = dialogParams.previewDraftId,
            draftStore = get(),
            appNavigator = get(),
            snackbarManager = get(),
        )
    }

    viewModel { params ->
        ResourceActionsBottomSheetViewModel(
            params = params.get<ResourceActionsParams>(),
            authRepository = get(),
            entriesRepository = get(),
            appNavigator = get(),
            snackbarManager = get(),
            screenshotShareDraftStore = get(),
            resourceActionUpdatesStore = get(),
        )
    }

    viewModel { params ->
        ResourceVotesBottomSheetViewModel(
            params = params.get<ResourceVotesParams>(),
            entriesRepository = get(),
            linksRepository = get(),
            appNavigator = get(),
            logger = get(),
            snackbarManager = get(),
        )
    }
}
