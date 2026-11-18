package ru.vitaliy.belyaev.wishapp.ui.screens.wish

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import kotlin.math.abs
import kotlin.math.floor
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.ImageRef
import ru.vitaliy.belyaev.wishapp.shared.domain.entity.TagEntity
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.components.wish.linkHost
import ru.vitaliy.belyaev.wishapp.ui.components.wish.toPaperTone
import ru.vitaliy.belyaev.wishapp.ui.screens.newwish.TagDot
import ru.vitaliy.belyaev.wishapp.ui.screens.photos.heroCacheKey
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.WishSheet
import ru.vitaliy.belyaev.wishapp.ui.screens.wishlist.sheetBottomPadding
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.titleM
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

/** Плавающие кнопки и капсулы: 48 dp на всех экранах (правило «Плавающие кнопки»). */
internal val FloatingButtonSize = 48.dp

/** Насколько контент наезжает на фото: скругление верха `radius-xl`. */
internal val SheetOverlap = 32.dp

/** «Фото» на снимке: компактнее плавающих кнопок, чтобы не спорила с фото. */
private val PhotoCapsuleHeight = 40.dp

/** Подложка кнопок поверх фото: чёрная полупрозрачная, без обводки и размытия. */
private val OnPhotoScrim = Color.Black.copy(alpha = 0.55f)

private val ActionButtonHeight = 52.dp
private val ActionInnerCorner = 12.dp
private val TagChipHeight = 36.dp
private val LinkRowHeight = 56.dp
private const val MAX_DOTS = 5

internal object WishScreenTestTags {
    const val SCREEN = "wish_screen"
    const val TITLE = "wish_title"
    const val COMMENT = "wish_comment"
    const val MENU = "wish_menu"
    const val CAME_TRUE = "wish_came_true"
    const val WISH_AGAIN = "wish_again"
    const val ADD_TAG = "wish_add_tag"
    const val PHOTO = "wish_photo"
}

/** Круглая кнопка `surface-card` с тенью, в тёмной теме с обводкой. */
@Composable
internal fun FloatingCircleButton(
    @DrawableRes icon: Int,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .size(FloatingButtonSize)
            .tokenShadow(CircleShape, WishAppElevation.dock(AppThemeExtras.isDark))
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = AppThemeExtras.colors.surfaceCard,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = floatingDarkOutline(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(icon), contentDescription = null)
        }
    }
}

/** «Добавить фото» у хотелки без фото: капсула на линии кнопок, как они, `surface-card` с тенью. */
@Composable
internal fun AddPhotoCapsule(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(FloatingButtonSize)
            .tokenShadow(CircleShape, WishAppElevation.dock(AppThemeExtras.isDark)),
        shape = CircleShape,
        color = AppThemeExtras.colors.surfaceCard,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = floatingDarkOutline(),
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            Icon(painter = painterResource(R.drawable.ic_ms_add_photo_alternate), contentDescription = null)
            Text(text = stringResource(R.string.photo_add), style = MaterialTheme.typography.label)
        }
    }
}

/**
 * Фото хотелки: листаются, нажатие открывает просмотр. Пока фото на первом плане, видны точки и «Фото»,
 * при сворачивании они гаснут: [fade] от 1 до 0.
 */
@Composable
internal fun WishPhotos(
    images: List<ImageRef>,
    pagerState: PagerState,
    onPhotoClick: (Int) -> Unit,
    onAddPhoto: () -> Unit,
    fade: () -> Float,
    hiddenImageId: () -> String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(modifier = modifier.clipToBounds()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), key = { images[it].id }) { page ->
            val image = images[page]
            // Свой ключ кэша: переход в просмотр берёт эту же картинку сразу, без загрузки
            val request = remember(image.id) {
                ImageRequest.Builder(context).data(image).memoryCacheKey(heroCacheKey(image.id)).build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    // Открытое в просмотре фото прячется: просмотр вырастает из его места и сжимается обратно
                    .graphicsLayer { alpha = if (hiddenImageId() == image.id) 0f else 1f }
                    .testTag(WishScreenTestTags.PHOTO)
                    .clickable { onPhotoClick(page) },
            )
        }
        val bottom = SheetOverlap + WishAppSpacing.s3
        if (images.size > 1) {
            PhotoDots(
                count = images.size,
                position = { pagerState.currentPage + pagerState.currentPageOffsetFraction },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottom + 11.dp)
                    .graphicsLayer { alpha = fade() },
            )
        }
        Surface(
            onClick = onAddPhoto,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = WishAppSpacing.s3, bottom = bottom)
                .height(PhotoCapsuleHeight)
                .graphicsLayer { alpha = fade() },
            shape = CircleShape,
            color = OnPhotoScrim,
            contentColor = Color.White,
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, end = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_ms_add_photo_alternate),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(text = stringResource(R.string.chip_photo), style = MaterialTheme.typography.label)
            }
        }
    }
}

/**
 * Точки фото: не больше пяти на виду, лента едет за пальцем. Подсветка перетекает с точки на точку,
 * пока листаешь, а не прыгает. В середине длинной ленты подсветка стоит в центре, а точки текут под ней.
 * Крайние точки мельчают, если за ними есть ещё фото, у самого края ленты они полного размера.
 * Рисуется целиком в фазе отрисовки: листание не перестраивает композицию.
 */
