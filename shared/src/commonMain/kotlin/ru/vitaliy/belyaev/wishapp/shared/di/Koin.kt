package ru.vitaliy.belyaev.wishapp.shared.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk

// Android
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()

        modules(
            platformModule(),
            wishAppSdkModule
        )
    }

// iOS
fun initKoin() = initKoin {}

val wishAppSdkModule = module {
    single { WishAppSdk(get()) }
}