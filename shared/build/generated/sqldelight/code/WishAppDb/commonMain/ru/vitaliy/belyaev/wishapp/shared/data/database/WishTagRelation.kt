package ru.vitaliy.belyaev.wishapp.shared.`data`.database

public data class WishTagRelation(
    public val wishId: String,
    public val tagId: String
) {
    public override fun toString(): String = """
  |WishTagRelation [
  |  wishId: $wishId
  |  tagId: $tagId
  |]
  """.trimMargin()
}
