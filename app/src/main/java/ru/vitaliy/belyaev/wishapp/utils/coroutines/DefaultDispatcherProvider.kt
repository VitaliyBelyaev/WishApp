package ru.vitaliy.belyaev.wishapp.utils.coroutines

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Singleton
class DefaultDispatcherProvider @Inject constructor() : DispatcherProvider {
    override fun main(): CoroutineDispatcher = Dispatchers.Main
    override fun default(): CoroutineDispatcher = Dispatchers.Default
    override fun io(): CoroutineDispatcher = Dispatchers.IO
    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}