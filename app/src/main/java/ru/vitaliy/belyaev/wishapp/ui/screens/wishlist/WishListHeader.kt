package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.layout.layout
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.body
import ru.vitaliy.belyaev.wishapp.ui.theme.displayL
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

/** Высота строки шапки под статус-баром. На столько же сдвинуто начало списка. */
internal val HeaderRowHeight: Dp = 56.dp

/** Сколько надо прокрутить, чтобы заголовок ушёл, а кнопки оказались в капсуле. */
internal val HeaderCollapseDistance: Dp = 72.dp

/** Кнопки шапки крупнее стандартных 44 dp, как в тулбаре системных экранов Samsung. */
private val HeaderButtonSize = 48.dp

/** Зазор между капсулой кнопок и меню «⋮», чтобы меню не прилипало к капсуле. */
private val MenuGap = 8.dp

/** Точка «сортировка не по умолчанию» на значке пункта меню: меньше точки на «⋮», значок там 18 dp. */
private val MenuDotSize = 7.dp

/**
 * Шапка варианта K: крупный заголовок слева, кнопки справа. При прокрутке заголовок уходит вверх и гаснет,
 * а кнопки собираются в капсулу `surface-card`. Сверху лёгкая подложка цвета фона, чтобы карточки
 * не спорили с кнопками.
 *
 * @param progress от 0 (начало списка) до 1 (прокручено на [HeaderCollapseDistance] и больше).
 */
@Composable
internal fun WishListHeader(
    title: String,
    isTagTitle: Boolean,
    progress: () -> Float,
    showShareIcon: Boolean,
    menu: HeaderMenuState,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        HeaderScrim(progress)
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .height(HeaderRowHeight)
                .padding(start = WishAppSpacing.s4, end = WishAppSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val shift = with(LocalDensity.current) { 14.dp.toPx() }
            Text(
                text = title,
                style = if (isTagTitle) {
                    MaterialTheme.typography.displayL.copy(fontSize = 20.sp, lineHeight = 26.sp)
                } else {
                    MaterialTheme.typography.displayL
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
                    .graphicsLayer {
                        val p = progress()
                        alpha = (1f - p * 2.5f).coerceIn(0f, 1f)
                        translationY = -shift * p
                    },
            )
            Spacer(modifier = Modifier.size(WishAppSpacing.s2))
            HeaderButtons(
                progress = progress,
                showShareIcon = showShareIcon,
                menu = menu,
                onSearchClick = onSearchClick,
                onShareClick = onShareClick,
            )
        }
    }
}

/**
 * Затемнение у статус-бара, как у системных экранов Samsung: самое плотное у верхнего края,
 * гаснет уже внутри статус-бара и кончается на [ScrimTail] ниже него. Строку кнопок оно почти
 * не задевает, под капсулой контент читается. Затухание по smoothstep, без заметной границы.
 */
@Composable
private fun HeaderScrim(progress: () -> Float) {
    val surface = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current
    val statusBar = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    val total = statusBar + ScrimTail
    val plateau = (statusBar * ScrimPlateau / total).coerceIn(0f, 1f)
    val brush = remember(surface, plateau) {
        val stops = buildList {
            add(0f to surface.copy(alpha = ScrimTopAlpha))
            for (i in 0..ScrimSteps) {
                val u = i / ScrimSteps.toFloat()
                val fade = 1f - u * u * (3f - 2f * u)
                add(plateau + (1f - plateau) * u to surface.copy(alpha = ScrimTopAlpha * fade))
            }
        }
        Brush.verticalGradient(*stops.toTypedArray())
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(total)
            .graphicsLayer { alpha = (progress() * 3f).coerceIn(0f, 1f) }
            .background(brush),
    )
}

/** Насколько затемнение заходит ниже статус-бара. */
private val ScrimTail = 20.dp

/** Какая доля статус-бара сверху закрыта плотно, дальше начинается затухание. */
private const val ScrimPlateau = 0.4f
private const val ScrimTopAlpha = 0.94f
private const val ScrimSteps = 8

@Composable
private fun HeaderButtons(
    progress: () -> Float,
    showShareIcon: Boolean,
    menu: HeaderMenuState,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val capsule = CircleShape
    var isMenuExpanded by remember { mutableStateOf(false) }
    Box {
        // Капсула под кнопками проявляется при прокрутке отдельно от них, чтобы тень не гасла рывком.
        // Полупрозрачный слой рисуется в отдельный буфер размером со слой: тень за его краями
        // обрезалась, и при неполной прозрачности был виден прямоугольник. Поэтому слой шире капсулы
        // на запас под тень, а капсула внутри него с отступом
        Box(
            modifier = Modifier
                .matchParentSize()
                .outset(CapsuleShadowRoom)
                .graphicsLayer { alpha = (progress() * 3f).coerceIn(0f, 1f) }
                .padding(CapsuleShadowRoom)
                .tokenShadow(capsule, WishAppElevation.dock(AppThemeExtras.isDark))
                .background(AppThemeExtras.colors.surfaceCard, capsule)
                .then(floatingDarkOutline()?.let { Modifier.border(it, capsule) } ?: Modifier),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
            HeaderIconButton(R.drawable.ic_ms_search, stringResource(R.string.cd_search), onSearchClick)
            if (showShareIcon) {
                HeaderIconButton(R.drawable.ic_ms_share, stringResource(R.string.menu_share), onShareClick)
            }
            Box {
                Box {
                    HeaderIconButton(R.drawable.ic_ms_more_vert, stringResource(R.string.cd_more)) {
                        isMenuExpanded = true
                    }
                    if (menu.showSortDot) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .testTag(WishListTestTags.SORT_DOT)
                                .offset(x = (-10).dp, y = 10.dp)
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                        )
                    }
                }
                HeaderOverflowMenu(menu, isMenuExpanded) { isMenuExpanded = false }
            }
        }
    }
}

