package ru.vitaliy.belyaev.wishapp.ui.screens.backup

interface BackupLoadProgressListener {

    fun onProgressChanged(progress: Double, currentBytesLoaded: Long)
}