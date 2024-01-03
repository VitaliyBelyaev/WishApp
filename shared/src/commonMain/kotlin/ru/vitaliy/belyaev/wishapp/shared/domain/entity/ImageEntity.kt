package ru.vitaliy.belyaev.wishapp.shared.domain.entity

data class ImageEntity(
    val id: String,
    val wishId: String,
    val rawData: ByteArray,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ImageEntity

        if (id != other.id) return false
        if (wishId != other.wishId) return false
        return rawData.contentEquals(other.rawData)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + wishId.hashCode()
        result = 31 * result + rawData.contentHashCode()
        return result
    }
}