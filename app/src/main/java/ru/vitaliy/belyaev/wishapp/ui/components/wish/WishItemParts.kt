package ru.vitaliy.belyaev.wishapp.ui.components.wish

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS

/*
 * Общие части карточки (WishCard) и строки (WishRow): что показать в заголовке, рамка с нажатием и выбором,
 * фото с запасным видом, наклейка, ссылка, ярлык и фраза для TalkBack.
 */

/** Режим выбора: вне его и у невыбранной меток нет, у выбранной обводка и галочка. */
enum class WishSelection { NONE, UNSELECTED, SELECTED }

/** Ссылки внизу: домен первой и сколько ещё. Без домена только `+N`, когда домен уже в заголовке. */
@Immutable
internal data class WishLinkLine(val host: String?, val moreCount: Int)

/** Текст карточки или строки: заголовок, комментарий под ним и строка ссылок. */
@Immutable
internal data class WishItemText(val heading: String, val comment: String, val links: WishLinkLine?)

/** Заголовок: название, иначе комментарий, иначе домен первой ссылки. */
internal fun WishCardModel.resolveText(): WishItemText {
    val linkLine = linkHost?.let { WishLinkLine(host = it, moreCount = linksCount - 1) }
    return when {
        title.isNotBlank() -> WishItemText(heading = title, comment = comment.trim(), links = linkLine)
        comment.isNotBlank() -> WishItemText(heading = comment, comment = "", links = linkLine)
        // Домен уже в заголовке: одна ссылка без строки внизу, несколько только с «+N»
        linkHost != null -> WishItemText(
            heading = linkHost,
            comment = "",
            links = if (linksCount > 1) WishLinkLine(host = null, moreCount = linksCount - 1) else null,
        )
        else -> WishItemText(heading = "", comment = "", links = null)
    }
}

internal val WishCardModel.hasTags: Boolean get() = shownTag != null || otherTagsCount > 0

// Отскок как у прототипа: cubic-bezier(.3, 1.4, .5, 1), 220 мс
private val PressEasing = CubicBezierEasing(0.3f, 1.4f, 0.5f, 1f)
private const val PRESSED_SCALE = 0.97f
private val SelectionBorderWidth = 3.dp

/**
 * Рамка карточки и строки: фон `paper-*`, нажатие с уменьшением, у выбранной обводка и галочка.
 *
 * Содержимое для TalkBack скрыто, вместо него читается одна фраза [description]. Галочка лежит внутри
 * карточки, своей цели нажатия у неё нет. У невыбранной в режиме выбора внешне ничего не меняется:
 * режим виден по шапке и панели действий, а TalkBack говорит «не выбрано».
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WishItemFrame(
    model: WishCardModel,
    shape: Shape,
    selection: WishSelection,
    description: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) PRESSED_SCALE else 1f,
        animationSpec = tween(durationMillis = 220, easing = PressEasing),
        label = "wishItemPress",
    )
    val stateText = when (selection) {
        WishSelection.NONE -> null
        WishSelection.UNSELECTED -> stringResource(R.string.a11y_not_selected)
        WishSelection.SELECTED -> stringResource(R.string.a11y_selected)
    }
    val clickLabel = when (selection) {
        WishSelection.NONE -> stringResource(R.string.a11y_action_open)
        WishSelection.UNSELECTED -> stringResource(R.string.a11y_action_select)
        WishSelection.SELECTED -> stringResource(R.string.a11y_action_unselect)
    }

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClickLabel = clickLabel,
                    onLongClickLabel = stringResource(R.string.a11y_action_select_many),
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .semantics {
                    contentDescription = description
                    if (stateText != null) stateDescription = stateText
                },
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = AppThemeExtras.colors.paper(model.tone),
                contentColor = colors.onSurface,
            ),
            elevation = CardDefaults.cardElevation(),
        ) {
            // На всю ширину, иначе обводка выбора сожмётся до ширины текста
            Box(modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}) { content() }
        }

        if (selection == WishSelection.SELECTED) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(SelectionBorderWidth, colors.primary, shape),
            )
            SelectedCheck(modifier = Modifier.align(Alignment.TopEnd).padding(SelectedCheckInset))
        }
    }
}

private val SelectedCheckSize = 22.dp
internal val SelectedCheckInset = 8.dp

/**
 * Отступ справа у заголовка в любом режиме. С ним галочка выбора не садится на конец первой строки,
 * а текст при выборе не переносится заново.
 */
