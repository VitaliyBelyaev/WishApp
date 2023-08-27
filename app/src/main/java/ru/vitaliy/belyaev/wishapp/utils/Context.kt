package ru.vitaliy.belyaev.wishapp.utils

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.core.os.postDelayed
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.ui.AppActivity

fun Context.openGooglePlay() {
    try {
        val uriString = "market://details?id=${BuildConfig.APPLICATION_ID}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
    } catch (e: ActivityNotFoundException) {
        val webUriString = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webUriString)))
    }
}

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterialApi::class
)
fun Context.restartApp() {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val topActivity = activityManager.appTasks[0].taskInfo.topActivity ?: return

    Handler(mainLooper).postDelayed(1500) {
        val intent = Intent(this, AppActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        kotlin.system.exitProcess(0)
    }
}