package ru.vitaliy.belyaev.wishapp.ui.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.vitaliy.belyaev.wishapp.BuildConfig
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        if (BuildConfig.DEBUG) {
            throw throwable
        }
        FirebaseCrashlytics.getInstance().recordException(throwable)
        Timber.e(throwable.toString())
    }

    protected fun launchSafe(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend () -> Unit,
    ): Job {
        return viewModelScope.launch(context + exceptionHandler) {
            block.invoke()
        }
    }
}