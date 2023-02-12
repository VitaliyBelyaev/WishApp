package ru.vitaliy.belyaev.wishapp.shared.domain.repository

interface WishTagRelationRepository {

    fun insertWishTagRelation(wishId: String, tagId: String)

    fun deleteWishTagRelation(wishId: String, tagId: String)
}