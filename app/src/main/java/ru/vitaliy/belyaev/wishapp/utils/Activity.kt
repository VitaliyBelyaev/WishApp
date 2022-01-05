package ru.vitaliy.belyaev.wishapp.utils

import android.app.Activity
import androidx.annotation.ColorInt

fun Activity.setStatusBarColor(@ColorInt color: Int) {
    window.statusBarColor = color
}
