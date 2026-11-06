package ru.vitaliy.belyaev.wishapp.shared.domain.repository

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines

interface WishTagRelationRepository {

    @NativeCoroutines
    suspend fun insertWishTagRelation(wishId: String, tagId: String)

    @NativeCoroutines
    suspend fun deleteWishTagRelation(wishId: String, tagId: String)

    /** «Показывать на карточке». Без выбора показан первый добавленный ярлык. */
    @NativeCoroutines
    suspend fun setShownTag(wishId: String, tagId: String)
}