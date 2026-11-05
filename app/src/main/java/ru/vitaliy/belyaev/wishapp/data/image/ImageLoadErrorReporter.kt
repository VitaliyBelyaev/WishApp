package ru.vitaliy.belyaev.wishapp.data.image

import coil3.EventListener
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/** Пользователь видит вместо картинки только иконку, поэтому причину отправляем в Crashlytics. */
object ImageLoadErrorReporter : EventListener() {

    override fun onError(request: ImageRequest, result: ErrorResult) {
        val error = result.throwable
        if (error is ImageNotFoundException) return

        Timber.e(error)
        FirebaseCrashlytics.getInstance().recordException(error)
    }
}
