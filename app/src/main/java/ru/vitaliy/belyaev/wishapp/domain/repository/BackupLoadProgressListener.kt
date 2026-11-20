package ru.vitaliy.belyaev.wishapp.domain.repository

fun interface BackupLoadProgressListener {

    fun onProgressChanged(progress: Double, currentBytesLoaded: Long)
}
