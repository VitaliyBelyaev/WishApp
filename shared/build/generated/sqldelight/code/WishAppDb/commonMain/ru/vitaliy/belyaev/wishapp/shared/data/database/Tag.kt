package ru.vitaliy.belyaev.wishapp.shared.`data`.database

public data class Tag(
    public val tagId: String,
    public val title: String
) {
    public override fun toString(): String = """
  |Tag [
  |  tagId: $tagId
  |  title: $title
  |]
  """.trimMargin()
}
