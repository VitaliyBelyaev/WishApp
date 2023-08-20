package ru.vitaliy.belyaev.wishapp.shared.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.vitaliy.belyaev.wishapp.shared.data.WishAppSdk

class WishAppSdkDiHelper : KoinComponent {

    val wishAppSdk: WishAppSdk by inject()
}