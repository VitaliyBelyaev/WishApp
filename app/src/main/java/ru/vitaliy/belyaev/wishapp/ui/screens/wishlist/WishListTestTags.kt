package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

/** Теги для UI-тестов экрана списка: у этих элементов нет ни текста, ни описания для TalkBack. */
object WishListTestTags {
    /** Сетка или список хотелок. */
    const val LIST = "wishList.list"

    /** Точка на «⋮»: сортировка раздела не по умолчанию. */
    const val SORT_DOT = "wishList.sortDot"

    /** Такая же точка на пункте «Сортировка» в меню «⋮». */
    const val MENU_SORT_DOT = "wishList.menuSortDot"

    /** Режим выбора: крестик «Снять выбор», счётчик и кнопки панели. */
    const val SELECTION_CLEAR = "wishList.selectionClear"
    const val SELECTION_COUNT = "wishList.selectionCount"
    const val SELECTION_DONE = "wishList.selectionDone"
    const val SELECTION_DELETE = "wishList.selectionDelete"

    /** Столбик меню «Поделиться»: по нему видно, сверху меню или над панелью выбора. */
    const val SHARE_MENU = "wishList.shareMenu"
}
