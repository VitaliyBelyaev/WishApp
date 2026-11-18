package ru.vitaliy.belyaev.wishapp.ui.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** Activity, к которой привязан контекст, например контекст `View` в Compose. */
internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
