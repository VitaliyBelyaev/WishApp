package ru.vitaliy.belyaev.wishapp.utils

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult

fun ActivityResult.getDataOrNull(): Intent? {
    return when (resultCode) {
        Activity.RESULT_OK -> data
        else -> null
    }
}