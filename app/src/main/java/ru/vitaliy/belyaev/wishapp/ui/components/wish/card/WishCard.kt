package ru.vitaliy.belyaev.wishapp.ui.components.wish.card

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.wish.CompletedSticker
import ru.vitaliy.belyaev.wishapp.ui.components.wish.FadingText
import ru.vitaliy.belyaev.wishapp.ui.components.wish.HeadingEndPadding
import ru.vitaliy.belyaev.wishapp.ui.components.wish.LinkLine
import ru.vitaliy.belyaev.wishapp.ui.components.wish.SelectedCheckInset
import ru.vitaliy.belyaev.wishapp.ui.components.wish.TagsLine
import ru.vitaliy.belyaev.wishapp.ui.components.wish.TextFade
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishCardModel
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishItemFrame
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishItemText
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishPhoto
import ru.vitaliy.belyaev.wishapp.ui.components.wish.WishSelection
import ru.vitaliy.belyaev.wishapp.ui.components.wish.hasTags
import ru.vitaliy.belyaev.wishapp.ui.components.wish.resolveText
import ru.vitaliy.belyaev.wishapp.ui.components.wish.wishDescription
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.bodyS
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

private const val TITLE_MAX_LINES = 4
private const val COMMENT_MAX_LINES = 4

/**
 * Карточка хотелки в сетке: без рамки и тени, фон `paper-*`. Спецификация: компонент `WishCard`
 * в артефакте дизайн-системы.
 *
 * Сверху фото 1:1 с центр-кропом, ниже наклейка «Сбылось», заголовок (до 4 строк), комментарий
 * (до 4 строк), ссылка и ярлык на отдельных строках. Длинный текст растворяется, а не обрезается
 * многоточием. Сбывшиеся не зачёркиваются. Если есть только фото и нет ярлыка, карточка это квадрат фото.
 *
 * У выбранной обводка `primary` поверх фото и галочка справа сверху, счётчик фото уходит левее неё.
 * У невыбранной в режиме выбора ничего не меняется. Размер и раскладка при выборе те же: у заголовка
 * всегда есть отступ справа под галочку.
 */
@Composable
fun WishCard(
    model: WishCardModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    selection: WishSelection = WishSelection.NONE,
) {
    val text = model.resolveText()
    WishItemFrame(
        model = model,
        shape = MaterialTheme.shapes.large,
        selection = selection,
        description = wishDescription(model),
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (model.photos.isNotEmpty()) {
                CardPhoto(photos = model.photos, isSelected = selection == WishSelection.SELECTED)
            }
            val hasBody = model.isCompleted || text.heading.isNotEmpty() || text.links != null || model.hasTags
            if (hasBody) {
                CardBody(model = model, text = text)
            }
        }
    }
}

@Composable
private fun CardBody(model: WishCardModel, text: WishItemText) {
    Column(modifier = Modifier.padding(WishAppSpacing.s3)) {
        if (model.isCompleted) {
            CompletedSticker(modifier = Modifier.padding(bottom = WishAppSpacing.s2))
        }
        if (text.heading.isNotEmpty()) {
            FadingText(
                text = text.heading,
                style = MaterialTheme.typography.titleM,
                maxLines = TITLE_MAX_LINES,
                fade = TextFade.Bottom(fadeFrom = 0.6f),
                modifier = Modifier.padding(end = HeadingEndPadding),
            )
        }
        if (text.comment.isNotEmpty()) {
            FadingText(
                text = text.comment,
                style = MaterialTheme.typography.bodyS,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = COMMENT_MAX_LINES,
                fade = TextFade.Bottom(fadeFrom = 0.55f),
                modifier = Modifier.padding(top = WishAppSpacing.s1),
            )
        }
        if (text.links != null) {
            LinkLine(line = text.links, modifier = Modifier.padding(top = WishAppSpacing.s2))
        }
        if (model.hasTags) {
            TagsLine(
                shownTag = model.shownTag,
                otherTagsCount = model.otherTagsCount,
                modifier = Modifier.padding(top = WishAppSpacing.s2),
            )
        }
    }
}

@Composable
private fun CardPhoto(photos: List<Any>, isSelected: Boolean) {
    // У выбранной счётчик уходит левее галочки: правый край в 36 dp от края карточки
    val counterEnd by animateDpAsState(
        targetValue = if (isSelected) CounterEndSelected else SelectedCheckInset,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "photoCounterEnd",
    )
    // Квадрат занят сразу: пока фото грузится, виден фон карточки, и она не прыгает
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
    ) {
        WishPhoto(photos = photos, modifier = Modifier.matchParentSize())
        if (photos.size > 1) {
            // Счётчик считает все фото, в том числе не открывшиеся
            Text(
                text = pluralStringResource(R.plurals.photo_count, photos.size, photos.size),
                style = MaterialTheme.typography.labelS,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = WishAppSpacing.s2, end = counterEnd)
                    .background(PhotoCounterScrim, CircleShape)
                    .padding(horizontal = WishAppSpacing.s2, vertical = 2.dp),
            )
        }
    }
}

private val CounterEndSelected = 36.dp

// Подложка счётчика поверх фото: читается на любом снимке, от темы не зависит
private val PhotoCounterScrim = Color(0xAD140C08)
