package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef

interface ImagesRepository {

    @NativeCoroutines
    suspend fun insertImage(image: ImageEntity)

    @NativeCoroutines
    suspend fun insertImages(images: List<ImageEntity>)

    @NativeCoroutines
    suspend fun getImageRawData(id: String): ByteArray?

    @NativeCoroutines
    fun observeImageRefsByWishId(wishId: String): Flow<List<ImageRef>>

    @NativeCoroutines
    suspend fun deleteImageById(id: String)

    @NativeCoroutines
    suspend fun deleteImagesByIds(ids: List<String>)
}