/** Запас под тень капсулы `shadow-dock`: размытие 20 dp и сдвиг 6 dp. */
private val CapsuleShadowRoom = 32.dp

/** Растягивает элемент на [extra] во все стороны за его место в раскладке. */
private fun Modifier.outset(extra: Dp): Modifier = layout { measurable, constraints ->
    val extraPx = extra.roundToPx()
    val placeable = measurable.measure(
        Constraints.fixed(constraints.maxWidth + extraPx * 2, constraints.maxHeight + extraPx * 2),
    )
    layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-extraPx, -extraPx) }
}

@Composable
private fun HeaderIconButton(@DrawableRes icon: Int, description: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(HeaderButtonSize)) {
        Icon(painter = painterResource(icon), contentDescription = description)
    }
}

/** Что показывает меню «⋮». Пустой раздел оставляет только «Настройки». */
internal class HeaderMenuState(
    val showShare: Boolean,
    val shareHint: String?,
    val sortLabel: String?,
    val showSortDot: Boolean,
    val layoutLabel: String?,
    @DrawableRes val layoutIcon: Int,
    val onShare: () -> Unit,
    val onSort: () -> Unit,
    val onToggleLayout: () -> Unit,
    val onSettings: () -> Unit,
)

/**
 * Меню «⋮» по макету `hk-dmenu`: пункты 48 dp, текст 14 sp 500, подсказка справа в строке.
 */
@Composable
private fun HeaderOverflowMenu(menu: HeaderMenuState, isExpanded: Boolean, onDismiss: () -> Unit) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(0.dp, MenuGap),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.widthIn(min = 252.dp),
    ) {
        val close: (() -> Unit) -> Unit = { action ->
            onDismiss()
            action()
        }
        if (menu.showShare) {
            MenuItem(R.drawable.ic_ms_share, stringResource(R.string.menu_share), menu.shareHint) { close(menu.onShare) }
        }
        menu.sortLabel?.let { sortLabel ->
            // Точка как на «⋮»: показывает, к какому пункту она относится
            MenuItem(R.drawable.ic_ms_sort, stringResource(R.string.menu_sort), sortLabel, showDot = menu.showSortDot) {
                close(menu.onSort)
            }
        }
        menu.layoutLabel?.let { layoutLabel ->
            MenuItem(menu.layoutIcon, layoutLabel, null) { close(menu.onToggleLayout) }
        }
        MenuItem(R.drawable.ic_ms_settings, stringResource(R.string.settings), null) { close(menu.onSettings) }
    }
}

@Composable
private fun MenuItem(
    @DrawableRes icon: Int,
    text: String,
    hint: String?,
    showDot: Boolean = false,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.label.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
            )
        },
        leadingIcon = {
            Box {
                Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
                if (showDot) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .testTag(WishListTestTags.MENU_SORT_DOT)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(MenuDotSize)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
            }
        },
        trailingIcon = hint?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        },
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = WishAppSpacing.s3),
    )
}

/**
 * Шапка поиска: стрелка назад и поле. Клавиатура открывается сама, только если запрос пустой:
 * при возврате с экрана хотелки она не выскакивает.
 */
@Composable
internal fun SearchHeader(
    query: String,
    hint: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var requestedFocus by rememberSaveable { mutableStateOf(false) }
    // Текст живёт в поле: через поток ViewModel быстрый ввод терял буквы
    var text by rememberSaveable { mutableStateOf(query) }
    val updateText: (String) -> Unit = {
        text = it
        onQueryChange(it)
    }
    LaunchedEffect(Unit) {
        if (!requestedFocus && text.isEmpty()) {
            focusRequester.requestFocus()
        }
        requestedFocus = true
    }
    Box(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(HeaderRowHeight),
        )
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxWidth()
                .height(HeaderRowHeight)
                .padding(horizontal = WishAppSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderIconButton(R.drawable.ic_ms_arrow_back, stringResource(R.string.cd_close_search), onClose)
            Box(modifier = Modifier.weight(1f).padding(start = WishAppSpacing.s1), contentAlignment = Alignment.CenterStart) {
                if (text.isEmpty()) {
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = updateText,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.body.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
            if (text.isNotEmpty()) {
                HeaderIconButton(R.drawable.ic_ms_close, stringResource(R.string.cd_clear_search)) {
                    updateText("")
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
            }
        }
    }
}

/** Высота шапки вместе со статус-баром: отступ сверху для списка. */
@Composable
internal fun headerHeight(): Dp {
    val density = LocalDensity.current
    val statusBar = with(density) { WindowInsets.statusBars.getTop(this).toDp() }
    return statusBar + HeaderRowHeight
}
