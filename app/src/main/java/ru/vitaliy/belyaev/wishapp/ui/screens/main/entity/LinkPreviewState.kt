package ru.vitaliy.belyaev.wishapp.ui.screens.main.entity

sealed class LinkPreviewState

object Loading : LinkPreviewState()

object None : LinkPreviewState()

object NoData : LinkPreviewState()

data class Data(val linkInfo: LinkInfo) : LinkPreviewState()
