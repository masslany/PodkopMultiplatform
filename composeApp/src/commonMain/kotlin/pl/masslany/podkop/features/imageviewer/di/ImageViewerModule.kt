package pl.masslany.podkop.features.imageviewer.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.imageviewer.ImageViewerViewModel

val imageViewerModule = module {
    viewModel { params ->
        ImageViewerViewModel(
            imageUrl = params.get<String>(),
            appNavigator = get(),
            imageDownloader = get(),
            snackbarManager = get(),
        )
    }
}
