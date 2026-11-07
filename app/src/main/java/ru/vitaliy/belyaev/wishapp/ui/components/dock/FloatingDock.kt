package ru.vitaliy.belyaev.wishapp.ui.components.dock

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.PaperTone
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.labelS
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

/** Какая вкладка панели выбрана. У ярлыка вкладка подписана его названием и окрашена его цветом. */
sealed interface DockTab {
    data object Wishes : DockTab
    data object Done : DockTab
    data class Tag(val title: String, val tone: PaperTone) : DockTab
}

private val DockHeight = 60.dp
private val TabIconWidth = 52.dp
private val TabIconHeight = 30.dp

/**
 * Подложка под панелью: фон экрана сгущается снизу, карточки уходят под панель мягче и не сливаются
 * с капсулой. Флаг на время подбора, лишнее уберём, когда решим.
 */
internal const val DOCK_FADE_ENABLED = true

/**
 * Тонкая обводка плавающих капсул в тёмной теме: панели и кнопок шапки. Там капсула и карточки
 * без ярлыка почти одного тона.
 */
internal const val FLOATING_DARK_OUTLINE_ENABLED = true

/**
 * Плавающая панель: «Хотелки», «Сбылось», «Ярлыки» и плюс. По макету `FloatingDock`: капсула `surface-card`
 * с тенью `shadow-dock`, рядом плюс `primary` 64 dp, который при нажатии скругляется сильнее.
 */
@Composable
fun FloatingDock(
    selected: DockTab,
    doneCount: Long,
    onWishesClick: () -> Unit,
    onDoneClick: () -> Unit,
    onTagsClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = AppThemeExtras.isDark
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(DockHeight)
                .tokenShadow(CircleShape, WishAppElevation.dock(isDark)),
            shape = CircleShape,
            color = AppThemeExtras.colors.surfaceCard,
            border = floatingDarkOutline(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DockTabItem(
                    label = stringResource(R.string.nav_wishes),
                    icon = R.drawable.ic_ms_favorite,
                    selectedIcon = R.drawable.ic_ms_favorite_filled,
                    isSelected = selected == DockTab.Wishes,
                    selectedBackground = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onWishesClick,
                    modifier = Modifier.weight(1f),
                )
                DockTabItem(
                    label = stringResource(R.string.nav_done),
                    icon = R.drawable.ic_ms_check_circle,
                    selectedIcon = R.drawable.ic_ms_check_circle_filled,
                    isSelected = selected == DockTab.Done,
                    selectedBackground = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedIconTint = MaterialTheme.colorScheme.onTertiaryContainer,
                    badge = doneCount.takeIf { it > 0 },
                    onClick = onDoneClick,
                    modifier = Modifier.weight(1f),
                )
                val tagTab = selected as? DockTab.Tag
                DockTabItem(
                    // Длинное название обрезается многоточием: полное видно в заголовке экрана
                    label = tagTab?.title ?: stringResource(R.string.nav_tags),
                    icon = R.drawable.ic_ms_label,
                    selectedIcon = R.drawable.ic_ms_label_filled,
                    isSelected = tagTab != null,
                    selectedBackground = tagTab?.let { AppThemeExtras.colors.paper(it.tone) }
                        ?: MaterialTheme.colorScheme.primaryContainer,
                    selectedIconTint = MaterialTheme.colorScheme.onSurface,
                    selectedOutline = tagTab != null,
                    onClick = onTagsClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        AddButton(onClick = onAddClick)
    }
}

@Composable
private fun DockTabItem(
    label: String,
    @DrawableRes icon: Int,
    @DrawableRes selectedIcon: Int,
    isSelected: Boolean,
    selectedBackground: Color,
    selectedIconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: Long? = null,
    selectedOutline: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(
        targetValue = if (isSelected) selectedBackground else Color.Transparent,
        animationSpec = tween(150),
        label = "tabBackground",
    )
    // Нажатие видно на капсуле значка, как у навигации Material 3, а не прямоугольником на всю вкладку
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = isSelected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(width = TabIconWidth, height = TabIconHeight)
                    .clip(CircleShape)
                    .background(background)
                    .indication(interactionSource, ripple())
                    .then(
                        if (isSelected && selectedOutline) {
                            Modifier.border(1.dp, colors.onSurface.copy(alpha = 0.18f), CircleShape)
                        } else {
                            Modifier
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (isSelected) selectedIcon else icon),
                    contentDescription = null,
                    tint = if (isSelected) selectedIconTint else colors.onSurfaceVariant,
                )
            }
            if (badge != null) {
                DoneBadge(
                    count = badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-4).dp, y = (-2).dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelS,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

/** Счётчик на «Сбылось» по макету `hk-tab__b`: 16 dp в высоту, цифры 10 sp по центру. */
@Composable
private fun DoneBadge(count: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(16.dp)
            .widthIn(min = 16.dp)
            .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelS.copy(
                fontSize = 10.sp,
                lineHeight = 10.sp,
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiary,
            maxLines = 1,
        )
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (isPressed) 32.dp else 24.dp,
        animationSpec = tween(200),
        label = "addCorner",
    )
    val shape = RoundedCornerShape(corner)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(DockHeight)
            .tokenShadow(shape, WishAppElevation.dock(AppThemeExtras.isDark)),
        shape = shape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_ms_add),
                contentDescription = stringResource(R.string.cd_add_wish),
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

/** Обводка плавающей капсулы при [FLOATING_DARK_OUTLINE_ENABLED]: 1 dp `outline` на 60%, только в тёмной теме. */
@Composable
internal fun floatingDarkOutline(): BorderStroke? = if (AppThemeExtras.isDark && FLOATING_DARK_OUTLINE_ENABLED) {
    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = DARK_OUTLINE_ALPHA))
} else {
    null
}

private const val DARK_OUTLINE_ALPHA = 0.6f

/**
 * Подложка под [FloatingDock] при [DOCK_FADE_ENABLED]: от прозрачного вверху до почти плотного фона
 * у нижнего края, по smoothstep. Ставится под панелью на всю ширину, высота с запасом над капсулой.
 */
@Composable
fun FloatingDockFade(modifier: Modifier = Modifier) {
    if (!DOCK_FADE_ENABLED) return
    val surface = MaterialTheme.colorScheme.surface
    val navBar = with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val brush = remember(surface) {
        val stops = (0..FADE_STEPS).map { i ->
            val u = i / FADE_STEPS.toFloat()
            u to surface.copy(alpha = FADE_BOTTOM_ALPHA * u * u * (3f - 2f * u))
        }
        Brush.verticalGradient(*stops.toTypedArray())
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(navBar + DockHeight + FadeAbove)
            .background(brush),
    )
}

/** Насколько подложка поднимается над панелью (вместе с её нижним отступом). */
private val FadeAbove = 36.dp
private const val FADE_BOTTOM_ALPHA = 0.92f
private const val FADE_STEPS = 8

/** Высота панели: под неё список оставляет место. */
val FloatingDockHeight = DockHeight
