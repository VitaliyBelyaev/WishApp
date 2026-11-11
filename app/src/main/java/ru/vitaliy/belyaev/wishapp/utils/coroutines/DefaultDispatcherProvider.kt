package ru.vitaliy.belyaev.wishapp.utils.coroutines

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Единственное место, где Dispatchers берутся напрямую: отсюда их получает остальной код
@Suppress("InjectDispatcher")
@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override fun main(): CoroutineDispatcher = Dispatchers.Main
    override fun default(): CoroutineDispatcher = Dispatchers.Default
    override fun io(): CoroutineDispatcher = Dispatchers.IO
    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
