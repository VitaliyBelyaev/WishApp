package ru.vitaliy.belyaev.wishapp.utils

import android.os.Build

val isAndroidVersionSOrAbove: Boolean
    get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S