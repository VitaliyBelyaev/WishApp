package ru.vitaliy.belyaev.wishapp.shared.di

import org.koin.core.module.Module
import org.koin.dsl.module
import ru.vitaliy.belyaev.wishapp.shared.data.database.DatabaseDriverFactory

actual fun platformModule(): Module = module {
    single { DatabaseDriverFactory(get()) }
}