package ru.vitaliy.belyaev.wishapp.domain.model

data class ImageData(
    val rawData: ByteArray,
    val rotationDegrees: Int,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (!rawData.contentEquals(other.rawData)) return false
        return rotationDegrees == other.rotationDegrees
    }

    override fun hashCode(): Int {
        var result = rawData.contentHashCode()
        result = 31 * result + rotationDegrees
        return result
    }
}