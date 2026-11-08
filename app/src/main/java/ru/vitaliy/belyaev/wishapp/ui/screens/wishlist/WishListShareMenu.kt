package ru.vitaliy.belyaev.wishapp.ui.screens.wishlist

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.vitaliy.belyaev.wishapp.R
import ru.vitaliy.belyaev.wishapp.ui.theme.AppThemeExtras
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppElevation
import ru.vitaliy.belyaev.wishapp.ui.theme.WishAppSpacing
import ru.vitaliy.belyaev.wishapp.ui.theme.label
import ru.vitaliy.belyaev.wishapp.ui.theme.tokenShadow

private val ShareButtonShape = RoundedCornerShape(18.dp)
private val CaptionBackground = Color(0xB8140C08)

/** Сдвиг между появлением кнопок, снизу вверх, как в макете. */
private const val STAGGER_MILLIS = 35

/**
 * Меню «Поделиться» варианта K (`hk-smenu--top`): затемнение и столбик кнопок справа под шапкой.
 * Сверху подпись, что уходит: «Хотелки: 12 хотелок». Пока PDF собирается, вместо его кнопки
 * неактивная строка с прогрессом, меню закрывается, когда файл отдан системе.
 *
 * Кнопки «Выбрать только часть» пока нет: режим выбора появится в части 2.3.
 */
@Composable
internal fun ShareMenu(
    scope: String,
    isSharingPdf: Boolean,
    onShareText: () -> Unit,
    onSharePdf: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SheetScrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
    ) {
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
                color = Color.White,
                modifier = Modifier
                    .background(CaptionBackground, CircleShape)
                    .padding(horizontal = WishAppSpacing.s3, vertical = WishAppSpacing.s1),
            )
            Rising(order = 1) {
                if (isSharingPdf) {
                    ShareMenuButton(
                        text = stringResource(R.string.share_generating_pdf_button_text),
                        icon = null,
                        enabled = false,
                        onClick = {},
                    )
                } else {
                    ShareMenuButton(
                        text = stringResource(R.string.share_as_pdf),
                        icon = R.drawable.ic_ms_picture_as_pdf,
                        onClick = onSharePdf,
                    )
                }
            }
            Rising(order = 0) {
                ShareMenuButton(
                    text = stringResource(R.string.share_as_text),
                    icon = R.drawable.ic_ms_notes,
                    enabled = !isSharingPdf,
                    onClick = onShareText,
                )
            }
        }
    }
}

/** Кнопка поднимается на 24 dp и проявляется, нижние раньше верхних. */
@Composable
private fun Rising(order: Int, content: @Composable () -> Unit) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    val rise = with(LocalDensity.current) { 24.dp.roundToPx() }
    val delay = order * STAGGER_MILLIS
    AnimatedVisibility(
        visibleState = state,
        enter = fadeIn(tween(220, delayMillis = delay)) +
            slideInVertically(tween(280, delayMillis = delay)) { rise },
    ) {
        content()
    }
}

@Composable
private fun ShareMenuButton(
    text: String,
    @DrawableRes icon: Int?,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = ShareButtonShape,
        color = AppThemeExtras.colors.surfaceCard,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
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
