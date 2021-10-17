package ru.vitaliy.belyaev.wishapp.entity

data class Wish(
    val id: String,
    val value: String,
    val comment: String = "",
    val link: String = "",
)