@Composable
private fun PhotoDots(count: Int, position: () -> Float, modifier: Modifier = Modifier) {
    val slots = minOf(count, MAX_DOTS)
    val description = pluralStringResource(R.plurals.photo_count, count, count)
    val width = DotStep * (slots - 1) + DotActiveWidth
    Canvas(
        modifier = modifier
            .background(OnPhotoScrim, CircleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .size(width = width, height = DotSize)
            .semantics { contentDescription = description },
    ) {
        val p = position().coerceIn(0f, (count - 1).toFloat())
        // Начало окна: подсветка идёт по первым точкам, потом в центре, у конца снова идёт к краю
        val start = (p - (slots - 1) / 2f).coerceIn(0f, (count - slots).toFloat())
        val hiddenLeft = start.coerceAtMost(1f)
        val hiddenRight = (count - slots - start).coerceIn(0f, 1f)
        val step = DotStep.toPx()
        val dot = DotSize.toPx()
        val firstX = DotActiveWidth.toPx() / 2
        val from = floor(start).toInt() - 1
        for (i in from.coerceAtLeast(0)..minOf(count - 1, from + slots + 2)) {
            val slot = i - start
            val scale = minOf(edgeScale(slot, hiddenLeft), edgeScale(slots - 1 - slot, hiddenRight))
            if (scale <= 0f) continue
            val active = (1f - abs(i - p)).coerceIn(0f, 1f)
            val w = (dot + (DotActiveWidth.toPx() - dot) * active) * scale
            val h = dot * scale
            val cx = firstX + slot * step
            drawRoundRect(
                color = Color.White.copy(alpha = DOT_ALPHA + (1f - DOT_ALPHA) * active),
                topLeft = Offset(cx - w / 2, (size.height - h) / 2),
                size = Size(w, h),
                cornerRadius = CornerRadius(h / 2),
            )
        }
    }
}

/**
 * Размер точки у края окна: [slot] — насколько точка внутри окна от этого края (0 — на крае,
 * отрицательное — уже за ним). [hidden] от 0 до 1 — есть ли за краем ещё фото.
 */
private fun edgeScale(slot: Float, hidden: Float): Float {
    val atEdge = 1f - (1f - DOT_EDGE_SCALE) * hidden
    return when {
        slot >= 1f -> 1f
        slot >= 0f -> atEdge + (1f - atEdge) * slot
        else -> (atEdge * (1f + slot)).coerceAtLeast(0f)
    }
}

private val DotSize = 6.dp
private val DotActiveWidth = 14.dp

/** Шаг между центрами точек: подсветке 14 dp хватает места между соседями. */
private val DotStep = 13.dp
private const val DOT_ALPHA = 0.6f
private const val DOT_EDGE_SCALE = 0.55f

/** Ярлыки хотелки: показанный на карточке залит своим цветом, остальные с обводкой, в конце «+ Ярлык». */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WishTags(
    tags: List<TagEntity>,
    shownTagId: String?,
    onTagClick: (TagEntity) -> Unit,
    onAddTag: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag ->
            val isShown = tag.id == shownTagId
            TagChip(
                text = tag.title,
                fill = if (isShown) AppThemeExtras.colors.paper(tag.color.toPaperTone()) else null,
                onClick = { onTagClick(tag) },
            )
        }
        TagChip(
            text = stringResource(R.string.chip_tag),
            fill = null,
            icon = R.drawable.ic_ms_add,
            onClick = onAddTag,
            testTag = WishScreenTestTags.ADD_TAG,
        )
    }
}

