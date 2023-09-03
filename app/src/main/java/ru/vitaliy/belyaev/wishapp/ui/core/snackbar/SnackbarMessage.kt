package ru.vitaliy.belyaev.wishapp.ui.core.snackbar

sealed class SnackbarMessage {

    sealed class StringResInt : SnackbarMessage() {

        abstract val value: Int

        data class Message(override val value: Int) : StringResInt()
    }

    sealed class StringValue : SnackbarMessage() {

        abstract val value: String

        data class Message(override val value: String) : StringValue()
    }
}
