package ru.vitaliy.belyaev.wishapp.entity

data class AppFeedback(
    val feedbackEmail: String = "vitaliy.belyaev.wishapp@gmail.com",
    val subject: String = "Feedback",
    val feedbackMessage: String
)
