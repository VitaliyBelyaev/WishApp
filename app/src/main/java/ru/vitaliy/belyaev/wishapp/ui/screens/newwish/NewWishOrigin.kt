package ru.vitaliy.belyaev.wishapp.ui.screens.newwish

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

/**
 * Откуда растёт панель: плюс в доке кладёт сюда свои границы перед открытием. Пока панель открыта
 * или летит назад, [isLifted] равно `true`, и док прячет плюс: панель и есть поднявшийся плюс.
 */
@Stable
class NewWishOrigin {
    /** Границы плюса в координатах корня или `null`, если панель открыли не плюсом. */
    var bounds: Rect? by mutableStateOf(null)
    var isLifted: Boolean by mutableStateOf(false)
        internal set
}
