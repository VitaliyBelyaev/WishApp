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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.components.dock.floatingDarkOutline
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

private val ShareButtonShape = RoundedCornerShape(18.dp)

/** Сдвиг между появлением подписи и кнопок сверху вниз. */
private const val STAGGER_MILLIS = 40
private const val SCRIM_MILLIS = 200
private const val POP_MILLIS = 320
private const val POP_FADE_MILLIS = 160
private const val POP_EXIT_MILLIS = 140
private const val POP_START_SCALE = 0.85f
private const val POP_END_SCALE = 0.95f

/** Съезжают вниз на треть своей высоты. */
private const val POP_SLIDE_PART = 3
private val TopEndOrigin = TransformOrigin(1f, 0f)
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/**
 * Меню «Поделиться» варианта K (`hk-smenu--top`): затемнение и столбик кнопок справа под шапкой.
 * Сверху подпись, что уходит: «Хотелки: 12 хотелок». Пока PDF собирается, вместо его кнопки
 * неактивная строка с прогрессом, меню закрывается, когда файл отдан системе.
 *
 * Кнопки вырастают из угла под «⋮», откуда меню и открыли: проявляются, чуть съезжают вниз и
 * подрастают, сверху вниз по очереди. Закрываются все вместе, быстрее.
 */
@Composable
internal fun ShareMenu(
    visible: Boolean,
    scope: String,
    isSharingPdf: Boolean,
    onShareText: () -> Unit,
    onSharePdf: () -> Unit,
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
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = headerHeight(), end = WishAppSpacing.s3),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(WishAppSpacing.s2),
            ) {
                Text(
                    text = scope,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier
                        .then(popFromCorner(order = 0))
                        .background(MaterialTheme.colorScheme.inverseSurface, CircleShape)
                        .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s1),
                )
                val pdfModifier = popFromCorner(order = 1)
                if (isSharingPdf) {
                    ShareMenuButton(
                        text = stringResource(R.string.share_generating_pdf_button_text),
                        icon = null,
                        enabled = false,
                        onClick = {},
                        modifier = pdfModifier,
                    )
                } else {
                    ShareMenuButton(
                        text = stringResource(R.string.share_as_pdf),
                        icon = R.drawable.ic_ms_picture_as_pdf,
                        onClick = onSharePdf,
                        modifier = pdfModifier,
                    )
                }
                ShareMenuButton(
                    text = stringResource(R.string.share_as_text),
                    icon = R.drawable.ic_ms_notes,
                    enabled = !isSharingPdf,
                    onClick = onShareText,
                    modifier = popFromCorner(order = 2),
                )
            }
        }
    }
}

/** Появление из правого верхнего угла, [order] — очередь сверху вниз. */
private fun AnimatedVisibilityScope.popFromCorner(order: Int): Modifier {
    val delay = order * STAGGER_MILLIS
    return Modifier.animateEnterExit(
        enter = fadeIn(tween(POP_FADE_MILLIS, delayMillis = delay)) +
            scaleIn(
                animationSpec = tween(POP_MILLIS, delayMillis = delay, easing = EmphasizedDecelerate),
                initialScale = POP_START_SCALE,
                transformOrigin = TopEndOrigin,
            ) +
            slideInVertically(tween(POP_MILLIS, delayMillis = delay, easing = EmphasizedDecelerate)) {
                -it /
                    POP_SLIDE_PART
            },
        exit = fadeOut(tween(POP_EXIT_MILLIS)) +
            scaleOut(tween(POP_EXIT_MILLIS), targetScale = POP_END_SCALE, transformOrigin = TopEndOrigin),
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
