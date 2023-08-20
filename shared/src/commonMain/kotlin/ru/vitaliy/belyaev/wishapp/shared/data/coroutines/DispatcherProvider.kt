package ru.vitaliy.belyaev.wishapp.shared.data.coroutines

import kotlinx.coroutines.CoroutineDispatcher

interface DispatcherProvider {
    fun main(): CoroutineDispatcher
    fun default(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun unconfined(): CoroutineDispatcher
}

expect fun getDispatcherProvider(): DispatcherProvider
