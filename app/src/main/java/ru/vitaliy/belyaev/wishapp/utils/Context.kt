package ru.vitaliy.belyaev.wishapp.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.vitaliy.belyaev.wishapp.BuildConfig

fun Context.openGooglePlay() {
    try {
        val uriString = "market://details?id=${BuildConfig.APPLICATION_ID}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
    } catch (e: ActivityNotFoundException) {
        val webUriString = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUriString)))
    }
}