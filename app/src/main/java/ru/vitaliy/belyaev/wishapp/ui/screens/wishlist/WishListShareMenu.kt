package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.dock.FloatingDockHeight
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.components.header.headerHeight
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

private val ShareButtonShape = RoundedCornerShape(18.dp)

/** Сдвиг между появлением подписи и кнопок, по очереди от кнопки, которой меню открыли. */
private const val STAGGER_MILLIS = 40
private const val SCRIM_MILLIS = 200
private const val POP_MILLIS = 320
private const val POP_FADE_MILLIS = 160
private const val POP_EXIT_MILLIS = 140
private const val POP_START_SCALE = 0.85f
private const val POP_END_SCALE = 0.95f

/** Съезжают от кнопки на треть своей высоты. */
private const val POP_SLIDE_PART = 3
private val TopEndOrigin = TransformOrigin(1f, 0f)
private val BottomEndOrigin = TransformOrigin(1f, 1f)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/**
 * Меню «Поделиться» варианта K (`hk-smenu--top`): затемнение и столбик кнопок справа под шапкой.
 * Сверху подпись, что уходит: «Хотелки: 12 хотелок». Пока PDF собирается, вместо его кнопки
 * неактивная строка с прогрессом, меню закрывается, когда файл отдан системе.
 *
 * Меню выходит оттуда, где нажали «Поделиться» ([anchor]). Из шапки оно выпадает из правого
 * верхнего угла, из панели выбора поднимается над ней от правого края: палец не едет через весь
 * экран. Кнопки проявляются, чуть съезжают от угла и подрастают по очереди, начиная с ближней к
 * этому углу. Закрываются все вместе, быстрее.
 *
 * [onPickPart] — пункт «Выбрать только часть»: открывает выбор, где отмечено всё. Без него пункта
 * нет: в режиме выбора и так уходит часть.
 */
@Composable
internal fun ShareMenu(
    visible: Boolean,
    anchor: ShareMenuAnchor,
    scope: String,
    preparing: ShareFormat?,
    onShareText: () -> Unit,
    onSharePdf: () -> Unit,
    onShareImage: () -> Unit,
    onPickPart: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    BackHandler(enabled = visible, onBack = onDismiss)
    AnimatedVisibility(
        visible = visible,
        // Сам слой не анимируется: затемнение и кнопки появляются каждое по-своему
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .animateEnterExit(enter = fadeIn(tween(SCRIM_MILLIS)), exit = fadeOut(tween(SCRIM_MILLIS)))
                    .background(sheetScrim())
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            val isTop = anchor == ShareMenuAnchor.TOP
            // Очередь появления идёт от угла, где была кнопка: сверху вниз или снизу вверх
            val count = if (onPickPart != null) ITEMS_WITH_PICK else ITEMS
            val pop: AnimatedVisibilityScope.(Int) -> Modifier = { index ->
                popFromCorner(order = if (isTop) index else count - 1 - index, fromTop = isTop)
            }
            Column(
                modifier = if (isTop) {
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = headerHeight(), end = WishAppSpacing.s3)
                } else {
                    // Над панелью выбора: её отступ от низа, высота и зазор 8 dp
                    Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(
                            end = WishAppSpacing.s3,
                            bottom =
                            WishAppSpacing.s3 + FloatingDockHeight + WishAppSpacing.s2,
                        )
                }.testTag(WishListTestTags.SHARE_MENU),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
            ) {
                Text(
                    text = scope,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier
                        .then(pop(0))
                        .background(MaterialTheme.colorScheme.inverseSurface, CircleShape)
                        .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s1),
                )
                // С пунктом выбора форматы сдвигаются в очереди на одно место
                val shift = if (onPickPart != null) 1 else 0
                if (onPickPart != null) {
                    ShareMenuButton(
                        text = stringResource(R.string.share_pick_part),
                        icon = R.drawable.ic_ms_checklist,
                        enabled = preparing == null,
                        onClick = onPickPart,
                        modifier = pop(1),
                    )
                }
                // Пока файл собирается, его кнопка пишет об этом, а остальные ждут
                ShareMenuButton(
                    text = stringResource(
                        if (preparing == ShareFormat.IMAGE) R.string.share_preparing_image else R.string.share_as_image,
                    ),
                    icon = R.drawable.ic_ms_image.takeIf { preparing != ShareFormat.IMAGE },
                    enabled = preparing == null,
                    onClick = onShareImage,
                    modifier = pop(1 + shift),
                )
                ShareMenuButton(
                    text = stringResource(
                        if (preparing ==
                            ShareFormat.PDF
                        ) {
                            R.string.share_generating_pdf_button_text
                        } else {
                            R.string.share_as_pdf
                        },
                    ),
                    icon = R.drawable.ic_ms_picture_as_pdf.takeIf { preparing != ShareFormat.PDF },
                    enabled = preparing == null,
                    onClick = onSharePdf,
                    modifier = pop(2 + shift),
                )
                ShareMenuButton(
                    text = stringResource(R.string.share_as_text),
                    icon = R.drawable.ic_ms_notes,
                    enabled = preparing == null,
                    onClick = onShareText,
                    modifier = pop(3 + shift),
                )
            }
        }
    }
}

/** Откуда открыли меню «Поделиться»: кнопка в шапке или в панели выбора внизу. */
internal enum class ShareMenuAnchor { TOP, BOTTOM }

/** Подпись, картинка, PDF и текст; с пунктом «Выбрать только часть» на один больше. */
private const val ITEMS = 4
private const val ITEMS_WITH_PICK = 5

/** Появление из правого угла, верхнего или нижнего, [order] — очередь от этого угла. */
private fun AnimatedVisibilityScope.popFromCorner(order: Int, fromTop: Boolean): Modifier {
    val delay = order * STAGGER_MILLIS
    val origin = if (fromTop) TopEndOrigin else BottomEndOrigin
    val direction = if (fromTop) -1 else 1
    return Modifier.animateEnterExit(
        enter = fadeIn(tween(POP_FADE_MILLIS, delayMillis = delay)) +
            scaleIn(
                animationSpec = tween(POP_MILLIS, delayMillis = delay, easing = EmphasizedDecelerate),
                initialScale = POP_START_SCALE,
                transformOrigin = origin,
            ) +
            slideInVertically(tween(POP_MILLIS, delayMillis = delay, easing = EmphasizedDecelerate)) {
                direction * it / POP_SLIDE_PART
            },
        exit = fadeOut(tween(POP_EXIT_MILLIS)) +
            scaleOut(tween(POP_EXIT_MILLIS), targetScale = POP_END_SCALE, transformOrigin = origin),
    )
}

@Composable
private fun ShareMenuButton(
    text: String,
    @DrawableRes icon: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ShareButtonShape,
        color = AppThemeExtras.colors.surfaceCard,
        contentColor = MaterialTheme.colorScheme.onSurface,
        // В тёмной теме кнопки с обводкой, как док: тень на тёмном почти не видна
        border = floatingDarkOutline(),
        modifier = modifier
            .height(48.dp)
            .tokenShadow(ShareButtonShape, WishAppElevation.dock(AppThemeExtras.isDark)),
    ) {
        Row(
            modifier = Modifier.padding(ShareButtonPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
        ) {
            if (icon != null) {
                Icon(painter = painterResource(icon), contentDescription = null, modifier = Modifier.size(18.dp))
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
            }
            Text(text = text, style = MaterialTheme.typography.label)
        }
    }
}

private val ShareButtonPadding = PaddingValues(start = 14.dp, end = 18.dp)
