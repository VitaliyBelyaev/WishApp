package ru.vitaliy.belyaev.wishapp.shared.data

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object NapierProxyKt {

    fun debugBuild() {
        Napier.base(DebugAntilog())
    }
}