internal val HeadingEndPadding = 16.dp

/** Галочка выбранной карточки: кружок `primary` в правом верхнем углу, у невыбранных меток нет. */
@Composable
private fun SelectedCheck(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(SelectedCheckSize)
            .background(colors.primary, CircleShape),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_check),
            contentDescription = null,
            tint = colors.onPrimary,
            modifier = Modifier.size(14.dp),
        )
    }
}

/**
 * Главное фото с центр-кропом. Если файл не открылся, берётся следующее живое фото,
 * а если живых нет, показывается [LostPhoto]. Пока фото грузится, виден фон карточки.
 */
@Composable
internal fun WishPhoto(photos: List<Any>, modifier: Modifier = Modifier) {
    var current by remember(photos) { mutableIntStateOf(0) }
    when {
        LocalInspectionMode.current -> {
            // В превью Android Studio картинки не грузятся: заливка вместо фото, «пропавшие» помечены в модели
            val live = photos.firstOrNull { it.toString() != PREVIEW_LOST_PHOTO }
            if (live == null) LostPhoto(modifier) else Box(modifier.background(PreviewPhotoColor))
        }
        current >= photos.size -> LostPhoto(modifier)
        else -> AsyncImage(
            model = photos[current],
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { current++ },
            modifier = modifier,
        )
    }
}

/** Модель фото, которое в превью рисуется как пропавшее. */
internal const val PREVIEW_LOST_PHOTO = "preview://lost"
private val PreviewPhotoColor = Color(0xFF9DB8C9)

/** Фото не загрузилось: фон карточки с лёгким затемнением и значок по центру, без текста. */
@Composable
private fun LostPhoto(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(colors.onSurface.copy(alpha = 0.07f)),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_broken_image),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
internal fun CompletedSticker(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s1),
        modifier = modifier
            .background(colors.tertiaryContainer, CircleShape)
            .padding(start = 6.dp, end = 10.dp, top = 3.dp, bottom = 3.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_check_circle_filled),
            contentDescription = null,
            tint = colors.onTertiaryContainer,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(R.string.nav_done),
            style = MaterialTheme.typography.labelS.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onTertiaryContainer,
        )
    }
}

/** Значок ссылки, домен целиком (длинный переносится, многоточия нет) и `+N`. */
@Composable
internal fun LinkLine(line: WishLinkLine, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_ms_link),
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(WishAppSpacing.s1))
        if (line.host != null) {
            Text(
                text = line.host,
                style = MaterialTheme.typography.labelS,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        if (line.moreCount > 0) {
            Text(
                text = if (line.host != null) " +${line.moreCount}" else "+${line.moreCount}",
                style = MaterialTheme.typography.labelS.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
                maxLines = 1,
            )
        }
    }
}

/**
 * Ярлык и `+N` в одну строку. Длинное имя растворяется у правого края, `+N` всегда виден.
 * Ярлык на карточке это только подпись, он не нажимается.
 */
@Composable
internal fun TagsLine(shownTag: String?, otherTagsCount: Int, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s1), modifier = modifier) {
        if (shownTag != null) {
            TagLabel(text = shownTag, modifier = Modifier.weight(1f, fill = false))
        }
        if (otherTagsCount > 0) {
            TagLabel(text = "+$otherTagsCount")
        }
    }
}

