package ru.vitaliy.belyaev.wishapp.model.repository.wishtagrelation

interface WishTagRelationRepository {

    fun insertWishTagRelation(wishId: String, tagId: Long)

    fun deleteWishTagRelation(wishId: String, tagId: Long)
}