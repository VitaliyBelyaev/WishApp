package ru.vitaliy.belyaev.wishapp.ui.main

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import ru.vitaliy.belyaev.wishapp.entity.Wish

class MainViewModel : ViewModel() {

    var wishItems = mutableStateListOf<Wish>()
        private set


    init {
        val items = listOf(
            Wish("1", "Value 1"),
            Wish("2", "Value 1"),
            Wish("3", "Value 1"),
            Wish("4", "Value 1"),
            Wish("5", "Value 1"),
            Wish("6", "Value 1"),
            Wish("7", "Value 1"),
            Wish("8", "Value 1"),
            Wish("9", "Value 1"),
            Wish("10", "Value 1"),
        )
        wishItems.addAll(items)
    }

    fun addItem(item: Wish) {
        wishItems.add(item)
    }

    fun removeItem(item: Wish) {
        wishItems.remove(item)
    }

    fun onItemClicked(item: Wish) {

    }
}