@Composable
private fun TagLabel(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .background(colors.onSurface.copy(alpha = 0.09f), CircleShape)
            .padding(horizontal = WishAppSpacing.s2, vertical = 2.dp),
    ) {
        FadingText(
            text = text,
            style = MaterialTheme.typography.labelS.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurface,
            maxLines = 1,
            fade = TextFade.End,
        )
    }
}

/** Куда растворяется не поместившийся текст. [Bottom] начинает растворение с доли высоты [fadeFrom]. */
internal sealed interface TextFade {
    data class Bottom(val fadeFrom: Float) : TextFade
    data object End : TextFade
}

private val EndFadeWidth = 20.dp

/**
 * Текст, который при нехватке места не обрезается многоточием, а растворяется.
 * Растворение включается, только если текст действительно не поместился.
 */
@Composable
internal fun FadingText(
    text: String,
    style: TextStyle,
    maxLines: Int,
    fade: TextFade,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    var isOverflowing by remember(text) { mutableStateOf(false) }
    Text(
        text = text,
        style = style,
        color = color,
        maxLines = maxLines,
        softWrap = fade !is TextFade.End,
        overflow = TextOverflow.Clip,
        onTextLayout = { isOverflowing = it.hasVisualOverflow },
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                if (!isOverflowing) return@drawWithContent
                val brush = when (fade) {
                    is TextFade.Bottom -> Brush.verticalGradient(
                        fade.fadeFrom to Color.Black,
                        1f to Color.Transparent,
                    )
                    TextFade.End -> Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent),
                        startX = size.width - EndFadeWidth.toPx(),
                        endX = size.width,
                    )
                }
                drawRect(brush = brush, blendMode = BlendMode.DstIn)
            },
    )
}

private const val COMMENT_SPOKEN_LENGTH = 60

/**
 * Фраза для TalkBack: заголовок или то, что вместо него, «Сбылось», фото, ярлыки и ссылки.
 * Комментарий под названием не читается, домены тоже.
 */
@Composable
internal fun wishDescription(model: WishCardModel): String {
    val heading = when {
        model.title.isNotBlank() -> model.title.trim()
        model.comment.isNotBlank() -> model.comment.trim().spokenStart()
        model.linkHost != null -> model.linkHost
        model.photos.isNotEmpty() -> stringResource(R.string.card_photo_only)
        else -> ""
    }
    val parts = listOfNotNull(
        heading.takeIf { it.isNotEmpty() },
        if (model.isCompleted) stringResource(R.string.nav_done) else null,
        when {
            model.photos.size > 1 -> pluralStringResource(R.plurals.photo_count, model.photos.size, model.photos.size)
            model.photos.size == 1 -> stringResource(R.string.a11y_card_with_photo)
            else -> null
        },
        when {
            model.shownTag != null && model.otherTagsCount > 0 ->
                stringResource(R.string.a11y_card_tag_more, model.shownTag, model.otherTagsCount)
            model.shownTag != null -> stringResource(R.string.a11y_card_tag, model.shownTag)
            model.otherTagsCount > 0 ->
                pluralStringResource(R.plurals.a11y_card_other_tags, model.otherTagsCount, model.otherTagsCount)
            else -> null
        },
        when {
            model.linksCount > 1 -> pluralStringResource(R.plurals.a11y_card_links, model.linksCount, model.linksCount)
            model.linksCount == 1 -> stringResource(R.string.a11y_card_has_link)
            else -> null
        },
    )
    return parts.joinToString(", ")
}

/** Начало длинного комментария по границе слова. */
private fun String.spokenStart(): String {
    if (length <= COMMENT_SPOKEN_LENGTH) return this
    val cut = lastIndexOf(' ', startIndex = COMMENT_SPOKEN_LENGTH).takeIf { it > 0 } ?: COMMENT_SPOKEN_LENGTH
    return substring(0, cut).trimEnd(' ', ',', '.', ':', ';')
}
