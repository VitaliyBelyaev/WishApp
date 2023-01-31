package ru.vitaliy.belyaev.wishapp.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform