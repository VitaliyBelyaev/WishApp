package ru.vitaliy.belyaev.wishapp.domain.model

import android.content.res.Resources
import android.os.Build
import ru.vitaliy.belyaev.wishapp.BuildConfig
import ru.vitaliy.belyaev.wishapp.R

data class AppFeedback(
    val email: String,
    val subject: String,
    val message: String
)

fun createAppFeedback(resources: Resources): AppFeedback {
    val message = StringBuilder().apply {
        append("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
        append("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE}\n")
    }
    val appName = resources.getString(R.string.app_name)
    return AppFeedback(
        email = "vitaliy.belyaev.wishapp@gmail.com",
        subject = resources.getString(R.string.feedback_subject_pattern, appName),
        message = message.toString()
    )
}