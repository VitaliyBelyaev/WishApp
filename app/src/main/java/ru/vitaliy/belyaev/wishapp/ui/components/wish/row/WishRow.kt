package ru.vitaliy.belyaev.wishapp.ui.components.wish.row

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.wish.CompletedSticker
import ru.vitaliy.belyaev.wishapp.ui.components.wish.FadingText
import ru.vitaliy.belyaev.wishapp.ui.components.wish.HeadingEndPadding
import ru.vitaliy.belyaev.wishapp.ui.components.wish.LinkLine
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
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM

private const val TITLE_MAX_LINES = 3
private const val COMMENT_MAX_LINES = 2

// Скругление строки как в дизайн-системе: между radius-md и radius-lg, отдельного токена нет
private val RowShape = RoundedCornerShape(20.dp)
private val ThumbnailWidth = 84.dp

/**
 * Хотелка в виде списка. Логика та же, что у [WishCard]: заголовок, «Сбылось», ссылка, ярлык,
 * режим выбора и фраза для TalkBack. Спецификация: компонент `WishRow` в артефакте дизайн-системы.
 *
 * Отличия от карточки: миниатюра главного фото слева шириной 84 dp и по высоте текста (центр-кроп),
 * заголовок до 3 строк, комментарий под названием до 2 строк. Если есть только фото, вместо заголовка
 * серая подпись «Фото»: строка без текста выглядела бы сломанной.
 */
@Composable
fun WishRow(
    model: WishCardModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    selection: WishSelection = WishSelection.NONE,
) {
    val text = model.resolveText()
    WishItemFrame(
        model = model,
        shape = RowShape,
        selection = selection,
        description = wishDescription(model),
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier,
    ) {
        ThumbnailRowLayout(
            thumbnail = if (model.photos.isNotEmpty()) {
                { WishPhoto(photos = model.photos, modifier = Modifier.fillMaxSize()) }
            } else {
                null
            },
            body = { RowBody(model = model, text = text) },
        )
    }
}

@Composable
private fun RowBody(model: WishCardModel, text: WishItemText) {
    val colors = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = WishAppSpacing.s3, vertical = 10.dp)) {
        if (model.isCompleted) {
            CompletedSticker(modifier = Modifier.padding(bottom = WishAppSpacing.s2))
        }
        when {
            text.heading.isNotEmpty() -> FadingText(
                text = text.heading,
                style = MaterialTheme.typography.titleM,
                maxLines = TITLE_MAX_LINES,
                fade = TextFade.Bottom(fadeFrom = 0.6f),
                modifier = Modifier.padding(end = HeadingEndPadding),
            )
            model.photos.isNotEmpty() -> FadingText(
                text = stringResource(R.string.card_photo_only),
                style = MaterialTheme.typography.titleM.copy(fontWeight = FontWeight.Medium),
                color = colors.onSurfaceVariant,
                maxLines = 1,
                fade = TextFade.End,
                modifier = Modifier.padding(end = HeadingEndPadding),
            )
        }
        if (text.comment.isNotEmpty()) {
            FadingText(
                text = text.comment,
                style = MaterialTheme.typography.bodyS,
                color = colors.onSurfaceVariant,
                maxLines = COMMENT_MAX_LINES,
                fade = TextFade.Bottom(fadeFrom = 0.45f),
                modifier = Modifier.padding(top = 2.dp),
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

/**
 * Миниатюра слева по высоте текста. Высоту задаёт только текст: размер самой картинки на неё не влияет,
 * поэтому раскладка своя, а не `Row` с `IntrinsicSize`.
 */
@Composable
private fun ThumbnailRowLayout(thumbnail: (@Composable () -> Unit)?, body: @Composable () -> Unit) {
    Layout(
        content = {
            if (thumbnail != null) Box { thumbnail() }
            body()
        },
    ) { measurables, constraints ->
        val thumbWidth = if (thumbnail != null) ThumbnailWidth.roundToPx() else 0
        val bodyMeasurable = measurables.last()
        val bodyMaxWidth = if (constraints.hasBoundedWidth) {
            (constraints.maxWidth - thumbWidth).coerceAtLeast(0)
        } else {
            Constraints.Infinity
        }
        val bodyPlaceable = bodyMeasurable.measure(Constraints(maxWidth = bodyMaxWidth))
        val height = bodyPlaceable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        val thumbPlaceable = if (thumbnail != null) {
            measurables.first().measure(Constraints.fixed(thumbWidth, height))
        } else {
            null
        }
        val width = if (constraints.hasBoundedWidth) {
            constraints.maxWidth
        } else {
            thumbWidth + bodyPlaceable.width
        }
        layout(width, height) {
            thumbPlaceable?.place(0, 0)
            bodyPlaceable.place(thumbWidth, 0)
        }
    }
}
