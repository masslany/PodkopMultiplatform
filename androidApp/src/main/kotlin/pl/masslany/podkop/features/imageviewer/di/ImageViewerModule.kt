package pl.masslany.podkop.features.imageviewer.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import pl.masslany.podkop.features.imageviewer.DefaultImageExportService
import pl.masslany.podkop.features.imageviewer.ImageExportService
import pl.masslany.podkop.features.imageviewer.ImageViewerViewModel

val imageViewerModule = module {
    single<ImageExportService> { DefaultImageExportService(imageClipboard = get(), imageDownloader = get()) }
    viewModel { params ->
        ImageViewerViewModel(
            imageUrl = params.get<String>(),
            appNavigator = get(),
            imageExportService = get(),
            snackbarManager = get(),
            dismissAfterAction = params.getOrNull<Boolean>() ?: false,
        )
    }
}
