package ru.vitaliy.belyaev.wishapp.ui.screens.test

data class TestScreenState(
    val items: List<TestItem> = emptyList(),
    val selectedIds: List<String> = emptyList()
)