@Composable
private fun TagChip(
    text: String,
    fill: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    testTag: String? = null,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(TagChipHeight)
            .clip(CircleShape)
            .then(if (fill != null) Modifier.background(fill) else Modifier.border(1.dp, colors.outline, CircleShape))
            .clickable(role = Role.Button, onClick = onClick)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(start = if (icon != null) 8.dp else 12.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s1),
    ) {
        if (icon != null) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.label.copy(fontSize = MaterialTheme.typography.labelS.fontSize),
            color = colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Ссылка: строка открывает браузер, «⋮» даёт «Скопировать», «Изменить», «Удалить». */
@Composable
internal fun LinkRow(link: String, onOpen: () -> Unit, onMenu: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val host = remember(link) { linkHost(link).ifEmpty { link } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(LinkRowHeight)
            .clip(MaterialTheme.shapes.medium)
            .background(colors.surfaceContainerLow)
            .clickable(onClickLabel = stringResource(R.string.open_link), onClick = onOpen)
            .padding(start = WishAppSpacing.s3, end = WishAppSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = host,
                style = MaterialTheme.typography.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // Адрес в малой строке обрезается: это единственное допустимое многоточие
            Text(
                text = link.substringAfter("://"),
                style = MaterialTheme.typography.labelS,
                fontWeight = FontWeight.Normal,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(onClick = onMenu) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_more_vert),
                contentDescription = stringResource(R.string.cd_more),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Главные действия под пальцем: «Открыть ссылку» и «Сбылось», у сбывшейся «Снова хочу».
 * Кнопки группой: внешние углы круглые, внутренние 12 dp.
 */
@Composable
internal fun WishActions(
    isCompleted: Boolean,
    hasLink: Boolean,
    onOpenLink: () -> Unit,
    onCameTrue: () -> Unit,
    onWishAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s1)) {
        when {
            isCompleted -> ActionButton(
                text = stringResource(R.string.wish_again),
                icon = R.drawable.ic_ms_favorite,
                container = colors.surfaceContainerHigh,
                content = colors.onSurface,
                shape = CircleShape,
                onClick = onWishAgain,
                modifier = Modifier.weight(1f).testTag(WishScreenTestTags.WISH_AGAIN),
            )
            else -> {
                if (hasLink) {
                    ActionButton(
                        text = stringResource(R.string.open_link),
                        icon = R.drawable.ic_ms_arrow_outward,
                        container = colors.primary,
                        content = colors.onPrimary,
                        shape = groupShape(start = true),
                        onClick = onOpenLink,
                        modifier = Modifier.weight(1.5f),
                    )
                }
                ActionButton(
                    text = stringResource(R.string.action_came_true),
                    icon = R.drawable.ic_ms_check_circle,
                    container = colors.tertiaryContainer,
                    content = colors.onTertiaryContainer,
                    shape = if (hasLink) groupShape(start = false) else CircleShape,
                    onClick = onCameTrue,
                    modifier = Modifier.weight(1f).testTag(WishScreenTestTags.CAME_TRUE),
                )
            }
        }
    }
}

private fun groupShape(start: Boolean): RoundedCornerShape {
    val outer = CornerSize(percent = 50)
    val inner = CornerSize(ActionInnerCorner)
    return if (start) RoundedCornerShape(outer, inner, inner, outer) else RoundedCornerShape(inner, outer, outer, inner)
}

@Composable
private fun RowScope.ActionButton(
    text: String,
    @DrawableRes icon: Int,
    container: Color,
    content: Color,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(ActionButtonHeight)
            .tokenShadow(shape, WishAppElevation.dock(AppThemeExtras.isDark)),
        shape = shape,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = WishAppSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(text = text, style = MaterialTheme.typography.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Строка шторки действий, 52 dp. [isDanger] красит её в `error`. */
@Composable
internal fun SheetActionRow(
    text: String,
    @DrawableRes icon: Int,
    onClick: (() -> Unit)?,
    hint: String? = null,
    isDanger: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val color = when {
        isDanger -> colors.error
        onClick == null -> colors.onSurfaceVariant
        else -> colors.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = WishAppSpacing.s3, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s3),
    ) {
        Icon(painter = painterResource(icon), contentDescription = null, tint = color)
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = MaterialTheme.typography.body, color = color)
            if (hint != null) {
                Text(text = hint, style = MaterialTheme.typography.labelS, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** Нажатие на ярлык: «Показывать на карточке» и «Убрать с этой хотелки». */
@Composable
internal fun TagActionsSheet(
    tag: TagEntity,
    isShown: Boolean,
    onShow: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    WishSheet(onDismiss = onDismiss) {
        SheetTitle(text = tag.title) { TagDot(AppThemeExtras.colors.paper(tag.color.toPaperTone())) }
        if (isShown) {
            SheetActionRow(text = stringResource(R.string.tag_shown), icon = R.drawable.ic_ms_check, onClick = null)
        } else {
            SheetActionRow(
                text = stringResource(R.string.tag_show),
                icon = R.drawable.ic_ms_label,
                hint = stringResource(R.string.tag_show_hint),
                onClick = onShow,
            )
        }
        SheetActionRow(
            text = stringResource(R.string.tag_remove),
            icon = R.drawable.ic_ms_close,
            isDanger = true,
            onClick = onRemove,
        )
        SheetEnd()
    }
}

/** «⋮» у ссылки. */
@Composable
internal fun LinkActionsSheet(
    link: String,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    WishSheet(onDismiss = onDismiss) {
        SheetTitle(text = linkHost(link).ifEmpty { link })
        SheetActionRow(
            text = stringResource(R.string.link_copy),
            icon = R.drawable.ic_ms_content_copy,
            onClick = onCopy,
        )
        SheetActionRow(text = stringResource(R.string.link_edit), icon = R.drawable.ic_ms_edit, onClick = onEdit)
        SheetActionRow(
            text = stringResource(R.string.delete),
            icon = R.drawable.ic_ms_delete,
            isDanger = true,
            onClick = onRemove,
        )
        SheetEnd()
    }
}

@Composable
private fun SheetTitle(text: String, leading: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier.padding(start = WishAppSpacing.s1, bottom = WishAppSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
    ) {
        leading?.invoke()
        Text(text = text, style = MaterialTheme.typography.titleM, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SheetEnd() {
    Box(modifier = Modifier.height(sheetBottomPadding()))
}

/** Отступ снизу под кнопками действий: их высота и зазор 12 dp. */
internal val ActionsSpace: Dp = ActionButtonHeight + WishAppSpacing.s3
