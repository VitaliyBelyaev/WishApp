package ru.vitaliy.belyaev.wishapp.data.repository.wishtagrelation

interface WishTagRelationRepository {

    fun insertWishTagRelation(wishId: String, tagId: String)

    fun deleteWishTagRelation(wishId: String, tagId: String)
}