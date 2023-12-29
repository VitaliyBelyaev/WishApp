package ru.vitaliy.belyaev.wishapp.domain.model.error

class GoogleSignInException(override val cause: Throwable? = null) : RuntimeException() {

    override val message: String
        get() = "User is not signed in"
}