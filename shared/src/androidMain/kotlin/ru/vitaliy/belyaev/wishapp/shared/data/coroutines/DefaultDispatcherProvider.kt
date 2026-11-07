package ru.vitaliy.belyaev.wishapp.shared.data.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Единственное место, где Dispatchers берутся напрямую: отсюда их получает остальной код
@Suppress("InjectDispatcher")
object DefaultDispatcherProvider : DispatcherProvider {

    override fun main(): CoroutineDispatcher = Dispatchers.Main

    override fun default(): CoroutineDispatcher = Dispatchers.Default

    override fun io(): CoroutineDispatcher = Dispatchers.IO

    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}

actual fun getDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider
