package ru.vitaliy.belyaev.wishapp.data.image

import coil3.ImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.Options
import dagger.Lazy
import okio.Buffer
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.repository.ImagesRepository

/** Читает байты картинки из базы только на время декодирования. */
class ImageRefFetcher(
    private val image: ImageRef,
    private val options: Options,
    private val imagesRepository: ImagesRepository,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val rawData = imagesRepository.getImageRawData(image.id)
            ?: error("Image not found: ${image.id}")
        return SourceFetchResult(
            source = ImageSource(Buffer().write(rawData), options.fileSystem),
            mimeType = null,
            dataSource = DataSource.DISK,
        )
    }

    class Factory(private val imagesRepository: Lazy<ImagesRepository>) : Fetcher.Factory<ImageRef> {

        override fun create(data: ImageRef, options: Options, imageLoader: ImageLoader): Fetcher {
            return ImageRefFetcher(data, options, imagesRepository.get())
        }
    }
}
