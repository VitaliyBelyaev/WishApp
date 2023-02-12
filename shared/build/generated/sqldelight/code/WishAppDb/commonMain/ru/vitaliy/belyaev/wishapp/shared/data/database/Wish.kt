package ru.vitaliy.belyaev.wishapp.shared.`data`.database

public data class Wish(
    public val wishId: String,
    public val title: String,
    public val link: String,
    public val comment: String,
    public val isCompleted: Boolean,
    public val createdTimestamp: Long,
    public val updatedTimestamp: Long,
    public val position: Long
) {
    public override fun toString(): String = """
  |Wish [
  |  wishId: $wishId
  |  title: $title
  |  link: $link
  |  comment: $comment
  |  isCompleted: $isCompleted
  |  createdTimestamp: $createdTimestamp
  |  updatedTimestamp: $updatedTimestamp
  |  position: $position
  |]
  """.trimMargin()
}
