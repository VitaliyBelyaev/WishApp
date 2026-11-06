package ru.vitaliy.belyaev.wishapp.domain.repository

interface BackupLoadProgressListener {

    fun onProgressChanged(progress: Double, currentBytesLoaded: Long)
}