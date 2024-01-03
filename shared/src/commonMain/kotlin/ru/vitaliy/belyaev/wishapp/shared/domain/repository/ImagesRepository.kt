package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageEntity

interface ImagesRepository {

    @NativeCoroutines
    suspend fun insertImage(image: ImageEntity)

    @NativeCoroutines
    suspend fun getImageById(id: String): ImageEntity

    @NativeCoroutines
    suspend fun getAllImages(): List<ImageEntity>

    @NativeCoroutines
    suspend fun getImagesByWishId(wishId: String): List<ImageEntity>

    @NativeCoroutines
    fun observeImagesByWishId(wishId: String): Flow<List<ImageEntity>>

    @NativeCoroutines
    suspend fun deleteImageById(id: String)

    @NativeCoroutines
    suspend fun deleteImagesByIds(ids: List<String>)
}