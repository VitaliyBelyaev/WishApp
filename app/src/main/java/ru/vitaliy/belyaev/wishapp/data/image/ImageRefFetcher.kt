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

/** Картинку могли удалить, пока её запрос ещё выполнялся. Это не ошибка. */
class ImageNotFoundException(imageId: String) : Exception("Image not found: $imageId")

/** Читает байты картинки из базы только на время декодирования. */
class ImageRefFetcher(
    private val image: ImageRef,
    private val options: Options,
    private val imagesRepository: ImagesRepository,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val rawData = imagesRepository.getImageRawData(image.id)
            ?: throw ImageNotFoundException(image.id)
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
