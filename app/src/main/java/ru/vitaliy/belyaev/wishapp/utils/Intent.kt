package ru.vitaliy.belyaev.wishapp.utils

import android.content.Intent
import android.net.Uri

fun createSendEmailIntent(to: String, subject: String, body: String): Intent {
    return Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
}